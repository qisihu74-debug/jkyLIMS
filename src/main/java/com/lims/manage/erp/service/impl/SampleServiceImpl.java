package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.SampleAddDetailVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SamplePublicInfoVo;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class SampleServiceImpl implements SampleService {
    @Autowired
    TestProductDao testProductDao;

    @Autowired
    SampleEntityMapper sampleEntityMapper;

    @Override
    public Integer addSampleData(SampleAddParamVo addParamVo, MultipartFile[] file) {
        int result = 0;
        String insertFlag = System.currentTimeMillis() + "";
        //查询产品名称
        String productName = testProductDao.getProductNameById(addParamVo.getSampleName());
        //处理详情数据
        List<SampleAddDetailVo> details = addParamVo.getDetails();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = Integer.parseInt(maxNumber);
        }
        if (details != null) {
            for (int i = 0; i < details.size(); i++) {
                //生成样品编号
                StringBuilder code = new StringBuilder("YP-" + sdf.format(now) + "-");
                String sampleCode;
                if (addParamVo.getSampleGroups() > 1) {
                    //组数大于1，将前n个命名为
                    if (i % addParamVo.getQuantityPerGroup() == 0) {
                        newMax = newMax + 1;
                    }
                    String num = String.format("%0" + 4 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i % addParamVo.getQuantityPerGroup() + 1);
                    sampleCode = prefix + "_" + suffix;
                } else {
                    //生成样品编号
                    String num = String.format("%0" + 4 + "d", newMax + 1);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i + 1);
                    if (addParamVo.getQuantityPerGroup() > 1) {
                        sampleCode = prefix + "_" + suffix;
                    } else {
                        sampleCode = prefix.toString();
                    }
                }
                //保存样品图片

                MultipartFile multipartFile = null;
                int pictureName = i + 1;
                String suffix = "";
                if (file.length > 0) {//有上传文件
                    for (int j = 0; j < file.length; j++) {
                        String originalFilename = file[j].getOriginalFilename();
                        String[] split = originalFilename.split("\\.");
                        if ((pictureName + "").equals(split[0])) {
                            suffix = split[1];
                            multipartFile = file[j];
                            break;
                        }
                    }
                }
                String pictureFileName = null;
                if (!"".equals(suffix)) {
                    pictureFileName = sampleCode + "." + suffix;
                }
                String pictureUrl = null;
                if (multipartFile != null) {
                    pictureUrl = MinIoUtil.upload("test-sample", multipartFile, pictureFileName);
                }
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo, details.get(i), productName, sampleCode, pictureUrl, insertFlag);
                result = sampleEntityMapper.insert(sampleEntity);
            }
        } else {
            StringBuilder code = new StringBuilder("YP-" + sdf.format(now) + "-");
            //生成样品编号
            String num = String.format("%0" + 4 + "d", newMax + 1);
            StringBuilder sampleCode = code.append(num);
            SampleEntity sampleEntity = new SampleEntity(addParamVo, null, productName, sampleCode.toString(), null, insertFlag);
            result = sampleEntityMapper.insert(sampleEntity);
        }
        return result;
    }

    @Override
    public List<SamplePublicInfoVo> getSamplePublicInfos(SampleEntity paramVo) {
        return sampleEntityMapper.getSamplePublicInfos(paramVo);
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public SampleDetailVo getSampleGroupInfo(String insertFlag) {
        return sampleEntityMapper.getSampleGroupInfo(insertFlag);
    }

    @Override
    public List<SampleDetailVo> selectSampleList2(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList2(sampleEntity);
    }

    @Override
    public Workbook getSampleTagInfo(Integer sampleId) {
        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        String fileName = "";
        if (sampleTagInfo != null) {
            fileName = sampleTagInfo.getSampleCode() + "样品标签.xlsx";
            result.put("result", sampleTagInfo);
        } else {
            return null;
        }
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
        Workbook workbook = null;
        try {
            workbook = transformer.transformXLS(fileStream, result);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return workbook;

//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        HttpHeaders headers = null;
//        try {
//            headers = getHttpHeaders("");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        byte[] bytes = null;
//        Workbook sheets = null;
//        try {
//            sheets = transformer.transformXLS(fileStream, result);
//            try {
//                sheets.write(outputStream);
//                bytes = outputStream.toByteArray();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }
//        if(bytes != null ){
//            return new ResponseEntity<>(bytes, headers, HttpStatus.CREATED);
//        }else{
//            return null;
//        }
    }
}
