package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ImagePro;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.job.QiYueSuoHnadler;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ReportApprovalMapper;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.ReportTemplateEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ImageToPdfUtils;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.JudgmentBasisVo;
import com.lims.manage.erp.vo.ReportCheckItemDetailVo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import com.lims.manage.erp.vo.ReportSampleDetailVo;
import io.minio.MinioClient;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
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

    /**
     * 提交审批
     * @param id
     * @param name 报告提交申请人
     * @return
     */
    @Transactional
    @Override
    public Boolean getReportSubmit(Long id,String name) {
        // 根据委托单id 查询报告信息 state=1
        ReportRecordEntity reportData = recordEntityMapper.getReportEntrust(id);
        if(reportData.getState().equals("1")){
            // 修改状态
            reportData.setReportCompleteTime(new Date());
            reportData.setApplicant(name);
             recordEntityMapper.updateByEntrustIdSelective(reportData);
            // 根据任务单主键 获取委托单主键 更改委托单状态
            EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(id);
            if(entrustBaseInfo.getState()!=null&&entrustBaseInfo.getState()<7){
                taskMapper.updateEntrustById(entrustBaseInfo.getId(),7);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<ReportListVo> getReportList_history(String search) {

        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        return reportMapper.getReportList_history(reportListVo);
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
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemList(id);
        reportSampleDetailVo.setCheckItems(checkItemList);
        return reportSampleDetailVo;
    }

    @Override
    public ReportDetailVo getReportDetail(Long id) {
        return reportMapper.getReportDetail(id);
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
    public Boolean preserve(ReportPreserveVo vo) {
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByEntrustId(vo.getEntrustmentId());
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
//            if ("1".equals(state)) {
//                reportRecordEntity1.setReportCompleteTime(new Timestamp(System.currentTimeMillis()));
//            }
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
//            if ("1".equals(state)) {
//                reportRecordEntity1.setReportCompleteTime(new Timestamp(System.currentTimeMillis()));
//            }
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
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    @Override
    public PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize,String reportType) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSealList(type, search,reportType);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Boolean seal(List<String> list, Long id) {
        //TODO 根据印章类型，请求契约所的印章
        String url = "";
        MinioClient client = MinIoUtil.minioClient;
        Long entrustId = taskMapper.getEntrustIdByTaskId(id);
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
                qiYueSuoHnadler.creatFile(file,code,"pdf",null,null,null);
            }
        }



        //更新url数据到表test_report_record
        entityMapper.updateImgByid(id, url);

        // 根据任务单主键 获取委托单主键 更改委托单状态
        EntrustAddVo entrustAddVo = reportApprovalMapper.getEntrustAddVoDetail(id);
        if(entrustAddVo.getState()!=null&&entrustAddVo.getState()<10){
            taskMapper.updateEntrustById(entrustAddVo.getId(),10);
        }

        return true;
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
    public String downLoad(MinioClient client, String code, Long id) throws Exception { XWPFDocument doc = null;
        client.statObject("report-word", code + ".docx");
        InputStream object = client.getObject("report-word", code + ".docx");
        doc = new XWPFDocument(object);
        //写入数据
        ReportRecordEntity reportRecordEntity = selectByEntrustId(id);
        List<ReportRecordDetailEntity> checkItemList = getCheckInfoByRecordId(reportRecordEntity.getId());
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(checkItemList)) {
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
        String sealUrl = reportRecordEntity.getSealUrl();
        List<ImagePro> imagePros = com.google.api.client.util.Lists.newArrayList();
        if (!StringUtils.isEmpty(sealUrl) && sealUrl != null) {
            String[] split = sealUrl.split(",");
            for (int i = 0; i < split.length; i++) {
                ImagePro pro = new ImagePro(100 * (i + 1), 100, 15F, split[i]);
                imagePros.add(pro);
            }
        }
        InputStream inputStream1 = new ByteArrayInputStream(b1.toByteArray());
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ByteArrayOutputStream b3;
        InputStream inputStream;
        if(imagePros.size()>0){
            b3 = ImageToPdfUtils.writeToPdf4(inputStream1, b2, imagePros);
            inputStream = FileAndFolderUtil.parseOut(b3);
        }else{
            inputStream = inputStream1;
        }
//        InputStream inputStream = FileAndFolderUtil.parseOut(b3);
        String url = "";
        url = MinIoUtil.upload("report-download", reportRecordEntity.getReportCode() + ".pdf", inputStream, "application/octet-stream");
        updateReportUrl(reportRecordEntity.getId(), url,code);
//            ServletOutputStream outputStream = response.getOutputStream();
//            FileAndFolderUtil.parseIn(inputStream)
        return url;
    }

}
