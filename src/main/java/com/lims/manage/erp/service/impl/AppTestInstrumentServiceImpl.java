package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AppTestInstrumentService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;

/**
 * @Author: DLC
 * @Date: 2023/4/3 15:39
 */
@Service
@Slf4j
public class AppTestInstrumentServiceImpl implements AppTestInstrumentService {

    @Autowired
    TaskMapper taskMapper;
    @Autowired
    TeamMapper teamMapper;
    @Autowired
    TaskServiceImpl taskService;
    @Autowired
    InstrumentRecordEntityMapper instrumentRecordEntityMapper;
    @Autowired
    TestDetectionDao testDetectionDao;
    @Autowired
    LogManagerService logManagerService;
    @Autowired
    TestInstrumentDao testInstrumentDao;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    public List<TaskListVo> detectionTaskList(String search, Long userId) {
        return taskMapper.detectionTaskList(search, userId);
    }

    @Override
    public List<TaskListVo> taskList(String search, Long instrumentId) {
        return taskMapper.taskList(search, instrumentId);
    }

    @Override
    public List<LabelValueVo> returnPersonList(Long userId) {
        // 获取当前用户所在科室id
        Long department = teamMapper.getTeamIdByUid(userId);
        // 获取顶级部门 为空则是当前部门
        Long topDepartment = taskService.getTopDepartment(department);
        if (StringUtils.isEmpty(topDepartment)) {
            topDepartment = department;
        }
        // 获取团队下所有子集团队下技术人员集合
        List<TestTeam> testTeamList = teamMapper.getIdsByTeamId(topDepartment);
        List<LabelValueVo> teamVos = new ArrayList<>();
        if (CollectionUtils.isEmpty(testTeamList)) {
            // 团队id集合 返回人员信息
            Set<Long> deptIds = new HashSet<>();
            deptIds.add(topDepartment);
            teamVos = taskMapper.getMemberInformation(deptIds);
        } else {
            for (TestTeam testTeam : testTeamList) {
                if (!StringUtils.isEmpty(testTeam)) {
                    LabelValueVo labelValueVo = new LabelValueVo();
                    labelValueVo.setLabel(testTeam.getName());
                    labelValueVo.setValue(testTeam.getUserId());
                    teamVos.add(labelValueVo);
                }
            }
        }
        return teamVos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startToTest(InstrumentVo instrumentVo) {
        if (instrumentVo != null && !CollectionUtils.isEmpty(instrumentVo.getCheckItemInfoList())) {
            //校验设备使用状态
            Long id = instrumentVo.getId();
            InstrumentRecordEntity recordEntity1 = instrumentRecordEntityMapper.checkDeviceStatus(id);
            if(recordEntity1 != null){
                return ResultUtil.error("设备被用户【"+recordEntity1.getUser()+"】正在任务单【"+recordEntity1.getTaskCode()+"】中使用！");
            }
            //校验设备开始时间
            Date startTime = instrumentVo.getStartTime();
            InstrumentRecordEntity recordEntity2 = instrumentRecordEntityMapper.checkDeviceStartTime(id, startTime);
            if(recordEntity2 != null){
                return ResultUtil.error("设备与其他任务使用时间冲突，请重新选择！");
            }
            // 存储 test_instrument_use_record 设备使用记录
            for (CheckItemInfoVo checkItemInfoVo : instrumentVo.getCheckItemInfoList()) {
                //保存设备与检测项关系
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setSidItem(checkItemInfoVo.getItemId());
                testChItemInstrumentMiddleEntity.setStartTime(startTime);
                testChItemInstrumentMiddleEntity.setIntrusmentId(instrumentVo.getId().intValue());
                testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                //保存设备使用记录
                InstrumentRecordEntity recordEntity = new InstrumentRecordEntity();
                // 记录id
                recordEntity.setId(GenID.getID());
                // 仪器id
                recordEntity.setInstrumentId(instrumentVo.getId());
                // 检测项主键
                recordEntity.setEscRelId(checkItemInfoVo.getItemId().longValue());
                // 类型：试验使用
                recordEntity.setType("试验使用");
                // 开始时间
                recordEntity.setStartTime(instrumentVo.getStartTime());
                // 温度
                recordEntity.setTemperature(instrumentVo.getEnvironmentTemperature());
                // 湿度
                recordEntity.setHumidity(instrumentVo.getAmbientHumidity());
                // 使用前状态
                recordEntity.setBeforeStatus(instrumentVo.getDeviceState());
                // 使用后状态
//                recordEntity.setAfterStatus(instrumentVo.getDeviceState());
                // 操作人
                recordEntity.setUser(instrumentVo.getUser());
                recordEntity.setTime(new Date());
                recordEntity.setTaskId(checkItemInfoVo.getTaskId());
                recordEntity.setTaskCode(instrumentVo.getTaskCode());
                // 仪器使用记录
                instrumentRecordEntityMapper.insert(recordEntity);
                //记录日志
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(" 记录id" + recordEntity.getId());
                stringBuilder1.append(" 仪器id:" + recordEntity.getInstrumentId());
                stringBuilder1.append(" 检测项主键:" + recordEntity.getEscRelId());
                stringBuilder1.append(" 类型：试验使用:" + recordEntity.getType());
                stringBuilder1.append(" 开始时间:" + recordEntity.getStartTime());
                stringBuilder1.append(" 温度:" + recordEntity.getTemperature());
                stringBuilder1.append(" 湿度:" + recordEntity.getHumidity());
                stringBuilder1.append(" 使用前状态:" + recordEntity.getBeforeStatus());
//                stringBuilder1.append(" 使用后状态:" + recordEntity.getAfterStatus());
                stringBuilder1.append(" 操作人:" + recordEntity.getUser());
                stringBuilder1.append(" 任务单id:" + recordEntity.getTaskId());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端新增仪器使用记录\n\t" + stringBuilder1.toString(), Const.TASK_TEST, true);
            }
            // 改变检测项状态 任务单状态记录开始、检测项进入开始状态。
            // 任务单集合
            Set<Long> taskIds = new HashSet<>();
            Set<Long> sampleIds = new HashSet<>();
            for (CheckItemInfoVo checkItemInfoVo : instrumentVo.getCheckItemInfoList()) {
                taskIds.add(checkItemInfoVo.getTaskId());
                Long sampleId = testDetectionDao.getSampleId(checkItemInfoVo.getItemId());
                sampleIds.add(sampleId);
            }
            // 遍历检测项 改变状态
            for (CheckItemInfoVo checkItemInfoVo : instrumentVo.getCheckItemInfoList()) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                // 检测项id
                sampleItemInstrumentEntity.setItemId(checkItemInfoVo.getItemId());
                // 检测项 开始时间更新
                sampleItemInstrumentEntity.setStartTime(instrumentVo.getStartTime());
                // 判断 test_entrusted_sample_checkitem_rel 中 start_time 是否为空
                SampleItemInstrumentEntity sampleItemInstrumentEntity1 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(checkItemInfoVo.getItemId());
                if (sampleItemInstrumentEntity1.getStartTime() == null || sampleItemInstrumentEntity1.getState() == 0 || sampleItemInstrumentEntity1.getState() == 4) {
                    // 检测项 状态 =1 检测中
                    sampleItemInstrumentEntity.setState(1);
                    //记录日志
                    StringBuilder stringBuilder1 = new StringBuilder();
                    stringBuilder1.append(" 检测项id" + sampleItemInstrumentEntity1.getItemId());
                    stringBuilder1.append(" 检测项名称:" + sampleItemInstrumentEntity1.getCheckItemName());
                    stringBuilder1.append(" 检测项状态:" + sampleItemInstrumentEntity.getState());
                    stringBuilder1.append(" 检测项开始时间:");
                    if (!StringUtils.isEmpty(sampleItemInstrumentEntity.getStartTime())) {
                        stringBuilder1.append(new Timestamp(sampleItemInstrumentEntity.getStartTime().getTime()));
                    }
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端试验检测-开始检测\n\t" + stringBuilder1.toString(), Const.TASK_TEST, true);
                    testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
                }
            }
            // 处理任务单
            if (!CollectionUtils.isEmpty(taskIds)) {
                for (Long taskId : taskIds) {
                    // 根据 任务单id  开始检测时间 判定是否为空
                    TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
                    if (taskTestEntity.getStartDetectionTime() == null) {
                        taskTestEntity.setId(taskId);
                        // 任务单状态 == 实验中
                        taskTestEntity.setState(3);
                        //任务单 开始试验时间 年月日
                        taskTestEntity.setStartDetectionTime(new Date(System.currentTimeMillis()));
                        //记录日志
                        StringBuilder stringBuilder1 = new StringBuilder();
                        stringBuilder1.append(" 任务单id" + taskTestEntity.getId());
                        stringBuilder1.append(" 任务单编号:" + taskTestEntity.getCode());
                        stringBuilder1.append(" 任务单状态:" + taskTestEntity.getState());
                        stringBuilder1.append(" 任务单开始时间:" + new Timestamp(taskTestEntity.getStartDetectionTime().getTime()));
                        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP试验检测-任务单开始检测\n\t" + stringBuilder1.toString(), Const.TASK_TEST, true);
                        taskMapper.updateTestTask(taskTestEntity);
                        // 根据任务单主键 获取委托单主键
                        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
                        if (entrustEntity != null && entrustEntity.getState() < 3) {
                            taskMapper.updateEntrustById(entrustEntity.getId(), 3);
                        }
                    }
                }
            }
            //更新样品状态为在检1
            if (!CollectionUtils.isEmpty(sampleIds)){
                for (Long sampleId:sampleIds) {
                    sampleEntityMapper.updateSampleState(sampleId.intValue(),1);
                    SampleCirculationRecord sa = new SampleCirculationRecord();
                    sa.setSampleId(sampleId.intValue());
                    sa.setStatus("1");
                    SysUserEntity userInfo = ShiroUtils.getUserInfo();
                    sa.setOperatorId(userInfo.getUserId());
                    sa.setOperatorName(userInfo.getName());
                    sa.setTime(new Date());
                    sampleEntityMapper.saveSampleCirculationRecord(sa);
                }
            }
        }
        return ResultUtil.success("开始实验成功",null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startToTestNew(InstrumentParamVo instrumentVo) {
        Long id = instrumentVo.getId();
        //校验设备任务是否开始
        int i = instrumentRecordEntityMapper.taskStatus(id);
        if(i > 0){
            return ResultUtil.error("当前设备组队任务已开始，不需要重复开始试验！");
        }
        //查询组队信息,构造所需数据
        List<TestChItemInstrumentMiddleEntity> middleEntities = Lists.newArrayList();//设备与检测项关系
        List<InstrumentRecordEntity> recordEntities = Lists.newArrayList();//设备使用记录
        List<SampleItemInstrumentEntity> entrustSampleItems = Lists.newArrayList();//委托样品检测项更新数据
        List<SampleCirculationRecord> circulationRecords = Lists.newArrayList();//样品流转信息
        Set<Long> taskIds = new HashSet<>();//任务ID
        Set<Long> sampleIds = new HashSet<>();//样品ID
        Map<Long,String> user = new HashMap<>();//样品与使用人
        List<InstrumentUseGroup> groupInfo = instrumentRecordEntityMapper.getGroupInfo(id);
        for (int j = 0; j < groupInfo.size(); j++) {
            InstrumentUseGroup group = groupInfo.get(j);
            List<Long> escRelIds = Lists.newArrayList();
            if(group.getEscRelIds().contains(",")){
                String[] split = group.getEscRelIds().split(",");
                for (int k = 0; k < split.length; k++) {
                    escRelIds.add(Long.parseLong(split[k]));
                }
            }else{
                escRelIds.add(Long.parseLong(group.getEscRelIds()));
            }
            for (int k = 0; k < escRelIds.size(); k++) {
                //构造设备与检测项关系数据
                TestChItemInstrumentMiddleEntity middleEntity = new TestChItemInstrumentMiddleEntity();
                middleEntity.setSidItem(escRelIds.get(k).intValue());
                middleEntity.setStartTime(instrumentVo.getStartTime());
                middleEntity.setIntrusmentId(instrumentVo.getId().intValue());
                middleEntities.add(middleEntity);
                //构造设备仪器使用记录
                InstrumentRecordEntity recordEntity = new InstrumentRecordEntity();
                recordEntity.setId(GenID.getID());// 记录id
                recordEntity.setInstrumentId(id);// 仪器id
                recordEntity.setEscRelId(escRelIds.get(k));// 检测项主键
                recordEntity.setType("试验使用");// 类型：试验使用
                recordEntity.setStartTime(instrumentVo.getStartTime());// 开始时间
                recordEntity.setTemperature(instrumentVo.getEnvironmentTemperature());// 温度
                recordEntity.setHumidity(instrumentVo.getAmbientHumidity());// 湿度
                recordEntity.setBeforeStatus(instrumentVo.getDeviceState());// 使用前状态
                recordEntity.setUser(group.getUser());// 操作人
                recordEntity.setTime(new Date());
                recordEntity.setTaskId(group.getTaskId());
                recordEntity.setTaskCode(group.getTaskCode());
                recordEntity.setParallel(group.getParallel());
                recordEntities.add(recordEntity);
                //处理样品数据
                Long sampleId = testDetectionDao.getSampleId(escRelIds.get(k).intValue());
                sampleIds.add(sampleId);
                user.put(sampleId,group.getUser());
                //构造委托样品检测项关系表数据
                SampleItemInstrumentEntity detail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(escRelIds.get(k).intValue());
                if (detail.getStartTime() == null || detail.getState() == 0 || detail.getState() == 4) {//更新检测项状态和开始时间
                    SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                    sampleItemInstrumentEntity.setItemId(escRelIds.get(k).intValue());
                    sampleItemInstrumentEntity.setStartTime(instrumentVo.getStartTime());
                    sampleItemInstrumentEntity.setState(1);//状态 =1 检测中
                    entrustSampleItems.add(sampleItemInstrumentEntity);
                }
            }
            //处理任务单数据
            taskIds.add(group.getTaskId());
        }
        //更新任务单状态，委托单状态
        List<TaskTestEntity> taskTestEntities = Lists.newArrayList();//所需更新任务单数据
        List<Long> entrustIds = Lists.newArrayList();//所需更新委托单数据
        for (Long taskId : taskIds) {
            //根据任务单id开始检测时间判定是否为空
            TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
            if (taskTestEntity.getStartDetectionTime() == null) {
                taskTestEntity.setId(taskId);
                taskTestEntity.setState(3);// 任务单状态 == 实验中
                taskTestEntity.setStartDetectionTime(instrumentVo.getStartTime());
                taskTestEntities.add(taskTestEntity);
                //根据任务单主键获取委托单主键
                EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
                if (entrustEntity != null && entrustEntity.getState() < 3) {
                    entrustIds.add(entrustEntity.getId());
                }
            }
        }
        //更新样品状态，样品流转信息
        List<Long> updateSampleIds = Lists.newArrayList();//要更新样品状态的数据
        for (Long sampleId:sampleIds) {
            SampleEntity sampleEntity = sampleEntityMapper.selectState(sampleId.intValue());
            if(sampleEntity.getState().equals(0)){
                updateSampleIds.add(sampleId);
                //如果更新样品状态，也需要更新样品流转信息
                SampleCirculationRecord sa = new SampleCirculationRecord();
                sa.setSampleId(sampleId.intValue());
                sa.setStatus("1");
                String userName = user.get(sampleId);
                sa.setOperatorName(userName);
                SysUserEntity userIdByName = sysUserDao.getUserIdByName(userName);
                sa.setOperatorId(userIdByName.getUserId());
                sa.setTime(instrumentVo.getStartTime());
                circulationRecords.add(sa);
            }
        }
        //新增设备与检测项关系
        testDetectionDao.batchAddItemInstrumentMiddleRel(middleEntities);
        //新增设备使用记录
        instrumentRecordEntityMapper.batchInsert(recordEntities);
        //更新委托样品检测项状态
        if(!CollectionUtils.isEmpty(entrustSampleItems)){
            testDetectionDao.batchUpdateSampleItemInstrumentEntity(entrustSampleItems);
        }
        //更新任务单状态
        if (!CollectionUtils.isEmpty(taskTestEntities)) {
            taskMapper.batchUpdateTestTaskState(taskTestEntities);
        }
        //更新委托单状态
        if (!CollectionUtils.isEmpty(entrustIds)) {
            taskMapper.batchUpdateEntrustById(entrustIds, 3);
        }
        //更新样品状态
        if (!CollectionUtils.isEmpty(updateSampleIds)) {
            sampleEntityMapper.batchUpdateSampleState(updateSampleIds, 1);
        }
        //新增样品流转信息
        if (!CollectionUtils.isEmpty(circulationRecords)) {
            sampleEntityMapper.batchSaveSampleCirculationRecord(circulationRecords);
        }
        // 批量更新任务单操作时间
        if (CollectionUtil.isNotEmpty(taskIds)) {
            taskMapper.bathUpdateTaskUpdateTime(taskIds);
        }
        //更新队伍试验状态
        instrumentRecordEntityMapper.updateGroupState(id);
        return ResultUtil.success("开始实验成功！", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startToTestNewNo(InstrumentParamVo instrumentParamVo) {
        List<InstrumentVo> instrumentVoList = instrumentParamVo.getInstrumentVoList();
        Date startTime = instrumentVoList.get(0).getStartTime();
        //查询组队信息,构造所需数据
        List<TestChItemInstrumentMiddleEntity> middleEntities = Lists.newArrayList();//设备与检测项关系
        List<SampleItemInstrumentEntity> entrustSampleItems = Lists.newArrayList();//委托样品检测项更新数据
        List<SampleCirculationRecord> circulationRecords = Lists.newArrayList();//样品流转信息
        Set<Long> taskIds = new HashSet<>();//任务ID
        Set<Long> sampleIds = new HashSet<>();//样品ID
        Map<Long,String> user = new HashMap<>();//样品与使用人
        for (int i = 0; i < instrumentVoList.size(); i++) {
            InstrumentVo instrumentVo = instrumentVoList.get(i);
            InstrumentUseGroup group = new InstrumentUseGroup();
            group.setInstrumentId(instrumentVo.getId());
            group.setUser(instrumentVo.getUser());
            group.setTaskId(instrumentVo.getTaskId());
            group.setTaskCode(instrumentVo.getTaskCode());
            group.setParallel(instrumentVo.getSampleSize());
            List<CheckItemInfoVo> checkItemInfoList = instrumentVo.getCheckItemInfoList();
            StringBuilder escRelIdsStr = new StringBuilder();
            for (int j = 0; j < checkItemInfoList.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfoList.get(j);
                escRelIdsStr.append(checkItemInfoVo.getItemId());
                if(j != checkItemInfoList.size() -1){
                    escRelIdsStr.append(",");
                }
            }
            group.setEscRelIds(escRelIdsStr.toString());

            List<Long> escRelIds = Lists.newArrayList();
            if (group.getEscRelIds().contains(",")) {
                String[] split = group.getEscRelIds().split(",");
                for (int k = 0; k < split.length; k++) {
                    escRelIds.add(Long.parseLong(split[k]));
                }
            } else {
                escRelIds.add(Long.parseLong(group.getEscRelIds()));
            }
            for (int k = 0; k < escRelIds.size(); k++) {
                //构造设备与检测项关系数据
                TestChItemInstrumentMiddleEntity middleEntity = new TestChItemInstrumentMiddleEntity();
                middleEntity.setSidItem(escRelIds.get(k).intValue());
                middleEntity.setStartTime(instrumentVo.getStartTime());
                middleEntity.setEndTime(instrumentVo.getStartTime());//只记录
                middleEntity.setIntrusmentId(instrumentVo.getId().intValue());
                middleEntities.add(middleEntity);
                //处理样品数据
                Long sampleId = testDetectionDao.getSampleId(escRelIds.get(k).intValue());
                sampleIds.add(sampleId);
                user.put(sampleId, group.getUser());
                //构造委托样品检测项关系表数据
                SampleItemInstrumentEntity detail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(escRelIds.get(k).intValue());
                if (detail.getStartTime() == null || detail.getState() == 0 || detail.getState() == 4) {//更新检测项状态和开始时间
                    SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                    sampleItemInstrumentEntity.setItemId(escRelIds.get(k).intValue());
                    sampleItemInstrumentEntity.setStartTime(instrumentVo.getStartTime());
                    sampleItemInstrumentEntity.setState(1);//状态 =1 检测中
                    entrustSampleItems.add(sampleItemInstrumentEntity);
                }
            }
            //处理任务单数据
            taskIds.add(group.getTaskId());
        }



        //更新任务单状态，委托单状态
        List<TaskTestEntity> taskTestEntities = Lists.newArrayList();//所需更新任务单数据
        List<Long> entrustIds = Lists.newArrayList();//所需更新委托单数据
        for (Long taskId : taskIds) {
            //根据任务单id开始检测时间判定是否为空
            TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
            if (taskTestEntity.getStartDetectionTime() == null) {
                taskTestEntity.setId(taskId);
                taskTestEntity.setState(3);// 任务单状态 == 实验中
                taskTestEntity.setStartDetectionTime(startTime);
                taskTestEntities.add(taskTestEntity);
                //根据任务单主键获取委托单主键
                EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
                if (entrustEntity != null && entrustEntity.getState() < 3) {
                    entrustIds.add(entrustEntity.getId());
                }
            }
        }
        //更新样品状态，样品流转信息
        List<Long> updateSampleIds = Lists.newArrayList();//要更新样品状态的数据
        for (Long sampleId:sampleIds) {
            SampleEntity sampleEntity = sampleEntityMapper.selectState(sampleId.intValue());
            if(sampleEntity.getState().equals(0)){
                updateSampleIds.add(sampleId);
                //如果更新样品状态，也需要更新样品流转信息
                SampleCirculationRecord sa = new SampleCirculationRecord();
                sa.setSampleId(sampleId.intValue());
                sa.setStatus("1");
                String userName = user.get(sampleId);
                sa.setOperatorName(userName);
                SysUserEntity userIdByName = sysUserDao.getUserIdByName(userName);
                sa.setOperatorId(userIdByName.getUserId());
                sa.setTime(startTime);
                circulationRecords.add(sa);
            }
        }
        //新增设备与检测项关系
        testDetectionDao.batchAddItemInstrumentMiddleRel(middleEntities);
        //更新委托样品检测项状态
        if(!CollectionUtils.isEmpty(entrustSampleItems)){
            testDetectionDao.batchUpdateSampleItemInstrumentEntity(entrustSampleItems);
        }
        //更新任务单状态
        if(!CollectionUtils.isEmpty(taskTestEntities)){
            taskMapper.batchUpdateTestTaskState(taskTestEntities);
        }
        //更新委托单状态
        if(!CollectionUtils.isEmpty(entrustIds)){
            taskMapper.batchUpdateEntrustById(entrustIds, 3);
        }
        //更新样品状态
        if (!CollectionUtils.isEmpty(updateSampleIds)) {
            sampleEntityMapper.batchUpdateSampleState(updateSampleIds, 1);
        }
        //新增样品流转信息
        if (!CollectionUtils.isEmpty(circulationRecords)) {
            sampleEntityMapper.batchSaveSampleCirculationRecord(circulationRecords);
        }
        // 批量更新任务单操作时间
        if (CollectionUtil.isNotEmpty(taskIds)) {
            taskMapper.bathUpdateTaskUpdateTime(taskIds);
        }
        return ResultUtil.success("开始实验成功！", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startToTestNewInsert(InstrumentParamVo instrumentParamVo) {
        List<InstrumentVo> instrumentVoList = instrumentParamVo.getInstrumentVoList();
        Date startTime = instrumentVoList.get(0).getStartTime();
        //查询组队信息,构造所需数据
        List<TestChItemInstrumentMiddleEntity> middleEntities = Lists.newArrayList();//设备与检测项关系
        List<InstrumentRecordEntity> recordEntities = Lists.newArrayList();//设备使用记录
        List<SampleItemInstrumentEntity> entrustSampleItems = Lists.newArrayList();//委托样品检测项更新数据
        List<SampleCirculationRecord> circulationRecords = Lists.newArrayList();//样品流转信息
        Set<Long> taskIds = new HashSet<>();//任务ID
        Set<Long> sampleIds = new HashSet<>();//样品ID
        Map<Long,String> user = new HashMap<>();//样品与使用人
        //更新任务单状态，委托单状态
        List<TaskTestEntity> taskTestEntities = Lists.newArrayList();//所需更新任务单数据
        List<Long> entrustIds = Lists.newArrayList();//所需更新委托单数据
        for (int i = 0; i < instrumentVoList.size(); i++) {
            InstrumentVo instrumentVo = instrumentVoList.get(i);
            InstrumentRecordEntity recordInfo = instrumentRecordEntityMapper.getRecordInfo(instrumentVo);
            InstrumentUseGroup group = new InstrumentUseGroup();
            group.setInstrumentId(instrumentVo.getId());
            group.setUser(instrumentVo.getUser());
            group.setTaskId(instrumentVo.getTaskId());
            group.setTaskCode(instrumentVo.getTaskCode());
            group.setParallel(instrumentVo.getSampleSize());
            List<CheckItemInfoVo> checkItemInfoList = instrumentVo.getCheckItemInfoList();
            StringBuilder escRelIdsStr = new StringBuilder();
            for (int j = 0; j < checkItemInfoList.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfoList.get(j);
                escRelIdsStr.append(checkItemInfoVo.getItemId());
                if(j != checkItemInfoList.size() -1){
                    escRelIdsStr.append(",");
                }
            }
            group.setEscRelIds(escRelIdsStr.toString());
            List<Long> escRelIds = Lists.newArrayList();
            if(group.getEscRelIds().contains(",")){
                String[] split = group.getEscRelIds().split(",");
                for (int k = 0; k < split.length; k++) {
                    escRelIds.add(Long.parseLong(split[k]));
                }
            }else{
                escRelIds.add(Long.parseLong(group.getEscRelIds()));
            }
            for (int k = 0; k < escRelIds.size(); k++) {
                //构造设备与检测项关系数据
                TestChItemInstrumentMiddleEntity middleEntity = new TestChItemInstrumentMiddleEntity();
                middleEntity.setSidItem(escRelIds.get(k).intValue());
                middleEntity.setStartTime(instrumentVo.getStartTime());
                middleEntity.setEndTime(instrumentVo.getEndTime());
                middleEntity.setIntrusmentId(instrumentVo.getId().intValue());
                middleEntities.add(middleEntity);
                //构造设备仪器使用记录
                InstrumentRecordEntity recordEntity = new InstrumentRecordEntity();
                recordEntity.setId(GenID.getID());// 记录id
//                recordEntity.setInstrumentId(recordInfo.getInstrumentId());// 仪器id
                recordEntity.setInstrumentId(instrumentVo.getId());// 仪器id
                recordEntity.setEscRelId(escRelIds.get(k));// 检测项主键
                recordEntity.setType("试验使用");// 类型：试验使用
                if(instrumentParamVo.getInsertType().equals(0)){
                    recordEntity.setStartTime(recordInfo.getStartTime());// 开始时间
                    recordEntity.setEndTime(recordInfo.getEndTime());
                    recordEntity.setTemperature(recordInfo.getTemperature());// 温度
                    recordEntity.setHumidity(recordInfo.getHumidity());// 湿度
                    recordEntity.setBeforeStatus(recordInfo.getBeforeStatus());// 使用前状态
                    recordEntity.setAfterStatus(recordInfo.getAfterStatus());// 使用前状态
                }else{
                    recordEntity.setStartTime(instrumentVo.getStartTime());// 开始时间
                    recordEntity.setEndTime(instrumentVo.getEndTime());
                    recordEntity.setTemperature(instrumentVo.getEnvironmentTemperature());// 温度
                    recordEntity.setHumidity(instrumentVo.getAmbientHumidity());// 湿度
                    recordEntity.setBeforeStatus(instrumentVo.getDeviceState());// 使用前状态
                    recordEntity.setAfterStatus(instrumentVo.getDeviceState());// 使用前状态
                }
                recordEntity.setUser(group.getUser());// 操作人
                recordEntity.setTime(startTime);
                recordEntity.setTaskId(group.getTaskId());
                recordEntity.setTaskCode(group.getTaskCode());
                recordEntity.setParallel(group.getParallel());
                recordEntities.add(recordEntity);
                //处理样品数据
                Long sampleId = testDetectionDao.getSampleId(escRelIds.get(k).intValue());
                sampleIds.add(sampleId);
                user.put(sampleId,group.getUser());
                //构造委托样品检测项关系表数据
                SampleItemInstrumentEntity detail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(escRelIds.get(k).intValue());
                if (detail.getStartTime() == null || detail.getState() == 0 || detail.getState() == 4) {//更新检测项状态和开始时间
                    SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                    sampleItemInstrumentEntity.setItemId(escRelIds.get(k).intValue());
                    sampleItemInstrumentEntity.setStartTime(instrumentVo.getStartTime());
                    sampleItemInstrumentEntity.setState(1);//状态 =1 检测中
                    entrustSampleItems.add(sampleItemInstrumentEntity);
                }
            }
            //处理任务单数据
            taskIds.add(group.getTaskId());
            for (Long taskId : taskIds) {
                //根据任务单id开始检测时间判定是否为空
                TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(taskId);
                if (taskTestEntity.getStartDetectionTime() == null) {
                    taskTestEntity.setId(taskId);
                    taskTestEntity.setState(3);// 任务单状态 == 实验中
                    taskTestEntity.setStartDetectionTime(instrumentVo.getStartTime());
                    taskTestEntities.add(taskTestEntity);
                    //根据任务单主键获取委托单主键
                    EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
                    if (entrustEntity != null && entrustEntity.getState() < 3) {
                        entrustIds.add(entrustEntity.getId());
                    }
                }
            }
        }
        //更新样品状态，样品流转信息
        List<Long> updateSampleIds = Lists.newArrayList();//要更新样品状态的数据
        for (Long sampleId:sampleIds) {
            SampleEntity sampleEntity = sampleEntityMapper.selectState(sampleId.intValue());
            if(sampleEntity.getState().equals(0)){
                updateSampleIds.add(sampleId);
                //如果更新样品状态，也需要更新样品流转信息
                SampleCirculationRecord sa = new SampleCirculationRecord();
                sa.setSampleId(sampleId.intValue());
                sa.setStatus("1");
                String userName = user.get(sampleId);
                sa.setOperatorName(userName);
                SysUserEntity userIdByName = sysUserDao.getUserIdByName(userName);
                sa.setOperatorId(userIdByName.getUserId());
                sa.setTime(startTime);
                circulationRecords.add(sa);
            }
        }
        //新增设备与检测项关系
        testDetectionDao.batchAddItemInstrumentMiddleRel(middleEntities);
        //新增设备使用记录
        instrumentRecordEntityMapper.batchInsert(recordEntities);
        //更新委托样品检测项状态
        if(!CollectionUtils.isEmpty(entrustSampleItems)){
            testDetectionDao.batchUpdateSampleItemInstrumentEntity(entrustSampleItems);
        }
        //更新任务单状态
        if(!CollectionUtils.isEmpty(taskTestEntities)){
            taskMapper.batchUpdateTestTaskState(taskTestEntities);
        }
        //更新委托单状态
        if(!CollectionUtils.isEmpty(entrustIds)){
            taskMapper.batchUpdateEntrustById(entrustIds, 3);
        }
        //更新样品状态
        if (!CollectionUtils.isEmpty(updateSampleIds)) {
            sampleEntityMapper.batchUpdateSampleState(updateSampleIds, 1);
        }
        //新增样品流转信息
        if (!CollectionUtils.isEmpty(circulationRecords)) {
            sampleEntityMapper.batchSaveSampleCirculationRecord(circulationRecords);
        }
        // 批量更新任务单操作时间
        if (CollectionUtil.isNotEmpty(taskIds)) {
            taskMapper.bathUpdateTaskUpdateTime(taskIds);
        }
        return ResultUtil.success("开始实验成功！", null);
    }

    @Override
    public Result createGroup(InstrumentParamVo instrumentVos) {
        //校验设备的状态
        List<InstrumentVo> instrumentVoList = instrumentVos.getInstrumentVoList();
        if(CollectionUtils.isEmpty(instrumentVoList)){
            return ResultUtil.error("请选择任务！");
        }
        Long instrumentId = instrumentVoList.get(0).getId();
        int state = instrumentRecordEntityMapper.taskStatus(instrumentId);
        if(state > 0){
            return ResultUtil.error("设备已经开始试验，组队失败！");
        }
        //校验加入组队的任务样品数量
        Integer parallel = instrumentVoList.get(0).getParallel();
        Integer sampleSize = 0;
        List<InstrumentUseGroup> groupList = Lists.newArrayList();
        for (int i = 0; i < instrumentVoList.size(); i++) {
            InstrumentVo instrumentVo = instrumentVoList.get(i);
            sampleSize = sampleSize + instrumentVo.getSampleSize();
            //构造组队信息
            InstrumentUseGroup group = new InstrumentUseGroup();
            group.setInstrumentId(instrumentVo.getId());
            group.setUser(instrumentVo.getUser());
            group.setTaskId(instrumentVo.getTaskId());
            group.setTaskCode(instrumentVo.getTaskCode());
            group.setState(0);//等待开始
            group.setParallel(instrumentVo.getSampleSize());
            List<CheckItemInfoVo> checkItemInfoList = instrumentVo.getCheckItemInfoList();
            if(CollectionUtils.isEmpty(checkItemInfoList)){
                return ResultUtil.error("任务单【"+instrumentVo.getTaskCode()+"】没有选择检测项，请选择检测项后加入队列！");
            }
            StringBuilder escRelIdsStr = new StringBuilder();
            for (int j = 0; j < checkItemInfoList.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfoList.get(j);
                escRelIdsStr.append(checkItemInfoVo.getItemId());
                if(j != checkItemInfoList.size() -1){
                    escRelIdsStr.append(",");
                }
            }
            group.setEscRelIds(escRelIdsStr.toString());
            groupList.add(group);
        }
        Integer useSize = instrumentRecordEntityMapper.useSize(instrumentId);
        int useSize1 = useSize == null ? 0 : useSize;
        //加入组队，校验并行数
        if(parallel - useSize1 - sampleSize < 0){//最大并行数 - 已用数 - 要用数 不能小于0
            return ResultUtil.error("超出设备最大并行数量，组队失败！");
        }
        List<InstrumentUseGroup> insertGroupList = Lists.newArrayList();
        List<InstrumentUseGroup> updateGroupList = Lists.newArrayList();
        for (int i = 0; i < groupList.size(); i++) {
            InstrumentUseGroup group = groupList.get(i);
            InstrumentUseGroup groupInfoDetail = instrumentRecordEntityMapper.getGroupInfoDetail(group);
            if(groupInfoDetail == null){
                insertGroupList.add(group);
            }else{
                updateGroupList.add(groupInfoDetail);
            }
        }
        //更新
        if(!CollectionUtils.isEmpty(updateGroupList)){
            instrumentRecordEntityMapper.batchUpdateGroup(updateGroupList);
        }
        if(!CollectionUtils.isEmpty(insertGroupList)){
            instrumentRecordEntityMapper.batchInsertGroup(insertGroupList);
        }
        return ResultUtil.success("组队成功！",null);
    }

    @Override
    public Result deleteGroup(InstrumentUseGroup group) {
        if (group.getTaskId() == null) {
            return ResultUtil.error("请选择要退出的任务单！");
        }
        if (group.getInstrumentId() == null) {
            return ResultUtil.error("设备ID不能为空！");
        }
        //校验队伍是否开始试验
        if (group.getState().equals(1)) {
            return ResultUtil.error("队伍已经开始试验，无法退出队伍！");
        }
        instrumentRecordEntityMapper.deleteGroup(group);
        return ResultUtil.success("退出队伍成功！",null);
    }

    /**
     * 结束试验
     *
     * @param instrumentVo 数据源
     * @param type         类型 （结束试验的话 type = 1、点击提交复核 type =2）
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result endToTest(InstrumentVo instrumentVo, Integer type) {
        //结束试验的话 列1： 改变endTime 时间。
        //点击提交复核 列2： state = 2 待复核。
        Integer state = null;
        if (type == 2) {
            state = 2;
        }
        //校验设备使用开始时间结束时间是否冲突
        DeviceUseTimeVo vo = new DeviceUseTimeVo();
        vo.setDeviceId(instrumentVo.getId());
        vo.setEndTime(instrumentVo.getEndTime());
        vo.setStartTime(instrumentVo.getStartTime());
        List<InstrumentRecordEntity> instrumentRecordEntities = instrumentRecordEntityMapper.checkTime(vo);
        if(!org.apache.commons.collections.CollectionUtils.isEmpty(instrumentRecordEntities)){
            return ResultUtil.error("选择的结束时间与设备在其他任务中使用的时间冲突，请重新选择！");
        }
        // 遍历 设备使用记录id
        if (!CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            for (InstrumentRecordListVo instrumentRecordListVo : instrumentVo.getInstrumentRecordListVos()) {
                //更新设备使用结束时间
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setEndTime(instrumentVo.getEndTime());
                testChItemInstrumentMiddleEntity.setSidItem(instrumentRecordListVo.getEscRelId().intValue());
                testDetectionDao.updateItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);

                // 更新仪器使用记录
                InstrumentRecordEntity instrumentRecordEntity = new InstrumentRecordEntity();
                // 仪器使用记录id
                instrumentRecordEntity.setId(instrumentRecordListVo.getRecordId());
                // 仪器使用记录结束时间
                instrumentRecordEntity.setEndTime(instrumentVo.getEndTime());
                instrumentRecordEntity.setAfterStatus(instrumentVo.getDeviceState());
                instrumentRecordEntityMapper.updateByPrimaryKeySelective(instrumentRecordEntity);

                //记录日志
                StringBuilder stringBuilder10 = new StringBuilder();
                stringBuilder10.append(" 仪器使用记录id：" + instrumentRecordEntity.getId());
                stringBuilder10.append(" 仪器使用记录结束时间：" + instrumentRecordEntity.getEndTime());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端更新使用记录id\n\t" + stringBuilder10.toString(), Const.TASK_TEST, true);
            }
            // 通过仪器记录id 集合 获取 数据源。
            // 仪器id
            Set<Long> instrumentIds = new HashSet<>();
            // 检测项id
//            Set<Long> itemIds = new HashSet<>();
//            // taskId
//            Set<Long> taskIds = new HashSet<>();
            // 通过记录id集合 获取所属 记录id、仪器id、检测项id、taskId
//            List<InstrumentAppVo> ids = instrumentRecordEntityMapper.getIds(instrumentVo.getInstrumentRecordListVos());
//            if (!CollectionUtils.isEmpty(ids)) {
//                for (InstrumentAppVo data : ids) {
//                    // 仪器id
//                    instrumentIds.add(data.getId());
//                    // 检测项id
//                    itemIds.add(data.getEscRelId());
//                    // taskId
//                    taskIds.add(data.getTaskId());
//                }
//            }
            // 更新仪器状态
            for (Long id : instrumentIds) {
                InstrumentAppVo instrumentAppVo = new InstrumentAppVo();
                instrumentAppVo.setId(id);
                instrumentAppVo.setDeviceState(instrumentVo.getDeviceState());
                testInstrumentDao.updateInstrument(instrumentAppVo);

                //记录日志
                StringBuilder stringBuilder11 = new StringBuilder();
                stringBuilder11.append(" 仪器id：" + instrumentAppVo.getId());
                stringBuilder11.append(" 仪器状态：" + instrumentAppVo.getDeviceState());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端更新仪器状态\n\t" + stringBuilder11.toString(), Const.TASK_TEST, true);
            }
            // 更新检测项结束时间
//            for (Long id : itemIds) {
//                SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
//                sampleItemInstrumentEntity.setItemId(id.intValue());
//                sampleItemInstrumentEntity.setEndTime(instrumentVo.getEndTime());
//                sampleItemInstrumentEntity.setState(state);
//                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
//
//                //记录日志
//                StringBuilder stringBuilder13 = new StringBuilder();
//                stringBuilder13.append(" 检测项id：" + sampleItemInstrumentEntity.getId());
//                stringBuilder13.append(" 检测项状态：" + sampleItemInstrumentEntity.getState());
//                stringBuilder13.append(" 检测项结束时间：" + sampleItemInstrumentEntity.getEndTime());
//                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端更新检测项\n\t" + stringBuilder13.toString(), Const.TASK_TEST, true);
//            }
            // 通过任务单id 查询检测项
//            for (Long taskId : taskIds) {
//                Boolean status = true;
//                // 通过任务单id 获取所属检测项列表
//                List<CheckItemInfoVo> itemList = taskMapper.getEntrustItemVos(taskId);
//                if (!CollectionUtils.isEmpty(itemList)) {
//                    for (CheckItemInfoVo checkItemInfoVo : itemList) {
//                        // 检测项未 全部开检 则任务单无法结束试验
//                        if (checkItemInfoVo.getState() != null && checkItemInfoVo.getState() < 2) {
//                            status = false;
//                        }
//                    }
//                }
//                // if status = true 更新任务单为结束。
//                if (status) {
//                    // 更新任务单状态
//                    TaskTestEntity taskTestEntity = new TaskTestEntity();
//                    taskTestEntity.setId(taskId);
//                    // 任务单 == 4 试验完成
//                    taskTestEntity.setState(4);
//                    taskTestEntity.setEndDetectionTime(new Date(System.currentTimeMillis()));
//                    //记录日志
//                    StringBuilder stringBuilder1 = new StringBuilder();
//                    stringBuilder1.append(" 任务单id：" + taskTestEntity.getId());
//                    stringBuilder1.append(" 任务单状态：" + taskTestEntity.getState());
//                    stringBuilder1.append(" 任务结束时间：");
//                    if (!StringUtils.isEmpty(taskTestEntity.getEndDetectionTime())) {
//                        stringBuilder1.append(new Timestamp(taskTestEntity.getEndDetectionTime().getTime()));
//                    }
//                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端试验检测-任务单结束试验\n\t" + stringBuilder1.toString(), Const.TASK_TEST, true);
//                    taskMapper.updateTestTask(taskTestEntity);
//                }
//            }
        }
        return ResultUtil.success("结束试验",null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result endToTestNew(InstrumentVo instrumentVo) {
        //校验设备使用开始时间结束时间是否冲突
        DeviceUseTimeVo vo = new DeviceUseTimeVo();
        vo.setDeviceId(instrumentVo.getId());
        vo.setEndTime(instrumentVo.getEndTime());
        vo.setStartTime(instrumentVo.getStartTime());
        List<InstrumentRecordEntity> instrumentRecordEntities = instrumentRecordEntityMapper.checkTime(vo);
        if(!org.apache.commons.collections.CollectionUtils.isEmpty(instrumentRecordEntities)){
            return ResultUtil.error("选择的结束时间与设备在其他任务中使用的时间冲突，请重新选择！");
        }
        //更新设备与检测项关系中的结束时间
        List<Long> escRelIds = Lists.newArrayList();
        List<InstrumentUseGroup> groupInfo = instrumentRecordEntityMapper.getGroupInfo(instrumentVo.getId());
        for (InstrumentUseGroup group : groupInfo) {
            String escRelIdStr = group.getEscRelIds();
            if (escRelIdStr.contains(",")) {
                String[] split = escRelIdStr.split(",");
                for (String s : split) {
                    escRelIds.add(Long.parseLong(s));
                }
            } else {
                escRelIds.add(Long.parseLong(escRelIdStr));
            }
        }
        testDetectionDao.updateItemInstrumentMiddleRelEnd(escRelIds,instrumentVo.getId(),instrumentVo.getEndTime());
        //更新设备使用记录结束时间，设备状态
        instrumentRecordEntityMapper.updateRecordEndTime(escRelIds,instrumentVo.getId(),instrumentVo.getEndTime(),instrumentVo.getDeviceState());
        //删除组队信息
        instrumentRecordEntityMapper.deleteGroupByInstrumentId(instrumentVo.getId());
        return ResultUtil.success("结束试验",null);
    }

    @Override
    public InstrumentAppVo InstrumentDetails(Long id) {
        InstrumentAppVo InstrumentDetails = testInstrumentDao.selectDetails(id);
        InstrumentRecordParamVo paramVo = new InstrumentRecordParamVo();
        paramVo.setInstrumentId(id);
        // 使用记录
        List<InstrumentRecordListVo> instrumentRecord = instrumentRecordEntityMapper.getInstrumentRecord(paramVo);
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(instrumentRecord)) {
            map.put("usageRecord", new HashMap<>());
        } else {
            map.put("usageRecord", instrumentRecord);
        }
        map.put("maintenanceRecord", new HashMap<>());
        map.put("maintenanceLog", new HashMap<>());
        InstrumentDetails.setRecordFile(map);
        // 通过设备id 查询仪器记录
        return InstrumentDetails;
    }

    @Override
    public InstrumentAppVo getDetailsNew(Long id) {
        InstrumentAppVo InstrumentDetails = testInstrumentDao.selectDetails(id);
        InstrumentRecordParamVo paramVo = new InstrumentRecordParamVo();
        paramVo.setInstrumentId(id);
        // 使用记录
        List<InstrumentRecordListVo> instrumentRecord = instrumentRecordEntityMapper.getInstrumentRecord(paramVo);
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(instrumentRecord)) {
            map.put("usageRecord", new HashMap<>());
        } else {
            map.put("usageRecord", instrumentRecord);
        }
        map.put("maintenanceRecord", new HashMap<>());
        map.put("maintenanceLog", new HashMap<>());
        InstrumentDetails.setRecordFile(map);
        // 通过设备id 查询仪器记录
        //设备组队信息
        List<InstrumentUseGroup> groupInfo = instrumentRecordEntityMapper.getGroupInfo(id);
        InstrumentDetails.setGroupInfo(groupInfo);
        return InstrumentDetails;
    }

    @Override
    public List<InstrumentRecordVo> getInstrumentUseTime(InstrumentVo instrumentVo) {
        return instrumentRecordEntityMapper.getInstrumentUseTime(instrumentVo);
    }

    @Override
    public InstrumentAppVo getRecordDetails(Long id) {
        // 查询记录id详情 、 查询仪器详情
        return instrumentRecordEntityMapper.selectRecordDetails(id);
    }
}
