package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SampleFileTableDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleMixInfoEntityMapper;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import com.lims.manage.erp.vo.SampleJudgeBasisVo;
import com.lims.manage.erp.vo.SampleSimpleListVo;
import com.lims.manage.erp.vo.SamplesAddVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TestSampleEntityServiceImpl extends ServiceImpl<TestSampleEntityMapper, TestSampleEntity> implements TestSampleEntityService {
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private SampleFileTableDao sampleFileTableDao;
    @Autowired
    private TestSampleMixInfoEntityMapper mixInfoEntityMapper;
    Logger logger = LoggerFactory.getLogger(TestSampleEntityServiceImpl.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
    private final Date now = new Date();

    private int getNewSampleCode() {
        //获取数据库当前年份最大的样品编号
        String maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = Integer.parseInt(maxNumber);
        }
        return newMax;
    }

    @Override
    public Integer batchInsertSample(List<SampleDetailAddVo> samples) {
        List<TestSampleEntity> entities = Lists.newArrayList();
        //获取数据库当前年份最大的样品编号
        int newMax = getNewSampleCode();
        for (int i = 0; i < samples.size(); i++) {
            int code = newMax + i + 1;
            String codeStr = new DecimalFormat("0000").format(code);
            String sampleCode;
            if (samples.get(i).getQuantityPerGroup() > 1) {
                String numStr = new DecimalFormat("00").format(samples.get(i).getQuantityPerGroup());
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr + "（01~" + numStr + "）";
            } else {
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
            }
            TestSampleEntity entity = new TestSampleEntity(samples.get(i), sampleCode);
            entities.add(entity);
        }
        return testSampleEntityMapper.insertBatch(entities);
    }

    @Transactional
    @Override
    public Integer batchInsertMixSample(SamplesAddVo samples) {
        List<TestSampleEntity> param = Lists.newArrayList();
        //处理配合比样品数据
        Integer newId = testSampleEntityMapper.getMaxId() + 1;
        SampleDetailAddVo vo = new SampleDetailAddVo(samples, newId);
        int newMax = getNewSampleCode() + 1;
        String codeStr = new DecimalFormat("0000").format(newMax);
        String sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
        TestSampleEntity mainSample = new TestSampleEntity(vo, sampleCode);
        param.add(mainSample);
        //处理子样品
        List<SampleDetailAddVo> samples1 = samples.getSamples();
        int i = 1;
        for (SampleDetailAddVo sampleDetailAddVo : samples1) {
            //设置样品编号
            String format = new DecimalFormat("00").format(i);
            String code = sampleCode + "_" + format;
            //设置签收人
            sampleDetailAddVo.setInspector(vo.getInspector());
            //设置签收时间
            sampleDetailAddVo.setReceivedDate(vo.getReceivedDate());
            //设置检验类型
            sampleDetailAddVo.setSampleType(vo.getSampleType());
            //设置原材PID
            sampleDetailAddVo.setPid(newId);
            //设置委托单位
            sampleDetailAddVo.setCompanyId(vo.getCompanyId());
            TestSampleEntity sample = new TestSampleEntity(sampleDetailAddVo, code);
            param.add(sample);
            i++;
        }
        TestSampleMixInfoEntity mixInfoEntity = new TestSampleMixInfoEntity(samples, newId);
        //插入配合比参数信息
        mixInfoEntityMapper.insert(mixInfoEntity);
        return testSampleEntityMapper.insertBatchMixSamples(param);
    }

    @Override
    public PageInfo querySampleList(TestSampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleSimpleListVo> simpleList = testSampleEntityMapper.getSimpleList(sampleEntity);
        PageInfo<SampleSimpleListVo> pageInfo = new PageInfo<>(simpleList);
        return pageInfo;
    }

    @Override
    public Boolean uploading(Integer id, MultipartFile[] file) {
        SampleFileTableEntity sampleFileTableEntity = new SampleFileTableEntity();
        sampleFileTableEntity.setSampleId(id);
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            // 根据file文件数量 规定文件名存储编号规则
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.buckets_sample_enclosure, multipartFile, fileCode + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（文件编号&委托文档资料.pdf,文件编号&原始文档.docx）
                stringfileUrlStr.append(fileCode + "&" + name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                sampleFileTableEntity.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                sampleFileTableEntity.setFileUrlStr(substring);
            }
        }
        sampleFileTableEntity.setCarateTime(new Date());
        sampleFileTableDao.insertSampleFileTableEntity(sampleFileTableEntity);
        return true;
    }

    @Override
    public Boolean removeding(Integer id) {
        // 根据附件id 查询文件名称 进行删除minIo文件服务器中内容。
        SampleFileTableEntity SampleFileData = sampleFileTableDao.getSampleFileTableEntityId(id);
        if (SampleFileData != null && SampleFileData.getFileUrl() != null) {
            // 去清除 MinIo 桶数据。
            try {
                String[] strings2 = SampleFileData.getFileUrlStr().split(",");
                for (int i = 0; i < strings2.length; i++) {
                    String[] strings3 = strings2[i].split("\\.");
                    if (strings3.length >= 2) {
                        String[] strings4 = strings3[0].split("&");
                        // 获取 文件编号
                        Long fileCode = Long.parseLong(strings4[0]);
                        MinIoUtil.deleteFile(BucketsConst.buckets_sample_enclosure, fileCode + "." + strings3[1]);
                    }
                }
            } catch (Exception e) {
                logger.info("修改委托下清除 MinIo 桶数据 出错");
            }
        }

        sampleFileTableDao.deleteSampleFileTableEntity(id);
        return true;
    }

    @Override
    public TestSampleEntity sampleDetail(Integer id) {
        TestSampleEntity entity = testSampleEntityMapper.selectByPrimaryKey(id);
        if (entity != null) {
            String outward = entity.getOutward();
            if (outward != null) {
                String replace = outward.replace("[", "");
                String replace1 = replace.replace("]", "");
                String replace2 = replace1.replace("\"", "");
                String[] split = replace2.split(",");
                List<String> outwardArr = Lists.newArrayList();
                for (String s : split) {
                    outwardArr.add(s.trim());
                }
                entity.setOutwardArr(outwardArr);
            }
            List<TestSampleCollectionJSON> fileArrays = new ArrayList<>();
            // 根据样品id 查询 对应的文件信息。
            List<SampleFileTableEntity> sampleFileTableEntityList = sampleFileTableDao.getSampleFileTableEntityList(id);
            if (sampleFileTableEntityList != null && !sampleFileTableEntityList.isEmpty()) {
                for (SampleFileTableEntity sampleFileTableEntity : sampleFileTableEntityList) {
                    TestSampleCollectionJSON testSampleCollectionJSON = new TestSampleCollectionJSON();
                    testSampleCollectionJSON.setLable(sampleFileTableEntity.getFileUrl());
                    testSampleCollectionJSON.setValue(sampleFileTableEntity.getFileUrlStr());
                    testSampleCollectionJSON.setId(sampleFileTableEntity.getId());
                    fileArrays.add(testSampleCollectionJSON);
                }
            }
            entity.setFileArrays(fileArrays);
            //根据主样品信息查询节点样品信息
            List<TestSampleEntity> testSampleEntities = testSampleEntityMapper.selectByPid(id);
            if(!CollectionUtils.isEmpty(testSampleEntities)){
                for (TestSampleEntity entity1 : testSampleEntities) {
                    if (entity1.getOutward() != null) {
                        String replace = entity1.getOutward().replace("[", "");
                        String replace1 = replace.replace("]", "");
                        String replace2 = replace1.replace("\"", "");
                        String[] split = replace2.split(",");
                        List<String> outwardArr = Lists.newArrayList();
                        for (String s : split) {
                            outwardArr.add(s.trim());
                        }
                        entity1.setOutwardArr(outwardArr);
                    }
                    // 根据样品id 查询 对应的文件信息。
                    List<SampleFileTableEntity> sampleFileList = sampleFileTableDao.getSampleFileTableEntityList(entity1.getId());
                    if (sampleFileList != null && !sampleFileList.isEmpty()) {
                        for (SampleFileTableEntity sampleFileTableEntity : sampleFileList) {
                            TestSampleCollectionJSON testSampleCollectionJSON = new TestSampleCollectionJSON();
                            testSampleCollectionJSON.setLable(sampleFileTableEntity.getFileUrl());
                            testSampleCollectionJSON.setValue(sampleFileTableEntity.getFileUrlStr());
                            testSampleCollectionJSON.setId(sampleFileTableEntity.getId());
                            fileArrays.add(testSampleCollectionJSON);
                        }
                    }
                    entity1.setFileArrays(fileArrays);
                }
            }
            entity.setNodeSample(testSampleEntities);
            //根据主样品信息查询配合比检测信息
            TestSampleMixInfoEntity mixInfoEntity = mixInfoEntityMapper.selectBySampleId(id);
            entity.setMixInfo(mixInfoEntity);
            // 处理原始记录名称
//            if (entity.getFile() != null && entity.getFileUrlStr() != null) {
//                String[] file = entity.getFile().split(",");
//                String[] fileUrlStr = entity.getFileUrlStr().split(",");
//                for (int i = 0; i < file.length; i++) {
//                    TestSampleCollectionJSON testSampleCollectionJSON = new TestSampleCollectionJSON();
//                    testSampleCollectionJSON.setLable(fileUrlStr[i]);
//                    testSampleCollectionJSON.setValue(file[i]);
//                    fileArrays.add(testSampleCollectionJSON);
//                }
//            }
        }
        return entity;
    }

    @Override
    public int updateSample(TestSampleEntity sampleEntity) {
        return testSampleEntityMapper.updateByPrimaryKeyNotAll(sampleEntity);
    }
}
