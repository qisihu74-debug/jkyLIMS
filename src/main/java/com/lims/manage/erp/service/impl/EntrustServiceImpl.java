package com.lims.manage.erp.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EntrustServiceImpl implements EntrustService {

    Logger logger = LoggerFactory.getLogger(EntrustServiceImpl.class);
    @Autowired
    private EntrustEntityMapper entityMapper;
    @Autowired
    TestCompanyDao testCompanyDao;
    @Autowired
    TestCustomerDao testCustomerDao;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    TestProductDao testProductDao;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ProductItemEntityMapper itemEntityMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private TestSampleMixInfoEntityMapper mixInfoEntityMapper;
    @Autowired
    private ReportApprovalMapper reportApprovalMapper;
    @Autowired
    private ReportRecordDetailEntityMapper reportRecordDetailEntityMapper;
    @Autowired
    private ReportRecordEntityMapper recordEntityMapper;
    @Autowired
    private TestSampleEntityService testSampleEntityService;
    @Autowired
    private EntrustFileTableDao entrustFileTableDao;
    @Autowired
    private TestEntrustedTaskRelDao testEntrustedTaskRelDao;

    public static HttpHeaders getHttpHeaders(String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
        return headers;
    }

    /**
     * 新增委托任务
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean addEntrust(EntrustAddVo vo, MultipartFile[] file) {
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        basisInfo.setId(GenID.getID());
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (SampleItemEntity entity : sampleCheckItem) {
                        double money = entity.getTimes() * entity.getUnitPrice();
                        totalMoney = totalMoney + money;
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (SampleItemEntity entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setCountPrice(totalMoney + "");
        entityMapper.insertEntrustInfo(basisInfo);
        return true;
    }

    /**
     * 新增委托 测试
     *
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean addEntrustTest(EntrustAddVo vo, MultipartFile[] file) {

        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        long id = GenID.getID();
        basisInfo.setId(id);
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        // 通过委托编号 查询是否存在
        if (entityMapper.getByData(basisInfo.getEntrustmentNo()) != null) {
            return false;
        }
        // 通过样品ID 查询委托单信息和样品Id 绑定关系 （==null 正常，!=null false）
        if (!CollectionUtils.isEmpty(vo.getSamples())) {
            for (SampleEntity sampleEntity : vo.getSamples()) {
                if (entityMapper.getEntrustIdBySampleId(sampleEntity.getId()) != null) {
                    return false;
                }
            }
        }
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();

            // 根据file文件数量 规定文件名存储编号规则
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, fileCode + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（文件编号&委托文档资料.pdf,文件编号&原始文档.docx）
                stringfileUrlStr.append(fileCode + "&" + name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                basisInfo.setFileUrlStr(substring);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                // 迭代样品下检测项单价信息 如果为空 删除此检测项信息
//                try {
//                    if(!CollectionUtils.isEmpty(sampleCheckItem)){
//                        Iterator<SampleItemEntity> sampleCheckItemList = sampleCheckItem.iterator();
//                        while (sampleCheckItemList.hasNext()){
//                            SampleItemEntity dataItem = sampleCheckItemList.next();
//                            if(dataItem.getUnitPrice()==null){
//                                sampleCheckItemList.remove();
//                            }
//                        }
//                    }
//                }
//                catch (Exception e){
//                    logger.error("删除样品下检测项单价为空时异常");
//                }

                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        List<SampleItemEntity> ItemList = entityMapper.getItemRecursionList(entity.getCheckItemId());
                        //处理检测项 遍历出来的层级数据 拼接层级名。
                        HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity0 : ItemList) {
                                if (entity0.getCheckItemId().equals(entity.getCheckItemId())) {
                                    entity0.setCheckItemName(entity.getCheckItemName());
                                }
                                itemMap.put(entity0.getCheckItemId(), entity0);
                            }
                            for (SampleItemEntity entity2 : ItemList) {
                                SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                                if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                                    // 变更检测项名为： 伪造a-伪造b
                                    entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                                }
                            }
                            //
                        }
                        // 根据检测项id 遍历检测项层级和价格 获取集合
//                        List<SampleItemEntity> ItemList = entityMapper.getyItemList(entity.getCheckItemId());
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity1 : ItemList) {
                                //计算检测项总价钱
                                if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                                     double money = entity.getTimes() * entity1.getUnitPrice();
                                    totalMoney = totalMoney + money;
                                }
                                //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                                entity1.setSampleId(sampleEntity.getId());
                                entity1.setEntrustId(basisInfo.getId());
                                entity1.setMethodId(entity.getMethodId());
                                entity1.setStandardId(entity.getStandardId());
                                entity1.setTimes(entity.getTimes());
//                                if(!entity1.getCheckItemName().equals(entity.getCheckItemName())&&entity1.getUnitPrice()==null){
//                                    entity1.setCheckItemName(entity.getCheckItemName()+"-"+entity1.getCheckItemName());
//                                }
                                // 比对检测项父级名称 进行存储例如：（）。
//                                entity1.setCheckItemName(entity.getCheckItemName());
                            }
                            entityMapper.BatchSaveEntrustSampleItem(ItemList);
                        }

                    }
                }

                //根据委托检测类别关联 配合比检测信息和委托单ID
                if (vo.getEntrustTestType().contains("配合比")) {
                    TestSampleMixInfoEntity record = new TestSampleMixInfoEntity();
                    record.setEntrustmentId(id);
                    record.setSampleId(sampleEntity.getId());
                    mixInfoEntityMapper.updateBySampleId(record);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
//        basisInfo.setCountPrice(totalMoney + "");2022年5月19日修改不在后端计算价格
        basisInfo.setState(0);
        // 解析印章数组
        if (vo.getSealTypes() != null && vo.getSealTypes().length > 0) {
            StringBuilder sealTypes = new StringBuilder();
            for (int i = 0; i < vo.getSealTypes().length; i++) {
                sealTypes.append(vo.getSealTypes()[i]);
                sealTypes.append(",");
            }
            basisInfo.setSealType(sealTypes.deleteCharAt(sealTypes.length() - 1).toString());
        }
        // 通过委托单id 获取公司名称。
        basisInfo.setEntrustCompany(entityMapper.getCompanyNameId(basisInfo.getEntrustCompanyId(), 1));
        if (!StringUtils.isEmpty(basisInfo.getEntrustCompany()) && !StringUtils.isEmpty(basisInfo.getEntrustPeople())  &&!StringUtils.isEmpty(basisInfo.getEntrustPhone())) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getEntrustCompany());
            testCompanyJsonEntity.setContacts(basisInfo.getEntrustPeople());
            testCompanyJsonEntity.setContactWay(basisInfo.getEntrustPhone());
            testCompanyJsonEntity.setType("1");
            String entrustCompanystr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (entrustCompanystr == null) {
                // 保存新的委托联系人姓名 和所属委托单位公司id
//                Integer companyId = entityMapper.getCompanyId(basisInfo.getEntrustCompany(), 1);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(basisInfo.getEntrustCompanyId());
                testCustomerEntity.setContacts(basisInfo.getEntrustPeople());
                testCustomerEntity.setPhone(basisInfo.getEntrustPhone());
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 通过见证单位和类型 查看联系人 （手机号可以不填）
        if (!StringUtils.isEmpty(basisInfo.getWitnessUint()) && !StringUtils.isEmpty(basisInfo.getWitnessPerson())) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getWitnessUint());
            testCompanyJsonEntity.setContacts(basisInfo.getWitnessPerson());
            testCompanyJsonEntity.setContactWay(basisInfo.getWitnessPhone());
            testCompanyJsonEntity.setType("2");
            String WitnessUintstr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (WitnessUintstr == null) {
                // 保存新的见证联系人姓名 和所属见证单位公司id
                Integer companyId = entityMapper.getCompanyId(basisInfo.getWitnessUint(), 2);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(companyId);
                testCustomerEntity.setContacts(basisInfo.getWitnessPerson());
                testCustomerEntity.setPhone(basisInfo.getWitnessPhone());
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        entityMapper.insertEntrustInfo(basisInfo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized String addEntrustTest0620(EntrustAddVo vo, MultipartFile[] file) {
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        long id = GenID.getID();
        basisInfo.setId(id);
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大样品编号
        PageHelper.clearPage();
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        // 通过委托编号 查询是否存在
        PageHelper.clearPage();
        if (entityMapper.getByData(basisInfo.getEntrustmentNo()) != null) {
            return "新增委托失败!:\t委托编号已存在\t"+basisInfo.getEntrustmentNo();
        }
        // 通过样品ID 查询委托单信息和样品Id 绑定关系 （==null 正常，!=null false）
        if (!CollectionUtils.isEmpty(vo.getSamples())) {
            for (SampleEntity sampleEntity : vo.getSamples()) {
                PageHelper.clearPage();
                if (entityMapper.getEntrustIdBySampleId(sampleEntity.getId()) != null) {
                    return "新增委托失败!:\t样品与委托单与建立关系\t"+sampleEntity.getId();
                }
            }
        }
        //附件存在上传附件到服务器
        if (file.length != 0) {
            for (MultipartFile multipartFile : file) {
                // 通过委托单新id 处理附件操作
                uploading(basisInfo.getId(), multipartFile);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        //样品ID
                        entity.setSampleId(sampleEntity.getId());
                        //委托单ID
                        entity.setEntrustId(basisInfo.getId());
                    }
                }
                entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                //根据委托检测类别关联 配合比检测信息和委托单ID
                if (vo.getEntrustTestType().contains("配合比")) {
                    TestSampleMixInfoEntity record = new TestSampleMixInfoEntity();
                    record.setEntrustmentId(id);
                    record.setSampleId(sampleEntity.getId());
                    mixInfoEntityMapper.updateBySampleId(record);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        basisInfo.setState(0);
        // 解析印章数组
        if (vo.getSealTypes() != null && vo.getSealTypes().length > 0) {
            StringBuilder sealTypes = new StringBuilder();
            for (int i = 0; i < vo.getSealTypes().length; i++) {
                sealTypes.append(vo.getSealTypes()[i]);
                sealTypes.append(",");
            }
            basisInfo.setSealType(sealTypes.deleteCharAt(sealTypes.length() - 1).toString());
        }
        // 通过委托单id 获取公司名称。
        basisInfo.setEntrustCompany(entityMapper.getCompanyNameId(basisInfo.getEntrustCompanyId(), 1));
        // 通过委托单位和类型 查看联系人和手机号是否存在
        if (!StringUtils.isEmpty(basisInfo.getEntrustCompany()) && !StringUtils.isEmpty(basisInfo.getEntrustPeople()) && !StringUtils.isEmpty(basisInfo.getEntrustPhone())) {
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getEntrustCompany());
            testCompanyJsonEntity.setContacts(basisInfo.getEntrustPeople());
            testCompanyJsonEntity.setContactWay(basisInfo.getEntrustPhone());
            testCompanyJsonEntity.setType("1");
            PageHelper.clearPage();
            String entrustCompanystr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (entrustCompanystr == null) {
                // 保存新的委托联系人姓名 和所属委托单位公司id
//                Integer companyId = entityMapper.getCompanyId(basisInfo.getEntrustCompany(), 1);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(basisInfo.getEntrustCompanyId());
                testCustomerEntity.setContacts(basisInfo.getEntrustPeople());
                testCustomerEntity.setPhone(basisInfo.getEntrustPhone());
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 通过见证单位和类型 查看联系人 （手机号可以不填）
        if (!StringUtils.isEmpty(basisInfo.getWitnessUint()) && !StringUtils.isEmpty(basisInfo.getWitnessPerson())) {
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getWitnessUint());
            testCompanyJsonEntity.setContacts(basisInfo.getWitnessPerson());
            if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())){
                testCompanyJsonEntity.setContactWay(basisInfo.getWitnessPhone());
            }
            testCompanyJsonEntity.setType("2");
            PageHelper.clearPage();
            String WitnessUintstr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (WitnessUintstr == null) {
                // 保存新的见证联系人姓名 和所属见证单位公司id
                Integer companyId = entityMapper.getCompanyId(basisInfo.getWitnessUint(), 2);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(companyId);
                testCustomerEntity.setContacts(basisInfo.getWitnessPerson());
                if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())){
                    testCustomerEntity.setPhone(basisInfo.getWitnessPhone());
                }
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 获取当前用户所在科室id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        Long department = teamMapper.getTeamIdByUid(userInfo.getUserId());
        // 委托单创建人所属部门
        if(StringUtils.isEmpty(department)){
            basisInfo.setDepartment(null);
        }
        else {
            basisInfo.setDepartment(department);
        }
        entityMapper.insertEntrustInfo(basisInfo);
        return "新建委托成功";
    }


    /**
     * 修改委托
     *
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrust(EntrustAddVo vo, MultipartFile[] file) {
       /* EntrustEntity basisInfo = new EntrustEntity(vo);
        Integer code = vo.getEntrustmentNo();
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                // 去清除 MinIo 桶数据。
                MinIoUtil.deleteFile(BucketsConst.buckets_entrust_enclosure, code + "." + strings[strings.length - 1]);
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        // 刪除的样品id集合
//        List<Integer>  removeSamplesId =  entityMapper.getSampleIdSet(basisInfo.getId());
        // 删除样品id
        entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        //修改样品为未使用
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        // 删除缴费信息
        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 样品下检测依据
        entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (SampleItemEntity entity : sampleCheckItem) {
                        int money = entity.getTimes() * entity.getUnitPrice();
                        totalMoney = totalMoney + money;
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (SampleItemEntity entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }

                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }

        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setPaymentCount(totalMoney + "");
        //存放委托基本信息==》test_entrusted
        entityMapper.updateEntrustInfo(basisInfo);*/
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean updateEntrustTestNew(EntrustAddVo vo, MultipartFile[] file) throws ParseException {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        //附件存在上传附件到服务器
        if (file.length!=0) {
            for (MultipartFile multipartFile : file) {
                // 通过委托单新id 处理附件操作
                uploading(basisInfo.getId(), multipartFile);
            }
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
//            pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //存放委托基本信息==》test_entrusted
        // 解析印章数组
        if (vo.getSealTypes() != null && vo.getSealTypes().length > 0) {
            StringBuilder sealTypes = new StringBuilder();
            for (int i = 0; i < vo.getSealTypes().length; i++) {
                sealTypes.append(vo.getSealTypes()[i]);
                sealTypes.append(",");
            }
            basisInfo.setSealType(sealTypes.deleteCharAt(sealTypes.length() - 1).toString());
        }
        else {
            basisInfo.setSealType(null);
        }
        // 通过委托单id 获取公司名称。
        PageHelper.clearPage();
        basisInfo.setEntrustCompany(entityMapper.getCompanyNameId(basisInfo.getEntrustCompanyId(), 1));
        if (!StringUtils.isEmpty(basisInfo.getEntrustCompany()) && !StringUtils.isEmpty(basisInfo.getEntrustPeople())  &&!StringUtils.isEmpty(basisInfo.getEntrustPhone())) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getEntrustCompany());
            testCompanyJsonEntity.setContacts(basisInfo.getEntrustPeople());
            testCompanyJsonEntity.setContactWay(basisInfo.getEntrustPhone());
            testCompanyJsonEntity.setType("1");
            PageHelper.clearPage();
            String entrustCompanystr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (entrustCompanystr == null) {
                // 保存新的委托联系人姓名 和所属委托单位公司id
//                Integer companyId = entityMapper.getCompanyId(basisInfo.getEntrustCompany(), 1);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(basisInfo.getEntrustCompanyId());
                testCustomerEntity.setContacts(basisInfo.getEntrustPeople());
                testCustomerEntity.setPhone(basisInfo.getEntrustPhone());
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 通过见证单位和类型 查看联系人和手机号是否存在
        if (!StringUtils.isEmpty(basisInfo.getWitnessUint()) && !StringUtils.isEmpty(basisInfo.getWitnessPerson())) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getWitnessUint());
            testCompanyJsonEntity.setContacts(basisInfo.getWitnessPerson());
            if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())) {
                testCompanyJsonEntity.setContactWay(basisInfo.getWitnessPhone());
            }
            testCompanyJsonEntity.setType("2");
            PageHelper.clearPage();
            String WitnessUintstr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (WitnessUintstr == null) {
                // 保存新的见证联系人姓名 和所属见证单位公司id
                PageHelper.clearPage();
                Integer companyId = entityMapper.getCompanyId(basisInfo.getWitnessUint(), 2);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(companyId);
                testCustomerEntity.setContacts(basisInfo.getWitnessPerson());
                if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())) {
                    testCustomerEntity.setPhone(basisInfo.getWitnessPhone());
                }
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        /**
         * 6月27日 update 更新
         */
        // 获取当前用户所在科室id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        Long department = teamMapper.getTeamIdByUid(userInfo.getUserId());
        // 委托单创建人所属部门
        if(StringUtils.isEmpty(department)){
            basisInfo.setDepartment(null);
        }
        else {
            basisInfo.setDepartment(department);
        }
        entityMapper.updateEntrustInfo(basisInfo);
        //修改样品委托单位
        List<Integer> sampleIds = entityMapper.getAllSampleIdentrustmentId(basisInfo.getId());
        if(!CollectionUtils.isEmpty(sampleIds)){
            List<TestSampleEntity> entities = Lists.newArrayList();
            for (int i = 0; i < sampleIds.size(); i++) {
                TestSampleEntity entity = new TestSampleEntity();
                entity.setId(sampleIds.get(i));
                entity.setCompanyId(basisInfo.getEntrustCompanyId());
                entities.add(entity);
            }
            entityMapper.updateSampleCompany(entities);
        }
        // 修改委托信息后： 触发联动效果。 同步更新任务单对应字段。
        methodModifyTheTask(basisInfo.getId());
        // 修改委托信息后： 触发联动效果。同步更新样品信息
        methodModifyTheSample(basisInfo.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSample(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 删除样品id
        // 统计是否存在
        if (entityMapper.countSampleDetailsRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        //修改样品为未使用
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        // 删除判定依据id
        if (entityMapper.countSampleStandardRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        }
        // 删除缴费信息
//        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 样品下检测依据
        if (entityMapper.countSampleCheckitemRel(basisInfo.getId()) > 0) {
            entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());
        }

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                // 样品依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<JudgmentBasisVo> sampleCheckItem = sampleEntity.getJudgmentBasisVoStr();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    //计算检测项总价钱
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        if (entity.getCheckPrice() != null && !entity.getCheckPrice().equals("")) {
                            int money = entity.getTimes() * Integer.parseInt(entity.getCheckPrice());
                            totalMoney = totalMoney + money;
                        }
                    }
                    //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        entity.setSampleId(sampleEntity.getId());
                        entity.setId(basisInfo.getId());
                    }
                    // 判定依据集合转成 符合数据存储 样品依据
                    List<SampleItemEntity> sampleItemList = new ArrayList<>();
                    for (JudgmentBasisVo entity : sampleCheckItem) {
                        SampleItemEntity sampleItemEntity = new SampleItemEntity();
                        sampleItemEntity.setSampleId(entity.getSampleId());
                        sampleItemEntity.setEntrustId(entity.getId());
                        if (entity.getMethodId() != null && entity.getMethodId() >= 0) {
                            sampleItemEntity.setMethodId(entity.getMethodId());
                        }
                        if (entity.getCheckItemId() != null && entity.getCheckItemId() >= 0) {
                            sampleItemEntity.setCheckItemId(entity.getCheckItemId().longValue());
                        }
                        if (entity.getStandardId() != null && entity.getStandardId() >= 0) {
                            sampleItemEntity.setStandardId(entity.getStandardId().intValue());
                        }
                        sampleItemEntity.setTimes(entity.getTimes());
                        if (entity.getCheckPrice() != null && !entity.getCheckPrice().equals("")) {
                            sampleItemEntity.setUnitPrice(Double.parseDouble(entity.getCheckPrice()));
                        }
                        sampleItemList.add(sampleItemEntity);
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleItemList);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
            basisInfo.setPaymentCount(totalMoney + "");
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSampleEnscript1(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 删除样品id
        // 统计是否存在
//        if (entityMapper.countSampleDetailsRel(basisInfo.getId()) > 0) {
//            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
//        }
        // 判断 样品与委托单是否存在
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                //修改样品为未使用
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
            // 1.0 样品与委托单已存在 1.1、删除样品id
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        // 删除缴费信息
//        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 删除样品下检测项
        entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                // 样品依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项集合
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();

                // 迭代样品下检测项单价信息 如果为空 删除此检测项信息
//                try {
//                    if(!CollectionUtils.isEmpty(sampleCheckItem)){
//                        Iterator<SampleItemEntity> sampleCheckItemList = sampleCheckItem.iterator();
//                        while (sampleCheckItemList.hasNext()){
//                            SampleItemEntity dataItem = sampleCheckItemList.next();
//                            if(dataItem.getUnitPrice()==null){
//                                sampleCheckItemList.remove();
//                            }
//                        }
//                    }
//                }
//                catch (Exception e){
//                    logger.error("删除样品下检测项单价为空时异常");
//                }
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        List<SampleItemEntity> ItemList = entityMapper.getItemRecursionList(entity.getCheckItemId());
                        //处理检测项 遍历出来的层级数据 拼接层级名。
                        HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity0 : ItemList) {
                                if (entity0.getCheckItemId().equals(entity.getCheckItemId())) {
                                    entity0.setCheckItemName(entity.getCheckItemName());
                                }
                                itemMap.put(entity0.getCheckItemId(), entity0);
                            }
                            for (SampleItemEntity entity2 : ItemList) {
                                SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                                if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                                    // 变更检测项名为： 伪造a-伪造b
                                    entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                                }
                            }
                            //
                        }
                        // 根据检测项id 遍历检测项层级和价格 获取集合
