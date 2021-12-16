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
            // 判断 test_entrusted_sample_checkitem_rel 中 start_time 是否为空
            SampleItemInstrumentEntity sampleItemInstrumentEntity1 =   testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
            if(sampleItemInstrumentEntity1.getStartTime()==null){
                // 检测项 开始时间更新
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
                // 检测项下 仪器表 新增
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
                for (Integer id : sampleItemInstrumentEntity.getIds()) {
                    testChItemInstrumentMiddleEntity.setIntrusmentId(id);
                    testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                }
            }
            // 根据 任务单id  开始检测时间 判定是否为空
            TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(data.getTaskId());
            if(taskTestEntity.getStartDetectionTime()==null){
                taskTestEntity.setId(data.getTaskId());
                // 任务单状态 == 实验中
                taskTestEntity.setState(3);
                // 开始试验时间
                java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
                taskTestEntity.setStartDetectionTime(currentDate);
                taskMapper.updateTestTask(taskTestEntity);
            }
        }
        return true;
    }
}
