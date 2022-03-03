package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 团队管理(TestTeam)表控制层
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
@RestController
@RequestMapping("team")
@Api(value = "科室信息管理",tags ={"科室信息管理"})
public class TestTeamController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestTeamService testTeamService;

    /**
     * 分页查询所有数据
     *
     * @param testTeam 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询科室信息")
    public Result selectAll(TestTeam testTeam) {
        LambdaQueryWrapper<TestTeam> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotEmpty(testTeam.getName())) {
            wrapper.like(TestTeam::getName, testTeam.getName());
        }
        Page<TestTeam> page=new Page<>(testTeam.getCurrent(),testTeam.getSize());
        wrapper.eq(TestTeam::getDelFlag,0);
        wrapper.orderByDesc(TestTeam::getCreateTime);
        IPage<TestTeam> teamIPage = this.testTeamService.page(page, wrapper);
        return ResultUtil.success(teamIPage);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("根据ID查询科室信息")
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return ResultUtil.success(this.testTeamService.getById(id));
    }
    /**
     * 新增数据
     *
     * @param testTeam 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    @ApiOperation("添加科室信息")
    public Result insert(@RequestBody TestTeam testTeam) {
        LambdaQueryWrapper<TestTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestTeam::getName, testTeam.getName());
        int count = testTeamService.count(wrapper);
        if (count > 0) {
            return ResultUtil.error("团队名称已存在,不能重复");
        } else {
            return ResultUtil.success(this.testTeamService.save(testTeam));
        }

    }

    /**
     * 修改数据
     *
     * @param testTeam 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @ApiOperation("修改科室信息")
    public Result update(@RequestBody TestTeam testTeam) {
        return ResultUtil.success(this.testTeamService.updateById(testTeam));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping("/del")
    @ApiOperation("删除科室信息")
    public Result delete(@RequestParam("idList") List<Long> idList) {
        return ResultUtil.success(this.testTeamService.removeByIds(idList));
    }
}

