package com.lims.manage.erp.service.impl;

import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustPamentEntity;
import com.lims.manage.erp.entity.EntrustSampleEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.SampleAddDetailVo;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
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

    public static HttpHeaders getHttpHeaders(String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
        return headers;
    }
    /**
     * 新增委托任务
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addEntrust(EntrustAddVo vo, MultipartFile[] file) {
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        basisInfo.setId(GenID.getID());
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0,6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum !=null && entrustNum>0){
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)){
                code = entrustNum+1;
            }else {
                code = Integer.parseInt(currentTime+"0001");
            }
        }else {
            code = Integer.parseInt(currentTime+"0001");
        }
        basisInfo.setEntrustmentNo(code);
        //附件存在上传附件到服务器
        if (file != null){
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile :file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code+"."+strings[strings.length-1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)){
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<SampleItemEntity> sampleItemList = new ArrayList<>();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(samples)){
            for (SampleEntity sampleEntity:samples) {
                EntrustSampleEntity entrustSampleEntity = new EntrustSampleEntity();
                entrustSampleEntity.setEntrustmentId(basisInfo.getId());
                entrustSampleEntity.setSampleId(sampleEntity.getId());
                list.add(entrustSampleEntity);
                List<Integer> standardFileIds = sampleEntity.getStandardFileIds();
                if (!CollectionUtils.isEmpty(standardFileIds)){
                    for (Integer integer:standardFileIds) {
                        EntrustSampleEntity sampleEntity1 = new EntrustSampleEntity();
                        sampleEntity1.setSampleId(sampleEntity.getId());
                        sampleEntity1.setStandardId(integer);
                        sampleEntity1.setEntrustmentId(basisInfo.getId());
                        list1.add(sampleEntity1);
                    }
                }
                //样品下检测项
                List<SampleItemEntity> sampleCheckItem = sampleEntity.getSampleCheckItem();
                //计算检测项总价钱
                for (SampleItemEntity entity:sampleCheckItem) {
                    int money = entity.getTimes() * entity.getUnitPrice();
                    totalMoney = totalMoney+money;
                }
                sampleItemList.addAll(sampleCheckItem);
            }
            if (!CollectionUtils.isEmpty(list)){
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)){
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel
        for (SampleItemEntity entity:sampleItemList) {
            entity.setEntrustId(basisInfo.getId());
        }
        if (!CollectionUtils.isEmpty(sampleItemList)){
            entityMapper.BatchSaveEntrustSampleItem(sampleItemList);
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())){
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setPaymentDate(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
            if (ShiroUtils.getUserInfo() != null){
                pamentEntity.setOperator(ShiroUtils.getUserInfo().getUsername());
            }
            entityMapper.saveEntrustPayRecord(pamentEntity);
        }
        //得到总价钱，再保存委托基本信息
        basisInfo.setPaymentCount(totalMoney+"");
        entityMapper.insertEntrustInfo(basisInfo);
        return true;
    }

    /**
     * 修改委托
     * @param vo
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrust(EntrustAddVo vo, MultipartFile[] file) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        Integer code =  vo.getEntrustmentNo();
        //附件存在上传附件到服务器
        if (file != null){
            StringBuilder stringBuilder = new StringBuilder();
            for (MultipartFile multipartFile :file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                // 去清除 MinIo 桶数据。
               MinIoUtil.deleteFile(BucketsConst.buckets_entrust_enclosure,code+"."+strings[strings.length-1]);
                String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, multipartFile, code+"."+strings[strings.length-1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)){
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                basisInfo.setFileUrl(substring);
            }
        }
        // 遍历 样品信息 获取价格。
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件
        int totalMoney = 0;
        List<SampleEntity> samples = vo.getSamples();
        List<SampleItemEntity> sampleItemList = new ArrayList<>();
        List<EntrustSampleEntity> list = new ArrayList<>();
        List<EntrustSampleEntity> list1 = new ArrayList<>();
        List<Integer>  newSamplesId =new ArrayList<>();
        if(!CollectionUtils.isEmpty(samples)){
            for(SampleEntity sampleEntity:samples){
                newSamplesId.add(sampleEntity.getId());
            }
        }
        // 刪除的样品id集合
        List<Integer>  removeSamplesId =  entityMapper.getSampleIdSet(basisInfo.getId());
        // 新增的id集合
        List<Integer> addNumber = new ArrayList<>();
        Map<Integer,String> map = new HashMap<>();
        Iterator<Integer> dataIter = removeSamplesId.iterator();
        while (dataIter.hasNext()) {
            Integer dataNumber = dataIter.next();
            map.put(dataNumber,"t1");
            for(Integer number1:newSamplesId){
                if(number1.equals(dataNumber)){
                    dataIter.remove();
                }
            }
        }
        for (Integer number1 : newSamplesId) {
            if(map.get(number1)==null){
                addNumber.add(number1);
            }
        }
        // 新增表 test_entrusted_sample_details_rel
        if(!addNumber.isEmpty()){
            for(Integer number1:addNumber){
                sampleEntityMapper.addSampleEntity(number1,basisInfo.getId());
            }
        }
        // 删除表 test_entrusted_sample_details_rel
        if(!removeSamplesId.isEmpty()){
            for(Integer number2:removeSamplesId){
                sampleEntityMapper.removeSamplesId(number2,basisInfo.getId());
            }
        }
        //存放委托基本信息==》test_entrusted
        entityMapper.updateEntrustInfo(basisInfo);
        return true;
    }

    @Override
    public List<CheckItemDetailVo> getAllItemByProductId(Integer productId) {
        return itemEntityMapper.getAllItemByProductId(productId);
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids) {
        return itemEntityMapper.getItemInfo2(ids);
    }

    @Override
    public Map<String,List<LabelValueVo>> getItemMethodStandard(Integer id) {
        Map<String,List<LabelValueVo>> result = Maps.newHashMap();
        List<LabelValueVo> itemMethod = itemEntityMapper.getItemMethod(id);
        List<LabelValueVo> itemStandard = itemEntityMapper.getItemStandard(id);
        result.put("itemMethod",itemMethod);
        result.put("itemStandard",itemStandard);
        return result;
    }

    @Override
    public  Map<String,List<LabelValueVo>> returnEntrustData() {
        Map<String,List<LabelValueVo>> map = new HashMap<>();
        // type =1 委托单位
       List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // type =2 见证单位
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        List<TestInitDataEntity> ReturnBasisData  = testCompanyDao.selectEntrustBasis();
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
        for(TestInitDataEntity testInitDataEntity:ReturnBasisData){
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 1:  arryEntrust.add(labelValueVo); break;
                case 2:   arrySampling.add(labelValueVo); break;
                case 3:   arryCheckout.add(labelValueVo); break;
                case 4:   arryGetReport.add(labelValueVo); break;
                case 5:   arrySampleAppearance.add(labelValueVo); break;
                case 6:   arrySeal.add(labelValueVo); break;
                case 11:   arryPayment.add(labelValueVo); break;
                default:break;
            }
        }
        map.put("entrustCompany",EntrustCompany);
        map.put("witnessCompany",witnessCompany);
        map.put("arryEntrust",arryEntrust);
        map.put("arrySampling",arrySampling);
        map.put("arryCheckout",arryCheckout);
        map.put("arryGetReport",arryGetReport);
        map.put("arrySampleAppearance",arrySampleAppearance);
        map.put("arrySeal",arrySeal);
        map.put("arryTeam",arryTeam);
        map.put("arryPayment",arryPayment);
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
        if(statusNumber>=1){
            TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
            testCustomerEntity.setCompanyId(testCompanyEntity1.getCompanyId());
            testCustomerEntity.setContacts(testCompanyEntity.getContacts());
            testCustomerEntity.setPhone(testCompanyEntity.getContactWay());
           int addCustomer = testCustomerDao.insertTestCustomer(testCustomerEntity);
           if(addCustomer>=1){
               return true;
           }
            return false;
        }
        return false;
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {
        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public List<SampleDetailVo> selectSampleList2(SampleEntity paramVo) {
        return sampleEntityMapper.selectSampleList2(paramVo);
    }

    @Override
    public List<LabelValueVo> selectProductList(String productName) {
        return testProductDao.selectProductList(productName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addSampleData(SampleAddParamVo addParamVo,MultipartFile[] file) {

        int result = 0;
        //查询产品名称
        String productName = testProductDao.getProductNameById(addParamVo.getSampleName());
        //处理详情数据
        List<SampleAddDetailVo> details = addParamVo.getDetails();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = testProductDao.getMaxNumber(sdf.format(now));
        int newMax;
        if(maxNumber == null){
            newMax = 0;
        }else{
            newMax = Integer.parseInt(maxNumber);
        }
        if(details != null){
            for (int i = 0; i < details.size(); i++) {
                //生成样品编号
                StringBuilder code = new StringBuilder("YP-"+sdf.format(now)+"-");
                String sampleCode ;
                if(addParamVo.getSampleGroups()>1){
                    //组数大于1，将前n个命名为
                    if(i%addParamVo.getQuantityPerGroup() == 0){
                        newMax = newMax+1;
                    }
                    String num = String.format("%0" + 4 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i%addParamVo.getQuantityPerGroup()+1);
                    sampleCode = prefix + "_" + suffix;
                }else{
                    //生成样品编号
                    String num = String.format("%0" + 4 + "d", newMax+1);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i+1);
                    if(addParamVo.getQuantityPerGroup()>1){
                        sampleCode = prefix + "_" + suffix;
                    }else{
                        sampleCode = prefix.toString();
                    }
                }
                //保存样品图片

                MultipartFile multipartFile = null;
                int pictureName = i + 1;
                String suffix = "";
                if(file.length>0){//有上传文件
                    for (int j = 0; j < file.length; j++) {
                        String originalFilename = file[j].getOriginalFilename();
                        String[] split = originalFilename.split("\\.");
                        if((pictureName+"").equals(split[0])){
                            suffix = split[1];
                            multipartFile = file[j];
                            break;
                        }
                    }
                }
                String pictureFileName = null;
                if(!"".equals(suffix)){
                    pictureFileName = sampleCode + "." + suffix;
                }
                String pictureUrl = null;
                if(multipartFile != null){
                    pictureUrl = MinIoUtil.upload("test-sample", multipartFile, pictureFileName);
                }
                System.out.println("图片："+pictureUrl);
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo,details.get(i),productName,sampleCode,pictureUrl);
                result = sampleEntityMapper.insert(sampleEntity);
            }
        }else {
            StringBuilder code = new StringBuilder("YP-"+sdf.format(now)+"-");
            //生成样品编号
            String num = String.format("%0" + 4 + "d", newMax+1);
            StringBuilder sampleCode = code.append(num);
            SampleEntity sampleEntity = new SampleEntity(addParamVo,null,productName,sampleCode.toString(),null);
            result = sampleEntityMapper.insert(sampleEntity);
        }
        return result;
    }

    @Override
    public Integer updateSampleData(SampleAddParamVo addParamVo, MultipartFile[] file) {
        int result = 0;
        //查询产品名称
        String productName = testProductDao.getProductNameById(addParamVo.getSampleName());
        //处理详情数据
        List<SampleAddDetailVo> details = addParamVo.getDetails();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = testProductDao.getMaxNumber(sdf.format(now));
        int newMax;
        if(maxNumber == null){
            newMax = 0;
        }else{
            newMax = Integer.parseInt(maxNumber);
        }
        if(details != null){
            for (int i = 0; i < details.size(); i++) {
                //生成样品编号
                StringBuilder code = new StringBuilder("YP-"+sdf.format(now)+"-");
                String sampleCode ;
                if(addParamVo.getSampleGroups()>1){
                    //组数大于1，将前n个命名为
                    if(i%addParamVo.getQuantityPerGroup() == 0){
                        newMax = newMax+1;
                    }
                    String num = String.format("%0" + 4 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i%addParamVo.getQuantityPerGroup()+1);
                    sampleCode = prefix + "_" + suffix;
                }else{
                    //生成样品编号
                    String num = String.format("%0" + 4 + "d", newMax+1);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i+1);
                    if(addParamVo.getQuantityPerGroup()>1){
                        sampleCode = prefix + "_" + suffix;
                    }else{
                        sampleCode = prefix.toString();
                    }
                }
                //保存样品图片

                MultipartFile multipartFile = null;
                int pictureName = i + 1;
                String suffix = "";
                if(file.length>0){//有上传文件
                    for (int j = 0; j < file.length; j++) {
                        String originalFilename = file[j].getOriginalFilename();
                        String[] split = originalFilename.split("\\.");
                        if((pictureName+"").equals(split[0])){
                            suffix = split[1];
                            multipartFile = file[j];
                            break;
                        }
                    }
                }
                String pictureFileName = null;
                if(!"".equals(suffix)){
                    pictureFileName = sampleCode + "." + suffix;
                }
                String pictureUrl = null;
                if(multipartFile != null){
                    pictureUrl = MinIoUtil.upload("test-sample", multipartFile, pictureFileName);
                }
                System.out.println("图片："+pictureUrl);
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo,details.get(i),productName,sampleCode,pictureUrl);
                result = sampleEntityMapper.insert(sampleEntity);
            }
        }else {
            StringBuilder code = new StringBuilder("YP-"+sdf.format(now)+"-");
            //生成样品编号
            String num = String.format("%0" + 4 + "d", newMax+1);
            StringBuilder sampleCode = code.append(num);
            SampleEntity sampleEntity = new SampleEntity(addParamVo,null,productName,sampleCode.toString(),null);
            result = sampleEntityMapper.insert(sampleEntity);
        }
        return result;
    }

    @Override
    public List<LabelValueVo> getJudges(Integer productId) {
        return entityMapper.getJudges(productId);
    }

    @Override
    public List<EntrustHistoryEntity> getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) {
        return entityMapper.selectEntrustHistoryList(entrustHistoryEntity);
    }

    @Override
    public List<EntrustHistoryEntity> getEntrustReleasedList(EntrustHistoryEntity entrustHistoryEntity) {
        entrustHistoryEntity.setState(0);
        return entityMapper.selectEntrustHistoryList(entrustHistoryEntity);
    }

    @Override
    public EntrustAddVo getEntrustHistoryDetail(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo   = entityMapper.selectByKeyId(entrustmentId);
        // 通过委托ID 样品集合 → test_sample
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 样品信息 进行补充 检测依据集合，检测项集合
        for(SampleEntity sampleEntity:sampleCollection){
            // 样品下 检测项、检测依据 补充。
            sampleEntity.setJudgmentBasisVos(sampleEntityMapper.selectTestStandardList(sampleEntity.getId(),entrustmentId));
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
        String currentTime = DateUtil.getTodayString().substring(2,6);
        if (entrustNum !=null && entrustNum>0){
            String substring = entrustNum.toString().substring(0, 4);
            if (substring.equals(currentTime)){
                code = entrustNum+1;
            }else {
                code = Integer.parseInt(currentTime+"001");
            }
        }else {
            code = Integer.parseInt(currentTime+"001");
        }
        entity.setCode(code.toString());
        if (!StringUtils.isEmpty(entity.getTeamId())){
            //设置接收人为团队副团长
            List<SysUserEntity> userEntity = teamMapper.getUsersByTid(entity.getTeamId());
            if (!CollectionUtils.isEmpty(userEntity)){
                for (SysUserEntity sysUserEntity:userEntity) {
                    if (sysUserEntity.getPosition().equals(Const.SYS_MANAGER_LOG)){
                        entity.setReceiver(sysUserEntity.getUsername());
                    }
                }
                entity.setReceiveTime(new java.sql.Date(System.currentTimeMillis()));
            }
        }
        //任务单保存
        taskMapper.save(entity);
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId());
        return true;
    }

    @Override
    public XWPFDocument downloadEntrust(EntrustAddVo detail, InputStream object) {
        XWPFDocument doc = null;
        try {
            //TODO 设置模板数据
            doc = new XWPFDocument(object);
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            List<XWPFTableCell> cells;
            for (XWPFTable table : tables) {
                //表格属性
                CTTblPr pr = table.getCTTbl().getTblPr();
                //获取表格对应的行
                rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    //获取行对应的单元格
                    cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        System.out.println(cell.getText());;
                    }
                }
            }


        }catch (Exception e){
            logger.error("设置委托单信息到模板异常:{}",e);
        }
        return doc;
    }

    @Override
    public ResponseEntity<byte[]> getSampleTagInfo(Integer sampleId) {
        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        HashMap<String, SampleDetailVo> result = Maps.newHashMap();
        String fileName ="";
        if(sampleTagInfo != null ){
            fileName = sampleTagInfo.getSampleCode()+"样品标签.xlsx";
            result.put("result",sampleTagInfo);
        }else{
            return null;
        }
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        HttpHeaders headers = null;
        try {
            headers = getHttpHeaders("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        byte[] bytes = null;
        Workbook sheets = null;
        try {
            sheets = transformer.transformXLS(fileStream, result);
            try {
                sheets.write(outputStream);
                bytes = outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        if(bytes != null ){
            return new ResponseEntity<>(bytes, headers, HttpStatus.CREATED);
        }else{
            return null;
        }
    }

    @Override
    public String getSampleTagInfo2(Integer sampleId) {
        SampleDetailVo sampleTagInfo = sampleEntityMapper.getSampleTagInfo(sampleId);
        if(sampleTagInfo == null){
            return null;
        }else{
            String fileName =sampleTagInfo.getSampleCode()+"样品标签.xlsx";
            String fileUrl = null;
            HashMap<String, SampleDetailVo> result = Maps.newHashMap();
            result.put("result",sampleTagInfo);
            XLSTransformer transformer = new XLSTransformer();
            InputStream fileStream = MinIoUtil.getFileStream("test-sample-template", "sample-template.xlsx");
            try {
                transformer.transformXLS(fileStream,result);
                fileUrl = MinIoUtil.upload("test-sample-template", fileName, fileStream, "application/octet-stream");
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            return fileUrl;
        }
    }
}
