package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TestChItemInstrumentMiddleEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;
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
    public Boolean PostOnTest(SampleItemInstrumentVo data) {
        if (data.getStartTime() == null) {
            data.setStartTime(new Date());
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            sampleItemInstrumentEntity.setStartTime(data.getStartTime());
            // 检测项 开始时间更新
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            // 检测项下 仪器表 新增
            TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
            testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
            for (Integer id : sampleItemInstrumentEntity.getIds()) {
                testChItemInstrumentMiddleEntity.setIntrusmentId(id);
                testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
            }
            TaskTestEntity taskTestEntity = new TaskTestEntity();
            taskTestEntity.setId(data.getTaskId());
            // 任务单状态 == 实验中
            taskTestEntity.setState(3);
            taskMapper.updateTestTask(taskTestEntity);
        }
        return true;
    }
}
