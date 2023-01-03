package com.lims.manage.erp.service.impl;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Worksheet;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleCirculationRecord;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SysUserRoleDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.PDFHelper3;
import com.lims.manage.erp.util.QRCodeUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SampleAddDetailVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SampleEntrustAddVo;
import com.lims.manage.erp.vo.SamplePrivateInfoVo;
import com.lims.manage.erp.vo.SamplePublicInfoVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class SampleServiceImpl implements SampleService {
    @Autowired
    TestProductDao testProductDao;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    private EntrustEntityMapper mapper;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;

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
        Integer maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = maxNumber;
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
                    String num = String.format("%0" + 5 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i % addParamVo.getQuantityPerGroup() + 1);
                    sampleCode = prefix + "_" + suffix;
                } else {
                    //生成样品编号
                    String num = String.format("%0" + 5 + "d", newMax + 1);
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
                // 去除 Outward 标点符号(、)进行删除
                if (addParamVo.getOutward().length() == 1) {
                    // 赋值为空
                    addParamVo.setOutward(null);
                } else {
                    addParamVo.setOutward(addParamVo.getOutward().substring(1, addParamVo.getOutward().length()));
                }
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo, details.get(i), productName, sampleCode, pictureUrl, insertFlag);
                result = sampleEntityMapper.insert(sampleEntity);
            }
        } else {
            StringBuilder code = new StringBuilder("YP-" + sdf.format(now) + "-");
            //生成样品编号
            String num = String.format("%0" + 5 + "d", newMax + 1);
            StringBuilder sampleCode = code.append(num);
            SampleEntity sampleEntity = new SampleEntity(addParamVo, null, productName, sampleCode.toString(), null, insertFlag);
            result = sampleEntityMapper.insert(sampleEntity);
        }
        return result;
    }

    @Override
    public List<SamplePublicInfoVo> getSamplePublicInfos(SampleDetailVo paramVo) {
        if (paramVo.getReceivedDate() != null) {
            String[] split = paramVo.getReceivedDate().split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        return sampleEntityMapper.getSamplePublicInfos(paramVo);
    }

    @Override
    public PageInfo getSamplePublicInfos2(SampleDetailVo paramVo) {
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        if (paramVo.getReceivedDate() != null) {
            String[] split = paramVo.getReceivedDate().split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        List<SamplePublicInfoVo> list = sampleEntityMapper.getSamplePublicInfos1(paramVo);
        List<String> insertFlags = Lists.newArrayList();
        for (SamplePublicInfoVo vo : list) {
            insertFlags.add(vo.getInsertFlag());
        }
        List<SamplePrivateInfoVo> samplePrivateInfos1 = sampleEntityMapper.getSamplePrivateInfos1(insertFlags);
        for (SamplePublicInfoVo vo : list) {
            String insertFlag = vo.getInsertFlag();
            List<SamplePrivateInfoVo> childNode = Lists.newArrayList();
            for (SamplePrivateInfoVo privateInfoVo : samplePrivateInfos1) {
                String insertFlag1 = privateInfoVo.getInsertFlag();
                if (insertFlag.equals(insertFlag1)) {
                    childNode.add(privateInfoVo);
                }
                //TODO gjl添加样品状态
                EntrustServiceImpl service = new EntrustServiceImpl();
                String state = service.findStateBySampleId(privateInfoVo.getId(), mapper, taskMapper);
                privateInfoVo.setState(state);
            }
            vo.setChildNode(childNode);
        }
        PageInfo<SamplePublicInfoVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public PageInfo getSampleDataList2(SampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleEntity> list = sampleEntityMapper.selectSampleList(sampleEntity);
        List<SampleEntity> sampleEntities = sampleEntityMapper.selectSampleList(sampleEntity);
//        List<SampleEntrustAddVo> sampleEntrustAddVos = sampleEntityMapper.selectSampleList(sampleEntity);
        PageInfo<SampleEntity> pageInfo = new PageInfo<>(sampleEntities);
        return pageInfo;
    }

    @Override
    public PageInfo getSampleDataListNew(SampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleEntrustAddVo> topList = sampleEntityMapper.selectSampleListTop(sampleEntity);
        List<String> codes = Lists.newArrayList();
        for (SampleEntrustAddVo sampleEntrustAddVo : topList) {
            codes.add(sampleEntrustAddVo.getCode());
        }
        if (!CollectionUtils.isEmpty(codes)) {
            List<SampleEntity> groupNode = sampleEntityMapper.getGroupNode(codes);
            for (SampleEntrustAddVo sampleEntrustAddVo : topList) {
                List<SampleEntity> samples = Lists.newArrayList();
                for (SampleEntity sampleEntity1 : groupNode) {
                    if (sampleEntrustAddVo.getCode().equals(sampleEntity1.getSampleCode().substring(0, 12))) {
                        samples.add(sampleEntity1);
                    }
                }
                sampleEntrustAddVo.setSamples(samples);
            }
        }

        PageInfo<SampleEntrustAddVo> pageInfo = new PageInfo<>(topList);
        return pageInfo;
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
    public int updateSampleInfo(SampleEntity record) {
        return sampleEntityMapper.updateSampleInfo(record);
    }

    @Override
    public SampleDetailVo getSampleTagInfo(Integer sampleId) {
        return sampleEntityMapper.getSampleTagInfo(sampleId);
    }

    /**
     * 根据样品id 获取Excel表格集合 存储到zip里面。
     *
     * @return
     */
    @Override
    public ZipOutputStream packagingWorkbookZip(Integer sampleId, HttpServletResponse response) throws IOException {

        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        // 通过输入参数 返回 对应的处理成功的EXCEL数据。
        ServletOutputStream outputStream = response.getOutputStream();
        ZipOutputStream out = new ZipOutputStream(outputStream);
        // 查询样品标签时： 区别 原材 和 配合比验证。
        if (sampleTagInfo.getSampleType().equals("原材")) {
            methodSampleTypeOriginalMaterial(sampleTagInfo, out);
        } else {
            methodSampleTypeLineBlend(sampleTagInfo, out);
        }
        return out;
    }

    @Override
    public ServletOutputStream downloadNewSampleTab(Integer sampleId,SampleDetailVo sampleTagInfo, HttpServletResponse response) {
        ServletOutputStream outputStream = null;
        List<SampleDetailVo> sampleDetailVoList = new ArrayList<>();
        try {
            outputStream = response.getOutputStream();
            List<String> codeList = Lists.newArrayList();
            if (sampleTagInfo.getSampleType().equals("原材")) {
                if (sampleTagInfo != null) {
                    //样品编号格式处理：YP-2022-9200-01~03 情况2：YP-2022-0096
                    String[] sampleSplits = sampleTagInfo.getSampleCode().split("-");
                    //获取样品标签编号集合
                    String s = "";
                    if (sampleSplits.length > 3) {
                        String[] strings = sampleSplits[3].split("~");
                        int startNum = Integer.parseInt(strings[0].substring(1));
                        int endNum = Integer.parseInt(strings[1].substring(1));
                        int index = startNum;
                        for (int i = 0; i < endNum; i++) {
                            s = sampleSplits[0] + "-" + sampleSplits[1] + "-" + sampleSplits[2] + "-" + index;
                            codeList.add(s);
                            index++;
                        }
                    } else {
                        codeList.add(sampleTagInfo.getSampleCode());
                    }
                    // 处理样品描述信息 Outward、 outwardDescribe 组合输出
                    StringBuilder stringBuilder = new StringBuilder();
                    if (sampleTagInfo.getOutward() != null && sampleTagInfo.getOutward().length() > 0) {
                        stringBuilder.append(sampleTagInfo.getOutward() + ",");
                    }
                    if (sampleTagInfo.getOutwardDescribe() != null && sampleTagInfo.getOutwardDescribe().length() > 0) {
                        stringBuilder.append(sampleTagInfo.getOutwardDescribe() + ",");
                    }
                    if (stringBuilder.length() > 1) {
                        sampleTagInfo.setOutward(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                    } else {
                        sampleTagInfo.setOutward("");
                    }
                }
                sampleTagInfo.setCodeList(codeList);
                sampleDetailVoList.add(sampleTagInfo);
            }else {
                //获取配合比集合。
                sampleDetailVoList = sampleEntityMapper.getSampleTagInfoPidList(sampleTagInfo.getId());
            }
            //填充数据,sampleDetailVoList size=1根据codeList取编号，大于1直接取数据的编号
            PDFHelper3.getLicense();
            com.aspose.cells.Workbook newBook = new com.aspose.cells.Workbook();
            newBook.getWorksheets().clear();
            if (sampleDetailVoList.size()==1){
                SampleDetailVo sampleDetailVo = sampleDetailVoList.get(0);
                List<String> list = sampleDetailVo.getCodeList();
                for (int i=0;i<list.size();i++) {
                    //创建一个新的Excel文档
                    InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-tag.xlsx");
                    com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(fileStream);
                    Worksheet worksheet = workbook.getWorksheets().get(0);
                    //填充数据
                    worksheet.getCells().get("B2").setValue(list.get(i));
                    worksheet.getCells().get("B3").setValue(sampleDetailVo.getAliasName());
                    worksheet.getCells().get("B4").setValue(sampleDetailVo.getSpecs());
                    worksheet.getCells().get("B5").setValue(sampleDetailVo.getOutwardDescribe());
                    //设置二维码
                    BufferedImage bufferedImage = QRCodeUtil.getBufferedImage("https://hntri.hkgglclc.com/home/JkyErp/sp?id="+sampleDetailVo.getId());
                    InputStream stream = bufferedImageToInputStream(bufferedImage);
                    worksheet.getPictures().add(5,3,stream,30,30);
                    //合并sheet
                    Worksheet worksheetS = newBook.getWorksheets().add(list.get(i));
                    worksheetS.copy(worksheet);
                }
            }
            if (sampleDetailVoList.size()>1){
                for (int i=0;i<sampleDetailVoList.size();i++) {
                    //创建一个新的Excel文档
                    InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-tag.xlsx");
                    com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(fileStream);
                    Worksheet worksheet = workbook.getWorksheets().get(0);
                    //填充数据
                    worksheet.getCells().get("B2").setValue(sampleDetailVoList.get(i).getSampleCode());
                    worksheet.getCells().get("B3").setValue(sampleDetailVoList.get(i).getAliasName());
                    worksheet.getCells().get("B4").setValue(sampleDetailVoList.get(i).getSpecs());
                    worksheet.getCells().get("B5").setValue(sampleDetailVoList.get(i).getOutwardDescribe());
                    //设置二维码
                    BufferedImage bufferedImage = QRCodeUtil.getBufferedImage(sampleDetailVoList.get(i).getId() + "");
                    InputStream stream = bufferedImageToInputStream(bufferedImage);
                    worksheet.getPictures().add(5,3,stream,30,30);
                    //合并sheet
                    Worksheet worksheetS = newBook.getWorksheets().add(sampleDetailVoList.get(i).getSampleCode());
                    worksheetS.copy(worksheet);
                }
            }
            newBook.save(outputStream, SaveFormat.XLSX);
        }catch (Exception e){
            log.error("下载样品标签异常:{}",e);
        }
        return outputStream;
    }

    @Override
    public TestSampleEntity sampleInfo(Integer sampleId) {
        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        if (sampleTagInfo != null){
            TestSampleEntity entity = new TestSampleEntity();
            entity.setId(sampleId);
            entity.setSampleCode(sampleTagInfo.getSampleCode());
            entity.setSampleName(sampleTagInfo.getSampleName());
            entity.setSpecs(sampleTagInfo.getSpecs());
            entity.setOutwardDescribe(sampleTagInfo.getOutwardDescribe());
            //查询样品流转记录
            List<SampleCirculationRecord> list = sampleEntityMapper.getRecords(sampleId);
            entity.setCirculationCecords(list);
            //根据当前用户设置手机端的扫描操作状态
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            List<Long> integerList = Lists.newArrayList();
            integerList.add(0L);
            if (userInfo != null){
                //根据用户id获取所拥有的角色id，1领样，3留样，4处置，角色初始化时确定死
                List<Long> roles = sysUserRoleDao.getRoleIdsByUserId(userInfo.getUserId());
                for (Long id:roles) {
                    if (id==1 || id==3 || id==4){
                        integerList.add(id);
                    }
                }
                entity.setOperateType(integerList);
            }else {
                entity.setOperateType(integerList);
            }
            return entity;
        }else {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateState(Integer sampleId,Integer state) {
        //2领样，3留样，4处置
        List<Integer> ids = sampleEntityMapper.getExist(sampleId,state);
        if (ids != null && ids.size() >= 1){
            return false;
        }
        //更新样品表状态
        if (state == 2){
            sampleEntityMapper.updateSampleState(sampleId,state);
        }
        if (state >= 3){
            Integer status = null;
            if (state == 3){
                status =0;
            }
            if (state == 4){
                status = 1;
            }
            sampleEntityMapper.updateIsSave(sampleId,status);
        }
        //插入流转记录
        SampleCirculationRecord record = new SampleCirculationRecord();
        record.setSampleId(sampleId);
        record.setStatus(state+"");
        record.setTime(new Date());
        record.setOperatorId(ShiroUtils.getUserInfo().getUserId());
        record.setOperatorName(ShiroUtils.getUserInfo().getUsername());
        sampleEntityMapper.insertRecord(record);
        return true;
    }

    /**
     * 将BufferedImage转换为InputStream
     * @param image
     * @return
     */
    public InputStream bufferedImageToInputStream(BufferedImage image){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            return input;
        } catch (IOException e) {
            log.error("提示:",e);
        }
        return null;
    }

    /**
     * 处理样品类型为： 原材。 进行zip。
     *
     * @param sampleTagInfo
     * @param out
     */
    public void methodSampleTypeOriginalMaterial(SampleDetailVo sampleTagInfo, ZipOutputStream out) throws IOException {
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        if (sampleTagInfo != null) {
            // 样品编号格式： 情况1： YP-2022-0095（01~02） 情况2：YP-2022-0096 废弃
            // 新样品编号格式处理： 情况1： YP-2022-9200-01~03 情况2：YP-2022-0096
            String[] sampleSplits = sampleTagInfo.getSampleCode().split("-");
            int startNumber = 0;
            // 判断 样品编号情况
            StringBuilder sampleCodeStr = new StringBuilder();
            // 判断 样品编号情况
            if(sampleSplits.length>3){
                startNumber =1;
                for(int i=0;i<sampleSplits.length-1;i++){
                    sampleCodeStr.append(sampleSplits[i]);
                    sampleCodeStr.append("-");
                }
            }
            // 处理多个样品 打包成zip
            if (startNumber > 0) {
                String[] strings = sampleTagInfo.getSampleCode().substring(startNumber + 1).split("~");
                Integer maxNumber = Integer.valueOf(strings[1]);
                // 处理样品 外观描述，和 外观
//                if (sampleTagInfo.getOutwardDescribe() != null && !sampleTagInfo.getOutwardDescribe().equals("") && sampleTagInfo.getOutward() != null && !sampleTagInfo.getOutward().equals("")) {
//                    sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1) + "\t" + sampleTagInfo.getOutwardDescribe());
//                } else {
//                    if (sampleTagInfo.getOutward() != null && !sampleTagInfo.getOutward().equals("")) {
//                        sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1));
//                    }
//                }

//                StringBuilder outward = new StringBuilder();
//                if(sampleTagInfo.getOutward() != null && !sampleTagInfo.getOutward().equals("")) {
//                    String replace = sampleTagInfo.getOutward().replace("[","");
//                    String replace1 = replace.replace("]", "");
//                    outward.append(replace1);
//                    if(sampleTagInfo.getOutwardDescribe() != null && !sampleTagInfo.getOutwardDescribe().equals("")){
//                        outward.append(",");
//                        outward.append(sampleTagInfo.getOutwardDescribe());
//                    }
//                }else{
//                    if(sampleTagInfo.getOutwardDescribe() != null && !sampleTagInfo.getOutwardDescribe().equals("")){
//                        outward.append(sampleTagInfo.getOutwardDescribe());
//                    }
//                }
//                sampleTagInfo.setOutward(outward.toString());
                // 处理样品描述信息 Outward、 outwardDescribe 组合输出
                StringBuilder stringBuilder = new StringBuilder();
                if(sampleTagInfo.getOutward()!=null&&sampleTagInfo.getOutward().length() >0){
                    stringBuilder.append(sampleTagInfo.getOutward()+",");
                }
                if(sampleTagInfo.getOutwardDescribe()!=null&&sampleTagInfo.getOutwardDescribe().length()>0){
                    stringBuilder.append(sampleTagInfo.getOutwardDescribe()+",");
                }
                if(stringBuilder.length()>1){
                    sampleTagInfo.setOutward(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
                }
                else {
                    sampleTagInfo.setOutward("");
                }

                for (int i = 1; i <= maxNumber; i++) {
                    InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
                    StringBuilder fileName = new StringBuilder("");
                    // 在样品标签中 test_sample  别名  = 产品名
                    sampleTagInfo.setSampleName(sampleTagInfo.getAliasName());
                    sampleTagInfo.setSampleCode(sampleCodeStr + String.valueOf(i));
                    fileName.append(sampleTagInfo.getSampleCode());
                    fileName.append("样品标签.xlsx");
                    result.put("result", sampleTagInfo);
                    try {
                        XLSTransformer transformer = new XLSTransformer();
                        Workbook workbook = null;
                        workbook = transformer.transformXLS(fileStream, result);
                        // 根据单个Workbook 进行处理打包。
                        DealWithZip(workbook, fileName.toString(), out);
                    } catch (InvalidFormatException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // 单个样品 打包成zip 文件。
            else {
                InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
                StringBuilder fileName = new StringBuilder("");
                // 处理样品描述信息 Outward、 outwardDescribe 组合输出
                StringBuilder stringBuilder = new StringBuilder();
                if(sampleTagInfo.getOutward()!=null&&sampleTagInfo.getOutward().length() >0){
                    stringBuilder.append(sampleTagInfo.getOutward()+",");
                }
                if(sampleTagInfo.getOutwardDescribe()!=null&&sampleTagInfo.getOutwardDescribe().length()>0){
                    stringBuilder.append(sampleTagInfo.getOutwardDescribe()+",");
                }
                if(stringBuilder.length()>1){
                    sampleTagInfo.setOutward(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
                }
                else {
                    sampleTagInfo.setOutward("");
                }
                // 在样品标签中 test_sample  别名  = 产品名
                sampleTagInfo.setSampleName(sampleTagInfo.getAliasName());
                fileName.append(sampleTagInfo.getSampleCode());
                fileName.append("样品标签.xlsx");
                result.put("result", sampleTagInfo);
                try {
                    XLSTransformer transformer = new XLSTransformer();
                    Workbook workbook = null;
                    workbook = transformer.transformXLS(fileStream, result);
                    DealWithZip(workbook, fileName.toString(), out);
                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 关闭输入流
            out.closeEntry();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    /**
     * 处理样品类型为： 配合比验证。 进行zip。
     *
     * @param sampleTagInfo
     * @param out
     */
    public void methodSampleTypeLineBlend(SampleDetailVo sampleTagInfo, ZipOutputStream out) throws IOException {
        /**
         * 获取配合比集合。
         */
        List<SampleDetailVo> sampleDetailVoList = new ArrayList<>();
        sampleDetailVoList = sampleEntityMapper.getSampleTagInfoPidList(sampleTagInfo.getId());
        // 存储顶级标签信息。
        sampleDetailVoList.add(sampleTagInfo);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        for (int i = 0; i < sampleDetailVoList.size(); i++) {
            SampleDetailVo sampleData = sampleDetailVoList.get(i);
            // 处理样品 外观描述，和 外观
//            if (sampleData.getOutwardDescribe() != null && !sampleData.getOutwardDescribe().equals("") && sampleData.getOutward() != null && !sampleData.getOutward().equals("")) {
//                sampleData.setOutward(sampleData.getOutward().substring(1, sampleData.getOutward().length() - 1) + "\t" + sampleData.getOutwardDescribe());
//            } else {
//
//            }

//            StringBuilder outward = new StringBuilder();
//            if(sampleData.getOutward() != null && !sampleData.getOutward().equals("")) {
//                String replace = sampleData.getOutward().replace("[","");
//                String replace1 = replace.replace("]", "");
//                outward.append(replace1);
//                if(sampleData.getOutwardDescribe() != null && !sampleData.getOutwardDescribe().equals("")){
//                    outward.append(",");
//                    outward.append(sampleData.getOutwardDescribe());
//                }
//            }else{
//                if(sampleData.getOutwardDescribe() != null && !sampleData.getOutwardDescribe().equals("")){
//                    outward.append(sampleData.getOutwardDescribe());
//                }
//            }
//            sampleData.setOutward(outward.toString());
            // 处理样品描述信息 Outward、 outwardDescribe 组合输出
            StringBuilder stringBuilder = new StringBuilder();
            if(sampleData.getOutward()!=null&&sampleData.getOutward().length() >0){
                stringBuilder.append(sampleData.getOutward()+",");
            }
            if(sampleData.getOutwardDescribe()!=null&&sampleData.getOutwardDescribe().length()>0){
                stringBuilder.append(sampleData.getOutwardDescribe()+",");
            }
            if(stringBuilder.length()>1){
                sampleData.setOutward(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
            }


            InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
            // 在样品标签中 test_sample  别名  = 产品名
            sampleTagInfo.setSampleName(sampleTagInfo.getAliasName());
            StringBuilder fileName = new StringBuilder("");
            fileName.append(sampleData.getSampleCode());
            fileName.append("样品标签.xlsx");
            result.put("result", sampleData);
            try {
                XLSTransformer transformer = new XLSTransformer();
                Workbook workbook = null;
                workbook = transformer.transformXLS(fileStream, result);
                // 根据单个Workbook 进行处理打包。
                DealWithZip(workbook, fileName.toString(), out);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 关闭输入流
        out.closeEntry();
        if (out != null) {
            out.flush();
            out.close();
        }
    }

    /**
     * 根据单个Workbook 进行处理打包。
     */
    public static void DealWithZip(Workbook workbook, String fileName, ZipOutputStream out) {
        try {
            // 将源文件数组中的当前文件读入 FileInputStream 流中
            ZipEntry entry = new ZipEntry(fileName);
            out.putNextEntry(entry);
            //这里讲一下，workBook.write会指定关闭数据流，如果这里直接用workbook.write(out)，下次就会抛出out已被关闭的异常，所有用ByteArrayOutputStream来拷贝一下。
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<HashMap<String, SampleDetailVo>> getSampleTagInfoList(Integer sampleId) {
        List<HashMap<String, SampleDetailVo>> results = Lists.newArrayList();
        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        if (sampleTagInfo != null) {
            // 处理样品描述信息 Outward、 outwardDescribe 组合输出
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(sampleTagInfo.getOutward() == null?"":sampleTagInfo.getOutward());
            if(stringBuilder.length()>0){
                stringBuilder.append(",");
            }
            stringBuilder.append(sampleTagInfo.getOutwardDescribe()==null?"":sampleTagInfo.getOutwardDescribe());
            stringBuilder.append(",");
            sampleTagInfo.setOutward(stringBuilder.deleteCharAt(stringBuilder.length()-1).toString());
            // 处理样品描述信息 Outward 清除两边[]
//            if (sampleTagInfo.getOutwardDescribe() == null) {
//                sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1));
//            } else {
//                sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1) + "," + sampleTagInfo.getOutwardDescribe());
//            }
            // 样品编号格式： 情况1： YP-2022-0095（01~02） 情况2：YP-2022-0096
            String sampleCode = sampleTagInfo.getSampleCode();
            if (sampleCode.contains("~")) {
                //获取样品数量
                Integer i = sampleTagInfo.getQuantityPerGroup();
                //构造样品编号
                for (int j = 1; j <= i; j++) {
                    String prefix = sampleCode.substring(0, sampleCode.indexOf("（"));
                    String suffix = new DecimalFormat("00").format(j);
                    SampleDetailVo vo = new SampleDetailVo();
                    vo.setSampleCode(prefix + "_" + suffix);
                    vo.setSampleName(sampleTagInfo.getSampleName());
                    vo.setSpecs(sampleTagInfo.getSpecs());
                    vo.setOutward(sampleTagInfo.getOutward());
                    HashMap<String, SampleDetailVo> result = Maps.newHashMap();
                    result.put("result", vo);
                    results.add(result);
                }
            } else {
                HashMap<String, SampleDetailVo> result = Maps.newHashMap();
                result.put("result", sampleTagInfo);
                results.add(result);
            }
        }
        return results;
    }
}
