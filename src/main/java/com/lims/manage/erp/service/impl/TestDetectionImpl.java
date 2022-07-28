package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:28
 */
@Service
public class TestDetectionImpl implements TestDetectionService {
    @Autowired
    TestDetectionDao testDetectionDao;
    @Autowired
    TaskMapper taskMapper;


    @Override
    public List<TestInstrumentEntity> getTheInstrument(Integer checkItemId) {
        return testDetectionDao.selectTheInstrument(checkItemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean postStartTest(SampleItemInstrumentVo data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = format.format(data.getStartTime());
        if (stringDate.equals("1970-01-01")) {
            data.setStartTime(new Date());
        }
        if (data.getStartTime() == null) {
            data.setStartTime(new Date());
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            sampleItemInstrumentEntity.setStartTime(data.getStartTime());
            // 判断 test_entrusted_sample_checkitem_rel 中 start_time 是否为空
            SampleItemInstrumentEntity sampleItemInstrumentEntity1 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
            if (sampleItemInstrumentEntity1.getStartTime() == null || sampleItemInstrumentEntity1.getState() == 0 || sampleItemInstrumentEntity1.getState() == 4) {
                // 检测项 状态 =1 检测中
                sampleItemInstrumentEntity.setState(1);
                // 检测项 开始时间更新
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            }
        }
        // 根据 任务单id  开始检测时间 判定是否为空
        TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(data.getTaskId());
        if (taskTestEntity.getStartDetectionTime() == null) {
            taskTestEntity.setId(data.getTaskId());
            // 任务单状态 == 实验中
            taskTestEntity.setState(3);
            //任务单 开始试验时间 年月日
            taskTestEntity.setStartDetectionTime(new Date(System.currentTimeMillis()));
            taskMapper.updateTestTask(taskTestEntity);
            // 根据任务单主键 获取委托单主键
            EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
            if (entrustEntity != null && entrustEntity.getState() < 3) {
                taskMapper.updateEntrustById(entrustEntity.getId(), 3);
            }
        }
        return true;
    }

    @Override
    public Boolean postSelectInstrument(InstrumentEntity instrumentEntity) {
        // 新增前 删除存留信息
        testDetectionDao.deleteInstrument(instrumentEntity.getItemId());
        // 检测项下 仪器表 新增
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setSidItem(instrumentEntity.getItemId());
                testChItemInstrumentMiddleEntity.setStartTime(new Date());
                if (CollectionUtils.isNotEmpty(instrumentEntity.getIds())) {
                    for (Integer id : instrumentEntity.getIds()) {
                        testChItemInstrumentMiddleEntity.setIntrusmentId(id);
                        testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                    }
                }
                 return true;
    }

    @Override
    public Boolean VerifyTheLogin(Long userId, Long taskId) {
        TaskTestEntity data = taskMapper.getTaskOrders(taskId);
        if (data.getInspector() != null) {
            String[] strings2 = data.getInspector().split(",");
            for (int i = 0; i < strings2.length; i++) {
                String[] strings3 = strings2[i].split("&");
                if(userId.equals(Long.parseLong(strings3[1]))){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Boolean reviewTheLogin(Long userId, Long taskId) {
        TaskTestEntity data = taskMapper.getTaskOrders(taskId);
        if (data.getReviewer() != null) {
            String[] strings2 = data.getReviewer().split(",");
            for (int i = 0; i < strings2.length; i++) {
                String[] strings3 = strings2[i].split("&");
                if(userId.equals(Long.parseLong(strings3[1]))){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean postEndTest(SampleItemInstrumentVo data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = format.format(data.getEndTime());
        if (stringDate.equals("1970-01-01")) {
            data.setEndTime(new Date());
        }
        if (data.getEndTime() == null) {
            data.setEndTime(new Date());
        }
        // 遍历检测项 判断状态
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            // 检测项 结束时间更新
            sampleItemInstrumentEntity.setEndTime(data.getEndTime());
            // 试验完成
            sampleItemInstrumentEntity.setState(2);
            // 检测结论
            sampleItemInstrumentEntity.setResult(data.getResult());
            // 查询 当前检测项状态 文件是否上传
//            SampleItemInstrumentEntity checkItemData = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
//            if (checkItemData == null) {
//                return false;
//            }
            // 5月31日 上传原始记录先隐藏掉，也能试验完成，后面流程一样能进行。
//            if (checkItemData.getOriginUrl() == null) {
//                return false;
//            }
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            // 存放 仪器的使用记录
            // 根据检测项 主键 获取 仪器id
            List<TestChItemInstrumentMiddleEntity> getCollection = testDetectionDao.getInstrumentCollection(sampleItemInstrumentEntity.getItemId());
            if (CollectionUtils.isNotEmpty(getCollection)) {
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                // 依据检测项主键 统一 更新。
                testChItemInstrumentMiddleEntity.setEndTime(data.getEndTime());
                testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
                testDetectionDao.updateItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean JudgmentTaskDetail(TaskDetailInfoVo dataGather, Long TaskId) {
        for (SampleDetailVo sampleDetailVo : dataGather.getSampleDetailList()) {
            for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                SampleItemInstrumentEntity dataDisplay = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(checkItemInfoVo.getItemId());
               // 检测项未 全部开检 则任务单无法结束试验
                if (dataDisplay.getState() != null && dataDisplay.getState()<2) {
                    return false;
                }
            }
        }
        // 更新任务单状态
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(TaskId);
        // 任务单 == 4 试验完成
        taskTestEntity.setState(4);
        // 任务单试验完成时间 只展示年月日
//        taskTestEntity.setEndDetectionTime(new Date(DateUtil.getDayStartMs(System.currentTimeMillis())));
        taskTestEntity.setEndDetectionTime(new Date(System.currentTimeMillis()));
        taskMapper.updateTestTask(taskTestEntity);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
        if (entrustEntity != null && entrustEntity.getState() < 4) {
            taskMapper.updateEntrustById(entrustEntity.getId(), 4);
        }
        return true;
    }

    /**
     * 返回信息
     *
     * @param dataGather
     * @param TaskId
     * @param itemId
     * @return
     */
    @Override
    public Boolean getTestDetails(TaskDetailInfoVo dataGather, Long TaskId, Integer itemId) {
        SampleItemInstrumentEntity CheckItemDetail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        System.out.println(CheckItemDetail);
        return null;
    }

    @Override
    public Boolean Postreview(Integer itemId) {
        // 依据检测项 主键
        SampleItemInstrumentEntity CheckItemDetail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        if (CheckItemDetail.getState() != 2) {
            // 说明未复核
            SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
            sampleItemInstrumentEntity.setState(2);
            sampleItemInstrumentEntity.setItemId(itemId);
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            return true;
        }
        return false;
    }

}
