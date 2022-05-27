package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.TestSkillList;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTeamService;
import com.lims.manage.erp.vo.TestTeamVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 团队管理(TestTeamVo)表控制层
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


    @GetMapping("/getList")
    public Result getAll(TestTeam testTeam) {
        QueryWrapper<TestTeam> queryWrapper=new QueryWrapper<>(testTeam);
        queryWrapper.eq("del_flag",0);
        queryWrapper.orderByDesc("create_time");
        List<TestTeam> list = this.testTeamService.list(queryWrapper);
        List<Integer> pids = Lists.newArrayList();
        List<TestTeam> newList = Lists.newArrayList();
        for (TestTeam team:list) {
            pids.add(team.getPid());
        }
        //过滤id没被作为pid的数据
        for (TestTeam team:list) {
            if (!pids.contains(team.getId())){
                newList.add(team);
            }
        }
        return ResultUtil.success(newList);
    }
    /**
     * 分页查询所有数据
     *
     * @param testTeam 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询科室信息")
    public Result selectAll(Page<TestTeamVo> page,TestTeam testTeam) {
        QueryWrapper<TestTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.del_flag",0);
        if (StrUtil.isNotEmpty(testTeam.getName())){
            queryWrapper.like("t.name",testTeam.getName());
        }
        queryWrapper.orderByDesc("t.create_time");
        IPage<TestTeamVo> teamIPage = this.testTeamService.getListPage(page, queryWrapper);
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
        if (StrUtil.isEmptyIfStr(testTeam)){
            return ResultUtil.error("数据为空");
        }
        return this.testTeamService.addTestTeam(testTeam);
    }

    /**
     * 修改数据
     *
     * @param testTeam 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    @ApiOperation("修改科室信息")
    public Result update(@RequestBody TestTeam testTeam) {
        if (StrUtil.isEmptyIfStr(testTeam)){
            return ResultUtil.error("数据为空");
        }
        return this.testTeamService.updTestTeam(testTeam);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    @ApiOperation("删除科室信息")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testTeamService.delTestTeam(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

