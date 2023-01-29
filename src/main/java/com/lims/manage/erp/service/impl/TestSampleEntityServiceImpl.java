package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.SampleFileTableEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestSampleCollectionJSON;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SampleFileTableDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleMixInfoEntityMapper;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.SampleDetailAddVo;
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

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private LogManagerService logManagerService;
    Logger logger = LoggerFactory.getLogger(TestSampleEntityServiceImpl.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    private int getNewSampleCode() {
        Date now = new Date();
        //获取数据库当前年份最大的样品编号
        PageHelper.clearPage();
        Integer maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        logger.debug("样品编号:{}",maxNumber);
        Integer newMax;
        if (maxNumber == null) {
            newMax = 0;
        }else{
            newMax = maxNumber;
        }
        return newMax;
    }

    @Override
    public Integer batchInsertSample(List<SampleDetailAddVo> samples) {
        Date now = new Date();
        List<TestSampleEntity> entities = Lists.newArrayList();
        //获取数据库当前年份最大的样品编号
        int newMax = getNewSampleCode();
        for (int i = 0; i < samples.size(); i++) {
            int code = newMax + i + 1;
            String codeStr = new DecimalFormat("00000").format(code);
            String sampleCode;
            if (samples.get(i).getQuantityPerGroup() > 1) {
                String numStr = new DecimalFormat("00").format(samples.get(i).getQuantityPerGroup());
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr + "-01~" + numStr;
            } else {
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
            }
            StringBuilder outwardStr = new StringBuilder();
            List<String> outward = samples.get(i).getOutward();
            if(!CollectionUtils.isEmpty(outward)){
                for (int j = 0; j < outward.size(); j++) {
                    if(j != outward.size()-1){
                        outwardStr.append(outward.get(j));
                        outwardStr.append(",");
                    }else{
                        outwardStr.append(outward.get(j));
                    }
                }
            }
            TestSampleEntity entity = new TestSampleEntity(samples.get(i), sampleCode,outwardStr.toString());
            entities.add(entity);
        }
        return testSampleEntityMapper.insertBatch(entities);
    }

    @Transactional
    @Override
    public Integer batchInsertMixSample(SamplesAddVo samples) {
        Date now = new Date();
        List<TestSampleEntity> param = Lists.newArrayList();
        //处理配合比样品数据
        Integer newId = testSampleEntityMapper.getMaxId() + 1;
        SampleDetailAddVo vo = new SampleDetailAddVo(samples, newId);
        int newMax = getNewSampleCode() + 1;
        String codeStr = new DecimalFormat("00000").format(newMax);
        String sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
        TestSampleEntity mainSample = new TestSampleEntity(vo, sampleCode,null);
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
            StringBuilder outwardStr = new StringBuilder();
            List<String> outward = sampleDetailAddVo.getOutward();
            if(!CollectionUtils.isEmpty(outward)){
                for (int j = 0; j < outward.size(); j++) {
                    if(j != outward.size()-1){
                        outwardStr.append(outward.get(j));
                        outwardStr.append(",");
                    }else{
                        outwardStr.append(outward.get(j));
                    }
                }
            }
            TestSampleEntity sample = new TestSampleEntity(sampleDetailAddVo, code,outwardStr.toString());
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
        List<SampleSimpleListVo> newDto = simpleList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(SampleSimpleListVo::getSampleCode))),ArrayList::new));
        PageInfo<SampleSimpleListVo> pageInfo = new PageInfo<>(newDto);
        return pageInfo;
    }

    @Override
    public PageInfo showSampleList(TestSampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        if ("2".equals(sampleEntity.getState())){
            sampleEntity.setState("1");
        }
        if ("3".equals(sampleEntity.getState())){
            sampleEntity.setState("2");
        }
        List<SampleSimpleListVo> simpleList = testSampleEntityMapper.showSimpleList(sampleEntity);
        List<SampleSimpleListVo> newDto = simpleList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(SampleSimpleListVo::getSampleCode))),ArrayList::new));
        PageInfo<SampleSimpleListVo> pageInfo = new PageInfo<>(newDto);
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
                if(!StringUtils.isEmpty(upload)){
                    String[] fileUrls = upload.split("\\?");
                    stringBuilder.append(fileUrls[0]);
                }
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
            if (!CollectionUtils.isEmpty(testSampleEntities)) {
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
        }
        return entity;
    }

    @Override
    public int updateSample(TestSampleEntity sampleEntity) {
        // 根据样品ID 判断 样品与委托单是否绑定 、绑定 产品id是否变动，变动 则清除委托单下检测项信息。
        methodUpdateSample(sampleEntity.getId(),sampleEntity.getProductId());
        String outward = sampleEntity.getOutward();
        if(outward != null){
            String replace = outward.replace("[", "");
            String replace1 = replace.replace("]", "");
            String replace2 = replace1.replace("\"", "");
            sampleEntity.setOutward(replace2);
        }
        //修改样品数量时，处理样品编号
        StringBuilder newSampleCode = new StringBuilder();
        String sampleCode = sampleEntity.getSampleCode();
        String prefix = sampleCode.substring(0,8);
        String code = sampleCode.substring(8);
        int i = code.indexOf("-");
        String num;
        if(i > 0){
            num = code.substring(0, i);
        }else{
            num = code;
        }
        newSampleCode.append(prefix+num);
        if(sampleEntity.getQuantityPerGroup() > 1){
            newSampleCode.append("-01~");
            String numStr = new DecimalFormat("00").format(sampleEntity.getQuantityPerGroup());
            newSampleCode.append(numStr);
        }
        sampleEntity.setSampleCode(newSampleCode.toString());
        return testSampleEntityMapper.updateByPrimaryKeyNotAll(sampleEntity);
    }

    @Transactional
    @Override
    public int updateSampleBatch(TestSampleEntity sampleEntity) {
        // 根据样品ID 判断 样品与委托单是否绑定 、绑定 产品id是否变动，变动 则清除委托单下检测项信息。
        methodUpdateSample(sampleEntity.getId(),sampleEntity.getProductId());
        List<Integer> allNodeIds = testSampleEntityMapper.getAllNodeIds(sampleEntity.getId());
        List<TestSampleEntity> nodeSample = sampleEntity.getNodeSample();
        nodeSample.add(sampleEntity);
        int result = 0;
        int i = 1;
        for (TestSampleEntity entity : nodeSample) {
            if (entity.getId() != null) {
                //设置签收人
                entity.setInspector(sampleEntity.getInspector());
                //设置签收时间
                entity.setReceivedDate(sampleEntity.getReceivedDate());
                //设置检验类型
                entity.setSampleType(sampleEntity.getSampleType());
                //设置委托单位
                entity.setCompanyId(sampleEntity.getCompanyId());
                String outward = entity.getOutward();
                if(outward != null){
                    String replace = outward.replace("[", "");
                    String replace1 = replace.replace("]", "");
                    String replace2 = replace1.replace("\"", "");
                    entity.setOutward(replace2);
                }
                result = testSampleEntityMapper.updateByPrimaryKeyNotAll(entity);
            } else {
                //设置样品编号
                String format = new DecimalFormat("00").format(i);
                String code = sampleEntity.getSampleCode() + "_" + format;
                //设置签收人
                entity.setInspector(sampleEntity.getInspector());
                //设置签收时间
                entity.setReceivedDate(sampleEntity.getReceivedDate());
                //设置检验类型
                entity.setSampleType(sampleEntity.getSampleType());
                //设置原材PID
                entity.setPid(sampleEntity.getId());
                //设置委托单位
                entity.setCompanyId(sampleEntity.getCompanyId());
                List<TestSampleEntity> list = Lists.newArrayList();
                list.add(entity);
                result = testSampleEntityMapper.insertBatchMixSamples(list);
            }
            allNodeIds.remove(entity.getId());
        }
        mixInfoEntityMapper.updateBySampleId(new TestSampleMixInfoEntity(sampleEntity));
        //删除原材
        if(!CollectionUtils.isEmpty(allNodeIds)){
            testSampleEntityMapper.deleteBatchIds(allNodeIds);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TestSampleEntity>  batchInsertSampleCopy(List<SampleDetailAddVo> samples) {
        Date now = new Date();
        List<TestSampleEntity> entities = Lists.newArrayList();
        //获取数据库当前年份最大的样品编号
        int newMax = getNewSampleCode();
        for (int i = 0; i < samples.size(); i++) {
            int code = newMax + i + 1;
            String codeStr = new DecimalFormat("00000").format(code);
            String sampleCode;
            if (samples.get(i).getQuantityPerGroup() > 1) {
                String numStr = new DecimalFormat("00").format(samples.get(i).getQuantityPerGroup());
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr + "-01~" + numStr;
            } else {
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
            }
            StringBuilder outwardStr = new StringBuilder();
            List<String> outward = samples.get(i).getOutward();
            if(!CollectionUtils.isEmpty(outward)){
                for (int j = 0; j < outward.size(); j++) {
                    if(j != outward.size()-1){
                        outwardStr.append(outward.get(j));
                        outwardStr.append(",");
                    }else{
                        outwardStr.append(outward.get(j));
                    }
                }
            }
            TestSampleEntity entity = new TestSampleEntity(samples.get(i), sampleCode,outwardStr.toString());
            entities.add(entity);
        }
        if(!CollectionUtils.isEmpty(entities)){
            StringBuilder stringBuilder1 = new StringBuilder();
            for(TestSampleEntity testSampleEntity :entities){
                stringBuilder1.append("样品主键\t"+testSampleEntity.getId()+"\t样品编号\t"+testSampleEntity.getSampleCode());
                stringBuilder1.append("产品名称\t"+testSampleEntity.getSampleName()+"样品别名\t"+testSampleEntity.getAliasName());
                stringBuilder1.append("委托单位\t"+testSampleEntity.getCompanyId()+"规格等级\t"+testSampleEntity.getSpecs());
                stringBuilder1.append("委托检测类别\t"+testSampleEntity.getSampleType());
                if(testSampleEntity.getCheckDate()!=null){
                    stringBuilder1.append("来样时间"+(new Timestamp(testSampleEntity.getCheckDate().getTime())));
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：新增原材样品\t"+stringBuilder1.toString(), Const.ENTRUST_FOUND, true);
        }
        testSampleEntityMapper.insertBatch(entities);
        return entities;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestSampleMixInfoEntity batchInsertMixSampleCopy(SamplesAddVo samples,long id) {
        Date now = new Date();
        List<TestSampleEntity> param = Lists.newArrayList();
        //处理配合比样品数据
        Integer newId = testSampleEntityMapper.getMaxId() + 1;
        SampleDetailAddVo vo = new SampleDetailAddVo(samples, newId);
        int newMax = getNewSampleCode() + 1;
        String codeStr = new DecimalFormat("00000").format(newMax);
        String sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
        TestSampleEntity mainSample = new TestSampleEntity(vo, sampleCode,null);
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
            StringBuilder outwardStr = new StringBuilder();
            List<String> outward = sampleDetailAddVo.getOutward();
            if(!CollectionUtils.isEmpty(outward)){
                for (int j = 0; j < outward.size(); j++) {
                    if(j != outward.size()-1){
                        outwardStr.append(outward.get(j));
                        outwardStr.append(",");
                    }else{
                        outwardStr.append(outward.get(j));
                    }
                }
            }
            TestSampleEntity sample = new TestSampleEntity(sampleDetailAddVo, code,outwardStr.toString());
            param.add(sample);
            i++;
        }
        TestSampleMixInfoEntity mixInfoEntity = new TestSampleMixInfoEntity(samples, newId);
        mixInfoEntity.setEntrustmentId(id);
        //插入配合比参数信息
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("新增 配合比id="+newId+"配合比样品id"+mixInfoEntity.getSampleId());
        stringBuilder.append("\t设计强度（MPa）\t"+mixInfoEntity.getDesignStrength()+"\t配制强度（MPa）\t"+mixInfoEntity.getIntensityConfiguration()
                +"\t抗（渗、冻）等级\t"+mixInfoEntity.getAntifreezeLevel()+"\t水胶比\t"+mixInfoEntity.getWaterBinderRatio()+"\t单位用水量（kg）\t"+mixInfoEntity.getUnitWaterUse()
                +"\t砂率（%）\t"+mixInfoEntity.getSandRatio()+"\t设计坍落度（mm）\t"+mixInfoEntity.getDesignSlump()+"\t拌和方式\t"+mixInfoEntity.getMixingWay()+"\t样品id\t"+mixInfoEntity.getSampleId());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：插入配合比参数信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
        mixInfoEntityMapper.insert(mixInfoEntity);
        if(!CollectionUtils.isEmpty(param)){
            StringBuilder stringBuilder1 = new StringBuilder();
            for(TestSampleEntity testSampleEntity:param){
                stringBuilder1.append("配合比主键\t"+testSampleEntity.getId()+"\t样品编号\t"+testSampleEntity.getSampleCode());
                stringBuilder1.append("产品名称\t"+testSampleEntity.getSampleName()+"样品别名\t"+testSampleEntity.getAliasName());
                stringBuilder1.append("委托单位\t"+testSampleEntity.getCompanyId()+"规格等级\t"+testSampleEntity.getSpecs());
                stringBuilder1.append("委托检测类别\t"+testSampleEntity.getSampleType());
                if(testSampleEntity.getCheckDate()!=null){
                    stringBuilder1.append("来样时间"+(new Timestamp(testSampleEntity.getCheckDate().getTime())));
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：新增配合比样品\t"+stringBuilder1.toString(), Const.ENTRUST_FOUND, true);
        }
        testSampleEntityMapper.insertBatchMixSamples(param);
        return mixInfoEntity;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean judgmentSampleUnit(Integer id, Integer companyId) {
        //        根据样品ID 判断 样品与委托单是否绑定 、绑定 产品id是否变动，变动 则清除委托单下检测项信息。
        TestSampleEntity testSampleEntity = testSampleEntityMapper.selectByPrimaryKey(id);
        if(testSampleEntity.getIsUse()==1&&!testSampleEntity.getCompanyId().equals(companyId)){
            return false;
        }
        if(!testSampleEntity.getCompanyId().equals(companyId)){
            TestSampleEntity record = new TestSampleEntity();
            record.setId(id);
            record.setCompanyId(companyId);
            testSampleEntityMapper.updateByPrimaryKeySelective(record);
        }
        return true;
    }

    /**
     * 根据样品id 处理 是否需要变更。
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void methodUpdateSample(Integer id,Integer productId){
//        根据样品ID 判断 样品与委托单是否绑定 、绑定 产品id是否变动，变动 则清除委托单下检测项信息。
        TestSampleEntity testSampleEntity = testSampleEntityMapper.selectByPrimaryKey(id);
        // IsUse=1 则与委托单建立关系。
        if(testSampleEntity.getIsUse()==1){
            // 通过样品id 获得委托单id 获取委托单状态 是否未发布。
            EntrustEntity entrustEntity =   testSampleEntityMapper.selectEntrustState(id);
            // 产品id已经修改 AND 委托单未发布  则清除委托单改样品信息检测项集合。
            if(!testSampleEntity.getProductId().equals(productId)&&entrustEntity.getState()==0){
                List<SampleItemEntity>  allOldCheckItemInfo = entrustEntityMapper.getAllOldCheckItemInfo(id,entrustEntity.getId());
                if(!allOldCheckItemInfo.isEmpty())
                {
                    // 获取检测项主键 依次删除。
                    for(int i=0;i<allOldCheckItemInfo.size();i++){
                        SampleItemEntity sampleItemEntity  = allOldCheckItemInfo.get(i);
                        entrustEntityMapper.deleteEntrustedSampleCheckitemRel(sampleItemEntity.getId());
                    }
                }
            }
        }
    }
}
