package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestReportQualifcation;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestReportQualifcationVo;

import java.util.List;

/**
 * (TestReportQualifcation)表服务接口
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
public interface TestReportQualifcationService extends IService<TestReportQualifcation> {
    Result addtestReportQualifcation(TestReportQualifcation testReportQualifcation);
    Result updtestReportQualifcation(TestReportQualifcation testReportQualifcation);
    Result deltestReportQualifcation(List<Long> idList);
    IPage<TestReportQualifcationVo> getPageList(Page<TestReportQualifcationVo> page, QueryWrapper<TestReportQualifcation> queryWrapper);

}

