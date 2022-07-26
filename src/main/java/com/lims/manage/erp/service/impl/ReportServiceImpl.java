package com.lims.manage.erp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.http.QiYueSuoDocment;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.ConvertUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.HttpDownloadUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.PDFHelper3;
import com.lims.manage.erp.util.PageInfoUtils;
import com.lims.manage.erp.util.PdfDoc;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.WordUtils;
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
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
            List<String> sampleNames = reportMapper.getSampleNames(reportListVo.getId());
            StringBuilder sampleName = new StringBuilder();
            for (int i = 0; i < sampleNames.size(); i++) {
                sampleName.append(sampleNames.get(i));
                if(i != sampleNames.size()-1){
                    sampleName.append("/");
                }
            }
            reportListVo.setSampleName(sampleName.toString());
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

    public PageInfo reportDownloadListHistory(String search, Integer pageNum, Integer pageSize) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setReportCode(search);
        reportListVo.setDeptIds(userTeamIds);
        List<ReportListVo> list = reportMapper.reportDownloadListHistory(reportListVo);
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
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemListByTaskId(taskId);
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
        return reportDetail;
    }

    @Override
    public ReportDetailVo getReportDetail0620(Long taskId) {
        Long recordId = recordEntityMapper.getRecordId(taskId);
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        ReportDetailVo reportDetail = reportMapper.getReportDetail(taskId, userTeamIds);
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
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
                e.setTaskId(vo.getTaskId());
                List<Long> checkItemIds = recordDetailEntityMapper.getCheckItemIds(reportRecordEntity1.getId(),vo.getTaskId(),e.getSampleId());
                if (checkItemIds.contains(e.getCheckItemId())) {
                    recordDetailEntityMapper.updateByRecordIdSelective(e);
                } else {
                    recordDetailEntityMapper.insert(e);
                }
//                int insert1;
//                if (checkItemIds.contains(e.getCheckItemId())) {
//                    insert1 = recordDetailEntityMapper.updateByRecordIdSelective(e);
//                } else {
//                    insert1 = recordDetailEntityMapper.insert(e);
//                }
//                if (insert1 < 1) {
//                    return false;
//                }
            }
            //校验其他任务单是否完成
            if(vo.getReportComplete() == 2){
                reportRecordEntity1.setState(2+"");
            }else{
                List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
                if(allReportComplete.contains(2)){
                    reportRecordEntity1.setState(2+"");
                }else{
                    reportRecordEntity1.setState(1+"");
                    reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
                }
            }
