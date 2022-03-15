package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestReportTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (TestReportTemplate)表控制层
 *
 * @author makejava
 * @since 2022-03-02 16:22:06
 */
@RestController
@RequestMapping("testReportTemplate")
public class TestReportTemplateController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestReportTemplateService testReportTemplateService;

    @GetMapping("/getList")
    public Result getAll(TestReportTemplate testReportTemplate) {
        QueryWrapper<TestReportTemplate> queryWrapper=new QueryWrapper<>(testReportTemplate);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag",0);
        return ResultUtil.success(this.testReportTemplateService.list(queryWrapper));
    }
    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testReportTemplate 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestReportTemplate> page, TestReportTemplate testReportTemplate) {
        QueryWrapper<TestReportTemplate> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testReportTemplate.getReportName()!=null){
            queryWrapper.like("report_name",testReportTemplate.getReportName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testReportTemplateService.page(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Serializable id) {
        if (id!=null&&id!=""){
            TestReportTemplate testMethod=this.testReportTemplateService.getOne(new QueryWrapper<TestReportTemplate>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(testMethod);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testReportTemplate 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestReportTemplate testReportTemplate) {
        if (StrUtil.isEmptyIfStr(testReportTemplate)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportTemplateService.addReportTemplate(testReportTemplate);
    }

    /**
     * 修改数据
     *
     * @param testReportTemplate 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestReportTemplate testReportTemplate) {
        if (StrUtil.isEmptyIfStr(testReportTemplate)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportTemplateService.updReportTemplate(testReportTemplate);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testReportTemplateService.delReportTemplate(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

