package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.SampleService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.*;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            String num = String.format("%0" + 4 + "d", newMax + 1);
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

    @Override
    public Workbook returningData(InputStream fileStream, HashMap<String, SampleDetailVo> result, String fileName) {
        try {
            XLSTransformer transformer = new XLSTransformer();
            Workbook workbook = null;
            workbook = transformer.transformXLS(fileStream, result);
            workbook.close();
            return workbook;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据样品id 获取Excel表格集合 存储到zip里面。
     *
     * @return
     */
    @Override
    public ZipOutputStream packagingWorkbookZip(Integer sampleId,HttpServletResponse response) throws IOException {

        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        // 通过输入参数 返回 对应的处理成功的EXCEL数据。
        ServletOutputStream outputStream = response.getOutputStream();
        ZipOutputStream out = new ZipOutputStream(outputStream);
        if (sampleTagInfo != null) {
            // 样品编号格式： 情况1： YP-2022-0095（01~02） 情况2：YP-2022-0096
            String sampleCode = sampleTagInfo.getSampleCode();
            int startNumber = sampleCode.indexOf("（");
            int endNumber = sampleCode.indexOf("）");
            // 处理多个样品 打包成zip
            if (startNumber > 0 && endNumber > 0) {
                String[] strings = sampleCode.substring(startNumber + 1, endNumber).split("~");
                Integer maxNumber = Integer.valueOf(strings[1]);
                sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1));
                for (int i = 1; i <= maxNumber; i++) {
                    InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
                    StringBuilder fileName = new StringBuilder("");
                    sampleTagInfo.setSampleCode(sampleCode.substring(0, startNumber) + "_" + i);
                    fileName.append(sampleTagInfo.getSampleCode());
                    fileName.append("样品标签.xlsx");
                    result.put("result", sampleTagInfo);
                    try {
                        XLSTransformer transformer = new XLSTransformer();
                        Workbook workbook = null;
                        workbook = transformer.transformXLS(fileStream, result);
                        // 根据单个Workbook 进行处理打包。
                        DealWithZip(workbook,fileName.toString(),out);
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
                // 处理样品描述信息 Outward 清除两边[]
                sampleTagInfo.setOutward(sampleTagInfo.getOutward().substring(1, sampleTagInfo.getOutward().length() - 1));
                fileName.append(sampleTagInfo.getSampleCode());
                fileName.append("样品标签.xlsx");
                result.put("result", sampleTagInfo);
                try {
                    XLSTransformer transformer = new XLSTransformer();
                    Workbook workbook = null;
                    workbook = transformer.transformXLS(fileStream, result);
                    DealWithZip(workbook,fileName.toString(),out);
                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
             // 关闭输入流
            out.closeEntry();
            if(out!=null){
                out.flush();
                out.close();
            }
        }
        return out;
    }

    @Override
    public Boolean packagingZip(List<Workbook> dateSet) {
        // 创建 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 创建 ZipOutputStream
        ZipOutputStream zipOutputStream = null;
        // 创建 FileInputStream 对象
        FileInputStream fileInputStream = null;
        File zipFile = new File("D:\\poiFile\\ZipFile.zip");
        // 判断压缩后的文件存在不，不存在则创建
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // 实例化 FileOutputStream 对象
            fileOutputStream = new FileOutputStream(zipFile);
            // 实例化 ZipOutputStream 对象
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件数组
            for (int i = 0; i < dateSet.size(); i++) {
                // 将源文件数组中的当前文件读入 FileInputStream 流中
                Workbook workbook = dateSet.get(i);
//                zipOutputStream.write(workbook.getSheetAt(0));
//                fileInputStream = new FileInputStream("D:\\poiFilShiroSessionManagere\\YP-2022-0139_3样品标签.xlsx");
//                // 实例化 ZipEntry 对象，源文件数组中的当前文件
//                zipEntry = new ZipEntry("excel"+i);
//                zipOutputStream.putNextEntry(zipEntry);
//                // 该变量记录每次真正读的字节个数
//                int len;
//                // 定义每次读取的字节数组
//                byte[] buffer = new byte[1024];
//                while ((len = fileInputStream.read(buffer)) > 0) {
//                    zipOutputStream.write(buffer, 0, len);
//                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            fileInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据单个Workbook 进行处理打包。
     */
    public void DealWithZip(Workbook workbook, String fileName,ZipOutputStream out){
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
}
