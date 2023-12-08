package com.lims.manage.erp.controller;



import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestReportTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestReportTemplateService;
import com.lims.manage.erp.vo.TestReportTemplateVo;
import org.apache.commons.lang3.StringUtils;
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

    @GetMapping("/getList/{id}")
    public Result getAll(@PathVariable Serializable id) {
        return this.testReportTemplateService.getList(id);
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
        QueryWrapper<TestReportTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (testReportTemplate.getReportName() != null) {
            queryWrapper.like("report_name", testReportTemplate.getReportName());
        }
        queryWrapper.orderByDesc("create_time");
        IPage<TestReportTemplate> iPage = this.testReportTemplateService.page(page, queryWrapper);
        for (TestReportTemplate bean : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(bean.getReportFileUri())) {
                bean.setReportFileUri(bean.getReportFileUri().substring(0, bean.getReportFileUri().indexOf("?")));
                String substring = bean.getReportFileUri().substring(bean.getReportFileUri().lastIndexOf(".") + 1);
                bean.setRemark(substring);
            }
        }
        return ResultUtil.success(iPage);
    }

    /**
     * 查询所有数据
     *
     * @return 所有数据
     */
    @GetMapping("/getAllList")
    public Result getAllList() {
        LambdaQueryWrapper<TestReportTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TestReportTemplate::getDelFlag, 0);
        lambdaQueryWrapper.orderByDesc(TestReportTemplate::getCreateTime);
        List<TestReportTemplate> list = this.testReportTemplateService.list(lambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            list.stream().forEach(testReportTemplate -> testReportTemplate.setReportFileUri(null));
        }
        return ResultUtil.success(list);
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
            return this.testReportTemplateService.getUpdOne(id);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestReportTemplateVo testReportTemplateVo) {
        if (StrUtil.isEmptyIfStr(testReportTemplateVo)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportTemplateService.addReportTemplate(testReportTemplateVo);
    }

    /**
     * 修改数据
     *
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestReportTemplateVo testReportTemplateVo) {
        if (StrUtil.isEmptyIfStr(testReportTemplateVo)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportTemplateService.updReportTemplate(testReportTemplateVo);
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

    /***********************************************/

    @PostMapping("/change")
    public Result change(@RequestBody TestReportTemplateVo testReportTemplateVo) {
        if (StrUtil.isEmptyIfStr(testReportTemplateVo)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportTemplateService.changeReportTemplate(testReportTemplateVo);
    }

    @GetMapping("getRecordList")
    public Result getRecordList(Integer pid, Integer pageNum, Integer pageSize) {
        if (pid!=null){
            return ResultUtil.success("查询报告变更列表成功！",this.testReportTemplateService.getRecords(pid,pageNum,pageSize));
        }else {
            return ResultUtil.error("查询报告变更列表失败！");
        }
    }
}

