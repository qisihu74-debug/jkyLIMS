package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.QiYueSuoSealEntity;
import com.lims.manage.erp.entity.QuotaEntity;
import com.lims.manage.erp.entity.QuotaRes;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.http.QiYueSuoDocment;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.ReportTemplateEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestReportQualifcationDao;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.WordUtils;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.JudgmentBasisVo;
import com.lims.manage.erp.vo.ReportCheckItemDetailVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportHistoryDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import io.minio.MinioClient;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportServiceImpl implements ReportService {
    Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportRecordEntityMapper entityMapper;
    @Autowired
    private ReportTemplateEntityMapper templateEntityMapper;

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
    private TestReportQualifcationDao dao;

    @Override
    public List<ReportListVo> getReportList() {
        // 报告生成列表
        List<ReportListVo> reportList = reportMapper.getReportList();
        // 已经生成的 报告列表
        List<ReportRecordEntity> list = recordEntityMapper.getReportList();
        //                        0 审批灰色 1 是可以审批
        Iterator<ReportListVo> iterator = reportList.iterator();
        while (iterator.hasNext()){
            ReportListVo reportListVo=iterator.next();
            for(ReportRecordEntity reportRecordEntity:list){
                // 如果test_report_report 的 EntrustmentId = reportListVo.getId()
                if(reportRecordEntity.getEntrustmentId().equals(reportListVo.getId())){
                    // state =1 并且 没有提交审批
                    if(reportRecordEntity.getState().equals("1")&&reportRecordEntity.getReportCompleteTime()==null){
                        // 报告已完成
                        reportListVo.setState(1);
                    }
                    // 报告完成 已经提交
                    if(reportRecordEntity.getReportCompleteTime()!=null){
                        iterator.remove();
                    }
                    if(reportRecordEntity.getState().equals("2")){
                        // 报告已完成
                        reportListVo.setState(0);
                    }
                }
                if(reportListVo.getState()==null){
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
    public PageInfo makeReport(Integer pageNum,Integer pageSize,String search) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.getReportList2(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()),search);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }


    public PageInfo reportDownloadList(Integer pageNum,Integer pageSize,String search) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.reportDownloadList(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()),search);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    /**
     * 提交审批
     * @param id
     * @param name 报告提交申请人
     * @return
     */
    @Transactional
    @Override
    public Boolean getReportSubmit(Long id,String name) {
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
        if(entrustBaseInfo.getState()!=null&&entrustBaseInfo.getState()<7){
            taskMapper.updateEntrustById(entrustBaseInfo.getId(),7);
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
        if(entrustBaseInfo.getState()!=null&&entrustBaseInfo.getState()<7){
            taskMapper.updateEntrustById(entrustBaseInfo.getId(),7);
        }
        return true;
    }

    @Override
    public PageInfo getReportList_history(String search,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        reportListVo.setDeptIds(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
        List<ReportListVo> list = reportMapper.getReportList_history(reportListVo);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    public PageInfo reportDownloadListHistory(String search,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        reportListVo.setDeptIds(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
        List<ReportListVo> list = reportMapper.reportDownloadListHistory(reportListVo);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public ReportSampleDetailVo getReportList_history_details(Long id) {
        ReportSampleDetailVo reportSampleDetailVo = new ReportSampleDetailVo();
        // 获取报告头部信息
        List<ReportSampleDetailVo> list = reportMapper.getReportHeadDetails(id);
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
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemList(id,teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
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
        return reportMapper.getReportDetail(taskId,userTeamIds);
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
            if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<200){
                taskMapper.updateEntrustById(entrustAddVo.getId(),200);
            }
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public Boolean preserve1(ReportPreserveVo vo) {
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByEntrustId(vo.getEntrustmentId());
//        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByTaskId(vo.getTaskId());
        if (reportRecordEntity1 != null) {
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                int insert1 = recordDetailEntityMapper.updateByRecordIdSelective(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            reportRecordEntity1.setState(state);
            if ("1".equals(state)) {
                reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            long recordId = GenID.getID();
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                int insert1 = recordDetailEntityMapper.insert(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
            reportRecordEntity.setState(state);
            if ("1".equals(state)) {
                reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            //生成报告编号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String year = sdf.format(new Date());
            Integer maxCode = recordEntityMapper.getMaxCode(year);
            if(maxCode == null){
                reportRecordEntity.setReportCode("ZX-"+year+"-JC-0001");
            }else{
                int newCode = maxCode + 1;
                reportRecordEntity.setReportCode("ZX-"+year+"-JC-"+newCode);
            }
            reportRecordEntity.setId(recordId);
            reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    @Transactional
    @Override
    public Boolean preserve(ReportPreserveVo vo) {
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByEntrustId(vo.getEntrustmentId());
//        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByTaskId(vo.getTaskId());
        if (reportRecordEntity1 != null) {
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                List<Long> checkItemIds = recordDetailEntityMapper.getCheckItemIds(reportRecordEntity1.getId());
                int insert1;
                if(checkItemIds.contains(e.getCheckItemId())){
                    insert1 = recordDetailEntityMapper.updateByRecordIdSelective(e);
                }else{
                    insert1 = recordDetailEntityMapper.insert(e);
                }
                if (insert1 < 1) {
                    return false;
                }
            }
            reportRecordEntity1.setState(state);
            if ("1".equals(state)) {
                reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            //修改任务报告状态
            taskMapper.updateReportStatus(Integer.parseInt(state),vo.getTaskId());
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            long recordId = GenID.getID();
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                int insert1 = recordDetailEntityMapper.insert(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
            reportRecordEntity.setState(state);
            if ("1".equals(state)) {
                reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            //生成报告编号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String year = sdf.format(new Date());
            Integer maxCode = recordEntityMapper.getMaxCode(year);
            if(maxCode == null){
                reportRecordEntity.setReportCode("ZX-"+year+"-JC-0001");
            }else{
                int newCode = maxCode + 1;
                reportRecordEntity.setReportCode("ZX-"+year+"-JC-"+newCode);
            }
            reportRecordEntity.setId(recordId);
            reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            //修改任务报告状态
            taskMapper.updateReportStatus(Integer.parseInt(state),vo.getTaskId());
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    @Override
    public PageInfo sealList(String search, Integer pageNum, Integer pageSize,String reportType,String state) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isEmpty(state)){
            state = "1";
        }
        List<ReportRecordEntity> list = entityMapper.getSealList(search,reportType,state);
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
                    if (!CollectionUtils.isEmpty(samples)){
                        StringBuilder stringBuilder1 = new StringBuilder();
                        for (SampleEntity entity :samples) {
                            List<JudgmentBasisVo> sampleCheckItem = entity.getJudgmentBasisVos();
                            for (JudgmentBasisVo itemEntity:sampleCheckItem) {
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
                    rows.get(10).getTableCells().get(1).setText(detail.getEntrustmentNo()+"");
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
    public List<ReportTemplateEntity> getReportTemplateList(String productId) {
        return templateEntityMapper.getReportTemplateList(productId);
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
    public PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize, String type) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSendList(search, reportType, type);
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
        if(!CollectionUtils.isEmpty(judgeBasis)){
            for (int i = 0; i < judgeBasis.size(); i++) {
                result.append(judgeBasis.get(i));
                if(judgeBasis.size()-1!=i){
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
        if(!CollectionUtils.isEmpty(checkBasis)){
            for (int i = 0; i < checkBasis.size(); i++) {
                result.append(checkBasis.get(i));
                if(checkBasis.size()-1!=i){
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
        if(!CollectionUtils.isEmpty(equipment)){
            for (int i = 0; i < equipment.size(); i++) {
                result.append(equipment.get(i));
                if(equipment.size()-1!=i){
                    result.append(",");
                }
            }
        }
        return result.toString();
    }

    @Override
    public int updateReportUrl(Long id, String url,String code) {
        return reportMapper.updateReportUrl(id,url,code);
    }

    @Override
    public String downLoad(MinioClient client, String code, Long id) throws Exception {
        String[] split = code.split("\\?");
        String[] strings = split[0].split("\\/");
        String bluckName = strings[3];
        String fileName  = strings[4];
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
            textMap.put("code",reportRecordEntity.getReportCode());
            textMap.put("page",doc.getTables().size()+"");
            textMap.put("sampleName",reportRecordEntity.getSampleName());
            textMap.put("dept",entrustAddVo.getEntrustCompany());
            textMap.put("part",entrustAddVo.getProjectPart());
            textMap.put("checkType",entrustAddVo.getCheckPurpose());
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
                    rows.get(12).getCell(1).removeParagraph(0);
                    rows.get(12).getCell(1).setText(sampleEntity.getSpecs() == null ? "——" : sampleEntity.getSpecs());
                    //代表数量
                    rows.get(12).getCell(3).removeParagraph(0);
                    rows.get(12).getCell(3).setText(sampleEntity.getGeneration() == null ? "——" : sampleEntity.getGeneration());
                }
                //存放检测数据
                for (ReportRecordDetailEntity item : checkItemList) {
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
        updateReportUrl(reportRecordEntity.getId(), url,code);
//            ServletOutputStream outputStream = response.getOutputStream();
//            FileAndFolderUtil.parseIn(inputStream)
        return url;
    }

    @Override
    public Boolean seal(Long entrustId,String title,String fileType) {
        //step1 根据文件类型创建合同文档
        String url = "";
        MinioClient client = MinIoUtil.minioClient;
        String code = recordEntityMapper.getReportModelNameById(entrustId);
        try {
            url = downLoad(client,code,entrustId);
        }catch (Exception e){
            logger.error("盖章下载报告文件失败:{}",e);
        }
        if (StringUtils.isNotEmpty(url)){
            File file = null;
            try {
                file = FileAndFolderUtil.getFile(url);
            }catch (Exception e){
                logger.error("将报告地址转为File文件失败:{}",e);
            }
            if (file != null){
                QiYueSuoResponse response = qiYueSuoHnadler.creatFile(file, title, fileType, null, null, null);
                if (response != null && response.getCode() == 0){
                    //根据委托id存储文档id
                    List<QiYueSuoDocment> result = response.getResult();
                    entityMapper.updateDocIdAndState(entrustId,result.get(0).getDocumentId(),"2");
                }
            }
        }
        return true;
    }

    @Override
    public QiYueSuoResponse createbycategory(QiYueSuoReqBean reqBean) {
        //设置文档标识
        List<ReportRecordEntity> entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        List<String> docs = new ArrayList<>();
        docs.add(entity.get(0).getQysDocmentId());
        reqBean.setDocuments(docs);
        QiYueSuoResponse response = qiYueSuoHnadler.createbycategory(reqBean);
        //根据委托id存储文档id
        entityMapper.updateContractIdAndState(reqBean.getEntrustId(),response.getContractId(),"3");
        return response;
    }

    @Override
    public QiYueSuoResponse signurl(QiYueSuoSeaLBean reqBean) {
        //设置合同标识
        List<ReportRecordEntity> entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        reqBean.setContractId(Long.valueOf(entity.get(0).getContractId()));
        QiYueSuoResponse response = qiYueSuoHnadler.signurl(reqBean);
        //根据委托更新报告签署url
        entityMapper.updateUrlAndState(reqBean.getEntrustId(),response.getSignUrl(),"4");
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
        taskMapper.updateEntrustById(entrustId,10);
        //更新报告状态
        entityMapper.updateFileState(contractId,"5");
        logger.debug("接收契约锁回调参数进行数据更新完成！");
    }

    @Override
    public QiYueSuoResponse deptList(String tenantType, String companyName) {
        return qiYueSuoHnadler.getDeptListOfQYS(tenantType,companyName);
    }

    @Override
    public QiYueSuoResponse sealListOfQys(String category, String companyName, String sealType) {
        QiYueSuoResponse qiYueSuoResponse = qiYueSuoHnadler.sealList(category, companyName);
        //根据sealType过来qiYueSuoResponse
        List<QiYueSuoSealEntity> newList = Lists.newArrayList();
        String[] split = sealType.split(",");
        List<QiYueSuoSealEntity> list = qiYueSuoResponse.getList();
        for (QiYueSuoSealEntity qiYueSuoSealEntity:list) {
            for (String s :split) {
                if (qiYueSuoSealEntity.getName().contains(s)){
                    newList.add(qiYueSuoSealEntity);
                }
            }
            //检验检测专用章（室内试验）、检验检测专用章（外业检测）作为通用章返回
            if (qiYueSuoSealEntity.getName().equals("检验检测专用章（室内试验）" )
                    || qiYueSuoSealEntity.getName().equals("检验检测专用章（外业检测）")){
                newList.add(qiYueSuoSealEntity);
            }
        }
        qiYueSuoResponse.setList(newList);
        return qiYueSuoResponse;
    }

    @Override
    public List<QuotaRes> getQuota(Long taskId) {
        Map<String,List<QuotaEntity>> map = new HashMap<>();
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        ReportDetailVo reportDetail = reportMapper.getReportDetail(taskId, userTeamIds);
        List<Long> ids = Lists.newArrayList();
        for (ReportSampleDetailVo bean:reportDetail.getSamples()) {
            //获取检测id
            List<ReportCheckItemDetailVo> checkItems = bean.getCheckItems();
            for (ReportCheckItemDetailVo detailVo:checkItems) {
                ids.add(detailVo.getCheckItemId());
            }
        }
        List<QuotaEntity> list = dao.getListById(ids);
        for (QuotaEntity bean:list) {
            if (map.get(bean.getConditionValue()) == null){
                List<QuotaEntity> quotaEntityList = Lists.newArrayList();
                QuotaEntity quotaEntity = new QuotaEntity();
                quotaEntity.setCheckItemId(bean.getCheckItemId());
                quotaEntity.setSpecsContent(bean.getSpecsContent());
                quotaEntityList.add(quotaEntity);
                map.put(bean.getConditionValue(),quotaEntityList);
            }else {
                List<QuotaEntity> entities = map.get(bean.getConditionValue());
                QuotaEntity quota = new QuotaEntity();
                quota.setCheckItemId(bean.getCheckItemId());
                quota.setSpecsContent(bean.getSpecsContent());
                entities.add(quota);
                map.put(bean.getConditionValue(),entities);
            }
        }
        //处理map
        List<QuotaRes> lis = Lists.newArrayList();
        Set<String> set = map.keySet();
        for (String key:set) {
            QuotaRes res = new QuotaRes();
            res.setKey(key);
            res.setValue(map.get(key));
            lis.add(res);
        }
        return lis;
    }

}
