package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestReportTemplateDao;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestReportTemplateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (TestReportTemplate)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 16:22:10
 */
@Service("testReportTemplateService")
public class TestReportTemplateServiceImpl extends ServiceImpl<TestReportTemplateDao, TestReportTemplate> implements TestReportTemplateService {

    @Override
    public Result addReportTemplate(TestReportTemplate testReportTemplate) {
        if (testReportTemplate.getProductId()==null){
            return ResultUtil.error("产品ID不能为空");
        }
        testReportTemplate.setStatus("0");
        testReportTemplate.setDelFlag(0);
        testReportTemplate.setCreateTime(new Date());
        if (this.save(testReportTemplate)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updReportTemplate(TestReportTemplate testReportTemplate) {
        if (testReportTemplate.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testReportTemplate.getProductId()==null) {
            return ResultUtil.error("产品ID不能为空");
        }
        testReportTemplate.setUpdateTime(new Date());
        if (this.updateById(testReportTemplate)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delReportTemplate(List<Long> idList) {
        List<TestReportTemplate> testReportTemplates=new ArrayList<>();
        for (Long aLong : idList) {
            TestReportTemplate testReportTemplate=new TestReportTemplate();
            testReportTemplate.setUpdateTime(new Date());
            testReportTemplate.setDelFlag(1);
            testReportTemplate.setId(aLong.intValue());
            testReportTemplates.add(testReportTemplate);
        }
        if (this.updateBatchById(testReportTemplates)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }
}

