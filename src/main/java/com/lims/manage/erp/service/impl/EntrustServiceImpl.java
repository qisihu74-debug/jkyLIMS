package com.lims.manage.erp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.pdf.facades.IFormEditor;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.EntrustFileTableDao;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.mapper.TestEntrustedTaskRelDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleMixInfoEntityMapper;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.converter.core.Color;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.docx4j.vml.officedrawing.STColorMode;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;
    @Autowired
    private LogManagerService logManagerService;

    public static HttpHeaders getHttpHeaders(String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
        return headers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized String addEntrustTest0620(EntrustAddVo vo, MultipartFile[] file) throws Exception {
            // 获取业务受理人id
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            // 样品编号变动 = true
            Boolean sampleStatus = false;
            //存放委托基本信息==》test_entrusted
            EntrustEntity basisInfo = new EntrustEntity(vo);
            long id = GenID.getID();
            basisInfo.setId(id);
            //设置委托编号
            SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
            String acceptanceDate = yyyyMMddHH_NOT_.format(basisInfo.getAcceptanceDate()).substring(0,6);
            //获取并设置委托编号，相应的类别
            EntrustCategoryVo entrustCategoryVo = returnEntrustCategoryVo(vo.getEntrustCategory(),acceptanceDate);
            basisInfo.setEntrustmentNo(entrustCategoryVo.getEntrustmentNo());
            basisInfo.setEntrustCategory(entrustCategoryVo.getEntrustCategory());
            basisInfo.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            // 通过委托编号 查询是否存在
            PageHelper.clearPage();
            if (entityMapper.getByDataEntrustMaxNo(basisInfo.getEntrustmentNo(),basisInfo.getEntrustCategoryType()) != null) {
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
                    SampleEntity sampleData = new SampleEntity();
                    // 使用方法 处理样品来样时间 与委托单受理日期
                    sampleStatus = methodAcceptanceDate(sampleEntity.getId(),vo.getAcceptanceDate(),sampleData);
                    // 委托单创建 更新样品状态 state 待检0
                    sampleData.setState("0");
                    sampleEntityMapper.updateByPrimaryKeySelective(sampleData);
//                    // 增加样品样品流转状态
//                    SampleCirculationRecord sa = new SampleCirculationRecord();
//                    sa.setSampleId(sampleData.getId());
//                    sa.setStatus("0");
//                    sa.setOperatorId(userInfo.getUserId());
//                    sa.setOperatorName(vo.getBusinessAcceptor());
//                    sa.setTime(new Date());
//                    sampleEntityMapper.saveSampleCirculationRecord(sa);
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
                            //处理检测项名称中包含中文（），《》
                            String checkItemName = entity.getCheckItemName();
                            char char1 = '（';
                            char char2 = '）';
                            char char3 = '，';
                            char char4 = '《';
                            char char5 = '》';
                            String newItemName = StrUtil.removeAll(checkItemName, char1, char2, char3, char4, char5);
                            entity.setCheckItemName(newItemName);
                        }
                    }
                    if(!CollectionUtils.isEmpty(sampleCheckItem)){
                        //记录日志
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("委托编号为\t"+basisInfo.getEntrustmentNo());
                        for(SampleItemEntity sampleItemEntity:sampleCheckItem){
                            stringBuilder.append("\t检测项名称为\t"+sampleItemEntity.getCheckItemName()+"\t单价为\t"+sampleItemEntity.getUnitPrice()+"\t检测样次\t"+sampleItemEntity.getTimes()
                            +"\t检测项依据为\t"+sampleItemEntity.getStandardId());
                        }
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增委托-批量保存委托样品下检测项信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                        entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
                    }
                    //根据委托检测类别关联 配合比检测信息和委托单ID
                    if (vo.getEntrustTestType().contains("配合比")) {
                        TestSampleMixInfoEntity record = new TestSampleMixInfoEntity();
                        record.setEntrustmentId(id);
                        record.setSampleId(sampleEntity.getId());
                        //记录日志
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "根据委托检测类别关联 配合比检测信息和委托单ID"
                                +"\t委托编号为\t"+basisInfo.getEntrustmentNo()+"\t设计强度（MPa）\t"+record.getDesignStrength()+"\t配制强度（MPa）\t"+record.getIntensityConfiguration()
                                +"\t抗（渗、冻）等级\t"+record.getAntifreezeLevel()+"\t水胶比\t"+record.getWaterBinderRatio()+"\t单位用水量（kg）\t"+record.getUnitWaterUse()
                                +"\t砂率（%）\t"+record.getSandRatio()+"\t设计坍落度（mm）\t"+record.getDesignSlump()+"\t拌和方式\t"+record.getMixingWay()+"\t样品id\t"+record.getSampleId(), Const.ENTRUST_FOUND, true);
                        mixInfoEntityMapper.updateBySampleId(record);
                    }
                }
                if (!CollectionUtils.isEmpty(list)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("委托编号"+basisInfo.getEntrustmentNo());
                    for(EntrustSampleEntity entrustSampleEntity:list){
                        stringBuilder.append("\t样品id\t"+entrustSampleEntity.getSampleId());
                    }
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "委托单与样品建立关系\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                    entityMapper.BatchSaveEntrustSample(list);
                }
                if (!CollectionUtils.isEmpty(list1)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("委托编号"+basisInfo.getEntrustmentNo());
                    for(EntrustSampleEntity entrustSampleEntity:list1){
                        stringBuilder.append("\t样品id\t"+entrustSampleEntity.getSampleId()+"\t样品委托依据\t"+entrustSampleEntity.getStandardId());
                    }
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "保存委托单样品，判定依据信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                    entityMapper.BatchSaveSampleStandard(list1);
                }
            }
            //更新委托单收费记录信息
            if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
                EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
                pamentEntity.setEntrustmentId(basisInfo.getId());
                pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
                pamentEntity.setPrice(vo.getPaymentRecord());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增委托缴费记录新增\t"+"\t委托编号\t"+basisInfo.getEntrustmentNo()+"\t委托单收费记录\t"+pamentEntity.getPrice(), Const.ENTRUST_FOUND, true);
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
                /**
                 *  处理委托单位信息
                 */
                TestCompanyVo companyClientVo = new TestCompanyVo();
                companyClientVo.setType(1);
                companyClientVo.setCompanyName(basisInfo.getEntrustCompany());
                companyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getEntrustPeople()) ? basisInfo.getEntrustPeople() : null);
                companyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getEntrustPhone()) ? basisInfo.getEntrustPhone() : null);
                /**
                 *  使用方法处理委托单位信息
                 */
                Integer entrustCompanyId = methodUnit(companyClientVo);
                basisInfo.setEntrustCompanyId(entrustCompanyId);
                //处理见证单位信息
                TestCompanyVo witnessCompanyClientVo = new TestCompanyVo();
                witnessCompanyClientVo.setType(2);
                witnessCompanyClientVo.setCompanyName(!StringUtils.isEmpty(basisInfo.getWitnessUint()) ? basisInfo.getWitnessUint() : null);
                witnessCompanyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getWitnessPerson()) ? basisInfo.getWitnessPerson() : null);
                witnessCompanyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getWitnessPhone()) ? basisInfo.getWitnessPhone() : null);
                // 处理见证单位信息
                methodUnit(witnessCompanyClientVo);
                // 获取当前用户所在科室id
            Long department = teamMapper.getTeamIdByUid(userInfo.getUserId());
            // 委托单创建人所属部门
            if(StringUtils.isEmpty(department)){
                basisInfo.setDepartment(null);
            }
            else {
                basisInfo.setDepartment(department);
            }
            // 判断取报告方式 非邮寄的话 清空状态
            if(!StringUtils.isEmpty(basisInfo.getReportType()) && !basisInfo.getReportType().equals("邮寄")){
                basisInfo.setAddress(null);
                basisInfo.setMobile(null);
                basisInfo.setAddressee(null);
                basisInfo.setReportReceivingUnit(null);
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), " 新增委托信息成功\t委托编号为\t"+basisInfo.getEntrustmentNo()+"\t委托单位\t"+basisInfo.getEntrustCompany()
                   +"\t委托人\t"+basisInfo.getEntrustPeople()+ "\t要求完成时间\t"+(new Timestamp(basisInfo.getRequestDate().getTime()))+"\t委托检测类别\t"+basisInfo.getEntrustTestType()+"\t检测目的\t"+basisInfo.getCheckPurpose()
                    +"\t业务受理人\t"+basisInfo.getBusinessAcceptor()+"\t报告份数\t"+basisInfo.getReportCount()+"\t受理日期\t"+(new Timestamp(basisInfo.getAcceptanceDate().getTime()))
                    +"\t任务来源\t"+basisInfo.getTaskSource()+"\t实收价格\t"+basisInfo.getActualPrice()+"\t应收价格\t"+basisInfo.getSystemPrice()+"\t折扣率\t"+basisInfo.getDiscount(), Const.ENTRUST_FOUND, true);
            basisInfo.setAuditState("1");
            basisInfo.setCreateTime(new Date());
            // 委托单是否留样1.保留2.废弃 默认：否
            basisInfo.setIsSave("否");
            // 新增经营人员
            basisInfo.setOperatingPersonnel(vo.getOperatingPersonnel());
            entityMapper.insertEntrustInfo(basisInfo);
            if(sampleStatus){
                return "新建委托成功\n"+"委托与样品时间不一致，样品编号及签收时间发生变动";
            }
            return "新建委托成功";
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
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "修改委托：缴费记录新增\t"+"\t委托编号\t"+basisInfo.getEntrustmentNo()+"\t委托单收费记录\t"+pamentEntity.getPrice(), Const.ENTRUST_FOUND, true);
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
        // 针对客户委托单 不进行存储相关委托单位信息与见证单位信息
        PageHelper.clearPage();
        if(StringUtils.isEmpty(entityMapper.selectEntrustClientStatus(basisInfo.getId()))){
            /**
             *  处理委托单位信息
             */
            TestCompanyVo companyClientVo = new TestCompanyVo();
            companyClientVo.setType(1);
            companyClientVo.setCompanyName(basisInfo.getEntrustCompany());
            companyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getEntrustPeople()) ? basisInfo.getEntrustPeople() : null);
            companyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getEntrustPhone()) ? basisInfo.getEntrustPhone() : null);
            /**
             *  使用方法处理委托单位信息
             */
            Integer entrustCompanyId = methodUnit(companyClientVo);
            basisInfo.setEntrustCompanyId(entrustCompanyId);
            //处理见证单位信息
            TestCompanyVo witnessCompanyClientVo = new TestCompanyVo();
            witnessCompanyClientVo.setType(2);
            witnessCompanyClientVo.setCompanyName(!StringUtils.isEmpty(basisInfo.getWitnessUint()) ? basisInfo.getWitnessUint() : null);
            witnessCompanyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getWitnessPerson()) ? basisInfo.getWitnessPerson() : null);
            witnessCompanyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getWitnessPhone()) ? basisInfo.getWitnessPhone() : null);
            // 处理见证单位信息
            methodUnit(witnessCompanyClientVo);
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("变更前"+"\t委托单位\t"+vo.getEntrustCompany()
                        +"\t委托人\t"+vo.getEntrustPeople()+ "\t要求完成时间\t"+(new Timestamp(vo.getRequestDate().getTime()))+"\t委托检测类别\t"+vo.getEntrustTestType()+"\t检测目的\t"+vo.getCheckPurpose()
                        +"\t业务受理人\t"+vo.getBusinessAcceptor()+"\t报告份数\t"+vo.getReportCount()+"\t受理日期\t"+(new Timestamp(vo.getAcceptanceDate().getTime())));
        stringBuilder.append("\n变更后"+"\t委托单位\t"+basisInfo.getEntrustCompany()
                +"\t委托人\t"+basisInfo.getEntrustPeople()+ "\t要求完成时间\t"+(new Timestamp(basisInfo.getRequestDate().getTime()))+"\t委托检测类别\t"+basisInfo.getEntrustTestType()+"\t检测目的\t"+basisInfo.getCheckPurpose()
                +"\t业务受理人\t"+basisInfo.getBusinessAcceptor()+"\t报告份数\t"+basisInfo.getReportCount()+"\t受理日期\t"+(new Timestamp(basisInfo.getAcceptanceDate().getTime()))
                +"\t任务来源\t"+basisInfo.getTaskSource()+"\t实收价格\t"+basisInfo.getActualPrice()+"\t应收价格\t"+basisInfo.getSystemPrice()+"\t折扣率\t"+basisInfo.getDiscount());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), " 更新委托：成功\t委托编号为\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
        // 判断取报告方式 非邮寄的话 清空状态
        if(!StringUtils.isEmpty(basisInfo.getReportType()) && !basisInfo.getReportType().equals("邮寄")){
            basisInfo.setAddress(null);
            basisInfo.setMobile(null);
            basisInfo.setAddressee(null);
            basisInfo.setReportReceivingUnit(null);
        }
        // 新增经营人员
        basisInfo.setOperatingPersonnel(vo.getOperatingPersonnel());
        entityMapper.updateEntrustInfo(basisInfo);
        // 修改委托信息后： 触发联动效果。 同步更新任务单对应字段。
        methodModifyTheTask(basisInfo.getId());
        // 修改委托信息后： 触发联动效果。同步更新样品信息
        methodModifyTheSample(basisInfo.getId());
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
            entityMapper.updateEntrustInfos(basisInfo);
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
            entityMapper.updateEntrustInfos(basisInfo);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEntrustTestNewSampleEnscript0621(EntrustAddVo vo) {
        EntrustEntity basisInfo = new EntrustEntity(vo);
        // 判断 样品与委托单是否存在
        List<Integer> sampleIds = entityMapper.getSampleId(basisInfo.getId());
        // 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 旧样品id
        List<Integer> oldSampleIds = new ArrayList<>();
        oldSampleIds.addAll(sampleIds);
        // 前端返回样品id
        List<Integer> leadingEndIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                //修改样品为未使用
                sampleEntityMapper.updateSampleUse(sampleId, 0);
                oldSampleIds.add(sampleId);
            }
            // 1.0 样品与委托单已存在 1.1、删除样品id
            entityMapper.removeTestEntrustedSampleDetailsRel(basisInfo.getId());
        }
        if(!CollectionUtils.isEmpty(vo.getSamples())){
            for(SampleEntity leadSample :vo.getSamples()){
                leadingEndIds.add(leadSample.getId());
            }
        }
        // 进行方法处理 样品状态操作
