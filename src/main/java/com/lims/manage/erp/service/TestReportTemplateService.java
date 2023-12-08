package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestReportTemplateVo;

import java.io.Serializable;
import java.util.List;

/**
 * (TestReportTemplate)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 16:22:10
 */
public interface TestReportTemplateService extends IService<TestReportTemplate> {
    Result addReportTemplate(TestReportTemplateVo testReportTemplate);

    Result updReportTemplate(TestReportTemplateVo testReportTemplate);

    Result delReportTemplate(List<Long> idList);

    Result getList(Serializable id);

    Result getUpdOne(Serializable id);

    String getNameById(Integer reportModelId);

    Result changeReportTemplate(TestReportTemplateVo testReportTemplate);

    PageInfo getRecords(Integer pid, Integer pageNum, Integer pageSize);
}

