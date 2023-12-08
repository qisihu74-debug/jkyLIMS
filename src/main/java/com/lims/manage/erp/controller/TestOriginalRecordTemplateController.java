package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;

import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestOriginalRecordTemplateService;
import com.lims.manage.erp.vo.TestReportTemplateVo;
import com.lims.manage.erp.vo.TorttpiVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 原始记录模板(TestOriginalRecordTemplate)表控制层
 *
 * @author makejava
 * @since 2022-03-16 14:12:38
 */
@RestController
@RequestMapping("testOriginalRecordTemplate")
public class TestOriginalRecordTemplateController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestOriginalRecordTemplateService testOriginalRecordTemplateService;


    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testOriginalRecordTemplate 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TorttpiVo> page, TestOriginalRecordTemplate testOriginalRecordTemplate) {
        QueryWrapper<TestOriginalRecordTemplate> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tort.del_flag",0);
        if (testOriginalRecordTemplate.getName()!=null){
            queryWrapper.like("name",testOriginalRecordTemplate.getName());
        }
        queryWrapper.orderByDesc("tort.create_time");
        IPage<TorttpiVo> pageList = this.testOriginalRecordTemplateService.getPageList(page, queryWrapper);
        for (TorttpiVo bean:pageList.getRecords()) {
            if (StringUtils.isNotEmpty(bean.getFileUrl())){
                bean.setFileUrl(bean.getFileUrl().substring(0,bean.getFileUrl().indexOf("?")));
            }
        }
        return ResultUtil.success(pageList);
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
            TestOriginalRecordTemplate testMethod=this.testOriginalRecordTemplateService.getOne(new QueryWrapper<TestOriginalRecordTemplate>().eq("id",id).eq("del_flag",0));
            testMethod.setCopyUrl(testMethod.getFileUrl());
            return ResultUtil.success(testMethod);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testOriginalRecordTemplate 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestOriginalRecordTemplate testOriginalRecordTemplate) {
        if (StrUtil.isEmptyIfStr(testOriginalRecordTemplate)){
            return ResultUtil.error("数据为空");
        }
        return this.testOriginalRecordTemplateService.addtestOriginalRecordTemplate(testOriginalRecordTemplate);


    }

    /**
     * 修改数据
     *
     * @param testOriginalRecordTemplate 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestOriginalRecordTemplate testOriginalRecordTemplate) {
        if (StrUtil.isEmptyIfStr(testOriginalRecordTemplate)){
            return ResultUtil.error("数据为空");
        }
        return this.testOriginalRecordTemplateService.updtestOriginalRecordTemplate(testOriginalRecordTemplate);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testOriginalRecordTemplateService.delTtestOriginalRecordTemplate(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }


    /**
     * 查询原始记录数据
     *
     * @return 所有数据
     */
    @GetMapping("/getAllList")
    public Result getAllList() {

        return this.testOriginalRecordTemplateService.getAllList();
    }

    /***********************************************/

    @PostMapping("/change")
    public Result change(@RequestBody TestOriginalRecordTemplate testOriginalRecordTemplate) {
        if (StrUtil.isEmptyIfStr(testOriginalRecordTemplate)){
            return ResultUtil.error("数据为空");
        }
        return this.testOriginalRecordTemplateService.changeTestOriginalRecordTemplate(testOriginalRecordTemplate);
    }

    @GetMapping("getRecordList")
    public Result getRecordList(Integer pid, Integer pageNum, Integer pageSize) {
        if (pid!=null){
            return ResultUtil.success("查询原始记录变更列表成功！",this.testOriginalRecordTemplateService.getRecords(pid,pageNum,pageSize));
        }else {
            return ResultUtil.error("查询原始记录变更列表失败！");
        }
    }

}

