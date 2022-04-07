package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.SampleDetailAddVo;

import java.util.List;

public interface TestSampleEntityService extends IService<TestSampleEntity> {
    /**
     * 批量新增样品
     * @param samples
     * @return
     */
    String batchInsertSample(List<SampleDetailAddVo> samples);
}