//                        List<SampleItemEntity> ItemList = entityMapper.getyItemList(entity.getCheckItemId());
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity1 : ItemList) {
                                //计算检测项总价钱
                                if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                                    double money = entity.getTimes() * entity1.getUnitPrice();
                                    totalMoney = totalMoney + money;
                                }
                                //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                                entity1.setSampleId(sampleEntity.getId());
                                entity1.setEntrustId(basisInfo.getId());
                                entity1.setMethodId(entity.getMethodId());
                                entity1.setStandardId(entity.getStandardId());
                                entity1.setTimes(entity.getTimes());
//                                if(!entity1.getCheckItemName().equals(entity.getCheckItemName())&&entity1.getUnitPrice()==null){
//                                    entity1.setCheckItemName(entity.getCheckItemName()+"-"+entity1.getCheckItemName());
//                                }
                                // 比对检测项父级名称 进行存储例如：（）。
//                                entity1.setCheckItemName(entity.getCheckItemName());
                            }
                            entityMapper.BatchSaveEntrustSampleItem(ItemList);
                        }

                    }
                }


            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }


        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
            basisInfo.setPaymentCount(totalMoney + "");
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSampleEnscript(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 判断 样品与委托单是否存在
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                //修改样品为未使用
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
            // 1.0 样品与委托单已存在 1.1、删除样品id
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        // 删除缴费信息
//        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 删除样品下检测项
        entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                // 样品依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项集合
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        List<SampleItemEntity> ItemList = entityMapper.getItemRecursionList(entity.getCheckItemId());
                        //处理检测项 遍历出来的层级数据 拼接层级名。
                        HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity0 : ItemList) {
                                if (entity0.getCheckItemId().equals(entity.getCheckItemId())) {
                                    entity0.setCheckItemName(entity.getCheckItemName());
                                }
                                itemMap.put(entity0.getCheckItemId(), entity0);
                            }
                            for (SampleItemEntity entity2 : ItemList) {
                                SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                                if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                                    // 变更检测项名为： 伪造a-伪造b
                                    entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                                }
                            }
                            //
                        }
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity1 : ItemList) {
                                //计算检测项总价钱
                                if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                                    double money = entity.getTimes() * entity1.getUnitPrice();
                                    totalMoney = totalMoney + money;
                                }
                                //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                                entity1.setSampleId(sampleEntity.getId());
                                entity1.setEntrustId(basisInfo.getId());
                                entity1.setMethodId(entity.getMethodId());
                                entity1.setStandardId(entity.getStandardId());
                                entity1.setTimes(entity.getTimes());
                            }
                            entityMapper.BatchSaveEntrustSampleItem(ItemList);
                        }
                    }
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
//            basisInfo.setPaymentCount(totalMoney + "");2022年5月20日修改，不在后台计算检测项价格
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSampleEnscript0621(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 判断 样品与委托单是否存在
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                //修改样品为未使用
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
            // 1.0 样品与委托单已存在 1.1、删除样品id
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        // 删除缴费信息
//        entityMapper.removeTestEntrustedPaymentRecordInfo(basisInfo.getId());
        // 删除样品下检测项
        entityMapper.removeTestEntrustedSampleCheckitemRel(basisInfo.getId());

        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                sampleEntityMapper.updateSampleUse(sampleEntity.getId(), 1);
                // 样品依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项集合
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        //计算检测项总价钱
                        if (entity.getUnitPrice() != null && entity.getUnitPrice() >= 0) {
                            double money = entity.getTimes() * entity.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
//            basisInfo.setPaymentCount(totalMoney + "");2022年5月20日修改，不在后台计算检测项价格
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }
        return true;
    }

    @Override
    public Boolean updateEntrustCheckItem(EntrustAddVo vo){
//        //委托单当前状态
//        Integer state = entityMapper.getEntrustStateNow(vo.getId());
        //查询当前委托单下的任务单数量
        Integer reportStateTaskNum = entityMapper.getReportStateTaskNum(vo.getId());
        if(reportStateTaskNum>0){//已发布
            return updatePublishedEntrust0711(vo);
        }else{//未发布
            return updateEntrustTestNewSampleEnscript0621(vo);
        }
    }

    /**
     * 修改已分配的检测项
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    Boolean updatePublishedEntrust(EntrustAddVo vo){
        EntrustEntity basisInfo = new EntrustEntity(vo);
        //获取委托单原有信息
        EntrustAddVo oldEntrustInfo = getEntrustHistoryDetailTest(basisInfo.getId());
        //当前委托单状态
        Integer state = oldEntrustInfo.getState();
        //查询报告状态
        String reportState = entityMapper.getReportState(basisInfo.getId());
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        Double totalMoney = 0D;
        //样品
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (int i = 0; i < samples.size(); i++) {
                SampleEntity sampleEntity = samples.get(i);
                SampleEntity sampleEntityOld = oldEntrustInfo.getSamples().get(i);
                //修改样品判定依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //保存样品判定依据
                if (!CollectionUtils.isEmpty(list1)) {
                    entityMapper.BatchSaveSampleStandard(list1);
                }
                //原有检测项信息
                List<SampleItemEntity> sampleCheckItemOld = entityMapper.getAllOldCheckItemInfo(sampleEntityOld.getId(),basisInfo.getId());
                //新检测项信息
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                //存放修改的检测项
                List<SampleItemEntity> updateList = Lists.newArrayList();
                if(!CollectionUtils.isEmpty(sampleCheckItemOld) && !CollectionUtils.isEmpty(sampleCheckItem)){
                    for (int k = 0; k < sampleCheckItemOld.size(); k++) {
                        SampleItemEntity sampleItemEntity = sampleCheckItemOld.get(k);
                        if(sampleItemEntity!=null){
                            Long checkItemId = sampleItemEntity.getCheckItemId();
                            for (int m = 0; m < sampleCheckItem.size(); m++) {
                                SampleItemEntity sampleItemEntity1 = sampleCheckItem.get(m);
                                Long checkItemId1 = sampleItemEntity1.getCheckItemId();
                                if(checkItemId1.equals(checkItemId)){
                                    for (int j = 0; j < sampleCheckItemOld.size(); j++) {
                                        SampleItemEntity old = sampleCheckItemOld.get(j);
                                        if(old != null && old.getCheckItemName().contains(sampleItemEntity1.getCheckItemName())){
                                            old.setTimes(sampleItemEntity1.getTimes());//修改次数
                                            old.setStandardId(sampleItemEntity1.getStandardId());//修改检测依据
                                            updateList.add(old);
                                            sampleCheckItemOld.set(j,null);
                                            if(CollectionUtils.isEmpty(sampleCheckItemOld)){
                                                break;
                                            }
                                        }
                                    }
                                    updateList.add(sampleItemEntity1);
                                    sampleCheckItemOld.set(k,null);
//                                sampleCheckItemOld.remove(sampleItemEntity);
                                    sampleCheckItem.remove(sampleItemEntity1);
                                }
                                if(CollectionUtils.isEmpty(sampleCheckItem)){
                                    break;
                                }
                            }
                            if(CollectionUtils.isEmpty(sampleCheckItemOld)){
                                break;
                            }
                        }
                    }
                }
                //增加新的检测项
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        List<SampleItemEntity> ItemList = entityMapper.getItemRecursionList(entity.getCheckItemId());
                        //处理检测项 遍历出来的层级数据 拼接层级名。
                        HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity0 : ItemList) {
                                if (entity0.getCheckItemId().equals(entity.getCheckItemId())) {
                                    entity0.setCheckItemName(entity.getCheckItemName());
                                }
                                itemMap.put(entity0.getCheckItemId(), entity0);
                            }
                            for (SampleItemEntity entity2 : ItemList) {
                                SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                                if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                                    // 变更检测项名为： 伪造a-伪造b
                                    entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                                }
                            }
                        }
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity1 : ItemList) {
                                //计算检测项总价钱
                                if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                                    double money = entity.getTimes() * entity1.getUnitPrice();
                                    totalMoney = totalMoney + money;
                                }
                                //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                                entity1.setSampleId(sampleEntity.getId());
                                entity1.setEntrustId(basisInfo.getId());
                                entity1.setMethodId(entity.getMethodId());
                                entity1.setStandardId(entity.getStandardId());
                                entity1.setTimes(entity.getTimes());
                            }
                            entityMapper.BatchSaveEntrustSampleItem(ItemList);
                        }
                    }
                    state = 0;
                    //修改报告的状态，和审批，复核信息
                    if(!"2".equals(reportState)){
                        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                        reportApprovalVo.setState(2);
                        reportApprovalVo.setEntrustmentId(basisInfo.getId());
                        reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                    }
                }
                //修改原有检测项
                if (!CollectionUtils.isEmpty(updateList)){
                    for (SampleItemEntity entity1 : updateList) {
                        //计算检测项总价钱
                        if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                            double money = entity1.getTimes() * entity1.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                    }
                    entityMapper.batchUpdateEntrustSampleItem(updateList);
                }

                //删除原有检测项——判断是否删除有子检测项的
                if (sampleCheckItemOld != null && !CollectionUtils.isEmpty(sampleCheckItemOld)){
                    //
                    for (int j = 0; j < sampleCheckItemOld.size(); j++) {
                        SampleItemEntity sampleItemEntity = sampleCheckItemOld.get(i);
                        if(sampleItemEntity == null ){
                            sampleCheckItemOld.remove(sampleItemEntity);
                        }
                    }
                    //删除委托检测项表中的检测项
                    entityMapper.batchDeleteEntrustSampleItem(sampleCheckItemOld);
                    //根据委托单Id查询报告数据主键
                    List<ReportRecordDetailEntity> detailEntityList = Lists.newArrayList();
                    Long reportId = entityMapper.getReportId(basisInfo.getId());
                    for (SampleItemEntity sampleItemEntity : sampleCheckItemOld) {
                        ReportRecordDetailEntity entity = new ReportRecordDetailEntity();
                        if(sampleItemEntity != null){
                            entity.setCheckItemId(sampleItemEntity.getCheckItemId());
                            entity.setRecordId(reportId);
                            detailEntityList.add(entity);
                        }
                    }
                    //并且删除报告详情表中关联检测项
                    if(!CollectionUtils.isEmpty(detailEntityList)){
                        reportRecordDetailEntityMapper.deleteByEntrustIdandCheckItemId(detailEntityList);
                    }
                    //修改报告的状态，和审批，复核信息
                    if(!"2".equals(reportState)){
                        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                        reportApprovalVo.setState(2);
                        reportApprovalVo.setEntrustmentId(basisInfo.getId());
                        reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                    }
                }
            }
        }
        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
//            basisInfo.setPaymentCount(totalMoney + "");2022年5月20日修改，委托单价格不在后台计算
            basisInfo.setState(state);
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }
        return true;
    }
    /**
     * 修改已分配的检测项-0621
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    Boolean updatePublishedEntrust0621(EntrustAddVo vo){
        EntrustEntity basisInfo = new EntrustEntity(vo);
        //获取委托单原有信息
        EntrustAddVo oldEntrustInfo = getEntrustHistoryDetailTest(basisInfo.getId());
        //当前委托单状态
        Integer state = oldEntrustInfo.getState();
        //查询报告状态
        String reportState = entityMapper.getReportState(basisInfo.getId());
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        Double totalMoney = 0D;
        //样品
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            //存放要删除的检测项
            List<SampleItemEntity> deleteCheckItems = Lists.newArrayList();
            //存放最新的检测项
            List<SampleItemEntity> allNewCheckItems = Lists.newArrayList();
            //处理检测项
            for (int i = 0; i < samples.size(); i++) {

                SampleEntity sampleEntity = samples.get(i);
                SampleEntity sampleEntityOld = oldEntrustInfo.getSamples().get(i);
                //修改样品判定依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //保存样品判定依据
                if (!CollectionUtils.isEmpty(list1)) {
                    entityMapper.BatchSaveSampleStandard(list1);
                }
                //原有检测项信息
                List<SampleItemEntity> sampleCheckItemOld = entityMapper.getAllOldCheckItemInfo(sampleEntityOld.getId(),basisInfo.getId());
                //新检测项信息
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                allNewCheckItems.addAll(sampleCheckItem);
                //存放修改的检测项
                List<SampleItemEntity> updateList = Lists.newArrayList();
                if(!CollectionUtils.isEmpty(sampleCheckItemOld) && !CollectionUtils.isEmpty(sampleCheckItem)){
                    for (int k = 0; k < sampleCheckItemOld.size(); k++) {
                        SampleItemEntity sampleItemEntity = sampleCheckItemOld.get(k);
                        if(sampleItemEntity!=null){
                            Long checkItemId = sampleItemEntity.getCheckItemId();
                            for (int m = 0; m < sampleCheckItem.size(); m++) {
                                SampleItemEntity sampleItemEntity1 = sampleCheckItem.get(m);
                                Long checkItemId1 = sampleItemEntity1.getCheckItemId();
                                if(checkItemId1.equals(checkItemId)){
                                    for (int j = 0; j < sampleCheckItemOld.size(); j++) {
                                        SampleItemEntity old = sampleCheckItemOld.get(j);
                                        if(old != null && old.getCheckItemName().contains(sampleItemEntity1.getCheckItemName())){
                                            old.setTimes(sampleItemEntity1.getTimes());//修改次数
                                            old.setStandardId(sampleItemEntity1.getStandardId());//修改检测依据
                                            updateList.add(old);
                                            sampleCheckItemOld.set(j,null);
                                            if(CollectionUtils.isEmpty(sampleCheckItemOld)){
                                                break;
                                            }
                                        }
                                    }
                                    updateList.add(sampleItemEntity1);
                                    sampleCheckItemOld.set(k,null);
//                                sampleCheckItemOld.remove(sampleItemEntity);
                                    sampleCheckItem.remove(sampleItemEntity1);
                                }
                                if(CollectionUtils.isEmpty(sampleCheckItem)){
                                    break;
                                }
                            }
                            if(CollectionUtils.isEmpty(sampleCheckItemOld)){
                                break;
                            }
                        }
                    }
                }
                //增加新的检测项
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    for (SampleItemEntity entity : sampleCheckItem) {
                        //计算检测项总价钱
                        if (entity.getUnitPrice() != null && entity.getUnitPrice() >= 0) {
                            double money = entity.getTimes() * entity.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                    }
                    entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                    state = 0;
                    //修改报告的状态，和审批，复核信息
                    if(!"2".equals(reportState)){
                        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                        reportApprovalVo.setState(2);
                        reportApprovalVo.setEntrustmentId(basisInfo.getId());
                        reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                    }
                }
                //修改原有检测项
                if (!CollectionUtils.isEmpty(updateList)){
                    for (SampleItemEntity entity1 : updateList) {
                        //计算检测项总价钱
                        if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                            double money = entity1.getTimes() * entity1.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                    }
                    entityMapper.batchUpdateEntrustSampleItem(updateList);
                }

                //删除原有检测项——判断是否删除有子检测项的
                if (sampleCheckItemOld != null && !CollectionUtils.isEmpty(sampleCheckItemOld)){
                    //
                    List<SampleItemEntity> temp = Lists.newArrayList();
                    for (int j = 0; j < sampleCheckItemOld.size(); j++) {
                        SampleItemEntity sampleItemEntity = sampleCheckItemOld.get(i);
                        if(sampleItemEntity != null ){
                            temp.add(sampleItemEntity);
                        }
                    }
                    //把要删除的检测项存放到循环外
                    deleteCheckItems.addAll(temp);
                    //删除委托检测项表中的检测项
                    if(!CollectionUtils.isEmpty(temp)){
                        entityMapper.batchDeleteEntrustSampleItem(temp);
                    }
                    //根据委托单Id查询报告数据主键
                    List<ReportRecordDetailEntity> detailEntityList = Lists.newArrayList();
                    Long reportId = entityMapper.getReportId(basisInfo.getId());
                    for (SampleItemEntity sampleItemEntity : sampleCheckItemOld) {
                        ReportRecordDetailEntity entity = new ReportRecordDetailEntity();
                        if(sampleItemEntity != null){
                            entity.setCheckItemId(sampleItemEntity.getCheckItemId());
                            entity.setRecordId(reportId);
                            detailEntityList.add(entity);
                        }
                    }
                    //并且删除报告详情表中关联检测项
                    if(!CollectionUtils.isEmpty(detailEntityList)){
                        reportRecordDetailEntityMapper.deleteByEntrustIdandCheckItemId(detailEntityList);
                    }
                    //修改报告的状态，和审批，复核信息
                    if(!"2".equals(reportState)){
                        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                        reportApprovalVo.setState(2);
                        reportApprovalVo.setEntrustmentId(basisInfo.getId());
                        reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                    }
                }
            }
            //处理任务单价格
            List<Long> taskIds = Lists.newArrayList();
            if(!CollectionUtils.isEmpty(allNewCheckItems)){
                for (SampleItemEntity sampleItemEntity : allNewCheckItems) {
                    taskIds.add(sampleItemEntity.getTaskId());
                }
            }
//            Map<Long,Double> taskPriceMap = Maps.newHashMap();
            List<TaskPriceVo> priceVos = Lists.newArrayList();
            if(!CollectionUtils.isEmpty(taskIds)){
                for (Long taskId : taskIds) {
                    double taskPrice = 0D;
                    for (SampleItemEntity sampleItemEntity : allNewCheckItems) {
                        if(Objects.equals(taskId, sampleItemEntity.getTaskId())){
                            double price = sampleItemEntity.getUnitPrice() * sampleItemEntity.getTimes() * Double.parseDouble(vo.getDiscount());
                            taskPrice = taskPrice + price;
                        }
                    }
//                    taskPriceMap.put(taskId,taskPrice);
                    TaskPriceVo taskPriceVo = new TaskPriceVo(taskId,taskPrice);
                    priceVos.add(taskPriceVo);
                }
            }
            //处理老检测项价钱
            if(!CollectionUtils.isEmpty(priceVos)){
                for (TaskPriceVo taskPriceVo : priceVos) {
                    Double price = taskPriceVo.getPrice();
                    Long taskId = taskPriceVo.getTaskId();
                    if (!CollectionUtils.isEmpty(deleteCheckItems)) {
                        for (SampleItemEntity sampleItemEntity : deleteCheckItems) {
                            if(sampleItemEntity != null){
                                Integer state1 = sampleItemEntity.getState();
                                Long taskId1 = sampleItemEntity.getTaskId();
                                if (state1 >= 2) {//检测项已完成
                                    double completePrice = sampleItemEntity.getUnitPrice() * sampleItemEntity.getTimes() * Double.parseDouble(vo.getDiscount());
                                    if (Objects.equals(taskId1, taskId)) {
                                        price = price + completePrice;
                                    }
                                }
                            }
                        }
                    }
                    taskPriceVo.setPrice(price);
                }
                //批量更新任务单价格
                entityMapper.batchUpdateTaskPrice(priceVos);
            }
        }
        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
//            basisInfo.setPaymentCount(totalMoney + "");2022年5月20日修改，委托单价格不在后台计算
            basisInfo.setState(state);
            //存放委托基本信息==》test_entrusted
            entityMapper.updateEntrustInfo(basisInfo);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    Boolean updatePublishedEntrust0711(EntrustAddVo vo){
        EntrustEntity basisInfo = new EntrustEntity(vo);
        //获取委托单原有信息
        EntrustAddVo oldEntrustInfo = getEntrustHistoryDetailTest(basisInfo.getId());
        //当前委托单状态
        Integer state = oldEntrustInfo.getState();
        //查询报告状态
        String reportState = entityMapper.getReportState(basisInfo.getId());
        // 删除判定依据id
        entityMapper.removeTestEntrustedSampleStandardRel(basisInfo.getId());
        int totalMoney = 0;
        boolean flag = false;
        //样品
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            //存放要删除的检测项
            List<SampleItemEntity> deleteCheckItems = Lists.newArrayList();
            //存放最新的检测项
            List<SampleItemEntity> allNewCheckItems = Lists.newArrayList();
            //处理检测项
            for (int i = 0; i < samples.size(); i++) {
                SampleEntity sampleEntity = samples.get(i);
                SampleEntity sampleEntityOld = oldEntrustInfo.getSamples().get(i);
                //修改样品判定依据
                List<JudgmentBasisVo> standardFileIds = sampleEntity.getStandardFileIdStr();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (JudgmentBasisVo integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer.getStandardId());
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //保存样品判定依据
                if (!CollectionUtils.isEmpty(list1)) {
                    entityMapper.BatchSaveSampleStandard(list1);
                }
                //原有检测项信息
                List<SampleItemEntity> sampleCheckItemOld = entityMapper.getAllOldCheckItemInfo(sampleEntityOld.getId(),basisInfo.getId());
                //新检测项信息
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                allNewCheckItems.addAll(sampleCheckItem);
                //存放修改的检测项
                List<SampleItemEntity> updateList = Lists.newArrayList();
                if(!CollectionUtils.isEmpty(sampleCheckItemOld) && !CollectionUtils.isEmpty(sampleCheckItem)){
                    for (int k = 0; k < sampleCheckItemOld.size(); k++) {
                        SampleItemEntity oldItem = sampleCheckItemOld.get(k);
                        if(oldItem!=null){
                            Long oldItemId = oldItem.getCheckItemId();
                            for (int m = 0; m < sampleCheckItem.size(); m++) {
                                SampleItemEntity newItem = sampleCheckItem.get(m);
                                if(newItem != null){
                                    Long newItemId = newItem.getCheckItemId();
                                    if(newItemId.equals(oldItemId)){
                                        newItem.setSampleId(oldItem.getSampleId());
                                        newItem.setEntrustId(oldItem.getEntrustId());
                                        updateList.add(newItem);
                                        sampleCheckItemOld.set(k,null);
                                        sampleCheckItem.set(m,null);
                                    }
                                }
                            }
                        }
                    }
                }
                //增加新的检测项
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    List<SampleItemEntity> saveList = Lists.newArrayList();
                    for (SampleItemEntity entity : sampleCheckItem) {
                        if(entity != null){
                            //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                            entity.setSampleId(sampleEntity.getId());
                            entity.setEntrustId(basisInfo.getId());
                            saveList.add(entity);
                        }
                    }
                    if(!CollectionUtils.isEmpty(saveList)){
                        entityMapper.BatchSaveEntrustSampleItem(saveList);
                        state = 0;
                        flag = true;
                        //修改报告的状态，和审批，复核信息
                        if(!"2".equals(reportState)){
                            ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                            reportApprovalVo.setState(2);
                            reportApprovalVo.setEntrustmentId(basisInfo.getId());
                            reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                        }
                    }
                }
                //修改原有检测项
                if (!CollectionUtils.isEmpty(updateList)){
                    entityMapper.batchUpdateEntrustSampleItem(updateList);
                }
                //删除原有检测项——判断是否删除有子检测项的
                if (sampleCheckItemOld != null && !CollectionUtils.isEmpty(sampleCheckItemOld)){
                    List<SampleItemEntity> temp = Lists.newArrayList();
                    for (SampleItemEntity sampleItemEntity : sampleCheckItemOld) {
                        if (sampleItemEntity != null) {
                            temp.add(sampleItemEntity);
                        }
                    }
                    //把要删除的检测项存放到循环外
                    deleteCheckItems.addAll(temp);
                    //删除委托检测项表中的检测项
                    if(!CollectionUtils.isEmpty(temp)){
                        entityMapper.batchDeleteEntrustSampleItem(temp);
                    }
                    //根据委托单Id查询报告数据主键
                    List<ReportRecordDetailEntity> detailEntityList = Lists.newArrayList();
                    Long reportId = entityMapper.getReportId(basisInfo.getId());
                    for (SampleItemEntity sampleItemEntity : sampleCheckItemOld) {
                        ReportRecordDetailEntity entity = new ReportRecordDetailEntity();
                        if(sampleItemEntity != null){
                            entity.setCheckItemId(sampleItemEntity.getCheckItemId());
                            entity.setRecordId(reportId);
                            entity.setSampleId(sampleItemEntity.getSampleId());
                            detailEntityList.add(entity);
                        }
                    }
                    //并且删除报告详情表中关联检测项
                    if(!CollectionUtils.isEmpty(detailEntityList)){
                        reportRecordDetailEntityMapper.deleteByEntrustIdandCheckItemId(detailEntityList);
                    }
                    //修改报告的状态，和审批，复核信息
                    if(!"2".equals(reportState)){
                        ReportApprovalVo reportApprovalVo = new ReportApprovalVo();
                        reportApprovalVo.setState(2);
                        reportApprovalVo.setEntrustmentId(basisInfo.getId());
                        reportApprovalMapper.updateentrustAndApprovalMonad(reportApprovalVo);
                    }
                }
            }
            //处理任务单价格
            List<Long> taskIds = Lists.newArrayList();
            if(!CollectionUtils.isEmpty(allNewCheckItems)){
                for (SampleItemEntity sampleItemEntity : allNewCheckItems) {
                    taskIds.add(sampleItemEntity.getTaskId());
                }
            }
            List<TaskPriceVo> priceVos = Lists.newArrayList();
            if(!CollectionUtils.isEmpty(taskIds)){
                for (Long taskId : taskIds) {
                    double taskPrice = 0D;
                    for (SampleItemEntity sampleItemEntity : allNewCheckItems) {
                        if(Objects.equals(taskId, sampleItemEntity.getTaskId())){
                            double price = sampleItemEntity.getUnitPrice() * sampleItemEntity.getTimes() * Double.parseDouble(vo.getDiscount());
                            taskPrice = taskPrice + price;
                        }
                    }
                    TaskPriceVo taskPriceVo = new TaskPriceVo(taskId,taskPrice);
                    priceVos.add(taskPriceVo);
                }
            }
            //处理老检测项价钱
            if(!CollectionUtils.isEmpty(priceVos)){
                for (TaskPriceVo taskPriceVo : priceVos) {
                    Double price = taskPriceVo.getPrice();
                    Long taskId = taskPriceVo.getTaskId();
                    if (!CollectionUtils.isEmpty(deleteCheckItems)) {
                        for (SampleItemEntity sampleItemEntity : deleteCheckItems) {
                            if(sampleItemEntity != null){
                                Integer state1 = sampleItemEntity.getState();
                                Long taskId1 = sampleItemEntity.getTaskId();
                                if (state1 >= 2) {//检测项已完成
                                    double completePrice = sampleItemEntity.getUnitPrice() * sampleItemEntity.getTimes() * Double.parseDouble(vo.getDiscount());
                                    if (Objects.equals(taskId1, taskId)) {
                                        price = price + completePrice;
                                    }
                                }
                            }
                        }
                    }
                    taskPriceVo.setPrice(price);
                }
                //批量更新任务单价格
                entityMapper.batchUpdateTaskPrice(priceVos);
            }
        }
//        if (flag) {
//
//        }
        basisInfo.setState(state);
        entityMapper.updateEntrustInfo(basisInfo);
        return true;
    }

    @Override
    public String abandonEntrust(EntrustEntity entrustEntity) {
        //查询当前委托单下的任务单数量
        Integer reportStateTaskNum = entityMapper.getReportStateTaskNum(entrustEntity.getId());
        if(reportStateTaskNum>0){
            //已发布
            return "作废委托失败！:\t 委托单已经发布";
         }
        entrustEntity.setState(144);
        entityMapper.updateEntrustInfo(entrustEntity);
        List<Integer> sampleIds = entityMapper.getSampleId(entrustEntity.getId());
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                sampleEntityMapper.updateSampleUse(sampleId, 0);
            }
        }
        // 删除 委托单id与样品id 中间表关系。
        if (!CollectionUtils.isEmpty(sampleIds)) {
            // 1.0 样品与委托单已存在 1.1、删除样品id
            entityMapper.removeTestEntrustedSampleDetailsRel(entrustEntity.getId());
        }
        return "作废委托单成功";
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids) {
        return itemEntityMapper.getItemInfo2(ids);
    }

    @Override
    public Map<String, List<LabelValueVo>> getItemMethodStandard(Integer id) {
        Map<String, List<LabelValueVo>> result = Maps.newHashMap();
        List<LabelValueVo> itemMethod = itemEntityMapper.getItemMethod(id);
        List<LabelValueVo> itemStandard = itemEntityMapper.getItemStandard(id);
        result.put("itemMethod", itemMethod);
        result.put("itemStandard", itemStandard);
        return result;
    }

    @Override
    public Map<String, List<LabelValueVo>> returnEntrustData() {
        Map<String, List<LabelValueVo>> map = new HashMap<>();
        // type =1 委托单位
        List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // type =2 见证单位
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        List<TestInitDataEntity> ReturnBasisData = testCompanyDao.selectEntrustBasis();
        // 团队信息
        List<LabelValueVo> arryTeam = testCompanyDao.selectTestTeam();
//        1 = 委托方式： 2=取样方式 ： 3=检测目的：4 ： 取报告方式 5：样品外观  6：签章类型： 7：用户来源： 8：设备类型  9：支付方式
        // 委托方式 type =1
        List<LabelValueVo> arryEntrust = new ArrayList<>();
        // 取样方式 type =2
        List<LabelValueVo> arrySampling = new ArrayList<>();
        // 3=检测目的
        List<LabelValueVo> arryCheckout = new ArrayList<>();
        // 4=取报告方式
        List<LabelValueVo> arryGetReport = new ArrayList<>();
        // 5：样品外观
        List<LabelValueVo> arrySampleAppearance = new ArrayList<>();
        // 6：签章类型：
        List<LabelValueVo> arrySeal = new ArrayList<>();
        // 8：设备类型
        List<LabelValueVo> arryEquipment = new ArrayList<>();
        // 11：支付方式
        List<LabelValueVo> arryPayment = new ArrayList<>();
        for (TestInitDataEntity testInitDataEntity : ReturnBasisData) {
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 1:
                    arryEntrust.add(labelValueVo);
                    break;
                case 2:
                    arrySampling.add(labelValueVo);
                    break;
                case 3:
                    arryCheckout.add(labelValueVo);
                    break;
                case 4:
                    arryGetReport.add(labelValueVo);
                    break;
                case 5:
                    arrySampleAppearance.add(labelValueVo);
                    break;
                case 6:
                    arrySeal.add(labelValueVo);
                    break;
                case 11:
                    arryPayment.add(labelValueVo);
                    break;
                default:
                    break;
            }
        }
        map.put("entrustCompany", EntrustCompany);
        map.put("witnessCompany", witnessCompany);
        map.put("arryEntrust", arryEntrust);
        map.put("arrySampling", arrySampling);
        map.put("arryCheckout", arryCheckout);
        map.put("arryGetReport", arryGetReport);
        map.put("arrySampleAppearance", arrySampleAppearance);
        map.put("arrySeal", arrySeal);
        map.put("arryTeam", arryTeam);
        map.put("arryPayment", arryPayment);
        return map;
    }

    @Override
    public List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId) {
        return testCompanyDao.selectPeopleInformation(companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity) {
        TestCompanyEntity testCompanyEntity1 = new TestCompanyEntity();
        testCompanyEntity1.setCompanyName(testCompanyEntity.getCompanyName());
        testCompanyEntity1.setType(testCompanyEntity.getType());
        int statusNumber = testCompanyDao.insert(testCompanyEntity1);
        if (statusNumber >= 1) {
            TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
            testCustomerEntity.setCompanyId(testCompanyEntity1.getCompanyId());
            testCustomerEntity.setContacts(testCompanyEntity.getContacts());
            testCustomerEntity.setPhone(testCompanyEntity.getContactWay());
            int addCustomer = testCustomerDao.insertTestCustomer(testCustomerEntity);
            if (addCustomer >= 1) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCompanyDataTwo(TestCompanyJsonEntity testCompanyEntity) {
        TestCompanyEntity testCompanyEntity1 = new TestCompanyEntity();
        testCompanyEntity1.setCompanyName(testCompanyEntity.getCompanyName());
        testCompanyEntity1.setType(testCompanyEntity.getType());
        testCompanyEntity1.setAddress(testCompanyEntity.getAddress());
        testCompanyEntity1.setAddTime(new java.util.Date());
        testCompanyDao.insert(testCompanyEntity1);
        return true;
    }

    @Override
    public PageInfo getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getDateInterval() != null) {
            String[] strArry = entrustHistoryEntity.getDateInterval().split("~");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i <= strArry.length - 1; i++) {
                if (i == 0) {
                    entrustHistoryEntity.setStartDate(dateFormat.parse(strArry[i]));
                }
                if (i == 1) {
                    entrustHistoryEntity.setEndingDate(dateFormat.parse(strArry[i]));
                }
            }
        }
        // 获取状态
        List<EntrustHistoryEntity> dataList = new ArrayList<>();
        if (!StringUtils.isEmpty(entrustHistoryEntity.getState())&&entrustHistoryEntity.getState() == 1) {
//            PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
            PageHelper.clearPage();
            dataList = entityMapper.selectEntrustHistoryTaskListRelease_of(entrustHistoryEntity);
            //存放任务编号
//            if(!CollectionUtils.isEmpty(dataList)){
//                for (EntrustHistoryEntity entrustHistoryEntity1 : dataList) {
//                    entrustHistoryEntity1.setTaskCodes(entityMapper.getTaskCode(entrustHistoryEntity1.getId()));
//                }
//            }
            PageInfo<EntrustHistoryEntity> result = PageInfoUtils.list2PageInfo(dataList, entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
//            PageInfo<EntrustHistoryEntity> result = new PageInfo<>(dataList);
            return result;
        }
//        PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
        PageHelper.clearPage();
        dataList = entityMapper.selectEntrustTaskHistoryList(entrustHistoryEntity);
//        if(!CollectionUtils.isEmpty(dataList)){
//            for (EntrustHistoryEntity entrustHistoryEntity1 : dataList) {
//                entrustHistoryEntity1.setTaskCodes(entityMapper.getTaskCode(entrustHistoryEntity1.getId()));
//            }
//        }
//        PageInfo<EntrustHistoryEntity> result = new PageInfo<>(dataList);
        PageInfo<EntrustHistoryEntity> result = PageInfoUtils.list2PageInfo(dataList, entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
        return result;
    }

    @Override
    public PageInfo getEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity) throws ParseException {
        entrustHistoryEntity.setState(0);
        if (entrustHistoryEntity.getDateInterval() != null) {
            String[] strArry = entrustHistoryEntity.getDateInterval().split("~");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i <= strArry.length - 1; i++) {
                if (i == 0) {
                    entrustHistoryEntity.setStartDate(dateFormat.parse(strArry[i]));
                }
                if (i == 1) {
                    entrustHistoryEntity.setEndingDate(dateFormat.parse(strArry[i]));
                }
            }
        }
        PageHelper.clearPage();
        PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
        List<EntrustHistoryTaskEntity> dataList = entityMapper.selectEntrustReleasedList(entrustHistoryEntity);
        if(!CollectionUtils.isEmpty(dataList)){
            for (EntrustHistoryTaskEntity entity : dataList) {
                List<EntrustSampleInfoVo> entrustSampleInfos = Lists.newArrayList();
                entrustSampleInfos = entityMapper.getEntrustSampleInfos(entity.getId());
                entity.setSampleInfoVos(entrustSampleInfos);
            }
        }
        PageInfo<EntrustHistoryTaskEntity> result = new PageInfo<>(dataList);
        return result;
    }

    /**
     * 查询委托单详情。
     *
     * @param entrustmentId
     * @return
     */
    @Override
    public EntrustAddVo getEntrustHistoryDetail(Long entrustmentId) {
        PageHelper.clearPage();
        //暂存配合比下的的样品信息
        List<TestSampleEntity> nodeSample = Lists.newArrayList();
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        //查询实际缴费
        String total = entityMapper.getRecordCountById(entrustmentId);
        if(total!=null&&total.length()>0){
            entrustAddVo.setPaymentRecordShow(total);
            entrustAddVo.setPaymentRecord(total);
        }
        else {
            entrustAddVo.setPaymentRecordShow("——");
            entrustAddVo.setPaymentRecord("——");
        }
        /**
         * 委托单文件file 处理
         * 通过委托单id 查询相应附件集合
         */
        List<EntrustFileTableEntity> fileList = entrustFileTableDao.getEntrustFileTableEntityList(entrustAddVo.getId());
        if(CollectionUtils.isEmpty(fileList)){
            // 返回空集合
            List<EntrustFileTableEntity> fileListNull = new ArrayList<>();
            entrustAddVo.setFileArrays(fileListNull);
        }
        else {
            entrustAddVo.setFileArrays(fileList);
        }
        if (entrustAddVo.getOperateUser() != null) {
            // 获取做废人id 查询账号姓名
            entrustAddVo.setOperateUserStr(sysUserDao.getSysUserName(entrustAddVo.getOperateUser()));
        }
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        //暂存配合比下的的样品信息
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            // 根据 委托单状态 进行选择项查询 0&&144 查询默认部门信息 state =1 查询所属指定部门信息
//            if (entrustAddVo.getState() == 0 || entrustAddVo.getState() == 144) {
//                List<JudgmentBasisVo> list = sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId);
//                if (list != null && !list.isEmpty()) {
//                    // 根据检测项id 查询 默认匹配部门信息
////                    for (JudgmentBasisVo data : list) {
//////                        List<String> strings = sampleEntityMapper.getTeamNameStrings(data.getCheckItemId());
//////                        data.setTestingRoom(strings.toString());
////                        data.setTestingRoom("——");
////                    }
//                    sampleEntity.setJudgmentBasisVos(list);
////                    //根据检测项ID查询可做该检测项的科室labelvalue集合
////                    for (JudgmentBasisVo data : list) {
////                        List<LabelValueVo> testingRoomList = sampleEntityMapper.getTestingRoomList(data.getCheckItemId());
////                        data.setTestingRoomList(testingRoomList);
////                    }
////                    sampleEntity.setJudgmentBasisVos(list);
//                }
//            } else {
//            }
            sampleEntity.setJudgmentBasisVos(sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId));
            // 补充样品下 依据集合
            sampleEntity.setStandardFileIds(sampleEntityMapper.getSampleBasisSet(sampleEntity.getId(), entrustAddVo.getId()));
            //补充配合比下的的样品信息
            if (sampleEntity.getSampleType().contains("配合比")) {
                nodeSample.addAll(testSampleEntityMapper.selectByPid(sampleEntity.getId()));
            }
        }
        entrustAddVo.setSamples(sampleCollection);
        entrustAddVo.setNodeSample(nodeSample);
        //查询当前委托任务信息
        List<TaskProgressVo> taskProgressList = dealTaskState(entrustmentId);
        entrustAddVo.setTaskProgressList(taskProgressList);
        //查询当前委托报告信息
        ReportProgressVo reportProgressVo = dealReportState(entrustmentId);
        entrustAddVo.setReportProgress(reportProgressVo);
        return entrustAddVo;
    }

    /**
     * 处理任务进度展示信息
     * @param entrustmentId
     * --数据库存放的状态
     * 0：任务发布
     * 1：任务领取
     * 3：试验开始
     * 4：实验完成
     * 6：复核完成
     *--转换成页面所需状态
     * 0：任务发布
     * 1：任务领取
     * 2：试验开始
     * 3：实验完成
     * 4：复核完成
     * @return
     */
    private List<TaskProgressVo> dealTaskState(Long entrustmentId){
        List<TaskProgressVo> taskProgressList = Lists.newArrayList();
        taskProgressList = taskMapper.getTaskStateByEntrustId(entrustmentId);
        if(!CollectionUtils.isEmpty(taskProgressList)){
            for (TaskProgressVo taskProgressVo : taskProgressList) {
                Integer state = taskProgressVo.getState();
                if (state == 3) {
                    taskProgressVo.setState(2);
                } else if (state == 4) {
                    taskProgressVo.setState(3);
                } else if (state == 6) {
                    taskProgressVo.setState(4);
                }
                List<TaskProgressStateVo> stateVoList = Lists.newArrayList();
                for (int j = 0; j <= 4; j++) {
                    if (j == 0) {
                        TaskProgressStateVo vo = new TaskProgressStateVo("任务发布", taskProgressVo.getOrderTime());
                        stateVoList.add(vo);
                    } else if (j == 1) {
                        TaskProgressStateVo vo = new TaskProgressStateVo("任务领取", taskProgressVo.getReceiveTime());
                        stateVoList.add(vo);
                    } else if (j == 2) {
                        TaskProgressStateVo vo = new TaskProgressStateVo("试验开始", taskProgressVo.getStartDetectionTime());
                        stateVoList.add(vo);
                    } else if (j == 3) {
                        TaskProgressStateVo vo = new TaskProgressStateVo("试验完成", taskProgressVo.getEndDetectionTime());
                        stateVoList.add(vo);
                    } else if (j == 4) {
                        TaskProgressStateVo vo = new TaskProgressStateVo("复核完成", taskProgressVo.getReviewTime());
                        stateVoList.add(vo);
                    }
                }
                taskProgressVo.setStateVoList(stateVoList);
                // 根据任务单 id 查 流转单 列表
                List<TestEntrustedTaskRelEntity> taskOrderFlowList = Lists.newArrayList();
                taskOrderFlowList = testEntrustedTaskRelDao.getTaskList(taskProgressVo.getTaskId());
                if(!CollectionUtils.isEmpty(taskOrderFlowList)){
                    for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity :taskOrderFlowList){
                        // 处理信息 部门id&部门名称 获取为 部门名称
                        if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getDepartment())){
                            String[] deptIds = testEntrustedTaskRelEntity.getDepartment().split("&");
                            testEntrustedTaskRelEntity.setDeptName(deptIds[1]);
                        }
                    }
                }
                taskProgressVo.setTaskOrderFlowList(taskOrderFlowList);
            }
        }
        return taskProgressList;
    }

    /**
     * 处理报告进度展示信息
     * @param entrustmentId
     * @return
     */
    private ReportProgressVo dealReportState(Long entrustmentId){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ReportProgressVo result;
        ReportNodeVo reportRecordEntity = recordEntityMapper.getReportNodeByEntrustId(entrustmentId);
        if(reportRecordEntity == null){
            result = null;
        }else{
            Integer state = Integer.parseInt(reportRecordEntity.getState());
            if(state == 0 || state == 2){
                reportRecordEntity.setState("0");
            }else if(state == 3){//报告合成
                reportRecordEntity.setState("1");
            }else if(state == 4){
                reportRecordEntity.setState("2");
            }else if(state == 6){
                reportRecordEntity.setState("3");
            }else if(state == 7){
                reportRecordEntity.setState("4");
            }else if(state == 8){
                reportRecordEntity.setState("5");
            }
            result = new ReportProgressVo(reportRecordEntity.getReportCode(),Integer.parseInt(reportRecordEntity.getState()));
            List<ReportProgressStateVo> reportProgressStateList = Lists.newArrayList();
            for (int i = 0; i <=5 ; i++) {
                if(i == 0){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("报告制作中");
                    vo.setTime(reportRecordEntity.getReportCompleteTime());
                    reportProgressStateList.add(vo);
                }else if(i == 1){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("报告合成");
                    vo.setTime(reportRecordEntity.getCombineTime());
                    reportProgressStateList.add(vo);
                }else if(i == 2){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("审核完成");
                    vo.setTime(reportRecordEntity.getVerifyerTime());
                    reportProgressStateList.add(vo);
                }else if(i == 3){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("签发完成");
                    vo.setTime(reportRecordEntity.getIssuerTime());
                    reportProgressStateList.add(vo);
                }else if(i == 4){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("盖章完成");
                    vo.setTime(reportRecordEntity.getSealTime());
                    reportProgressStateList.add(vo);
                }else if(i == 5){
                    ReportProgressStateVo vo = new ReportProgressStateVo();
                    vo.setTitle("报告发出");
                    vo.setTime(reportRecordEntity.getOperateTime());
                    reportProgressStateList.add(vo);
                }
            }
            result.setReportProgressStateList(reportProgressStateList);
        }
        return result;
    }



    /**
     * 再来一单（复制委托单详情）
     * 样品信息：以样品签收中委托单位id相同的信息为准。否则为空。
     *
     * @param entrustmentId
     * @return
     */
    @Override
    public EntrustAddVo getAnotherList(Long entrustmentId) {
        // 通过委托单id 获取copy 数据。
        EntrustAddVo entrustAddVo = getEntrustHistoryDetailTest(entrustmentId);
        // 处理印章数组。
        if (entrustAddVo.getSealTypes() != null && entrustAddVo.getSealTypes().length > 0) {
            entrustAddVo.setSealTypes(entrustAddVo.getSealType().split(","));
        } else {
            String[] sealTypes = new String[0];
            entrustAddVo.setSealTypes(sealTypes);
        }
        // 通过委托单位id 获取样品已签收的 产品id集合。有数据则赋值，否则返回null。
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListObtain(entrustAddVo.getEntrustCompanyId());
        Iterator<SampleEntity> it = entrustAddVo.getSamples().iterator();
        while (it.hasNext()) {
            SampleEntity sampleEntity = it.next();
            // 标志符。
            Boolean flag = false;
            if (!CollectionUtils.isEmpty(sampleCollection)) {
                for (SampleEntity sampleEntity1 : sampleCollection) {
                    if (sampleEntity.getProductId().equals(sampleEntity1.getProductId())) {
                        // 并对 样品下 检测项ID所属样品ID 重新赋值。
                        if (!CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVoStr())) {
                            for (JudgmentBasisVo judgmentBasisVo : sampleEntity.getJudgmentBasisVoStr()) {
                                judgmentBasisVo.setSampleId(sampleEntity1.getId());
                            }
                        }
                        // 产品id相同，但是独有的属性不同，因此赋值。
                        sampleEntity.setId(sampleEntity1.getId());
                        // 样品名称
                        sampleEntity.setSampleName(sampleEntity1.getSampleName());
                        // 样品编号
                        sampleEntity.setSampleCode(sampleEntity1.getSampleCode());
                        // 规格、型号
                        sampleEntity.setSpecs(sampleEntity1.getSpecs());
                        // 批号
                        sampleEntity.setBatchNumber(sampleEntity1.getBatchNumber());
                        // 厂家
                        sampleEntity.setManufacturer(sampleEntity1.getManufacturer());
                        // 产地
                        sampleEntity.setSampleOrigin(sampleEntity1.getSampleOrigin());
                        // 代表批量
                        sampleEntity.setGeneration(sampleEntity1.getGeneration());
                        // 别名
                        sampleEntity.setAliasName(sampleEntity1.getAliasName());
                        flag = true;
                        break;
                    }
                }
            }
            // 样品签收的productId 与copy委托单位id下产品Id不一致 则清除。
            if (!flag) {
                it.remove();
            }
        }
        return entrustAddVo;
    }

    /**
     * 通过委托单id 获取样品集合 并遍历样品 分别处理：1、样品原材 2、配合比。
     *
     * @param entrustmentId
     * @param state
     * @return 与委托单关联的样品集合。
     */
    public List<SampleEntity> methodReturnSampleCollection(Long entrustmentId, Integer state) {
        // 通过委托单id 获取样品集合 并遍历样品 分别处理：1、样品原材 2、配合比。
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 返回样品集合信息。
        List<SampleEntity> ReturnsampleCollection = new ArrayList<>();
        //暂存配合比下的的样品信息
        // 样品信息 遍历样品 分别处理：1、样品原材 2、配合比。
        for (SampleEntity sampleEntity : sampleCollection) {
            // 根据 委托单状态 进行选择项查询 0&&144 查询默认部门信息 state =1 查询所属指定部门信息
            if (state == 0 || state == 144) {
                List<JudgmentBasisVo> list = sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId);
                if (list != null && !list.isEmpty()) {
                    // 根据检测项id 查询 默认匹配部门信息
                    for (JudgmentBasisVo data : list) {
                        List<String> strings = sampleEntityMapper.getTeamNameStrings(data.getCheckItemId());
                        data.setTestingRoom(strings.toString());
                    }
                    sampleEntity.setJudgmentBasisVos(list);
                }
                // 补充样品下 依据集合
                sampleEntity.setStandardFileIds(sampleEntityMapper.getSampleBasisSet(sampleEntity.getId(), entrustmentId));
                // 存储样品。
                ReturnsampleCollection.add(sampleEntity);
                // 判断样品 是 原材 还是 配合比。
                if (sampleEntity.getSampleType().contains("配合比")) {
                    // 存储 获取配合比下样品集合
                    List<SampleEntity> SampleEntityNextLevel = sampleEntityMapper.selectByPid(sampleEntity.getId());
                    if (!CollectionUtils.isEmpty(SampleEntityNextLevel)) {
                        ReturnsampleCollection.addAll(SampleEntityNextLevel);
                    }
                }
            } else {
                // 判断样品 是 原材 还是 配合比。
                if (sampleEntity.getSampleType().contains("配合比")) {
                    // 存储 获取配合比下样品集合
                    List<SampleEntity> SampleEntityNextLevel = sampleEntityMapper.selectByPid(sampleEntity.getId());
                    if (!CollectionUtils.isEmpty(SampleEntityNextLevel)) {
                        ReturnsampleCollection.addAll(SampleEntityNextLevel);
                    }
                }
                sampleEntity.setJudgmentBasisVos(sampleEntityMapper.selectTestStandardList(sampleEntity.getId(), entrustmentId));
                // 补充样品下 依据集合
                sampleEntity.setStandardFileIds(sampleEntityMapper.getSampleBasisSet(sampleEntity.getId(), entrustmentId));
                // 存储样品。
                ReturnsampleCollection.add(sampleEntity);
            }
        }
        return ReturnsampleCollection;
    }


    /**
     * 分布详情——检测项无价格不展示。
     *
     * @param entrustmentId
     * @return
     */
    @Override
    public EntrustAddVo getEntrustDistributionDetail(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        PageHelper.clearPage();
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        List<LabelValueVo> allTestRoom = Lists.newArrayList();

        if (entrustAddVo.getOperateUser() != null) {
            // 获取做废人id 查询账号姓名
            entrustAddVo.setOperateUserStr(sysUserDao.getSysUserName(entrustAddVo.getOperateUser()));
        }
        // 通过委托单id 获取缴费记录 依据id 同价价格
        entrustAddVo.setPaymentRecord(entityMapper.getTestEntrustedPaymentRecordInfoPrice(entrustmentId));
        // —— 支付方式。
//        entrustAddVo.setPaymentMethod(entityMapper.getTestEntrustedInfoMethodName(entrustmentId));
        // 联系地址
//        entrustAddVo.setAdress(entityMapper.getEntrustingParty(entrustmentId));
        // 通过委托ID 样品集合 → test_sample
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        //暂存配合比下的的样品信息
        List<TestSampleEntity> nodeSample = Lists.newArrayList();
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            // 根据 委托单状态 进行选择项查询 0&&144 查询默认部门信息 state =1 查询所属指定部门信息
            if (entrustAddVo.getState() == 0 || entrustAddVo.getState() == 144) {
                List<JudgmentBasisVo> list = sampleEntityMapper.getCheckItemNoDistribution(sampleEntity.getId(), entrustmentId);
                // 遍历检测项数据处理 价格为空的不展示（删除） 暂时废弃
//                if (list != null && !list.isEmpty()) {
//                    Iterator<JudgmentBasisVo> it = list.iterator();
//                    while (it.hasNext()) {
//                        JudgmentBasisVo judgmentBasisVo = it.next();
//                        if (judgmentBasisVo.getCheckPrice() == null) {
//                            it.remove();
//                        }
//                    }
//                }
                if (list != null && !list.isEmpty()) {
                    // 根据检测项id 查询 默认匹配部门信息
                    for (JudgmentBasisVo data : list) {
                        List<String> strings = new ArrayList<>();
                        List<TestTeam> testingRoomInfoList = sampleEntityMapper.getTestingRoomInfoList(data.getCheckItemId());
//                        ALL团队信息
                        List<TestTeam> Alllist = sampleEntityMapper.getAllRoomInfoList();
                        //如果团队非顶级团队，则团队名称展示为父级名称-本团队名称team1需要处理的数据，team所有团队数据
                        for (TestTeam team1:testingRoomInfoList) {
                            for (TestTeam team:Alllist) {
                                if (team1.getPid() !=0){
                                    if (team1.getPid().equals(team.getId())){
                                        String name = team.getName()+"—"+team1.getName();
                                        team1.setName(name);
                                        strings.add(name);
                                    }
                                }
                            }
                        }
                        data.setTestingRoom(strings.toString());
//                        allTestRoom.addAll(testingRoomList);
                        List<Integer> pids = Lists.newArrayList();
                        List<LabelValueVo> testingRoomList = Lists.newArrayList();
                        for (TestTeam team:testingRoomInfoList) {
                            pids.add(team.getPid());
                        }
                        //过滤id没被作为pid的数据
                        for (TestTeam team:testingRoomInfoList) {
                            if (!pids.contains(team.getId())){
                                LabelValueVo vo= new LabelValueVo();
                                vo.setValue(Long.parseLong(team.getId().toString()));
                                vo.setLabel(team.getName());
                                testingRoomList.add(vo);
                            }
                        }
                        data.setTestingRoomList(testingRoomList);
                        allTestRoom.addAll(testingRoomList);
                    }
                    sampleEntity.setJudgmentBasisVos(list);
                }
            }
            //补充配合比样品的原材样品信息
            if (sampleEntity.getSampleType().contains("配合比")) {
                nodeSample.addAll(testSampleEntityMapper.selectByPid(sampleEntity.getId()));
            }
            // 补充样品下 依据集合
            sampleEntity.setStandardFileIds(sampleEntityMapper.getSampleBasisSet(sampleEntity.getId(), entrustAddVo.getId()));
        }
        entrustAddVo.setNodeSample(nodeSample);
        entrustAddVo.setSamples(sampleCollection);
        LinkedHashSet<LabelValueVo> hashSet = new LinkedHashSet<>(allTestRoom);
        ArrayList<LabelValueVo> allTestRooms = new ArrayList<>(hashSet);
        entrustAddVo.setAllTestRoom(allTestRooms);
        return entrustAddVo;
    }

    @Override
    public List<LabelValueVo> getDept(Integer checkItemId) {
        return entityMapper.getDept(checkItemId);
    }

    @Override
    public EntrustAddVo getEntrustHistoryDetailTest(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        if (entrustAddVo.getSealType() != null) {
            entrustAddVo.setSealTypes(entrustAddVo.getSealType().split(","));
        }
        else {
            entrustAddVo.setSealTypes(new String[0]);
        }
        // 通过委托单id 获取缴费记录 依据id 同价价格
        String total = entityMapper.getTestEntrustedPaymentRecordInfoPrice(entrustmentId);
        if(total!=null&&total.length()>0){
            entrustAddVo.setPaymentRecordShow(total);
            entrustAddVo.setPaymentRecord(total);
        }
        else {
            entrustAddVo.setPaymentRecordShow("0");
            entrustAddVo.setPaymentRecord("0");
        }
        /**
         * 委托单文件file 处理
         * 通过委托单id 查询相应附件集合
         */
        List<EntrustFileTableEntity> fileList = entrustFileTableDao.getEntrustFileTableEntityList(entrustAddVo.getId());
        if(CollectionUtils.isEmpty(fileList)){
            // 返回空集合
            List<EntrustFileTableEntity> fileListNull = new ArrayList<>();
            entrustAddVo.setFileArrays(fileListNull);
        }
        else {
            entrustAddVo.setFileArrays(fileList);
        }
        // —— 支付方式。
//        entrustAddVo.setPaymentMethod(entityMapper.getTestEntrustedInfoMethodName(entrustmentId));
        // 联系地址
//        entrustAddVo.setAdress(entityMapper.getEntrustingParty(entrustmentId));
        // 通过委托ID 样品集合 → test_sample
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 处理信息 样品下检测项信息无价格不展示。
        for (SampleEntity sampleEntity0 : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            List<JudgmentBasisVo> listJson = Lists.newArrayList();
            List<JudgmentBasisVo> itemList = sampleEntityMapper.selectTestStandardList(sampleEntity0.getId(), entrustmentId);
            Iterator<JudgmentBasisVo> it = itemList.iterator();
            while (it.hasNext()) {
                JudgmentBasisVo judgmentBasisVo = it.next();
                if (judgmentBasisVo.getCheckPrice() == null) {
                    it.remove();
                }
            }
            listJson.addAll(itemList);
            sampleEntity0.setJudgmentBasisVoStr(listJson);
        }
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 补充样品下 依据集合
            List<JudgmentBasisVo> standardList = Lists.newArrayList();
            standardList.addAll(sampleEntityMapper.getSampleBasisList(sampleEntity.getId(), entrustAddVo.getId()));
            sampleEntity.setStandardFileIdStr(standardList);
            //补充检测项可选的全部检测依据
            if (!CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVoStr())) {
                for (JudgmentBasisVo judgmentBasisVo : sampleEntity.getJudgmentBasisVoStr()) {
                    List<LabelValueVo> allCheckBasis = Lists.newArrayList();
                    allCheckBasis.addAll(testProductDao.getAllCheckBasis(judgmentBasisVo.getCheckItemId()));
                    judgmentBasisVo.setCheckBasisList(allCheckBasis);
                }
            }
            //补充产品可选的全部判定依据
            List<LabelValueVo> judges = Lists.newArrayList();
            judges.addAll(testProductDao.getJudges(sampleEntity.getProductId()));
            sampleEntity.setAllStandardFileList(judges);
        }
        entrustAddVo.setSamples(sampleCollection);
        return entrustAddVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishTask(TaskEntity entity) {
        //根据委托单生成任务单
        entity.setId(GenID.getID());
        //设置任务编号由团队代码（单字符英文字母）+年月(4字符)+“-”+三位流水号（3字符），如A2108-015。
        //团队编号这部分在团队表中，任务编号生成时不考虑，展示时拼接上即可
        //获取当前最大样品编号
        Integer code = null;
        Integer entrustNum = taskMapper.selectMaxNo();
        String currentTime = DateUtil.getTodayString().substring(2, 6);
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 4);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "001");
            }
        } else {
            code = Integer.parseInt(currentTime + "001");
        }
        entity.setCode(code.toString());
        if (!StringUtils.isEmpty(entity.getTeamId())) {
            //设置接收人为团队副团长
            List<SysUserEntity> userEntity = teamMapper.getUsersByTid(entity.getTeamId());
            if (!CollectionUtils.isEmpty(userEntity)) {
                for (SysUserEntity sysUserEntity : userEntity) {
                    if (sysUserEntity.getPosition().equals(Const.SYS_MANAGER_LOG)) {
                        entity.setReceiver(sysUserEntity.getUsername());
                    }
                }
                entity.setReceiveTime(new java.sql.Date(System.currentTimeMillis()));
            }
        }
        //任务单保存
        taskMapper.save(entity);
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionTask(TaskVo entity) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
//        Long dept = null;
        List<Long> dept = Lists.newArrayList();
        for (CheckItemDeptVo vo : checkItemDeptVoList) {
            if (!deptIds.contains(vo.getDeptId())) {
                deptIds.add(vo.getDeptId());
            }
            String issueReport = vo.getIssueReport();
            if ("是".equals(issueReport)) {
                dept.add(vo.getDeptId());
            }
        }
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        for (Long deptId : deptIds) {
            TaskVo vo = new TaskVo();
            vo.setId(GenID.getID());
            String teamCode = taskMapper.getTeamCode(deptId);
            Integer integer = taskMapper.selectMaxNoByCode(teamCode);
            Integer code = null;
            if (integer == null) {
                String currentTime = DateUtil.getTodayString().substring(2, 6);
                code = Integer.parseInt(currentTime + "001");
            } else {
                code = integer + 1;
            }
            String codeStr = code + "";
            vo.setDeptId(deptId);
            vo.setCode(codeStr);
            vo.setTaskCode(teamCode + codeStr.substring(0, 4) + "-" + codeStr.substring(4, 7));
            vo.setEntrustmentId(entity.getEntrustmentId());
            vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
            vo.setOrderTime(entity.getOrderTime());
            vo.setState(0);
            vo.setReportComplete(2);
            vo.setOrderer(ShiroUtils.getUserInfo().getName());
//            if(deptId.equals(dept)){
            if (dept.contains(deptId)) {
                vo.setIssueReport("是");
            } else {
                vo.setIssueReport("否");
            }
            vos.add(vo);
        }
        //任务单保存
        taskMapper.batchSave(vos);
        //更新检测项信息
        taskMapper.batchUpdateCheckItem(entity.getCheckItemDeptVoList());
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionTask412(TaskVo entity) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
        for (CheckItemDeptVo vo : checkItemDeptVoList) {
            if (!deptIds.contains(vo.getDeptId())) {
                deptIds.add(vo.getDeptId());
            }
        }
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        for (Long deptId : deptIds) {
            //计算本单价格
            double taskPrice = 0L;
            for (CheckItemDeptVo vo : checkItemDeptVoList) {
                if (deptId.equals(vo.getDeptId())) {
                    taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                            (vo.getCheckPrice() == null ? 0 : vo.getCheckPrice()) * vo.getTimes());
                }
            }
            TaskVo vo = new TaskVo();
            long id = GenID.getID();
            vo.setId(id);
            vo.setTaskPrice(taskPrice);
            String teamCode = taskMapper.getTeamCode(deptId);
            Integer integer = taskMapper.selectMaxNoByCode(teamCode);
            Integer code = null;
            if (integer == null) {
                String currentTime = DateUtil.getTodayString().substring(2, 6);
                code = Integer.parseInt(currentTime + "001");
            } else {
                code = integer + 1;
            }
            String codeStr = code + "";
            vo.setDeptId(deptId);
            vo.setCode(codeStr);
            vo.setTaskCode(teamCode + codeStr.substring(0, 4) + "-" + codeStr.substring(4, 7));
            vo.setEntrustmentId(entity.getEntrustmentId());
            vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
            vo.setOrderTime(entity.getOrderTime());
            vo.setState(0);
            vo.setReportComplete(2);
            vo.setOrderer(ShiroUtils.getUserInfo().getName());
            vo.setPresentInformation(entity.getPresentInformation());
