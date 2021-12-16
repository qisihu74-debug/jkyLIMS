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

    Boolean PostOnTest(SampleItemInstrumentVo data);
}
