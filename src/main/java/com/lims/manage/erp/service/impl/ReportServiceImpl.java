package com.lims.manage.erp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.ConclusionEntity;
import com.lims.manage.erp.entity.ParamEntity;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.QiYueSuoSealEntity;
import com.lims.manage.erp.entity.QuotaEntity;
import com.lims.manage.erp.entity.QuotaRes;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportRecordMidEntity;
import com.lims.manage.erp.entity.ReportResBean;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.SealEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TeamTreeStructureEntity;
import com.lims.manage.erp.entity.TestEntrustedTaskRelEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.entity.TreeEntity;
import com.lims.manage.erp.http.QiYueSuoDocment;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordMidEntityMapper;
import com.lims.manage.erp.mapper.ReportTemplateEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestEntrustedTaskRelDao;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.mapper.TestReportQualifcationDao;
import com.lims.manage.erp.mapper.TestReportTemplateDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleMixInfoEntityMapper;
import com.lims.manage.erp.mapper.TestTechnicistDao;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportRecordEntityMapper entityMapper;
    @Autowired
    private ReportTemplateEntityMapper templateEntityMapper;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportRecordEntityMapper recordEntityMapper;
    @Autowired
    private ReportRecordDetailEntityMapper recordDetailEntityMapper;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private QiYueSuoHnadler qiYueSuoHnadler;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    ReportApprovalMapper reportApprovalMapper;
    @Autowired
    EntrustServiceImpl entrustService;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TestProductDao testProductDao;
    @Autowired
    private TestReportQualifcationDao dao;
    @Resource
    private TestProductItemDao itemDao;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private TestSampleMixInfoEntityMapper mixInfoEntityMapper;
    @Autowired
    private TestReportTemplateDao templateDao;
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;
    @Autowired
    private TestTechnicistDao testTechnicistDao;
    @Autowired
    private TestEntrustedTaskRelDao taskRelDao;
    @Autowired
    private ReportRecordMidEntityMapper midReportMapper;

    @Override
    public List<ReportListVo> getReportList() {
        // 报告生成列表
        List<ReportListVo> reportList = reportMapper.getReportList();
        // 已经生成的 报告列表
        List<ReportRecordEntity> list = recordEntityMapper.getReportList();
        //                        0 审批灰色 1 是可以审批
        Iterator<ReportListVo> iterator = reportList.iterator();
        while (iterator.hasNext()) {
            ReportListVo reportListVo = iterator.next();
            for (ReportRecordEntity reportRecordEntity : list) {
                // 如果test_report_report 的 EntrustmentId = reportListVo.getId()
                if (reportRecordEntity.getEntrustmentId().equals(reportListVo.getId())) {
                    // state =1 并且 没有提交审批
                    if (reportRecordEntity.getState().equals("1") && reportRecordEntity.getReportCompleteTime() == null) {
                        // 报告已完成
                        reportListVo.setState(1);
                    }
                    // 报告完成 已经提交
                    if (reportRecordEntity.getReportCompleteTime() != null) {
                        iterator.remove();
                    }
                    if (reportRecordEntity.getState().equals("2")) {
                        // 报告已完成
                        reportListVo.setState(0);
                    }
                }
                if (reportListVo.getState() == null) {
                    reportListVo.setState(0);
                }
            }
        }
        return reportList;
    }

    //    @Override
