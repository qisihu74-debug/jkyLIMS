package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * (TestReportTemplate)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 16:22:10
 */
public interface TestReportTemplateService extends IService<TestReportTemplate> {
    Result addReportTemplate(TestReportTemplate testReportTemplate);
    Result updReportTemplate(TestReportTemplate testReportTemplate);
    Result delReportTemplate(List<Long> idList);
}

