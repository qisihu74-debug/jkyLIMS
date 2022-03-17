package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestOriginalRecordTemplateDao;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestOriginalRecordTemplateService;
import com.lims.manage.erp.vo.TorttpiVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 原始记录模板(TestOriginalRecordTemplate)表服务实现类
 *
 * @author makejava
 * @since 2022-03-16 14:12:44
 */
@Service("testOriginalRecordTemplateService")
public class TestOriginalRecordTemplateServiceImpl extends ServiceImpl<TestOriginalRecordTemplateDao, TestOriginalRecordTemplate> implements TestOriginalRecordTemplateService {
    @Autowired
    private TestOriginalRecordTemplateDao testOriginalRecordTemplateDao;
    @Override
    public Result addtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate) {

        if (testOriginalRecordTemplate.getName()==null){
            return ResultUtil.error("原始模板名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestOriginalRecordTemplate>().eq("name",testOriginalRecordTemplate.getName()))!=null){
            return ResultUtil.error("原始模板名称重复");
        }
        testOriginalRecordTemplate.setStatus("0");
        testOriginalRecordTemplate.setDelFlag(0);
        testOriginalRecordTemplate.setCreateTime(new Date());
        if (this.save(testOriginalRecordTemplate)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate) {

        if (testOriginalRecordTemplate.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testOriginalRecordTemplate.getName()==null){
            return ResultUtil.error("模板名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestOriginalRecordTemplate>().eq("name",testOriginalRecordTemplate.getName()).eq("del_flag",0).ne("id",testOriginalRecordTemplate.getId()))!=null){
            return ResultUtil.error("名称重复");
        }
        testOriginalRecordTemplate.setUpdateTime(new Date());
        if (this.updateById(testOriginalRecordTemplate)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTtestOriginalRecordTemplate(List<Long> idList) {

        List<TestOriginalRecordTemplate> testOriginalRecordTemplate=new ArrayList<>();
        for (Long aLong : idList) {
            TestOriginalRecordTemplate testProductType=new TestOriginalRecordTemplate();
            testProductType.setUpdateTime(new Date());
            testProductType.setDelFlag(1);
            testProductType.setId(aLong.intValue());
            testOriginalRecordTemplate.add(testProductType);
        }
        if (this.updateBatchById(testOriginalRecordTemplate)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }

    }

    @Override
    public IPage<TorttpiVo> getPageList(Page<TorttpiVo> page, QueryWrapper<TestOriginalRecordTemplate> queryWrapper) {
        return testOriginalRecordTemplateDao.getPageList(page,queryWrapper);
    }

}

