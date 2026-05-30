package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestReportTemplateProductRef;
import com.lims.manage.erp.mapper.TestReportTemplateProductRefDao;
import com.lims.manage.erp.service.TestReportTemplateProductRefService;
import org.springframework.stereotype.Service;

/**
 * 产品与产品模板关联表(TestReportTemplateProductRef)表服务实现类
 *
 * @author makejava
 * @since 2022-04-13 09:49:09
 */
@Service("testReportTemplateProductRefService")
public class TestReportTemplateProductRefServiceImpl extends ServiceImpl<TestReportTemplateProductRefDao, TestReportTemplateProductRef> implements TestReportTemplateProductRefService {

}