//        methodSampleIds(oldSampleIds,leadingEndIds,userInfo.getName(),userInfo.getUserId());
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
                    if(!CollectionUtils.isEmpty(sampleCheckItem)){
                        entityMapper.BatchSaveEntrustSampleItem(sampleCheckItem);
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
//        if (totalMoney != 0) {
            //得到总价钱，再保存委托基本信息
//            basisInfo.setPaymentCount(totalMoney + "");2022年5月20日修改，不在后台计算检测项价格
            //存放委托基本信息==》test_entrusted
            basisInfo.setState(0);
            entityMapper.updateEntrustInfos(basisInfo);
//        }
        return true;
    }

    @Override
    public String updateEntrustCheckItem(EntrustAddVo vo){
        // 样品编号变动 = true
        Boolean sampleStatus = false;
        if(!CollectionUtils.isEmpty(vo.getSamples())){
            // 获取委托单受理日期
            EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(vo.getId());
            List<SampleEntity> samples = vo.getSamples();
            for(SampleEntity sampleEntity1:samples){
                SampleEntity sampleData = new SampleEntity();
                // 使用方法 处理样品来样时间 与委托单受理日期
                sampleStatus = methodAcceptanceDate(sampleEntity1.getId(),entrustAddVo.getAcceptanceDate(),sampleData);
                sampleEntityMapper.updateByPrimaryKeySelective(sampleData);
            }
        }
        //查询当前委托单下的任务单数量
        Integer reportStateTaskNum = entityMapper.getReportStateTaskNum(vo.getId());
        if(reportStateTaskNum>0){//已发布
             if(!updatePublishedEntrust0711(vo)){
                 return null;
             }
        }else{//未发布
            // 记录日志
            StringBuffer log = new StringBuffer();
            log.append("未发布:前端提供数据 ");
            // 记录前端提供数据日志
            if(vo!=null){
                if(!CollectionUtils.isEmpty(vo.getSamples())){
                    for(SampleEntity sampleEntity  : vo.getSamples()){
                        log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode());
                        if(!CollectionUtils.isEmpty(sampleEntity.getSampleCheckItem())){
                            // 遍历
                            for(SampleItemEntity sampleItemEntity : sampleEntity.getSampleCheckItem()){
                                log.append("检测项id"+sampleItemEntity.getId()+"检测项名"+sampleItemEntity.getCheckItemName()
                                        + "checkItemId"+sampleItemEntity.getCheckItemId()+"单价"+sampleItemEntity.getUnitPrice()
                                        +"次数"+sampleItemEntity.getTimes()+"所属任务单id"+sampleItemEntity.getTaskId()
                                        +"依据id"+sampleItemEntity.getStandardId()+" ");
                            }
                        }else {
                            log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode()+"下提供的检测数据为空");
                        }
                    }
                }
            }
            // 未发布 日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                    " 修改样品信息：\t"+log, Const.ENTRUST_FOUND, true);
            if(!updateEntrustTestNewSampleEnscript0621(vo)){
                return null;
            }
        }
        if(sampleStatus){
            return "修改委托下样品成功\t"+"委托与样品时间不一致，样品编号及签收时间发生变动";
        }
        return "修改委托下样品成功";
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
            entityMapper.updateEntrustInfos(basisInfo);
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
            entityMapper.updateEntrustInfos(basisInfo);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    Boolean updatePublishedEntrust0711(EntrustAddVo vo){
        // 记录日志
        StringBuffer log = new StringBuffer();
        log.append("样品修改前:旧数据");
        EntrustEntity basisInfo = new EntrustEntity(vo);
        //获取委托单原有信息
        EntrustAddVo oldEntrustInfo = getEntrustHistoryDetailTest(basisInfo.getId());
        // 记录旧数据日志
        if(oldEntrustInfo!=null){
            if(!CollectionUtils.isEmpty(oldEntrustInfo.getSamples())){
               for(SampleEntity sampleEntity  : oldEntrustInfo.getSamples()){
                   log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode()+" ");
                   if(!CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVoStr())){
                        // 遍历
                       for(JudgmentBasisVo sampleItemEntity : sampleEntity.getJudgmentBasisVoStr()){
                           log.append("检测项id"+sampleItemEntity.getId()+"检测项名"+sampleItemEntity.getCheckItemName()
                                   + "checkItemId"+sampleItemEntity.getCheckItemId()+"单价"+sampleItemEntity.getCheckPrice()
                                   +"次数"+sampleItemEntity.getTimes()+"所属任务单id"+sampleItemEntity.getTaskId()
                                   +"依据id"+sampleItemEntity.getStandardId()+"依据名称"+sampleItemEntity.getStandardName()+" ");
                       }
                   } else {
                       log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode()+"下提供的检测数据为空 ");
                   }
               }
            }
        }
        log.append("前端提供数据");
        // 记录前端提供数据日志
        if(vo!=null){
            if(!CollectionUtils.isEmpty(vo.getSamples())){
                for(SampleEntity sampleEntity  : vo.getSamples()){
                    log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode());
                    if(!CollectionUtils.isEmpty(sampleEntity.getSampleCheckItem())){
                        // 遍历
                        for(SampleItemEntity sampleItemEntity : sampleEntity.getSampleCheckItem()){
                            log.append("检测项id"+sampleItemEntity.getId()+"检测项名"+sampleItemEntity.getCheckItemName()
                                    + "checkItemId"+sampleItemEntity.getCheckItemId()+"单价"+sampleItemEntity.getUnitPrice()
                                    +"次数"+sampleItemEntity.getTimes()+"所属任务单id"+sampleItemEntity.getTaskId()
                                    +"依据id"+sampleItemEntity.getStandardId()+" ");
                        }
                    }else {
                        log.append("样品id"+sampleEntity.getId()+"样品编号"+sampleEntity.getSampleCode()+"下提供的检测数据为空");
                    }
                }
            }
        }
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
                        logger.info("委托单编号："+oldEntrustInfo.getEntrustmentNo()+"有新增检测项，状态值已变更为"+state);
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

        // 通过检测项 是否有所属团队 设置委托单状态 task!=null 设置state=默认值，task==null 设置待发布 state=1。
        List<Long> entrustIds = new ArrayList<>();
        entrustIds.add(vo.getId());
        List<SampleItemEntity> itemList = entityMapper.getSampleItemList(entrustIds);
        if(!CollectionUtils.isEmpty(itemList)){
            for(SampleItemEntity itemData :itemList){
                if(itemData !=null && itemData.getTaskId()== null){
                    state = 0;
                }
            }
        }
        basisInfo.setState(state);
        logger.info("委托单编号："+oldEntrustInfo.getEntrustmentNo()+"的委托单的状态为"+state);
        entityMapper.updateEntrustInfos(basisInfo);
        // 记录已经发布委托单  原有检测项信息 更新后检测项信息
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                " 修改样品信息：\t"+log, Const.ENTRUST_FOUND, true);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String abandonEntrust(EntrustEntity entrustEntity) {
//        //查询当前委托单下的任务单数量
//        Integer reportStateTaskNum = entityMapper.getReportStateTaskNum(entrustEntity.getId());
//        if(reportStateTaskNum>0){
//            //已发布
//            return "作废委托失败！:\t 委托单已经发布";
//         }
        entrustEntity.setState(144);
        entityMapper.updateEntrustInfos(entrustEntity);
        /**
         *  增加日志
         */
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("废弃委托单  委托单id:"+entrustEntity.getId()+" 状态为:"+entrustEntity.getState());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.ENTRUST_FOUND, true);
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
        PageHelper.clearPage();
        List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // 查询委托单下 所有委托单位信息
        PageHelper.clearPage();
        List<LabelValueVo> EntustCompanys = new ArrayList<>();
        List<String> entustCompanys = testCompanyDao.selectEntrustCompanys();
        if(!CollectionUtils.isEmpty(entustCompanys)){
            for(int i=0;i<entustCompanys.size();i++){
                LabelValueVo labelValueVo = new LabelValueVo();
                String company = entustCompanys.get(i);
                labelValueVo.setLabel(company);
                labelValueVo.setValue((long) i);
                EntustCompanys.add(labelValueVo);
            }
        }
        // type =2 见证单位
        PageHelper.clearPage();
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        PageHelper.clearPage();
        List<TestInitDataEntity> ReturnBasisData = testCompanyDao.selectEntrustBasis();
        // 团队信息
        PageHelper.clearPage();
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
        // 15：编号类别
        List<LabelValueVo> numberCategory = new ArrayList<>();
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
                case 15:
                    numberCategory.add(labelValueVo);
                    break;
                default:
                    break;
            }
        }
        map.put("entrustCompany", EntrustCompany);
        map.put("entrustCompanys", EntustCompanys);
        map.put("witnessCompany", witnessCompany);
        map.put("arryEntrust", arryEntrust);
        map.put("arrySampling", arrySampling);
        map.put("arryCheckout", arryCheckout);
        map.put("arryGetReport", arryGetReport);
        map.put("arrySampleAppearance", arrySampleAppearance);
        map.put("arrySeal", arrySeal);
        map.put("arryTeam", arryTeam);
        map.put("arryPayment", arryPayment);
        map.put("numberCategory", numberCategory);
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
        /**
         *  增加日志
         */
        StringBuilder stringBuilder1 = new StringBuilder();
        // 变更前：
        stringBuilder1.append("新增单位信息：单位名称:"+testCompanyEntity1.getCompanyName());
        stringBuilder1.append(" 单位类型：");
        if(!StringUtils.isEmpty(testCompanyEntity1.getType())){
            if(testCompanyEntity1.getType().equals(1)){
                stringBuilder1.append("委托单位");
            }
            if(testCompanyEntity1.getType().equals(2)){
                stringBuilder1.append("见证单位");
            }
        }
        stringBuilder1.append(" 单位地址："+testCompanyEntity1.getAddress());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.Applicant_Info, true);
        testCompanyDao.insert(testCompanyEntity1);
        return true;
    }