//    public List<ReportListVo> makeReport() {
//        return reportMapper.getReportList2(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
//    }
    @Override
    public PageInfo makeReport(Integer pageNum, Integer pageSize, String search) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.getReportList2(userTeamIds, search);
        for (ReportListVo reportListVo : list) {
            StringBuilder sampleName = new StringBuilder();
            List<String> sampleNames = reportMapper.getSampleNames(reportListVo.getId());
            for (int j = 0; j <sampleNames.size(); j++) {
                sampleName.append(sampleNames.get(j));
                if(j != sampleNames.size()-1){
                    sampleName.append("/");
                }
            }
            reportListVo.setSampleName(sampleName.toString());
        }
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }


    @Override
    public PageInfo reportDownloadList(Integer pageNum, Integer pageSize, String search) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        PageHelper.clearPage();
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.reportDownloadList(userTeamIds, search);
        for (ReportListVo reportListVo : list) {
            //设置样品信息
            List<String> sampleNames = reportMapper.getSampleNames(reportListVo.getId());
            StringBuilder sampleName = new StringBuilder();
            for (int i = 0; i < sampleNames.size(); i++) {
                sampleName.append(sampleNames.get(i));
                if(i != sampleNames.size()-1){
                    sampleName.append("/");
                }
            }
            reportListVo.setSampleName(sampleName.toString());
            //设置任务单号
            List<String> taskCodes = reportMapper.getTaskCodes(reportListVo.getId());
            reportListVo.setTaskCodes(taskCodes);
        }
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 提交审批
     *
     * @param id
     * @param name 报告提交申请人
     * @return
     */
    @Transactional
    @Override
    public Boolean getReportSubmit(Long id, String name) {
//        // 根据委托单id 查询报告信息 state=1
//        ReportRecordEntity reportData = recordEntityMapper.getReportEntrust(id);
//        if(reportData.getState().equals("1")){
//            // 修改状态
//            reportData.setReportCompleteTime(new Date());
//            reportData.setApplicant(name);
//             recordEntityMapper.updateByEntrustIdSelective(reportData);
//            // 根据任务单主键 获取委托单主键 更改委托单状态
//            EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(id);
//            if(entrustBaseInfo.getState()!=null&&entrustBaseInfo.getState()<7){
//                taskMapper.updateEntrustById(entrustBaseInfo.getId(),7);
//            }
//            return true;
//        }
//        return false;
        // 修改状态 提交审批 直接进行改状态 state=3
        ReportRecordEntity reportData = new ReportRecordEntity();
        reportData.setReportCompleteTime(new Date());
        reportData.setApplicant(name);
        reportData.setState("3");
        recordEntityMapper.updateByEntrustIdSelective(reportData);
        // 根据任务单主键 获取委托单主键 更改委托单状态
        EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(id);
        if (entrustBaseInfo.getState() != null && entrustBaseInfo.getState() < 7) {
            taskMapper.updateEntrustById(entrustBaseInfo.getId(), 7);
        }
        return true;

    }

    @Override
    public Boolean getReportSubmit_two(ReportRecordEntity reportData) {

        // 修改状态 提交审批 直接进行改状态 state=3
        reportData.setReportCompleteTime(new Date());
        reportData.setState("3");
        String[] verifyers = reportData.getVerifyer().split("&");
        reportData.setVerifyer(verifyers[0]);
        reportData.setVerifyerId(Long.parseLong(verifyers[1]));
        String[] issuers = reportData.getIssuer().split("&");
        reportData.setIssuer(issuers[0]);
        reportData.setIssuerId(Long.parseLong(issuers[1]));
        recordEntityMapper.updateByEntrustId(reportData);
        // 根据任务单主键 获取委托单主键 更改委托单状态
        EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(reportData.getEntrustmentId());
        if (entrustBaseInfo.getState() != null && entrustBaseInfo.getState() < 7) {
            taskMapper.updateEntrustById(entrustBaseInfo.getId(), 7);
        }
        return true;
    }

    @Override
    public PageInfo getReportList_history(String search, Integer pageNum, Integer pageSize) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        reportListVo.setDeptIds(userTeamIds);
        List<ReportListVo> list = reportMapper.getReportList_history(reportListVo);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo reportDownloadListHistory(String search, Integer pageNum, Integer pageSize) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setReportCode(search);
        reportListVo.setDeptIds(userTeamIds);
        List<ReportListVo> list = reportMapper.reportDownloadListHistory(reportListVo);
        //设置任务单号
        for (ReportListVo reportListVo1 : list) {
            List<String> taskCodes = reportMapper.getTaskCodes(reportListVo1.getId());
            reportListVo1.setTaskCodes(taskCodes);
        }
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public ReportSampleDetailVo getReportList_history_details(Long recordId,Long taskId) {
        ReportSampleDetailVo reportSampleDetailVo = new ReportSampleDetailVo();
        // 获取报告头部信息
        List<ReportSampleDetailVo> list = reportMapper.getReportHeadDetails(recordId);
        // 获取样品编号
        Set<String> setSampleCode = new HashSet<>();
        // 获取外观
        Set<String> setOutwarde = new HashSet<>();
        // 规格等级
        Set<String> setSpecs = new HashSet<>();
        // 判定依据
        Set<String> setStandard = new HashSet<>();
        // 处理信息
        for (ReportSampleDetailVo reportSampleDetailVo1 : list) {
            if (reportSampleDetailVo1.getSampleCode() != null && !reportSampleDetailVo1.getSampleCode().equals("")) {
                setSampleCode.add(reportSampleDetailVo1.getSampleCode());
            }
            if (reportSampleDetailVo1.getOutward() != null && !reportSampleDetailVo1.getOutward().equals("")) {
                setOutwarde.add(reportSampleDetailVo1.getOutward());
            }
            if (reportSampleDetailVo1.getSpecs() != null && !reportSampleDetailVo1.getSpecs().equals("")) {
                setSpecs.add(reportSampleDetailVo1.getSpecs());
            }
            if (reportSampleDetailVo1.getStandard() != null && !reportSampleDetailVo1.getStandard().equals("")) {
                setStandard.add(reportSampleDetailVo1.getStandard());
            }
            reportSampleDetailVo.setSampleName(reportSampleDetailVo1.getSampleName());
        }
        for (String str1 : setSampleCode) {
            if (reportSampleDetailVo.getSampleCode() == null) {
                reportSampleDetailVo.setSampleCode(str1 + "、");
            } else {
                reportSampleDetailVo.setSampleCode(reportSampleDetailVo.getSampleCode() + str1 + "、");
            }
        }
        for (String str2 : setOutwarde) {
            if (reportSampleDetailVo.getOutward() == null) {
                reportSampleDetailVo.setOutward(str2 + "、");
            } else {
                reportSampleDetailVo.setOutward(reportSampleDetailVo.getOutward() + str2 + "、");
            }

        }
        for (String str3 : setSpecs) {
            if (reportSampleDetailVo.getSpecs() == null) {
                reportSampleDetailVo.setSpecs(str3 + "、");
            } else {
                reportSampleDetailVo.setSpecs(reportSampleDetailVo.getSpecs() + str3 + "、");
            }
        }
        for (String str4 : setStandard) {
            if (reportSampleDetailVo.getStandard() == null) {
                reportSampleDetailVo.setStandard(str4 + "、");
            } else {
                reportSampleDetailVo.setStandard(reportSampleDetailVo.getStandard() + str4 + "、");
            }

        }
        // 获取检测项
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemListByRecordId(recordId,taskId);
        reportSampleDetailVo.setCheckItems(checkItemList);
        return reportSampleDetailVo;
    }

    @Override
    public ReportDetailVo getReportDetail1(Long id) {
        return reportMapper.getReportDetail1(id);
    }

    @Override
    public ReportDetailVo getReportDetail(Long taskId) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        ReportDetailVo reportDetail = reportMapper.getReportDetail(taskId, userTeamIds);
        //兼容中间报告
        if (reportDetail == null){
            reportDetail = reportMapper.getReportDetailZj(taskId, userTeamIds);
        }
        if (reportDetail != null){
            List<ReportSampleDetailVo> samples = reportDetail.getSamples();
            for (ReportSampleDetailVo reportSampleDetailVo : samples) {
                List<ReportCheckItemDetailVo> checkItems = reportSampleDetailVo.getCheckItems();
                for (int j = 0; j < checkItems.size(); j++) {
                    ReportCheckItemDetailVo reportCheckItemDetailVo = checkItems.get(j);
                    int last = testProductDao.isLast(reportCheckItemDetailVo.getCheckItemId().intValue());
                    if (last > 0) {
                        checkItems.remove(reportCheckItemDetailVo);
                    }
                }
                reportSampleDetailVo.setCheckItems(checkItems);
            }
            reportDetail.setSamples(samples);
        }
        return reportDetail;
    }

    @Override
    public ReportDetailVo getReportDetail0620(Long taskId) {
        Long recordId = recordEntityMapper.getRecordId(taskId);
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        ReportDetailVo reportDetail = reportMapper.getReportDetail(taskId, userTeamIds);
        //兼容中间报告
        if (reportDetail == null){
            reportDetail = reportMapper.getReportDetailZj(taskId, userTeamIds);
        }
        if (reportDetail != null){
            List<ReportSampleDetailVo> samples = reportDetail.getSamples();
            //处理每组样品下检测项
            StringBuilder sampleName = new StringBuilder();
            int i = 0;
            for (ReportSampleDetailVo reportSampleDetailVo : samples) {
                List<ReportCheckItemDetailVo> result = Lists.newArrayList();
                List<SampleItemEntity> temp = Lists.newArrayList();
                List<ReportCheckItemDetailVo> checkItems = reportSampleDetailVo.getCheckItems();
                //查询子级检测项信息
                for (int j = 0; j < checkItems.size(); j++) {
                    ReportCheckItemDetailVo reportCheckItemDetailVo = checkItems.get(j);
                    int last = testProductDao.isLast(reportCheckItemDetailVo.getCheckItemId().intValue());
                    if (last > 0) {
                        //移除有子检测项的父检测项
                        checkItems.remove(reportCheckItemDetailVo);
                        //查询该父检测项下的子检测项信息
                        List<SampleItemEntity> nodeItems = entrustEntityMapper.getItemRecursionList(reportCheckItemDetailVo.getCheckItemId());
                        List<SampleItemEntity> tempNodeItems = Lists.newArrayList();
                        //将父级原始记录传递给子级
                        for (SampleItemEntity nodeItem : nodeItems) {
                            nodeItem.setOriginUrl(reportCheckItemDetailVo.getOriginUrl());
                            nodeItem.setSampleId(Integer.parseInt(reportSampleDetailVo.getSampleId()+""));
                            tempNodeItems.add(nodeItem);
                        }
                        temp.addAll(tempNodeItems);
                    }
                }
                //拼接父检测项名称和子检测项名称
                HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
                if(!CollectionUtils.isEmpty(temp)){
                    for (SampleItemEntity entity0 : temp) {
                        itemMap.put(entity0.getCheckItemId(), entity0);
                    }
                    for (SampleItemEntity entity2 : temp) {
                        SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                        if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                            entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                        }
                    }
                }
                if(!CollectionUtils.isEmpty(temp)){
                    for (SampleItemEntity sampleItemEntity : temp) {
                        //去除父检测项
                        int last = testProductDao.isLast(sampleItemEntity.getCheckItemId().intValue());
                        if (last > 0) {
                            continue;
                        }
                        ReportCheckItemDetailVo vo = new ReportCheckItemDetailVo();
                        ReportRecordDetailEntity entity = recordDetailEntityMapper.selectByRecordIdAndItemId(recordId, sampleItemEntity.getCheckItemId().intValue(),Integer.parseInt(sampleItemEntity.getSampleId()+""));
                        if(recordId != null && entity != null){
                            vo.setCheckItemId(entity.getCheckItemId());
                            vo.setCheckItemName(entity.getCheckItemName());
                            vo.setCoordinate(entity.getCoordinate());
                            vo.setOriginUrl(entity.getOriginUrl());

                            vo.setId(entity.getId());
                            vo.setSpecsContent(entity.getSpecsContent());
                            vo.setCheckResult(entity.getCheckResult());
                            vo.setJudgeResult(entity.getJudgeResult());
                        }else{
                            vo.setCheckItemId(sampleItemEntity.getCheckItemId());
                            vo.setCheckItemName(sampleItemEntity.getCheckItemName());
                            vo.setCoordinate(sampleItemEntity.getCoordinate());
                            vo.setOriginUrl(sampleItemEntity.getOriginUrl());
                        }
                        result.add(vo);
                    }
                }
                if(!CollectionUtils.isEmpty(checkItems)){
                    result.addAll(checkItems);
                }
                reportSampleDetailVo.setCheckItems(result);
                sampleName.append(reportSampleDetailVo.getSampleName());
                if(i != samples.size() -1){
                    sampleName.append("/");
                }
                i++;
            }
            reportDetail.setSampleName(sampleName.toString());
            reportDetail.setSamples(samples);
        }
        return reportDetail;
    }

    @Override
    public ReportHistoryDetailVo getDetailCheckItem(Long id) {
        return reportMapper.getDetailCheckItem(id);
    }

    @Override
    public ReportRecordEntity getDetail(Long id) {
        // 查询报告单信息
        ReportRecordEntity reportDetail = recordEntityMapper.getDetail(id);
        EntrustAddVo entrustData = new EntrustAddVo();
        if (reportDetail.getReportType() == null) {
            // 根据委托单id 获取 委托单信息下 报告方式、领报告人、收件电话、邮寄地址、邮箱
            entrustData = entrustEntityMapper.selectByKeyId(reportDetail.getEntrustmentId());
            // 报告发出方式
            if (entrustData.getReportType() != null && !entrustData.getReportType().equals("")) {
                reportDetail.setReportType(entrustData.getReportType());
            }
            // 领报告人
            if (entrustData.getAddressee() != null && !entrustData.getAddressee().equals("")) {
                reportDetail.setAddressee(entrustData.getAddressee());
            }
            // 收件电话
            if (entrustData.getMobile() != null && !entrustData.getMobile().equals("")) {
                reportDetail.setReportPhone(entrustData.getMobile());
            }
            // 邮寄地址
            if (entrustData.getAddress() != null && !entrustData.getAddress().equals("")) {
                reportDetail.setReportMailingAddress(entrustData.getAddress());
            }
            // 邮箱
            if (entrustData.getMailbox() != null && !entrustData.getMailbox().equals("")) {
                reportDetail.setEmail(entrustData.getMailbox());
            }
        }
        return reportDetail;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveMessage(ReportRecordEntity reportRecordEntity) {
        // 8已邮寄
        reportRecordEntity.setState("8");
        Integer status = entityMapper.updateByPrimaryKeySelective(reportRecordEntity);
        if (status == 1) {
            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(reportRecordEntity.getId());
            if (entrustAddVo.getState() != null && entrustAddVo.getState() < 200) {
                taskMapper.updateEntrustById(entrustAddVo.getId(), 200);
            }
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public Boolean preserve(ReportPreserveVo vo) {
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByEntrustId(vo.getEntrustmentId());
        if (reportRecordEntity1 != null) {
            //处理报告检测项数据
            List<ReportRecordDetailEntity> insertList = Lists.newArrayList();
            List<ReportRecordDetailEntity> updateList = Lists.newArrayList();
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
                e.setTaskId(vo.getTaskId());
                List<Long> checkItemIds = recordDetailEntityMapper.getCheckItemIds(reportRecordEntity1.getId(),vo.getTaskId(),e.getSampleId());
                if (checkItemIds.contains(e.getCheckItemId())) {
                    updateList.add(e);
                } else {
                    insertList.add(e);
                }
            }
            //批量更新
            if(!CollectionUtils.isEmpty(updateList)){
                recordDetailEntityMapper.batchUpdateRecords(updateList);
            }
            //批量插入
            if(!CollectionUtils.isEmpty(insertList)){
                recordDetailEntityMapper.batchInsert(insertList);
            }
            //处理报告数据
            if(vo.getReportComplete() == 2){
                reportRecordEntity1.setState(2+"");
            }else{
                List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
                if(allReportComplete.contains(2)){
                    reportRecordEntity1.setState(2+"");
                }else{
                    reportRecordEntity1.setState(1+"");
                    reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
                    if(reportRecordEntity1.getReportCode() == null){//当前任务单号为空时才会设置新的报告编号
                        reportRecordEntity1.setReportCode(getMaxCode(vo.getEntrustmentId()));
                    }
                }
            }
            //修改任务报告状态
            taskMapper.updateReportStatus(vo.getReportComplete(), vo.getTaskId());
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            //保存报告检测项数据
            long recordId = GenID.getID();
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            List<ReportRecordDetailEntity> tempCheckInfos = Lists.newArrayList();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
                e.setTaskId(vo.getTaskId());
                tempCheckInfos.add(e);
            }
            recordDetailEntityMapper.batchInsert(tempCheckInfos);
            //保存报告数据
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
            if(vo.getReportComplete() == 2){//未完成
                reportRecordEntity.setState(2+"");
            }else{//该任务单已完成，判断有没有其他任务单，和其他任务单状态
                List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
                if(allReportComplete.contains(2)){
                    reportRecordEntity.setState(2+"");
                }else{
                    reportRecordEntity.setState(1+"");
                    reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
                    reportRecordEntity.setReportCode(getMaxCode(vo.getEntrustmentId()));
                }
            }
            reportRecordEntity.setId(recordId);
            //设置为最终报告
            reportRecordEntity.setType(0+"");
            //修改任务报告状态
            taskMapper.updateReportStatus(vo.getReportComplete(), vo.getTaskId());
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    private String getMaxCode(Long entrustId){
        //获取父级code
        Long deptId = taskMapper.getDeptByEntrustId(entrustId);
        String topDepartmentCode = teamMapper.getTopDepartmentCode(deptId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String year = sdf.format(new Date());
        //判断委托编号类型
        String entrustCategoryType = recordEntityMapper.getEntrustCategoryType(entrustId);
        if(entrustCategoryType != null){
            Integer maxCode = recordEntityMapper.getOtherMaxCode(year,topDepartmentCode,entrustCategoryType);
            if(maxCode != null){
                Integer maxCodeMid = recordEntityMapper.getOtherMaxCodeMid(year,topDepartmentCode,entrustCategoryType);
                if(maxCodeMid != null && maxCode < maxCodeMid){
                    maxCode = maxCodeMid;
                }
            }
            if (maxCode == null) {
                return topDepartmentCode+"-" + year + "-"+entrustCategoryType+"-0001";
            } else {
                int newCode = maxCode + 1;
                return topDepartmentCode+"-" + year + "-"+entrustCategoryType+"-" + new DecimalFormat("0000").format(newCode);
            }
        }else{
            Integer maxCode = recordEntityMapper.getMaxCode(year,topDepartmentCode);
            if(maxCode != null){
                Integer maxCodeMid = recordEntityMapper.getMaxCodeMid(year,topDepartmentCode);
                if(maxCodeMid != null && maxCode < maxCodeMid){
                    maxCode = maxCodeMid;
                }
            }
            if (maxCode == null) {
                return topDepartmentCode+"-" + year + "-YC-0001";
            } else {
                int newCode = maxCode + 1;
                return topDepartmentCode+"-" + year + "-YC-" + new DecimalFormat("0000").format(newCode);
            }
        }
    }

    @Transactional
    @Override
    public Boolean middleReportPreserve(ReportPreserveVo vo) {
        //获取父级code
        PageHelper.clearPage();
//        Long deptId = taskMapper.getDeptByEntrustId(vo.getEntrustmentId());
//        String topDepartmentCode = teamMapper.getTopDepartmentCode(deptId);
        long recordId = GenID.getID();
        List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
        List<ReportRecordDetailEntity> tempCheckInfos = Lists.newArrayList();
        for (ReportRecordDetailEntity e : checkInfos) {
            e.setRecordId(recordId);
            e.setId(null);
//            int insert1 = recordDetailEntityMapper.insert(e);
//            if (insert1 < 1) {
//                return false;
//            }
            tempCheckInfos.add(e);
        }
        recordDetailEntityMapper.batchInsert(tempCheckInfos);
        ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo,vo.getEntrustmentId());
        //生成报告编号
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
//        String year = sdf.format(new Date());
//        Integer maxCode = recordEntityMapper.getMaxCode(year,topDepartmentCode);
//        if (maxCode == null) {
//            reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-0001");
//        } else {
//            int newCode = maxCode + 1;
//            reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-" + new DecimalFormat("0000").format(newCode));
//        }
        reportRecordEntity.setReportCode(getMaxCode(vo.getEntrustmentId()));
        reportRecordEntity.setId(recordId);
        reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
        reportRecordEntity.setState(1+"");//报告已合成，设置为待发起审批
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(vo.getEntrustmentId());
        reportRecordEntity.setNumber(entrustAddVo.getReportCount());
        reportRecordEntity.setReportType(entrustAddVo.getReportType());
        reportRecordEntity.setSealType(entrustAddVo.getSealType());
        reportRecordEntity.setEntrustId(vo.getEntrustmentId());
        //设置为中间报告
        reportRecordEntity.setType(1+"");
        int insert = recordEntityMapper.insert(reportRecordEntity);
        if (insert < 1) {
            return false;
        }
        //修改任务流转中间报告的状态和recordId
        TestEntrustedTaskRelEntity relEntity = new TestEntrustedTaskRelEntity();
        relEntity.setId(vo.getTaskFlowId());
        relEntity.setState(1);
        relEntity.setRecordId(recordId);
        int i = taskRelDao.updateMiddleReportState(relEntity);
        if (i < 1) {
            return false;
        }
        return true;
    }

    @Transactional
    @Override
    public Boolean middleReportUpdate(ReportPreserveVo vo) {
        List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
        for (ReportRecordDetailEntity e : checkInfos) {
            recordDetailEntityMapper.updateById(e);
        }
        return true;
    }

    @Override
    public PageInfo sealList(String search, Integer pageNum, Integer pageSize, String reportType, String state,Integer reportTypeStatus) {
        if (StringUtils.isEmpty(state)) {
            state = "1";
        }
        //每个团队看子集大团队任务产生的报告列表
        Long userId = ShiroUtils.getUserInfo().getUserId();
        List<Integer> ids = Lists.newArrayList();
        //校验用户id是否分配团队
        int teamId = testTechnicistDao.getSealer(userId);
        if (teamId > 0) {
            //获取顶级团队
            Long topTeamId = this.getTopDepartment((long) teamId);
            //获取顶级团队下的所有下级团队
            if (topTeamId == null){
                topTeamId = (long)teamId;
            }
            List<TeamTreeStructureEntity> chirds = teamMapper.getChirds(topTeamId);
            for (TeamTreeStructureEntity entity:chirds) {
                ids.add(Integer.valueOf(entity.getId()+""));
            }
        }
        if (ids.size()<=0){
            ids = null;
        }
        PageHelper.startPage(pageNum, pageSize);
        //TODO 兼容中间报告
        List<ReportRecordEntity> list = entityMapper.getSealList(search, reportType, state,reportTypeStatus,ids);
        for (ReportRecordEntity recordEntity:list) {
            if (StringUtils.isNotEmpty(recordEntity.getSealType()) && recordEntity.getSealType().contains("null")){
                recordEntity.setSealType("");
            }
            if ("0".equals(recordEntity.getType())){
                recordEntity.setType("最终报告");
            }else {
                recordEntity.setType("中间报告");
            }
            //TODO 兼容中间报告
            if (recordEntity.getEntrustmentId() == null){
                recordEntity.setEntrustmentId(recordEntity.getEntrustId());
            }
        }
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public ReportRecordEntity getUrlByCode(String reportCode) {
        return entityMapper.getUrlByCode(reportCode);
    }

    @Override
    public Long getEntrustIdByCode(String reportCode) {
        return entityMapper.getEntrustIdByCode(reportCode);
    }

    @Override
    public List<ReportRecordDetailEntity> getReportDetailByCode(String reportCode) {
        return recordDetailEntityMapper.getReportDetailByCode(reportCode);
    }

    @Override
    public List<ReportTemplateEntity> getReportTemplateListOld(String productId) {
        return templateEntityMapper.getReportTemplateListOld(productId);
    }

    @Override
    public List<ReportTemplateEntity> getReportTemplateList(Long id) {
        List<ReportTemplateEntity> result = Lists.newArrayList();
        List<Long> allReportId = entityMapper.getAllReportId(id);
        if (!CollectionUtils.isEmpty(allReportId)) {
            result = templateEntityMapper.getReportTemplateList(allReportId);
        }
        //根据ids获取所属的产品id集合
        List<ReportTemplateEntity> list = null;
        Map<Integer,List<String>> map = new HashedMap();
        if (allReportId.size()>0){
            list = templateEntityMapper.getProductIdsByIds(allReportId);
        }
        if (!CollectionUtils.isEmpty(list)){
            for (ReportTemplateEntity entity:list) {
                if (map.get(entity.getId()) == null){
                    List<String> stringList = Lists.newArrayList();
                    stringList.add(entity.getProductId());
                    map.put(entity.getId(),stringList);
                }else {
                    List<String> strings = map.get(entity.getId());
                    strings.add(entity.getProductId());
                    map.put(entity.getId(),strings);
                }
            }
        }
        for (ReportTemplateEntity bean:result) {
            bean.setProductIds(map.get(bean.getId()));
        }
        return result;
    }

    @Override
    public List<ReportTemplateEntity> getMiddleReportTemplateList(Long id) {
        List<ReportTemplateEntity> result = Lists.newArrayList();
        List<Long> allReportId = entityMapper.getAllMiddleReportId(id);
        if (!CollectionUtils.isEmpty(allReportId)) {
            result = templateEntityMapper.getReportTemplateList(allReportId);
        }
        //根据ids获取所属的产品id集合
        List<ReportTemplateEntity> list = null;
        Map<Integer,List<String>> map = new HashedMap();
        if (allReportId.size()>0){
            list = templateEntityMapper.getProductIdsByIds(allReportId);
        }
        if (!CollectionUtils.isEmpty(list)){
            for (ReportTemplateEntity entity:list) {
                if (map.get(entity.getId()) == null){
                    List<String> stringList = Lists.newArrayList();
                    stringList.add(entity.getProductId());
                    map.put(entity.getId(),stringList);
                }else {
                    List<String> strings = map.get(entity.getId());
                    strings.add(entity.getProductId());
                    map.put(entity.getId(),strings);
                }
            }
        }
        for (ReportTemplateEntity bean:result) {
            bean.setProductIds(map.get(bean.getId()));
        }
        return result;
    }
    @Override
    public List<ReportProductRelVo> getReportTemplateList0706(Long id,Long recordId) {
        //用于中间报告去除多余模板
        List<Integer> sampleIds = Lists.newArrayList();
        if(recordId != null){
//            sampleIds = recordDetailEntityMapper.getSampleIds(recordId);
            List<ReportRecordDetailEntity> checkInfos = recordDetailEntityMapper.getCheckInfoByRecordId(recordId);
            for (int i = 0; i < checkInfos.size(); i++) {
                ReportRecordDetailEntity reportRecordDetailEntity = checkInfos.get(i);
                String checkResult = reportRecordDetailEntity.getCheckResult();
                Integer sampleId = reportRecordDetailEntity.getSampleId();
                if(checkResult != null && !sampleIds.contains(sampleId)){
                    sampleIds.add(sampleId);
                }
            }
        }
        if(CollectionUtils.isEmpty(sampleIds)){
            sampleIds = null;
        }
        List<ReportProductRelVo> result = templateEntityMapper.getSampleIdByEntrust(id,sampleIds);
        if(!CollectionUtils.isEmpty(result)){
            for (ReportProductRelVo reportProductRelVo : result) {
                List<ReportTemplateEntity> reportTemplateList0706 = templateEntityMapper.getReportTemplateList0706(id, reportProductRelVo.getSampleId());
                reportProductRelVo.setReportTemplates(reportTemplateList0706);
            }
        }
        return result;
    }

    @Override
    public ReportRecordEntity selectByEntrustId(Long entrustId) {
        return recordEntityMapper.selectByEntrustId(entrustId);
    }

    @Override
    public List<ReportRecordDetailEntity> getCheckInfoByRecordId(Long recordId) {
        return recordDetailEntityMapper.getCheckInfoByRecordId(recordId);
    }

    @Override
    public PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize, String type,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSendList(search, reportType, type,reportTypeStatus);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo getSendList0623(String search, String reportType, Integer pageNum, Integer pageSize, String type,String category,Integer reportTypeStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSendList0623(search, reportType, type,category,reportTypeStatus);
        if(!CollectionUtils.isEmpty(list)){
            for(ReportRecordEntity reportRecordEntity :list){
                // 判断收件人为 null 则根据委托单位查询
                if(org.springframework.util.StringUtils.isEmpty(reportRecordEntity.getAddressee()) &&
                        !org.springframework.util.StringUtils.isEmpty(reportRecordEntity.getEntrustCompany())){
                    HistoryEntrustDataVo historyEntrustDataVo = entrustEntityMapper.getContactWayData(reportRecordEntity.getEntrustCompany());
                    if(!org.springframework.util.StringUtils.isEmpty(historyEntrustDataVo)){
                        reportRecordEntity.setReportMailingAddress(historyEntrustDataVo.getAddress());
                        reportRecordEntity.setAddressee(historyEntrustDataVo.getAddressee());
                        reportRecordEntity.setReportPhone(historyEntrustDataVo.getMobile());
                    }
                }
            }
        }
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Boolean isApprove(Long id) {
        String approve = entityMapper.isApprove(id);
        if ("1".equals(approve)) {
            return true;
        }
        return false;
    }

    @Override
    public String getJudgeBasis(Long id,Integer sampleId) {
        StringBuilder result = new StringBuilder("");
        List<String> judgeBasis = reportMapper.getJudgeBasis(id,sampleId);
        if (!CollectionUtils.isEmpty(judgeBasis)) {
            for (int i = 0; i < judgeBasis.size(); i++) {
                result.append(judgeBasis.get(i));
                if (judgeBasis.size() - 1 != i) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    public String getJudgeBasisRe(Long id,Integer sampleId) {
        StringBuilder result = new StringBuilder("");
        List<String> judgeBasis = reportMapper.getJudgeBasisRe(id,sampleId);
        if (!CollectionUtils.isEmpty(judgeBasis)) {
            for (int i = 0; i < judgeBasis.size(); i++) {
                result.append(judgeBasis.get(i));
                if (judgeBasis.size() - 1 != i) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    @Override
    public String getCheckBasis(Long id,Integer sampleId) {
        StringBuilder result = new StringBuilder("");
        List<String> checkBasis = reportMapper.getCheckBasis(id,sampleId);
        if (!CollectionUtils.isEmpty(checkBasis)) {
            for (int i = 0; i < checkBasis.size(); i++) {
                result.append(checkBasis.get(i));
                if (checkBasis.size() - 1 != i) {
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    @Override
    public String getEquipment(Long id,Integer sampleId) {
        StringBuilder result = new StringBuilder("");
        List<String> equipment = reportMapper.getEquipment(id,sampleId);
        List<String> list = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(equipment)){
            list=equipment.stream().distinct().collect(Collectors.toList());
        }
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                result.append(list.get(i));
                if (list.size() - 1 != i) {
                    result.append("、");
                }
            }
        }
        return result.toString();
    }

    @Override
    public int updateReportUrl(Long id, String url, String code) {
        return reportMapper.updateReportUrl(id, url, code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadReport(String reportCode, MultipartFile file, String verifyer,
                                String issuer, Long verifyerId, Long issuerId, String code,
                                String conclusion,String additional,String mixInfo,String type,String inspector,String reportType) {
        //解析code
        Map<String,List<String>> map = JSON.parseObject(code, Map.class);
        List<ParamEntity> entities = Lists.newArrayList();
        Boolean flag = false;
        String url = "";
        if (file == null){
            //下载模板填充数据
            MinioClient client = MinIoUtil.minioClient;
            try {
                for (String s:map.keySet()) {
                    List<String> list = map.get(s);
                    for (String ss:list) {
                        ParamEntity paramEntity = new ParamEntity();
                        paramEntity.setSampleId(Integer.parseInt(s));
                        paramEntity.setUrl(ss);
                        entities.add(paramEntity);
                    }
                }
                List<String> conclusions = JSONArray.parseArray(conclusion, String.class);
                List<String> additionals = JSONArray.parseArray(additional, String.class);
                List<ConclusionEntity> list = Lists.newArrayList();
                for (int i=0;i<entities.size();i++) {
                    ConclusionEntity conclusionEntity = new ConclusionEntity();
                    conclusionEntity.setUrl(entities.get(i).getUrl());
                    conclusionEntity.setSampleId(entities.get(i).getSampleId());
                    conclusionEntity.setConclusion(conclusions.get(i));
                    conclusionEntity.setAdditional(additionals.get(i));
                    list.add(conclusionEntity);
                }
                ReportResBean resBean = null;
                if ("配合比".equals(type)){
                    TestSampleMixInfoEntity mixInfoEntity = JSON.parseObject(mixInfo,TestSampleMixInfoEntity.class);
                    resBean = this.submitDownLoadMix(client, list, Long.parseLong(reportCode),mixInfoEntity,reportType);
                    url = resBean.getUrl();
                }else {
                    resBean = this.submitDownLoad(client, list, Long.parseLong(reportCode),reportType);
                    url = resBean.getUrl();
                }
                flag = true;
            }catch (Exception e){
                logger.error("提交报告审批失败:{}",e);
                return false;
            }
        }else {
            try {
                //如果上传的是excel转为pdf
                String originalFilename = file.getOriginalFilename();
                boolean b = false;
                if (originalFilename.contains(".xls") || originalFilename.contains(".xlsx") || originalFilename.contains(".pdf")){
                    b = true;
                }
                if (!b){
                    return false;
                }
                if (originalFilename.contains(".pdf")){
                    url = MinIoUtil.upload("report-download", file, GenID.getID() + ".pdf");
                    flag = true;
                }else {
                    InputStream inputStream = file.getInputStream();
                    //excel转pdf
                    ByteArrayOutputStream byteArrayOutputStream = PDFHelper3.excel2pdf2(inputStream, qiYueSuoEntity.getAutographPath() + GenID.getID() + ".pdf");
                    InputStream inputStream1 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    url = MinIoUtil.upload("report-download", GenID.getID() + ".pdf", inputStream1, "application/octet-stream");
                    flag = true;
                }
            } catch (Exception e) {
                logger.info("提交审批中上传文件失败:{}",e);
                return false;
            }
        }
        //转为pdf
        if (!url.contains(".pdf")){
            MinioClient client = MinIoUtil.minioClient;
            String[] split = url.split("\\?");
            String[] strings = split[0].split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            try {
                client.statObject(bluckName, fileName);
                InputStream object = client.getObject(bluckName, fileName);
                //相应pdf
                ByteArrayOutputStream b1 = PDFHelper3.excel2pdf2(object,qiYueSuoEntity.getAutographPath()+GenID.getID()+".pdf");
                InputStream inputStream = FileAndFolderUtil.parseOut(b1);
                url = MinIoUtil.upload("report-download", reportCode + ".pdf", inputStream, "application/octet-stream");
            }catch (Exception e){
                logger.error("Excel转pdf异常");
            }
        }
        //更新签名
        Long along = recordEntityMapper.checkExist(Long.parseLong(reportCode),reportType);
        //TODO (报告) 兼容中间报告
        if (along == null){
            reportMapper.updateVerAndIssZj(reportCode, verifyer, issuer, verifyerId,new Date(System.currentTimeMillis()), issuerId);
        }else {
            reportMapper.updateVerAndIss(reportCode, verifyer, issuer, verifyerId,new Date(System.currentTimeMillis()), issuerId);
        }
        //设置签名信息
        String url1 = "";
        try {
            //TODO (报告) 兼容中间报告
            url1 = insertPicToPdf(url,Long.parseLong(reportCode),inspector,reportType);
            logger.info("设置签名信息：{}",url1);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(url1)){
                url = url1;
            }
        }catch (Exception e){
            logger.error("报告签名失败:{}",e);
        }
        if (url.contains("?")){
           url = url.substring(0,url.indexOf("?"));
        }
        //TODO (报告) 兼容中间报告
        if (along == null){
            reportMapper.updateUrlZj(reportCode, url, verifyer, issuer, verifyerId, issuerId,new Date(),ShiroUtils.getUserInfo().getName());
        }else {
            reportMapper.updateUrl(reportCode, url, verifyer, issuer, verifyerId, issuerId,new Date(),ShiroUtils.getUserInfo().getName());
        }
        logger.info("签名信息更新成功！:{}",reportCode+":"+url);
        //更新配合比信息
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mixInfo)){
            TestSampleMixInfoEntity entity = JSON.parseObject(mixInfo,TestSampleMixInfoEntity.class);
            //TODO (报告) 兼容中间报告
            mixInfoEntityMapper.updateByEntrustId(reportCode,entity);
        }
        return flag;
    }

    @Override
    public ReportRecordEntity getDetailByEntrustId(Long entrustId) {
        return reportMapper.getDetailByEntrustId(entrustId);
    }

    @Override
    public ReportRecordEntity getDetailByEntrustIdZj(Long entrustId) {
        return reportMapper.getDetailByEntrustIdZj(entrustId);
    }

    @Override
    public String downLoadByCheckItemId(MinioClient client, String code, Long id) throws Exception {
        String[] split = code.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        XWPFDocument doc = null;
        client.statObject(bluckName, fileName);
        InputStream object = client.getObject(bluckName, fileName);
        doc = new XWPFDocument(object);
        //写入数据
        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
        List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
        return null;
    }

    @Override
    public Boolean seal(Long entrustId, String title, String fileType,String reportType) {
        //step1 根据文件类型创建合同文档
        String url = "";
        try {
            //url = downLoad(client,code,entrustId);
            url = reportMapper.getUrlByEntrustId(entrustId);
            //TODO 兼容中间报告
            if (StringUtils.isEmpty(url)){
                url = reportMapper.getUrlByZjEntrustId(entrustId);
            }
        } catch (Exception e) {
            logger.error("盖章下载报告文件失败:{}", e);
            return false;
        }
        if (StringUtils.isNotEmpty(url)) {
            File file = null;
            try {
                String uri = "";
                if (url.contains("?")){
                    uri = url.substring(0,url.indexOf("?"));
                }else {
                    uri = url;
                }
                file = FileAndFolderUtil.getFile(uri);
            } catch (Exception e) {
                logger.error("将报告地址转为File文件失败:{}", e);
                return false;
            }
            if (file != null) {
                QiYueSuoResponse response = qiYueSuoHnadler.creatFile(file, title, fileType, null, null, null);
                if (response != null && response.getCode() == 0) {
                    //根据委托id存储文档id
                    List<QiYueSuoDocment> result = response.getResult();
                    //TODO 兼容中间报告
                    Long aLong = entityMapper.checkExist(entrustId,reportType);
                    if (aLong == null){
                        entityMapper.updateDocIdAndStateZj(entrustId, result.get(0).getDocumentId(), "2");
                    }else {
                        entityMapper.updateDocIdAndState(entrustId, result.get(0).getDocumentId(), "2");
                    }
                    entityMapper.updateSeal(result.get(0).getDocumentId());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public QiYueSuoResponse createbycategory(QiYueSuoReqBean reqBean) {
        //设置文档标识
        List<ReportRecordEntity> entity = Lists.newArrayList();
        //TODO 兼容中间报告
        Long aLong = entityMapper.checkExist(reqBean.getEntrustId(),reqBean.getReportType());
        if (aLong == null){
            entity = entityMapper.selectMessageByZjEntrustId(reqBean.getEntrustId());
        }else {
            entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        }
        List<String> docs = new ArrayList<>();
        docs.add(entity.get(0).getQysDocmentId());
        reqBean.setDocuments(docs);
        QiYueSuoResponse response = qiYueSuoHnadler.createbycategory(reqBean);
        //根据委托id存储文档id
        //TODO 兼容中间报告
        if (aLong == null){
            entityMapper.updateContractIdAndStateZj(reqBean.getEntrustId(), response.getContractId(), "3");
        }else {
            entityMapper.updateContractIdAndState(reqBean.getEntrustId(), response.getContractId(), "3");
        }
        return response;
    }

    @Override
    public QiYueSuoResponse signurl(QiYueSuoSeaLBean reqBean) {
        //设置合同标识
        List<ReportRecordEntity> entity = Lists.newArrayList();
        //TODO 兼容中间报告
        Long aLong = entityMapper.checkExist(reqBean.getEntrustId(),reqBean.getReportType());
        if (aLong == null){
            entity = entityMapper.selectMessageByZjEntrustId(reqBean.getEntrustId());
        }else {
            entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        }
        reqBean.setContractId(Long.valueOf(entity.get(0).getContractId()));
        QiYueSuoResponse response = qiYueSuoHnadler.signurl(reqBean);
        //根据委托更新报告签署url
        //设置盖章人和盖章时间
        Long userId = ShiroUtils.getUserInfo().getUserId();
        String sysUserName = sysUserDao.getSysUserName(userId);
        //TODO 兼容中间报告
        if (aLong == null){
            entityMapper.updateUrlAndStateZj(reqBean.getEntrustId(), response.getSignUrl(), "4",sysUserName+"&"+userId+"",new Date(System.currentTimeMillis()));
        }else {
            entityMapper.updateUrlAndState(reqBean.getEntrustId(), response.getSignUrl(), "4",sysUserName+"&"+userId+"",new Date(System.currentTimeMillis()));
        }
        return response;
    }

    @Override
    public byte[] downloadQysFile(Long enstustId, Long contractId, String name, String contact) {
        byte[] inputStream = qiYueSuoHnadler.downloadQysFile(contractId, name, contact);
        //entityMapper.updateState(enstustId,"6");
        return inputStream;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callback(Long contractId) {
        Long entrustId = entityMapper.getEntrustIdByCid(contractId);
        Long id = entityMapper.getIdByCid(contractId);
        //更新状态，更新
        if (entrustId == null){
            Long idByCid = entityMapper.getEntrustByCid(contractId);
            taskMapper.updateEntrustById(idByCid, 10);
        }else {
            taskMapper.updateEntrustById(entrustId, 10);
        }
        //更新报告状态
        entityMapper.updateFileState(contractId, "5");
        //移除中间报告
        moveReportRecord(id);
        logger.debug("接收契约锁回调参数进行数据更新完成！");
    }

    @Override
    public QiYueSuoResponse deptList(String tenantType, String companyName) {
        return qiYueSuoHnadler.getDeptListOfQYS(tenantType, companyName);
    }

    @Override
    public QiYueSuoResponse sealListOfQys(String category, String companyName, String sealType) {
        QiYueSuoResponse qiYueSuoResponse = qiYueSuoHnadler.sealList(category, companyName);
        //根据sealType过来qiYueSuoResponse
        List<QiYueSuoSealEntity> newList = Lists.newArrayList();
        String[] split = sealType.split(",");
        List<QiYueSuoSealEntity> list = qiYueSuoResponse.getList();
        for (QiYueSuoSealEntity qiYueSuoSealEntity : list) {
            for (String s : split) {
                if (qiYueSuoSealEntity.getName().contains(s)) {
                    newList.add(qiYueSuoSealEntity);
                }
            }
            //检验检测专用章（室内试验）、检验检测专用章（外业检测）作为通用章返回
            if (qiYueSuoSealEntity.getName().equals("检验检测专用章（室内试验）")
                    || qiYueSuoSealEntity.getName().equals("检验检测专用章（外业检测）")) {
                newList.add(qiYueSuoSealEntity);
            }
        }
        qiYueSuoResponse.setList(newList);
        return qiYueSuoResponse;
    }

    @Override
    public List<QuotaRes> getQuota(Long taskId) {
        Map<String, List<QuotaEntity>> map = new HashMap<>();
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        ReportDetailVo reportDetail = reportMapper.getReportDetail(taskId, userTeamIds);
        //兼容中间报告
        if (reportDetail == null){
            reportDetail = reportMapper.getReportDetailZj(taskId, userTeamIds);
        }
        List<QuotaRes> lis = Lists.newArrayList();
        if (reportDetail != null){
            Set<Long> ids = new HashSet<>();
            for (ReportSampleDetailVo bean : reportDetail.getSamples()) {
                //获取检测id
                List<ReportCheckItemDetailVo> checkItems = bean.getCheckItems();
                for (ReportCheckItemDetailVo detailVo : checkItems) {
                    ids.add(detailVo.getCheckItemId());
                }
            }
            //获取检测项下的子检测项
            List<Long> itemIds = itemDao.getChirldsByIds(ids);
            List<QuotaEntity> list = dao.getListById(itemIds);
            for (QuotaEntity bean : list) {
                if (map.get(bean.getConditionValue()) == null) {
                    List<QuotaEntity> quotaEntityList = Lists.newArrayList();
                    QuotaEntity quotaEntity = new QuotaEntity();
                    quotaEntity.setCheckItemId(bean.getCheckItemId());
                    quotaEntity.setSpecsContent(bean.getSpecsContent());
                    quotaEntityList.add(quotaEntity);
                    map.put(bean.getConditionValue(), quotaEntityList);
                } else {
                    List<QuotaEntity> entities = map.get(bean.getConditionValue());
                    QuotaEntity quota = new QuotaEntity();
                    quota.setCheckItemId(bean.getCheckItemId());
                    quota.setSpecsContent(bean.getSpecsContent());
                    entities.add(quota);
                    map.put(bean.getConditionValue(), entities);
                }
            }
            //处理map
            Set<String> set = map.keySet();
            for (String key : set) {
                QuotaRes res = new QuotaRes();
                res.setKey(key);
                res.setValue(map.get(key));
                lis.add(res);
            }
        }
        return lis;
    }

    @SneakyThrows
    @Override
    public ReportResBean submitDownLoad(MinioClient client, List<ConclusionEntity> list, Long id,String reportType) {
        int totalPageNew = 0;
        Map<Integer, Integer> countMap = new LinkedHashMap();
        Map<Integer, Workbook> map = new LinkedHashMap<>();
        //处理坐标提示信息
        ReportResBean resBean = new ReportResBean();
        Map<String,String> mesMap = new HashedMap();
        ReportRecordEntity reportRecordEntity = null;
        EntrustAddVo entrustHistoryDetail = null;
        //存放表头信息
        entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
        SampleEntity sampleEntity = null;
        //TODO 兼容中间报告
        Long aLong = recordEntityMapper.checkExist(id,reportType);
        if (aLong == null){
            reportRecordEntity = selectByEntrustIdZj(id);
        }else {
            reportRecordEntity = selectByEntrustId(id);
        }
        int index = 1;
        for (ConclusionEntity conclusionEntity:list) {
            for (SampleEntity entity:entrustHistoryDetail.getSamples()) {
                if (conclusionEntity.getSampleId() == entity.getId()){
                    sampleEntity = entity;
                }
            }
            String[] strings = conclusionEntity.getUrl().split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            Workbook doc = null;
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            doc = new Workbook(object);
            totalPageNew = doc.getWorksheets().getCount()+totalPageNew;
            logger.debug("报告页数:{}",totalPageNew);
            //写入数据
            List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
                int size = doc.getWorksheets().getCount();
                countMap.put(index,size);
                //获取表格信息
                for (int i = 0;i<size;i++){
                    Worksheet worksheet = doc.getWorksheets().get(i);
                    Cells cells = worksheet.getCells();
                    int maxRow = cells.getMaxRow();
                    int column = cells.getMaxColumn();
                    if (i == 0) {
                        //检测依据
                        String checkBasis = getCheckBasis(id,sampleEntity.getId());
                        //判定依据
                        String judgeBasis = getJudgeBasis(id,sampleEntity.getId());
                        //根据委托单id，查询委托任务下实验开始的时间和实验结束的时间
                        Date start = taskMapper.getStartTime(id);
                        Date end = taskMapper.getEndTime(id);
                        String s = "";
                        String e = "";
                        if (start == null){
                            s = DateUtil.formatDate(new Date(System.currentTimeMillis()));
                        }else {
                            s = DateUtil.formatDate(start);
                        }
                        if (end == null){
                            e = DateUtil.formatDate(new Date(System.currentTimeMillis()));
                        }else {
                            e = DateUtil.formatDate(end);
                        }
                        //主要仪器
                        String equipment = getEquipment(id,sampleEntity.getId());

                        for (int n=0;n<maxRow;n++) {
                            for (int j=0;j<column;j++) {
                                Cell cell = cells.get(n, j);
                                if (cell != null){
                                    Object value = cell.getValue();
                                    if (value != null){
                                        String string = value.toString();
                                        if ("${检测单位名称}".equals(string)){
                                            cells.get(n,j).setValue("检测单位名称：河南省公路工程试验检测中心有限公司");
                                        }
                                        if ("${报告编号}".equals(string)){
                                            cells.get(n,j).setValue(reportRecordEntity.getReportCode());
                                        }
                                        if ("${委托单位}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getEntrustCompany())?"——":entrustHistoryDetail.getEntrustCompany());
                                        }
                                        if ("${工程名称}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectName())?"——":entrustHistoryDetail.getProjectName());
                                        }
                                        if ("${工程部位}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectPart())?"——":entrustHistoryDetail.getProjectPart());
                                        }
                                        if ("${样品信息}".equals(string)){
                                            cells.get(n,j).setValue("样品名称：" + (sampleEntity.getAliasName() == null ? "——" : sampleEntity.getAliasName())
                                                    + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode().replace("~","~"))
                                                    + "；样品数量：" + (sampleEntity.getSampleQuantity() == null ? "——" : sampleEntity.getSampleQuantity())
                                                    + "；代表批量：" + (sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration())
                                                    + "；规格等级：" + (sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs())
                                                    + "；样品状态：" + (StringUtils.isEmpty(sampleEntity.getOutwardDescribe()) ? "——" : sampleEntity.getOutwardDescribe())
                                                    + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                                        }
                                        if ("${检测依据}".equals(string)){
                                            cells.get(n,j).setValue(checkBasis.equals("") ? "——" : checkBasis);
                                        }
                                        if ("${判定依据}".equals(string)){
                                            cells.get(n,j).setValue(judgeBasis.equals("") ? "——" : judgeBasis);
                                        }
                                        if ("${检测日期}".equals(string)){
                                            if (s != null && e != null){
                                                if (s.equals(e)){
                                                    cells.get(n,j).setValue(s);
                                                }else {
                                                    cells.get(n,j).setValue(s + "~" + e);
                                                }
                                            }
                                        }
                                        if ("${仪器设备}".equals(string)){
                                            cells.get(n,j).setValue(equipment.equals("") ? "——" : equipment);
                                        }
                                        if ("${委托编号}".equals(string)){
                                            //委托编号
                                            cells.get(n,j).setValue(entrustHistoryDetail.getEntrustmentNo() + "");
                                        }
                                        if ("${检测类别}".equals(string)){
                                            //检测类别
                                            cells.get(n,j).setValue(entrustHistoryDetail.getCheckPurpose());
                                        }
                                        if ("${批号}".equals(string)){
                                            //批号
                                            cells.get(n,j).setValue(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                                        }
                                        if ("${生产厂家}".equals(string)){
                                            //生产厂家
                                            cells.get(n,j).setValue(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                                        }
                                        if ("${规格}".equals(string)){
                                            //规格等级
                                            cells.get(n,j).setValue(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                                        }
                                        if ("${代表数量}".equals(string)){
                                            //代表数量
                                            cells.get(n,j).setValue(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //过滤每个报告模板的检测项
                    List<ReportRecordDetailEntity> entities = Lists.newArrayList();
                    List<ReportRecordDetailEntity> entities1 = Lists.newArrayList();
                    String[] strings11 = conclusionEntity.getUrl().split("\\/");
                    String fileName11 = strings11[4];
                    List<Long> longList = itemDao.getItemsByTemplateLikeUrl(fileName11);
                    for (ReportRecordDetailEntity entity:checkItemList) {
                        for (Long itemId:longList) {
                            if (entity.getCheckItemId().equals(itemId)){
                                entities.add(entity);
                            }
                        }
                    }
                    //过滤每组样品的检测项(根据委托单id和样品id)
                    List<ReportRecordDetailEntity> list1 = entrustEntityMapper.getItemIdByEntrustIdAndSampleId(id,conclusionEntity.getSampleId());
                    //获取每组样品检测项生成到报告上的参数
                    for (ReportRecordDetailEntity entity:entities) {
                        for (ReportRecordDetailEntity itemId:list1) {
                            if (entity.getCheckItemId().equals(itemId.getCheckItemId())){
                                entities1.add(itemId);
                            }
                        }
                    }
                    //存放检测数据checkItemList为该报告模板所属的检测项
                    for (ReportRecordDetailEntity item : entities1) {
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(item.getCoordinate())){
                            int last = testProductDao.isLast(item.getCheckItemId().intValue());
                            //技术指标
                            String specsContent = "";
                            //检测结果
                            String checkResult = "";
                            //判定结果
                            String judgeResult = "";
                            if (last == 0) {
                                try {
                                    //1A1,1B2,1C3&1D1根据坐标填充数据 TODO
                                    specsContent = item.getCoordinate().split(",")[0];
                                    checkResult = item.getCoordinate().split(",")[1];
                                    judgeResult = item.getCoordinate().split(",")[2];
                                }catch (Exception e){
                                    mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标格式错误");
                                    logger.error("检测项在报告中的坐标格式错误:{}",e);
                                    continue;
                                }
                                try {
                                    String[] specs = specsContent.split("&");
                                    String[] checks = checkResult.split("&");
                                    String[] judes = judgeResult.split("&");
                                    for (String spec:specs) {
                                        if (size > 1){
                                            if (StringUtils.isNotEmpty(specsContent) && Integer.parseInt(spec.substring(0, 1)) == i+1){
                                                cells.get(spec.substring(1)).setValue(StringUtils.isEmpty(item.getSpecsContent())?"——":item.getSpecsContent());
                                            }
                                        }else {
                                            if (StringUtils.isNotEmpty(specsContent)){
                                                cells.get(spec).setValue(StringUtils.isEmpty(item.getSpecsContent())?"——":item.getSpecsContent());
                                            }
                                        }
                                    }
                                    for (String check:checks) {
                                        if (size > 1){
                                            if (StringUtils.isNotEmpty(checkResult) && Integer.parseInt(check.substring(0,1)) == i+1){
                                                cells.get(check.substring(1)).setValue(StringUtils.isEmpty(item.getCheckResult())?"——":item.getCheckResult());
                                            }
                                        }else {
                                            if (StringUtils.isNotEmpty(checkResult)){
                                                cells.get(check).setValue(StringUtils.isEmpty(item.getCheckResult())?"——":item.getCheckResult());
                                            }
                                        }
                                    }
                                    for (String jude:judes) {
                                        if (size > 1){
                                            if (StringUtils.isNotEmpty(judgeResult) && Integer.parseInt(jude.substring(0,1)) == i+1){
                                                cells.get(jude.substring(1)).setValue(StringUtils.isEmpty(item.getJudgeResult())?"——":item.getJudgeResult());
                                            }
                                        }else {
                                            if (StringUtils.isNotEmpty(judgeResult)){
                                                cells.get(jude).setValue(StringUtils.isEmpty(item.getJudgeResult())?"——":item.getJudgeResult());
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    logger.error("检测项在报告中的坐标错误:{}",e);
                                    System.out.println("======="+JSON.toJSONString(e));
                                    continue;
                                }
                            }
                        }else {
                            mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标未录入");
                        }
                    }
                    //处理附加声明和检测结论
                    if (i==size-1){
                        Worksheet worksheet1 = doc.getWorksheets().get(size - 1);
                        Cells cells1 = worksheet1.getCells();
                        int maxRow1 = cells.getMaxRow();
                        int column1 = cells.getMaxColumn();
                        for (int n=0;n<maxRow1;n++) {
                            for (int j = 0; j < column1; j++) {
                                Cell cell = cells1.get(n, j);
                                if (cell != null) {
                                    Object value = cell.getValue();
                                    if (value != null) {
                                        String string = value.toString();
                                        if ("${检测结论}".equals(string)) {
                                            cells.get(n, j).setValue("检测结论："+conclusionEntity.getConclusion());
                                        }
                                        if ("${附加声明}".equals(string)){
                                            cells.get(n, j).setValue("附加声明："+conclusionEntity.getAdditional());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //按照顺序存放doc
                map.put(index,doc);
                index++;
            }
        }
        //存放提示信息
        resBean.setMap(mesMap);
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.xls");
        Workbook topDoc = new Workbook(fileStream);
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        logger.debug("本次报告总页数:{}",totalPageNew);
        setReportTop1(topDoc,entrustAddVo,reportRecordEntity,totalPageNew,entrustHistoryDetail);
        //合并成一个excel
        Workbook document = workbookCopy(topDoc,map);
        //处理页码
        handlerPage(document,countMap);
        //上传合并完成的excel到服务器
        long name = GenID.getID();
        String path = qiYueSuoEntity.getAutographPath()+name+".xlsx";
        document.save(path);
        File file = new File(path);
        MultipartFile fileToMultipart = AsposeUtil.fileToMultipart(file, name + "");
        String url = MinIoUtil.upload("report-download", fileToMultipart, reportRecordEntity.getReportCode() + ".xlsx");
        StringBuilder stringBuilder = new StringBuilder();
        for (ConclusionEntity entity:list) {
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        String name1 = "";
        if (StringUtils.isNotEmpty(stringBuilder.toString())){
            name1 = stringBuilder.toString().substring(0,stringBuilder.length()-2);
        }
        updateReportUrl(reportRecordEntity.getId(), url, name1);
        //存放提示信息
        resBean.setUrl(url);
        FileAndFolderUtil.delete(path);
        return resBean;
    }

    private ReportRecordEntity selectByEntrustIdZj(Long id) {
        return recordEntityMapper.selectByEntrustIdZj(id);
    }

    /**
     * 处理合并完整的excel每个报告的页码
     * @param document
     * @param countMap
     */
    private void handlerPage(Workbook document, Map<Integer, Integer> countMap) {
        //报告总页数
        int total = document.getWorksheets().getCount() - 2;
        //填充每个子报告每页的页码
        Set<Integer> keySet = countMap.keySet();
        int index = 2;
        int count = 1;
        for (Integer page:keySet) {
            //每个报告有几个sheet
            Integer integer = countMap.get(page);
            for (int i =0;i<integer;i++) {
                Worksheet worksheet = document.getWorksheets().get(index);
                Cells cells = worksheet.getCells();
                int maxRow = cells.getMaxRow();
                int column = cells.getMaxColumn();
                for (int n=0;n<maxRow;n++) {
                    for (int j = 0; j < column; j++) {
                        Cell cell = cells.get(n, j);
                        if (cell != null) {
                            Object value = cell.getValue();
                            if (value != null) {
                                String string = value.toString();
                                if ("第 ${page} 页，共 ${total} 页".equals(string)) {
                                    cells.get(n, j).setValue("第"+ count+"页，共"+ total +"页");
                                }
                            }
                        }
                    }
                }
                count++;
                index++;
            }
        }

    }

    @SneakyThrows
    @Override
    public ReportResBean submitDownLoadMix(MinioClient client, List<ConclusionEntity> list, Long id,TestSampleMixInfoEntity mixInfoEntity,String reportType) {
        int totalPageNew = 0;
        Map<Integer, Integer> countMap = new LinkedHashMap();
        Map<Integer,Workbook> map = new HashedMap();
        //处理坐标提示信息
        ReportResBean resBean = new ReportResBean();
        Map<String,String> mesMap = new HashedMap();
        ReportRecordEntity reportRecordEntity = null;
        EntrustAddVo entrustHistoryDetail = null;
        //存放表头信息
        entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
        SampleEntity sampleEntity = null;
        //TODO 兼容中间报告
        Long aLong = recordEntityMapper.checkExist(id,reportType);
        if (aLong == null){
            reportRecordEntity = selectByEntrustIdZj(id);
        }else {
            reportRecordEntity = selectByEntrustId(id);
        }
        int index = 1;
        //配合比实验，设计到原材的报告模板忽略
        List<ConclusionEntity> conclusionEntityList = Lists.newArrayList();
        for (ConclusionEntity entity :list) {
            String[] split = entity.getUrl().split("\\?");
            String[] strings = split[0].split("\\/");
            String fileName = strings[4];
            String type = templateDao.getTypeByUrl(fileName);
            if ("非常规".equals(type)){
                conclusionEntityList.add(entity);
            }
        }
        for (ConclusionEntity conclusionEntity:conclusionEntityList) {
            for (SampleEntity entity:entrustHistoryDetail.getSamples()) {
                if (conclusionEntity.getSampleId() == entity.getId()){
                    sampleEntity = entity;
                }
            }
            String[] strings = conclusionEntity.getUrl().split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            Workbook doc = null;
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            doc = new Workbook(object);
            totalPageNew = doc.getWorksheets().getCount()+totalPageNew;
            //写入数据
            List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
                int size = doc.getWorksheets().getCount();
                countMap.put(index,size);
                //处理表格
                for (int i = 0;i<size;i++){
                    Worksheet worksheet = doc.getWorksheets().get(i);
                    Cells cells = worksheet.getCells();
                    int maxRow = cells.getMaxRow();
                    int column = cells.getMaxColumn();
                    if (i == 0) {
                        //样品信息
                        //检测依据
                        String checkBasis = getCheckBasis(id,sampleEntity.getId());
                        //判定依据
                        String judgeBasis = getJudgeBasis(id,sampleEntity.getId());
                        //根据委托单id，查询委托任务下实验开始的时间和实验结束的时间
                        Date start = taskMapper.getStartTime(id);
                        Date end = taskMapper.getEndTime(id);
                        String e = "";
                        String s = "";
                        if (start == null){
                            s = DateUtil.formatDate(new Date(System.currentTimeMillis()));
                        }else {
                            s = DateUtil.formatDate(start);
                        }
                        if (end == null){
                            e = DateUtil.formatDate(new Date(System.currentTimeMillis()));
                        }else {
                            e = DateUtil.formatDate(end);
                        }
                        //主要仪器
                        String equipment = getEquipment(id,sampleEntity.getId());
                        //如果样品数量超出模板行数,和定位样品信息要插入的位置
                        int insertRow = 0;
                        int indexName = 0;
                        int indexGg = 0;
                        int indexCd = 0;
                        int indexPh = 0;
                        int indexNum = 0;
                        int indexZt =0;
                        int indexBh = 0;

                        for (int n=0;n<maxRow;n++) {
                            for (int j = 0; j < column; j++) {
                                Cell cell = cells.get(n, j);
                                if (cell != null) {
                                    Object value = cell.getValue();
                                    if (value != null) {
                                        String string = value.toString();
                                        if ("${检测单位名称}".equals(string)){
                                            cells.get(n,j).setValue("检测单位名称：河南省公路工程试验检测中心有限公司");
                                        }
                                        if ("${报告编号}".equals(string)){
                                            cells.get(n,j).setValue(reportRecordEntity.getReportCode());
                                        }
                                        if ("${委托单位}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getEntrustCompany())?"——":entrustHistoryDetail.getEntrustCompany());
                                        }
                                        if ("${工程名称}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectName())?"——":entrustHistoryDetail.getProjectName());
                                        }
                                        if ("${工程部位}".equals(string)){
                                            cells.get(n,j).setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectPart())?"——":entrustHistoryDetail.getProjectPart());
                                        }
                                        if ("${样品信息}".equals(string)){
                                            cells.get(n,j).setValue("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                                    + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode().replace("~","~"))
                                                    + "；样品数量：" + (sampleEntity.getSampleQuantity() == null ? "——" : sampleEntity.getSampleQuantity())
                                                    + "；代表批量：" + (sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration())
                                                    + "；规格等级：" + (sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs())
                                                    + "；样品状态：" + (StringUtils.isEmpty(sampleEntity.getOutwardDescribe()) ? "——" : sampleEntity.getOutwardDescribe())
                                                    + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                                        }
                                        if ("${检测依据}".equals(string)){
                                            cells.get(n,j).setValue(checkBasis.equals("") ? "——" : checkBasis);
                                        }
                                        if ("${判定依据}".equals(string)){
                                            cells.get(n,j).setValue(judgeBasis.equals("") ? "——" : judgeBasis);
                                        }
                                        if ("${检测日期}".equals(string)){
                                            if (s != null && e != null){
                                                if (s.equals(e)){
                                                    cells.get(n,j).setValue(s);
                                                }else {
                                                    cells.get(n,j).setValue(s + "~" + e);
                                                }
                                            }
                                        }
                                        if ("${仪器设备}".equals(string)){
                                            cells.get(n,j).setValue(equipment.equals("") ? "——" : equipment);
                                        }
                                        if ("${委托编号}".equals(string)){
                                            //委托编号
                                            cells.get(n,j).setValue(entrustHistoryDetail.getEntrustmentNo() + "");
                                        }
                                        if ("${检测类别}".equals(string)){
                                            //检测类别
                                            cells.get(n,j).setValue(entrustHistoryDetail.getCheckPurpose());
                                        }
                                        if ("${批号}".equals(string)){
                                            //批号
                                            cells.get(n,j).setValue(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                                        }
                                        if ("${生产厂家}".equals(string)){
                                            //生产厂家
                                            cells.get(n,j).setValue(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                                        }
                                        if ("${规格}".equals(string)){
                                            //规格等级
                                            cells.get(n,j).setValue(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                                        }
                                        if ("${代表数量}".equals(string)){
                                            //代表数量
                                            cells.get(n,j).setValue(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                                        }
                                        //设计参数
                                        if (mixInfoEntity != null){
                                            if ("${设计强度}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getDesignStrength() == null ? "——" : mixInfoEntity.getDesignStrength());
                                            }
                                            if ("${配制强度}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getIntensityConfiguration() == null ? "——" : mixInfoEntity.getIntensityConfiguration());
                                            }
                                            if ("${等级}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getAntifreezeLevel() == null ? "——" : mixInfoEntity.getAntifreezeLevel());
                                            }

                                            if ("${水胶比}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getWaterBinderRatio() == null ? "——" : mixInfoEntity.getWaterBinderRatio());
                                            }
                                            if ("${单位用水量}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getUnitWaterUse() == null ? "——" : mixInfoEntity.getUnitWaterUse());
                                            }
                                            if ("${砂率}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getSandRatio() == null ? "——" : mixInfoEntity.getSandRatio());
                                            }
                                            if ("${设计坍落度}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getDesignSlump() == null ? "——" : mixInfoEntity.getDesignSlump());
                                            }
                                            if ("${拌和方式}".equals(string)){
                                                cells.get(n,j).setValue(mixInfoEntity.getMixingWay() == null ? "——" : mixInfoEntity.getMixingWay());
                                            }
                                            if ("材料名称".equals(string)){
                                                insertRow = n;
                                                indexName = j;
                                            }
                                            if ("规格".equals(string)){
                                                indexGg = j;
                                            }
                                            if ("生产厂家/产地".equals(string)){
                                                indexCd = j;
                                            }
                                            if ("生产批号".equals(string)){
                                                indexPh = j;
                                            }
                                            if ("样品数量".equals(string)){
                                                indexNum = j;
                                            }
                                            if ("样品状态".equals(string)){
                                                indexZt = j;
                                            }
                                            if ("样品编号".equals(string)){
                                                indexBh = j;
                                            }
                                        }else {
                                            TestSampleMixInfoEntity entity = mixInfoEntityMapper.selectByEntrustId(id);
                                            if (entity != null){
                                                if ("${设计强度}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getDesignStrength() == null ? "——" : entity.getDesignStrength());
                                                }
                                                if ("${配制强度}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getIntensityConfiguration() == null ? "——" : entity.getIntensityConfiguration());
                                                }
                                                if ("${等级}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getAntifreezeLevel() == null ? "——" : entity.getAntifreezeLevel());
                                                }

                                                if ("${水胶比}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getWaterBinderRatio() == null ? "——" : entity.getWaterBinderRatio());
                                                }
                                                if ("${单位用水量}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getUnitWaterUse() == null ? "——" : entity.getUnitWaterUse());
                                                }
                                                if ("${砂率}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getSandRatio() == null ? "——" : entity.getSandRatio());
                                                }
                                                if ("${设计坍落度}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getDesignSlump() == null ? "——" : entity.getDesignSlump());
                                                }
                                                if ("${拌和方式}".equals(string)){
                                                    cells.get(n,j).setValue(entity.getMixingWay() == null ? "——" : entity.getMixingWay());
                                                }
                                                if ("材料名称".equals(string)){
                                                    insertRow = n;
                                                    indexName = j;
                                                }
                                                if ("规格".equals(string)){
                                                    indexGg = j;
                                                }
                                                if ("生产厂家/产地".equals(string)){
                                                    indexCd = j;
                                                }
                                                if ("生产批号".equals(string)){
                                                    indexPh = j;
                                                }
                                                if ("样品数量".equals(string)){
                                                    indexNum = j;
                                                }
                                                if ("样品状态".equals(string)){
                                                    indexZt = j;
                                                }
                                                if ("样品编号".equals(string)){
                                                    indexBh = j;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //填充配合比下原材样品信息
                        List<TestSampleEntity> testSampleEntities = testSampleEntityMapper.selectByPid(entrustHistoryDetail.getSamples().get(0).getId());
                        if (testSampleEntities.size()>11){
                            //获取要插入的位置行
                            cells.insertRows(insertRow+1,testSampleEntities.size()-11);
                        }
                        for (int m=0;m<testSampleEntities.size();m++) {
                            cells.get(m+insertRow+1,indexName).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getSampleName())?"——":testSampleEntities.get(m).getSampleName());
                            cells.get(m+insertRow+1,indexGg).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getSpecs())?"——":testSampleEntities.get(m).getSpecs());
                            cells.get(m+insertRow+1,indexCd).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getManufacturer())?"——":testSampleEntities.get(m).getManufacturer());
                            cells.get(m+insertRow+1,indexPh).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getBatchNumber())?"——":testSampleEntities.get(m).getBatchNumber());
                            cells.get(m+insertRow+1,indexNum).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getGeneration())?"——":testSampleEntities.get(m).getGeneration());
                            cells.get(m+insertRow+1,indexZt).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getOutward())?"——":testSampleEntities.get(m).getOutward());
                            cells.get(m+insertRow+1,indexBh).setValue(StringUtils.isEmpty(testSampleEntities.get(m).getSampleCode())?"——":testSampleEntities.get(m).getSampleCode());
                        }
                    }
                    //过滤每个报告模板的检测项
                    List<ReportRecordDetailEntity> entities = Lists.newArrayList();
                    List<ReportRecordDetailEntity> entities1 = Lists.newArrayList();
                    String[] strings11 = conclusionEntity.getUrl().split("\\/");
                    String fileName11 = strings11[4];
                    List<Long> longList = itemDao.getItemsByTemplateLikeUrl(fileName11);
                    for (ReportRecordDetailEntity entity:checkItemList) {
                        for (Long itemId:longList) {
                            if (entity.getCheckItemId().equals(itemId)){
                                entities.add(entity);
                            }
                        }
                    }

                    //过滤每组样品的检测项(根据委托单id和样品id)
                    List<ReportRecordDetailEntity> list1 = entrustEntityMapper.getItemIdByEntrustIdAndSampleId(id,conclusionEntity.getSampleId());
                    //获取每组样品检测项生成到报告上的参数
                    for (ReportRecordDetailEntity entity:entities) {
                        for (ReportRecordDetailEntity itemId:list1) {
                            if (entity.getCheckItemId().equals(itemId.getCheckItemId())){
                                entities1.add(itemId);
                            }
                        }
                    }
                    //存放检测数据checkItemList为该报告模板所属的检测项
                    for (ReportRecordDetailEntity item : entities1) {
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(item.getCoordinate())){
                            int last = testProductDao.isLast(item.getCheckItemId().intValue());
                            //检测结果
                            String checkResult = "";
                            if (last == 0) {
                                try {
                                    checkResult = item.getCoordinate();
                                }catch (Exception e){
                                    mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标格式错误");
                                    logger.error("检测项在报告中的坐标格式错误:{}",e);
                                    continue;
                                }
                                try {
                                    if (size > 1){
                                        if (StringUtils.isNotEmpty(checkResult) && Integer.parseInt(checkResult.substring(0, 1)) == i+1){
                                            cells.get(checkResult.substring(1)).setValue(StringUtils.isEmpty(item.getCheckResult())?"——":item.getCheckResult());
                                        }
                                    }else {
                                        if (StringUtils.isNotEmpty(checkResult)){
                                            cells.get(checkResult).setValue(StringUtils.isEmpty(item.getCheckResult())?"——":item.getCheckResult());
                                        }
                                    }
                                }catch (Exception e){
                                    mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标错误");
                                    logger.error("检测项在报告中的坐标错误:{}",e);
                                    continue;
                                }
                            }
                        }else {
                            mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标未录入");
                        }
                    }
                    if (i==size-1){
                        Worksheet worksheet1 = doc.getWorksheets().get(size - 1);
                        Cells cells1 = worksheet1.getCells();
                        int maxRow1 = cells.getMaxRow();
                        int column1 = cells.getMaxColumn();
                        for (int n=0;n<maxRow1;n++) {
                            for (int j = 0; j < column1; j++) {
                                Cell cell = cells1.get(n, j);
                                if (cell != null) {
                                    Object value = cell.getValue();
                                    if (value != null) {
                                        String string = value.toString();
                                        if ("${检测结论}".equals(string)) {
                                            cells.get(n, j).setValue("检测结论："+conclusionEntity.getConclusion());
                                        }
                                        if ("${附加声明}".equals(string)){
                                            cells.get(n, j).setValue("附加声明："+conclusionEntity.getAdditional());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //按照顺序存放doc
                map.put(index,doc);
            }
        }
        //存放提示信息
        resBean.setMap(mesMap);
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.xls");
        Workbook topDoc = new Workbook(fileStream);
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        setReportTop1(topDoc,entrustAddVo,reportRecordEntity,totalPageNew,entrustHistoryDetail);
        //合并成一个excel
        Workbook document = workbookCopy(topDoc,map);
        //处理页码
        handlerPage(document,countMap);
        //上传合并完成的doc到服务器
        long name = GenID.getID();
        String path = qiYueSuoEntity.getAutographPath()+name+".xlsx";
        document.save(path);
        File file = new File(path);
        MultipartFile fileToMultipart = AsposeUtil.fileToMultipart(file, name + "");
        String url = MinIoUtil.upload("report-download", fileToMultipart, reportRecordEntity.getReportCode() + ".xlsx");
        StringBuilder stringBuilder = new StringBuilder();
        for (ConclusionEntity entity:list) {
            if (entity.getUrl().contains("?")){
                entity.setUrl(entity.getUrl().substring(0,url.indexOf("?")));
            }
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        resBean.setUrl(url);
        FileAndFolderUtil.delete(path);
        String name1 = "";
        if (StringUtils.isNotEmpty(stringBuilder.toString())){
            name1 = stringBuilder.toString().substring(0,stringBuilder.length()-2);
        }
        updateReportUrl(reportRecordEntity.getId(), url, name1);
        return resBean;
    }

    /**
     * 填充报告头部信息
     * @param topDoc
     * @param entrustAddVo
     */
    private void setReportTop1(Workbook topDoc, EntrustAddVo entrustAddVo,ReportRecordEntity reportRecordEntity,int totalPage,EntrustAddVo entrustHistoryDetail) {
        Worksheet worksheet = topDoc.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        cells.get("Y6").setValue(reportRecordEntity.getReportCode());
        cells.get("Y7").setValue(totalPage+"");
        //TODO 设置为样品别名
        List<SampleEntity> samples = entrustHistoryDetail.getSamples();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<samples.size();i++){
            if (StringUtils.isNotEmpty(samples.get(i).getAliasName())){
                stringBuilder.append(samples.get(i).getAliasName());
            }else {
                stringBuilder.append(samples.get(i).getSampleName());
            }
            if (samples.size()-1 != i){
                stringBuilder.append("、");
            }
        }
        cells.get("J32").setValue(stringBuilder.toString());
        cells.get("J33").setValue(entrustAddVo.getEntrustCompany());
        cells.get("J34").setValue(entrustAddVo.getCheckPurpose());
    }

    @Override
    public String reportUrl(Long entrustId) {
        //TODO 兼容中间报告
        String url = "";
        url = reportMapper.getUrlByEntrustId(entrustId);
        if (StringUtils.isEmpty(url)){
            url = reportMapper.getUrlByZjEntrustId(entrustId);
        }
        return url;
    }

    @Override
    public List<ConclusionEntity> getResut(Long entrustId,Integer reportType) {
        List<ReportTemplateEntity> templateList;
        //TODO 需要从报告模板和产品关系表中查询产品ids
        if(reportType == 1){//中间报告查询
            templateList = reportService.getMiddleReportTemplateList(entrustId);
        }else{//最终报告查询
            templateList = reportService.getReportTemplateList(entrustId);
        }
        List<ConclusionEntity> list = Lists.newArrayList();
        EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(entrustId);
        List<SampleEntity> samples = entrustHistoryDetail.getSamples();
        for (ReportTemplateEntity templateEntity:templateList) {
            for (SampleEntity sampleEntity :samples) {
                if (templateEntity.getProductIds().contains(sampleEntity.getProductId()+"")){
                    sampleEntity.setFileUrl(templateEntity.getReportFileUri());
                }
            }
        }
        for (SampleEntity sampleEntity :samples) {
            //处理模板下不同样品描述
            String judgeBasis = getJudgeBasisRe(entrustId,sampleEntity.getId());
            ConclusionEntity conclusionEntity =  new ConclusionEntity();
            conclusionEntity.setSampleId(sampleEntity.getId());
            conclusionEntity.setUrl(sampleEntity.getFileUrl());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("1.委托人："+entrustHistoryDetail.getEntrustPeople()+"；");
            stringBuilder.append("2."+(StringUtils.isEmpty(entrustHistoryDetail.getWitnessUint())?"见证单位：无":"见证单位："+entrustHistoryDetail.getWitnessUint())+"；");
            stringBuilder.append("3."+(StringUtils.isEmpty(entrustHistoryDetail.getWitnessPerson())?"见证人：无":"见证人："+entrustHistoryDetail.getWitnessPerson())+"；");
            stringBuilder.append("4.委托方提供："+ (StringUtils.isEmpty(entrustHistoryDetail.getRemark())?"无":entrustHistoryDetail.getRemark())+" ；");
            conclusionEntity.setAdditional(stringBuilder.toString());
            String sampleDes = sampleEntity.getSampleName()+" "+"样品,"+delItemDes(sampleEntity.getJudgmentBasisVos(),sampleEntity.getFileUrl(),entrustId);
            conclusionEntity.setConclusion("经检测，该"+sampleDes+"均符合"+judgeBasis+"中的技术要求。");
            list.add(conclusionEntity);
        }
        return list;
    }

    @Override
    public TestSampleMixInfoEntity getMixSampleInfo(Long entrustId) {
        return mixInfoEntityMapper.selectByEntrustId(entrustId);
    }

    /**
     * 处理检测项描述
     * @param sampleCheckItem
     * @param url
     * @return
     */
    private String delItemDes(List<JudgmentBasisVo> sampleCheckItem,String url,Long entrustId) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!CollectionUtils.isEmpty(sampleCheckItem)){
            for (JudgmentBasisVo entity:sampleCheckItem) {
                //过滤每个报告模板的检测项
                List<Long> longList = itemDao.getItemsByTemplateUrl(url);
                if (!CollectionUtils.isEmpty(longList)){
                    for (Long itemId:longList) {
                        if (entity.getCheckItemId().longValue() == itemId.longValue()){
                            String name = recordDetailEntityMapper.getIdByItemId(entity.getCheckItemId(),entrustId);
                            if (StringUtils.isNotEmpty(name)){
                                stringBuilder.append(name);
                                stringBuilder.append("，");
                            }
                        }
                    }
                }
            }
        }
        if (stringBuilder.length()>0){
            return stringBuilder.toString().substring(0,stringBuilder.length()-1);
        }else {
            return "";
        }

    }


    /**
     *
     * @param document word模板数据源路径
     * @param fileName word导出路径
     * @param map 关键字键值对映射
     * @throws Exception
     */
    public static void replaceWord(XWPFDocument document,String fileName,Map<String, String> map) throws Exception {
        FileOutputStream out = null;
        FileInputStream input = null;
        try {
            if("doc".equals(fileName.split("\\.")[1])) {
                //doc转inputstream
                InputStream inputStream = AsposeUtil.docToIo(document);
                HWPFDocument hwpfDocument = new HWPFDocument(inputStream);
                Range range = hwpfDocument.getRange();
                for(Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getValue() == null) {
                        //TODO
                        entry.setValue("——");
                    }
                    range.replaceText(entry.getKey(), entry.getValue());
                }
            }else {
                // 替换段落中的指定文字
                Iterator<XWPFParagraph> itPara = document.getParagraphsIterator();
                while (itPara.hasNext()) {
                    XWPFParagraph paragraph = itPara.next();
                    List<XWPFRun> runs = paragraph.getRuns();
                    for (XWPFRun run : runs) {
                        String oneparaString = run.getText(run.getTextPosition());
                        if (StringUtils.isEmpty(oneparaString)){
                            continue;
                        }
                        for (Map.Entry<String, String> entry :
                                map.entrySet()) {
                            oneparaString = oneparaString.replace(entry.getKey(), entry.getValue());
                        }
                        run.setText(oneparaString, 0);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                out.close();
            }
            if(input != null) {
                input.close();
            }
        }
    }

    /**
     * 指定表格位置插入图片签名
     * @param pdfUrl 需要签名的pdf公网地址
     * @param entrustId 委托单id
     * @throws Exception
     */
    @Override
    public String insertPicToPdf(String pdfUrl, Long entrustId,String inspector,String reportType) throws Exception {
        HashSet<String> delList = new HashSet<>();
        //TODO (报告) 兼容中间报告
        ReportRecordEntity detailByEntrustId = null;
        Long aLong = recordEntityMapper.checkExist(entrustId,reportType);
        if (aLong == null){
            detailByEntrustId = reportMapper.getDetailByEntrustIdZj(entrustId);//审核人、签发人
        }else {
            detailByEntrustId = reportMapper.getDetailByEntrustId(entrustId);//审核人、签发人
        }
        logger.info("查询签发复合信息:{}",JSON.toJSONString(detailByEntrustId));
        String verUrl = sysUserDao.getSignatureById(detailByEntrustId.getVerifyerId());
        logger.info("签发人:{}",verUrl);
        String issUrl = sysUserDao.getSignatureById(detailByEntrustId.getIssuerId());
        logger.info("批准人:{}",issUrl);
        List<String> checkUrl = Lists.newArrayList();
        String[] split = inspector.split(",");
        for (String name:split) {
            String signature = sysUserDao.getInspectorByName(name);
            logger.info("实验人:{}",signature);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(signature)){
                checkUrl.add(signature);
            }
        }
        logger.info("checkUrl大小:{}",checkUrl.size());
        if (StringUtils.isEmpty(verUrl) || StringUtils.isEmpty(issUrl) || checkUrl.size()<1){
            logger.info("缺少签名信息:{}",verUrl+"#"+issUrl+"#"+checkUrl.size());
            return "";
        }
        String basePath = qiYueSuoEntity.getAutographPath();
        logger.info("临时文件路径:{}",basePath);
        //临时文件路径（使用后删除）
        String startPath = "";
        String endPath = "";
        String suffix = ".pdf";
        //现将服务器上的报告文件、图片签名文件缓存到本地
        String localPdfPath = HttpDownloadUtil.download(pdfUrl, basePath);
        String verUrlPath = HttpDownloadUtil.download(verUrl, basePath);
        delList.add(basePath+verUrlPath);
        String issUrlPath = HttpDownloadUtil.download(issUrl, basePath);
        delList.add(basePath+issUrlPath);
        String signaturePath = "";
        float x = 1;
        float y = -10;
        int index = 1;
        startPath= basePath+localPdfPath;
        delList.add(startPath);
        endPath = basePath+1+suffix;
        delList.add(endPath);
        for (String s:checkUrl) {
            signaturePath = HttpDownloadUtil.download(s, basePath);
            //图片插入
            PdfDoc pdf = new PdfDoc(startPath, endPath);
            pdf.addImage(basePath+signaturePath, "检测：",x,y, 30, 20);
            index ++;
            startPath = endPath;
            endPath = basePath+index+suffix;
            x = x+49;
            delList.add(endPath);
        }
        PdfDoc pdf2 = new PdfDoc(startPath, endPath);
        pdf2.addImage(basePath+verUrlPath, "审核：",15,-10, 30, 20);
        index = index+1;
        startPath = endPath;
        endPath = endPath = basePath+index+suffix;
        delList.add(endPath);

        PdfDoc pdf3 = new PdfDoc(startPath, endPath);
        pdf3.addImage(basePath+issUrlPath, "批准：",20,-10, 30, 20);
        //将最终本地的pdf报告上传到文件服务器
        File file = new File(endPath);
        MultipartFile multipartFile = AsposeUtil.fileToMultipart(file, detailByEntrustId.getReportCode());
        logger.info("上传带签名的报告");
        String url = MinIoUtil.upload("report-download", multipartFile, detailByEntrustId.getReportCode() + ".pdf");
        logger.info("上传完成带签名的报告:{}",url);
        //删除产生的临时文件
        for (String del:delList) {
            FileAndFolderUtil.delete(del);
        }
        reportMapper.updateInspector(detailByEntrustId.getReportCode(),inspector);
        return url;
    }

    @Override
    public PageInfo reportList(ReportDetailListParamVo paramVo) {
        if (paramVo.getReportCompleteTime() != null) {
            String[] strArry = paramVo.getReportCompleteTime().split("~");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                paramVo.setStartDate(dateFormat.parse(strArry[0]));
            } catch (ParseException e) {
                logger.error("====报告查询列表开始时间转换失败====");
                e.printStackTrace();
            }
            try {
                paramVo.setEndingDate(dateFormat.parse(strArry[1]));
            } catch (ParseException e) {
                logger.error("====报告查询列表结束时间转换失败====");
                e.printStackTrace();
            }
        }
        //拆分委托编号
        if(!StringUtils.isEmpty(paramVo.getEntrustmentNostr())){
            EntrustCategoryVo entrustCategoryVo = EntrustNoStrUtils.splitEntrustNo(paramVo.getEntrustmentNostr());
            paramVo.setEntrustCategoryType(entrustCategoryVo.getEntrustCategoryType());
            paramVo.setEntrustmentNo(String.valueOf(entrustCategoryVo.getEntrustmentNo()));
        }
        PageHelper.startPage(paramVo.getPageNum(),paramVo.getPageSize());
        List<ReportDetailListVo> reportDetailListVos;
        if(paramVo.getReportTypeStatus() == 0){//最终报告查询
            if(paramVo.getTaskCode() == null){//无任务单号多条
                reportDetailListVos = entityMapper.reportList0808(paramVo);
                //处理任务单号
                if(!CollectionUtils.isEmpty(reportDetailListVos)){
                    for (ReportDetailListVo reportDetailListVo : reportDetailListVos) {
                        List<TaskCodeVo> taskAndTeam = entrustEntityMapper.getTaskAndTeam(reportDetailListVo.getEntrustId());
                        reportDetailListVo.setTaskCodes(taskAndTeam);
                    }
                }
            }else{//有任务单号单条
                reportDetailListVos = entityMapper.reportListTask0808(paramVo);
            }
        }else{//中间报告查询
            if(paramVo.getTaskCode() == null){//无任务单号多条
                reportDetailListVos = entityMapper.reportListMid0808(paramVo);
                //处理任务单号
                if(!CollectionUtils.isEmpty(reportDetailListVos)){
                    for (ReportDetailListVo reportDetailListVo : reportDetailListVos) {
                        List<TaskCodeVo> taskAndTeam = entrustEntityMapper.getTaskAndTeam(reportDetailListVo.getEntrustId());
                        reportDetailListVo.setTaskCodes(taskAndTeam);
                    }
                }
            }else{//有任务单号单条
                reportDetailListVos = entityMapper.reportListTaskMid0808(paramVo);
            }
        }
        PageInfo<ReportDetailListVo> pageInfo = new PageInfo<>(reportDetailListVos);
        return pageInfo;
    }

    @Override
    public PageInfo middleReportList(Integer pageNum, Integer pageSize,Integer state, String search) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        if(userTeamIds.size() == 0){
            userTeamIds = null;
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.getMiddleReportList(userTeamIds,state, search);
        for (ReportListVo reportListVo : list) {
            StringBuilder sampleName = new StringBuilder();
            List<String> sampleNames = reportMapper.getSampleNames(reportListVo.getId());
            for (int j = 0; j <sampleNames.size(); j++) {
                sampleName.append(sampleNames.get(j));
                if(j != sampleNames.size()-1){
                    sampleName.append("/");
                }
            }
            String entrustTestType = entrustEntityMapper.getEntrustTestType(reportListVo.getId());
            reportListVo.setSampleName(sampleName.toString());
            reportListVo.setEntrustTestType(entrustTestType);
            if(state == 0){
                //判断该条数据是否可以编辑
                Boolean flag = true;
//                List<TestEntrustedTaskRelEntity> entrustMidReport = taskRelDao.getEntrustMidReport(reportListVo.getId(), reportListVo.getTaskFlowId());
//                if(!CollectionUtils.isEmpty(entrustMidReport)){
//                    for (TestEntrustedTaskRelEntity relEntity : entrustMidReport) {
//                        Long recordId = relEntity.getRecordId();
//                        if (recordId == null){
//                            flag = false;
//                        }else{
//                            ReportRecordMidEntity reportRecordMidEntity = midReportMapper.selectByPrimaryKey(recordId);
//                            if(reportRecordMidEntity == null){
//                                flag = false;
//                            }
//                        }
//                    }
//                }
                Integer midReportNum = entityMapper.getMidReportNum(reportListVo.getId());
                if(midReportNum>0){
                    flag = false;
                }
                reportListVo.setFlag(flag);
            }else if(state == 1){//查询历史时
                if(reportListVo.getReportState() == null){
                    ReportRecordMidEntity midEntity = midReportMapper.selectByPrimaryKey(reportListVo.getRecordId());
                    if(midEntity != null){
                        reportListVo.setReportState(Integer.parseInt(midEntity.getState()));
                        reportListVo.setContractId(midEntity.getContractId());
                        reportListVo.setCategory(midEntity.getCategory());
                    }
                }
            }
        }
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public ReportDetailVo getMiddleReportDetail(Integer taskFlowId, Long taskId) {
//        Long recordId = recordEntityMapper.getRecordId(taskId);
//        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
//        Long entrustId = taskMapper.getEntrustId(taskId);
        TaskTestEntity taskTestEntity = taskMapper.getTaskTestEntityById(taskId);
        Long entrustId = taskTestEntity.getEntrustmentId();
        ReportDetailVo reportDetail;
        reportDetail = reportMapper.getMiddleReportDetail(taskFlowId,entrustId);
        if(reportDetail == null){
            reportDetail = new ReportDetailVo();
            reportDetail.setSamples(Lists.newArrayList());
            return reportDetail;
        }
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.getLatestReport(entrustId);
        List<ReportRecordDetailEntity> checkInfoByRecordId = Lists.newArrayList();
        if(reportRecordEntity1 != null){
            checkInfoByRecordId = recordDetailEntityMapper.getCheckInfoByRecordId(reportRecordEntity1.getId());
        }

        List<ReportSampleDetailVo> samples = reportDetail.getSamples();
        //处理每组样品下检测项
        StringBuilder sampleName = new StringBuilder();
        int i = 0;
        for (ReportSampleDetailVo reportSampleDetailVo : samples) {
            List<ReportCheckItemDetailVo> result = Lists.newArrayList();
            List<SampleItemEntity> temp = Lists.newArrayList();
            List<ReportCheckItemDetailVo> checkItems = reportSampleDetailVo.getCheckItems();
            //查询子级检测项信息
            for (int j = 0; j < checkItems.size(); j++) {
                ReportCheckItemDetailVo reportCheckItemDetailVo = checkItems.get(j);
                int last = testProductDao.isLast(reportCheckItemDetailVo.getCheckItemId().intValue());
                if (last > 0) {
                    //移除有子检测项的父检测项
                    checkItems.remove(reportCheckItemDetailVo);
                    //查询该父检测项下的子检测项信息
                    List<SampleItemEntity> nodeItems = entrustEntityMapper.getItemRecursionList(reportCheckItemDetailVo.getCheckItemId());
                    List<SampleItemEntity> tempNodeItems = Lists.newArrayList();
                    //将父级原始记录传递给子级
                    for (SampleItemEntity nodeItem : nodeItems) {
                        nodeItem.setOriginUrl(reportCheckItemDetailVo.getOriginUrl());
                        nodeItem.setSampleId(Integer.parseInt(reportSampleDetailVo.getSampleId()+""));
                        nodeItem.setTaskId(reportCheckItemDetailVo.getTaskId());
                        tempNodeItems.add(nodeItem);
                    }
                    temp.addAll(tempNodeItems);
                }
            }
            //拼接父检测项名称和子检测项名称
            HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(temp)){
                for (SampleItemEntity entity0 : temp) {
                    itemMap.put(entity0.getCheckItemId(), entity0);
                }
                for (SampleItemEntity entity2 : temp) {
                    SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                    if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                        entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                    }
                }
            }
            if(!CollectionUtils.isEmpty(temp)){
                for (SampleItemEntity sampleItemEntity : temp) {
                    //去除父检测项
                    int last = testProductDao.isLast(sampleItemEntity.getCheckItemId().intValue());
                    if (last > 0) {
                        continue;
                    }
                    ReportCheckItemDetailVo vo = new ReportCheckItemDetailVo();
//                    ReportRecordDetailEntity entity = recordDetailEntityMapper.selectByRecordIdAndItemId(recordId, sampleItemEntity.getCheckItemId().intValue(),Integer.parseInt(sampleItemEntity.getSampleId()+""));
//                    if(recordId != null && entity != null){
//                        vo.setCheckItemId(entity.getCheckItemId());
//                        vo.setCheckItemName(entity.getCheckItemName());
//                        vo.setCoordinate(entity.getCoordinate());
//                        vo.setOriginUrl(entity.getOriginUrl());
//
//                        vo.setId(entity.getId());
//                        vo.setSpecsContent(entity.getSpecsContent());
//                        vo.setCheckResult(entity.getCheckResult());
//                        vo.setJudgeResult(entity.getJudgeResult());
//                    }else{
//
//                    }
                    vo.setCheckItemId(sampleItemEntity.getCheckItemId());
                    vo.setCheckItemName(sampleItemEntity.getCheckItemName());
                    vo.setCoordinate(sampleItemEntity.getCoordinate());
                    vo.setOriginUrl(sampleItemEntity.getOriginUrl());
                    vo.setSampleId(sampleItemEntity.getSampleId());
                    vo.setTaskId(sampleItemEntity.getTaskId());
                    Long checkItemId = vo.getCheckItemId();
                    Integer sampleId = vo.getSampleId();
                    //设置上次报告制作记录
                    if(!CollectionUtils.isEmpty(checkInfoByRecordId)){
                        for (ReportRecordDetailEntity reportRecordDetailEntity : checkInfoByRecordId) {
                            Long checkItemId1 = reportRecordDetailEntity.getCheckItemId();
                            Integer sampleId1 = reportRecordDetailEntity.getSampleId();
                            if(checkItemId.equals(checkItemId1) && sampleId.equals(sampleId1)){
                                vo.setSpecsContent(reportRecordDetailEntity.getSpecsContent());
                                vo.setCheckResult(reportRecordDetailEntity.getCheckResult());
                                vo.setJudgeResult(reportRecordDetailEntity.getJudgeResult());
                            }
                        }
                    }
                    result.add(vo);
                }
            }
            if(!CollectionUtils.isEmpty(checkItems)){
                List<ReportCheckItemDetailVo> detailVos = Lists.newArrayList();
                for (ReportCheckItemDetailVo reportCheckItemDetailVo:checkItems) {
                    Long checkItemId = reportCheckItemDetailVo.getCheckItemId();
                    Integer sampleId = reportCheckItemDetailVo.getSampleId();
                    //设置上次报告制作记录
                    if(!CollectionUtils.isEmpty(checkInfoByRecordId)){
                        for (ReportRecordDetailEntity reportRecordDetailEntity : checkInfoByRecordId) {
                            Long checkItemId1 = reportRecordDetailEntity.getCheckItemId();
                            Integer sampleId1 = reportRecordDetailEntity.getSampleId();
                            if(checkItemId.equals(checkItemId1) && sampleId.equals(sampleId1)){
                                reportCheckItemDetailVo.setSpecsContent(reportRecordDetailEntity.getSpecsContent());
                                reportCheckItemDetailVo.setCheckResult(reportRecordDetailEntity.getCheckResult());
                                reportCheckItemDetailVo.setJudgeResult(reportRecordDetailEntity.getJudgeResult());
                            }
                        }
                    }
                    detailVos.add(reportCheckItemDetailVo);
                }
                result.addAll(detailVos);
            }
            reportSampleDetailVo.setCheckItems(result);
            sampleName.append(reportSampleDetailVo.getSampleName());
            if(i != samples.size() -1){
                sampleName.append("/");
            }
            i++;
        }
        reportDetail.setSampleName(sampleName.toString());
        reportDetail.setSamples(samples);
        //获取中间报告需求信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TestEntrustedTaskRelVo taskFlowById = taskRelDao.getTaskFlowById(taskFlowId);
        reportDetail.setRequestDate(sdf.format(taskFlowById.getTaskFlowDate()));
        reportDetail.setTaskCode(taskTestEntity.getTaskCode());
        return reportDetail;
    }

    @Override
    public ReportDetailVo middleReportEdit(Integer taskFlowId, Long taskId,Long recordId) {
        TaskTestEntity taskTestEntity = taskMapper.getTaskTestEntityById(taskId);
        Long entrustId = taskTestEntity.getEntrustmentId();
        ReportDetailVo reportDetail = reportMapper.getMiddleReportDetail(taskFlowId,entrustId);
        if(reportDetail == null){
            reportDetail = new ReportDetailVo();
            reportDetail.setSamples(Lists.newArrayList());
            return reportDetail;
        }
        //获取历史检测项检测结果数据
        List<ReportRecordDetailEntity> checkInfoByRecordId = recordDetailEntityMapper.getCheckInfoByRecordId(recordId);
        List<ReportSampleDetailVo> samples = reportDetail.getSamples();
        //处理每组样品下检测项
        StringBuilder sampleName = new StringBuilder();
        int i = 0;
        for (ReportSampleDetailVo reportSampleDetailVo : samples) {
            List<ReportCheckItemDetailVo> result = Lists.newArrayList();
            List<SampleItemEntity> temp = Lists.newArrayList();
            List<ReportCheckItemDetailVo> checkItems = reportSampleDetailVo.getCheckItems();
            //查询子级检测项信息
            for (int j = 0; j < checkItems.size(); j++) {
                ReportCheckItemDetailVo reportCheckItemDetailVo = checkItems.get(j);
                int last = testProductDao.isLast(reportCheckItemDetailVo.getCheckItemId().intValue());
                if (last > 0) {
                    //移除有子检测项的父检测项
                    checkItems.remove(reportCheckItemDetailVo);
                    //查询该父检测项下的子检测项信息
                    List<SampleItemEntity> nodeItems = entrustEntityMapper.getItemRecursionList(reportCheckItemDetailVo.getCheckItemId());
                    List<SampleItemEntity> tempNodeItems = Lists.newArrayList();
                    //将父级原始记录传递给子级
                    for (SampleItemEntity nodeItem : nodeItems) {
                        nodeItem.setOriginUrl(reportCheckItemDetailVo.getOriginUrl());
                        nodeItem.setSampleId(Integer.parseInt(reportSampleDetailVo.getSampleId()+""));
                        nodeItem.setTaskId(reportCheckItemDetailVo.getTaskId());
                        tempNodeItems.add(nodeItem);
                    }
                    temp.addAll(tempNodeItems);
                }
            }
            //拼接父检测项名称和子检测项名称
            HashMap<Long, SampleItemEntity> itemMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(temp)){
                for (SampleItemEntity entity0 : temp) {
                    itemMap.put(entity0.getCheckItemId(), entity0);
                }
                for (SampleItemEntity entity2 : temp) {
                    SampleItemEntity sampleItemEntity = itemMap.get(entity2.getCheckItemPid());
                    if (sampleItemEntity != null && entity2.getUnitPrice() == null) {
                        entity2.setCheckItemName(sampleItemEntity.getCheckItemName() + "-" + entity2.getCheckItemName());
                    }
                }
            }
            if(!CollectionUtils.isEmpty(temp)){
                for (SampleItemEntity sampleItemEntity : temp) {
                    //去除父检测项
                    int last = testProductDao.isLast(sampleItemEntity.getCheckItemId().intValue());
                    if (last > 0) {
                        continue;
                    }
                    ReportCheckItemDetailVo vo = new ReportCheckItemDetailVo();
                    vo.setCheckItemId(sampleItemEntity.getCheckItemId());
                    vo.setCheckItemName(sampleItemEntity.getCheckItemName());
                    vo.setCoordinate(sampleItemEntity.getCoordinate());
                    vo.setOriginUrl(sampleItemEntity.getOriginUrl());
                    vo.setSampleId(sampleItemEntity.getSampleId());
                    vo.setTaskId(sampleItemEntity.getTaskId());
                    Long checkItemId = vo.getCheckItemId();
                    Integer sampleId = vo.getSampleId();
                    //设置上次报告制作记录
                    if(!CollectionUtils.isEmpty(checkInfoByRecordId)){
                        for (ReportRecordDetailEntity reportRecordDetailEntity : checkInfoByRecordId) {
                            Long checkItemId1 = reportRecordDetailEntity.getCheckItemId();
                            Integer sampleId1 = reportRecordDetailEntity.getSampleId();
                            if(checkItemId.equals(checkItemId1) && sampleId.equals(sampleId1)){
                                vo.setSpecsContent(reportRecordDetailEntity.getSpecsContent());
                                vo.setCheckResult(reportRecordDetailEntity.getCheckResult());
                                vo.setJudgeResult(reportRecordDetailEntity.getJudgeResult());
                                vo.setId(reportRecordDetailEntity.getId());
                            }
                        }
                    }
                    result.add(vo);
                }
            }
            if(!CollectionUtils.isEmpty(checkItems)){
                List<ReportCheckItemDetailVo> detailVos = Lists.newArrayList();
                for (ReportCheckItemDetailVo reportCheckItemDetailVo:checkItems) {
                    Long checkItemId = reportCheckItemDetailVo.getCheckItemId();
                    Integer sampleId = reportCheckItemDetailVo.getSampleId();
                    //设置上次报告制作记录
                    if(!CollectionUtils.isEmpty(checkInfoByRecordId)){
                        for (ReportRecordDetailEntity reportRecordDetailEntity : checkInfoByRecordId) {
                            Long checkItemId1 = reportRecordDetailEntity.getCheckItemId();
                            Integer sampleId1 = reportRecordDetailEntity.getSampleId();
                            if(checkItemId.equals(checkItemId1) && sampleId.equals(sampleId1)){
                                reportCheckItemDetailVo.setSpecsContent(reportRecordDetailEntity.getSpecsContent());
                                reportCheckItemDetailVo.setCheckResult(reportRecordDetailEntity.getCheckResult());
                                reportCheckItemDetailVo.setJudgeResult(reportRecordDetailEntity.getJudgeResult());
                                reportCheckItemDetailVo.setId(reportRecordDetailEntity.getId());
                            }
                        }
                    }
                    detailVos.add(reportCheckItemDetailVo);
                }
                result.addAll(detailVos);
            }
            reportSampleDetailVo.setCheckItems(result);
            sampleName.append(reportSampleDetailVo.getSampleName());
            if(i != samples.size() -1){
                sampleName.append("/");
            }
            i++;
        }
        reportDetail.setSampleName(sampleName.toString());
        reportDetail.setSamples(samples);
        //获取中间报告需求信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TestEntrustedTaskRelVo taskFlowById = taskRelDao.getTaskFlowById(taskFlowId);
        reportDetail.setRequestDate(sdf.format(taskFlowById.getTaskFlowDate()));
        reportDetail.setTaskCode(taskTestEntity.getTaskCode());
        return reportDetail;
    }

    @Override
    public String getUrlById(Long id) {
        return reportMapper.getUrlById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean category(SealEntity sealEntity) {
        List<SealEntity> list = Lists.newArrayList();
        List<Long> ids = Lists.newArrayList();
        for (Long id:sealEntity.getId()) {
            SealEntity entity = new SealEntity();
            entity.setSealer(sealEntity.getSealer());
            entity.setSealTime(sealEntity.getSealTime());
            entity.setSealType(sealEntity.getSealType());
            entity.setKey(id);
            list.add(entity);
            ids.add(id);
        }
        //设置状态和用章类型
        reportMapper.updateCategory(list);
        //如果是中间报告，移除中间报到到新表
        if (ids.size()>=1){
            for (Long id:ids) {
                moveReportRecord(id);
                log.info("移除中间报告id:{}",id);
            }
        }
        return true;
    }

    @Override
    public Boolean withdrawReport(Long recordId,Long taskId) {
        ReportRecordEntity reportRecordEntity = recordEntityMapper.getByRecordId(recordId);
        String state = reportRecordEntity.getState();
        if(Integer.parseInt(state) >=3){
            return false;
        }else{
            taskMapper.updateReportStatus(2, taskId);
            return true;
        }
    }

    @Override
    public Long getEntrustIdById(Long id) {
        //TODO 兼容中间报告
        Long entrustIdById = null;
        entrustIdById = reportMapper.getEntrustIdById(id);
        if (entrustIdById == null){
            entrustIdById = reportMapper.getZjEntrustIdById(id);
        }
        return entrustIdById;
    }

    @Override
    public PageInfo<ReportRecordEntity> historyList(String reportCode, String reportType, String sealType, Integer pageNum, Integer pageSize,Long startDate,Long endDate) {
        //每个团队看子集大团队任务产生的报告列表
        Long userId = ShiroUtils.getUserInfo().getUserId();
        List<Integer> ids = Lists.newArrayList();
        //校验用户id是否分配团队
        int teamId = testTechnicistDao.getSealer(userId);
        if (teamId > 0) {
            //获取顶级团队
            Long topTeamId = this.getTopDepartment((long) teamId);
            if (topTeamId == null){
                topTeamId = (long)teamId;
            }
            //获取顶级团队下的所有下级团队
            List<TeamTreeStructureEntity> chirds = teamMapper.getChirds(topTeamId);
            for (TeamTreeStructureEntity entity:chirds) {
                ids.add(Integer.valueOf(entity.getId()+""));
            }
        }
        if (ids.size()<=0){
            ids = null;
        }
        List<ReportRecordEntity> list;
        PageHelper.startPage(pageNum,pageSize);
        if (Integer.parseInt(reportType) == 0 || StringUtils.isEmpty(reportType)){
            list = reportMapper.historyList(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
        }else {
            list = reportMapper.historyListZj(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
        }
        for (ReportRecordEntity entity:list) {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(entity.getSealer())){
                entity.setSealer(entity.getSealer().split("&")[0]);
            }
            if ("0".equals(entity.getType())){
                entity.setType("最终报告");
            }else {
                entity.setType("中间报告");
            }
        }
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<TestTeam> getSealer() {
        Long userId = ShiroUtils.getUserInfo().getUserId();
        //校验用户id是否分配团队
        int teamId = testTechnicistDao.getSealer(userId);
        Long aLong = this.getTopDepartment((long) teamId);
        if (aLong == null){
            aLong = (long)teamId;
        }
        if (teamId > 0){
            List<TestTeam> idsByTeamId = teamMapper.getIdsByTeamId((long) aLong);
            idsByTeamId.removeIf(Objects::isNull);
            return idsByTeamId;
        }else {
            return null;
        }
    }

    @Override
    public byte[] exportRecords(String reportCode, String reportType, String sealType, Long startDate, Long endDate) {
        //每个团队看子集大团队任务产生的报告列表
        Long userId = ShiroUtils.getUserInfo().getUserId();
        List<Integer> ids = Lists.newArrayList();
        //校验用户id是否分配团队
        int teamId = testTechnicistDao.getSealer(userId);
        if (teamId > 0) {
            //获取顶级团队
            Long topTeamId = this.getTopDepartment((long) teamId);
            if (topTeamId == null){
                topTeamId = (long)teamId;
            }
            //获取顶级团队下的所有下级团队
            List<TeamTreeStructureEntity> chirds = teamMapper.getChirds(topTeamId);
            for (TeamTreeStructureEntity entity:chirds) {
                ids.add(Integer.valueOf(entity.getId()+""));
            }
        }
        if (ids.size()<=0){
            ids = null;
        }
        //导出中间报告和最终报告
        List<ReportRecordEntity> list = reportMapper.exportRecords(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
        List<ReportRecordEntity> list1 = reportMapper.exportRecordsZj(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
        list.addAll(list1);
        byte[] bytes = null;
        try {
            bytes = exportExcel(list,qiYueSuoEntity.getAutographPath()+"sealList.xlsx");
        }catch (Exception e){
            log.error("报告历史记录导出失败:{}",e);
        }
        return bytes;
    }

    /**
     * 导出盖章历史数据
     * @param list
     */
    private byte[] exportExcel(List<ReportRecordEntity> list,String basePath) throws Exception {
        PDFHelper3.getLicense();
        //处理检测人、记录人，审核、签发人，日期，报告分数，产值
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "报告盖章登记表.xlsx");
        Workbook workbook = new Workbook(fileStream);
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        int n = 4;
        String row = "B";
        //设置标题
        cells.get("A1").setValue(Calendar.getInstance().get(Calendar.YEAR)+"年度检测报告盖章登记表");
        for (ReportRecordEntity entity:list) {
            //报告编号
            cells.get(row+n).setValue(entity.getReportCode());
            row = getNextUpEn(row);
            cells.get(row+n).setValue(entity.getSampleName());
            row = getNextUpEn(row);
            cells.get(row+n).setValue(entity.getInspector());
            row = getNextUpEn(row);
            cells.get(row+n).setValue(entity.getVerifyer());
            row = getNextUpEn(row);
            cells.get(row+n).setValue(entity.getIssuer());
            row = getNextUpEn(row);
            cells.get(row+n).setValue(DateUtil.formatDate(entity.getIssuerTime()));
            row = getNextUpEn(row);
            cells.get(row+n).setValue(DateUtil.formatDate(entity.getSealTime()));
            row = getNextUpEn(row);
            //盖章类型
            String sealType = entity.getSealType();
            if (sealType.contains("甲级")){
                cells.get(row+n).setValue("√");
            }
            row = getNextUpEn(row);
            if (sealType.contains("CMA")){
                cells.get(row+n).setValue("√");
            }
            row = getNextUpEn(row);
            if (sealType.contains("CNAS")){
                cells.get(row+n).setValue("√");
            }
            row = getNextUpEn(row);
            //报告分数
            Integer number = entity.getNumber();
            cells.get(row+n).setValue(1);
            row = getNextUpEn(row);
            cells.get(row+n).setValue(number-1);
            row = getNextUpEn(row);
            cells.get(row+n).setValue(number);
            row = getNextUpEn(row);
            //报告产值
            if (StringUtils.isNotEmpty(entity.getActualPrice())){
                cells.get(row+n).setValue(Double.valueOf(entity.getActualPrice()));
            }
            row = getNextUpEn(row);
            //报告类型
            cells.get(row+n).setValue("");
            //委托编号
            row = getNextUpEn(row);
            cells.get(row+n).setValue(entity.getEntrustmentNo());
            row = "B";
            n++;
        }
        workbook.save(basePath);
        File file = new File(basePath);
        byte[] bytes = FileAndFolderUtil.file2byte(file);

        return bytes;
    }

    /**
     * 合并:
     * 返回合并后的 Workbook
     */
    private static Workbook workbookCopy(Workbook finalWork,Map<Integer, Workbook> map) {
        PDFHelper3.getLicense();
        Set<Integer> keySet = map.keySet();
        int num = 1;
        try {
            for (Integer key:keySet) {
                Workbook workbook = map.get(key);
                // 遍历准备合并的工作簿
                int count = workbook.getWorksheets().getCount();
                for (int i = 0; i < count; i++) {
                    // sheet信息
                    Worksheet worksheet = workbook.getWorksheets().get(i);
                    String name = worksheet.getName();
                    // 判断合并后的 工作簿中是否已经存在了该sheet名称, 重复则+ 1
                    String newSheetName = null != finalWork.getWorksheets().get(name) ? name + num : name;
                    // 开始合并
                    Worksheet worksheetS = finalWork.getWorksheets().add(newSheetName);
                    worksheetS.copy(worksheet);
                    int count1 = finalWork.getWorksheets().getCount();
                    num ++;
                    log.info("页数:{}",count1);
                }
            }
            int count = finalWork.getWorksheets().getCount();
            log.info("合并后总页数:{}",count);
        } catch (Exception e) {
            log.error("合并多个excel异常:{}",e);
            return null;
        }
        return finalWork;
    }

    /**
     * 获取下一个字母
     * @param en
     * @return
     */
    public static String getNextUpEn(String en){
        char lastE = 'a';
        char st = en.toCharArray()[0];
        if(Character.isUpperCase(st)){
            if(en.equals("Z")){
                return "A";
            }
            if(en==null || en.equals("")){
                return "A";
            }
            lastE = 'Z';
        }else{
            if(en.equals("z")){
                return "a";
            }
            if(en==null || en.equals("")){
                return "a";
            }
            lastE = 'z';
        }
        int lastEnglish = (int)lastE;
        char[] c = en.toCharArray();
        if(c.length>1){
            return null;
        }else{
            int now = (int)c[0];
            if(now >= lastEnglish)
                return null;
            char uppercase = (char)(now+1);
            return String.valueOf(uppercase);
        }
    }

    /**
     * 获取顶级部门id
     * @return
     */
    public Long getTopDepartment(Long id){
        Long topId = null;
        List<TreeEntity> treeList = com.google.api.client.util.Lists.newArrayList();
        List<TestTeam> list = teamMapper.getTopDepartment(id);
        if (list.size() == 0){
            return id;
        }
        if (list.size() == 1){
            topId = Long.valueOf(list.get(0).getId()+"");
        }else {
            for (TestTeam team:list) {
                TreeEntity entity = new TreeEntity();
                entity.setId(team.getId()+"");
                entity.setPid(team.getPid()+"");
                treeList.add(entity);
            }
            //获取最顶级部门id
            List<TreeEntity> treeData = ConvertUtil.list2TreeList(treeList,"id","pid","children");
            topId = Long.valueOf(treeData.get(0).getId());
        }
        return topId;
    }

    @Override
    public List<String> inspectorList(String search) {
        //获取检测人员

        return testTechnicistDao.inspectorList(search);
    }

    @Override
    public void updateInspector(String reportCode, String inspector) {
        reportMapper.updateInspector(reportCode,inspector);
    }

    /**
     *             物理章
     * 移动中间报告数据到中间报告数据表
     * test_report_record-->test_report_record_mid
     * @param record
     */
    public void moveReportRecord(Long record){
        Long id = recordEntityMapper.getTypeById(record);
        if (id == null){
            ReportRecordEntity byRecordId = recordEntityMapper.getByRecordId(record);
            ReportRecordMidEntity midEntity = new ReportRecordMidEntity(byRecordId);
            int insert = midReportMapper.insert(midEntity);
            int i = recordEntityMapper.deleteByPrimaryKey(record);
        }
    }

}
