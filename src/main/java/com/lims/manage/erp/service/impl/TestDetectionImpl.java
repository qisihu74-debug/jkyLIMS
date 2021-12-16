package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TestChItemInstrumentMiddleEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
import org.flowable.common.engine.impl.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public synchronized Boolean PostOnTest(SampleItemInstrumentVo data) {
        if (data.getStartTime() == null) {
            data.setStartTime(new Date());
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            sampleItemInstrumentEntity.setStartTime(data.getStartTime());
            // 判断 test_entrusted_sample_checkitem_rel 中 start_time 是否为空
            SampleItemInstrumentEntity sampleItemInstrumentEntity1 =   testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
            if(sampleItemInstrumentEntity1.getStartTime()==null){
                // 检测项 状态 =1 检测中
                sampleItemInstrumentEntity.setState(1);
                // 检测项 开始时间更新
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
                // 检测项下 仪器表 新增
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
                testChItemInstrumentMiddleEntity.setStartTime(data.getStartTime());
                if(CollectionUtil.isNotEmpty(sampleItemInstrumentEntity.getIds())){
                    for (Integer id : sampleItemInstrumentEntity.getIds()) {
                        testChItemInstrumentMiddleEntity.setIntrusmentId(id);
                        testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                    }
                }
            }
            // 根据 任务单id  开始检测时间 判定是否为空
            TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(data.getTaskId());
            if(taskTestEntity.getStartDetectionTime()==null){
                taskTestEntity.setId(data.getTaskId());
                // 任务单状态 == 实验中
                taskTestEntity.setState(3);
                // 开始试验时间
                taskTestEntity.setStartDetectionTime(data.getStartTime());
                taskMapper.updateTestTask(taskTestEntity);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean PostEndTest(SampleItemInstrumentVo data) {
        if(data.getEndTime()==null){
            data.setEndTime(new Date());
        }
        // 遍历检测项 判断状态
        for(SampleItemInstrumentEntity sampleItemInstrumentEntity:data.getItemInstrumentEntityList()){
            SampleItemInstrumentEntity dataDisplay = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
            // 检测项未 全部开检 并且 原始记录 未上传
            if(dataDisplay.getState()==0 || dataDisplay.getOriginUrl()==null)
            {
                return false;
            }
            // 检测项 结束时间更新
            sampleItemInstrumentEntity.setEndTime(data.getEndTime());
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            // 存放 仪器的使用记录
            if(CollectionUtil.isNotEmpty(sampleItemInstrumentEntity.getIds())){
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setEndTime(data.getEndTime());
                testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
                testDetectionDao.updateItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
            }

        }
        // 更新任务单状态
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(data.getTaskId());
        // 任务单 == 4 试验完成
        taskTestEntity.setState(4);
        taskTestEntity.setEndDetectionTime(data.getEndTime());
        taskMapper.updateTestTask(taskTestEntity);
        return true;
    }

    @Override
    public List<TestInstrumentEntity> getInstrumentTestItem(Integer checkItemId) {
        return testDetectionDao.getInstrumentTestItem(checkItemId);
    }

    @Override
    public Boolean postIds(SampleItemInstrumentEntity sampleItemInstrumentEntity) {
        // 根据检测项 主键 获取 旧数据
        List<TestInstrumentEntity> dataAssemble = testDetectionDao.getInstrumentTestItem(sampleItemInstrumentEntity.getItemId());
        // 检测项 新数据
        return null;
    }
}
