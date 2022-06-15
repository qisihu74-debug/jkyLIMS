package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.config.PoiConfig;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private TestDetectionDao testDetectionDao;
    @Autowired
    private ReportRecordEntityMapper reportRecordEntityMapper;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;

    @Override
    public TaskDetailInfoVo getTaskDetailInfo(Long taskId) {
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfo(taskId);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }

        return taskDetailInfoVo;
    }

    @Override
    public TaskDetailInfoVo getTaskDetailInfoTwo(Long taskId, String[] deptIds) {
        TaskListParamVo paramVo = new TaskListParamVo();
        paramVo.setTaskId(taskId);
        if (deptIds != null) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            // 查询任务单 所属部门id
            Long deptId = taskMapper.getTaskDept(taskId);
            if (deptId == null) {
                paramVo.setDeptIds(null);
            } else {
                List<Long> ids = new ArrayList<>();
                ids.add(deptId);
                paramVo.setDeptIds(ids);
            }
        }
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }
        // 根据itemId 查询设备仪器信息集合
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        if(org.apache.commons.lang.StringUtils.isNotEmpty(checkItemInfoVo.getOriginUrl())){
                            String[] split = checkItemInfoVo.getOriginUrl().split("\\?");
                            String[] strings1 = split[0].split("\\/");
                            String fileName = strings1[4];
                            String[] names = fileName.split("\\.");
                            if("pdf".equals(names[1]) || "xls".equals(names[1]) || "xlsx".equals(names[1])){
                                checkItemInfoVo.setSuffixType("1");
                            }
                            else if("jpeg".equals(names[1]) || "png".equals(names[1]) || "jpg".equals(names[1])){
                                checkItemInfoVo.setSuffixType("2");
                            }
                        }

                        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(checkItemInfoVo.getItemId());
//                        List<Integer> result = Lists.newArrayList();
                        // 设置数组 存放
                        int[] arrayInt = new int[instrumentEntityList.size()];
                        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            for (int i = 0; i < instrumentEntityList.size(); i++) {
                                arrayInt[i] = instrumentEntityList.get(0).getId();
                            }
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        } else {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        }
                    }
                }
                //设置原材样品信息
                taskDetailInfoVo.setNodeSample(testSampleEntityMapper.selectByPid(sampleDetailVo.getId()));
            }
        }
        return taskDetailInfoVo;
    }

    @Override
    public TaskDetailInfoVo getTaskTestDetails(Long taskId, String[] deptIds) {
        TaskListParamVo paramVo = new TaskListParamVo();
        paramVo.setTaskId(taskId);
        if (deptIds != null) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            // 查询任务单 所属部门id
            Long deptId = taskMapper.getTaskDept(taskId);
            if (deptId == null) {
                paramVo.setDeptIds(null);
            } else {
                List<Long> ids = new ArrayList<>();
                ids.add(deptId);
                paramVo.setDeptIds(ids);
            }
        }
        // 处理 委托单的文件链接
        TaskDetailInfoVo taskDetailInfoVo = taskMapper.getTaskDetailInfoTwo(paramVo);
        if (taskDetailInfoVo.getFileUrl() != null) {
            String[] array = taskDetailInfoVo.getFileUrl().split(",");
            taskDetailInfoVo.setArray(array);
        }
        // 遍历数据 处理检测项下 检测项价格单位为空不展示。
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {

            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    Iterator<CheckItemInfoVo> it = sampleDetailVo.getCheckItemInfoList().iterator();
                    while (it.hasNext()) {
                        CheckItemInfoVo judgmentBasisVo = it.next();
                        if (judgmentBasisVo.getCheckPrice() == null) {
                            it.remove();
                        }
                    }
                }
            }
        }
        // 获取文件附件
        Long entrustId = taskMapper.getEntrustIdByTaskId(taskId);
        List<String> strings = entrustEntityMapper.getSampleStandard(entrustId);
        StringBuilder stringBuilder = new StringBuilder();
        if (strings != null && !strings.isEmpty()) {
            for (String str : strings) {
                stringBuilder.append(str);
                stringBuilder.append(",");
            }
            taskDetailInfoVo.setJudgmentBasis(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }
        // 根据itemId 查询设备仪器信息集合
        if (taskDetailInfoVo.getSampleDetailList() != null && !taskDetailInfoVo.getSampleDetailList().isEmpty()) {
            for (SampleDetailVo sampleDetailVo : taskDetailInfoVo.getSampleDetailList()) {
                if (sampleDetailVo.getCheckItemInfoList() != null && !sampleDetailVo.getCheckItemInfoList().isEmpty()) {
                    for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(checkItemInfoVo.getItemId());
//                        List<Integer> result = Lists.newArrayList();
                        // 设置数组 存放
                        int[] arrayInt = new int[instrumentEntityList.size()];
                        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            for (int i = 0; i < instrumentEntityList.size(); i++) {
                                arrayInt[i] = instrumentEntityList.get(0).getId();
                            }
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        } else {
                            checkItemInfoVo.setTestInstrumentEntityList(instrumentEntityList);
                            checkItemInfoVo.setTestInstrumentEntityArray(arrayInt);
                        }
                    }
                }
            }
        }


        return taskDetailInfoVo;
    }

    @Override
    public List<TaskListVo> getTaskList(TaskListParamVo paramVo) {
        return taskMapper.getTaskList(paramVo);
    }

    @Override
    public String getDeptIds(Long userId) {

        // 根据人员id 返回团队id集合
        List<Long> deptIds = teamMapper.getUserTeamIds(userId);
        if (deptIds != null && !deptIds.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Long detId : deptIds) {
                stringBuilder.append(detId);
                stringBuilder.append(",");
            }
            return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        }
        return null;
    }

    /**
     * 查询任务列表列表 并设置
     *
     * @param paramVo
     * @param deptIds
     * @return
     */
    @Override
    public PageInfo getTaskListTwo(TaskListParamVo paramVo, String[] deptIds) {

        if (deptIds != null && deptIds.length >= 1) {
            // 根据部门id 遍历包含下级部门信息
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < deptIds.length; i++) {
                ids.add(Long.valueOf(deptIds[i]));
            }
            paramVo.setDeptIds(ids);
        } else {
            paramVo.setDeptIds(null);
        }
        List<TaskListVo> dataList = new ArrayList<>();
        if (paramVo.getState() != null && paramVo.getState() != 1) {
            PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
            dataList = taskMapper.getTaskListContainsSample(paramVo);
        }
        if(!CollectionUtils.isEmpty(dataList)){
            // 处理任务单 与信息。
            //TODO gjl添加样品状态
            EntrustServiceImpl service = new EntrustServiceImpl();
            for (TaskListVo sampleListVo : dataList) {
                List<SamplePrivateInfoVo> sampleList = sampleListVo.getSampleList();

                List<SamplePrivateInfoVo> nodeSampleList = Lists.newArrayList();
                for (SamplePrivateInfoVo samplePrivateInfoVo : sampleList) {
                    sampleListVo.setOutward(samplePrivateInfoVo.getOutward());
                    String state = service.findStateBySampleId(samplePrivateInfoVo.getId(), entrustEntityMapper, taskMapper);
                    samplePrivateInfoVo.setState(state);
                    //TODO PSH查询子原材样品信息
                    List<SamplePrivateInfoVo> nodeSampleList1 = taskMapper.getNodeSampleList(samplePrivateInfoVo.getId());
                    if(!CollectionUtils.isEmpty(nodeSampleList1)){
                        nodeSampleList.addAll(nodeSampleList1);
                    }
                }
                sampleList.addAll(nodeSampleList);
                //增加关联委托单信息
                StringBuilder correlationTask = new StringBuilder();
                List<String> correlationTaskList = taskMapper.getCorrelationTask(sampleListVo.getTaskId());
                if(CollectionUtils.isEmpty(correlationTaskList)){
                    correlationTask.append("——");
                }else{
                    for (int i = 0; i < correlationTaskList.size(); i++) {
                        correlationTask.append(correlationTaskList.get(i));
                        if(i!=correlationTaskList.size()-1){
                            correlationTask.append("\n");
                        }
                    }
                }
                sampleListVo.setCorrelationTaskCode(correlationTask.toString());
            }
        }
        if (paramVo.getState() == 1) {
            PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
            dataList = taskMapper.getTaskListTwoGreater(paramVo);
            // 返回前端的话 sampleListVo.getSampleList() 空集合 []
            for(TaskListVo sampleListVo:dataList){
                //增加关联委托单信息
                StringBuilder correlationTask = new StringBuilder();
                List<String> correlationTaskList = taskMapper.getCorrelationTask(sampleListVo.getTaskId());
                if(CollectionUtils.isEmpty(correlationTaskList)){
                    correlationTask.append("——");
                }else{
                    for (int i = 0; i < correlationTaskList.size(); i++) {
                        correlationTask.append(correlationTaskList.get(i));
                        if(i!=correlationTaskList.size()-1){
                            correlationTask.append("\n");
                        }
                    }
                }
                sampleListVo.setCorrelationTaskCode(correlationTask.toString());
                if(CollectionUtils.isEmpty(sampleListVo.getSampleList())){
                    sampleListVo.setSampleList(new ArrayList<>());
                }
            }
        }