//    public PageInfo getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException {
//        if (entrustHistoryEntity.getDateInterval() != null) {
//            String[] strArry = entrustHistoryEntity.getDateInterval().split("~");
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            for (int i = 0; i <= strArry.length - 1; i++) {
//                if (i == 0) {
//                    entrustHistoryEntity.setStartDate(dateFormat.parse(strArry[i]));
//                }
//                if (i == 1) {
//                    entrustHistoryEntity.setEndingDate(dateFormat.parse(strArry[i]));
//                }
//            }
//        }
//        // 获取状态
//        List<EntrustHistoryEntity> dataList = new ArrayList<>();
//        if (!StringUtils.isEmpty(entrustHistoryEntity.getState())&&entrustHistoryEntity.getState() == 1) {
////            PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
//            PageHelper.clearPage();
//            dataList = entityMapper.selectEntrustHistoryTaskListRelease_of(entrustHistoryEntity);
//            //存放任务编号
////            if(!CollectionUtils.isEmpty(dataList)){
////                for (EntrustHistoryEntity entrustHistoryEntity1 : dataList) {
////                    entrustHistoryEntity1.setTaskCodes(entityMapper.getTaskCode(entrustHistoryEntity1.getId()));
////                }
////            }
//            PageInfo<EntrustHistoryEntity> result = PageInfoUtils.list2PageInfo(dataList, entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
////            PageInfo<EntrustHistoryEntity> result = new PageInfo<>(dataList);
//            return result;
//        }
////        PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
//        PageHelper.clearPage();
//        dataList = entityMapper.selectEntrustTaskHistoryList(entrustHistoryEntity);
////        if(!CollectionUtils.isEmpty(dataList)){
////            for (EntrustHistoryEntity entrustHistoryEntity1 : dataList) {
////                entrustHistoryEntity1.setTaskCodes(entityMapper.getTaskCode(entrustHistoryEntity1.getId()));
////            }
////        }
////        PageInfo<EntrustHistoryEntity> result = new PageInfo<>(dataList);
//        PageInfo<EntrustHistoryEntity> result = PageInfoUtils.list2PageInfo(dataList, entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
//        return result;
//    }
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
        //拆分委托编号
        if(!StringUtils.isEmpty(entrustHistoryEntity.getEntrustmentNostr())){
            EntrustCategoryVo entrustCategoryVo = EntrustNoStrUtils.splitEntrustNo(entrustHistoryEntity.getEntrustmentNostr());
            entrustHistoryEntity.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            if(!StringUtils.isEmpty(entrustCategoryVo.getEntrustmentNo())){
                entrustHistoryEntity.setEntrustmentNo(String.valueOf(entrustCategoryVo.getEntrustmentNo()));
            }
        }
        // 获取状态
        PageInfo<EntrustHistoryEntity> pageInfo = new PageInfo<>();
        List<EntrustHistoryEntity> dataList = new ArrayList<>();
        PageHelper.startPage(entrustHistoryEntity.getPageNum(), entrustHistoryEntity.getPageSize());
        if (!StringUtils.isEmpty(entrustHistoryEntity.getState())&&entrustHistoryEntity.getState() == 1) {
            dataList = entityMapper.selectEntrustHistoryTaskListRelease_of_by_view(entrustHistoryEntity);
        }else{
            dataList = entityMapper.selectEntrustTaskHistoryList_by_view(entrustHistoryEntity);
        }
        pageInfo = new PageInfo<>(dataList);
        if (pageInfo.getTotal() ==1){
            PageHelper.clearPage();
            PageHelper.startPage(0, 1);
            if (!StringUtils.isEmpty(entrustHistoryEntity.getState())&&entrustHistoryEntity.getState() == 1) {
                dataList = entityMapper.selectEntrustHistoryTaskListRelease_of_by_view(entrustHistoryEntity);
            }else{
                dataList = entityMapper.selectEntrustTaskHistoryList_by_view(entrustHistoryEntity);
            }
            pageInfo = new PageInfo<>(dataList);
        }
        if(CollectionUtil.isNotEmpty(dataList)){
            // 遍历
            for(EntrustHistoryEntity entity :dataList){
                if(CollectionUtil.isNotEmpty(entity.getTaskCodes())){
                    List<TaskCodeVo> taskCodeVos = new ArrayList<>();
                    // 根据逗号 截取
                    String[] taskCodes = entity.getTaskCodes().get(0).getTaskCode().split("\\,");
                    String[] teamNames = entity.getTaskCodes().get(0).getTeamName().split("\\,");
                    Set<String> taskCodeSet = new HashSet<>();
                    for(int i=0; i<taskCodes.length; i++){
                        taskCodeSet.add(taskCodes[i]+","+teamNames[i]);
                    }
                    for(String str : taskCodeSet){
                        TaskCodeVo taskCodeVo = new TaskCodeVo();
                        String[] arrays = str.split("\\,");
                        taskCodeVo.setTaskCode(arrays[0]);
                        taskCodeVo.setTeamName(arrays[1]);
                        taskCodeVos.add(taskCodeVo);
                    }
                    entity.setTaskCodes(taskCodeVos);
                }
            }
        }
        //设置样品信息
        if(!CollectionUtils.isEmpty(dataList)){
            List<EntrustSampleInfoVo> entrustSampleInfos = entityMapper.getEntrustSampleInfoIds_by_view(pageInfo.getList());
            for (EntrustHistoryEntity entity : pageInfo.getList()) {
                HashSet<EntrustSampleInfoVo> sampleInfoVos = new HashSet<>();
                for(EntrustSampleInfoVo entrustSampleInfoVo : entrustSampleInfos){
                    if(entrustSampleInfoVo.getEntrustId().equals(entity.getId())){
                        sampleInfoVos.add(entrustSampleInfoVo);
                    }
                }
                List<EntrustSampleInfoVo> list = StreamSupport.stream(sampleInfoVos.spliterator(), false).collect(Collectors.toList());
                entity.setSampleInfoVos(list);
            }
        }
        //设置物流单号信息
        if(!CollectionUtils.isEmpty(pageInfo.getList())){
            for (EntrustHistoryEntity entity : pageInfo.getList()) {
                List<String> sampleLogisticsNoArr = Lists.newArrayList();
                String sampleLogisticsNo = entity.getSampleLogisticsNo();
                if(!StringUtils.isEmpty(sampleLogisticsNo)){
                    String[] split = sampleLogisticsNo.split(",");
                    for (int i = 0; i < split.length; i++) {
                        sampleLogisticsNoArr.add(split[i]);
                    }
                }
                entity.setSampleLogisticsNoArr(sampleLogisticsNoArr);
            }
        }
        return pageInfo;
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
        //拆分委托编号
        if(!StringUtils.isEmpty(entrustHistoryEntity.getEntrustmentNostr())){
            EntrustCategoryVo entrustCategoryVo = EntrustNoStrUtils.splitEntrustNo(entrustHistoryEntity.getEntrustmentNostr());
            entrustHistoryEntity.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            if(!StringUtils.isEmpty(entrustCategoryVo.getEntrustmentNo())){
                entrustHistoryEntity.setEntrustmentNo(String.valueOf(entrustCategoryVo.getEntrustmentNo()));
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
        // 查询团队名称
        if(entrustAddVo.getTeam() != null){
            LabelValueVo team = entityMapper.getIssueDeptById(entrustAddVo.getTeam());
            entrustAddVo.setTeamName(team.getLabel());
        }
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
        List<ReportProgressVo> reportProgressVo = dealReportsState(entrustmentId);
        entrustAddVo.setReportProgresses(reportProgressVo);
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
                // 任务单state =144 根据日期修改状态
                if(state == 144){
                    // 任务单废弃 则根据时间赋予状态
                    if(taskProgressVo.getOrderTime()!=null){
                        taskProgressVo.setState(0);
                    }
                    if(taskProgressVo.getReceiveTime()!=null){
                        taskProgressVo.setState(1);
                    }
                    if(taskProgressVo.getStartDetectionTime()!=null){
                        taskProgressVo.setState(2);
                    }
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
        //TODO 兼容中间报告
        ReportNodeVo reportRecordEntity = null;
        Long id = recordEntityMapper.checkExist(entrustmentId,"0");
        if (id == null){
            reportRecordEntity = recordEntityMapper.getReportNodeByZjEntrustId(entrustmentId);
        }else {
            reportRecordEntity = recordEntityMapper.getReportNodeByEntrustId(entrustmentId);
        }
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
            result = new ReportProgressVo(reportRecordEntity.getReportCode(),Integer.parseInt(reportRecordEntity.getState()),reportRecordEntity.getType());
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
    private List<ReportProgressVo> dealReportsState(Long entrustmentId){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ReportProgressVo> result = Lists.newArrayList();
        //TODO 兼容中间报告
//        ReportNodeVo reportRecordEntity = null;
//        Long id = recordEntityMapper.checkExist(entrustmentId,"0");
//        if (id == null){
//            reportRecordEntity = recordEntityMapper.getReportNodeByZjEntrustId(entrustmentId);
//        }else {
//            reportRecordEntity = recordEntityMapper.getReportNodeByEntrustId(entrustmentId);
//        }
        List<ReportNodeVo> reportNodes = recordEntityMapper.getReportNodesByEntrustId(entrustmentId);
        if(!CollectionUtils.isEmpty(reportNodes)){
            for (int i = 0; i < reportNodes.size(); i++) {
                ReportNodeVo reportRecordEntity = reportNodes.get(i);
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
                ReportProgressVo progressVo = new ReportProgressVo(reportRecordEntity.getReportCode(),Integer.parseInt(reportRecordEntity.getState()),reportRecordEntity.getType());
                List<ReportProgressStateVo> reportProgressStateList = Lists.newArrayList();
                for (int j = 0; j <=5 ; j++) {
                    if(j == 0){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("报告制作中");
                        vo.setTime(reportRecordEntity.getReportCompleteTime());
                        reportProgressStateList.add(vo);
                    }else if(j == 1){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("报告合成");
                        vo.setTime(reportRecordEntity.getCombineTime());
                        reportProgressStateList.add(vo);
                    }else if(j == 2){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("审核完成");
                        vo.setTime(reportRecordEntity.getVerifyerTime());
                        reportProgressStateList.add(vo);
                    }else if(j == 3){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("签发完成");
                        vo.setTime(reportRecordEntity.getIssuerTime());
                        reportProgressStateList.add(vo);
                    }else if(j == 4){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("盖章完成");
                        vo.setTime(reportRecordEntity.getSealTime());
                        reportProgressStateList.add(vo);
                    }else if(j == 5){
                        ReportProgressStateVo vo = new ReportProgressStateVo();
                        vo.setTitle("报告发出");
                        vo.setTime(reportRecordEntity.getOperateTime());
                        reportProgressStateList.add(vo);
                    }
                }
                progressVo.setReportProgressStateList(reportProgressStateList);
                result.add(progressVo);
            }
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
        // 通过委托单id 查询任务列表。委托单id不包含任务单 设置为 false。
        List<TaskTestEntity> taskList = entityMapper.selectTaskTestEntityList(entrustmentId);
        if(CollectionUtils.isEmpty(taskList)){
            entrustAddVo.setIsTaskList(false);
        }else{
//            entrustAddVo.setIsTaskList(true);
            // 获取任务单！=144 团队出具报告 进行赋值
            for(TaskTestEntity taskTestEntity : taskList){
                // 冒名顶替下 issue_report 转 receiver （类型转换）
                if(taskTestEntity.getState() !=144 && taskTestEntity.getReceiver().equals("是")){
                    entrustAddVo.setIsTaskList(true);
                }
            }
            // 没有被赋值 进行
            if(entrustAddVo.getIsTaskList() == null){
                entrustAddVo.setIsTaskList(false);
            }
        }
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
        List<SampleEntity> sampleCollection = Lists.newArrayList();
        sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        //暂存配合比下的的样品信息
        List<TestSampleEntity> nodeSample = Lists.newArrayList();
        // 样品信息 进行补充 检测依据集合，检测项集合
        for (SampleEntity sampleEntity : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            // 根据 委托单状态 进行选择项查询 0&&144 查询默认部门信息 state =1 查询所属指定部门信息
            if (entrustAddVo.getState() == 0 || entrustAddVo.getState() == 144) {
                List<JudgmentBasisVo> list = Lists.newArrayList();
                         list = sampleEntityMapper.getCheckItemNoDistribution(sampleEntity.getId(), entrustmentId);
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
                        // 委托单有：任务接收团队
                        if(entrustAddVo.getTeam()!=null){
                            List<TestTeam> testTeams = new ArrayList<>();
                            // 检测项下 团队pid == entrustAddVo.getTeam() && 唯一 则存储
                            if(!CollectionUtils.isEmpty(testingRoomInfoList)){
                                for(TestTeam testTeam : testingRoomInfoList){
                                    if(testTeam.getPid().equals(entrustAddVo.getTeam())){
                                        testTeams.add(testTeam);
                                    }
                                }
                            }
                            // 检测项 推荐团队 唯一则存储
                            if(!CollectionUtils.isEmpty(testTeams)&&testTeams.size()==1){
                                data.setRecommendTheTeam(testTeams.get(0).getId());
                            }
                            else {
                                data.setRecommendTheTeam(null);
                            }
                        }
                        allTestRoom.addAll(testingRoomList);
                    }
                }
                sampleEntity.setJudgmentBasisVos(list);
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
        entrustAddVo.setTaskProgressList(new ArrayList<>());
        return entrustAddVo;
    }

    @Override
    public List<LabelValueVo> getDept(Integer checkItemId) {
        return entityMapper.getDept(checkItemId);
    }

    @Override
    public EntrustAddVo getEntrustHistoryDetailTest(Long entrustmentId) {
        // 通过委托ID 委托单信息 → test_entrusted_info
        PageHelper.clearPage();
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustmentId);
        if (entrustAddVo.getSealType() != null) {
            entrustAddVo.setSealTypes(entrustAddVo.getSealType().split(","));
        }
        else {
            entrustAddVo.setSealTypes(new String[0]);
        }
        // 通过委托单id 获取缴费记录 依据id 同价价格
        PageHelper.clearPage();
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
        PageHelper.clearPage();
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
        PageHelper.clearPage();
        List<SampleEntity> sampleCollection = sampleEntityMapper.selectSampleListGroup(entrustmentId);
        // 处理信息 样品下检测项信息无价格不展示。
        for (SampleEntity sampleEntity0 : sampleCollection) {
            // 样品下 检测项、检测依据 补充。
            List<JudgmentBasisVo> listJson = Lists.newArrayList();
            PageHelper.clearPage();
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
            PageHelper.clearPage();
            standardList.addAll(sampleEntityMapper.getSampleBasisList(sampleEntity.getId(), entrustAddVo.getId()));
            sampleEntity.setStandardFileIdStr(standardList);
            //补充检测项可选的全部检测依据
            if (!CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVoStr())) {
                for (JudgmentBasisVo judgmentBasisVo : sampleEntity.getJudgmentBasisVoStr()) {
                    List<LabelValueVo> allCheckBasis = Lists.newArrayList();
                    PageHelper.clearPage();
                    allCheckBasis.addAll(testProductDao.getAllCheckBasis(judgmentBasisVo.getCheckItemId()));
                    judgmentBasisVo.setCheckBasisList(allCheckBasis);
                }
            }
            //补充产品可选的全部判定依据
            List<LabelValueVo> judges = Lists.newArrayList();
            PageHelper.clearPage();
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
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entity.getEntrustmentId());
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
            //根据委托单号月份确定任务单ID
            String teamCode = taskMapper.getTeamCode(deptId);
            String entrustmentNo = entrustAddVo.getEntrustmentNo()+"";
            String format = entrustmentNo.substring(2, 6);
            Integer integer = taskMapper.selectMaxNoByCode(teamCode+format);
            Integer code = null;
            if (integer == null) {
                String currentTime = DateUtil.getTodayString().substring(2, 6);
                code = Integer.parseInt(format + "001");
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
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            vo.setOrderer(userInfo.getName());
            vo.setPresentInformation(entity.getPresentInformation());
            if(!CollectionUtils.isEmpty(entity.getDeptIds())){
                if (entity.getDeptIds().contains(deptId)) {
                    vo.setIssueReport("是");
                } else {
                    vo.setIssueReport("否");
                }
            }
            // 任务单创建时间
            vo.setCreateTime(new Date());
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
            // 2023年9月26日 发布时：样品状态 = 待检
            // 通过委托单id 获取样品信息
            List<Integer> sampleIds = entityMapper.getSampleId(entity.getEntrustmentId());
            if (CollectionUtil.isNotEmpty(sampleIds)) {
                for (Integer sampleId : sampleIds) {
                    // 根据样品id 查询样品流转列表
                    List<SampleCirculationRecord> circulationList = sampleEntityMapper.getRecords(sampleId, 30);
                    if (CollectionUtils.isEmpty(circulationList)) {
                        // 增加样品样品流转状态
                        SampleCirculationRecord sa = new SampleCirculationRecord();
                        sa.setSampleId(sampleId);
                        sa.setStatus("0");
                        sa.setOperatorId(userInfo.getUserId());
                        sa.setOperatorName(userInfo.getName());
                        sa.setTime(new Date());
                        sampleEntityMapper.saveSampleCirculationRecord(sa);
                    } else {
                        Boolean flag = false;
                        for (SampleCirculationRecord sampleCirculationRecord : circulationList) {
                            if (sampleCirculationRecord.getStatus().equals("0")) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            // 增加样品样品流转状态
                            SampleCirculationRecord sa = new SampleCirculationRecord();
                            sa.setSampleId(sampleId);
                            sa.setStatus("0");
                            sa.setOperatorId(userInfo.getUserId());
                            sa.setOperatorName(userInfo.getName());
                            sa.setTime(new Date());
                            sampleEntityMapper.saveSampleCirculationRecord(sa);
                        }
                    }
                    }
                }
            // 记录发布任务单的日志
            StringBuffer stringBuilder1 = new StringBuffer();
            stringBuilder1.append("任务单发布日志");
            stringBuilder1.append("任务单id"+vo.getId());
            stringBuilder1.append("任务单号" + vo.getTaskCode());
            stringBuilder1.append("委托单id" + vo.getEntrustmentId());
            stringBuilder1.append("是否出具报告" + vo.getIssueReport());
            stringBuilder1.append("价格" + vo.getTaskPrice());
            stringBuilder1.append("任务单创建时间" + vo.getCreateTime());
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.TASK_FLOW, true);
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
                    rows.get(2).getTableCells().get(8).setText("№." + detail.getEntrustmentNostr());//委托编号 替换
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
                    rows.get(0).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getPresentInformation()) ? "——" : detail.getPresentInformation());//提供资料
                    rows.get(1).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getSamplingMethod()) ? "——" : detail.getSamplingMethod());//取样方式
                    rows.get(1).getTableCells().get(4).setText(StringUtils.isEmpty(detail.getCheckPurpose()) ? "——" : detail.getCheckPurpose());//检验目的
                    List<String> list = entityMapper.getSampleStandard(detail.getId());
                    StringBuilder stringBuilder = new StringBuilder();
                    if (!CollectionUtils.isEmpty(list)) {
                        for (String s : list) {
                            stringBuilder.append(s);
                            stringBuilder.append("，");
                        }
                        String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                        rows.get(1).getTableCells().get(6).setText(StringUtils.isEmpty(substring) ? "——" : substring);//产品标准 TODO 去重
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
                                        stringBuilder1.append("☐");
                                    }
                                }
                            }
                        }
                        if(stringBuilder1.length()>1){
                            String substring = stringBuilder1.toString().substring(0, stringBuilder1.length() - 1);
                            String[] split = substring.split("☐");
                            Set<String> set = new HashSet<>();
                            for (String s:split) {
                                set.add(s);
                            }
                            String substring1 = set.toString().substring(1, set.toString().length() - 1);
                            rows.get(2).getTableCells().get(2).setText( StringUtils.isEmpty(substring1) ? "——" : substring1);//检验项目及检测依据 TODO 去重
                        }
                    }
                    //TODO +1
                    rows.get(3).getTableCells().get(2).setText(detail.getReportCount().toString());//报告分数
                    rows.get(3).getTableCells().get(4).setText(StringUtils.isEmpty(detail.getReportType()) ? "——" : detail.getReportType());//取报告方式
                    rows.get(3).getTableCells().get(6).setText(StringUtils.isEmpty(detail.getReportReceivingUnit()) ? "——" : detail.getReportReceivingUnit());//收报告单位
                    rows.get(4).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getAddress()) ? "——" : detail.getAddress());//联系地址
                    rows.get(4).getTableCells().get(4).setText(StringUtils.isEmpty(detail.getAddressee()) ? "——" : detail.getAddressee());//联系人
                    rows.get(4).getTableCells().get(6).setText(StringUtils.isEmpty(detail.getMobile()) ? "——" : detail.getMobile());//联系方式
                    rows.get(5).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getEntrustPeople()) ? "——" : detail.getEntrustPeople());//委托人
                    rows.get(5).getTableCells().get(4).setText(StringUtils.isEmpty(detail.getEntrustPhone()) ? "——" : detail.getEntrustPhone());//委托人电话
                    rows.get(5).getTableCells().get(6).setText(StringUtils.isEmpty(detail.getWitnessPerson()) ? "——" : detail.getWitnessPerson());//见证人
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
                    if (stringBuilder2.toString().length()>=1){
                        rows.get(6).getTableCells().get(2).setText(stringBuilder2.toString().substring(0, stringBuilder2.length() - 1));//样品状态
                    }
                    rows.get(6).getTableCells().get(4).setText(detail.getIsSave());//样品保留
                    rows.get(7).getTableCells().get(2).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getActualPrice()) ? "——" : detail.getActualPrice());//检验收费
                    rows.get(7).getTableCells().get(4).setText(StringUtils.isEmpty(detail.getPaymentMethod()) ? "——" : detail.getPaymentMethod());//支付方式
                    //TODO 本次缴费统计缴费记录表
                    rows.get(7).getTableCells().get(6).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getPaymentRecordShow()) ? "——" : detail.getPaymentRecordShow());//本次交费
                    rows.get(8).getTableCells().get(2).setText(DateUtil.formatDate(detail.getRequestDate()));//完成期限
                    rows.get(8).getTableCells().get(4).setText( StringUtils.isEmpty(detail.getBusinessAcceptor()) ? "——" : detail.getBusinessAcceptor());//业务受理人
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
        // type = 1
        if(type !=null && type.equals(1)){
            HistoryEntrustDataVo historyEntrustDataVo = entityMapper.getContactWayData(name);
            if(!StringUtils.isEmpty(historyEntrustDataVo)){
                data.setAddress(historyEntrustDataVo.getAddress());
                data.setAddressee(historyEntrustDataVo.getAddressee());
                data.setMobile(historyEntrustDataVo.getMobile());
            }
            data.setReportReceivingUnit(entityMapper.getReportReceivingUnit(name));
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
        // 经营人员 = null
        entrustAddVo.setOperatingPersonnel(null);
        // 清除上传的 附件
        entrustAddVo.setFileArrays(new ArrayList<>());
        // 处理印章数组。
        if (entrustAddVo.getSealTypes() != null && entrustAddVo.getSealTypes().length > 0) {
            entrustAddVo.setSealTypes(entrustAddVo.getSealType().split(","));
        } else {
            String[] sealTypes = new String[0];
            entrustAddVo.setSealTypes(sealTypes);
        }
        // 业务受理人：=登录人
        entrustAddVo.setBusinessAcceptor(ShiroUtils.getUserInfo().getName());
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
    public synchronized String addEntrustCopy(EntrustAddVo vo, MultipartFile[] file) throws Exception {
        // 获取业务人员id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 样品编号变动 = true
        Boolean sampleStatus = false;
        // 获取前台得到的 vo.getId()
        long old = vo.getId();
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        long id = GenID.getID();
        basisInfo.setId(id);
        //获取并设置委托编号，相应的类别
        SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
        String acceptanceDate = yyyyMMddHH_NOT_.format(basisInfo.getAcceptanceDate()).substring(0,6);
        //获取并设置委托编号，相应的类别
        EntrustCategoryVo entrustCategoryVo = returnEntrustCategoryVo(vo.getEntrustCategory(),acceptanceDate);
        basisInfo.setEntrustmentNo(entrustCategoryVo.getEntrustmentNo());
        basisInfo.setEntrustCategory(entrustCategoryVo.getEntrustCategory());
        basisInfo.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
        // 通过委托编号 查询是否存在
        PageHelper.clearPage();
        if (entityMapper.getByDataEntrustMaxNo(basisInfo.getEntrustmentNo(),basisInfo.getEntrustCategoryType()) != null) {
            return "再来一单新增委托失败!:\t委托编号已存在\t"+basisInfo.getEntrustmentNo();
        }
        /**
         *  处理委托单位信息
         */
        TestCompanyVo companyClientVo = new TestCompanyVo();
        companyClientVo.setType(1);
        companyClientVo.setCompanyName(basisInfo.getEntrustCompany());
        companyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getEntrustPeople()) ? basisInfo.getEntrustPeople() : null);
        companyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getEntrustPhone()) ? basisInfo.getEntrustPhone() : null);
        /**
         *  使用方法处理委托单位信息
         */
        Integer entrustCompanyId = methodUnit(companyClientVo);
        basisInfo.setEntrustCompanyId(entrustCompanyId);
        //处理见证单位信息
        TestCompanyVo witnessCompanyClientVo = new TestCompanyVo();
        witnessCompanyClientVo.setType(2);
        witnessCompanyClientVo.setCompanyName(!StringUtils.isEmpty(basisInfo.getWitnessUint()) ? basisInfo.getWitnessUint() : null);
        witnessCompanyClientVo.setContacts(!StringUtils.isEmpty(basisInfo.getWitnessPerson()) ? basisInfo.getWitnessPerson() : null);
        witnessCompanyClientVo.setContactWay(!StringUtils.isEmpty(basisInfo.getWitnessPhone()) ? basisInfo.getWitnessPhone() : null);
        // 处理见证单位信息
        methodUnit(witnessCompanyClientVo);
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
                // 使用方法 处理样品来样时间 与委托单受理日期
                sampleStatus = methodAcceptanceDate(sampleEntity.getId(),vo.getAcceptanceDate(),sampleEntity2);
                // 委托单创建 更新样品状态 state 待检0
