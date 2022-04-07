package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.SampleDetailAddVo;

import java.util.List;

public interface TestSampleEntityService extends IService<TestSampleEntity> {
    /**
     * 批量新增样品
     * @param samples
     * @return
     */
    Integer batchInsertSample(List<SampleDetailAddVo> samples);

    /**
     * 样品查询打印列表
     * @param sampleEntity
     * @return
     */
    PageInfo querySampleList(TestSampleEntity sampleEntity);

    /**
     * 根据ID查询样品详情
     * @param id
     * @return
     */
    TestSampleEntity sampleDetail(Integer id);
}
