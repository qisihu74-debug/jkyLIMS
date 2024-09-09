package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.StatisticsMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TeamMapper;
import com.lims.manage.erp.service.StatisticsService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TeamMapper teamMapper;



    @Override
    public TaskStatsVo TaskDetails(Long taskId) {
        TaskStatsVo taskStatsVo = new TaskStatsVo();
        List<TaskStatsItemVo> itemList = new ArrayList<>();
        // 查询任务单详情 判断 进度
        TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
        taskStatsVo.setTaskProgress(taskTestEntity.getState() > 1 ? "已领取" : "未领取");
        TaskDetailInfoVo list = taskMapper.getTaskDetailInfo(taskId);
        taskStatsVo.setTaskCode(list.getTaskCode());
        taskStatsVo.setTaskId(list.getTaskId());
        if (!list.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : list.getSampleDetailList()) {
                if (!sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        TaskStatsItemVo taskStatsItemVo = new TaskStatsItemVo();
                        taskStatsItemVo.setCheckItemName(checkItemInfoVo.getCheckItemName());
                        taskStatsItemVo.setItemId(checkItemInfoVo.getItemId());
                        // 判断 试验开始
                        taskStatsItemVo.setTestBegun(checkItemInfoVo.getState() > 0 ? true : false);
                        // 原始记录上传
                        taskStatsItemVo.setUploadRecords(checkItemInfoVo.getOriginUrl() != null ? true : false);
                        // 已复核
                        taskStatsItemVo.setReview(checkItemInfoVo.getState() >= 3 ? true : false);
                        itemList.add(taskStatsItemVo);
                    }
                }
            }
        }
        taskStatsVo.setList(itemList);
        return taskStatsVo;
    }

    @Override
    public InputStream exportPersonDetails( PagingToolVo list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
//建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
//在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("任务单编号");
        row1.createCell(1).setCellValue("要求完成日期");
        row1.createCell(2).setCellValue("实际完成日期");
        row1.createCell(3).setCellValue("费用");
        row1.createCell(4).setCellValue("报告编号");
        row1.createCell(5).setCellValue("报告类型");
        row1.createCell(6).setCellValue("样品名称");
        row1.createCell(7).setCellValue("状态");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < list.getList().size(); i++) {
            TaskStatsVo personVo = (TaskStatsVo) list.getList().get(i);
            //在sheet里创建第二行
            HSSFRow row3 = sheet.createRow(i + 1);
            row3.createCell(0).setCellValue(personVo.getTaskCode());
            if (personVo.getRequestDate() != null) {
                String dateString = formatter.format(personVo.getRequestDate());
                row3.createCell(1).setCellValue(dateString);
            }
            if (personVo.getFinishDate() != null) {
                String dateString = formatter.format(personVo.getFinishDate());
                row3.createCell(2).setCellValue(dateString);
            }
            row3.createCell(3).setCellValue(personVo.getCost());
            row3.createCell(4).setCellValue(personVo.getReportCode());
            row3.createCell(5).setCellValue(personVo.getReportType());
            row3.createCell(6).setCellValue(personVo.getSampleName());
            row3.createCell(7).setCellValue(personVo.getTaskStatus());
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public PageInfo personalStats(PersonalStatsVo personalStats) {
        // 部门部门 集合。
        List<Long> deptIds = new ArrayList<>();
        if (!org.springframework.util.StringUtils.isEmpty(personalStats.getTeamId())) {
            List<Long> nodeTeam = teamMapper.getNodeTeamId(Long.parseLong(personalStats.getTeamId()));
            deptIds.addAll(nodeTeam);
        }
        // in 查询 遍历出所有人员信息。
        if (personalStats.getPageNum() != null && personalStats.getPageSize() != null) {
            PageHelper.startPage(personalStats.getPageNum(), personalStats.getPageSize());
        }
        List<PersonalStatsVo> perons = new ArrayList<>();
        if (!CollectionUtils.isEmpty(deptIds)) {
            perons = statisticsMapper.selectAllPerson(deptIds);
        } else {
            perons = statisticsMapper.selectAllPerson(null);
        }
        PageInfo<PersonalStatsVo> result = new PageInfo<>(perons);
        // 查询委托的全部信息。
        if (CollectionUtil.isNotEmpty(result.getList())) {
            List<String> names = new ArrayList<>();
            List<String> userIds = new ArrayList<>();
            List<Long> userIdSet = new ArrayList<>();
            for (PersonalStatsVo personalStatsVo : result.getList()) {
                names.add(personalStatsVo.getName());
                userIds.add(personalStatsVo.getName() + "&" + personalStatsVo.getUserId());
                userIdSet.add(personalStatsVo.getUserId());
            }
            personalStats.setNames(names);
            personalStats.setUserIds(userIds);
            personalStats.setUserIdSet(userIdSet);
        }
        if (CollectionUtil.isNotEmpty(personalStats.getNames())) {
            // 统计委托单人数
            Map<String, Map<String, Object>> entrustMap = statisticsMapper.selectEntrustBusinessAcceptorMap(personalStats);
            if (CollectionUtil.isNotEmpty(entrustMap.keySet())) {
                for (String key : entrustMap.keySet()) {
                    Map<String, Object> mapData = entrustMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if (personalStatsVo.getName().equals(key)) {
                            personalStatsVo.setEntrustNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计发布人
            Map<String, Map<String, Object>> releaseTaskMap = statisticsMapper.selectReleaseTaskMap(personalStats);
            if (CollectionUtil.isNotEmpty(releaseTaskMap.keySet())) {
                for (String key : releaseTaskMap.keySet()) {
                    Map<String, Object> mapData = releaseTaskMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if (personalStatsVo.getName().equals(key)) {
                            personalStatsVo.setTaskNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计检测人员信息条数
            Map<String, Map<String, Object>> inspectorMap = statisticsMapper.selectInspectorMap(personalStats);
            if (CollectionUtil.isNotEmpty(inspectorMap.keySet())) {
                for (String key : inspectorMap.keySet()) {
                    Map<String, Object> mapData = inspectorMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if ((personalStatsVo.getName() + "&" + personalStatsVo.getUserId()).equals(key)) {
                            personalStatsVo.setTestNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计复核人员信息条数
            Map<String, Map<String, Object>> reviewerMap = statisticsMapper.selectReviewerMap(personalStats);
            if (CollectionUtil.isNotEmpty(reviewerMap.keySet())) {
                for (String key : reviewerMap.keySet()) {
                    Map<String, Object> mapData = reviewerMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if ((personalStatsVo.getName() + "&" + personalStatsVo.getUserId()).equals(key)) {
                            personalStatsVo.setReviewNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 报告制作人
            Map<String, Map<String, Object>> reportProducerMap = statisticsMapper.selectReportProducerMap(personalStats);
            if (CollectionUtil.isNotEmpty(reportProducerMap.keySet())) {
                for (String key : reportProducerMap.keySet()) {
                    Map<String, Object> mapData = reportProducerMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if ((personalStatsVo.getName() + "&" + personalStatsVo.getUserId()).equals(key)) {
                            personalStatsVo.setMakeNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计报告审批人员信息条数
            Map<Long, Map<Long, Object>> reportApprovalMap = statisticsMapper.selectreportApprovalMap(personalStats);
            if (CollectionUtil.isNotEmpty(reportApprovalMap.keySet())) {
                for (Long key : reportApprovalMap.keySet()) {
                    Map<Long, Object> mapData = reportApprovalMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if (personalStatsVo.getUserId().equals(key)) {
                            personalStatsVo.setApprovalNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计报告签发人员信息条数
            Map<Long, Map<Long, Object>> reportIssuerIdMap = statisticsMapper.selectReportIssueMap(personalStats);
            if (CollectionUtil.isNotEmpty(reportIssuerIdMap.keySet())) {
                for (Long key : reportIssuerIdMap.keySet()) {
                    Map<Long, Object> mapData = reportIssuerIdMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if (personalStatsVo.getUserId().equals(key)) {
                            personalStatsVo.setIssueNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 统计报告盖章人员信息条数
            Map<String, Map<String, Object>> reportSealerMap = statisticsMapper.selectReportSealerMap(personalStats);
            if (CollectionUtil.isNotEmpty(reportSealerMap.keySet())) {
                for (String key : reportSealerMap.keySet()) {
                    Map<String, Object> mapData = reportSealerMap.get(key);
                    for (PersonalStatsVo personalStatsVo : result.getList()) {
                        if ((personalStatsVo.getName() + "&" + personalStatsVo.getUserId()).equals(key)) {
                            personalStatsVo.setSealNumber(Math.toIntExact((Long) mapData.get("count")));
                        }
                    }
                }
            }
            // 数据为空 补0
            for (PersonalStatsVo personalStatsVo : result.getList()) {
                if (personalStatsVo.getEntrustNumber() == null) {
                    personalStatsVo.setEntrustNumber(0);
                }
                if (personalStatsVo.getTaskNumber() == null) {
                    personalStatsVo.setTestNumber(0);
                }
                if (personalStatsVo.getTestNumber() == null) {
                    personalStatsVo.setTestNumber(0);
                }
                if (personalStatsVo.getReviewNumber() == null) {
                    personalStatsVo.setReviewNumber(0);
                }
                if (personalStatsVo.getIssueNumber() == null) {
                    personalStatsVo.setIssueNumber(0);
                }
                if (personalStatsVo.getApprovalNumber() == null) {
                    personalStatsVo.setApprovalNumber(0);
                }
                if (personalStatsVo.getMakeNumber() == null) {
                    personalStatsVo.setMakeNumber(0);
                }
                if (personalStatsVo.getSealNumber() == null) {
                    personalStatsVo.setSealNumber(0);
                }
            }
        }
        return result;
    }

    @Override
    public List<TestTeamVo> selectAllTeamVo() {
        List<TestTeamVo> testTeamVos = statisticsMapper.selectAllTeamVo();
        for (TestTeamVo team1 : testTeamVos) {
            for (TestTeamVo team : testTeamVos) {
                if (team1.getPid() != 0) {
                    if (team1.getPid().equals(team.getId())) {
                        String name = team.getName() + "—" + team1.getName();
                        team1.setName(name);
                    }
                }
            }
        }
        return testTeamVos;
    }

    @Override
    public InputStream personalStatsExport(PageInfo list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
//建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
//在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("姓名");
        row1.createCell(1).setCellValue("创建委托");
        row1.createCell(2).setCellValue("分配任务");
        row1.createCell(3).setCellValue("试验检测");
        row1.createCell(4).setCellValue("数据复核");
        row1.createCell(5).setCellValue("报告制作");
        row1.createCell(6).setCellValue("报告审批");
        row1.createCell(7).setCellValue("报告签发");
        row1.createCell(8).setCellValue("报告盖章");
        for (int i = 0; i < list.getList().size(); i++) {
            PersonalStatsVo personVo = (PersonalStatsVo) list.getList().get(i);
            //在sheet里创建第二行
            HSSFRow row3 = sheet.createRow(i + 1);
            row3.createCell(0).setCellValue(personVo.getName());
            row3.createCell(1).setCellValue(personVo.getEntrustNumber()!=null?personVo.getEntrustNumber():0);
            row3.createCell(2).setCellValue(personVo.getTaskNumber()!=null?personVo.getTaskNumber():0);
            row3.createCell(3).setCellValue(personVo.getTestNumber()!=null?personVo.getTestNumber():0);
            row3.createCell(4).setCellValue(personVo.getReviewNumber()!=null?personVo.getReviewNumber():0);
            row3.createCell(5).setCellValue(personVo.getMakeNumber()!=null?personVo.getMakeNumber():0);
            row3.createCell(6).setCellValue(personVo.getApprovalNumber()!=null?personVo.getApprovalNumber():0);
            row3.createCell(7).setCellValue(personVo.getIssueNumber()!=null?personVo.getIssueNumber():0);
            row3.createCell(8).setCellValue(personVo.getSealNumber()!=null?personVo.getSealNumber():0);
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * task方法统计处理 处理 检测人
     */
    public void methodInspectorTask(List<TaskTestEntity> taskList,HashMap<Long, Integer> userIdMap,List<PersonalStatsVo> perons){
        // 统计任务单信息
        for(TaskTestEntity taskTestEntity:taskList){
            // 检测人 邓喜旺&1647657004269101
            if(!org.springframework.util.StringUtils.isEmpty(taskTestEntity.getInspector())){
                String[] strings2 = taskTestEntity.getInspector().split(",");
                for (int i = 0; i < strings2.length; i++) {
                    String[] strings3 = strings2[i].split("&");
                    Integer mapValue = userIdMap.get((Long.parseLong(strings3[1])));
                    if (mapValue != null) {
                        userIdMap.put((Long.parseLong(strings3[1])), mapValue += 1);
                    }
                }
            }
        }
        for (PersonalStatsVo personalStatsVo : perons) {
            Integer map = userIdMap.get(personalStatsVo.getUserId());
            if(map!=null){
                personalStatsVo.setTestNumber(map);
                userIdMap.put(personalStatsVo.getUserId(),0);
            }
        }
    }
    /**
     *  task方法统计处理 处理 复核人
     */
    public void methodReviewTask(List<TaskTestEntity> taskList,HashMap<Long, Integer> userIdMap,List<PersonalStatsVo> perons){
        for(TaskTestEntity taskTestEntity:taskList){
            // 复核人 邓喜旺&1647657004269101
            if(!org.springframework.util.StringUtils.isEmpty(taskTestEntity.getReviewer())){
                String[] strings2 = taskTestEntity.getReviewer().split(",");
                for (int i = 0; i < strings2.length; i++) {
                    String[] strings3 = strings2[i].split("&");
                    Integer mapValue = userIdMap.get((Long.parseLong(strings3[1])));
                    if (mapValue != null) {
                        userIdMap.put((Long.parseLong(strings3[1])), mapValue += 1);
                    }
                }
            }
        }
        for (PersonalStatsVo personalStatsVo : perons) {
            Integer map = userIdMap.get(personalStatsVo.getUserId());
            if(map!=null){
                personalStatsVo.setReviewNumber(map);
                userIdMap.put(personalStatsVo.getUserId(),0);
            }
        }
    }
    /**
     *  report方法统计处理 处理 报告审批
     */
    public void methodApprovalReport(List<ReportRecordEntity> reportList,HashMap<Long, Integer> userIdMap,List<PersonalStatsVo> perons){
        for(ReportRecordEntity reportRecordEntity:reportList){
            // 审批人 1647657004269101
            if(!org.springframework.util.StringUtils.isEmpty(reportRecordEntity.getVerifyerId())){
                    Integer mapValue = userIdMap.get(reportRecordEntity.getVerifyerId());
                    if (mapValue != null) {
                        userIdMap.put(reportRecordEntity.getVerifyerId(), mapValue += 1);
                    }
            }
        }
        for (PersonalStatsVo personalStatsVo : perons) {
            Integer map = userIdMap.get(personalStatsVo.getUserId());
            if(map!=null){
                personalStatsVo.setApprovalNumber(map);
                userIdMap.put(personalStatsVo.getUserId(),0);
            }
        }
    }

    /**
     *  report方法统计处理 处理 报告签发
     */
    public void methodIssuerIdReport(List<ReportRecordEntity> reportList,HashMap<Long, Integer> userIdMap,List<PersonalStatsVo> perons){
        for(ReportRecordEntity reportRecordEntity:reportList){
            // 签发人 1647657004269101
            if(!org.springframework.util.StringUtils.isEmpty(reportRecordEntity.getIssuerId())){
                Integer mapValue = userIdMap.get(reportRecordEntity.getIssuerId());
                if (mapValue != null) {
                    userIdMap.put(reportRecordEntity.getIssuerId(), mapValue += 1);
                }
            }
        }
        for (PersonalStatsVo personalStatsVo : perons) {
            Integer map = userIdMap.get(personalStatsVo.getUserId());
            if(map!=null){
                personalStatsVo.setIssueNumber(map);
                userIdMap.put(personalStatsVo.getUserId(),0);
            }
        }
    }

    /**
     *  report方法统计处理 处理 报告盖章
     */
    public void methodsealIdReport(List<ReportRecordEntity> reportList,HashMap<Long, Integer> userIdMap,List<PersonalStatsVo> perons){
        for(ReportRecordEntity reportRecordEntity:reportList){
            // 徐明月&1647502682230103
            if(!org.springframework.util.StringUtils.isEmpty(reportRecordEntity.getSealer())){
                String[] strings2 = reportRecordEntity.getSealer().split(",");
                for (int i = 0; i < strings2.length; i++) {
                    String[] strings3 = strings2[i].split("&");
                    Integer mapValue = userIdMap.get((Long.parseLong(strings3[1])));
                    if (mapValue != null) {
                        userIdMap.put((Long.parseLong(strings3[1])), mapValue += 1);
                    }
                }
            }
        }
        for (PersonalStatsVo personalStatsVo : perons) {
            Integer map = userIdMap.get(personalStatsVo.getUserId());
            if(map!=null){
                personalStatsVo.setSealNumber(map);
                userIdMap.put(personalStatsVo.getUserId(),0);
            }
        }
    }

    @Override
    public InputStream areaStatisticsExportFunction(List<AreaStatisticsResultVo> list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet=wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1=sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("任务来源");
        row1.createCell(1).setCellValue("委托收费额");
        row1.createCell(2).setCellValue("交费金额");
        row1.createCell(3).setCellValue("报告实收产值");
        row1.createCell(4).setCellValue("报告应收产值");
        for(int i=0;i<list.size();i++){
            AreaStatisticsResultVo areaStatisticsResultVo = list.get(i);
            //在sheet里创建第二行
            HSSFRow row3=sheet.createRow(i+1);
            row3.createCell(0).setCellValue(areaStatisticsResultVo.getTaskSource());
            row3.createCell(1).setCellValue(areaStatisticsResultVo.getActualPrice());
            row3.createCell(2).setCellValue(areaStatisticsResultVo.getReceivedPrice());
            row3.createCell(3).setCellValue(areaStatisticsResultVo.getActualReportPrice());
            row3.createCell(4).setCellValue(areaStatisticsResultVo.getSystemReportPrice());
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public PageInfo areaStatistics(StatisticsParamVo paramVo) {
        String flag = "left";
        PageHelper.clearPage();
        int entrust = statisticsMapper.areaStatisticsEntrust(paramVo);
        PageHelper.clearPage();
        int report = statisticsMapper.areaStatisticsReport(paramVo);
        if(entrust<report){
            flag = "right";
        }
        paramVo.setFlag(flag);
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        List<AreaStatisticsResultVo> list = statisticsMapper.areaStatistics(paramVo);
        PageInfo<AreaStatisticsResultVo> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public List<AreaStatisticsResultVo> areaStatisticsExport(StatisticsParamVo paramVo) {
        return statisticsMapper.areaStatistics(paramVo);
    }

    @Override
    public PageInfo teamStatistics(StatisticsParamVo paramVo) {
        List<TeamOutputValueVo> result = Lists.newArrayList();
        if(paramVo.getTeamId() == null){
            //查询父级报告产值
            List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics0715(paramVo.getBeginDate(), paramVo.getEndDate(), null);
            result.addAll(reportPrice);
        }else{
            List<Long> nodeTeam = teamMapper.getNodeTeamId(Long.parseLong(paramVo.getTeamId()));
            if(nodeTeam.size()>1){
                List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics0715(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
                result.addAll(reportPrice);
            }
            List<TeamOutputValueVo> node = statisticsMapper.teamStatisticsNode0715(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
            result.addAll(node);
        }
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        PageInfo<TeamOutputValueVo> resultPage = new PageInfo<>(result);
        return resultPage;
    }

    @Override
    public PageInfo teamStatistics1219(StatisticsParamVo paramVo) {
        List<TeamOutputValueVo> result = Lists.newArrayList();
        if (paramVo.getTeamId() == null) {
            //查询父级报告产值
            List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics231219(paramVo.getBeginDate(), paramVo.getEndDate(), null);
            result.addAll(reportPrice);
        } else {
            List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatisticsNode1219(paramVo);
            result.addAll(reportPrice);
//            List<TestTeam> testTeamList = teamMapper.selectNodeTeamPId(Long.parseLong(paramVo.getTeamId()));
//            List<Long> nodeTeam = testTeamList.stream().filter(testTeam -> testTeam.getId() != null).map(testTeam -> testTeam.getId().longValue()).collect(Collectors.toList());
//
//            if (nodeTeam.size() > 1) {
////                List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics231219(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
//                result.addAll(reportPrice);
//            } else {
//                List<TeamOutputValueVo> node = statisticsMapper.teamStatisticsNode0715(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
//                result.addAll(node);
//            }
        }
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        PageInfo<TeamOutputValueVo> resultPage = new PageInfo<>(result);
        return resultPage;
    }

    @Override
    public PageInfo teamStatisticsNode(StatisticsParamVo paramVo) {
        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        List<TeamOutputValueVo> list = statisticsMapper.teamStatisticsNode1219(paramVo);
        PageInfo<TeamOutputValueVo> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public List<TeamOutputValueVo> teamStatisticsExport(StatisticsParamVo paramVo) {
        List<TeamOutputValueVo> result = Lists.newArrayList();
        if(paramVo.getTeamId() == null){
            //查询父级报告产值
            List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics231219(paramVo.getBeginDate(), paramVo.getEndDate(), null);
            result.addAll(reportPrice);
        }else{
            List<Long> nodeTeam = teamMapper.getNodeTeamId(Long.parseLong(paramVo.getTeamId()));
            if(nodeTeam.size()>1){
                List<TeamOutputValueVo> reportPrice = statisticsMapper.teamStatistics231219(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
                result.addAll(reportPrice);
            }else {
                List<TeamOutputValueVo> node = statisticsMapper.teamStatisticsNode0715(paramVo.getBeginDate(), paramVo.getEndDate(), nodeTeam);
                result.addAll(node);
            }
        }
        return result;
    }

    @Override
    public List<TeamOutputValueVo> teamStatisticsNodeExport(StatisticsParamVo paramVo) {
        return statisticsMapper.teamStatisticsNode1219(paramVo);
    }

    @Override
    public PageInfo taskQuery1111(TaskStatsVo taskStatsVo) {
        // 分页数据
        List<TaskStatsVo> personList = new ArrayList<>();
        if (!org.springframework.util.StringUtils.isEmpty(taskStatsVo.getTeamId())) {
            PageHelper.clearPage();
            List<Long> nodeTeam = teamMapper.getNodeTeamId(Long.parseLong(taskStatsVo.getTeamId()));
            taskStatsVo.setNodeTeam(nodeTeam);
        }
        PageHelper.clearPage();
        PageHelper.startPage(taskStatsVo.getPageNum(), taskStatsVo.getPageSize());
        // 基于视图
        List<TaskStatsVo> taskList = statisticsMapper.getTaskListShow(taskStatsVo);
        // 获取状态
        PageInfo<TaskStatsVo> pageInfo = new PageInfo<>();
        pageInfo = new PageInfo<>(taskList);
        // 遍历list数据
        if (CollectionUtil.isNotEmpty(taskList)) {
            List<Long> taskIds = new ArrayList<>();
            for (TaskStatsVo statsVo : taskList) {
                taskIds.add(statsVo.getTaskId());
            }
            List<TaskStatsVo> list = statisticsMapper.getTaskInListShow(taskIds);
            pageInfo.setList(list);
            for (TaskStatsVo taskDetailInfoVo : pageInfo.getList()) {
                if (!taskDetailInfoVo.getSampleDetailList().isEmpty()) {
                    Set<String> set = new HashSet<>();
                    for (SampleDetailVo sampleEntity : taskDetailInfoVo.getSampleDetailList()) {
                        set.add(sampleEntity.getSampleName());
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String str : set) {
                        stringBuilder.append(str);
                    }
                    taskDetailInfoVo.setSampleName(stringBuilder.toString());
                    // 任务单 state = 6.原始记录已复核， 其余都未复核
                    taskDetailInfoVo.setTaskStatus(taskDetailInfoVo.getState() != null && taskDetailInfoVo.getState() >= 6 ? "完成" : "未完成");
                    TaskStatsVo data = new TaskStatsVo();
                    data.setTaskId(taskDetailInfoVo.getTaskId());
                    data.setTaskCode(taskDetailInfoVo.getTaskCode());
                    data.setRequestDate(taskDetailInfoVo.getRequestDate());
                    data.setFinishDate(taskDetailInfoVo.getFinishDate() != null ? taskDetailInfoVo.getFinishDate() : null);
                    data.setCost(taskDetailInfoVo.getCost());
                    data.setReportCode(taskDetailInfoVo.getReportCode());
                    //                data.setReportType(taskDetailInfoVo.getReportType());
                    data.setSampleName(taskDetailInfoVo.getSampleName());
                    data.setTaskStatus(taskDetailInfoVo.getTaskStatus());
                    // 报告编号
                    data.setReportCode(taskDetailInfoVo.getReportCode() != null ? taskDetailInfoVo.getReportCode() : "--");
                    if (StringUtils.isNotEmpty(taskDetailInfoVo.getReportType())) {
                        data.setReportType("0".equals(taskDetailInfoVo.getReportType()) ? "最终报告" : "中间报告");
                    } else {
                        data.setReportType("--");
                    }
                    personList.add(data);
                }
            }
        }
        return pageInfo;
    }

    @Override
    public InputStream teamStatisticsExportFunction(List<TeamOutputValueVo> list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet=wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1=sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("团队名称");
        row1.createCell(1).setCellValue("团队代码");
        row1.createCell(2).setCellValue("任务产值");
        row1.createCell(3).setCellValue("报告实收产值");
        row1.createCell(4).setCellValue("报告应收产值");
        for(int i=0;i<list.size();i++){
            TeamOutputValueVo teamOutputValueVo = list.get(i);
            //在sheet里创建第二行
            HSSFRow row3=sheet.createRow(i+1);
            row3.createCell(0).setCellValue(teamOutputValueVo.getTeamName());
            row3.createCell(1).setCellValue(teamOutputValueVo.getTeamCode());
            row3.createCell(2).setCellValue(teamOutputValueVo.getTaskPrice());
            row3.createCell(3).setCellValue(teamOutputValueVo.getActualReportPrice());
            row3.createCell(4).setCellValue(teamOutputValueVo.getSystemReportPrice());
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public List<LabelValueVo> getAreas() {
        return statisticsMapper.getAreas();
    }

    @Override
    public List<TeamTreeStructureEntity> getChirds() {
        List<TeamTreeStructureEntity> result = Lists.newArrayList();
        Long userId = ShiroUtils.getUserInfo().getUserId();
        if (userId != null) {
            Long teamId = teamMapper.getTeamIdByUserId(userId);
            if (teamId == null) {
                result = teamMapper.getAllTeams();
            } else {
                result = teamMapper.getChirds(teamId);
            }
        }
        return result;
    }

    @Override
    public List<StatisticsNodeDetailVo> teamStatisticsNodeDetailExport(String teamId, String beginDate, String endDate) {

        return statisticsMapper.getTeamStatisticsNode(teamId, beginDate, endDate);
    }

    @Override
    public InputStream teamStatisticsNodeDetailExportFunction(List<StatisticsNodeDetailVo> list) throws IOException {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet("sheet0");
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1 = sheet.createRow(0);
        //创建单元格并设置单元格内容
        row1.createCell(0).setCellValue("团队信息");
        row1.createCell(1).setCellValue("任务单号");
        row1.createCell(2).setCellValue("委托单号");
        row1.createCell(3).setCellValue("折扣率");
        row1.createCell(4).setCellValue("应收价格");
        row1.createCell(5).setCellValue("实际收费");

        for (int i = 0; i < list.size(); i++) {
            StatisticsNodeDetailVo teamOutputValueVo = list.get(i);
            //在sheet里创建第二行
            HSSFRow row3 = sheet.createRow(i + 1);
            // 团队信息
            row3.createCell(0).setCellValue(teamOutputValueVo.getTeamName());
            // 任务单号
            row3.createCell(1).setCellValue(teamOutputValueVo.getTaskCode());
            // 委托单号
            row3.createCell(2).setCellValue(teamOutputValueVo.getEntrustmentNo());
            // 折扣率
            row3.createCell(3).setCellValue(teamOutputValueVo.getDiscount());
            // 应收价格
            row3.createCell(4).setCellValue(teamOutputValueVo.getActualPrice() != null ? teamOutputValueVo.getActualPrice().toString() : null);
            // 实际收费
            row3.createCell(5).setCellValue(teamOutputValueVo.getSystemPrice() != null ? teamOutputValueVo.getSystemPrice().toString() : null);
        }
        //输出Excel文件 字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();
        return new ByteArrayInputStream(os.toByteArray());
    }
}
