package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import com.lims.manage.erp.vo.SampleItemInstrumentVo;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:28
 */
public interface TestDetectionService {

    List<TestInstrumentEntity> getTheInstrument(Integer checkItemId);

    /**
     * 开始试验
     * @param data
     * @return
     */
    Boolean PostOnTest(SampleItemInstrumentVo data);
    /**
     * 结束试验
     */
    Boolean PostEndTest(SampleItemInstrumentVo data);
    /**
     * 实验完成-依据检测项主键 展示 所属仪器列表
     */
    List<TestInstrumentEntity> getInstrumentTestItem(Integer checkItemId);
}