//            if(deptId.equals(dept)){
            if (entity.getDeptIds().contains(deptId)) {
                vo.setIssueReport("是");
            } else {
                vo.setIssueReport("否");
            }
            vos.add(vo);
            //更新检测项分配的部门和任务单号
            List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
            for (CheckItemDeptVo checkItemDeptVo : checkItemDeptVoList) {
                if (deptId.equals(checkItemDeptVo.getDeptId())) {
                    checkItemDeptVo.setTaskId(id);
                    checkItemDeptVoList1.add(checkItemDeptVo);
                }
            }
            //更新检测项信息
            taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
        }
        //任务单保存
        taskMapper.batchSave(vos);
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        // 处理任务流转信息 通过委托单id 和 传入信息 !=taskRelEntities.isEmpty()
        if(!CollectionUtils.isEmpty(entity.getTaskRelEntities())){
            // 补充发布人ID和姓名
            SysUserEntity userEntity = ShiroUtils.getUserInfo();
            List<TestEntrustedTaskRelEntity> TaskRelEntities = entity.getTaskRelEntities();
            for(TestEntrustedTaskRelEntity taskdata:TaskRelEntities){
                taskdata.setUserId(userEntity.getUserId());
                taskdata.setAddressName(userEntity.getName());
                taskdata.setCreateDate(new Date());
            }
            methodDistributionOfFlow(entity.getEntrustmentId(),TaskRelEntities);
        }
        return true;
    }

    @Override
    public XWPFDocument downloadEntrust(EntrustAddVo detail, InputStream object) {
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(object);
            List<XWPFTable> tables = doc.getTables();
            //设置样品信息
            List<SampleEntity> sampleEntityList = Lists.newArrayList();
            List<SampleEntity> samples = detail.getSamples();
            List<TestSampleEntity> nodeSample = detail.getNodeSample();
            if (nodeSample != null && nodeSample.size() > 0) {
                for (TestSampleEntity node : nodeSample) {
                    SampleEntity entity = new SampleEntity(node);
                    sampleEntityList.add(entity);
                }
                samples.addAll(sampleEntityList);
            }
            for (int j = 0; j < tables.size(); j++) {
                List<XWPFTableRow> rows;
                //获取表格对应的行
                rows = tables.get(j).getRows();
                if (j == 0) {
                    //设置模板数据
                    rows.get(2).getTableCells().get(8).setText("№." + detail.getEntrustmentNo());//委托单位
                    rows.get(3).getTableCells().get(2).setText(detail.getEntrustCompany());//委托单位
                    rows.get(4).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getWitnessUint())?"——":detail.getWitnessUint());//见证单位
                    rows.get(5).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getProjectName())?"——":detail.getProjectName());//工程名称
                    rows.get(6).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getProjectPart())?"——":detail.getProjectPart());//工程部位
                    //新增的行数
                    int sampleIndex = 8;
                    int index = 1;
                    if (samples.size() > 6) {
                        AsposeUtil.addRows(tables.get(0), sampleIndex, samples.size() - 6);
                    }
                    for (int i = 0; i < samples.size(); i++) {
                        rows.get(sampleIndex).getTableCells().get(index).setText(samples.get(i).getAliasName());//样品名称
                        rows.get(sampleIndex).getTableCells().get(index + 1).setText(StringUtils.isEmpty(StringUtils.isEmpty(detail.getProjectPart())?"——":detail.getProjectPart())?"——":samples.get(i).getSpecs());//规格等级
                        rows.get(sampleIndex).getTableCells().get(index + 2).setText(StringUtils.isEmpty(samples.get(i).getBatchNumber())?"——":samples.get(i).getBatchNumber());//批号/编号
                        rows.get(sampleIndex).getTableCells().get(index + 3).setText(StringUtils.isEmpty(samples.get(i).getSampleQuantity())?"——":samples.get(i).getSampleQuantity());//样品数量
                        rows.get(sampleIndex).getTableCells().get(index + 4).setText(StringUtils.isEmpty(samples.get(i).getGeneration())?"——":samples.get(i).getGeneration());//代表批量
                        rows.get(sampleIndex).getTableCells().get(index + 5).setText(StringUtils.isEmpty(samples.get(i).getManufacturer())?"——":samples.get(i).getManufacturer());//样品产地/生产厂家
                        rows.get(sampleIndex).getTableCells().get(index + 6).setText(StringUtils.isEmpty(samples.get(i).getSampleRemark())?"——":samples.get(i).getSampleRemark());//样品备注
                        sampleIndex = sampleIndex + 1;
                    }
                }
                if (j == tables.size() - 1) {
                    //设置其它信息(第二个table)
                    String ss = "";
                    rows.get(0).getTableCells().get(2).setText(detail.getPresentInformation() == null ? "——" : detail.getPresentInformation());//提供资料
                    rows.get(1).getTableCells().get(2).setText(detail.getSamplingMethod() == null ? "——" : detail.getSamplingMethod());//取样方式
                    rows.get(1).getTableCells().get(4).setText(detail.getCheckPurpose() == null ? "——" : detail.getCheckPurpose());//检验目的
                    List<String> list = entityMapper.getSampleStandard(detail.getId());
                    StringBuilder stringBuilder = new StringBuilder();
                    if (!CollectionUtils.isEmpty(list)) {
                        for (String s : list) {
                            stringBuilder.append(s);
                            stringBuilder.append("，");
                        }
                        String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                        rows.get(1).getTableCells().get(6).setText(substring == null ? "——" : substring);//产品标准 TODO 去重
                    }
                    StringBuilder stringBuilder1 = new StringBuilder();
                    if (!CollectionUtils.isEmpty(samples)) {
                        for (SampleEntity entity : samples) {
                            List<JudgmentBasisVo> sampleCheckItem = entity.getJudgmentBasisVos();
                            if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                                for (JudgmentBasisVo itemEntity : sampleCheckItem) {
                                    //价钱为null的不展示
                                    if (itemEntity.getCheckPrice() != null) {
                                        String name = itemEntity.getCheckItemName();
                                        stringBuilder1.append(name);
                                        if (!StringUtils.isEmpty(itemEntity.getStandardName())) {
                                            stringBuilder1.append("（");
                                            String s = itemEntity.getStandardName();
                                            String aa = s.split("《")[0];
                                            stringBuilder1.append(aa);
                                            stringBuilder1.append("）");
                                        }
                                        stringBuilder1.append("，");
                                    }
                                }
                            }
                        }
                        String substring = stringBuilder1.toString().substring(0, stringBuilder1.length() - 1);
                        String[] split = substring.split("，");
                        Set<String> set = new HashSet<>();
                        for (String s:split) {
                            set.add(s);
                        }
                        String substring1 = set.toString().substring(1, set.toString().length() - 1);
                        rows.get(2).getTableCells().get(2).setText(substring1 == null ? "——" : substring1);//检验项目及检测依据 TODO 去重
                    }
                    //TODO +1
                    rows.get(3).getTableCells().get(2).setText(detail.getReportCount().toString());//报告分数
                    rows.get(3).getTableCells().get(4).setText(detail.getReportType() == null ? "——" : detail.getReportType());//取报告方式
                    rows.get(3).getTableCells().get(6).setText(detail.getAddress() == null ? "——" : detail.getEntrustCompany());//收报告单位
                    rows.get(4).getTableCells().get(2).setText(detail.getAddress() == null ? "——" : detail.getAddress());//联系地址
                    rows.get(4).getTableCells().get(4).setText(detail.getAddressee() == null ? "——" : detail.getAddressee());//联系人
                    rows.get(4).getTableCells().get(6).setText(detail.getMobile() == null ? "——" : detail.getMobile());//联系方式
                    rows.get(5).getTableCells().get(2).setText(detail.getEntrustPeople() == null ? "——" : detail.getEntrustPeople());//委托人
                    rows.get(5).getTableCells().get(4).setText(detail.getEntrustPhone() == null ? "——" : detail.getEntrustPhone());//委托人电话
                    rows.get(5).getTableCells().get(6).setText(detail.getWitnessPerson() == null ? "——" : detail.getWitnessPerson());//见证人
                    StringBuilder stringBuilder2 = new StringBuilder();
                    for (SampleEntity sampleEntity : samples) {
                        stringBuilder2.append(sampleEntity.getAliasName());
                        stringBuilder2.append("（");
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(sampleEntity.getSpecs())){
                            stringBuilder2.append(sampleEntity.getSpecs());
                        }else {
                            stringBuilder2.append("——");
                        }
                        stringBuilder2.append("、");
                        String s = sampleEntity.getOutwardDescribe();
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(s)){
                            stringBuilder2.append(s);
                        }else {
                            stringBuilder2.append("——");
                        }
                        stringBuilder2.append("）；");
                    }
                    rows.get(6).getTableCells().get(2).setText(stringBuilder2.toString().substring(0, stringBuilder2.length() - 1));//样品状态
                    rows.get(6).getTableCells().get(4).setText(detail.getIsSave());//样品保留
                    rows.get(7).getTableCells().get(2).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getActualPrice()) ? "——" : detail.getActualPrice());//检验收费
                    rows.get(7).getTableCells().get(4).setText(detail.getPaymentMethod() == null ? "——" : detail.getPaymentMethod());//支付方式
                    //TODO 本次缴费统计缴费记录表
                    rows.get(7).getTableCells().get(6).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getPaymentRecordShow()) ? "——" : detail.getPaymentRecordShow());//本次交费
                    rows.get(8).getTableCells().get(2).setText(DateUtil.formatDate(detail.getRequestDate()));//完成期限
                    rows.get(8).getTableCells().get(4).setText(detail.getBusinessAcceptor() == null ? "——" : detail.getBusinessAcceptor());//业务受理人
                    rows.get(8).getTableCells().get(6).setText(DateUtil.formatDate(detail.getAcceptanceDate()));//受理日期
                    rows.get(10).getTableCells().get(1).removeParagraph(0);
                    rows.get(10).getTableCells().get(1).setText(StringUtils.isEmpty(detail.getRemark())?"——":detail.getRemark());//备注
                }
            }
        } catch (Exception e) {
            logger.error("设置委托单信息到模板异常:{}", e);
        }
        return doc;
    }

    @Override
    public HistoryEntrustDataVo getHistoryData(String name) {
        return entityMapper.getHistoryData(name);
    }

    @Override
    public HistoryEntrustDataVo getHistoryData(String name, Integer type) {
        HistoryEntrustDataVo data = new HistoryEntrustDataVo();
        PageHelper.clearPage();
        HistoryEntrustDataVo jsonData = entityMapper.getHistoryData(name);
        if (jsonData != null) {
            data.setProjectName(jsonData.getProjectName());
            data.setProjectPart(jsonData.getProjectPart());
        }
        List<TestCompanyJsonEntity> dataList = entityMapper.getCompanyJsonEntityList(name, type);
        if (dataList.size() > 0) {
            data.setUnitData(dataList);
        } else {
            data.setUnitData(new ArrayList<TestCompanyJsonEntity>());
        }
        return data;
    }

    @Override
    public String findStateBySampleId(int sampleId, EntrustEntityMapper entityMapper, TaskMapper taskMapper) {

        String state = "";
        //五种样品状态1待检，2在检，3已检，||TODO 4留样，5处置
        if (sampleId > 0) {
            Long id = entityMapper.getMesBySampleId(sampleId);
            Long entrustId = entityMapper.getEntrustIdBySampleId(sampleId);
            if (id != null) {
                if (entrustId == null) {
                    state = "待检";
                } else {
                    List<String> status = taskMapper.getStateByEntrustId(entrustId);
                    if (!CollectionUtils.isEmpty(status)) {
                        List<Integer> longs = Lists.newArrayList();
                        for (String s : status) {
                            longs.add(Integer.parseInt(s));
                        }
                        Integer max = Collections.max(longs);
                        if (max <= 2) {
                            state = "待检";
                        }
                        if (3 == max) {
                            state = "在检";
                        }
                        Boolean flag = false;
                        for (Integer num : longs) {
                            if (num >= 4) {
                                flag = true;
                            } else {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            state = "已检";
                        }
                    }
                }

            }
        }
        return state;
    }

    @Override
    public String getMessage() {
        return entityMapper.getMessage();
    }

    @Override
    public List<CheckItemDetailVo> getCheckItemBasis(Integer productId) {
        return itemEntityMapper.getCheckItemBasis(productId);
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfo(List<Integer> ids) {
        List<CheckItemInfoVo> result = Lists.newArrayList();
        result.addAll(itemEntityMapper.getItemInfo3(ids));
        if (!CollectionUtils.isEmpty(result)) {
            for (CheckItemInfoVo checkItemInfoVo : result) {
                if (checkItemInfoVo.getCheckItemPid() != 0) {
                    checkItemInfoVo.setCheckItemName(getAllLevelName(checkItemInfoVo.getCheckItemPid())
                            + checkItemInfoVo.getCheckItemName());
                }
            }
        }
        return result;
    }

    @Override
    public List<LabelValueVo> getReportTeams(Long entrustmentId) {
        return entityMapper.getReportTeams(entrustmentId);
    }

    @Override
    public int updateReportTeam(Long entrustmentId, List<Integer> deptIds) {
        List<LabelValueVo> reportTeams = entityMapper.getReportTeams(entrustmentId);
        List<UpdateReportTeamVo> result = Lists.newArrayList();
        for (LabelValueVo vo : reportTeams) {
            UpdateReportTeamVo entity = new UpdateReportTeamVo();
            entity.setEntrustmentId(entrustmentId);
            entity.setDeptId(vo.getValue());
            if (deptIds.contains(Integer.parseInt(vo.getValue().toString()))) {
                entity.setIssueReport("是");
            } else {
                entity.setIssueReport("否");
            }
            result.add(entity);
        }
        return taskMapper.batchUpdateReportTeam(result);
    }

    private String getAllLevelName(Integer checkItemPid) {
        StringBuilder prefix = new StringBuilder();
        List<String> temp = Lists.newArrayList();
        Integer pid = checkItemPid;
        while (pid != 0) {
            CheckItemDetailVo parentInfo = itemEntityMapper.getParentInfo(checkItemPid);
            temp.add(parentInfo.getCheckItemName());
            Integer itemPid = parentInfo.getItemPid();
            pid = parentInfo.getItemPid();
        }
        for (int i = temp.size() - 1; i >= 0; i--) {
            prefix.append(temp.get(i)).append("-");
        }
        return prefix.toString();
    }

    /**
     * 扩展模板样品行列
     *
     * @param table            原始表格
     * @param sampleDetailList 待处理数据
     * @param modelSampleRows  需要新增行
     * @param columns          列数
     */
    public List<XWPFTableRow> extendTable(XWPFTable table, List<XWPFTableRow> rows, List<SampleEntity> sampleDetailList,
                                          int modelSampleRows, int columns) {
        if (sampleDetailList.size() > modelSampleRows) {
            int addRows = sampleDetailList.size() - modelSampleRows;
            // 表格插入
            XWPFDocument doc1 = new XWPFDocument();
            XWPFTable newTable = doc1.createTable(addRows, columns);
            // 创建表格后直接进行存放 后续多余数据
            List<XWPFTableRow> dataTable = newTable.getRows();
            int j = 0;
            for (int i = modelSampleRows; i < sampleDetailList.size(); i++) {
                dataTable.get(j).getTableCells().get(1).setText(sampleDetailList.get(i).getSampleName());//样品名称
                dataTable.get(j).getTableCells().get(2).setText(sampleDetailList.get(i).getSpecs());//规格等级
                dataTable.get(j).getTableCells().get(3).setText(sampleDetailList.get(i).getBatchNumber());//批号/编号
                dataTable.get(j).getTableCells().get(4).setText(sampleDetailList.get(i).getSampleQuantity());//样品数量
                dataTable.get(j).getTableCells().get(5).setText(sampleDetailList.get(i).getGeneration());//代表批量
                dataTable.get(j).getTableCells().get(6).setText(sampleDetailList.get(i).getManufacturer());//样品产地/生产厂家
                dataTable.get(j).getTableCells().get(7).setText(sampleDetailList.get(i).getSampleRemark());//样品备注
                table.addRow(dataTable.get(j));
                j++;
            }
            rows = table.getRows();
        }
        return rows;
    }

    /**
     * 再来一单（复制委托单详情）
     * 样品信息来源： 以旧委托单下 样品信息详情关联 返回前端时 样品id 伪造
     * @param entrustmentId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntrustAddVo getAnotherListCopy(Long entrustmentId) {
        // 通过委托单id 获取copy 数据。
        EntrustAddVo entrustAddVo = getEntrustHistoryDetailTest(entrustmentId);
        // 清除上传的 附件
        entrustAddVo.setFileArrays(new ArrayList<>());
        // 处理印章数组。
        if (entrustAddVo.getSealTypes() != null && entrustAddVo.getSealTypes().length > 0) {
            entrustAddVo.setSealTypes(entrustAddVo.getSealType().split(","));
        } else {
            String[] sealTypes = new String[0];
            entrustAddVo.setSealTypes(sealTypes);
        }
        List<SampleEntity> sampleCollection = entrustAddVo.getSamples();
        Integer sampleId = 0;
            if (!CollectionUtils.isEmpty(sampleCollection))
            {
                for (SampleEntity sampleEntity : sampleCollection)
                {
                    if(sampleEntity.getSampleType().contains("配合比"))
                    {
                        sampleEntity.setPid(sampleEntity.getId());
                    }
                    // 并对 样品下 检测项ID所属样品ID 重新赋值。
                    sampleId+=1;
                        if (!CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVoStr()))
                        {
                            for (JudgmentBasisVo judgmentBasisVo : sampleEntity.getJudgmentBasisVoStr()) {
                                judgmentBasisVo.setSampleId(sampleId);
                            }
                        }
                    sampleEntity.setOldSampleid(sampleEntity.getId());
                    // 产品id相同，伪造样品编号。
                    sampleEntity.setId(sampleId);
                }
            }
            return entrustAddVo;
    }

    /**
     * 新增委托_（针对 再来一单的数据保存）
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addEntrustCopy(EntrustAddVo vo, MultipartFile[] file) {
        // 获取前台得到的 vo.getId()
        long old = vo.getId();
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        long id = GenID.getID();
        basisInfo.setId(id);
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0, 6);
        //获取当前最大委托编号
        PageHelper.clearPage();
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum != null && entrustNum > 0) {
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)) {
                code = entrustNum + 1;
            } else {
                code = Integer.parseInt(currentTime + "0001");
            }
        } else {
            code = Integer.parseInt(currentTime + "0001");
        }
        basisInfo.setEntrustmentNo(code);
        // 通过委托编号 查询是否存在
        PageHelper.clearPage();
        if (entityMapper.getByData(basisInfo.getEntrustmentNo()) != null) {
            return "再来一单新增委托失败!:\t委托编号已存在\t"+basisInfo.getEntrustmentNo();
        }
        // 处理copy后的样品集合
        if(!vo.getSamples().isEmpty()){
            methodCopySamples(vo.getSamples(),old,id,basisInfo.getEntrustCompanyId());
        }
//         通过样品ID 查询委托单信息和样品Id 绑定关系 （==null 正常，!=null false）
        if (!CollectionUtils.isEmpty(vo.getSamples())) {
            for (SampleEntity sampleEntity : vo.getSamples()) {
                PageHelper.clearPage();
                if (entityMapper.getEntrustIdBySampleId(sampleEntity.getId()) != null) {
                    return "再来一单新增委托失败!:\t样品与委托单与建立关系\t"+sampleEntity.getId();
                }
            }
        }
//        附件存在上传附件到服务器
        if (file.length != 0) {
            for (MultipartFile multipartFile : file) {
                uploading(basisInfo.getId(),multipartFile);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        Double totalMoney = 0D;
        List<SampleEntity> samples = vo.getSamples();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                SampleEntity sampleEntity2 = new SampleEntity();
                sampleEntity2.setId(sampleEntity.getId());
                sampleEntity2.setIsUse(1);
                // 前端 变更的字段
                sampleEntity2.setSpecs(sampleEntity.getSpecs());
                sampleEntity2.setBatchNumber(sampleEntity.getBatchNumber());
                sampleEntity2.setGeneration(sampleEntity.getGeneration());
                sampleEntity2.setCheckDate(sampleEntity.getCheckDate()!=null?sampleEntity.getCheckDate():new Date());
                sampleEntity2.setCompanyId(basisInfo.getEntrustCompanyId());
                sampleEntityMapper.updateByPrimaryKeySelective(sampleEntity2);
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)) {
                    for (Integer integer : standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                    List<SampleItemEntity> ItemList = new ArrayList<>();
                    for (SampleItemEntity entity : sampleCheckItem) {
                        /** 废弃 6月21日需求变更。
                         // 根据检测项id 遍历检测项层级和价格 获取集合
                         List<SampleItemEntity> ItemList = entityMapper.getItemRecursionList(entity.getCheckItemId());
                        //处理检测项 遍历出来的层级数据 拼接层级名。
                        HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity0 : ItemList) {
                                if (entity0.getCheckItemId().equals(entity.getCheckItemId())) {
                                    entity0.setCheckItemName(entity.getCheckItemName());
                                }
                                itemMap.put(entity0.getCheckItemId(), entity0);
                            }
                            for (SampleItemEntity entity2 : ItemList) {
                                SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                                if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                                    // 变更检测项名为： 伪造a-伪造b
                                    entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                                }
                            }
                        }
                        // 根据检测项id 遍历检测项层级和价格 获取集合
                        if (!CollectionUtils.isEmpty(ItemList)) {
                            for (SampleItemEntity entity1 : ItemList) {
                                //计算检测项总价钱
                                if (entity1.getUnitPrice() != null && entity1.getUnitPrice() >= 0) {
                                    int money = entity.getTimes() * entity1.getUnitPrice();
                                    totalMoney = totalMoney + money;
                                }
                                //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                                entity1.setSampleId(sampleEntity.getId());
                                entity1.setEntrustId(basisInfo.getId());
                                entity1.setMethodId(entity.getMethodId());
                                entity1.setStandardId(entity.getStandardId());
                                entity1.setTimes(entity.getTimes());
                            }
                            entityMapper.BatchSaveEntrustSampleItem(ItemList);
                        }
                    }
                 *
                 */
                        // 正常存储检测项即可。
                        //计算检测项总价钱
                        if (entity.getUnitPrice() != null && entity.getUnitPrice() >= 0) {
                            double money = entity.getTimes() * entity.getUnitPrice();
                            totalMoney = totalMoney + money;
                        }
                        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
                        entity.setSampleId(sampleEntity.getId());
                        entity.setEntrustId(basisInfo.getId());
                        entity.setStandardId(entity.getStandardId());
                        entity.setMethodId(entity.getMethodId());
                        entity.setTimes(entity.getTimes());
                        ItemList.add(entity);
                    }
                    entityMapper.BatchSaveEntrustSampleItem(ItemList);
                    }
                //根据委托检测类别关联 配合比检测信息和委托单ID
                // 再来一单 add 不需要关联。