//                sampleEntity2.setState("0");
                // update样品信息
                sampleEntityMapper.updateByPrimaryKeySelective(sampleEntity2);
//                // 增加样品样品流转状态
//                SampleCirculationRecord sa = new SampleCirculationRecord();
//                sa.setSampleId(sampleEntity2.getId());
//                sa.setStatus("0");
//                sa.setOperatorId(userInfo.getUserId());
//                sa.setOperatorName(vo.getBusinessAcceptor());
//                sa.setTime(new Date());
//                sampleEntityMapper.saveSampleCirculationRecord(sa);
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
                    if(!CollectionUtils.isEmpty(ItemList)){
                        //记录日志
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("委托编号为\t"+basisInfo.getEntrustmentNo());
                        for(SampleItemEntity sampleItemEntity:sampleCheckItem){
                            stringBuilder.append("\t检测项名称为\t"+sampleItemEntity.getCheckItemName()+"\t单价为\t"+sampleItemEntity.getUnitPrice()+"\t检测样次\t"+sampleItemEntity.getTimes()
                                    +"\t检测项依据为\t"+sampleItemEntity.getStandardId());
                        }
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：委托-批量保存委托样品下检测项信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                        entityMapper.BatchSaveEntrustSampleItem(ItemList);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("委托编号"+basisInfo.getEntrustmentNo());
                for(EntrustSampleEntity entrustSampleEntity:list){
                    stringBuilder.append("\t样品id\t"+entrustSampleEntity.getSampleId());
                }
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：委托单与样品建立关系\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                entityMapper.BatchSaveEntrustSample(list);
            }
            if (!CollectionUtils.isEmpty(list1)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("委托编号"+basisInfo.getEntrustmentNo());
                for(EntrustSampleEntity entrustSampleEntity:list1){
                    stringBuilder.append("\t样品id\t"+entrustSampleEntity.getSampleId()+"\t样品委托依据\t"+entrustSampleEntity.getStandardId());
                }
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：委托单样品，判定依据信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                entityMapper.BatchSaveSampleStandard(list1);
            }
        }
        //更新委托单收费记录信息
        if (!StringUtils.isEmpty(vo.getPaymentRecord())) {
            EntrustPamentEntity pamentEntity = new EntrustPamentEntity();
            pamentEntity.setEntrustmentId(basisInfo.getId());
            pamentEntity.setTime(new Timestamp(new java.sql.Date(System.currentTimeMillis()).getTime()));
            pamentEntity.setPrice(vo.getPaymentRecord());
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "新增再来一单：委托缴费记录新增\t"+"\t委托编号\t"+basisInfo.getEntrustmentNo()+"\t委托单收费记录\t"+pamentEntity.getPrice(), Const.ENTRUST_FOUND, true);
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
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(userInfo.getUserId());
        // 委托单创建人所属部门
        if(StringUtils.isEmpty(department)){
            basisInfo.setDepartment(null);
        }
        else {
            basisInfo.setDepartment(department);
        }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), " 新增再来一单委托：信息成功\t委托编号为\t"+basisInfo.getEntrustmentNo()+"\t委托单位\t"+basisInfo.getEntrustCompany()
                    +"\t委托人\t"+basisInfo.getEntrustPeople()+ "\t要求完成时间\t"+(new Timestamp(basisInfo.getRequestDate().getTime()))+"\t委托检测类别\t"+basisInfo.getEntrustTestType()+"\t检测目的\t"+basisInfo.getCheckPurpose()
                    +"\t业务受理人\t"+basisInfo.getBusinessAcceptor()+"\t报告份数\t"+basisInfo.getReportCount()+"\t受理日期\t"+(new Timestamp(basisInfo.getAcceptanceDate().getTime()))
                    +"\t任务来源\t"+basisInfo.getTaskSource()+"\t实收价格\t"+basisInfo.getActualPrice()+"\t应收价格\t"+basisInfo.getSystemPrice()+"\t折扣率\t"+basisInfo.getDiscount(), Const.ENTRUST_FOUND, true);
        basisInfo.setAuditState("1");
        // 判断取报告方式 非邮寄的话 清空状态
        if(!StringUtils.isEmpty(basisInfo.getReportType()) && !basisInfo.getReportType().equals("邮寄")){
            basisInfo.setAddress(null);
            basisInfo.setMobile(null);
            basisInfo.setAddressee(null);
            basisInfo.setReportReceivingUnit(null);
        }
        basisInfo.setCreateTime(new Date());
        // 委托单是否留样1.保留2.废弃 默认：否
        basisInfo.setIsSave("否");
        // 经营人员
        basisInfo.setOperatingPersonnel(vo.getOperatingPersonnel());
        entityMapper.insertEntrustInfo(basisInfo);
        if(sampleStatus){
            return "新建委托成功\n"+"委托与样品时间不一致，样品编号及签收时间发生变动";
        }
        return "新建委托成功";
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
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
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
                                    // 再来一单时：样品新增时 新增流转SQL： 状态 = 5 收样。
                                    // 增加样品样品流转状态
                                    SampleCirculationRecord sa = new SampleCirculationRecord();
                                    sa.setSampleId(sampleEntity.getId());
                                    sa.setStatus("5");
                                    sa.setOperatorId(userInfo.getUserId());
                                    sa.setOperatorName(userInfo.getName());
                                    sa.setTime(new Date());
                                    sampleEntityMapper.saveSampleCirculationRecord(sa);
                                    // 更新样品信息 state = 5 收样
                                    sampleEntityMapper.updateSampleState(sampleEntity.getId(),5);
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
                if(!StringUtils.isEmpty(upload)){
                    String[] fileUrls = upload.split("\\?");
                    stringBuilder.append(fileUrls[0]);
                }
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
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("委托单id为："+entrustFileTableEntity.getEntrustId());
        stringBuilder1.append("文件附件链接："+entrustFileTableEntity.getFileUrl());
        stringBuilder1.append("文件附件名称:"+entrustFileTableEntity.getFileUrlStr());
        //增加日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "委托附件新增\t"+stringBuilder1.toString(), Const.ENTRUST_file, true);
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
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("委托单id为："+entrustFileTableEntity.getEntrustId());
        stringBuilder1.append("文件附件链接："+entrustFileTableEntity.getFileUrl());
        stringBuilder1.append("文件附件名称:"+entrustFileTableEntity.getFileUrlStr());
        //增加日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "委托附件删除\t"+stringBuilder1.toString(), Const.ENTRUST_file, true);
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
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("任务单编号 = "+taskTestEntity.getCode());
                stringBuilder.append("原任务单完成时间 = ");
                if(taskTestEntity.getRequiredCompletionTime()!=null){
                    stringBuilder.append(new Timestamp(taskTestEntity.getRequiredCompletionTime().getTime()));
                }
                stringBuilder.append("原任务单下单日期 = ");
                if(taskTestEntity.getOrderTime()!=null){
                    stringBuilder.append(new Timestamp(taskTestEntity.getOrderTime().getTime()));
                }
                stringBuilder.append("原任务提供资料 = "+taskTestEntity.getPresentInformation());
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
                stringBuilder.append("变更后任务单完成时间 = ");
                if(taskTestEntity.getRequiredCompletionTime()!=null){
                    stringBuilder.append(new Timestamp(taskTestEntity.getRequiredCompletionTime().getTime()));
                }
                stringBuilder.append("变更后任务单下单日期 = ");
                if(taskTestEntity.getOrderTime()!=null){
                    stringBuilder.append(new Timestamp(taskTestEntity.getOrderTime().getTime()));
                }
                stringBuilder.append("变更后任务提供资料 = "+taskTestEntity.getPresentInformation());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "更新委托：任务单变更\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
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
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("样品编号"+sampleData.getSampleCode());
                stringBuilder.append("原样品委托单位"+sampleData.getCompanyId());
                sampleData.setCompanyId(vo.getEntrustCompanyId());
                stringBuilder.append("变更后样品委托单位"+sampleData.getCompanyId());
                // update样品信息
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "更新委托：样品信息\t"+stringBuilder.toString(), Const.ENTRUST_FOUND, true);
                sampleEntityMapper.updateByPrimaryKeySelective(sampleData);
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
        for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity:taskRelEntities) {
            // 处理信息 部门id&部门名称 获取为 部门ID
            if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getDepartment())){
                String[] deptIds = testEntrustedTaskRelEntity.getDepartment().split("&");
                testEntrustedTaskRelEntity.setDeptId(Integer.parseInt(deptIds[0]));
            }
            for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity1 :testEntrustedTaskRelEntityList) {
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
            //设置中间报告的完成状态
            if(testEntrustedTaskRelEntity.getType().equals(1)){
                testEntrustedTaskRelEntity.setState(0);
            }
        }
        /**
         *  增加日志
         */
        if(!CollectionUtils.isEmpty(taskRelEntities)){
            StringBuilder stringBuilder1 = new StringBuilder();
            for (TestEntrustedTaskRelEntity testEntrustedTaskRelEntity : taskRelEntities){
                stringBuilder1.append("新增任务流转：委托单id:"+testEntrustedTaskRelEntity.getEntrustId()+"流转日期：");
                if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getTaskFlowDate())){
                    stringBuilder1.append(new Timestamp(testEntrustedTaskRelEntity.getTaskFlowDate().getTime()));
                }
                stringBuilder1.append("备注："+testEntrustedTaskRelEntity.getRemark()+"报告类型：");
                if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getType())){
                    if(testEntrustedTaskRelEntity.getType().equals(1)){
                        stringBuilder1.append("中间报告");
                    }
                    if(testEntrustedTaskRelEntity.getType().equals(0)){
                        stringBuilder1.append("最终报告");
                    }
                }
                stringBuilder1.append("任务单id："+testEntrustedTaskRelEntity.getTaskId());
                stringBuilder1.append("部门信息："+testEntrustedTaskRelEntity.getDepartment());
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.TASK_FLOW, true);
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
        // 查询任务流转详情
        TestEntrustedTaskRelVo testEntrustedTaskRelVo = testEntrustedTaskRelDao.getTaskFlowById(testEntrustedTaskRelEntity.getId());
        StringBuilder stringBuilder1 = new StringBuilder();
        // 变更前：
        stringBuilder1.append("修改任务流转前：id:"+testEntrustedTaskRelVo.getId()+"流转日期：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getTaskFlowDate())){
            stringBuilder1.append(new Timestamp(testEntrustedTaskRelVo.getTaskFlowDate().getTime()));
        }
        stringBuilder1.append("备注："+testEntrustedTaskRelVo.getRemark()+"报告类型：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getType())){
            if(testEntrustedTaskRelVo.getType().equals(1)){
                stringBuilder1.append("中间报告");
            }
            if(testEntrustedTaskRelVo.getType().equals(0)){
                stringBuilder1.append("最终报告");
            }
        }
        // 变更后：
        stringBuilder1.append("\n修改任务流转后：id:"+testEntrustedTaskRelEntity.getId()+"流转日期：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getTaskFlowDate())){
            stringBuilder1.append(new Timestamp(testEntrustedTaskRelEntity.getTaskFlowDate().getTime()));
        }
        stringBuilder1.append("备注："+testEntrustedTaskRelEntity.getRemark()+"报告类型：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getType())){
            if(testEntrustedTaskRelEntity.getType().equals(1)){
                stringBuilder1.append("中间报告");
            }
            if(testEntrustedTaskRelEntity.getType().equals(0)){
                stringBuilder1.append("最终报告");
            }
        }
        //增加日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "修改任务流转\n"+stringBuilder1.toString(), Const.TASK_FLOW, true);
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
        // 查询任务流转详情
        TestEntrustedTaskRelVo testEntrustedTaskRelVo = testEntrustedTaskRelDao.getTaskFlowById(id);
        StringBuilder stringBuilder1 = new StringBuilder();
        // 变更前：
        stringBuilder1.append("删除任务流转详情：id:"+testEntrustedTaskRelVo.getId()+"流转日期：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getTaskFlowDate())){
            stringBuilder1.append(new Timestamp(testEntrustedTaskRelVo.getTaskFlowDate().getTime()));
        }
        stringBuilder1.append("备注："+testEntrustedTaskRelVo.getRemark()+"报告类型：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getType())){
            if(testEntrustedTaskRelVo.getType().equals(1)){
                stringBuilder1.append("中间报告");
            }
            if(testEntrustedTaskRelVo.getType().equals(0)){
                stringBuilder1.append("最终报告");
            }
        }
        //增加日志
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.ENTRUST_file, true);
        //删除任务流转时同步删除未完成的中间报告数据
        if(testEntrustedTaskRelVo.getRecordId() != null){
            //删除报告信息
            recordEntityMapper.deleteByPrimaryKey(testEntrustedTaskRelVo.getRecordId());
            //删除检测项信息
            reportRecordDetailEntityMapper.deleteByRecordId(testEntrustedTaskRelVo.getRecordId());
        }
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
        //设置中间报告任务流转状态（0，未完成；1，已完成）
        if(testEntrustedTaskRelEntity.getType() == 1){
            testEntrustedTaskRelEntity.setState(0);
        }
        /**
         *  增加日志
         */
        StringBuilder stringBuilder1 = new StringBuilder();
        // 变更前：
        stringBuilder1.append("新增任务流转：委托单id:"+testEntrustedTaskRelEntity.getEntrustId()+"流转日期：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getTaskFlowDate())){
            stringBuilder1.append(new Timestamp(testEntrustedTaskRelEntity.getTaskFlowDate().getTime()));
        }
        stringBuilder1.append("备注："+testEntrustedTaskRelEntity.getRemark()+"报告类型：");
        if(!StringUtils.isEmpty(testEntrustedTaskRelEntity.getType())){
            if(testEntrustedTaskRelEntity.getType().equals(1)){
                stringBuilder1.append("中间报告");
            }
            if(testEntrustedTaskRelEntity.getType().equals(0)){
                stringBuilder1.append("最终报告");
            }
        }
        stringBuilder1.append("任务单id："+testEntrustedTaskRelEntity.getTaskId());
        stringBuilder1.append("部门信息："+testEntrustedTaskRelEntity.getDepartment());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.TASK_FLOW, true);
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
        //拆分委托编号
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getEntrustmentNostr())){
            EntrustCategoryVo entrustCategoryVo = EntrustNoStrUtils.splitEntrustNo(testEntrustedTaskRelVo.getEntrustmentNostr());
            testEntrustedTaskRelVo.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            testEntrustedTaskRelVo.setEntrustNo(entrustCategoryVo.getEntrustmentNo().toString());
            if(!StringUtils.isEmpty(entrustCategoryVo.getEntrustmentNo())){
                testEntrustedTaskRelVo.setEntrustNo(entrustCategoryVo.getEntrustmentNo().toString());
            }
        }
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
                // 任务流转 type =1 && 中间报告 =1 相等
                if(testEntrustedTaskRelVo1.getType().equals(testEntrustedTaskRelVo1.getMidReportType())){
                    // if 中间报告==空
                    if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getMidReportCode())){
                        testEntrustedTaskRelVo1.setReportCode("--");
                    }
                    else {
                        testEntrustedTaskRelVo1.setReportCode(testEntrustedTaskRelVo1.getMidReportCode());
                    }
                    // if 中间报告结束时间 ！= 空
                    if(!StringUtils.isEmpty(testEntrustedTaskRelVo1.getMidReportFinishTime()))
                    {
                        testEntrustedTaskRelVo1.setReportFinishTime(testEntrustedTaskRelVo1.getMidReportFinishTime());
                    }
                    else {
                        testEntrustedTaskRelVo1.setReportFinishTime(null);
                    }
                }
            }
        }
        Integer pageNum = testEntrustedTaskRelVo.getPageNum();
        Integer pageSize = testEntrustedTaskRelVo.getPageSize();
        if(StringUtils.isEmpty(pageNum)||pageNum<=0){
            pageNum = 1;
        }
        if(StringUtils.isEmpty(pageSize)||pageSize<=0){
            pageSize = 10;
        }
        // 如果页码展示数量大于最大数 返回最大数值
        if(pageSize>list.size()){
            pageSize = list.size();
        }
        PageInfo pageInfo = new PageInfo();
        //分页
        List<TestEntrustedTaskRelVo> subList;
        if (list.size() > 10 && list.size() / 10 >= pageNum) {
            subList = list.subList((pageNum - 1) * pageSize, pageNum * pageSize);
        } else {
            subList = list.subList((pageNum - 1) * pageSize, list.size());
        }
        getMethodSampleName(subList);
        pageInfo.setList(subList);
        pageInfo.setTotal(list.size());
        return pageInfo;
    }

    @Override
    public PageInfo getClientListExport(ClientOrderdetailVo clientOrderdetailVo) {
        List<ClientOrderdetailVo> list = Lists.newArrayList();
        PageHelper.clearPage();
        clientOrderdetailVo.setCompanyIds(null);
        if(clientOrderdetailVo.getCompanyStrs()!=null&&clientOrderdetailVo.getCompanyStrs().length==0){
            clientOrderdetailVo.setCompanyStrs(null);
        }
        list = entityMapper.selectClientOrderdetailVoList(clientOrderdetailVo);
        if(!CollectionUtils.isEmpty(list)){
            for(ClientOrderdetailVo clientOrderdetailVo1 :list){
                if(StringUtils.isEmpty(clientOrderdetailVo1.getProjectName())){
                    clientOrderdetailVo1.setProjectName("--");
                }
                if(StringUtils.isEmpty(clientOrderdetailVo1.getProjectPart())){
                    clientOrderdetailVo1.setProjectPart("--");
                }
                if(StringUtils.isEmpty(clientOrderdetailVo1.getOperatingPersonnel())){
                    clientOrderdetailVo1.setOperatingPersonnel("--");
                }
                HashSet<String> SampleNameSet = new HashSet<>();
                HashSet<String> SpecsSet = new HashSet<>();
                HashSet<String> BatchNumberSet = new HashSet<>();
                HashSet<String> CheckItemNameSet = new HashSet<>();
                // 处理样品信息及检测项信息
                if(!CollectionUtils.isEmpty(clientOrderdetailVo1.getSamples())){
                    for(SampleEntity sampleEntity :clientOrderdetailVo1.getSamples()){
                        if(!StringUtils.isEmpty(sampleEntity.getSampleName())){
                            SampleNameSet.add(sampleEntity.getSampleName());
                        }
                        if(!StringUtils.isEmpty(sampleEntity.getSpecs())){
                            SpecsSet.add(sampleEntity.getSpecs());
                        }
                        if(!StringUtils.isEmpty(sampleEntity.getBatchNumber())){
                            BatchNumberSet.add(sampleEntity.getBatchNumber());
                        }
                        if(!CollectionUtils.isEmpty(sampleEntity.getSampleCheckItem())){
                            for(SampleItemEntity sampleItemEntity :sampleEntity.getSampleCheckItem())
                            {
                                CheckItemNameSet.add(sampleItemEntity.getCheckItemName());
                            }
                        }
                    }
                }
                StringBuilder SampleNameB = new StringBuilder();
                for(String SampleName:SampleNameSet){
                    SampleNameB.append(SampleName);
                    SampleNameB.append("、");
                }
                StringBuilder SpecsB = new StringBuilder();
                for(String specs:SpecsSet){
                    SpecsB.append(specs);
                    SpecsB.append("、");
                }
                StringBuilder BatchNumberB = new StringBuilder();
                for(String BatchNumber:BatchNumberSet){
                    BatchNumberB.append(BatchNumber);
                    BatchNumberB.append("、");
                }
                StringBuilder CheckItemNameB = new StringBuilder();
                for(String CheckItemName:CheckItemNameSet){
                    CheckItemNameB.append(CheckItemName);
                    CheckItemNameB.append("、");
                }
                if(SampleNameB.length()>1){
                    clientOrderdetailVo1.setSampleName(SampleNameB.deleteCharAt(SampleNameB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setSampleName("--");
                }
                if(SpecsB.length()>1){
                    clientOrderdetailVo1.setSpecs(SpecsB.deleteCharAt(SpecsB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setSpecs("--");
                }
                if(BatchNumberB.length()>1){
                    clientOrderdetailVo1.setBatchNumber(BatchNumberB.deleteCharAt(BatchNumberB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setBatchNumber("--");
                }
                if(CheckItemNameB.length()>1){
                    clientOrderdetailVo1.setCheckItemName(CheckItemNameB.deleteCharAt(CheckItemNameB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setCheckItemName("--");
                }
                // 处理任务单信息。
                StringBuilder taskCodeB = new StringBuilder();
                if(!CollectionUtils.isEmpty(clientOrderdetailVo1.getTaskEntities())){
                    for(TaskEntity taskEntity : clientOrderdetailVo1.getTaskEntities()){
                        taskCodeB.append(taskEntity.getCode());
                        taskCodeB.append("、");
                    }
                }
                // 报告编号 和 发出日期
                StringBuilder reportCodeB = new StringBuilder();
                StringBuilder reportTimeB = new StringBuilder();
                if(!CollectionUtils.isEmpty(clientOrderdetailVo1.getReportRecordEntities())){
                    for(ReportRecordEntity reportRecordEntity : clientOrderdetailVo1.getReportRecordEntities()){
                        reportCodeB.append(reportRecordEntity.getReportCode());
                        reportCodeB.append("、");
                        if(reportRecordEntity.getReportTime()!=null){
                        reportTimeB.append(DateUtil.formatDate(reportRecordEntity.getReportTime()));
                        reportTimeB.append("、");
                      }
                    }
                }
                if(reportCodeB.length()>1){
                    clientOrderdetailVo1.setReportCode(reportCodeB.deleteCharAt(reportCodeB.length()-1).toString());
                }else {
                    clientOrderdetailVo1.setReportCode("--");
                }
                if(reportTimeB.length()>1){
                    clientOrderdetailVo1.setReportTime(reportTimeB.deleteCharAt(reportTimeB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setReportTime("--");
                }
                if(taskCodeB.length()>1){
                    clientOrderdetailVo1.setTaskCode(taskCodeB.deleteCharAt(taskCodeB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setTaskCode("--");
                }
            }
        }
            Integer pageNum = clientOrderdetailVo.getPageNum();
            Integer pageSize = clientOrderdetailVo.getPageSize();
            if(StringUtils.isEmpty(pageNum)||pageNum<=0){
                pageNum = 1;
            }
            if(StringUtils.isEmpty(pageSize)||pageSize<=0){
                pageSize = 10;
            }
            // 如果页码展示数量大于最大数 返回最大数值
            if(pageSize>list.size()){
                pageSize = list.size();
            }
            PageInfo pageInfo = new PageInfo();
            //分页
            List<ClientOrderdetailVo> subList;
            if (list.size() > 10 && list.size() / 10 >= pageNum) {
                subList = list.subList((pageNum - 1) * pageSize, pageNum * pageSize);
            } else {
                subList = list.subList((pageNum - 1) * pageSize, list.size());
            }
            pageInfo.setList(subList);
            pageInfo.setTotal(list.size());
            return pageInfo;
    }



    @Override
    public InputStream exportPersonDetails(List<ClientOrderdetailVo> list,ClientOrderdetailVo clientOrderdetailVo) throws Exception {
        InputStream fileStream = MinIoUtil.getFileStream("entrust-template", "客户委托详情表.xlsx");
        PDFHelper3.getLicense();
        Workbook workbook = new Workbook(fileStream);
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        // 第一行 标题
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder deptBuilder = new StringBuilder();
        if(clientOrderdetailVo.getCompanyStrs()!=null&&clientOrderdetailVo.getCompanyStrs().length>0){
            for(int i=0; i < clientOrderdetailVo.getCompanyStrs().length;i++){
                deptBuilder.append(clientOrderdetailVo.getCompanyStrs()[i]);
                deptBuilder.append("、");
            }
        }
        if(deptBuilder.length()>0){
            deptBuilder = deptBuilder.deleteCharAt(deptBuilder.length()-1);
        }
        deptBuilder.append("委托详情表");
        if(clientOrderdetailVo.getAcceptancebeganDate()!=null&&clientOrderdetailVo.getAcceptanceoverDate()!=null){
            String Begandate = formatter.format(clientOrderdetailVo.getAcceptancebeganDate());
            String Overdate = formatter.format(clientOrderdetailVo.getAcceptanceoverDate());
            deptBuilder.append(Begandate+"~"+Overdate);
        }
        cells.get("A1").setValue(deptBuilder.toString());
        Integer n = 3;
        String row = "A";
        ReportServiceImpl letterCycle = new ReportServiceImpl();
        for (int i = 0; i < list.size(); i++) {
            ClientOrderdetailVo personVo = list.get(i);
            //在sheet里创建第三行
            cells.get(row+n).setValue(personVo.getEntrustmentNostr());
            row = letterCycle.getNextUpEn(row);
            if (personVo.getAcceptanceDate() != null) {
                String dateString = formatter.format(personVo.getAcceptanceDate());
                cells.get(row+n).setValue(dateString);
            }
            row = letterCycle.getNextUpEn(row);
            if (personVo.getRequestDate() != null) {
                String dateString = formatter.format(personVo.getRequestDate());
                cells.get(row+n).setValue(dateString);
            }
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getEntrustPeople());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getOperatingPersonnel());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getProjectName());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getProjectPart());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getSampleName());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getSpecs());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getBatchNumber());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getCheckItemName());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getSystemPrice());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getTaskCode());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getReportCode());
            row = letterCycle.getNextUpEn(row);
            cells.get(row+n).setValue(personVo.getReportTime());
            row = "A";
            n++;
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.save(qiYueSuoEntity.getAutographPath()+"sealList.xlsx");
        File file = new File(qiYueSuoEntity.getAutographPath()+"sealList.xlsx");
        byte[] bytes = FileAndFolderUtil.file2byte(file);
        os.write(bytes);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }
    /**
     * 处理样品来样时间 与委托单受理日期
     * @param Id 样品id
     * @param AcceptanceDate 委托单受理日期
     * @param sampleData 样品update 数据
     */
    private Boolean methodAcceptanceDate(Integer Id,Date AcceptanceDate,SampleEntity sampleData){
        // 获取样品详情
        PageHelper.clearPage();
        TemplateSampleVo sampleEntityData  = sampleEntityMapper.getOriginalSampleInfo(Id);
        Debug.println("新增委托日志数据输出：根据样品id 获取详情\t",Id+" 详情 "+sampleEntityData);
        sampleData.setId(Id);
        sampleData.setIsUse(1);
        sampleData.setReceivedDate(sampleEntityData.getSampleTime());
        // 比较样品签收时间 < 委托单受理日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // true：样品签收时间改动
        Boolean status = false;
        try {
//            // 测试此日期是否在指定日期之后.时间不平等
//            if (!AcceptanceDate.after(date1)&&!AcceptanceDate.equals(date1)) {
//                // 签收时间 =委托单受理日期
//                sampleData.setReceivedDate(sdf.format(AcceptanceDate));
//                status = true;
//            }
            // 判断时间一致直接返回
            if(sampleData.getReceivedDate().equals(sdf.format(AcceptanceDate))){
                return false;
            }
            //ps： 更改为 样品签收时间 =委托单受理日期
                sampleData.setReceivedDate(sdf.format(AcceptanceDate));
                status = true;
        }
        catch (Exception e){
            Debug.println("新增委托日志异常输出:\t",e+"  update样品状态时");
        }
        if(status){
            SampleEntity sampleData1 = new SampleEntity();
            sampleData1.setId(sampleData.getId());
            sampleData1.setReceivedDate(sampleData.getReceivedDate());
            // 处理原材样品编号
            sampleData1.setSampleCode(methodSampleCode(sampleEntityData.getSampleNumber(),sampleData.getReceivedDate()));
            // update样品信息
            sampleEntityMapper.updateByPrimaryKeySelective(sampleData1);
            // 判断样品类别 处理配合比信息 进行同步时间。
            if(!sampleEntityData.getSampleType().equals("原材")){
                // 获取配合比信息：
                List<SampleDetailVo> sampleTagInfoPidList = Lists.newArrayList();
                sampleTagInfoPidList = sampleEntityMapper.getSampleTagInfoPidList(Id);
                if(!CollectionUtils.isEmpty(sampleTagInfoPidList)){
                    // 进行遍历塞配合比收样时间数值。
                    for(SampleDetailVo sampleDetailVo1 :sampleTagInfoPidList){
                        SampleEntity sampleData2 = new SampleEntity();
                        sampleData2.setId(sampleDetailVo1.getId());
                        sampleData2.setReceivedDate(sampleData.getReceivedDate());
                        if(sampleData1.getSampleCode()!=null){
                         // 处理配合比则 更改样品编号
                         sampleData2.setSampleCode(methodMixProportionSampleCode(sampleData1.getSampleCode(),sampleDetailVo1.getSampleCode()));
                        }
                        // update样品信息
                        sampleEntityMapper.updateByPrimaryKeySelective(sampleData2);
                    }
                }
            }
            // 记录日志
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "处理样品来样时间与委托单受理日期:\t" +
                    "委托单受理日期为:"+(new Timestamp(AcceptanceDate.getTime()))+
                    "\t样品编号"+sampleEntityData.getSampleNumber()+"\t样品来样时间为\t" +sampleEntityData.getSampleTime() +
                    "\t变更后样品来样时间为\t"+sampleData.getReceivedDate(), Const.ENTRUST_FOUND, true);
            return true;
        }
        return false;
    }
    @Override
    public PageInfo getClientList(ClientOrderdetailVo clientOrderdetailVo) {
        List<ClientOrderdetailVo> list = Lists.newArrayList();
        PageHelper.clearPage();
        clientOrderdetailVo.setCompanyIds(null);
        if(clientOrderdetailVo.getCompanyStrs()!=null&&clientOrderdetailVo.getCompanyStrs().length==0){
            clientOrderdetailVo.setCompanyStrs(null);
        }
        list = entityMapper.getEntrustList(clientOrderdetailVo);
        // 处理分页数据：
        Integer pageNum = clientOrderdetailVo.getPageNum();
        Integer pageSize = clientOrderdetailVo.getPageSize();
        if(StringUtils.isEmpty(pageNum)||pageNum<=0){
            pageNum = 1;
        }
        if(StringUtils.isEmpty(pageSize)||pageSize<=0){
            pageSize = 10;
        }
        PageInfo pageInfo = new PageInfo();
        //分页
        List<ClientOrderdetailVo> subList;
        if (list.size() > 10 && list.size() / 10 >= pageNum) {
            subList = list.subList((pageNum - 1) * pageSize, pageNum * pageSize);
        } else {
            subList = list.subList((pageNum - 1) * pageSize, list.size());
        }
        pageInfo.setList(subList);
        pageInfo.setTotal(list.size());
        // 处理List信息：
        if(!CollectionUtils.isEmpty(subList)){
            // 根据委托单主键条件进行搜索
            List<Long> entrustIds = new ArrayList<>();
            for(ClientOrderdetailVo clientOrderdetailVo0 :subList){
                if(StringUtils.isEmpty(clientOrderdetailVo0.getProjectName())){
                    clientOrderdetailVo0.setProjectName("--");
                }
                if(StringUtils.isEmpty(clientOrderdetailVo0.getProjectPart())){
                    clientOrderdetailVo0.setProjectPart("--");
                }
                if(StringUtils.isEmpty(clientOrderdetailVo0.getOperatingPersonnel())){
                    clientOrderdetailVo0.setOperatingPersonnel("--");
                }
                entrustIds.add(clientOrderdetailVo0.getEntrustmentId());
            }
            // 根据委托单id 获取 样品信息
            List<SampleEntity> sampleList = entityMapper.getSampleList(entrustIds);
            // 获取检测项列表
            List<SampleItemEntity> itemList  = entityMapper.getSampleItemList(entrustIds);
            // 获取任务单列表
            List<TaskEntity> taskList = entityMapper.getTaskList(entrustIds);
            // 获取报告列表
            List<ReportRecordEntity> reportList = entityMapper.getReportRecordList(entrustIds);
            for(ClientOrderdetailVo clientOrderdetailVo1 :subList){

                HashSet<String> SampleNameSet = new HashSet<>();
                HashSet<String> SpecsSet = new HashSet<>();
                HashSet<String> BatchNumberSet = new HashSet<>();
                HashSet<String> CheckItemNameSet = new HashSet<>();
                if(!CollectionUtils.isEmpty(sampleList)){
                    for(SampleEntity sampleEntity :sampleList){
                       if(sampleEntity.getEntrustId().equals(clientOrderdetailVo1.getEntrustmentId())){
                           if(!StringUtils.isEmpty(sampleEntity.getSampleName())){
                               SampleNameSet.add(sampleEntity.getSampleName());
                           }
                           if(!StringUtils.isEmpty(sampleEntity.getSpecs())){
                               SpecsSet.add(sampleEntity.getSpecs());
                           }
                           if(!StringUtils.isEmpty(sampleEntity.getBatchNumber())){
                               BatchNumberSet.add(sampleEntity.getBatchNumber());
                           }
                       }
                    }
                }
                // 处理检测项信息：
                if(!CollectionUtils.isEmpty(itemList)){
                    for(SampleItemEntity sampleItemEntity :itemList)
                    {
                        if(sampleItemEntity.getEntrustId()!=null&&sampleItemEntity.getEntrustId().equals(clientOrderdetailVo1.getEntrustmentId())){
                            CheckItemNameSet.add(sampleItemEntity.getCheckItemName());
                        }
                    }
                }
                StringBuilder SampleNameB = new StringBuilder();
                for(String SampleName:SampleNameSet){
                    SampleNameB.append(SampleName);
                    SampleNameB.append("、");
                }
                StringBuilder SpecsB = new StringBuilder();
                for(String specs:SpecsSet){
                    SpecsB.append(specs);
                    SpecsB.append("、");
                }
                StringBuilder BatchNumberB = new StringBuilder();
                for(String BatchNumber:BatchNumberSet){
                    BatchNumberB.append(BatchNumber);
                    BatchNumberB.append("、");
                }
                StringBuilder CheckItemNameB = new StringBuilder();
                for(String CheckItemName:CheckItemNameSet){
                    CheckItemNameB.append(CheckItemName);
                    CheckItemNameB.append("、");
                }
                if(SampleNameB.length()>1){
                    clientOrderdetailVo1.setSampleName(SampleNameB.deleteCharAt(SampleNameB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setSampleName("--");
                }
                if(SpecsB.length()>1){
                    clientOrderdetailVo1.setSpecs(SpecsB.deleteCharAt(SpecsB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setSpecs("--");
                }
                if(BatchNumberB.length()>1){
                    clientOrderdetailVo1.setBatchNumber(BatchNumberB.deleteCharAt(BatchNumberB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setBatchNumber("--");
                }
                if(CheckItemNameB.length()>1){
                    clientOrderdetailVo1.setCheckItemName(CheckItemNameB.deleteCharAt(CheckItemNameB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setCheckItemName("--");
                }
                // 处理任务单信息。
                StringBuilder taskCodeB = new StringBuilder();
                if(!CollectionUtils.isEmpty(taskList)){
                    for(TaskEntity taskEntity : taskList){
                        if(taskEntity.getEntrustmentId()!=null&&taskEntity.getEntrustmentId().equals(clientOrderdetailVo1.getEntrustmentId())){
                        taskCodeB.append(taskEntity.getCode());
                        taskCodeB.append("、");
                        }
                    }
                }
                // 报告编号 和 发出日期
                StringBuilder reportCodeB = new StringBuilder();
                StringBuilder reportTimeB = new StringBuilder();
                if(!CollectionUtils.isEmpty(reportList)){
                    for(ReportRecordEntity reportRecordEntity : reportList){
                        if(reportRecordEntity.getEntrustId()!=null&&reportRecordEntity.getEntrustId().equals(clientOrderdetailVo1.getEntrustmentId())){
                            if(reportRecordEntity.getReportCode()!=null){
                                reportCodeB.append(reportRecordEntity.getReportCode());
                                reportCodeB.append("、");
                            }
                            if(reportRecordEntity.getReportTime()!=null){
                                reportTimeB.append(DateUtil.formatDate(reportRecordEntity.getReportTime()));
                                reportTimeB.append("、");
                            }
                        }
                    }
                }
                if(reportCodeB.length()>1){
                    clientOrderdetailVo1.setReportCode(reportCodeB.deleteCharAt(reportCodeB.length()-1).toString());
                }else {
                    clientOrderdetailVo1.setReportCode("--");
                }
                if(reportTimeB.length()>1){
                    clientOrderdetailVo1.setReportTime(reportTimeB.deleteCharAt(reportTimeB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setReportTime("--");
                }
                if(taskCodeB.length()>1){
                    clientOrderdetailVo1.setTaskCode(taskCodeB.deleteCharAt(taskCodeB.length()-1).toString());
                }
                else {
                    clientOrderdetailVo1.setTaskCode("--");
                }
            }
        }
        return pageInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptEntrust(Long id) {
        SysUserEntity user = ShiroUtils.getUserInfo();
        String username = user.getUsername();
        String name = user.getName();
        // 处理样品信息
        List<Integer> sampleIds = entityMapper.getAllSampleIdentrustmentId(id);
        if(!CollectionUtils.isEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                // 更新样品状态：
                TestSampleEntity record = new TestSampleEntity();
                record.setId(sampleId);
                record.setInspector(name);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    record.setReceivedDate(sdf.format(new Date()));
                } catch (Exception e) {
                    Debug.println("受理时:\t", e + "  update样品状态时");
                }
                testSampleEntityMapper.updateByPrimaryKeySelective(record);
            }
        }
        // 受理委托单信息
        EntrustEntity basisInfo = new EntrustEntity();
        // 委托单已经 审核
        basisInfo.setAuditState("1");
        basisInfo.setAuditUser(username);
        basisInfo.setId(id);
        // 业务受理人
        basisInfo.setBusinessAcceptor(name);
        // 是否留样 ： 否
        basisInfo.setIsSave("否");
        // 委托检测类别（原材检测 配合比）
        basisInfo.setEntrustTestType("原材检测");
        Long department = teamMapper.getTeamIdByUid(user.getUserId());
        // 委托单创建人所属部门
        if(StringUtils.isEmpty(department)){
            basisInfo.setDepartment(null);
        }
        else {
            basisInfo.setDepartment(department);
        }
        // 审核时间 默认当天
        basisInfo.setAuditDate(new Date());
        entityMapper.updateEntrustInfos(basisInfo);
        return true;
    }

    /**
     * 处理单位信息：
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer methodUnit(TestCompanyVo CompanyClientEntity) {
        Integer CompanyId = null;
        /**
         *  处理单位信息
         */
        if (!StringUtils.isEmpty(CompanyClientEntity.getCompanyName())) {
            // 判断单位是否存在? 存在跳过:不存在增加
            PageHelper.clearPage();
            TestCompanyEntity testCompanyClientEntity = new TestCompanyEntity();
            testCompanyClientEntity = testCompanyDao.selectEntrustCompanyData(new TestCompanyVo(
                    CompanyClientEntity.getCompanyName(), CompanyClientEntity.getType(), CompanyClientEntity.getAdminId()));
            if (StringUtils.isEmpty(testCompanyClientEntity)) {
                // 查询条件为空   需要新增委托单位
                TestCompanyEntity testCompanyClientEntity2 = new TestCompanyEntity();
                testCompanyClientEntity2.setCompanyName(CompanyClientEntity.getCompanyName());
                testCompanyClientEntity2.setType(String.valueOf(CompanyClientEntity.getType()));
                testCompanyClientEntity2.setAddTime(new java.util.Date());
                testCompanyDao.insert(testCompanyClientEntity2);
                CompanyId = testCompanyClientEntity2.getCompanyId();
            }
            if (StringUtils.isEmpty(CompanyId)) {
                CompanyId = testCompanyClientEntity.getCompanyId();
            }
            // 处理单位下联系人信息：
            if (!StringUtils.isEmpty(CompanyClientEntity.getContacts())) {
                // 通过公司id效验联系人是否存在? 为空进行新增。
                PageHelper.clearPage();
                if (CollectionUtils.isEmpty(testCustomerDao.getTestCustomerClientList(new TestCustomerEntity(CompanyId, CompanyClientEntity.getContacts())))) {
                    TestCustomerEntity testCustomerClientEntity = new TestCustomerEntity();
                    testCustomerClientEntity.setCompanyId(CompanyId);
                    testCustomerClientEntity.setContacts(CompanyClientEntity.getContacts());
                    testCustomerClientEntity.setPhone(CompanyClientEntity.getContactWay());
                    testCustomerDao.insertTestCustomer(testCustomerClientEntity);
                }
            }
            return CompanyId;
        }
        return null;
    }

    @Override
    public Boolean efficacyState(Long id) {
        // 效验委托单状态
        Integer bit = entityMapper.selectEntustAuditState(id);
        if(bit.equals(1)){
            return true;
        }
        return false;
    }

    /**
     * 生成委托编号
     * 编号类别： null 常规原材试验、MN模拟试验、BD比对试验
     * @param EntrustCategory 常规原材试验、模拟试验、比对试验
     * @param acceptanceDate 受理时间：受理时间（202208）
     *   受理时间（202208） if 当前月份有委托编号（2022080100）、往后跟委托编号（生成2022080101）
     *        else 没有（null）(202208+1)则默认 2022080001
     * @return
     */
    public EntrustCategoryVo returnEntrustCategoryVo(String EntrustCategory,String acceptanceDate)
    {
        // 接收按类型返回数据
        EntrustCategoryVo data = new EntrustCategoryVo();
        // 入参
        String categoryType = null;
        if(StringUtils.isEmpty(EntrustCategory)){
            // 常规原材试验或参数null
            PageHelper.clearPage();
            data = entityMapper.selectEntrustMaxNo(categoryType,acceptanceDate);
        }
        else if(!StringUtils.isEmpty(EntrustCategory) && EntrustCategory.equals("常规原材试验")){
            // 常规原材试验或参数null
            PageHelper.clearPage();
            data = entityMapper.selectEntrustMaxNo(categoryType,acceptanceDate);
        }
        else if(!StringUtils.isEmpty(EntrustCategory) && EntrustCategory.equals("模拟试验")){
            categoryType = "MN";
            PageHelper.clearPage();
            data = entityMapper.selectEntrustMaxNo(categoryType,acceptanceDate);
        }
        else if(!StringUtils.isEmpty(EntrustCategory) && EntrustCategory.equals("比对试验")){
            categoryType = "BD";
            PageHelper.clearPage();
            data = entityMapper.selectEntrustMaxNo(categoryType,acceptanceDate);
        }
        Integer code = 0;
        if(!StringUtils.isEmpty(data)){
            if (data.getEntrustmentNo() != null && data.getEntrustmentNo() > 0) {
                String substring = data.getEntrustmentNo().toString().substring(0, 6);
                if (substring.equals(acceptanceDate)) {
                    code = data.getEntrustmentNo() + 1;
                } else {
                    code = Integer.parseInt(acceptanceDate + "0001");
                }
            } else {
                code = Integer.parseInt(acceptanceDate + "0001");
            }
            data.setEntrustmentNo(code);
            data.setEntrustCategoryType(categoryType);
            data.setEntrustCategory((EntrustCategory!=null?EntrustCategory:"常规原材试验"));
        }
        else {
            // 重新赋值
            EntrustCategoryVo data1 = new EntrustCategoryVo();
            code = Integer.parseInt(acceptanceDate + "0001");
            data1.setEntrustmentNo(code);
            data1.setEntrustCategoryType(categoryType);
            data1.setEntrustCategory((EntrustCategory!=null?EntrustCategory:"常规原材试验"));
            return data1;
        }
        return data;
    }

    /**
     *
     * @param strSampleCode 样品编号
     * @param strReceivedDate 样品签收时间
     * @return 处理后样品编号 String类型
     */
    public String methodSampleCode(String strSampleCode,String strReceivedDate){
        // 处理样品编号:来样时间与样品编号需要一致
        StringBuffer sampleCode = new StringBuffer();
        // 样品编号 比对 来样时间 年份不一致 则更改样品编号 为当前年份最大编号。
        // 截取样品编号
        String[] sampleCodes = strSampleCode.split("-");
        // 样品来样时间
        String[] times = strReceivedDate.split("-");
        if(!sampleCodes[1].equals(times[0])){
            // 根据年限 查询最大样品编号
            Integer maxSampleCode = sampleEntityMapper.getMaxNumber(times[0]);
            maxSampleCode+=1;
            // 更改样品年限
            sampleCodes[1] = String.valueOf(times[0]);
            // 更改样品编号
            String suffix = new DecimalFormat("00000").format(maxSampleCode);
            sampleCodes[2] = suffix;
            for(int i=0; i<sampleCodes.length; i++){
                sampleCode.append(sampleCodes[i]);
                sampleCode.append("-");
            }
           if(sampleCode.deleteCharAt(sampleCode.length()-1).toString().length()>1){
               return sampleCode.toString();
           }
        }
        return null;
    }

    /**
     * 处理配合比编号
     *
     * @param strCode 原材编号
     * @param selfNumber 自身编号
     * @return
     */
    public String methodMixProportionSampleCode(String strCode,String selfNumber){
        String[] sampleCodes = strCode.split("-");
        String[] numbers = selfNumber.split("-");
        numbers[1] = sampleCodes[1];
        // 获取配合比下后缀规则定位 "_"
        String[] numberSuffixs = numbers[2].split("_");
        numbers[2] = sampleCodes[2];
        StringBuffer sampleCode = new StringBuffer();
        for(int i=0; i<numbers.length; i++){
            sampleCode.append(numbers[i]);
            sampleCode.append("-");
        }
        if(sampleCode.deleteCharAt(sampleCode.length()-1).toString().length()>1){
            sampleCode.append("_");
            sampleCode.append(numberSuffixs[1]);
         return sampleCode.toString();
        }
        return null;
    }

    /**
     * 处理样品状态表状态 test_sample_circulation_record
     * 处理数据：1、差集deleteSampleIds 进行删除
     * 处理数据：2、差集addSampleIds 进行新增
     *
     * @param oldSampleIds 旧样品id
     * @param leadingEndIds 前端实际存储样品id
     * @param name  流转人名字
     * @param userId 用户id
     */
    public void methodSampleIds(List<Integer> oldSampleIds, List<Integer> leadingEndIds,String name,Long userId){
        // 差集
        List<Integer> deleteList = oldSampleIds.stream()
                .filter(item -> !leadingEndIds.stream().collect(Collectors.toList()).contains(item))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(deleteList)){
            for(Integer sampleId : deleteList){
                // 删除样品流转状态 =0 根据样品id
                entityMapper.deleteTestSampleCirculationRecordById(sampleId);
            }
        }
        // 获取 新增的样品id数据
        List<Integer> addList = leadingEndIds.stream()
                .filter(item -> !oldSampleIds.stream().collect(Collectors.toList()).contains(item))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(addList)){
            for(Integer sampleId :addList){
                // 新增样品流转状态
                // 增加样品样品流转状态
                SampleCirculationRecord sa = new SampleCirculationRecord();
                sa.setSampleId(sampleId);
                sa.setStatus("0");
                sa.setOperatorId(userId);
                sa.setOperatorName(name);
                sa.setTime(new Date());
                sampleEntityMapper.saveSampleCirculationRecord(sa);
            }
        }
    }
    @Override
    public Boolean verifySampleIsUsed(Long id, List<SampleEntity> samples) {
        // 获取委托单id 获取已占用样品id集合
        List<Integer> oldSampleIds = sampleEntityMapper.getSampleIsUsed(id);
        // 委托单id 未绑定样品 返回 true
        if(oldSampleIds.isEmpty()){
            return true;
        }
        // 前端样品数据源
        List<Integer> leadingEndIds = new ArrayList<>();
        samples.stream().forEach(sample -> leadingEndIds.add(sample.getId()));
        // 获取 差集
        List<Integer> addList = leadingEndIds.stream()
                .filter(item -> !oldSampleIds.stream().collect(Collectors.toList()).contains(item))
                .collect(Collectors.toList());
        // 比较差集 新增样品id 是否与委托id绑定，绑定则返回 false
        if(!CollectionUtils.isEmpty(addList)){
            for(Integer sampleId :addList){
                if (entityMapper.getEntrustIdBySampleId(sampleId) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据任务单id 获取所属样品名称。
     * @param subList
     */
    public void getMethodSampleName(List<TestEntrustedTaskRelVo> subList){
        // 逻辑处理 设置任务单下样品名
        LinkedList<Long> taskIds = new LinkedList<>();
        for(TestEntrustedTaskRelVo taskRelVo : subList){
            if(taskRelVo.getTaskId()!=null){
                taskIds.add(taskRelVo.getTaskId());
            }
        }
        if(!CollectionUtils.isEmpty(taskIds)){
            List<TestEntrustedTaskRelVo> list = taskMapper.getSampleNames(taskIds);
            // 遍历数据
            for(TestEntrustedTaskRelVo testEntrustedTaskRelVo : subList){
                StringBuffer stringBuffer = new StringBuffer();
                // 遍历得到的样品与任务单关系
                for(TestEntrustedTaskRelVo taskRelVo : list){
                    if(testEntrustedTaskRelVo.getTaskId()!=null && testEntrustedTaskRelVo.getTaskId().equals(taskRelVo.getTaskId())){
                        stringBuffer.append(taskRelVo.getTaskSampleName());
                        stringBuffer.append("、");
                    }
                }
                // 补充任务单下样品名称
                if(stringBuffer.length()>1){
                    testEntrustedTaskRelVo.setTaskSampleName(stringBuffer.deleteCharAt(stringBuffer.length()-1).toString());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyTaskListExists(Long entrustId) {
        // 根据委托单id 获取任务单 列表
        List<Long> taskIds = entityMapper.getEntrustTaskIds(entrustId);
        // 通过委托单id 获取检测项与对应任务单id
        List<CheckItemInfoVo> items = entityMapper.getEntrustItemVos(entrustId);
        // 通过任务单列表 循环比较检测项所属任务单id 是否存在
        if (!CollectionUtils.isEmpty(items)) {
            // 有必要检查 任务单id 不为空
            if (!CollectionUtils.isEmpty(taskIds)) {
                for (Long taskId : taskIds) {
                    // status = fasle 不存在上述关系 进行更新任务单状态 = 废弃
                    Boolean status = false;
                    for (CheckItemInfoVo checkItemInfoVo : items) {
                        if (checkItemInfoVo.getTaskId().equals(taskId)) {
                            status = true;
                        }
                    }
                    if (status == false) {
                        // 更新任务单状态 = 废弃
                        TaskTestEntity taskTestEntity = new TaskTestEntity();
                        taskTestEntity.setId(taskId);
                        taskTestEntity.setState(144);
                        // 价格为0
                        taskTestEntity.setTaskPrice(0d);
                        taskMapper.updateTestTask(taskTestEntity);
                        // 任务单废弃 删除任务流转要求
                        testEntrustedTaskRelDao.deleteTaskId(taskId);
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                                " 更新任务单状态\t"+taskTestEntity.getState()+"任务单id"+taskTestEntity.getId(), Const.ENTRUST_FOUND, true);

                    }
                }
            }
        } else {
            // 有必要检查 任务单id 不为空
            if (!CollectionUtils.isEmpty(taskIds)) {
                // 遍历任务单状态 全部置为 144
                for (Long taskId : taskIds) {
                    // 更新任务单状态 = 废弃
                    TaskTestEntity taskTestEntity = new TaskTestEntity();
                    taskTestEntity.setId(taskId);
                    taskTestEntity.setState(144);
                    // 价格为0
                    taskTestEntity.setTaskPrice(0d);
                    taskMapper.updateTestTask(taskTestEntity);
                    // 任务单废弃 删除任务流转要求
                    testEntrustedTaskRelDao.deleteTaskId(taskId);
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),
                            " 更新任务单状态\t"+taskTestEntity.getState()+"任务单id"+taskTestEntity.getId(), Const.ENTRUST_FOUND, true);
                }
            }
        }
    }

    @Override
    public Boolean verifyDistributionTask(TaskVo entity) {
        // 通过委托单id 查询任务列表
        List<TaskProgressVo> taskList = taskMapper.getTaskStateByEntrustId(entity.getEntrustmentId());
        // 没有任务单
        if(CollectionUtils.isEmpty(taskList)){
            return true;
        }
        // 任务单列表循环 读取数据
        for(TaskProgressVo taskProgressVo :taskList){
            // 遍历检测项 指向的 所属部门
            for(CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()){
                // 根据检测项所属团队比对任务单团队一致，并且 任务单state = 4 || state = 6
                if(checkItemDeptVo.getDeptId().equals(taskProgressVo.getDeptId().longValue())) {
                    if (taskProgressVo.getState().equals(4) || taskProgressVo.getState().equals(6)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 发布时 下拉选择团队：  新增逻辑（
     *                      * 情况1、 1.1：指定任务单存在相同团队。 合并，成功
     *                      *        1.2：启用废弃任务单，选择相同团队后。
     *                      * 情况2、不存在相同团队。 新增任务单即可。
     * 					都需要统一任务单价格计算。
     * @param entity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionTask320(TaskVo entity) {
        // 通过委托单id 查询任务列表？不存在（正常走发布列表）：存在 （处理情况1、2、3）
        List<TaskProgressVo> taskList = taskMapper.getTaskStateByEntrustId(entity.getEntrustmentId());
        // 没有任务单 走新增发布路线
        if(CollectionUtils.isEmpty(taskList)){
            Boolean flag = distributionTask412(entity);
            return flag;
        }

        // 处理情况1：任务单与检测项指向团队一致的话 更新任务单价格和检测项所属部门
        methodDistributionTask1(entity,taskList);
        // 情况2、不存在相同团队。 新增任务单即可。
        methodDistributionTask2(entity,taskList);
        return true;
    }

    /**
     * 情况1、 1.1：指定任务单存在相同团队。 合并，成功
     *         1.2：启用废弃任务单，选择相同团队后。
     * @param entity
     * @param taskList
     */
    void methodDistributionTask1(TaskVo entity,List<TaskProgressVo> taskList){
        Boolean status = true;
        // 遍历：通过委托单id下 任务单依次展示
        for(TaskProgressVo taskProgressVo : taskList){
            TaskTestEntity taskTestEntity = new TaskTestEntity();
            taskTestEntity.setId(taskProgressVo.getTaskId());
            //计算本单价格
            double taskPrice = taskProgressVo.getTaskPrice();
            // 任务单 =144 && 检测项重新发布 改变任务单状态
            if(taskProgressVo.getState().equals(144)){
                // 状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成
                Integer state = null;
                if(taskProgressVo.getOrderTime()!=null){
                    state = 0;
                }
                if(taskProgressVo.getReceiveTime()!=null){
                    state = 1;
                }
                if(taskProgressVo.getStartDetectionTime()!=null){
                    state = 3;
                }
                taskTestEntity.setState(state);
            }
            taskTestEntity.setState(taskTestEntity.getState());
            // 比对部门id 是否出具最终报告标记
            if (!CollectionUtils.isEmpty(entity.getDeptIds())&&entity.getDeptIds().contains(taskProgressVo.getDeptId())) {
                taskTestEntity.setIssueReport(1);
            } else {
                taskTestEntity.setIssueReport(0);
            }
            List<CheckItemDeptVo> checkItemDeptVoList1 = new ArrayList<>();
            // 遍历待发布检测项列表
            for(CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()){
                // if 任务单团队id = 检测项id 批量修改。
                if(checkItemDeptVo.getDeptId().equals(taskProgressVo.getDeptId().longValue())){
                    CheckItemDeptVo checkItemDeptVo1 = new CheckItemDeptVo();
                    checkItemDeptVo1.setId(checkItemDeptVo.getId());
                    checkItemDeptVo1.setDeptId(checkItemDeptVo.getDeptId());
                    checkItemDeptVo1.setTaskId(taskProgressVo.getTaskId());
                    // 任务单此次新增价格
                    taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                            (checkItemDeptVo.getCheckPrice() == null ? 0 : checkItemDeptVo.getCheckPrice()) * checkItemDeptVo.getTimes());
                    // 比对任务单团队与检测项团队一致的话 修改检测项所属团队信息。
                    checkItemDeptVoList1.add(checkItemDeptVo1);
                }
                // 设置此次任务单最新价格
                taskTestEntity.setTaskPrice(taskPrice);
            }
            if(!CollectionUtils.isEmpty(checkItemDeptVoList1)){
                //批量更新检测项信息
                taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
            }
            // 比对任务单价格发生变动则 更新数据
            if(!taskTestEntity.getTaskPrice().equals(taskProgressVo.getTaskPrice())){
                // 更新任务单价格并且状态修改为默认值
                taskMapper.updateTestTask(taskTestEntity);
                //更新委托单状态
                taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
                // status = false
                status = false;
            }
        }
        // 处理任务流转信息 通过委托单id 和 传入信息 !=taskRelEntities.isEmpty()
        if(!CollectionUtils.isEmpty(entity.getTaskRelEntities()) && !status){
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
    }
    /**
     * 情况2、不存在相同团队。 新增任务单即可。
     * @param entity 新增待发布信息
     * @param taskList 任务单列表
     */
    Boolean methodDistributionTask2(TaskVo entity,List<TaskProgressVo> taskList){
        // 待发布检测项集合
        List<CheckItemDeptVo> addItemList = new ArrayList<>();
        // 遍历检测项
        for(CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()){
            // 设置标志位
            Boolean status = true;
            // 当前待发布检测项（checkItemDeptVo） 不属于下列任务单（taskList）所属部门 则新增。
            for(TaskProgressVo taskProgressVo : taskList){
                // if 任务单团队deptId = 检测项deptId   status = false。
                if(checkItemDeptVo.getDeptId().equals(taskProgressVo.getDeptId().longValue())){
                    status = false;
                }
            }
            if(status){
                CheckItemDeptVo checkItemDeptVo1 = new CheckItemDeptVo();
                checkItemDeptVo1.setId(checkItemDeptVo.getId());
                checkItemDeptVo1.setDeptId(checkItemDeptVo.getDeptId());
                checkItemDeptVo1.setCheckPrice(checkItemDeptVo.getCheckPrice());
                checkItemDeptVo1.setTimes(checkItemDeptVo.getTimes());
                checkItemDeptVo1.setTaskId(null);
                addItemList.add(checkItemDeptVo1);
            }
        }
        // 待发布检测项集合 不等于空
        if(!CollectionUtils.isEmpty(addItemList)){
            // 把数据进行替换。
            entity.setCheckItemDeptVoList(addItemList);
            Boolean flag = distributionTask412(entity);
            return flag;
        }
        return true;
    }

    /**
     * 当天任务统计
     * @param testEntrustedTaskRelVo
     * @return
     */
    @Override
    public PageInfo taskStatisticsList2(TestEntrustedTaskRelVo testEntrustedTaskRelVo) {

        List<TestEntrustedTaskRelVo> list = Lists.newArrayList();
        //拆分委托编号
        if(!StringUtils.isEmpty(testEntrustedTaskRelVo.getEntrustmentNostr())){
            EntrustCategoryVo entrustCategoryVo = EntrustNoStrUtils.splitEntrustNo(testEntrustedTaskRelVo.getEntrustmentNostr());
            testEntrustedTaskRelVo.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            testEntrustedTaskRelVo.setEntrustNo(entrustCategoryVo.getEntrustmentNo().toString());
            if(!StringUtils.isEmpty(entrustCategoryVo.getEntrustmentNo())){
                testEntrustedTaskRelVo.setEntrustNo(entrustCategoryVo.getEntrustmentNo().toString());
            }
        }
        PageHelper.clearPage();
        // 查询 中间报告数据
         list = testEntrustedTaskRelDao.getTaskStatisticsMidList(testEntrustedTaskRelVo);
        if(!CollectionUtils.isEmpty(list)){
            // 中间报告 中 不包含 最终报告数据
            List<TestEntrustedTaskRelVo> allList = testEntrustedTaskRelDao.getTaskStatisticsAllList(testEntrustedTaskRelVo);
          if(!CollectionUtils.isEmpty(allList)){
              for(TestEntrustedTaskRelVo tt1 : allList){
                  for(TestEntrustedTaskRelVo tt2 : list){
                      if(tt2.getId().equals(tt1.getId()) && tt1.getType().equals(tt2.getType())){
                        // 对 中间数据源赋值。
                          if(!StringUtils.isEmpty(tt1.getReportCode())){
                              tt2.setReportCode(tt1.getReportCode());
                          }
                          if(!StringUtils.isEmpty(tt1.getReportFinishTime())){
                              tt2.setReportFinishTime(tt1.getReportFinishTime());
                          }
                      }
                  }
              }
          }
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
                // if 中间报告!=空 && 最终报告 =null
                if(!StringUtils.isEmpty(testEntrustedTaskRelVo1.getMidReportCode()) && StringUtils.isEmpty(testEntrustedTaskRelVo1.getReportCode())){
                    testEntrustedTaskRelVo1.setReportCode(testEntrustedTaskRelVo1.getMidReportCode());
                }
                if(StringUtils.isEmpty(testEntrustedTaskRelVo1.getReportCode())){
                    testEntrustedTaskRelVo1.setReportCode("--");
                }
                // if 中间报告结束时间 ！= 空
                if(!StringUtils.isEmpty(testEntrustedTaskRelVo1.getMidReportFinishTime()))
                {
                    testEntrustedTaskRelVo1.setReportFinishTime(testEntrustedTaskRelVo1.getMidReportFinishTime());
                }
                else {
                    testEntrustedTaskRelVo1.setReportFinishTime(null);
                }

            }
        }
        Integer pageNum = testEntrustedTaskRelVo.getPageNum();
        Integer pageSize = testEntrustedTaskRelVo.getPageSize();
        if(StringUtils.isEmpty(pageNum)||pageNum<=0){
            pageNum = 1;
        }
        if(StringUtils.isEmpty(pageSize)||pageSize<=0){
            pageSize = 10;
        }
        // 如果页码展示数量大于最大数 返回最大数值
        if(pageSize>list.size()){
            pageSize = list.size();
        }
        PageInfo pageInfo = new PageInfo();
        //分页
        List<TestEntrustedTaskRelVo> subList;
        if (list.size() > 10 && list.size() / 10 >= pageNum) {
            subList = list.subList((pageNum - 1) * pageSize, pageNum * pageSize);
        } else {
            subList = list.subList((pageNum - 1) * pageSize, list.size());
        }
        getMethodSampleName(subList);
        pageInfo.setList(subList);
        pageInfo.setTotal(list.size());
        return pageInfo;
    }

    @Override
    public Boolean verifyTaskState(List<TaskTestEntity> taskList){
        // 根据委托单查询任务单状态
        for(TaskTestEntity taskTestEntity : taskList){
            if(taskTestEntity.getState() >=3){
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean entrustRevocation(List<TaskTestEntity> list,Long entrustId){
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        String userName = userInfo.getUserId() + "&" + userInfo.getUsername();
        // 查询任务单信息
        for(TaskTestEntity taskTestEntity : list){
            // 查询任务单详情：
            TaskTestEntity data = taskMapper.selectTaskEntity(taskTestEntity.getId());
            // 删除时间
            data.setWasteTime(new Date());
            // 操作人
            data.setDerelict(userName);
            // 新增已删除任务单 插入表 test_task_used
            taskMapper.inserTasUsed(data);
            // 删除任务单
            taskMapper.deleteTaskById(data.getId());
            // 根据任务单id 删除流转信息
            taskMapper.deleteTaskRel(data.getId());
        }
        // 委托单 置为0
        EntrustEntity basisInfo = new EntrustEntity();
        basisInfo.setId(entrustId);
        basisInfo.setState(0);
        // 删除任务流转信息 根据任务单id
        entityMapper.updateEntrustInfos(basisInfo);
        // 根据委托单id 进行批量处理检测项状态
        taskMapper.batchUpdateItemState(entrustId);
        return true;
    }

    @Override
    public XWPFDocument downloadEntrustNew(EntrustAddVo detail, InputStream object) {
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
                XWPFTable table = tables.get(j);
                // 设置表格的字体
//                CTTblBorders borders = table.getCTTbl().getTblPr().addNewTblBorders();
//                CTBorder insideHBorder = borders.addNewInsideH();
//                insideHBorder.setVal(STBorder.SINGLE);
//                insideHBorder.setSz(new BigInteger("10")); // 设置字体大小为10
//                insideHBorder.setColor("auto");
                //设置字体
//                List<XWPFTableRow> tableRows = table.getRows();
//                for (int r =0;r<tableRows.size();r++) {
//                    List<XWPFTableCell> tableCells = tableRows.get(r).getTableCells();
//                    for (int k =0;k<tableCells.size();k++) {
//                        List<XWPFParagraph> paragraphs = tableCells.get(k).getParagraphs();
//                        for (int d=0;d<paragraphs.size();d++){
//                            XWPFParagraph paragraph = paragraphs.get(d);
//                            XWPFRun run = paragraph.createRun();
//                            run.setFontFamily("宋体");
//                            run.setFontSize(10);
//                        }
//                    }
//                }

                List<XWPFTableRow> rows;
                //获取表格对应的行
                rows = table.getRows();
                if (j == 0) {
                    //设置模板数据
                    rows.get(3).getTableCells().get(2).setText("No：" + detail.getEntrustmentNostr());//委托单号
                    rows.get(4).getTableCells().get(2).setText(detail.getEntrustCompany());//委托单位
                    rows.get(5).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getWitnessUint()) ? "——" : detail.getWitnessUint());//见证单位
                    rows.get(6).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getProjectName()) ? "——" : detail.getProjectName());//工程名称
                    rows.get(7).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getProjectPart()) ? "——" : detail.getProjectPart());//工程部位
                    //新增的行数
                    int sampleIndex = 9;
                    int index = 1;
                    int start = 15;
                    if (samples.size() > 6) {
                        AsposeUtil.addRowsIndex(tables.get(0), sampleIndex, samples.size() - 6,10);
                        start = 15 + (samples.size() - 6);
                    }
                    for (int i = 0; i < samples.size(); i++) {
                        rows.get(sampleIndex).getTableCells().get(index).setText(samples.get(i).getAliasName());//样品名称
                        rows.get(sampleIndex).getTableCells().get(index + 1).setText(StringUtils.isEmpty(StringUtils.isEmpty(detail.getProjectPart()) ? "——" : detail.getProjectPart()) ? "——" : samples.get(i).getSpecs());//规格等级
                        rows.get(sampleIndex).getTableCells().get(index + 2).setText(StringUtils.isEmpty(samples.get(i).getBatchNumber()) ? "——" : samples.get(i).getBatchNumber());//批号/编号
                        rows.get(sampleIndex).getTableCells().get(index + 3).setText(StringUtils.isEmpty(samples.get(i).getSampleQuantity()) ? "——" : samples.get(i).getSampleQuantity());//样品数量
                        rows.get(sampleIndex).getTableCells().get(index + 4).setText(StringUtils.isEmpty(samples.get(i).getGeneration()) ? "——" : samples.get(i).getGeneration());//代表批量
                        rows.get(sampleIndex).getTableCells().get(index + 5).setText(StringUtils.isEmpty(samples.get(i).getManufacturer()) ? "——" : samples.get(i).getManufacturer());//样品产地/生产厂家
                        rows.get(sampleIndex).getTableCells().get(index + 6).setText(StringUtils.isEmpty(samples.get(i).getSampleRemark()) ? "——" : samples.get(i).getSampleRemark());//样品备注
                        sampleIndex = sampleIndex + 1;
                    }
                    //设置其它信息
                    String ss = "";
                    rows.get(start).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getPresentInformation()) ? "——" : detail.getPresentInformation());//提供资料
                    start = start + 1;
                    if (org.apache.commons.lang.StringUtils.isEmpty(detail.getSamplingMethod())) {
                        rows.get(start).getTableCells().get(2).setText("——");//取样方式
                    } else {
                        String desc = "";
                        if ("送样".equals(detail.getSamplingMethod())) {
                            desc = "☑送样        ☐抽样        ☐现场检验      ☐其它";
                        } else if ("抽样".equals(detail.getSamplingMethod())) {
                            desc = "☐送样        ☑抽样        ☐现场检验      ☐其它";
                        } else if ("现场检验".equals(detail.getSamplingMethod())) {
                            desc = "☐送样        ☐抽样        ☑现场检验      ☐其它";
                        } else {
                            desc = "☐送样        ☐抽样        ☐现场检验      ☑其它";
                        }
                        rows.get(start).getTableCells().get(2).setText(desc);//取样方式
                    }
                    start = start + 1;
                    if (StringUtils.isEmpty(detail.getCheckPurpose())) {
                        rows.get(start).getTableCells().get(4).setText("——");//检验目的
                    } else {
                        String desc = "";
                        if ("委托检测".equals(detail.getCheckPurpose())) {
                            desc = desc = "☑委托检验    ☐执法检验    ☐新产品鉴定    ☐见证取样检验      ☐其它";
                        } else if ("执法检验".equals(detail.getCheckPurpose())) {
                            desc = desc = "☐委托检验    ☑执法检验    ☐新产品鉴定    ☐见证取样检验      ☐其它";
                        } else if ("新产品鉴定".equals(detail.getCheckPurpose())) {
                            desc = desc = "☐委托检验    ☐执法检验    ☑新产品鉴定    ☐见证取样检验      ☐其它";
                        } else if ("见证取样检测".equals(detail.getCheckPurpose())) {
                            desc = desc = "☐委托检验    ☐执法检验    ☐新产品鉴定    ☑见证取样检验      ☐其它";
                        } else {
                            desc = "☐委托检验    ☐执法检验    ☐新产品鉴定    ☐见证取样检验      ☑其它";
                        }
                        rows.get(start).getTableCells().get(2).setText(desc);//检验目的
                    }
                    start = start + 1;
                    //检测项目
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
                                        stringBuilder1.append("，");
                                    }
                                }
                            }
                        }
                        if (stringBuilder1.length() > 1) {
                            String substring = stringBuilder1.toString().substring(0, stringBuilder1.length() - 1);
                            rows.get(start).getTableCells().get(2).setText(substring);//检验项目
                        }else {
                            rows.get(start).getTableCells().get(2).setText("");//检验项目
                        }
                        start = start + 1;
                        //检测依据
                        StringBuilder stringBuilder11 = new StringBuilder();
                        if (!CollectionUtils.isEmpty(samples)) {
                            for (SampleEntity entity : samples) {
                                List<JudgmentBasisVo> sampleCheckItem = entity.getJudgmentBasisVos();
                                if (!CollectionUtils.isEmpty(sampleCheckItem)) {
                                    for (JudgmentBasisVo itemEntity : sampleCheckItem) {
                                        //价钱为null的不展示
                                        if (itemEntity.getCheckPrice() != null) {
                                            if (!StringUtils.isEmpty(itemEntity.getStandardName())) {
                                                String s = itemEntity.getStandardName();
                                                String aa = s.split("《")[0];
                                                stringBuilder11.append(aa);
                                            }
                                            stringBuilder11.append("☐");
                                        }
                                    }
                                }
                            }
                            if (stringBuilder11.length() > 1) {
                                String substring = stringBuilder11.toString().substring(0, stringBuilder11.length() - 1);
                                String[] split = substring.split("☐");
                                Set<String> set = new HashSet<>();
                                for (String s : split) {
                                    set.add(s);
                                }
                                String substring1 = set.toString().substring(1, set.toString().length() - 1);
                                rows.get(start).getTableCells().get(2).setText(substring1);//检测依据
                            }
                            List<String> list = entityMapper.getSampleStandard(detail.getId());
                            StringBuilder stringBuilder = new StringBuilder();
                            if (!CollectionUtils.isEmpty(list)) {
                                for (String s : list) {
                                    stringBuilder.append(s);
                                    stringBuilder.append("，");
                                }
                                String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                                rows.get(start).getTableCells().get(4).setText(StringUtils.isEmpty(substring) ? "——" : substring);//产品标准 TODO 去重
                            }
                            start = start + 1;
                        }
                        //取报告方式
                        rows.get(start).getTableCells().get(6).setText(StringUtils.isEmpty(detail.getReportReceivingUnit()) ? "——" : detail.getReportReceivingUnit());//收报告单位
                        rows.get(start).getTableCells().get(8).setText(StringUtils.isEmpty(detail.getAddressee()) ? "——" : detail.getAddressee());//联系人
                        start = start+1;
                        rows.get(start).getTableCells().get(2).setText(detail.getReportCount().toString());//报告分数
                        if (StringUtils.isEmpty(detail.getReportType())){
                            rows.get(start).getTableCells().get(3).setText("——");//取报告方式
                            rows.get(start).getTableCells().get(4).setText("——");//取报告方式
                        }else {
                            if ("自取".equals(detail.getReportType())){
                                rows.get(start).getTableCells().get(3).setText("√");//取报告方式
                            }
                            if ("邮寄".equals(detail.getReportType())){
                                rows.get(start).getTableCells().get(4).setText("√");//取报告方式
                            }
                        }
                        rows.get(start).getTableCells().get(6).setText(StringUtils.isEmpty(detail.getAddress()) ? "——" : detail.getAddress());//联系地址
                        rows.get(start).getTableCells().get(8).setText(StringUtils.isEmpty(detail.getMobile()) ? "——" : detail.getMobile());//联系方式
                        start = start +1;
                        //委托单立人签字、见证人、日期
                        rows.get(start).getTableCells().get(1).setText(StringUtils.isEmpty(detail.getEntrustPeople()) ? "——" : "委托代理人签字："+detail.getEntrustPeople());//委托人

                        Date acceptanceDate = detail.getAcceptanceDate();
                        if (acceptanceDate == null){
                            rows.get(start).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getWitnessPerson()) ? "——" : "见证人: "+detail.getWitnessPerson());//见证人
                        }else {
                            String s = DateUtil.formatDate(acceptanceDate);
                            String[] split = s.split("-");
                            rows.get(start).getTableCells().get(2).setText(StringUtils.isEmpty(detail.getWitnessPerson()) ? "——" : "见证人: "+detail.getWitnessPerson()+"  "+split[0]+"年"+split[1]+"月"+split[2]+"日");//见证人
                        }

                        start = start +1;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        for (SampleEntity sampleEntity : samples) {
                            stringBuilder2.append(sampleEntity.getAliasName());
                            stringBuilder2.append("（");
                            if (org.apache.commons.lang3.StringUtils.isNotEmpty(sampleEntity.getSpecs())) {
                                stringBuilder2.append(sampleEntity.getSpecs());
                            } else {
                                stringBuilder2.append("——");
                            }
                            stringBuilder2.append("、");
                            String s = sampleEntity.getOutwardDescribe();
                            if (org.apache.commons.lang3.StringUtils.isNotEmpty(s)) {
                                stringBuilder2.append(s);
                            } else {
                                stringBuilder2.append("——");
                            }
                            stringBuilder2.append("）；");
                        }
                        if (stringBuilder2.toString().length() >= 1) {
                            rows.get(start).getTableCells().get(2).setText(stringBuilder2.toString().substring(0, stringBuilder2.length() - 1));//样品状态
                        }
                        if ("1".equals(detail.getIsSave())){
                            rows.get(start).getTableCells().get(4).setText("是☑      "+
                                    "否☐");//样品保留
                        }else {
                            rows.get(start).getTableCells().get(4).setText("是☐      " +
                                    "否☑");//样品保留
                        }
                        start = start +1;
                        rows.get(start).getTableCells().get(2).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getActualPrice()) ? "——" : detail.getActualPrice());//检验收费
                        rows.get(start).getTableCells().get(4).setText(org.apache.commons.lang3.StringUtils.isEmpty(detail.getPaymentRecordShow()) ? "——" : detail.getPaymentRecordShow());//本次交费
                        start = start +1;
                        String date = DateUtil.formatDate(detail.getRequestDate());
                        String[] split = date.split("-");
                        String s = split[0]+"年"+split[1]+"月"+split[2]+"日";
                        rows.get(start).getTableCells().get(2).setText(s);//完成期限

                        String formatDate = DateUtil.formatDate(detail.getAcceptanceDate());
                        String[] strings = formatDate.split("-");
                        String s1 = strings[0]+"年"+strings[1]+"月"+strings[2]+"日";
                        if (StringUtils.isEmpty(detail.getBusinessAcceptor())){
                            rows.get(start).getTableCells().get(3).setText("业务受理人签字：——         "+s1);//业务受理人
                        }else {
                            rows.get(start).getTableCells().get(3).setText("业务受理人签字："+detail.getBusinessAcceptor()+"    "+s1);//业务受理人
                        }
                        //备注
                        start = start+2;
                        rows.get(start).getTableCells().get(1).setText(StringUtils.isEmpty(detail.getRemark())?"——":detail.getRemark());//备注
                    }
                }
            }
        } catch (Exception e) {
            logger.error("设置委托单信息到模板异常:{}", e);
        }
        return doc;
    }

    @Override
    public QrCodeAuthRes qrCodeAuth(String reportCode) {
        QrCodeAuthRes qrCodeAuthRes = new QrCodeAuthRes();
        String info = recordEntityMapper.getInitInfo();
        ReportRecordEntity entity = recordEntityMapper.getEntrust(reportCode);
        Long entrustId = entity.getEntrustmentId() == null ? entity.getEntrustId() : entity.getEntrustmentId();
        EntrustAddVo detail = this.getEntrustHistoryDetail(entrustId);
        qrCodeAuthRes.setEntrustCompany(detail.getEntrustCompany());
        qrCodeAuthRes.setProjectName(detail.getProjectName());
        qrCodeAuthRes.setEntrustPeople(detail.getEntrustPeople());
        qrCodeAuthRes.setAcceptanceDate(detail.getAcceptanceDate());
        List<SampleEntity> samples = detail.getSamples();
        StringBuilder codes = new StringBuilder();
        StringBuilder names = new StringBuilder();
        StringBuilder specs = new StringBuilder();
        for (int i =0;i<samples.size();i++){
            codes.append(samples.get(i).getSampleCode());
            names.append(samples.get(i).getSampleName());
            specs.append(samples.get(i).getSpecs());
            if (i < samples.size()-1){
                codes.append(",");
                names.append(",");
                specs.append(",");
            }
        }
        qrCodeAuthRes.setSampleCode(codes.toString());
        qrCodeAuthRes.setSampleName(names.toString());
        qrCodeAuthRes.setSpecs(specs.toString());
        qrCodeAuthRes.setCheckOrganization(info);
        qrCodeAuthRes.setReportCode(reportCode);
        return qrCodeAuthRes;
    }

    @Override
    public JSONObject operatingPersonnel(Long entrustId) {
        SysUserEntity sysUserEntity = entityMapper.operatingPersonnel(entrustId);
        JSONObject object = new JSONObject();
        if (sysUserEntity != null){
            object.put("name",sysUserEntity.getName());
            object.put("mobile",sysUserEntity.getMobile());
            return object;
        }else {
            object.put("name",ShiroUtils.getUserInfo().getName());
            object.put("mobile",ShiroUtils.getUserInfo().getMobile());
            return object;
        }
    }

}