//        if (dataList != null && !dataList.isEmpty()) {
//            for (TaskListVo data : dataList) {
//                if (data.getInspector() != null) {
//                    String[] strings2 = data.getInspector().split(",");
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (int i = 0; i < strings2.length; i++) {
//                        String[] strings3 = strings2[i].split("&");
//                        stringBuilder.append(strings3[0]);
//                        stringBuilder.append(",");
//                    }
//                    data.setInspector(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
//                }
//            }
//        }
        PageInfo<TaskListVo> result = new PageInfo<>(dataList);
        return result;
    }

    @Override
    public PageInfo getSampleList(TaskListParamVo paramVo) {
        //查询用户团队及子团队ID
        List<Long> userTeamIds = teamMapper.getUserTeamIds(ShiroUtils.getUserInfo().getUserId());
        //根据团队信息查询样品信息
        String receiveTime = paramVo.getReceiveTime();
        if (receiveTime != null) {
            String[] split = receiveTime.split("~");
            paramVo.setBeginDate(split[0]);
            paramVo.setEndDate(split[1]);
        }
        if (userTeamIds.size() > 0) {
            paramVo.setDeptIds(userTeamIds);
        } else {
            paramVo.setDeptIds(null);
            return null;
        }

        PageHelper.startPage(paramVo.getPageNum(), paramVo.getPageSize());
        List<ReceiveSampleListVo> dataList = taskMapper.getSampleList(paramVo);
        //TODO gjl添加样品状态
        EntrustServiceImpl service = new EntrustServiceImpl();
        for (ReceiveSampleListVo sampleListVo : dataList) {
            List<SamplePrivateInfoVo> sampleList = sampleListVo.getSampleList();
            List<SamplePrivateInfoVo> nodeSampleList = Lists.newArrayList();
            for (SamplePrivateInfoVo samplePrivateInfoVo : sampleList) {
                String state = service.findStateBySampleId(samplePrivateInfoVo.getId(), entrustEntityMapper, taskMapper);
                samplePrivateInfoVo.setState(state);
                //TODO PSH查询子原材样品信息
                List<SamplePrivateInfoVo> nodeSampleList1 = taskMapper.getNodeSampleList(samplePrivateInfoVo.getId());
                if(!CollectionUtils.isEmpty(nodeSampleList1)){
                    nodeSampleList.addAll(nodeSampleList1);
                }
            }
            sampleList.addAll(nodeSampleList);
        }
        PageInfo<ReceiveSampleListVo> result = new PageInfo<>(dataList);
        return result;
    }

    @Transactional
    @Override
    public int receiveSample(ReceiveSampleParamVo paramVo) {
        paramVo.setState(2);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        if (entrustEntity != null) {
            //更新任务单状态为已领样
            taskMapper.updateEntrustById(paramVo.getTaskId(), 2);
        }
        //更新样品状态为领样
//        sampleEntityMapper.updateSampleState(paramVo.getSampleId(),1);
        return taskMapper.updateSampler(paramVo);
    }

    @Override
    public Boolean isIntendedEffectReceive(Long taskId, String sampler) {
        List<String> strings = teamMapper.getTaskIdUserName(taskId);
        if (strings != null && strings.size() > 0) {
            for (String userName : strings) {
                if (userName.equals(sampler)) {
                    // 匹配成功
                    return true;
                }
            }
            // 领样人不属于此任务单下团队成员
            return false;
        }
        logger.error(taskId + "\t任务单下部门成员为空");
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postGrabASingle(TaskTestEntity taskTestEntity) {
        // 抢单
        taskTestEntity.setState(1);
        // 根据角色查询团队名
        if (taskTestEntity.getReceiver() != null) {
            // 任务编号 团队名称+编号=任务编号
            TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(Long.parseLong(taskTestEntity.getReceiver()));
            if (dataTeam == null) {
                return false;
            }
            String strDate = taskTestEntity.getCode();
            String str1 = strDate.substring(0, 4);
            String str2 = strDate.substring(strDate.length() - 3);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("-");
            stringBuilder.append(str2);
            taskTestEntity.setTaskCode(dataTeam.getCode() + stringBuilder);
            taskTestEntity.setTeamId(String.valueOf(dataTeam.getId()));
            // 抢单时间
            java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
            taskTestEntity.setReceiveTime(currentDate);
            taskMapper.updateTestTask(taskTestEntity);
            return true;
        }
        return false;
    }

    @Override
    public Boolean postGrabASingleTwo(TaskTestEntity taskTestEntity) {
        // 抢单 并领样。
        taskTestEntity.setState(2);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
        if (entrustEntity != null) {
            //更新任务单状态为已领样
            taskMapper.updateEntrustById(taskTestEntity.getId(), 2);
        }
        // 更新样品状态 test_sample state = 1。 在检。
        //更新样品状态为领样
//        sampleEntityMapper.updateSampleState(paramVo.getSampleId(),1);
        // 抢单时间
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        taskTestEntity.setReceiveTime(currentDate);
        // 领样时间
        taskTestEntity.setSampleReceivingTime(currentDate);
        taskMapper.updateTestTask(taskTestEntity);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchPostGrabASingle(List<TaskTestEntity> taskTestEntitys) {
        int i = taskMapper.batchUpdateTestTask(taskTestEntitys);
       if(i==0){
           logger.error("批量修改任务单信息失败！");
       }
        for (int j = 0; j < taskTestEntitys.size(); j++) {
            TaskTestEntity taskTestEntity = taskTestEntitys.get(i);
            // 根据任务单主键 获取委托单主键
            EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
            if (entrustEntity != null) {
                //更新任务单状态为已领样
                taskMapper.updateEntrustById(taskTestEntity.getId(), 2);
            }
        }
        return true;
    }

    @Override
    public List<LabelValueTeamVo> getTeamUserName(Long UserLong) {
        TaskTestTeamEntity dataTeam = taskMapper.selectTeamCode(UserLong);
        if (dataTeam != null) {
            return taskMapper.selectTeamList(dataTeam.getId());
        }
        return null;
    }

    @Override
    public TeamVo getTeamUserNameTwo(Long UserLong) {
        TeamVo teamVo = new TeamVo();
        // 返回团队id集合 根据人员id
        List<TeamTreeStructureEntity> dataDepts = taskMapper.getTeamDeptVo(UserLong);
        if (dataDepts != null && !dataDepts.isEmpty()) {
            Set<Long> deptIds = new HashSet<>();
            for (TeamTreeStructureEntity data : dataDepts) {
                deptIds.add(data.getId());
                if (data.getSId() != null) {
                    deptIds.add(data.getSId());
                }
            }
            // 团队id集合 返回人员信息
            List<LabelValueVo> teamVos = taskMapper.getMemberInformation(deptIds);
            teamVo.setTeamVo(teamVos);
        }
        // 复核人集合

        teamVo.setReviewVo(null);
        // 审批人集合
        List<LabelValueVo> ApproverVo = taskMapper.getRoleInformation(Const.approverLongUserId);
        teamVo.setApproverVo(ApproverVo);
        // 签发人集合
        List<LabelValueVo> SignerVo = taskMapper.getRoleInformation(Const.signerLongUserId);
        teamVo.setSignerVo(SignerVo);
        // 获取科室下人员信息 （一个科室下）

        return teamVo;
    }

    @Override
    public Boolean getJudgmentTaskList(Long id) {
        if (taskMapper.getJudgmentTaskList(id) == 0) {
            return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public XWPFDocument downloadEntrust(TaskDetailInfoVo taskDetailInfoVo, InputStream object) {
        XWPFDocument doc = null;
        doc = new XWPFDocument(object);
        List<XWPFTable> tables = doc.getTables();
        //                原材表格逐行赋值
        StringBuilder stringBuilder = new StringBuilder();
        // 通过任务单 获取对应的任务单中信息。
        TaskListVo taskListVo = taskMapper.selectTaskListDetails(taskDetailInfoVo.getTaskId());
        Integer cost = 0;
        for (int j = 0; j < tables.size(); j++) {
            List<XWPFTableRow> rows = tables.get(j).getRows();
            //表头部分
            Map<String, String> testMap = new HashMap<String, String>();
            // 下单日期
            testMap.put("date", taskDetailInfoVo.getOrderTime());
            // 编号
            testMap.put("number", taskDetailInfoVo.getTaskCode());
            // 发布人
            if(taskListVo.getRecorder()!=null){
                testMap.put("recorder", taskListVo.getRecorder());
            }
            else{
                testMap.put("recorder", "--");
            }
            // 领样人
            if(taskListVo.getSampler()!=null){
                testMap.put("sampler", taskListVo.getSampler());
            }else {
                testMap.put("sampler", "--");
            }
            // 领样日期
           if(taskListVo.getSampleReceivingTime()!=null){

               String sampleReceivingTime = DateUtil.longToString(taskListVo.getSampleReceivingTime().getTime()/1000,"yyyy-MM-dd");
               testMap.put("sampleReceivingTime", sampleReceivingTime);
           }else {
               testMap.put("sampleReceivingTime", "--");
           }
            // 样品描述
           if(taskListVo.getSampleStateDescription()!=null){
               testMap.put("sampleStateDescription", taskListVo.getSampleStateDescription());
           }
           else{
               testMap.put("sampleStateDescription", "--");

           }
            //委托单是否留样。
           if( taskListVo.getIssueReport()!=null&&taskListVo.getIssueReport().equals("是")){
                testMap.put("issueReport", "☑退还  □弃样");
            }
            else{
                testMap.put("issueReport", "□退还  ☑弃样");
            }
            //解析替换文本段落对象
//            PoiConfig.changeText(doc, testMap);
            //遍历表格,并替换模板
            PoiConfig.eachTable(rows, testMap);
            if (j == 0) {
                //设置样品信息
                List<SampleDetailVo> sampleEntityList = Lists.newArrayList();
                // 遍历 原材样品数据
                List<SampleDetailVo> samples = taskDetailInfoVo.getSampleDetailList();
                // 处理为配合比。
                List<TestSampleEntity> nodeSample = taskDetailInfoVo.getNodeSample();
                if (nodeSample != null && nodeSample.size() > 0) {
                    for (TestSampleEntity node : nodeSample) {
                        SampleEntity entity = new SampleEntity(node);
                        SampleDetailVo sampleDetailVo = new SampleDetailVo();
                        sampleDetailVo.setSampleName(entity.getSampleName());
                        sampleDetailVo.setSpecs(entity.getSpecs());
                        sampleDetailVo.setBatchNumber(entity.getBatchNumber());
                        sampleDetailVo.setSampleQuantity(entity.getSampleQuantity());
                        sampleDetailVo.setGeneration(entity.getGeneration());
                        sampleDetailVo.setSampleOrigin(entity.getSampleOrigin());
                        sampleDetailVo.setSampleRemark(entity.getSampleRemark());
                        sampleDetailVo.setSampleCode(entity.getSampleCode());
                        // 生产厂家/样品产地
                        sampleDetailVo.setManufacturer(entity.getManufacturer());
                        sampleEntityList.add(sampleDetailVo);
                    }
                    samples.addAll(sampleEntityList);
                }
                if (taskDetailInfoVo.getSampleDetailList() == null || taskDetailInfoVo.getSampleDetailList().isEmpty()) {
                    return doc;
                }
                if (samples.size() > 5) {
                    AsposeUtil.addRows(tables.get(0), 4, samples.size() - 5);
                }

                for (int i = 0; i < samples.size(); i++) {
                    SampleDetailVo sampleDetailVo = samples.get(i);
                    // 补充表格数据 样品名称
                    rows.get(i + 2).getTableCells().get(0).setText(sampleDetailVo.getSampleName());
                    // 规格/等级
                    rows.get(i + 2).getTableCells().get(1).setText(sampleDetailVo.getSpecs());
                    // 批号/编号
//                    rows.get(i + 1).getTableCells().get(2).setText(sampleDetailVo.getBatchNumber());
                    // 样品数量
                    rows.get(i + 2).getTableCells().get(2).setText(sampleDetailVo.getSampleQuantity());
                    // 样品产地
//                    rows.get(i + 1).getTableCells().get(4).setText(sampleDetailVo.getManufacturer());
                    //样品编号
                    rows.get(i + 2).getTableCells().get(3).setText(sampleDetailVo.getSampleCode());
                    // 备注
                    rows.get(i + 2).getTableCells().get(4).setText(sampleDetailVo.getSampleRemark());
                    // 处理检测项 依据名去除 只保留编号。
                    if (sampleDetailVo.getCheckItemInfoList() != null) {
                        for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                            String name = checkItemInfoVo.getCheckItemName();
                            stringBuilder.append(name);
                            if (!StringUtils.isEmpty(checkItemInfoVo.getStandardName())) {
                                stringBuilder.append("（");
                                String s = checkItemInfoVo.getStandardName();
                                String aa = s.split("《")[0];
                                stringBuilder.append(aa);
                                stringBuilder.append("）");
                            }
                            stringBuilder.append("，");
                            // 获取样品的检测项信息
                            checkItemInfoVo.setTimes(checkItemInfoVo.getTimes()!=null?checkItemInfoVo.getTimes():0);
                            checkItemInfoVo.setCheckPrice(checkItemInfoVo.getCheckPrice()!=null?checkItemInfoVo.getCheckPrice():"0");
                            cost +=  (checkItemInfoVo.getTimes()*Integer.parseInt(checkItemInfoVo.getCheckPrice()));
                        }
                    }
                }
            }
            // 提供资料
            if (j == 1) {
                rows.get(0).getTableCells().get(0).setText(taskDetailInfoVo.getPresentInformation());
                // 取样方式
                rows.get(1).getTableCells().get(1).setText(taskDetailInfoVo.getSamplingMethod());
                // 检验目的
                rows.get(1).getTableCells().get(3).setText(taskDetailInfoVo.getCheckPurpose());
                // 产品标准
                rows.get(1).getTableCells().get(5).setText(taskDetailInfoVo.getJudgmentBasis());
                // 检测项目及检验依据
                String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                rows.get(2).getTableCells().get(1).setText(substring);
                // 要求检验完成日期
                rows.get(3).getTableCells().get(1).setText(taskDetailInfoVo.getRequiredCompletionTime());
                // 本单产值
                rows.get(3).getTableCells().get(3).setText(String.valueOf(cost));
            // 获取委托单印章
            if (taskDetailInfoVo.getSealType() != null) {
                String[] sealTypes = taskDetailInfoVo.getSealType().split(",");
                // 任务单表格数据
                List<String> totalData = new ArrayList<>();
                totalData.add("综合甲级");
                totalData.add("CMA");
                totalData.add("CNAS");
                // 勾选数据
                String check = "☑";
                // 未勾选数据
                String untick = "□";
                // map 数据 委托单盖章数据
                HashMap<String, Integer> map = new HashMap<>();
                //   HashMap 去重利器
                StringBuilder sealType = new StringBuilder();
                for (int i = 0; i < sealTypes.length; i++) {
                    for (String totalData1 : totalData) {
                        // 盖章勾选
                        if (sealTypes[i].equals(totalData1)) {
                            sealType.append(check);
                            sealType.append(sealTypes[i]);
                            map.put(sealTypes[i], 1);
                        }
                    }
                }
                // 不勾选的数据查找
                for (String rowData1 : totalData) {
                    if (map.get(rowData1) == null) {
                        sealType.append(untick);
                        sealType.append(rowData1);
                    }
                }
                rows.get(20).getTableCells().get(1).setText(sealType.toString());
            }
            else{
                rows.get(20).getTableCells().get(1).setText("□综合甲级□CMA□CNAS");
            }
            }
        }
        return doc;
    }

    @Override
    public OriginalRecordDataVo getOriginalData(Long taskId, Integer sampleId, Integer checkItemId, Integer idItem) {
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(taskId);
        //工程名称及工程部位信息去掉不展示
        entrustBaseInfo.setProjectName("/");
        entrustBaseInfo.setProjectPart("/");
        //生成记录编号
        String recordNumber = "JL-"+entrustBaseInfo.getTaskCode();
        //获取样品信息
        TemplateSampleVo sampleVo = sampleEntityMapper.getOriginalSampleInfo(sampleId);

        // 得到样品信息数据; 分割。
        sampleVo.setSampleName(sampleVo.getSampleName() + "；");
        sampleVo.setSampleNumber(sampleVo.getSampleNumber() + "；");
        sampleVo.setSampleQuantity(sampleVo.getSampleQuantity() + "；");
        // 处理样品 外观描述，和 外观
        if (sampleVo.getOutwardDescribe() != null && !sampleVo.getOutwardDescribe().equals("") && sampleVo.getSampleDesc() != null && !sampleVo.getSampleDesc().equals("")) {
            sampleVo.setSampleDesc(sampleVo.getSampleDesc().substring(1, sampleVo.getSampleDesc().length() - 1) + "\t" + sampleVo.getOutwardDescribe());
        } else {
            if (sampleVo.getSampleDesc() != null && !sampleVo.getSampleDesc().equals("")) {
                sampleVo.setSampleDesc(sampleVo.getSampleDesc().substring(1, sampleVo.getSampleDesc().length() - 1));
            }
        }

        //获取检测依据
        log.debug("执行上一行完成---------------");
        String checkBasis = taskMapper.getCheckBasis(checkItemId, entrustBaseInfo.getId(), sampleId);
        log.debug("执行下一行完成--------------");
        //获取判定依据
        List<String> judgeBasisList = taskMapper.getJudgeBasis(sampleId, entrustBaseInfo.getId());
        StringBuilder judgeBasis = new StringBuilder("");
        if (judgeBasisList != null && !judgeBasisList.isEmpty()) {
            for (int i = 0; i < judgeBasisList.size(); i++) {
                judgeBasis.append(judgeBasisList.get(i));
                if(i != judgeBasisList.size()-1 ){
                    judgeBasis.append("\n");
                }
            }
        }
        StringBuilder sampleTime = new StringBuilder(sampleVo.getSampleTime() + ";");

        //补充原材信息
        if(sampleVo.getSampleType().contains("配合比")){
            List<SampleEntity> sampleEntities = sampleEntityMapper.selectByPid(sampleId);
            for (SampleEntity sampleEntity : sampleEntities) {
                sampleTime.append("样品名称：");
                sampleTime.append(sampleEntity.getAliasName()== null ? "——" :sampleEntity.getAliasName());
                sampleTime.append("；");
                sampleTime.append("样品编号：");
                sampleTime.append(sampleEntity.getSampleCode()== null ? "——" :sampleEntity.getSampleCode());
                sampleTime.append("；");
                sampleTime.append("样品数量：");
                sampleTime.append(sampleEntity.getSampleQuantity()== null ? "——": sampleEntity.getSampleQuantity());
                sampleTime.append("；");
                sampleTime.append("样品描述：");
                StringBuilder outward = new StringBuilder();
                if(sampleEntity.getOutward() != null){
                    outward.append(sampleEntity.getOutward());
                    if(sampleEntity.getOutwardDescribe() != null){
                        outward.append(",");
                        outward.append(sampleEntity.getOutwardDescribe());
                    }
                }else{
                    if(sampleEntity.getOutwardDescribe() != null){
                        outward.append(sampleEntity.getOutwardDescribe());
                    }
                }
                if("".equals(outward.toString())){
                    outward.append("——");
                }
                outward.append("；");
                sampleTime.append(outward);
                sampleTime.append("来样时间：");
                sampleTime.append(sampleEntity.getReceivedDate());
            }
        }
        sampleVo.setSampleTime(sampleTime.toString());
        OriginalRecordDataVo result = new OriginalRecordDataVo(recordNumber, entrustBaseInfo, sampleVo, checkBasis, judgeBasis.toString());
        // 检测项 开始检测日期。

        // 所使用的设备仪器。
        List<TestInstrumentEntity> instrumentEntityList = taskMapper.getInstrumentEntityList(idItem);
        if (instrumentEntityList != null && !instrumentEntityList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < instrumentEntityList.size(); i++) {
                stringBuilder.append(instrumentEntityList.get(i).getModel());
                stringBuilder.append(instrumentEntityList.get(i).getName());
                stringBuilder.append("（");
                stringBuilder.append(instrumentEntityList.get(i).getCode());
                stringBuilder.append("）");
                if(i != instrumentEntityList.size()-1){
                    stringBuilder.append("、");
                }
            }
            result.setEquipment(stringBuilder.toString());
        }

        return result;
    }

    @Override
    public String getOriginalTemplate(Integer checkItemId) {
        return taskMapper.getOriginalTemplate(checkItemId);
    }

    @Override
    public String getOriginalTemplateUrl(Integer checkItemId) {
        return taskMapper.getOriginalTemplateUrl(checkItemId);
    }

    @Override
    public int uploadOriginalRecord(OriginalRecordParamVo paramVo, MultipartFile file) {
        //获取委托单信息
        EntrustEntity entrustBaseInfo = taskMapper.getEntrustBaseInfo(paramVo.getTaskId());
        // 获取检测项详细信息 判断文件是否上传
        SampleItemInstrumentEntity sampleItemInstrumentEntity = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailIf(entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId());
        if (sampleItemInstrumentEntity.getOriginUrl() != null) {
            // 文件已经存在
            return 2;
        }
        String upload = "";
        String fileUrlStr = "";
        // 原始名称
        String fileName = "";
        if (file != null) {
            String name = file.getOriginalFilename();
            String[] strings = name.split("\\.");
            upload = MinIoUtil.upload("upload-original-record", file, entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1]);
            fileUrlStr = entrustBaseInfo.getId() + "-" + paramVo.getSampleId() + "-" + paramVo.getCheckItemId() + "." + strings[strings.length - 1];
            fileName = strings[0] + "." + strings[strings.length - 1];
        }
        // 根据任务单主键 获取委托单主键
        if (entrustBaseInfo != null) {
            if (entrustBaseInfo.getState() < 5) {
                taskMapper.updateEntrustById(entrustBaseInfo.getId(), 5);
            }
        }
        return taskMapper.updateOriginalFile(upload, entrustBaseInfo.getId(), paramVo.getSampleId(), paramVo.getCheckItemId(), fileUrlStr, fileName);
    }

    @Override
    public Boolean uploadingBatch(List<Integer> ids, MultipartFile file) {
        List<SampleItemInstrumentEntity> entityList = new ArrayList<>();
        for (Integer id : ids) {
            SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
            String upload = "";
            String fileUrlStr = "";
            // 原始名称
            String fileName = "";
            long fileUrlLongId = GenID.getID();
            if (file != null) {
                String name = file.getOriginalFilename();
                String[] strings = name.split("\\.");
                upload = MinIoUtil.upload("upload-original-record", file, fileUrlLongId + "." + strings[strings.length - 1]);
                fileUrlStr = fileUrlLongId + "." + strings[strings.length - 1];
                fileName = strings[0] + "." + strings[strings.length - 1];
            }
            sampleItemInstrumentEntity.setItemId(id);
            sampleItemInstrumentEntity.setOriginUrl(upload);
            sampleItemInstrumentEntity.setFileUrlStr(fileUrlStr);
            sampleItemInstrumentEntity.setItemFileName(fileName);
            entityList.add(sampleItemInstrumentEntity);
        }
        taskMapper.updateTestEntrustedSampleCheckitemRel(entityList);
        return true;
    }

    @Override
    public Boolean effectDataSet(List<Integer> ids) {
        List<SampleItemInstrumentEntity> list = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailList(ids);
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : list) {
            if (sampleItemInstrumentEntity.getOriginUrl() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检测项复核数据
     *
     * @param itemId
     * @return
     */
    @Override
    public ReviewVo getReviewInfo(Integer itemId) {
        // 根据检测项主键 获取委托单主键
        return taskMapper.getReviewInfo(itemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String passorno(Integer itemId, Integer state, String opinion) {
        // 驳回=4，通过=3，撤回=1
        if (state != null) {
            if (state == 1) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                // 检测项 撤回时 考虑 （盖章的话） test_report_record state = '7'
                // 通过委托单id 获取报告test_report_record state 状态
                ReportRecordEntity reportRecordEntity = reportRecordEntityMapper.selectByEntrustId(sampleItemInstrumentEntity2.getEntrustId());
                if (reportRecordEntity != null && reportRecordEntity.getState() != null) {
                    if (Integer.parseInt(reportRecordEntity.getState()) == 7 && Integer.parseInt(reportRecordEntity.getState()) >= 7) {
                        return "撤回失败！此报告已经盖章";
                    }
                }

                if (sampleItemInstrumentEntity2.getOriginUrl() != null && !sampleItemInstrumentEntity2.getOriginUrl().isEmpty()) {
                    // 去清除 MinIo 桶数据。
                    try {
                        MinIoUtil.deleteFile("upload-original-record", sampleItemInstrumentEntity2.getFileUrlStr());
                    } catch (Exception e) {
                        logger.info("修改委托下清除 MinIo 桶数据 出错");
                    }
                }
                SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                // 待检状态 =0
                sampleItemInstrumentEntity.setState(0);
                // 检测项 开始时间更新
                sampleItemInstrumentEntity.setStartTime(null);
                sampleItemInstrumentEntity.setItemId(itemId);
                sampleItemInstrumentEntity.setOpinion(opinion);
                sampleItemInstrumentEntity.setOriginUrl(null);
                sampleItemInstrumentEntity.setFileUrlStr(null);
                sampleItemInstrumentEntity.setEndTime(null);
                testDetectionDao.updateTaskPassorno(sampleItemInstrumentEntity);
                // 删除设备仪器
                testDetectionDao.deleteInstrument(itemId);
                return "撤回成功，检测项回到初始状态";
            }
            if (state == 3) {
                // 检测项复核通过
                taskMapper.updateState(itemId, 3, opinion);
                SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
                if (sampleItemInstrumentEntity2 != null && sampleItemInstrumentEntity2.getEntrustId() != null) {
                    EntrustAddVo entrustBaseInfo = entrustEntityMapper.selectByKeyId(sampleItemInstrumentEntity2.getEntrustId());
                    if (entrustBaseInfo.getState() != null && entrustBaseInfo.getState() < 6) {
                        taskMapper.updateEntrustById(entrustBaseInfo.getId(), 6);
                    }
                }
                // 通过委托单id 和部门ID为条件  遍历（判断每个状态 state = 3 复核通过。 改变任务单 6 否则 任务单还是为试验完成）
                List<Integer> states = testDetectionDao.getSampleCheckitemRelDetailState(sampleItemInstrumentEntity2.getEntrustId(), sampleItemInstrumentEntity2.getDeptId());
                for (Integer stateItem : states) {
                    if (stateItem != 3) {
                        return "当前任务单下检测项未全部复核成功";
                    }
                }
                // 修改test_task state 状态 为6：
//                Long testTaskId = taskMapper.getTestTaskId(sampleItemInstrumentEntity2.getEntrustId(), sampleItemInstrumentEntity2.getDeptId());
                Long testTaskId = taskMapper.getReturnTaskId(itemId);
                TaskTestEntity taskTestEntity = new TaskTestEntity();
                taskTestEntity.setId(testTaskId);
                taskTestEntity.setState(6);
                // 任务单 复核成功 记录复核时间。
                taskTestEntity.setReviewTime(new Date());
                taskMapper.updateTestTask(taskTestEntity);
                return "任务单复核成功";
            }
        }
        // state = 4 检测项状态驳回
        int status = taskMapper.updateState(itemId, state, opinion);
        if (status > 0) {
            return "成功";
        }
        return "失败";
    }

    @Override
    public String passorno_delete(Integer itemId) {
        SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        // 检测项 撤回时 考虑 （盖章的话） test_report_record state = '7'
        // 通过委托单id 获取报告test_report_record state 状态
        ReportRecordEntity reportRecordEntity = reportRecordEntityMapper.selectByEntrustId(sampleItemInstrumentEntity2.getEntrustId());
        if (reportRecordEntity != null && reportRecordEntity.getState() != null) {
            if (Integer.parseInt(reportRecordEntity.getState()) == 7 && Integer.parseInt(reportRecordEntity.getState()) >= 7) {
                return "撤回失败！此报告已经盖章";
            }
        }

        if (sampleItemInstrumentEntity2.getOriginUrl() != null && !sampleItemInstrumentEntity2.getOriginUrl().isEmpty()) {
            // 去清除 MinIo 桶数据。
            try {
                MinIoUtil.deleteFile("upload-original-record", sampleItemInstrumentEntity2.getFileUrlStr());
            } catch (Exception e) {
                logger.info("修改委托下清除 MinIo 桶数据 出错");
            }
        }
        SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
        sampleItemInstrumentEntity.setStartTime(null);
        sampleItemInstrumentEntity.setItemId(itemId);
        sampleItemInstrumentEntity.setOriginUrl(null);
        sampleItemInstrumentEntity.setFileUrlStr(null);
        sampleItemInstrumentEntity.setEndTime(null);
        sampleItemInstrumentEntity.setItemFileName(null);
        testDetectionDao.updateTaskPassorno(sampleItemInstrumentEntity);
        return "成功";
    }

    @Override
    public PersonInfoVo getPersonInfo(Long taskId) {
        return taskMapper.getPersonInfo(taskId);
    }

    @Override
    public int updatePersonInfo(PersonInfoVo vo) {
        return taskMapper.updatePersonInfo(vo);
    }

    /**
     * 扩展模板样品行列
     *
     * @param table            原始表格
     * @param rows             原始表格行数
     * @param sampleDetailList 待处理数据
     * @param modelSampleRows  需要新增行
     * @param columns          列数
     */
    public List<XWPFTableRow> extendTable(XWPFTable table, List<XWPFTableRow> rows, List<SampleDetailVo> sampleDetailList,
                                          int modelSampleRows, int columns) {
        //获取表格对应的行
        rows = table.getRows();
        // 判断表格行数 >5 sampleDetailList.size()
        // 补充增加表格数量。
        if (sampleDetailList.size() > modelSampleRows) {
            // 3.25 测试表格插入数据
            int addRows = sampleDetailList.size() - modelSampleRows;
            // 表格插入
            XWPFDocument doc1 = new XWPFDocument();
            XWPFTable newTable = doc1.createTable(addRows, columns);  //2行7格
            // 创建表格后直接进行存放 后续多余数据
            List<XWPFTableRow> dataTable = newTable.getRows();
            int j = 0;
            for (int i = modelSampleRows; i < sampleDetailList.size(); i++) {
                SampleDetailVo sampleDetailVo = sampleDetailList.get(i);
                // 补充表格数据 样品名称
                dataTable.get(j).getTableCells().get(0).setText(sampleDetailVo.getSampleName());
                // 规格/等级
                dataTable.get(j).getTableCells().get(1).setText(sampleDetailVo.getSpecs());
                // 批号/编号
                dataTable.get(j).getTableCells().get(2).setText(sampleDetailVo.getBatchNumber());
                // 样品数量
                dataTable.get(j).getTableCells().get(3).setText(sampleDetailVo.getGeneration());
                // 样品产地
                dataTable.get(j).getTableCells().get(4).setText(sampleDetailVo.getSampleOrigin());
                //样品编号
                dataTable.get(j).getTableCells().get(5).setText(sampleDetailVo.getSampleCode());
                // 备注
                dataTable.get(j).getTableCells().get(6).setText(sampleDetailVo.getRemark());
                table.addRow(dataTable.get(j));
                j++;
            }
            rows = table.getRows();
        }
        return rows;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchReview(TaskStatsVo taskStatsVo) {
        // 批量操作。
        List<TaskStatsItemVo> list = new ArrayList<>();
        // 检测项id
        Integer itemId =0;
        for(int i=0;i<taskStatsVo.getIntegers().length;i++){
            TaskStatsItemVo taskStatsItemVo = new TaskStatsItemVo();
            taskStatsItemVo.setItemId(taskStatsVo.getIntegers()[i]);
            taskStatsItemVo.setState(taskStatsVo.getState());
            taskStatsItemVo.setRemark(taskStatsVo.getRemark()!=null?taskStatsVo.getRemark():"--");
            list.add(taskStatsItemVo);
            itemId = taskStatsVo.getIntegers()[i];
        }
        taskMapper.batchReview(list);
        // 通过任务单 获取检测项状态 =3 通过状态
        if(taskStatsVo.getState()==3){
            SampleItemInstrumentEntity sampleItemInstrumentEntity2 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
            List<Integer> states = taskMapper.selectCheckItemState(taskStatsVo.getTaskId(),sampleItemInstrumentEntity2.getDeptId());
            for (Integer stateItem : states) {
                if (stateItem != 3) {
                    return "当前任务单下检测项未全部复核成功";
                }
            }
            // 修改test_task state 状态 为6：
            TaskTestEntity taskTestEntity = new TaskTestEntity();
            taskTestEntity.setId(taskStatsVo.getTaskId());
            taskTestEntity.setState(6);
            // 任务单 复核成功 记录复核时间。
            taskTestEntity.setReviewTime(new Date());
            taskMapper.updateTestTask(taskTestEntity);
            return "任务单复核成功";
        }
        return "成功";
    }

    @Override
    public ZipOutputStream packagingWorkbookZip(Integer[] Ids, HttpServletResponse response) throws IOException {
        // 通过输入参数 返回 对应的处理成功的EXCEL数据。
        ServletOutputStream outputStream = response.getOutputStream();
        ZipOutputStream out = new ZipOutputStream(outputStream);

        // 批量获取 检测项id（有可能对应多个模板） 再进行填充。
        // 通过检测项id 获取 相应的 id关联信息。
        List<TaskIdEntity> ids = taskMapper.selectconditionId(Ids);
        for(int i=0; i<ids.size(); i++ ){
            TaskIdEntity data = ids.get(i);
            // 有序信息。
            OriginalRecordDataVo originalData = getOriginalData(data.getTaskId(), data.getSampleId(),data.getCheckItemId(), data.getIdItem());
            Map<String, OriginalRecordDataVo> result = Maps.newHashMap();
            result.put("result", originalData);

            // 根据单个Workbook 进行处理打包。
            HttpServletResponse response1 = null;
            // 原始记录模板 比对信息
            try {
                // 链接 get minIo 检查是否存在
                String[] split = data.getFileUrl().split("/");
                String[] split1 = split[4].split("\\?");
                XLSTransformer transformer = new XLSTransformer();
                InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
                Workbook workbook = methodPlugTheData(data.getFileUrl(),result, null);
                SampleServiceImpl.DealWithZip(workbook, data.getTaskCode()+data.getCheckItemName()+".xls", out);
            }
            catch (Exception e){
                log.info("输出异常\t"+e);
            }

        }
        // 关闭输入流
        out.closeEntry();
        if (out != null) {
            out.flush();
            out.close();
        }
        return out;
    }

    /**
     * 如果原始记录文件不为空 塞数据
     */
    public Workbook methodPlugTheData(String originalTemplate,Map<String, OriginalRecordDataVo> result,HttpServletResponse response) throws InvalidFormatException {
        String[] split = originalTemplate.split("/");
        String[] split1 = split[4].split("\\?");
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("file-resources", split1[0]);
        org.apache.poi.ss.usermodel.Workbook workbook = null;
        workbook = transformer.transformXLS(fileStream, result);
        return workbook;
    }
}
