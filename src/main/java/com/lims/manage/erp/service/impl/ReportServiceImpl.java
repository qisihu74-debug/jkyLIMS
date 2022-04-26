package com.lims.manage.erp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.ConclusionEntity;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.QiYueSuoSeaLBean;
import com.lims.manage.erp.entity.QiYueSuoSealEntity;
import com.lims.manage.erp.entity.QuotaEntity;
import com.lims.manage.erp.entity.QuotaRes;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.http.QiYueSuoDocment;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.ReportTemplateEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.mapper.TestReportQualifcationDao;
import com.lims.manage.erp.mapper.TestReportTemplateDao;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleMixInfoEntityMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.WordUtils;
import com.lims.manage.erp.vo.ConcreteSampleVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.JudgmentBasisVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.ReportCheckItemDetailVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportHistoryDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
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
import java.text.DecimalFormat;
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
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.getReportList2(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()), search);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }


    @Override
    public PageInfo reportDownloadList(Integer pageNum, Integer pageSize, String search) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportListVo> list = reportMapper.reportDownloadList(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()), search);
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
        PageHelper.startPage(pageNum, pageSize);
        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        reportListVo.setDeptIds(teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
        List<ReportListVo> list = reportMapper.getReportList_history(reportListVo);
        PageInfo<ReportListVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    public PageInfo reportDownloadListHistory(String search, Integer pageNum, Integer pageSize) {
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
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemList(id, teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId()));
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
            if (maxCode == null) {
                reportRecordEntity.setReportCode("ZX-" + year + "-JC-0001");
            } else {
                int newCode = maxCode + 1;
                reportRecordEntity.setReportCode("ZX-" + year + "-JC-" + newCode);
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
//            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
//                if (e.getJudgeResult() == null) {
//                    state = "2";
//                }
                List<Long> checkItemIds = recordDetailEntityMapper.getCheckItemIds(reportRecordEntity1.getId());
                int insert1;
                if (checkItemIds.contains(e.getCheckItemId())) {
                    insert1 = recordDetailEntityMapper.updateByRecordIdSelective(e);
                } else {
                    insert1 = recordDetailEntityMapper.insert(e);
                }
                if (insert1 < 1) {
                    return false;
                }
            }
//            reportRecordEntity1.setState(state);
//            if ("1".equals(state)) {
//                reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
//            }
            //校验其他任务单是否完成
            List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
            if(allReportComplete.contains(2)){
                reportRecordEntity1.setState(2+"");
            }else{
                reportRecordEntity1.setState(1+"");
                reportRecordEntity1.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            //修改任务报告状态
            taskMapper.updateReportStatus(vo.getReportComplete(), vo.getTaskId());
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            long recordId = GenID.getID();
//            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
//                if (e.getJudgeResult() == null) {
//                    state = "2";
//                }
                int insert1 = recordDetailEntityMapper.insert(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
//            reportRecordEntity.setState(state);
//            if ("1".equals(state)) {
//                reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
//            }

            List<Integer> allReportComplete = taskMapper.getAllReportComplete(vo.getEntrustmentId(),vo.getTaskId());
            if(allReportComplete.contains(2)){
                reportRecordEntity.setState(2+"");
            }else{
                reportRecordEntity.setState(1+"");
                reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            }
            //生成报告编号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String year = sdf.format(new Date());
            Integer maxCode = recordEntityMapper.getMaxCode(year);
            if (maxCode == null) {
                reportRecordEntity.setReportCode("ZX-" + year + "-JC-0001");
            } else {
                int newCode = maxCode + 1;
                reportRecordEntity.setReportCode("ZX-" + year + "-JC-" + new DecimalFormat("0000").format(newCode));
            }
            reportRecordEntity.setId(recordId);
            reportRecordEntity.setReportCompleteTime(new Date(System.currentTimeMillis()));
            //修改任务报告状态
            taskMapper.updateReportStatus(vo.getReportComplete(), vo.getTaskId());
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    @Override
    public PageInfo sealList(String search, Integer pageNum, Integer pageSize, String reportType, String state) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isEmpty(state)) {
            state = "1";
        }
        List<ReportRecordEntity> list = entityMapper.getSealList(search, reportType, state);
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
        if (!CollectionUtils.isEmpty(judgeBasis)) {
            for (int i = 0; i < judgeBasis.size(); i++) {
                result.append(judgeBasis.get(i));
                if (judgeBasis.size() - 1 != i) {
                    result.append(",");
                }
            }
        }
        if (result.length()>=1){
            return result.toString().substring(0,result.length()-1);
        }else {
            return result.toString();
        }

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
        if (result.length()>=1){
            return result.toString().substring(0,result.length()-1);
        }else {
            return result.toString();
        }
    }

    @Override
    public String getEquipment(Long id) {
        StringBuilder result = new StringBuilder("");
        List<String> equipment = reportMapper.getEquipment(id);
        if (!CollectionUtils.isEmpty(equipment)) {
            for (int i = 0; i < equipment.size(); i++) {
                result.append(equipment.get(i));
                if (equipment.size() - 1 != i) {
                    result.append(",");
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
                                String conclusion,String additional,String mixInfo,String type) {
        Boolean flag = false;
        String url = "";
        if (file == null){
            //下载模板填充数据
            MinioClient client = MinIoUtil.minioClient;
            try {
                List<String> keys = JSONArray.parseArray(code, String.class);
                List<String> conclusions = JSONArray.parseArray(conclusion, String.class);
                List<String> additionals = JSONArray.parseArray(additional, String.class);
                List<ConclusionEntity> list = Lists.newArrayList();
                for (int i=0;i<keys.size();i++) {
                    ConclusionEntity conclusionEntity = new ConclusionEntity();
                    conclusionEntity.setUrl(keys.get(i));
                    conclusionEntity.setConclusion(conclusions.get(i));
                    conclusionEntity.setAdditional(additionals.get(i));
                    list.add(conclusionEntity);
                }
                if ("配合比".equals(type)){
                    TestSampleMixInfoEntity mixInfoEntity = JSON.parseObject(mixInfo,TestSampleMixInfoEntity.class);
                    url = this.submitDownLoadMix(client, list, Long.parseLong(reportCode),mixInfoEntity);
                }else {
                    url = this.submitDownLoad(client, list, Long.parseLong(reportCode));
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
        reportMapper.updateUrl(reportCode, url, verifyer, issuer, verifyerId, issuerId,new Date(),ShiroUtils.getUserInfo().getName());
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
                    entityMapper.updateDocIdAndState(entrustId, result.get(0).getDocumentId(), "2");
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
        List<ReportRecordEntity> entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        List<String> docs = new ArrayList<>();
        docs.add(entity.get(0).getQysDocmentId());
        reqBean.setDocuments(docs);
        QiYueSuoResponse response = qiYueSuoHnadler.createbycategory(reqBean);
        //根据委托id存储文档id
        entityMapper.updateContractIdAndState(reqBean.getEntrustId(), response.getContractId(), "3");
        return response;
    }

    @Override
    public QiYueSuoResponse signurl(QiYueSuoSeaLBean reqBean) {
        //设置合同标识
        List<ReportRecordEntity> entity = entityMapper.selectMessageByEntrustId(reqBean.getEntrustId());
        reqBean.setContractId(Long.valueOf(entity.get(0).getContractId()));
        QiYueSuoResponse response = qiYueSuoHnadler.signurl(reqBean);
        //根据委托更新报告签署url
        entityMapper.updateUrlAndState(reqBean.getEntrustId(), response.getSignUrl(), "4");
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
    public String submitDownLoad(MinioClient client, List<ConclusionEntity> list, Long id) {
        //2代表报告头2页
        int totalPage = 2;
        Map<Integer,XWPFDocument> map = new HashedMap();
        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
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
                    totalPage++;
                    index++;
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 1) {
                        rows.get(3).getCell(3).removeParagraph(0);
                        rows.get(3).getCell(3).setText(reportRecordEntity.getReportCode());
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
                                + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                                + "；样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                                + "；样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                                + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
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
                    //处理附加声明和检测结论
                    if (i==size){
                        XWPFTable xwpfTable = doc.getTables().get(size - 1);
                        int size1 = xwpfTable.getRows().size();
                        rows.get(size1-3).getCell(0).removeParagraph(0);
                        rows.get(size1-3).getCell(0).setText(conclusionEntity.getConclusion());
                        rows.get(size1-2).getCell(0).removeParagraph(0);
                        rows.get(size1-2).getCell(0).setText(conclusionEntity.getAdditional());
                        //TODO 获取到签名信息插入指定位置
                        ReportRecordEntity detailByEntrustId = reportMapper.getDetailByEntrustId(id);//审核人、签发人
                        List<String> stringList = taskMapper.getInspectorByEntrustId(id);
                        List<Long> list1 = Lists.newArrayList();//检测人
                        for (String s:stringList) {
                            String[] split1 = s.split("&");
                            list1.add(Long.parseLong(split1[1]));
                        }
                        //获取每个人的个人签名
                        String verUrl = sysUserDao.getSignatureById(detailByEntrustId.getVerifyerId());
                        String issUrl = sysUserDao.getSignatureById(detailByEntrustId.getIssuerId());
                        List<String> checkUrl = Lists.newArrayList();
                        for (Long uId:list1) {
                            String signature = sysUserDao.getSignatureById(uId);
                            checkUrl.add(signature);
                        }
                        //插入word指定位置
                        insertPicToDoc(doc,issUrl,size-1,size1-1,5);
                        insertPicToDoc(doc,verUrl,size-1,size1-1,3);
                        //插入实验人员
                        for (String url:checkUrl) {
                            insertPicToDoc(doc,url,size-1,size1-1,1);
                        }
                    }
                    i++;
                }
                //按照顺序存放doc
                map.put(index,doc);
            }
        }
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.docx");
        XWPFDocument topDoc = new XWPFDocument(fileStream);;
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        setReportTop(topDoc,entrustAddVo,reportRecordEntity,totalPage);
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
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        updateReportUrl(reportRecordEntity.getId(), url, stringBuilder.toString().substring(0,stringBuilder.length()-2));
        return url;
    }

    @SneakyThrows
    @Override
    public String submitDownLoadMix(MinioClient client, List<ConclusionEntity> list, Long id,TestSampleMixInfoEntity mixInfoEntity) {
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
        int totalPage = 2;
        Map<Integer,XWPFDocument> map = new HashedMap();
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
                    totalPage++;
                    index++;
                    XWPFTable table = it.next();
                    List<XWPFTableRow> rows = table.getRows();
                    //存放表头信息
                    EntrustAddVo entrustHistoryDetail = entrustService.getEntrustHistoryDetail(id);
                    if (i == 1) {
                        rows.get(3).getCell(3).removeParagraph(0);
                        rows.get(3).getCell(3).setText(reportRecordEntity.getReportCode());
                        rows.get(4).getCell(1).removeParagraph(0);
                        rows.get(4).getCell(1).setText(entrustHistoryDetail.getEntrustCompany());
                        rows.get(4).getCell(3).removeParagraph(0);
                        rows.get(4).getCell(3).setText(entrustHistoryDetail.getProjectName());

                        //样品信息
                        SampleEntity sampleEntity = entrustHistoryDetail.getSamples().get(0);
                        rows.get(6).getCell(1).removeParagraph(0);
                        rows.get(6).getCell(1).setText("样品名称：" + (sampleEntity.getSampleName() == null ? "——" : sampleEntity.getSampleName())
                                + "；样品编号：" + (sampleEntity.getSampleCode() == null ? "——" : sampleEntity.getSampleCode())
                                + "；样品数量：" + (sampleEntity.getQuantityPerGroup() == null ? "——" : sampleEntity.getQuantityPerGroup())
                                + "；样品状态：" + (sampleEntity.getOutward() == null ? "——" : sampleEntity.getOutward())
                                + "；收样时间：" + (sampleEntity.getReceivedDate() == null ? "——" : sampleEntity.getReceivedDate()));
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
                        //设计参数
                        if (mixInfoEntity != null){
                            rows.get(11).getCell(1).removeParagraph(0);
                            rows.get(11).getCell(1).setText(mixInfoEntity.getDesignStrength());
                            rows.get(11).getCell(3).removeParagraph(0);
                            rows.get(11).getCell(3).setText(mixInfoEntity.getIntensityConfiguration());
                            rows.get(12).getCell(1).removeParagraph(0);
                            rows.get(12).getCell(1).setText(mixInfoEntity.getAntifreezeLevel());
                            rows.get(12).getCell(3).removeParagraph(0);
                            rows.get(12).getCell(3).setText(mixInfoEntity.getWaterBinderRatio());
                            rows.get(13).getCell(1).removeParagraph(0);
                            rows.get(13).getCell(1).setText(mixInfoEntity.getUnitWaterUse());
                            rows.get(13).getCell(3).removeParagraph(0);
                            rows.get(13).getCell(3).setText(mixInfoEntity.getSandRatio());
                            rows.get(14).getCell(1).removeParagraph(0);
                            rows.get(14).getCell(1).setText(mixInfoEntity.getDesignSlump());
                            rows.get(14).getCell(3).removeParagraph(0);
                            rows.get(14).getCell(3).setText(mixInfoEntity.getMixingWay());
                        }else {
                            TestSampleMixInfoEntity entity = mixInfoEntityMapper.selectByEntrustId(id);
                            if (entity != null){
                                rows.get(11).getCell(1).removeParagraph(0);
                                rows.get(11).getCell(1).setText(entity.getDesignStrength());
                                rows.get(11).getCell(3).removeParagraph(0);
                                rows.get(11).getCell(3).setText(entity.getIntensityConfiguration());
                                rows.get(12).getCell(1).removeParagraph(0);
                                rows.get(12).getCell(1).setText(entity.getAntifreezeLevel());
                                rows.get(12).getCell(3).removeParagraph(0);
                                rows.get(12).getCell(3).setText(entity.getWaterBinderRatio());
                                rows.get(13).getCell(1).removeParagraph(0);
                                rows.get(13).getCell(1).setText(entity.getUnitWaterUse());
                                rows.get(13).getCell(3).removeParagraph(0);
                                rows.get(13).getCell(3).setText(entity.getSandRatio());
                                rows.get(14).getCell(1).removeParagraph(0);
                                rows.get(14).getCell(1).setText(entity.getDesignSlump());
                                rows.get(14).getCell(3).removeParagraph(0);
                                rows.get(14).getCell(3).setText(entity.getMixingWay());
                            }
                        }
                        //填充配合比下原材样品信息
                        List<TestSampleEntity> testSampleEntities = testSampleEntityMapper.selectByPid(entrustHistoryDetail.getSamples().get(0).getId());
                        if (testSampleEntities.size()>7){
                            AsposeUtil.addRows(table,16,testSampleEntities.size()-7);
                        }
                        for (int j=0;j<testSampleEntities.size();j++) {
                            rows.get(j+16).getCell(0).removeParagraph(0);
                            rows.get(j+16).getCell(0).setText(testSampleEntities.get(j).getSampleName());
                            rows.get(j+16).getCell(1).removeParagraph(0);
                            rows.get(j+16).getCell(1).setText(testSampleEntities.get(j).getSpecs());
                            rows.get(j+16).getCell(2).removeParagraph(0);
                            rows.get(j+16).getCell(2).setText(testSampleEntities.get(j).getManufacturer());
                            rows.get(j+16).getCell(3).removeParagraph(0);
                            rows.get(j+16).getCell(3).setText(testSampleEntities.get(j).getBatchNumber());
                            rows.get(j+16).getCell(4).removeParagraph(0);
                            rows.get(j+16).getCell(4).setText(testSampleEntities.get(j).getGeneration());
                            rows.get(j+16).getCell(5).removeParagraph(0);
                            rows.get(j+16).getCell(5).setText(testSampleEntities.get(j).getOutward());
                            rows.get(j+16).getCell(6).removeParagraph(0);
                            rows.get(j+16).getCell(6).setText(testSampleEntities.get(j).getSampleCode());
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
                            int last = testProductDao.isLast(item.getCheckItemId().intValue());
                            if (last == 0) {
                                int page = Integer.parseInt(item.getCoordinate().split(",")[0]);
                                int row = Integer.parseInt(item.getCoordinate().split(",")[1]);
                                int column = Integer.parseInt(item.getCoordinate().split(",")[2]);
                                if (i == page) {
                                    rows.get(row).getCell(column + 1).removeParagraph(0);
                                    rows.get(row).getCell(column + 1).setText(item.getCheckResult());
                                }
                            }
                        }
                        //处理附加声明和检测结论
                        XWPFTable xwpfTable = doc.getTables().get(size - 1);
                        int size1 = xwpfTable.getRows().size();
                        rows.get(size1-3).getCell(0).removeParagraph(0);
                        rows.get(size1-3).getCell(0).setText(conclusionEntity.getConclusion());
                        rows.get(size1-2).getCell(0).removeParagraph(0);
                        rows.get(size1-2).getCell(0).setText(conclusionEntity.getAdditional());
                        //TODO 获取到签名信息插入指定位置
                        ReportRecordEntity detailByEntrustId = reportMapper.getDetailByEntrustId(id);//审核人、签发人
                        List<String> stringList = taskMapper.getInspectorByEntrustId(id);
                        List<Long> list1 = Lists.newArrayList();//检测人
                        for (String s:stringList) {
                            String[] split1 = s.split("&");
                            list1.add(Long.parseLong(split1[1]));
                        }
                        //获取每个人的个人签名
                        String verUrl = sysUserDao.getSignatureById(detailByEntrustId.getVerifyerId());
                        String issUrl = sysUserDao.getSignatureById(detailByEntrustId.getIssuerId());
                        List<String> checkUrl = Lists.newArrayList();
                        for (Long uId:list1) {
                            String signature = sysUserDao.getSignatureById(uId);
                            checkUrl.add(signature);
                        }
                        //插入word指定位置
                        insertPicToDoc(doc,issUrl,size-1,size1-1,5);
                        insertPicToDoc(doc,verUrl,size-1,size1-1,3);
                        //插入实验人员
                        for (String url:checkUrl) {
                            insertPicToDoc(doc,url,size-1,size1-1,1);
                        }
                    }
                    i++;
                }
                //按照顺序存放doc
                map.put(index,doc);
            }
        }
        //获取报告头部模板填充头部数据
        InputStream fileStream = MinIoUtil.getFileStream("top-temlate", "top.docx");
        XWPFDocument topDoc = new XWPFDocument(fileStream);
        EntrustAddVo entrustAddVo = entrustEntityMapper.selectByKeyId(id);
        setReportTop(topDoc,entrustAddVo,reportRecordEntity,totalPage);
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
            stringBuilder.append(entity.getUrl());
            stringBuilder.append("&&");
        }
        updateReportUrl(reportRecordEntity.getId(), url, stringBuilder.toString().substring(0,stringBuilder.length()-2));
        return url;
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
        rows.get(14).getCell(1).setText(entrustAddVo.getProjectPart());
        rows.get(15).getCell(2).setText(entrustAddVo.getCheckPurpose());
    }

    @Override
    public String reportUrl(Long entrustId) {
        return reportMapper.getUrlByEntrustId(entrustId);
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
    public ReportRecordEntity getUserInfo(Long entrustId) {
        return reportMapper.getDetailByEntrustId(entrustId);
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
                        entry.setValue("--");
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
     * @param doc
     * @param picUrl
     * @param position
     */
    public void insertPicToDoc(XWPFDocument doc, String picUrl,int table,int size,int position) throws Exception {
       /* InputStream inputStream = AsposeUtil.docToIo(doc);
        Document document = new Document(inputStream);
        File file = FileAndFolderUtil.getFile(picUrl);
        FileInputStream fileInputStream = new FileInputStream(file);
        document.getTables().get(table).getRows().get(size).getCells().get(position).addParagraph().appendPicture(fileInputStream);*/

        XWPFTableCell tableCell = doc.getTables().get(table).getRows().get(size).getTableCells().get(position);
        File file = FileAndFolderUtil.getFile("http://121.89.242.0:9000/personal-signature/1647502446459100.png");
        FileInputStream fileInputStream = new FileInputStream(file);
        tableCell.addParagraph().createRun().addPicture(fileInputStream,XWPFDocument.PICTURE_TYPE_PNG,"bus.png,", Units.toEMU(20), Units.toEMU(20));

        /*XWPFParagraph xwpfParagraph = doc.getTables().get(table).getRows().get(size).getTableCells().get(position).getParagraphs().get(0);
        XmlCursor cursor = xwpfParagraph.getCTP().newCursor();
        XWPFParagraph newPara = doc.insertNewParagraph(cursor);
        newPara.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.valueOf(ParagraphAlignment.CENTER));//居中
        XWPFRun newParaRun = newPara.createRun();
        //将图片转为流
        File file = FileAndFolderUtil.getFile(picUrl);
        FileInputStream fileInputStream = new FileInputStream(file);
        newParaRun.addPicture(fileInputStream,XWPFDocument.PICTURE_TYPE_PNG,"bus.png,", Units.toEMU(20), Units.toEMU(20));
        doc.removeBodyElement(doc.getPosOfParagraph(xwpfParagraph));*/
    }

    /**
     * 扩展模板样品行列
     * @param table 原始表格
     * @param rows 原始表格行数
     * @param sampleDetailList 待处理数据
     * @param modelSampleRows 需要新增行
     * @param columns 列数
     */
    public void extendTable(XWPFTable table,List<XWPFTableRow> rows,List<TestSampleEntity> sampleDetailList,
                             int modelSampleRows,int columns){
        rows = table.getRows();
        if (sampleDetailList.size() > modelSampleRows) {
            int addRows = sampleDetailList.size() - modelSampleRows;
            // 表格插入
            XWPFDocument doc1 = new XWPFDocument();
            XWPFTable newTable = doc1.createTable(addRows, columns);
            // 创建表格后直接进行存放 后续多余数据
            List<XWPFTableRow> dataTable = newTable.getRows();
            int j = 0;
            for (int i = modelSampleRows; i < sampleDetailList.size(); i++) {
                dataTable.get(j).getCell(0).setText(sampleDetailList.get(i).getSampleName());
                dataTable.get(j).getCell(1).setText(sampleDetailList.get(i).getSpecs());
                dataTable.get(j).getCell(2).setText(sampleDetailList.get(i).getManufacturer());
                dataTable.get(j).getCell(3).setText(sampleDetailList.get(i).getBatchNumber());
                dataTable.get(j).getCell(4).setText(sampleDetailList.get(i).getGeneration());
                dataTable.get(j).getCell(5).setText(sampleDetailList.get(i).getOutward());
                dataTable.get(j).getCell(6).setText(sampleDetailList.get(i).getSampleCode());
                table.addRow(dataTable.get(j));
                j++;
            }
            rows = table.getRows();
        }
    }
}