//                if (vo.getEntrustTestType().contains("配合比")) {
//                    TestSampleMixInfoEntity record = new TestSampleMixInfoEntity();
//                    record.setEntrustmentId(id);
//                    record.setSampleId(sampleEntity.getId());
//                    mixInfoEntityMapper.updateBySampleId(record);
//                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
//        basisInfo.setCountPrice(totalMoney + "");2022年5月19日修改不在后端计算价格
        basisInfo.setState(0);
        // 解析印章数组
        if (vo.getSealTypes() != null && vo.getSealTypes().length > 0) {
            StringBuilder sealTypes = new StringBuilder();
            for (int i = 0; i < vo.getSealTypes().length; i++) {
                sealTypes.append(vo.getSealTypes()[i]);
                sealTypes.append(",");
            }
            basisInfo.setSealType(sealTypes.deleteCharAt(sealTypes.length() - 1).toString());
        }
        // 通过委托单id 获取公司名称。
        basisInfo.setEntrustCompany(entityMapper.getCompanyNameId(basisInfo.getEntrustCompanyId(), 1));
        if (!StringUtils.isEmpty(basisInfo.getEntrustCompany()) && !StringUtils.isEmpty(basisInfo.getEntrustPeople())  &&!StringUtils.isEmpty(basisInfo.getEntrustPhone())) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getEntrustCompany());
            testCompanyJsonEntity.setContacts(basisInfo.getEntrustPeople());
            testCompanyJsonEntity.setContactWay(basisInfo.getEntrustPhone());
            testCompanyJsonEntity.setType("1");
            PageHelper.clearPage();
            String entrustCompanystr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (entrustCompanystr == null) {
                // 保存新的委托联系人姓名 和所属委托单位公司id
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(basisInfo.getEntrustCompanyId());
                testCustomerEntity.setContacts(basisInfo.getEntrustPeople());
                testCustomerEntity.setPhone(basisInfo.getEntrustPhone());
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 通过见证单位和类型 查看联系人 （手机号可以不填）
        if (basisInfo.getWitnessUint() != null && basisInfo.getWitnessPerson() != null) {
            // 通过单位和类型 查看联系人和手机号是否存在
            TestCompanyJsonEntity testCompanyJsonEntity = new TestCompanyJsonEntity();
            testCompanyJsonEntity.setCompanyName(basisInfo.getWitnessUint());
            testCompanyJsonEntity.setContacts(basisInfo.getWitnessPerson());
            if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())) {
                testCompanyJsonEntity.setContactWay(basisInfo.getWitnessPhone());
            }
            testCompanyJsonEntity.setType("2");
            PageHelper.clearPage();
            String WitnessUintstr = entityMapper.GetDelegateInformation(testCompanyJsonEntity);
            if (WitnessUintstr == null) {
                // 保存新的见证联系人姓名 和所属见证单位公司id
                Integer companyId = entityMapper.getCompanyId(basisInfo.getWitnessUint(), 2);
                TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
                testCustomerEntity.setCompanyId(companyId);
                testCustomerEntity.setContacts(basisInfo.getWitnessPerson());
                if(!StringUtils.isEmpty(basisInfo.getWitnessPhone())){
                    testCustomerEntity.setPhone(basisInfo.getWitnessPhone());
                }
                testCustomerDao.insertTestCustomer(testCustomerEntity);
            }
        }
        // 获取当前用户所在科室id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        Long department = teamMapper.getTeamIdByUid(userInfo.getUserId());
        // 委托单创建人所属部门
        if(StringUtils.isEmpty(department)){
            basisInfo.setDepartment(null);
        }
        else {
            basisInfo.setDepartment(department);
        }
        entityMapper.insertEntrustInfo(basisInfo);
        return "再来一单新建委托成功";
    }

    @Override
    public Long checkEntrustId(Long entrustId) {
        return entityMapper.checkEntrustId(entrustId);
    }

    /**
     * 方法 用来处理 copy 伪造样品信息 id 并进行add
     * @param sampleList copy 样品信息集。
     * @param old 旧委托单id
     * @param id 新委托单id
     * @param entrustCompanyId 新委托单-单位id
     */
    @Transactional(rollbackFor = Exception.class)
    public void methodCopySamples(List<SampleEntity> sampleList,Long old,long id,Integer entrustCompanyId){
        // 获取样品集合 判断样品id 是否存在。 不存在 则 add样品。
            for (SampleEntity sampleEntity : sampleList) {
                SampleEntity sampleDetailVo  = sampleEntityMapper.getSampleTagInfo(sampleEntity.getId());
                // 已经找到伪造字段
                if(StringUtils.isEmpty(sampleDetailVo)){
                    // 通过旧委托单id 获取样品集合 得到 配合比信息集合。
                    List<SampleEntity> sampleSet  = sampleEntityMapper.selectSampleSet(old);
                    // 配合比 处理。
                    if(sampleEntity.getPid()!=null){
                        // 整理 配合比信息
                        SamplesAddVo samples = new SamplesAddVo();
                        //获取 以pid的 配合比子集信息集合。
                        List<SampleEntity> samplePidSub = sampleEntityMapper.selectByPid(sampleEntity.getPid());
                        List<SampleDetailAddVo> subset= new ArrayList<>();
                        if(!samplePidSub.isEmpty()){
                            for(SampleEntity sampleEntity1:samplePidSub){
                                SampleDetailAddVo sampleDetailAddVo = new SampleDetailAddVo(sampleEntity1);
                                // 增加 样品数量 与来样时间
                                sampleDetailAddVo.setSampleQuantity(sampleEntity1.getSampleQuantity());
                                sampleDetailAddVo.setReceivedDate(new Date());
                                subset.add(sampleDetailAddVo);
                            }
                        }
                        if(!sampleSet.isEmpty()){
                            for(SampleEntity sampleEntity2 :sampleSet){
                                if(sampleEntity2.getId().equals(sampleEntity.getPid())){
                                    // 前端 变更的字段
                                    sampleEntity2.setSpecs(sampleEntity.getSpecs());
                                    sampleEntity2.setBatchNumber(sampleEntity.getBatchNumber());
                                    sampleEntity2.setGeneration(sampleEntity.getGeneration());
                                   sampleEntity2.setCheckDate(sampleEntity.getCheckDate()!=null?sampleEntity.getCheckDate():new Date());
                                    sampleEntity2.setCompanyId(entrustCompanyId);
                                    SamplesAddVo samples1 = new SamplesAddVo(sampleEntity2);
                                    samples = samples1;
//                                    存储配合比子集。
                                    samples.setSamples(subset);
                                    // 获取配合比的样品字段
                                    TestSampleMixInfoEntity data = mixInfoEntityMapper.selectBySampleId(sampleEntity.getPid());
                                  if(data!=null){
                                      samples.setDesignStrength(data.getDesignStrength());
                                      samples.setIntensityConfiguration(data.getIntensityConfiguration());
                                      samples.setAntifreezeLevel(data.getAntifreezeLevel());
                                      samples.setWaterBinderRatio(data.getWaterBinderRatio());
                                      samples.setUnitWaterUse(data.getUnitWaterUse());
                                      samples.setSandRatio(data.getSandRatio());
                                      samples.setDesignSlump(data.getDesignSlump());
                                      samples.setMixingWay(data.getMixingWay());
                                  }
                                    samples.setCompanyId(entrustCompanyId);
                                    // 针对 配合比 进行处理
                                    TestSampleMixInfoEntity  addMixProportion  = testSampleEntityService.batchInsertMixSampleCopy(samples,id);
                                       if(addMixProportion!=null){
                                           sampleEntity.setId(addMixProportion.getSampleId());
                                       }
                                }
                            }
                        }
                    }
                    else{
//                        原材处理
                        if(!sampleSet.isEmpty()){
                            for(int i=0;i<sampleSet.size();i++){
                                SampleEntity oldSampleData = sampleSet.get(i);
                                if(oldSampleData.getId().equals(sampleEntity.getOldSampleid())){
                                    // 对此信息 重新 add。 并获取id 替换。
                                    List<SampleDetailAddVo> samples = new ArrayList<>();
                                    // 前端 变更的字段
                                    oldSampleData.setSpecs(sampleEntity.getSpecs());
                                    oldSampleData.setBatchNumber(sampleEntity.getBatchNumber());
                                    oldSampleData.setGeneration(sampleEntity.getGeneration());
                                    oldSampleData.setCheckDate(sampleEntity.getCheckDate()!=null?sampleEntity.getCheckDate():new Date());
                                    oldSampleData.setCompanyId(entrustCompanyId);
                                    // 其他信息不变更
                                    SampleDetailAddVo sampleDetailAddVo = new SampleDetailAddVo(oldSampleData);
                                    // 增加 样品数量 与来样时间
                                    sampleDetailAddVo.setSampleQuantity(oldSampleData.getSampleQuantity());
                                    sampleDetailAddVo.setReceivedDate(new Date());
                                    samples.add(sampleDetailAddVo);
                                    // 样品为原材的。
                                    List<TestSampleEntity> addSamples = testSampleEntityService.batchInsertSampleCopy(samples);
                                    TestSampleEntity addSample = addSamples.get(0);
                                    sampleEntity.setId(addSample.getId());
                                }
                            }
                        }
                    }
                }
            }
    }

    @Override
    public Boolean isPublish(Long entrustId) {
        Integer reportStateTaskNum = entityMapper.getReportStateTaskNum(entrustId);
        if(reportStateTaskNum > 0 ){
            return true;
        }
        return false;
    }

    /**
     * 单个 委托单附件新增
     * @param id
     * @param multipartFile
     * @return
     */
    @Override
    public Boolean uploading(Long id, MultipartFile multipartFile) {
        EntrustFileTableEntity entrustFileTableEntity = new EntrustFileTableEntity();
        entrustFileTableEntity.setEntrustId(id);
        //附件存在上传附件到服务器
//        if (file.length != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            // 根据file文件数量 规定文件名存储编号规则
//            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, fileCode + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（文件编号&委托文档资料.pdf,文件编号&原始文档.docx）
                stringfileUrlStr.append(fileCode + "&" + name);
                stringfileUrlStr.append(",");
//            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                entrustFileTableEntity.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                entrustFileTableEntity.setFileUrlStr(substring);
            }