//            List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
//            if(allReportComplete.contains(2)){
//                reportRecordEntity1.setState(2+"");
//            }else{
//                reportRecordEntity1.setState(1+"");
//                reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
//            }
            //修改任务报告状态
            taskMapper.updateReportStatus(vo.getReportComplete(), vo.getTaskId());
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            //获取父级code
            Long deptId = taskMapper.getDeptByEntrustId(vo.getEntrustmentId());
            String topDepartmentCode = teamMapper.getTopDepartmentCode(deptId);
            long recordId = GenID.getID();
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
                e.setTaskId(vo.getTaskId());
                int insert1 = recordDetailEntityMapper.insert(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
            if(vo.getReportComplete() == 2){
                reportRecordEntity.setState(2+"");
            }else{
                List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
                if(allReportComplete.contains(2)){
                    reportRecordEntity.setState(2+"");
                }else{
                    reportRecordEntity.setState(1+"");
                    reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
                }
            }
//            List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
//            if(allReportComplete.contains(2)){
//                reportRecordEntity.setState(2+"");
//            }else{
//                reportRecordEntity.setState(1+"");
//                reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
//            }
            //生成报告编号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String year = sdf.format(new Date());
            Integer maxCode = recordEntityMapper.getMaxCode(year,topDepartmentCode);
            if (maxCode == null) {
                reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-0001");
            } else {
                int newCode = maxCode + 1;
                reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-" + new DecimalFormat("0000").format(newCode));
            }
            reportRecordEntity.setId(recordId);
            reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
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

    @Transactional
    @Override
    public Boolean middleReportPreserve(ReportPreserveVo vo) {
        //获取父级code
        PageHelper.clearPage();
        Long deptId = taskMapper.getDeptByEntrustId(vo.getEntrustmentId());
        String topDepartmentCode = teamMapper.getTopDepartmentCode(deptId);
        long recordId = GenID.getID();
        List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
        for (ReportRecordDetailEntity e : checkInfos) {
            e.setRecordId(recordId);
            int insert1 = recordDetailEntityMapper.insert(e);
            if (insert1 < 1) {
                return false;
            }
        }
        ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo,vo.getEntrustmentId());
        //生成报告编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String year = sdf.format(new Date());
        Integer maxCode = recordEntityMapper.getMaxCode(year,topDepartmentCode);
        if (maxCode == null) {
            reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-0001");
        } else {
            int newCode = maxCode + 1;
            reportRecordEntity.setReportCode(topDepartmentCode+"-" + year + "-YC-" + new DecimalFormat("0000").format(newCode));
        }
        reportRecordEntity.setId(recordId);
        reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
        reportRecordEntity.setState(3+"");//设置为待发起审批
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
            if ("0".equals(recordEntity.getType())){
                recordEntity.setType("最终报告");
            }else {
                recordEntity.setType("中间报告");
            }
            //TODO 兼容中间报告
            if (recordEntity.getEntrustmentId() == null){
                recordEntity.setEntrustId(recordEntity.getEntrustmentId());
            }
        }
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public XWPFDocument preview(String reportCode, List<ReportRecordDetailEntity> detailEntityList, EntrustAddVo detail, InputStream object, String[] sealUrls) {
        XWPFDocument doc = null;
        Map<String, Object> map = new HashMap();
        try {
            doc = new XWPFDocument(object);
            //检测项所属table定义集合
            Map<Integer, List<ReportRecordDetailEntity>> listMap = new HashMap();
            for (ReportRecordDetailEntity entity : detailEntityList) {
                String[] split = entity.getCoordinate().split(",");
                Integer key = Integer.valueOf(split[0]);
                if (listMap.get(key) == null) {
                    List<ReportRecordDetailEntity> list = Lists.newArrayList();
                    list.add(entity);
                    listMap.put(key, list);
                } else {
                    List<ReportRecordDetailEntity> list = listMap.get(key);
                    list.add(entity);
                    listMap.put(key, list);
                }
                map.put(split[0], "index");//map作为下面的循环table的索引
            }
            int size = map.keySet().size();
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            for (int i = 0; i < size; i++) {
                XWPFTable table = tables.get(i);
                //第一个table包含表头、后面的table只有检测项数据
                //获取表格对应的行
                rows = table.getRows();
                List<ReportRecordDetailEntity> list = listMap.get(i + 1);//检测项
                if (i == 0) {
                    //设置模板数据
                    rows.get(3).getTableCells().get(1).setText("河南省公路工程实验检测中心有限公司");//检测单位
                    rows.get(3).getTableCells().get(2).setText(reportCode);//报告编号
                    rows.get(4).getTableCells().get(1).setText(detail.getEntrustCompany());//委托单位
                    rows.get(4).getTableCells().get(2).setText(detail.getProjectName());//工程名称
                    rows.get(5).getTableCells().get(1).setText(detail.getProjectPart());//工程部位
                    //设置样品信息
                    List<SampleEntity> samples = detail.getSamples();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (SampleEntity sampleEntity : samples) {
                        stringBuilder.append("样品名称：");
                        stringBuilder.append(sampleEntity.getSampleName());
                        stringBuilder.append(";");
                        stringBuilder.append("样品编号：");
                        stringBuilder.append(sampleEntity.getSampleCode());
                        stringBuilder.append(";");
                        stringBuilder.append("样品数量:");
                        stringBuilder.append(sampleEntity.getSampleGroups());
                        stringBuilder.append("片;");
                        stringBuilder.append("样品状态:");
                        stringBuilder.append(sampleEntity.getState());
                        stringBuilder.append(";");
                        stringBuilder.append("收样时间:");
                        stringBuilder.append(sampleEntity.getReceivedDate());
                        stringBuilder.append(";");
                    }
                    String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                    rows.get(6).getTableCells().get(1).setText(substring);
                    //检测依据
                    if (!CollectionUtils.isEmpty(samples)) {
                        StringBuilder stringBuilder1 = new StringBuilder();
                        for (SampleEntity entity : samples) {
                            List<JudgmentBasisVo> sampleCheckItem = entity.getJudgmentBasisVos();
                            for (JudgmentBasisVo itemEntity : sampleCheckItem) {
                                //TODO 根据类型区分检测依据和判定依据
                                Integer standardId = itemEntity.getStandardId();
                                //查询处理
                                stringBuilder1.append("GB/T 2012-1");
                                stringBuilder1.append(",");
                            }
                        }
                        String substring1 = stringBuilder1.toString().substring(0, stringBuilder1.length() - 1);
                        rows.get(7).getTableCells().get(1).setText(substring1);//检验依据
                        rows.get(7).getTableCells().get(2).setText(substring1);//判定依据
                    }
                    //检测日期
                    rows.get(8).getTableCells().get(1).setText(DateUtil.formatDate(detail.getOperateDate()));
                    //主要仪器设备名称及编号
                    rows.get(9).getTableCells().get(1).setText("");
                    //委托编号
                    rows.get(10).getTableCells().get(1).setText(detail.getEntrustmentNo() + "");
                    //检测类别
                    rows.get(10).getTableCells().get(2).setText(detail.getEntrustType());
                    //批号
                    rows.get(11).getTableCells().get(1).setText(samples.get(0).getBatchNumber());
                    //生成厂家
                    rows.get(11).getTableCells().get(2).setText(samples.get(0).getManufacturer());
                    //规格等级
                    rows.get(12).getTableCells().get(1).setText(samples.get(0).getSpecs());
                    //代表数量
                    rows.get(12).getTableCells().get(2).setText(samples.get(0).getGeneration());

                    //根据坐标设置检测项
                    for (ReportRecordDetailEntity entity : list) {
                        //获取坐标
                        String[] split = entity.getCoordinate().split(",");
                        Integer x = Integer.valueOf(split[1]);
                        Integer y = Integer.valueOf(split[2]);
                        //设置技术指标
                        rows.get(x).getTableCells().get(y + 1).setText(entity.getSpecsContent());
                        //设置检测结果
                        rows.get(x).getTableCells().get(y + 2).setText(entity.getCheckResult());
                        //设置判定结果
                        rows.get(x).getTableCells().get(y + 3).setText(entity.getJudgeResult());
                    }
                } else {
                    //根据坐标设置检测项
                    for (ReportRecordDetailEntity entity : list) {
                        //获取坐标
                        String[] split = entity.getCoordinate().split(",");
                        Integer x = Integer.valueOf(split[1]);
                        Integer y = Integer.valueOf(split[2]);
                        //设置技术指标
                        rows.get(x).getTableCells().get(y + 1).setText(entity.getSpecsContent());
                        //设置检测结果
                        rows.get(x).getTableCells().get(y + 2).setText(entity.getCheckResult());
                        //设置判定结果
                        rows.get(x).getTableCells().get(y + 3).setText(entity.getJudgeResult());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("报告查看异常:{}", e);
        }
        return doc;
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
    public String getJudgeBasis(Long id) {
        StringBuilder result = new StringBuilder("");
        List<String> judgeBasis = reportMapper.getJudgeBasis(id);
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
    public String getCheckBasis(Long id) {
        StringBuilder result = new StringBuilder("");
        List<String> checkBasis = reportMapper.getCheckBasis(id);
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
    public String getEquipment(Long id) {
        StringBuilder result = new StringBuilder("");
        List<String> equipment = reportMapper.getEquipment(id);
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
    public String downLoad(MinioClient client, String code, Long id) throws Exception {
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
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
            //设置报告首页参数
            EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
            Map<String, String> textMap = new HashMap<>();
            textMap.put("code", reportRecordEntity.getReportCode());
            textMap.put("page", doc.getTables().size() + "");
            textMap.put("sampleName", reportRecordEntity.getSampleName());
            textMap.put("dept", entrustAddVo.getEntrustCompany());
            textMap.put("part", entrustAddVo.getProjectPart());
            textMap.put("checkType", entrustAddVo.getCheckPurpose());
            WordUtils.changeText(doc, textMap);
            //处理表格
            Iterator<XWPFTable> it = doc.getTablesIterator();
            //表格索引
            int i = 1;
            //获取表格信息
            while (it.hasNext()) {
                XWPFTable table = it.next();
                List<XWPFTableRow> rows = table.getRows();
                //存放表头信息
                EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                if (i == 1) {
                    rows.get(4).getCell(1).removeParagraph(0);
                    rows.get(4).getCell(1).setText(entrustHistoryDetail.getEntrustCompany());
                    rows.get(4).getCell(3).removeParagraph(0);
                    rows.get(4).getCell(3).setText(entrustHistoryDetail.getProjectName());
                    rows.get(5).getCell(1).removeParagraph(0);
                    rows.get(5).getCell(1).setText(entrustHistoryDetail.getProjectPart());
                    //样品信息
                    SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                    rows.get(6).getCell(1).removeParagraph(0);
                    rows.get(6).getCell(1).setText("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                            + "，样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                            + "，样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                            + "，样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                            + "，收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                    //检测依据
                    String checkBasis = getCheckBasis(id);
                    if (checkBasis.contains("《")){
                        checkBasis = checkBasis.split("《")[0];
                    }
                    rows.get(7).getCell(1).removeParagraph(0);
                    rows.get(7).getCell(1).setText(checkBasis.equals("") ? "——" : checkBasis);
                    //判定依据
                    String judgeBasis = getJudgeBasis(id);
                    rows.get(7).getCell(3).removeParagraph(0);
                    rows.get(7).getCell(3).setText(judgeBasis.equals("") ? "——" : judgeBasis);
                    //检测日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    rows.get(8).getCell(1).removeParagraph(0);
                    rows.get(8).getCell(1).setText(sdf.format(entrustHistoryDetail.getAcceptanceDate()) + "~"
                            + sdf.format(reportRecordEntity.getReportCompleteTime() == null ? new Date() : reportRecordEntity.getReportCompleteTime())
                    );
                    //主要仪器
                    String equipment = getEquipment(id);
                    rows.get(9).getCell(1).removeParagraph(0);
                    rows.get(9).getCell(1).setText(equipment.equals("") ? "——" : equipment);
                    //委托编号
                    rows.get(10).getCell(1).removeParagraph(0);
                    rows.get(10).getCell(1).setText(entrustHistoryDetail.getEntrustmentNo() + "");
                    //检测类别
                    rows.get(10).getCell(3).removeParagraph(0);
                    rows.get(10).getCell(3).setText(entrustHistoryDetail.getCheckPurpose());
                    //批号
                    rows.get(11).getCell(1).removeParagraph(0);
                    rows.get(11).getCell(1).setText(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                    //生产厂家
                    rows.get(11).getCell(3).removeParagraph(0);
                    rows.get(11).getCell(3).setText(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                    //规格等级
                    rows.get(12).getCell(1).removeParagraph(0);
                    rows.get(12).getCell(1).setText(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                    //代表数量
                    rows.get(12).getCell(3).removeParagraph(0);
                    rows.get(12).getCell(3).setText(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                }
                //存放检测数据
                for (ReportRecordDetailEntity item : checkItemList) {
                    int last = testProductDao.isLast(item.getCheckItemId().intValue());
                    if (last == 0) {
                        int page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                        int row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                        int column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                        if (i == page) {
                            rows.get(row).getCell(column + 1).removeParagraph(0);
                            rows.get(row).getCell(column + 1).setText(item.getSpecsContent());
                            rows.get(row).getCell(column + 2).removeParagraph(0);
                            rows.get(row).getCell(column + 2).setText(item.getCheckResult());
                            rows.get(row).getCell(column + 3).removeParagraph(0);
                            rows.get(row).getCell(column + 3).setText(item.getJudgeResult());
                        }
                    }
                }
                i++;
            }
        }
        ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
        //盖章
//        String sealUrl = reportRecordEntity.getSealUrl();
//        List<ImagePro> imagePros = com.google.api.client.util.Lists.newArrayList();
//        if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
//            String[] split = sealUrl.split(",");
//            for (int i = 0; i < split.length; i++) {
//                ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
//                imagePros.add(pro);
//            }
//        }
//        InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
//        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
//        ByteArrayOutputStream b3 = ImageToPdfUtils.writeToPdf4(inputStream1, b2, imagePros);
        InputStream inputStream = FileAndFolderUtil.parseOut(b1);
        String url = "";
        url = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream, "application/octet-stream");
        updateReportUrl(reportRecordEntity.getId(), url, code);
//            ServletOutputStream outputStream = response.getOutputStream();
//            FileAndFolderUtil.parseIn(inputStream)
        return url;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadReport(String reportCode, MultipartFile file, String verifyer,
                                String issuer, Long verifyerId, Long issuerId, String code,
                                String conclusion,String additional,String mixInfo,String type,String inspector) {
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
                    resBean = this.submitDownLoadMix(client, list, Long.parseLong(reportCode),mixInfoEntity);
                    url = resBean.getUrl();
                }else {
                    resBean = this.submitDownLoad(client, list, Long.parseLong(reportCode));
                    url = resBean.getUrl();
                }
                flag = true;
            }catch (Exception e){
                logger.error("提交报告审批失败:{}",e);
                return false;
            }
        }else {
            try {
                //如果上传的是word转为pdf
                String originalFilename = file.getOriginalFilename();
                if (originalFilename.contains(".pdf")){
                    url = MinIoUtil.upload("report-download", file, GenID.getID() + ".pdf");
                    flag = true;
                }else {
                    XWPFDocument document = new XWPFDocument(file.getInputStream());
                    ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(document);
                    InputStream inputStream = FileAndFolderUtil.parseOut(b1);
                    url = MinIoUtil.upload("report-download", GenID.getID() + ".pdf", inputStream, "application/octet-stream");
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
            XWPFDocument doc = null;
            try {
                client.statObject(bluckName, fileName);
                InputStream object = client.getObject(bluckName, fileName);
                doc = new XWPFDocument(object);
                //相应pdf
                ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
                InputStream inputStream = FileAndFolderUtil.parseOut(b1);
                url = MinIoUtil.upload("report-download", reportCode + ".pdf", inputStream, "application/octet-stream");
            }catch (Exception e){
                logger.error("word转pdf异常");
            }
        }
        //更新签名
        reportMapper.updateVerAndIss(reportCode, verifyer, issuer, verifyerId,new Date(System.currentTimeMillis()), issuerId);
        //设置签名信息
        String url1 = "";
        try {
            url1 = insertPicToPdf(url,Long.parseLong(reportCode),inspector);
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
        reportMapper.updateUrl(reportCode, url, verifyer, issuer, verifyerId, issuerId,new Date(),ShiroUtils.getUserInfo().getName());
        logger.info("签名信息更新成功！:{}",reportCode+":"+url);
        //更新配合比信息
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(mixInfo)){
            TestSampleMixInfoEntity entity = JSON.parseObject(mixInfo,TestSampleMixInfoEntity.class);
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

    public String downLoadNew(MinioClient client, String code, Long id, List<Long> checkIds) throws Exception {
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
        List<ReportRecordDetailEntity> checkItemList = recordDetailEntityMapper.getCheckInfoByRecordIdAndCheckId(reportRecordEntity.getId(), checkIds);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
            //设置报告首页参数
            EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
            Map<String, String> textMap = new HashMap<>();
            textMap.put("code", reportRecordEntity.getReportCode());
            textMap.put("page", doc.getTables().size() + "");
            textMap.put("sampleName", reportRecordEntity.getSampleName());
            textMap.put("dept", entrustAddVo.getEntrustCompany());
            textMap.put("part", entrustAddVo.getProjectPart());
            textMap.put("checkType", entrustAddVo.getCheckPurpose());
            WordUtils.changeText(doc, textMap);
            //处理表格
            Iterator<XWPFTable> it = doc.getTablesIterator();
            //表格索引
            int i = 1;
            //获取表格信息
            while (it.hasNext()) {
                XWPFTable table = it.next();
                List<XWPFTableRow> rows = table.getRows();
                //存放表头信息
                EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                if (i == 1) {
                    rows.get(4).getCell(1).removeParagraph(0);
                    rows.get(4).getCell(1).setText(entrustHistoryDetail.getEntrustCompany());
                    rows.get(4).getCell(3).removeParagraph(0);
                    rows.get(4).getCell(3).setText(entrustHistoryDetail.getProjectName());
                    rows.get(5).getCell(1).removeParagraph(0);
                    rows.get(5).getCell(1).setText(entrustHistoryDetail.getProjectPart());
                    //样品信息
                    SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                    rows.get(6).getCell(1).removeParagraph(0);
                    rows.get(6).getCell(1).setText("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                            + "样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                            + "样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                            + "样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                            + "收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                    //检测依据
                    String checkBasis = getCheckBasis(id);
                    rows.get(7).getCell(1).removeParagraph(0);
                    rows.get(7).getCell(1).setText(checkBasis.equals("") ? "——" : checkBasis);
                    //判定依据
                    String judgeBasis = getJudgeBasis(id);
                    rows.get(7).getCell(3).removeParagraph(0);
                    rows.get(7).getCell(3).setText(judgeBasis.equals("") ? "——" : judgeBasis);
                    //检测日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    rows.get(8).getCell(1).removeParagraph(0);
                    rows.get(8).getCell(1).setText(sdf.format(entrustHistoryDetail.getAcceptanceDate()) + "~"
                            + sdf.format(reportRecordEntity.getReportCompleteTime() == null ? new Date() : reportRecordEntity.getReportCompleteTime())
                    );
                    //主要仪器
                    String equipment = getEquipment(id);
                    rows.get(9).getCell(1).removeParagraph(0);
                    rows.get(9).getCell(1).setText(equipment.equals("") ? "——" : equipment);
                    //委托编号
                    rows.get(10).getCell(1).removeParagraph(0);
                    rows.get(10).getCell(1).setText(entrustHistoryDetail.getEntrustmentNo() + "");
                    //检测类别
                    rows.get(10).getCell(3).removeParagraph(0);
                    rows.get(10).getCell(3).setText(entrustHistoryDetail.getCheckPurpose());
                    //批号
                    rows.get(11).getCell(1).removeParagraph(0);
                    rows.get(11).getCell(1).setText(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                    //生产厂家
                    rows.get(11).getCell(3).removeParagraph(0);
                    rows.get(11).getCell(3).setText(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                    //规格等级
                    if ("规格/等级".equals(rows.get(12).getCell(0).getText())) {
                        rows.get(12).getCell(1).removeParagraph(0);
                        rows.get(12).getCell(1).setText(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                    }
                    //代表数量
                    if ("代表数量".equals(rows.get(12).getCell(2).getText())) {
                        rows.get(12).getCell(3).removeParagraph(0);
                        rows.get(12).getCell(3).setText(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                    }
                }
                //存放检测数据
                for (ReportRecordDetailEntity item : checkItemList) {
                    int last = testProductDao.isLast(item.getCheckItemId().intValue());
                    if (last == 0) {
                        int page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                        int row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                        int column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                        if (i == page) {
                            rows.get(row).getCell(column + 1).removeParagraph(0);
                            rows.get(row).getCell(column + 1).setText(item.getSpecsContent());
                            rows.get(row).getCell(column + 2).removeParagraph(0);
                            rows.get(row).getCell(column + 2).setText(item.getCheckResult());
                            rows.get(row).getCell(column + 3).removeParagraph(0);
                            rows.get(row).getCell(column + 3).setText(item.getJudgeResult());
                        }
                    }
                }
                i++;
            }
        }
        ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
        //盖章
//        String sealUrl = reportRecordEntity.getSealUrl();
//        List<ImagePro> imagePros = com.google.api.client.util.Lists.newArrayList();
//        if (!StringUtils.isEmpty(sealUrl) || sealUrl != null) {
//            String[] split = sealUrl.split(",");
//            for (int i = 0; i < split.length; i++) {
//                ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
//                imagePros.add(pro);
//            }
//        }
//        InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
//        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
//        ByteArrayOutputStream b3 = ImageToPdfUtils.writeToPdf4(inputStream1, b2, imagePros);
        InputStream inputStream = FileAndFolderUtil.parseOut(b1);
        String url = "";
        url = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream, "application/octet-stream");
        updateReportUrl(reportRecordEntity.getId(), url, code);
//            ServletOutputStream outputStream = response.getOutputStream();
//            FileAndFolderUtil.parseIn(inputStream)
        return url;
    }
    public String downLoadNewCement(MinioClient client, String code, Long id, List<Long> checkIds) throws Exception {
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
        List<ReportRecordDetailEntity> checkItemList = recordDetailEntityMapper.getCheckInfoByRecordIdAndCheckId(reportRecordEntity.getId(), checkIds);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
            //设置报告首页参数
            EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
            Map<String, String> textMap = new HashMap<>();
            textMap.put("code", reportRecordEntity.getReportCode());
            textMap.put("page", doc.getTables().size() + "");
            textMap.put("sampleName", reportRecordEntity.getSampleName());
            textMap.put("dept", entrustAddVo.getEntrustCompany());
            textMap.put("part", entrustAddVo.getProjectPart());
            textMap.put("checkType", entrustAddVo.getCheckPurpose());
            WordUtils.changeText(doc, textMap);
            //处理表格
            Iterator<XWPFTable> it = doc.getTablesIterator();
            //表格索引
            int i = 1;
            //获取表格信息
            while (it.hasNext()) {
                XWPFTable table = it.next();
                List<XWPFTableRow> rows = table.getRows();
                //存放表头信息
                EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);

                if (i == 1) {
                    List<ConcreteSampleVo> samplesByEntrustID = sampleEntityMapper.getSamplesByEntrustID(id);
                    if(samplesByEntrustID.size()>7){
                        // 3.25 测试表格插入数据
                        int addRows = samplesByEntrustID.size()-5;
                        // 表格插入
                        XWPFDocument doc1 = new XWPFDocument();
                        XWPFTable newTable  = doc1.createTable(addRows,9);  //2行9格
                        // 创建表格后直接进行存放 后续多余数据
                        List<XWPFTableRow> dataTable = newTable.getRows();
                        int j=0;
                        for (int k = 5; k < samplesByEntrustID.size(); k++) {
//                            ConcreteSampleVo concreteSampleVo = samplesByEntrustID.get(k);
//                            //材料名称
//                            dataTable.get(j).getTableCells().get(0).setText("——");
//                            //规格
//                            dataTable.get(j).getTableCells().get(1).setText("——");
//                            //生产厂家
//                            dataTable.get(j).getTableCells().get(2).setText("——");
//                            //生产批号
//                            dataTable.get(j).getTableCells().get(3).setText("——");
//                            //样品数量
//                            dataTable.get(j).getTableCells().get(4).setText("——");
//                            //样品状态
//                            dataTable.get(j).getTableCells().get(5).setText("——");
//                            //样品编号
//                            dataTable.get(j).getTableCells().get(6).setText("——");
//                            //每m³用量
//                            dataTable.get(j).getTableCells().get(7).setText("——");
//                            //单位比
//                            dataTable.get(j).getTableCells().get(8).setText("——");
                            table.addRow(dataTable.get(j));
                            j++;
                        }
                        rows = table.getRows();
                    }
                    rows.get(4).getCell(1).removeParagraph(0);
                    rows.get(4).getCell(1).setText(entrustHistoryDetail.getEntrustCompany());
                    rows.get(4).getCell(3).removeParagraph(0);
                    rows.get(4).getCell(3).setText(entrustHistoryDetail.getProjectName());
                    rows.get(5).getCell(1).removeParagraph(0);
                    rows.get(5).getCell(1).setText(entrustHistoryDetail.getProjectPart());
                    //样品信息
                    SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                    rows.get(6).getCell(1).removeParagraph(0);
                    rows.get(6).getCell(1).setText("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                            + "样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                            + "样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                            + "样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                            + "收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                    //检测依据
                    String checkBasis = getCheckBasis(id);
                    rows.get(7).getCell(1).removeParagraph(0);
                    rows.get(7).getCell(1).setText(checkBasis.equals("") ? "——" : checkBasis);
                    //判定依据
                    String judgeBasis = getJudgeBasis(id);
                    rows.get(7).getCell(3).removeParagraph(0);
                    rows.get(7).getCell(3).setText(judgeBasis.equals("") ? "——" : judgeBasis);
                    //检测日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                    rows.get(8).getCell(1).removeParagraph(0);
                    rows.get(8).getCell(1).setText(sdf.format(entrustHistoryDetail.getAcceptanceDate()) + "~"
                            + sdf.format(reportRecordEntity.getReportCompleteTime() == null ? new Date() : reportRecordEntity.getReportCompleteTime())
                    );
                    //主要仪器
                    String equipment = getEquipment(id);
                    rows.get(9).getCell(1).removeParagraph(0);
                    rows.get(9).getCell(1).setText(equipment.equals("") ? "——" : equipment);
                    //委托编号
                    rows.get(10).getCell(1).removeParagraph(0);
                    rows.get(10).getCell(1).setText(entrustHistoryDetail.getEntrustmentNo() + "");
                    //检测类别
                    rows.get(10).getCell(3).removeParagraph(0);
                    rows.get(10).getCell(3).setText(entrustHistoryDetail.getCheckPurpose());
//                    //批号
//                    rows.get(11).getCell(1).removeParagraph(0);
//                    rows.get(11).getCell(1).setText(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
//                    //生产厂家
//                    rows.get(11).getCell(3).removeParagraph(0);
//                    rows.get(11).getCell(3).setText(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
//                    //规格等级
//                    if ("规格/等级".equals(rows.get(12).getCell(0).getText())) {
//                        rows.get(12).getCell(1).removeParagraph(0);
//                        rows.get(12).getCell(1).setText(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
//                    }
//                    //代表数量
//                    if ("代表数量".equals(rows.get(12).getCell(2).getText())) {
//                        rows.get(12).getCell(3).removeParagraph(0);
//                        rows.get(12).getCell(3).setText(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
//                    }
                    //设计强度
                    rows.get(11).getCell(1).removeParagraph(0);
                    rows.get(11).getCell(1).setText(entrustHistoryDetail.getDesignStrength());
                    //配制强度
                    rows.get(11).getCell(3).removeParagraph(0);
                    rows.get(11).getCell(3).setText(entrustHistoryDetail.getIntensityOfConfiguration());
                    //抗（渗、冻）等级
                    rows.get(12).getCell(1).removeParagraph(0);
                    rows.get(12).getCell(1).setText(entrustHistoryDetail.getAntifreezeLevel());
                    //水胶比
                    rows.get(12).getCell(3).removeParagraph(0);
                    rows.get(12).getCell(3).setText(entrustHistoryDetail.getWaterBinderRatio());
                    //单位用水量
                    rows.get(13).getCell(1).removeParagraph(0);
                    rows.get(13).getCell(1).setText(entrustHistoryDetail.getUnitWaterUse());
                    //砂率
                    rows.get(13).getCell(3).removeParagraph(0);
                    rows.get(13).getCell(3).setText(entrustHistoryDetail.getSandRatio());
                    //设计坍落度
                    rows.get(14).getCell(1).removeParagraph(0);
                    rows.get(14).getCell(1).setText(entrustHistoryDetail.getDesignSlump());
                    //拌和方式
                    rows.get(14).getCell(3).removeParagraph(0);
                    rows.get(14).getCell(3).setText(entrustHistoryDetail.getMixingWay());
                    //遍历样品信息
//                    for (int k = 0; k < samplesByEntrustID.size(); k++) {
//                        ConcreteSampleVo concreteSampleVo = samplesByEntrustID.get(k);
//                        //材料名称
//                        rows.get(16 + k).getCell(0).setText(concreteSampleVo.getSampleName());
////                        rows.get(16 + k).getTableCells().get(0).setText(concreteSampleVo.getSampleName());
//                        //规格
//                        rows.get(16 + k).getCell(1).setText(concreteSampleVo.getSpecs());
//                        //生产厂家
//                        rows.get(16 + k).getCell(2).setText(concreteSampleVo.getManufacturer());
//                        //生产批号
//                        rows.get(16 + k).getCell(3).setText(concreteSampleVo.getBatchNumber());
//                        //样品数量
//                        rows.get(16 + k).getCell(4).setText(concreteSampleVo.getSampleQuantity());
//                        //样品状态
//                        rows.get(16 + k).getCell(5).setText(concreteSampleVo.getOutward());
//                        //样品编号
//                        rows.get(16 + k).getCell(6).setText(concreteSampleVo.getSampleCode());
//                        //每m³用量
//                        rows.get(16 + k).getCell(7).setText("——");
//                        //单位比
//                        rows.get(16 + k).getCell(8).setText("——");
//                    }
                }
                //存放检测数据
                for (ReportRecordDetailEntity item : checkItemList) {
                    int last = testProductDao.isLast(item.getCheckItemId().intValue());
                    if (last == 0) {
                        int page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                        int row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                        int column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                        if (i == page) {
                            rows.get(row).getCell(column + 1).removeParagraph(0);
                            rows.get(row).getCell(column + 1).setText(item.getSpecsContent());
//                            rows.get(row).getCell(column + 2).removeParagraph(0);
//                            rows.get(row).getCell(column + 2).setText(item.getCheckResult());
//                            rows.get(row).getCell(column + 3).removeParagraph(0);
//                            rows.get(row).getCell(column + 3).setText(item.getJudgeResult());
                        }
                    }
                }
                i++;
            }
        }
        ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
        InputStream inputStream = FileAndFolderUtil.parseOut(b1);
        String url = "";
        url = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream, "application/octet-stream");
        updateReportUrl(reportRecordEntity.getId(), url, code);
        return url;
    }

    @Override
    public String downLoad2(MinioClient client, Long id) throws Exception {
        StringBuilder url = new StringBuilder();
        //通过ID查询委托单信息
        String entrustTestType = entrustEntityMapper.getEntrustTestType(id);
        List<String> allReportCode = entityMapper.getAllReportCode(id);
        List<LabelValueVo> checkReportRel = entityMapper.getCheckReportRel(id);
        if ("原材检测".equals(entrustTestType)) {
            for (String s : allReportCode) {
                List<Long> checkIds = Lists.newArrayList();
                for (LabelValueVo labelValueVo : checkReportRel) {
                    if (s.equals(labelValueVo.getLabel())) {
                        checkIds.add(labelValueVo.getValue());
                    }
                }
                String s1 = downLoadNew(client, s, id, checkIds);
                url.append(s1);
            }
        } else if ("配合比".equals(entrustTestType)) {
            for (String s : allReportCode) {
                List<Long> checkIds = Lists.newArrayList();
                for (LabelValueVo labelValueVo : checkReportRel) {
                    if (s.equals(labelValueVo.getLabel())) {
                        checkIds.add(labelValueVo.getValue());
                    }
                }
                String s1 = downLoadNewCement(client, s, id, checkIds);
                url.append(s1);
            }
        }
        return url.toString();
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
    public Boolean seal(Long entrustId, String title, String fileType) {
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
                    Long aLong = entityMapper.checkExist(entrustId);
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
        Long aLong = entityMapper.checkExist(reqBean.getEntrustId());
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
        Long aLong = entityMapper.checkExist(reqBean.getEntrustId());
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
        //根据contractId查询任务id
        Long entrustId = entityMapper.getEntrustIdByCid(contractId);
        //更新状态，更新
        taskMapper.updateEntrustById(entrustId, 10);
        //更新报告状态
        entityMapper.updateFileState(contractId, "5");
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
        List<QuotaRes> lis = Lists.newArrayList();
        Set<String> set = map.keySet();
        for (String key : set) {
            QuotaRes res = new QuotaRes();
            res.setKey(key);
            res.setValue(map.get(key));
            lis.add(res);
        }
        return lis;
    }

    @SneakyThrows
    @Override
    public ReportResBean submitDownLoad(MinioClient client, List<ConclusionEntity> list, Long id) {
        //2代表报告头2页
        String key = "——";
        //int totalPage = 2;
        int totalPageNew = 0;
        Map<Integer,XWPFDocument> map = new HashedMap();
        //处理坐标提示信息
        ReportResBean resBean = new ReportResBean();
        Map<String,String> mesMap = new HashedMap();
        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
        //TODO 兼容中间报告

        int index = 1;
        for (ConclusionEntity conclusionEntity:list) {
            String[] split = conclusionEntity.getUrl().split("\\?");
            String[] strings = split[0].split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            XWPFDocument doc = null;
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            doc = new XWPFDocument(object);
            totalPageNew = doc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages()+totalPageNew;
            logger.debug("报告页数:{}",totalPageNew);
            //写入数据
            List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
                int size = doc.getTables().size();
                //处理表格
                Iterator<XWPFTable> it = doc.getTablesIterator();
                //表格索引
                int i = 1;
                //获取表格信息
                while (it.hasNext()) {
                    //totalPage++;
                    index++;
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 1) {
                        WordUtils.replaceCellText(rows.get(3).getCell(1),key,"河南省公路工程试验检测中心有限公司");
                        WordUtils.replaceCellText(rows.get(3).getCell(3),key,reportRecordEntity.getReportCode());
                        WordUtils.replaceCellText(rows.get(4).getCell(1),key,entrustHistoryDetail.getEntrustCompany());
                        WordUtils.replaceCellText(rows.get(4).getCell(3),key,entrustHistoryDetail.getProjectName());
                        WordUtils.replaceCellText(rows.get(5).getCell(1),key,entrustHistoryDetail.getProjectPart());
                        //样品信息
                        SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                        WordUtils.replaceCellText(rows.get(6).getCell(1),key,"样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode().replace("~","~"))
                                + "；样品数量：" + (sampleEntity.getSampleQuantity() == null ? "——" : sampleEntity.getSampleQuantity())
                                + "；样品状态：" + (StringUtils.isEmpty(sampleEntity.getOutwardDescribe()) ? "——" : sampleEntity.getOutwardDescribe())
                                + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                        //检测依据
                        String checkBasis = getCheckBasis(id);
                        WordUtils.replaceCellText(rows.get(7).getCell(1),key,checkBasis.equals("") ? "——" : checkBasis);
                        //判定依据
                        String judgeBasis = getJudgeBasis(id);
                        WordUtils.replaceCellText(rows.get(7).getCell(3),key,judgeBasis.equals("") ? "——" : judgeBasis);
                        //检测日期 TODO 实验开始日期 -实验结束日期 时间起始相同展示一个时间即可
                        //根据委托单id，查询委托任务下实验开始的时间和实验结束的时间
                        Date start = taskMapper.getStartTime(id);
                        Date end = taskMapper.getEndTime(id);
                        String s = DateUtil.formatDate(start);
                        String e = DateUtil.formatDate(end);
                        if (start != null && end != null){
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                            if (s.equals(e)){
                                WordUtils.replaceCellText(rows.get(8).getCell(1),key,s);
                            }else {
                                WordUtils.replaceCellText(rows.get(8).getCell(1),key,s + "~" + e);
                            }
                        }
                        //主要仪器
                        String equipment = getEquipment(id);
                        WordUtils.replaceCellText(rows.get(9).getCell(1),key,equipment.equals("") ? "——" : equipment);
                        //委托编号
                        WordUtils.replaceCellText(rows.get(10).getCell(1),key,entrustHistoryDetail.getEntrustmentNo() + "");
                        //检测类别
                        WordUtils.replaceCellText(rows.get(10).getCell(3),key,entrustHistoryDetail.getCheckPurpose());
                        //批号
                        WordUtils.replaceCellText(rows.get(11).getCell(1),key,sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                        //生产厂家
                        WordUtils.replaceCellText(rows.get(11).getCell(3),key,sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                        //规格等级
                        WordUtils.replaceCellText(rows.get(12).getCell(1),key,sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                        //代表数量
                        WordUtils.replaceCellText(rows.get(12).getCell(3),key,sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                    }
                    //过滤每个报告模板的检测项
                    List<ReportRecordDetailEntity> entities = Lists.newArrayList();
                    List<ReportRecordDetailEntity> entities1 = Lists.newArrayList();
                    String[] split11 = conclusionEntity.getUrl().split("\\?");
                    String[] strings11 = split11[0].split("\\/");
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
                            int page = 0;
                            int row = 0;
                            int column = 0;
                            if (last == 0) {
                                try {
                                    page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                                    row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                                    column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                                }catch (Exception e){
                                    mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标格式错误");
                                    logger.error("检测项在报告中的坐标格式错误:{}",e);
                                    continue;
                                }
                                if (i == page) {
                                    try {
                                        WordUtils.replaceCellText(rows.get(row).getCell(column + 1),key,item.getSpecsContent());
                                        WordUtils.replaceCellText(rows.get(row).getCell(column + 2),key,item.getCheckResult());
                                        WordUtils.replaceCellText(rows.get(row).getCell(column + 3),key,item.getJudgeResult());
                                    }catch (Exception e){
                                        mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标错误");
                                        logger.error("检测项在报告中的坐标错误:{}",e);
                                        continue;
                                    }
                                }
                            }
                        }else {
                            mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标未录入");
                        }
                    }
                    //处理附加声明和检测结论
                    if (i==size){
                        XWPFTable xwpfTable = doc.getTables().get(size - 1);
                        int size1 = xwpfTable.getRows().size();
                        rows.get(size1-3).getCell(0).removeParagraph(0);
                        rows.get(size1-3).getCell(0).setText("检测结论："+conclusionEntity.getConclusion());
                        rows.get(size1-2).getCell(0).removeParagraph(0);
                        rows.get(size1-2).getCell(0).setText("附加声明："+conclusionEntity.getAdditional());
                    }
                    i++;
                }
                //按照顺序存放doc
                map.put(index,doc);
            }
        }
        //存放提示信息
        resBean.setMap(mesMap);
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.docx");
        XWPFDocument topDoc = new XWPFDocument(fileStream);;
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        logger.debug("本次报告总页数:{}",totalPageNew);
        setReportTop(topDoc,entrustAddVo,reportRecordEntity,totalPageNew);
        //报告头部合并顺序1
        map.put(1,topDoc);
        //将报告合并成一个完整的word
        XWPFDocument document = AsposeUtil.mergeDoc(map);
        //上传合并完成的doc到服务器
        MultipartFile multipartFile = AsposeUtil.xwpfDocumentToCommonsMultipartFile(document, reportRecordEntity.getReportCode() + ".pdf");
        //MultipartFile file = (MultipartFile) document;
        String url = MinIoUtil.upload("report-download", multipartFile, reportRecordEntity.getReportCode() + ".docx");
        StringBuilder stringBuilder = new StringBuilder();
        for (ConclusionEntity entity:list) {
            if (entity.getUrl().contains("?")){
                entity.setUrl(entity.getUrl().substring(0,url.indexOf("?")));
            }
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        updateReportUrl(reportRecordEntity.getId(), url, stringBuilder.toString().substring(0,stringBuilder.length()-2));
        //存放提示信息
        resBean.setUrl(url);
        return resBean;
    }

    @SneakyThrows
    @Override
    public ReportResBean submitDownLoad1(MinioClient client, List<ConclusionEntity> list, Long id) {
        int totalPageNew = 0;
        Map<Integer, Integer> countMap = new LinkedHashMap();
        Map<Integer, Workbook> map = new LinkedHashMap<>();
        //处理坐标提示信息
        ReportResBean resBean = new ReportResBean();
        Map<String,String> mesMap = new HashedMap();
        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
        int index = 1;
        for (ConclusionEntity conclusionEntity:list) {
            conclusionEntity.setUrl("http://192.168.2.35:9000/file-resources/掺合料.xls");
            String[] split = conclusionEntity.getUrl().split("\\?");
            String[] strings = split[0].split("\\/");
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
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 0) {
                        cells.get("${检测单位名称}").setValue("检测单位名称：河南省公路工程试验检测中心有限公司");
                        cells.get("${报告编号}").setValue(reportRecordEntity.getReportCode());
                        cells.get("${委托单位}").setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getEntrustCompany())?"——":entrustHistoryDetail.getEntrustCompany());
                        cells.get("${工程名称}").setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectName())?"——":entrustHistoryDetail.getProjectName());
                        cells.get("${工程部位}").setValue(org.apache.commons.lang.StringUtils.isEmpty(entrustHistoryDetail.getProjectPart())?"——":entrustHistoryDetail.getProjectPart());
                        //样品信息
                        SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                        cells.get("${样品信息}").setValue("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode().replace("~","~"))
                                + "；样品数量：" + (sampleEntity.getSampleQuantity() == null ? "——" : sampleEntity.getSampleQuantity())
                                + "；样品状态：" + (StringUtils.isEmpty(sampleEntity.getOutwardDescribe()) ? "——" : sampleEntity.getOutwardDescribe())
                                + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                        //检测依据
                        String checkBasis = getCheckBasis(id);
                        cells.get("${检测依据}").setValue(checkBasis.equals("") ? "——" : checkBasis);
                        //判定依据
                        String judgeBasis = getJudgeBasis(id);
                        cells.get("${判定依据}").setValue(judgeBasis.equals("") ? "——" : judgeBasis);
                        //根据委托单id，查询委托任务下实验开始的时间和实验结束的时间
                        Date start = taskMapper.getStartTime(id);
                        Date end = taskMapper.getEndTime(id);
                        String s = DateUtil.formatDate(start);
                        String e = DateUtil.formatDate(end);
                        if (start != null && end != null){
                            if (s.equals(e)){
                                cells.get("${检测日期}").setValue(s);
                            }else {
                                cells.get("${检测日期}").setValue(s + "~" + e);
                            }
                        }
                        //主要仪器
                        String equipment = getEquipment(id);
                        cells.get("${仪器设备}").setValue(equipment.equals("") ? "——" : equipment);
                        //委托编号
                        cells.get("${委托编号}").setValue(entrustHistoryDetail.getEntrustmentNo() + "");
                        //检测类别
                        cells.get("${检测类别}").setValue(entrustHistoryDetail.getCheckPurpose());
                        //批号
                        cells.get("${批号}").setValue(sampleEntity.getBatchNumber() == null ? "——" : sampleEntity.getBatchNumber());
                        //生产厂家
                        cells.get("${生产厂家}").setValue(sampleEntity.getManufacturer() == null ? "——" : sampleEntity.getManufacturer());
                        //规格等级
                        cells.get("${规格}").setValue(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                        //代表数量
                        cells.get("${代表数量}").setValue(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                    }
                    //过滤每个报告模板的检测项
                    List<ReportRecordDetailEntity> entities = Lists.newArrayList();
                    List<ReportRecordDetailEntity> entities1 = Lists.newArrayList();
                    String[] split11 = conclusionEntity.getUrl().split("\\?");
                    String[] strings11 = split11[0].split("\\/");
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
                                    specsContent = item.getCoordinate().split(",")[0];
                                    checkResult = item.getCoordinate().split(",")[1];
                                    judgeResult = item.getCoordinate().split(",")[2];
                                }catch (Exception e){
                                    mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标格式错误");
                                    logger.error("检测项在报告中的坐标格式错误:{}",e);
                                    continue;
                                }
                                try {
                                    if (StringUtils.isNotEmpty(specsContent)){
                                        cells.get(specsContent).setValue(item.getSpecsContent());
                                    }
                                    if (StringUtils.isNotEmpty(checkResult)){
                                        cells.get(checkResult).setValue(item.getCheckResult());
                                    }
                                    if (StringUtils.isNotEmpty(judgeResult)){
                                        cells.get(judgeResult).setValue(item.getJudgeResult());
                                    }
                                }catch (Exception e){
                                    logger.error("检测项在报告中的坐标错误:{}",e);
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
                        cells.get("${检测结论}").setValue("检测结论："+conclusionEntity.getConclusion());
                        cells.get("${附加声明}").setValue("附加声明："+conclusionEntity.getAdditional());
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
        setReportTop1(topDoc,entrustAddVo,reportRecordEntity,totalPageNew);
        //合并成一个excel
        Workbook document = workbookCopy(topDoc,map);
        //处理页码 TODO
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
        updateReportUrl(reportRecordEntity.getId(), url, stringBuilder.toString().substring(0,stringBuilder.length()-2));
        //存放提示信息
        resBean.setUrl(url);
        FileAndFolderUtil.delete(path);
        return resBean;
    }

    /**
     * 处理合并完整的excel每个报告的页码
     * @param document
     * @param countMap
     */
    private void handlerPage(Workbook document, Map<Integer, Integer> countMap) {


    }

    @SneakyThrows
    @Override
    public ReportResBean submitDownLoadMix(MinioClient client, List<ConclusionEntity> list, Long id,TestSampleMixInfoEntity mixInfoEntity) {
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
        //2代表报告头2页
        String key = "——";
        //int totalPage = 2;
        int totalPageNew = 0;
        Map<Integer,XWPFDocument> map = new HashedMap();
        //处理坐标提示信息
        ReportResBean resBean = new ReportResBean();
        Map<String,String> mesMap = new HashedMap();

        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
        int index = 1;
        for (ConclusionEntity conclusionEntity:conclusionEntityList) {
            String[] split = conclusionEntity.getUrl().split("\\?");
            String[] strings = split[0].split("\\/");
            String bluckName = strings[3];
            String fileName = strings[4];
            XWPFDocument doc = null;
            client.statObject(bluckName, fileName);
            InputStream object = client.getObject(bluckName, fileName);
            doc = new XWPFDocument(object);
            totalPageNew = doc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages()+totalPageNew;
            //写入数据
            List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
                int size = doc.getTables().size();
                //处理表格
                Iterator<XWPFTable> it = doc.getTablesIterator();
                //表格索引
                int i = 1;
                //获取表格信息
                while (it.hasNext()) {
                    //totalPage++;
                    index++;
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 1) {
                        WordUtils.replaceCellText(rows.get(3).getCell(1),key,"河南省公路工程试验检测中心有限公司");
                        WordUtils.replaceCellText(rows.get(3).getCell(3),key,reportRecordEntity.getReportCode());
                        WordUtils.replaceCellText(rows.get(4).getCell(1),key,entrustHistoryDetail.getEntrustCompany());
                        WordUtils.replaceCellText(rows.get(4).getCell(3),key,entrustHistoryDetail.getProjectName());
                        WordUtils.replaceCellText(rows.get(5).getCell(1),key,entrustHistoryDetail.getProjectPart());
                        //样品信息
                        SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                        WordUtils.replaceCellText(rows.get(6).getCell(1),key,"样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode().replace("~","~"))
                                + "；样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                                + "；样品状态：" + (StringUtils.isEmpty(sampleEntity.getOutward()) ? "——" : sampleEntity.getOutward())
                                + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
                        //检测依据
                        String checkBasis = getCheckBasis(id);
                        WordUtils.replaceCellText(rows.get(7).getCell(1),key,checkBasis.equals("") ? "——" : checkBasis);
                        //判定依据
                        String judgeBasis = getJudgeBasis(id);
                        WordUtils.replaceCellText(rows.get(7).getCell(3),key,judgeBasis.equals("") ? "——" : judgeBasis);
                        //检测日期
                        //根据委托单id，查询委托任务下实验开始的时间和实验结束的时间
                        Date start = taskMapper.getStartTime(id);
                        Date end = taskMapper.getEndTime(id);
                        String s = DateUtil.formatDate(start);
                        String e = DateUtil.formatDate(end);
                        if (start != null && end != null){
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                            if (s.equals(e)){
                                WordUtils.replaceCellText(rows.get(8).getCell(1),key,s);
                            }else {
                                WordUtils.replaceCellText(rows.get(8).getCell(1),key,s + "~" + e);
                            }
                        }
                        //主要仪器
                        String equipment = getEquipment(id);
                        WordUtils.replaceCellText(rows.get(9).getCell(1),key,equipment.equals("") ? "——" : equipment);
                        //委托编号
                        WordUtils.replaceCellText(rows.get(10).getCell(1),key,entrustHistoryDetail.getEntrustmentNo() + "");
                        //检测类别
                        WordUtils.replaceCellText(rows.get(10).getCell(3),key,entrustHistoryDetail.getCheckPurpose());
                        //设计参数
                        if (mixInfoEntity != null){
                            WordUtils.replaceCellText(rows.get(11).getCell(1),key,mixInfoEntity.getDesignStrength());
                            WordUtils.replaceCellText(rows.get(11).getCell(3),key,mixInfoEntity.getIntensityConfiguration());
                            WordUtils.replaceCellText(rows.get(12).getCell(1),key,mixInfoEntity.getAntifreezeLevel());
                            WordUtils.replaceCellText(rows.get(12).getCell(3),key,mixInfoEntity.getWaterBinderRatio());
                            WordUtils.replaceCellText(rows.get(13).getCell(1),key,mixInfoEntity.getUnitWaterUse());
                            WordUtils.replaceCellText(rows.get(13).getCell(3),key,mixInfoEntity.getSandRatio());
                            WordUtils.replaceCellText(rows.get(14).getCell(1),key,mixInfoEntity.getDesignSlump());
                            WordUtils.replaceCellText(rows.get(14).getCell(3),key,mixInfoEntity.getMixingWay());
                        }else {
                            TestSampleMixInfoEntity entity = mixInfoEntityMapper.selectByEntrustId(id);
                            if (entity != null){
                                WordUtils.replaceCellText(rows.get(11).getCell(1),key,entity.getDesignStrength());
                                WordUtils.replaceCellText(rows.get(11).getCell(3),key,entity.getIntensityConfiguration());
                                WordUtils.replaceCellText(rows.get(12).getCell(1),key,entity.getAntifreezeLevel());
                                WordUtils.replaceCellText(rows.get(12).getCell(3),key,entity.getWaterBinderRatio());
                                WordUtils.replaceCellText(rows.get(13).getCell(1),key,entity.getUnitWaterUse());
                                WordUtils.replaceCellText(rows.get(13).getCell(3),key,entity.getSandRatio());
                                WordUtils.replaceCellText(rows.get(14).getCell(1),key,entity.getDesignSlump());
                                WordUtils.replaceCellText(rows.get(14).getCell(3),key,entity.getMixingWay());
                            }
                        }
                        //填充配合比下原材样品信息
                        List<TestSampleEntity> testSampleEntities = testSampleEntityMapper.selectByPid(entrustHistoryDetail.getSamples().get(0).getId());
                        if (testSampleEntities.size()>7){
                            AsposeUtil.addRows(table,16,testSampleEntities.size()-7);
                        }
                        for (int j=0;j<testSampleEntities.size();j++) {
                            WordUtils.replaceCellText(rows.get(j+16).getCell(0),key,testSampleEntities.get(j).getSampleName());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(1),key,testSampleEntities.get(j).getSpecs());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(2),key,testSampleEntities.get(j).getManufacturer());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(3),key,testSampleEntities.get(j).getBatchNumber());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(4),key,testSampleEntities.get(j).getGeneration());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(5),key,testSampleEntities.get(j).getOutward());
                            WordUtils.replaceCellText(rows.get(j+16).getCell(6),key,testSampleEntities.get(j).getSampleCode());
                        }
                    }
                    if (i==size){
                        //过滤每个报告模板的检测项
                        List<ReportRecordDetailEntity> entities = Lists.newArrayList();

                        String[] split11 = conclusionEntity.getUrl().split("\\?");
                        String[] strings11 = split11[0].split("\\/");
                        String fileName11 = strings11[4];
                        List<Long> longList = itemDao.getItemsByTemplateLikeUrl(fileName11);
                        for (ReportRecordDetailEntity entity:checkItemList) {
                            for (Long itemId:longList) {
                                if (entity.getCheckItemId().equals(itemId)){
                                    entities.add(entity);
                                }
                            }
                        }
                        //存放检测数据checkItemList为该报告模板所属的检测项
                        for (ReportRecordDetailEntity item : entities) {
                            if (org.apache.commons.lang3.StringUtils.isNotEmpty(item.getCoordinate())){
                                int last = testProductDao.isLast(item.getCheckItemId().intValue());
                                int page = 0;
                                int row = 0;
                                int column = 0;
                                if (last == 0) {
                                    try {
                                        page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                                        row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                                        column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                                    }catch (Exception e){
                                        mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标格式错误");
                                        logger.error("检测项在报告中的坐标格式错误:{}",e);
                                        continue;
                                    }
                                    if (i == page) {
                                        try {
                                            WordUtils.replaceCellText(rows.get(row).getCell(column + 1),key,item.getCheckResult());
                                        }catch (Exception e){
                                            mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标错误");
                                            logger.error("检测项在报告中的坐标错误:{}",e);
                                            continue;
                                        }
                                    }
                                }
                            }else {
                                mesMap.put(item.getCheckItemName(),"检测项在报告中的坐标未录入");
                            }
                        }
                        //处理附加声明和检测结论
                        XWPFTable xwpfTable = doc.getTables().get(size - 1);
                        int size1 = xwpfTable.getRows().size();
                        rows.get(size1-3).getCell(0).removeParagraph(0);
                        rows.get(size1-3).getCell(0).setText("检测结论："+conclusionEntity.getConclusion());
                        rows.get(size1-2).getCell(0).removeParagraph(0);
                        rows.get(size1-2).getCell(0).setText("附加声明："+conclusionEntity.getAdditional());
                    }
                    i++;
                }
                //按照顺序存放doc
                map.put(index,doc);
            }
        }
        //存放提示信息
        resBean.setMap(mesMap);
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.docx");
        XWPFDocument topDoc = new XWPFDocument(fileStream);
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        setReportTop(topDoc,entrustAddVo,reportRecordEntity,totalPageNew);
        //报告头部合并顺序1
        map.put(1,topDoc);
        //将报告合并成一个完整的word
        XWPFDocument document = AsposeUtil.mergeDoc(map);
        //上传合并完成的doc到服务器
        MultipartFile multipartFile = AsposeUtil.xwpfDocumentToCommonsMultipartFile(document, reportRecordEntity.getReportCode() + ".pdf");
        //MultipartFile file = (MultipartFile) document;
        String url = MinIoUtil.upload("report-download", multipartFile, reportRecordEntity.getReportCode() + ".docx");
        resBean.setUrl(url);
        StringBuilder stringBuilder = new StringBuilder();
        for (ConclusionEntity entity:list) {
            if (entity.getUrl().contains("?")){
                entity.setUrl(entity.getUrl().substring(0,url.indexOf("?")));
            }
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        updateReportUrl(reportRecordEntity.getId(), url, stringBuilder.toString().substring(0,stringBuilder.length()-2));
        return resBean;
    }

    /**
     * 填充报告头部信息
     * @param topDoc
     * @param entrustAddVo
     */
    private void setReportTop(XWPFDocument topDoc, EntrustAddVo entrustAddVo,ReportRecordEntity reportRecordEntity,int totalPage) {
        XWPFTable table = topDoc.getTables().get(0);
        List<XWPFTableRow> rows = table.getRows();
        rows.get(1).getCell(2).setText(reportRecordEntity.getReportCode());
        rows.get(2).getCell(2).setText(totalPage+"");
        rows.get(12).getCell(2).setText(reportRecordEntity.getSampleName());
        rows.get(13).getCell(2).setText(entrustAddVo.getEntrustCompany());
        //rows.get(14).getCell(1).setText(entrustAddVo.getProjectPart());
        rows.get(14).getCell(2).setText(entrustAddVo.getCheckPurpose());
    }

    /**
     * 填充报告头部信息
     * @param topDoc
     * @param entrustAddVo
     */
    private void setReportTop1(Workbook topDoc, EntrustAddVo entrustAddVo,ReportRecordEntity reportRecordEntity,int totalPage) {
        Worksheet worksheet = topDoc.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        cells.get("Y6").setValue(reportRecordEntity.getReportCode());
        cells.get("Y7").setValue(totalPage+"");
        cells.get("J32").setValue(reportRecordEntity.getSampleName());
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
    public List<ConclusionEntity> getResut(Long entrustId) {
        List<ReportTemplateEntity> templateList = reportService.getReportTemplateList(entrustId);
        List<ConclusionEntity> list = Lists.newArrayList();
        EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(entrustId);
        List<SampleEntity> samples = entrustHistoryDetail.getSamples();
        for (ReportTemplateEntity templateEntity:templateList) {
            for (SampleEntity sampleEntity :samples) {
                String des = delItemDes(sampleEntity.getJudgmentBasisVos(),templateEntity.getReportFileUri(),entrustId);
                String judgeBasis = getJudgeBasis(entrustId);
                ConclusionEntity conclusionEntity =  new ConclusionEntity();
                conclusionEntity.setSampleId(sampleEntity.getId());
                conclusionEntity.setUrl(templateEntity.getReportFileUri());
                conclusionEntity.setConclusion("经检测，该"+sampleEntity.getSampleName()+"样品,"+des+"均符合"+judgeBasis+"中的技术要求。");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("1.委托人："+entrustHistoryDetail.getEntrustPeople()+"；");
                stringBuilder.append("2."+(StringUtils.isEmpty(entrustHistoryDetail.getWitnessUint())?"见证单位：无":"见证单位："+entrustHistoryDetail.getWitnessUint())+"；");
                stringBuilder.append("3."+(StringUtils.isEmpty(entrustHistoryDetail.getWitnessPerson())?"见证人：无":"见证人："+entrustHistoryDetail.getWitnessPerson())+"；");
                stringBuilder.append("4.委托方提供："+ (StringUtils.isEmpty(entrustHistoryDetail.getRemark())?"无":entrustHistoryDetail.getRemark())+" ；");
                conclusionEntity.setAdditional(stringBuilder.toString());
                list.add(conclusionEntity);
            }
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
    public String insertPicToPdf(String pdfUrl, Long entrustId,String inspector) throws Exception {
        HashSet<String> delList = new HashSet<>();
        ReportRecordEntity detailByEntrustId = reportMapper.getDetailByEntrustId(entrustId);//审核人、签发人
        logger.info("查询签发复合信息:{}",JSON.toJSONString(detailByEntrustId));
        /*List<String> stringList = taskMapper.getInspectorByEntrustId(entrustId);
        List<String> stringList1 = Lists.newArrayList();
        for (String string:stringList) {
            String[] split = string.split(",");
            for (String s:split) {
                stringList1.add(s);
            }
        }
        List<Long> list1 = Lists.newArrayList();//检测人
        for (String s:stringList1) {
            String[] split1 = s.split("&");
            list1.add(Long.parseLong(split1[1]));
        }*/
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
        List<ReportDetailListVo> reportDetailListVos = entityMapper.reportList(paramVo);
        PageInfo<ReportDetailListVo> pageInfo = PageInfoUtils.list2PageInfo(reportDetailListVos,
                paramVo.getPageNum(),
                paramVo.getPageSize());
        return pageInfo;
    }

    @Override
    public PageInfo middleReportList(Integer pageNum, Integer pageSize,Integer state, String search) {
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
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
            reportListVo.setSampleName(sampleName.toString());
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
    public String getUrlById(Long id) {
        return reportMapper.getUrlById(id);
    }

    @Override
    public Boolean category(SealEntity sealEntity) {
        List<SealEntity> list = Lists.newArrayList();
        for (Long id:sealEntity.getId()) {
            SealEntity entity = new SealEntity();
            entity.setSealer(sealEntity.getSealer());
            entity.setSealTime(sealEntity.getSealTime());
            entity.setSealType(sealEntity.getSealType());
            entity.setKey(id);
            list.add(entity);
        }
        //设置状态和用章类型
        try {
            reportMapper.updateCategory(list);
            return true;
        }catch (Exception e){
            logger.error("更新印章类型失败:{}",e);
            return false;
        }
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
        PageHelper.startPage(pageNum,pageSize);
        List<ReportRecordEntity> list = reportMapper.historyList(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
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
        List<ReportRecordEntity> list = reportMapper.exportRecords(reportCode,reportType,sealType,ids,startDate==null?null:new Date(startDate),endDate==null?null:new Date(endDate));
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
            cells.get(row+n).setValue(entity.getInspector().split("&")[0]+","+entity.getRecorder().split("&")[0]);
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
            cells.get(row+n).setValue(Double.valueOf(entity.getActualPrice()));
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
                    String newSheetName = null != finalWork.getWorksheets().get(name) ? name + 1 : name;
                    // 开始合并
                    Worksheet worksheetS = finalWork.getWorksheets().add(newSheetName);
                    worksheetS.copy(worksheet);
                    int count1 = finalWork.getWorksheets().getCount();
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
    public int updateInspector(String reportCode, String inspector) {


        return reportMapper.updateInspector(reportCode,inspector);
    }

}
