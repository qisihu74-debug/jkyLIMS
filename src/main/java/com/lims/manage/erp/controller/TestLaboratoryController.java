package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 实验室管理(TestLaboratory)表控制层
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
@RestController
@RequestMapping("testLaboratory")
@Api(value = "实验室管理",tags ={"实验室管理"})
public class TestLaboratoryController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestLaboratoryService testLaboratoryService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testLaboratory 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询实验室信息")
    public Result selectAll(Page<TestLaboratoryVo> page, TestLaboratoryVo testLaboratory) {
        QueryWrapper<TestLaboratory> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testLaboratory.getName()!=null){
            queryWrapper.like("name",testLaboratory.getName());
        }
        return ResultUtil.success(this.testLaboratoryService.getPageList(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @ApiOperation("根据ID查询实验室信息")
    public Result selectOne(@PathVariable Serializable id) {
        if (id!=null&&id!=""){
            return ResultUtil.success(this.testLaboratoryService.getOne(new QueryWrapper<TestLaboratory>().eq("id",id).eq("del_flag",0)));
        }else {
            return ResultUtil.error("参数为空!");
        }
    }

    /**
     * 新增数据
     *
     * @param testLaboratory 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    @ApiOperation("添加实验室信息")
    public Result insert(@RequestBody TestLaboratory testLaboratory) {
        if (StrUtil.isEmptyIfStr(testLaboratory)){
            return ResultUtil.error("数据为空");
        }
        return this.testLaboratoryService.addLaboratory(testLaboratory);
    }

    /**
     * 修改数据
     *
     * @param testLaboratory 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    @ApiOperation("修改实验室信息")
    public Result update(@RequestBody TestLaboratory testLaboratory) {
        if (StrUtil.isEmptyIfStr(testLaboratory)){
            return ResultUtil.error("数据为空");
        }
        return this.testLaboratoryService.updLaboratory(testLaboratory);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    @ApiOperation("删除实验室信息")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testLaboratoryService.delLaboratory(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