//        }
        entrustFileTableEntity.setCarateTime(new Date());
        entrustFileTableDao.insertEntrustFileTableEntity(entrustFileTableEntity);
        return true;

    }

    @Override
    public Boolean removeding(Integer id) {
        // 根据附件id 查询文件名称 进行删除minIo文件服务器中内容。
        EntrustFileTableEntity entrustFileTableEntity = entrustFileTableDao.getEntrustFileTableEntityId(id);
        if (entrustFileTableEntity != null && entrustFileTableEntity.getFileUrl() != null) {
            // 去清除 MinIo 桶数据。
            try {
                String[] strings2 = entrustFileTableEntity.getFileUrlStr().split(",");
                for (int i = 0; i < strings2.length; i++) {
                    String[] strings3 = strings2[i].split("\\.");
                    if (strings3.length >= 2) {
                        String[] strings4 = strings3[0].split("&");
                        // 获取 文件编号
                        Long fileCode = Long.parseLong(strings4[0]);
                        MinIoUtil.deleteFile(BucketsConst.buckets_entrust_enclosure, fileCode + "." + strings3[1]);
                    }
                }
            } catch (Exception e) {
                logger.info("修改委托下清除 MinIo 桶数据 出错");
            }
        }

        entrustFileTableDao.deleteEntrustFileTableEntity(id);
        return true;
    }

    /**
     * 修改委托信息后： 触发联动效果。 同步更新任务单对应字段。
     * @param id 委托单id
     */
    void methodModifyTheTask(Long id){
        // 通过委托单id 查询任务单信息
        List<TaskTestEntity> taskList = entityMapper.selectTaskTestEntityList(id);
        if(!CollectionUtils.isEmpty(taskList)){
            // 获取委托单详情
            EntrustAddVo vo = entityMapper.selectByKeyId(id);
            for(TaskTestEntity taskTestEntity :taskList){
                    // 进行update任务单 同步
                    // 丁连春：任务单完成时间 以委托单下单时间为准
                    taskTestEntity.setRequiredCompletionTime(vo.getRequestDate());
                    // 任务单下单日期等于委托单受理日期
                    taskTestEntity.setOrderTime(vo.getAcceptanceDate());
                    // 任务单提供资料等于委托单提供资料
                    if(!org.springframework.util.StringUtils.isEmpty(vo.getPresentInformation())){
                        taskTestEntity.setPresentInformation(vo.getPresentInformation());
                    }else {
                        taskTestEntity.setPresentInformation("--");
                    }
                    // update
                    taskMapper.updateTestTask(taskTestEntity);
            }
        }

    }

    /**
     * 修改委托信息后： 触发联动效果。 同步更新样品对应字段。
     * @param id 委托单id
     */
    void methodModifyTheSample(Long id) throws ParseException {
        // 通过委托单id 查询样品信息集合
        List<SampleEntity> sampleEntityList = sampleEntityMapper.selectSampleListGroup(id);
        if(!CollectionUtils.isEmpty(sampleEntityList)){
            // 获取委托单详情
            EntrustAddVo vo = entityMapper.selectByKeyId(id);
            for(SampleEntity sampleData:sampleEntityList){
                // 比较样品签收时间 < 委托单受理日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = sdf.parse(sampleData.getReceivedDate());
                // 测试此日期是否在指定日期之后。
                if (!vo.getAcceptanceDate().after(date1)) {
                    // 签收时间 =委托单受理日期
                    sampleData.setReceivedDate(sdf.format(vo.getAcceptanceDate()));
                    // update样品信息
                    sampleEntityMapper.updateByPrimaryKeySelective(sampleData);
                }
                // 处理配合比信息
                if(!sampleData.getSampleType().equals("原材")&&!vo.getAcceptanceDate().after(date1)){
                    // 获取配合比信息
                    List<SampleEntity> sampleEntities = sampleEntityMapper.selectByPid(sampleData.getId());
                    if(CollectionUtils.isEmpty(sampleEntities)){
                        for(SampleEntity sampleEntity:sampleEntities){
                            sampleEntity.setReceivedDate(sampleData.getReceivedDate());
                            // update样品信息
                            sampleEntityMapper.updateByPrimaryKeySelective(sampleData);
                        }
                    }
                }
            }
        }
    }


    /**
     * 分配任务： 任务单流转 需要业务员提供信息
     * @param id 委托单id
     * @param taskRelEntities 任务单流转列表
     */
    void methodDistributionOfFlow(Long id, List<TestEntrustedTaskRelEntity> taskRelEntities){
        // 通过委托单id 获取任务列表信息：
        List<TestEntrustedTaskRelEntity> testEntrustedTaskRelEntityList = testEntrustedTaskRelDao.getDeptByEntrustIdList(id);
        // 补充信息。testEntrustedTaskRelEntityList 集合中 taskId 补充存入
        for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity:taskRelEntities)
        {
            // 处理信息 部门id&部门名称 获取为 部门ID
            if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getDepartment())){
                String[] deptIds = testEntrustedTaskRelEntity.getDepartment().split("&");
                testEntrustedTaskRelEntity.setDeptId(Integer.parseInt(deptIds[0]));
            }
            for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity1 :testEntrustedTaskRelEntityList)
            {
                if(testEntrustedTaskRelEntity1.getDeptId().equals(testEntrustedTaskRelEntity.getDeptId())){
                    testEntrustedTaskRelEntity.setTaskId(testEntrustedTaskRelEntity1.getTaskId());
                    testEntrustedTaskRelEntity.setDepartment(testEntrustedTaskRelEntity1.getDeptId()+"&"+testEntrustedTaskRelEntity1.getDeptName());
                    // 如果传入的日期（taskFlowDate）为空 则存入 任务单 required_completion_time
                    if(StringUtils.isEmpty(testEntrustedTaskRelEntity.getTaskFlowDate())){
                        testEntrustedTaskRelEntity.setTaskFlowDate(testEntrustedTaskRelEntity1.getTaskFlowDate());
                    }
                    testEntrustedTaskRelEntity.setEntrustId(id);
                }
            }
        }
        // 进行批量 add操作
        testEntrustedTaskRelDao.addList(taskRelEntities);
    }

    /**
     * 修改任务流转要求
     * @param testEntrustedTaskRelEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTestEntrustedTaskRelEntity(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity) {
        testEntrustedTaskRelEntity.setUpdateDate(new Date());
        testEntrustedTaskRelDao.updateData(testEntrustedTaskRelEntity);
        return true;
    }

    /**
     * 删除任务流转要求
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTestEntrustedTask(Integer id) {
        testEntrustedTaskRelDao.deletedData(id);
        return true;
    }

    /**
     * 新增任务流转要求
     * @param testEntrustedTaskRelEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTestEntrustedTaskRelEntity(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity) {
        testEntrustedTaskRelEntity.setCreateDate(new Date());
        // 通过部门id 获取name值。
        PageHelper.clearPage();
        String deptName = teamMapper.getTeamIdByName(testEntrustedTaskRelEntity.getDeptId());
        testEntrustedTaskRelEntity.setDepartment(testEntrustedTaskRelEntity.getDeptId()+"&"+deptName);
        testEntrustedTaskRelDao.addData(testEntrustedTaskRelEntity);
        return true;
    }

    /**
     * 通过委托单id 获取流转单信息集合
     * @param entrustId
     * @return
     */
    @Override
    public List<TestEntrustedTaskRelEntity> getEntrustTaskRelList(Long entrustId) {
        PageHelper.clearPage();
        return testEntrustedTaskRelDao.getEntrustTaskRelList(entrustId);
    }

    /**
     *  支持批量修改
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustedTaskRelEntityList(List<TestEntrustedTaskRelEntity> list) {
        testEntrustedTaskRelDao.updateEntrustedTaskRelEntityList(list);
        return true;
    }

    /**
     * 当天任务统计
     * @param testEntrustedTaskRelVo
     * @return
     */
    @Override
    public PageInfo taskStatisticsList(TestEntrustedTaskRelVo testEntrustedTaskRelVo) {

        List<TestEntrustedTaskRelVo> list = Lists.newArrayList();
        PageHelper.clearPage();
        list = testEntrustedTaskRelDao.getTaskStatisticsList(testEntrustedTaskRelVo);
        if(!CollectionUtils.isEmpty(list)){
            for(TestEntrustedTaskRelVo testEntrustedTaskRelVo1:list){
                // 遍历输出数据
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getRemark()))
                {
                    testEntrustedTaskRelVo1.setRemark("--");
                }
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getAddressName())){
                    testEntrustedTaskRelVo1.setAddressName("--");
                }
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getTaskCode())){
                    testEntrustedTaskRelVo1.setTaskCode("--");
                }
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getTaskSource())){
                    testEntrustedTaskRelVo1.setTaskSource("--");
                }
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getReportCode())){
                    testEntrustedTaskRelVo1.setReportCode("--");
                }
            }
        }
        PageInfo<TestEntrustedTaskRelVo> result = PageInfoUtils.list2PageInfo(list, testEntrustedTaskRelVo.getPageNum(), testEntrustedTaskRelVo.getPageSize());
        return result;
    }


}
