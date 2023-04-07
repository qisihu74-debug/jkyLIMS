package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
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
    public String startToTest(InstrumentVo instrumentVo) {
        if (instrumentVo != null && !CollectionUtils.isEmpty(instrumentVo.getCheckItemInfoList())) {
            // 存储 test_instrument_use_record 设备使用记录
            for (CheckItemInfoVo checkItemInfoVo : instrumentVo.getCheckItemInfoList()) {
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
            for (CheckItemInfoVo checkItemInfoVo : instrumentVo.getCheckItemInfoList()) {
                taskIds.add(checkItemInfoVo.getTaskId());
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
        }
        return "开始实验成功";
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
    public String endToTest(InstrumentVo instrumentVo, Integer type) {
        //结束试验的话 列1： 改变endTime 时间。
        //点击提交复核 列2： state = 2 待复核。
        Integer state = null;
        if (type == 2) {
            state = 2;
        }
        // 遍历 设备使用记录id
        if (!CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            for (InstrumentRecordListVo instrumentRecordListVo : instrumentVo.getInstrumentRecordListVos()) {
                // 更新仪器使用记录
                InstrumentRecordEntity instrumentRecordEntity = new InstrumentRecordEntity();
                // 仪器使用记录id
                instrumentRecordEntity.setId(instrumentRecordListVo.getRecordId());
                // 仪器使用记录结束时间
                instrumentRecordEntity.setEndTime(instrumentVo.getEndTime());
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
            Set<Long> itemIds = new HashSet<>();
            // taskId
            Set<Long> taskIds = new HashSet<>();
            // 通过记录id集合 获取所属 记录id、仪器id、检测项id、taskId
            List<InstrumentAppVo> ids = instrumentRecordEntityMapper.getIds(instrumentVo.getInstrumentRecordListVos());
            if (!CollectionUtils.isEmpty(ids)) {
                for (InstrumentAppVo data : ids) {
                    // 仪器id
                    instrumentIds.add(data.getId());
                    // 检测项id
                    itemIds.add(data.getEscRelId());
                    // taskId
                    taskIds.add(data.getTaskId());
                }
            }
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
            for (Long id : itemIds) {
                SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
                sampleItemInstrumentEntity.setItemId(id.intValue());
                sampleItemInstrumentEntity.setEndTime(instrumentVo.getEndTime());
                sampleItemInstrumentEntity.setState(state);
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);

                //记录日志
                StringBuilder stringBuilder13 = new StringBuilder();
                stringBuilder13.append(" 检测项id：" + sampleItemInstrumentEntity.getId());
                stringBuilder13.append(" 检测项状态：" + sampleItemInstrumentEntity.getState());
                stringBuilder13.append(" 检测项结束时间：" + sampleItemInstrumentEntity.getEndTime());
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端更新检测项\n\t" + stringBuilder13.toString(), Const.TASK_TEST, true);
            }
            // 通过任务单id 查询检测项
            for (Long taskId : taskIds) {
                Boolean status = true;
                // 通过任务单id 获取所属检测项列表
                List<CheckItemInfoVo> itemList = taskMapper.getEntrustItemVos(taskId);
                if (!CollectionUtils.isEmpty(itemList)) {
                    for (CheckItemInfoVo checkItemInfoVo : itemList) {
                        // 检测项未 全部开检 则任务单无法结束试验
                        if (checkItemInfoVo.getState() != null && checkItemInfoVo.getState() < 2) {
                            status = false;
                        }
                    }
                }
                // if status = true 更新任务单为结束。
                if (status) {
                    // 更新任务单状态
                    TaskTestEntity taskTestEntity = new TaskTestEntity();
                    taskTestEntity.setId(taskId);
                    // 任务单 == 4 试验完成
                    taskTestEntity.setState(4);
                    taskTestEntity.setEndDetectionTime(new Date(System.currentTimeMillis()));
                    //记录日志
                    StringBuilder stringBuilder1 = new StringBuilder();
                    stringBuilder1.append(" 任务单id：" + taskTestEntity.getId());
                    stringBuilder1.append(" 任务单状态：" + taskTestEntity.getState());
                    stringBuilder1.append(" 任务结束时间：");
                    if (!StringUtils.isEmpty(taskTestEntity.getEndDetectionTime())) {
                        stringBuilder1.append(new Timestamp(taskTestEntity.getEndDetectionTime().getTime()));
                    }
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "APP端试验检测-任务单结束试验\n\t" + stringBuilder1.toString(), Const.TASK_TEST, true);
                    taskMapper.updateTestTask(taskTestEntity);
                }
            }
        }
        return "结束试验";
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
    public InstrumentAppVo getRecordDetails(Long id) {
        // 查询记录id详情 、 查询仪器详情
        return instrumentRecordEntityMapper.selectRecordDetails(id);
    }
}
