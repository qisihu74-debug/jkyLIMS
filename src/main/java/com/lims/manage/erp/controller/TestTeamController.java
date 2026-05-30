package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTeamService;
import com.lims.manage.erp.vo.Node;
import com.lims.manage.erp.vo.TestTeamVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Api(value = "科室信息管理", tags = {"科室信息管理"})
public class TestTeamController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestTeamService testTeamService;


    @GetMapping("/getList")
    public Result getAll(TestTeam testTeam) {
        QueryWrapper<TestTeam> queryWrapper = new QueryWrapper<>(testTeam);
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByAsc("sort");
        List<TestTeam> list = this.testTeamService.list(queryWrapper);
        /*List<Integer> pids = Lists.newArrayList();
        List<TestTeam> newList = Lists.newArrayList();
        for (TestTeam team:list) {
            pids.add(team.getPid());
        }
        //过滤id没被作为pid的数据
        for (TestTeam team:list) {
            if (!pids.contains(team.getId())){
                newList.add(team);
            }
        }*/
        //如果团队非顶级团队，则团队名称展示为父级名称-本团队名称team1需要处理的数据，team所有团队数据
        for (TestTeam team1 : list) {
            for (TestTeam team : list) {
                if (team1.getPid() != 0) {
                    if (team1.getPid().equals(team.getId())) {
                        String name = team.getName() + "—" + team1.getName();
                        team1.setName(name);
                    }
                }
            }
        }
        return ResultUtil.success(list);
    }

    /**
     * 分页查询所有数据
     *
     * @param testTeam 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询科室信息")
    public Result selectAll(Page<TestTeamVo> page, TestTeam testTeam) {
        QueryWrapper<TestTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.del_flag", 0);
        if (StrUtil.isNotEmpty(testTeam.getName())) {
            queryWrapper.like("t.name", testTeam.getName());
        }
        queryWrapper.orderByDesc("t.create_time");
        IPage<TestTeamVo> teamIPage = this.testTeamService.getListPage(page, queryWrapper);
        //查询所有团队
        QueryWrapper<TestTeam> queryWrapperAll = new QueryWrapper<>(testTeam);
        queryWrapperAll.eq("del_flag", 0);
        queryWrapperAll.orderByAsc("sort");
        List<TestTeam> list = this.testTeamService.list(queryWrapperAll);
        for (TestTeamVo team1 : teamIPage.getRecords()) {
            for (TestTeam team : list) {
                if (team1.getPid() != 0) {
                    if (team1.getPid().equals(team.getId())) {
                        String name = team.getName() + "—" + team1.getName();
                        team1.setName(name);
                    }
                }
            }
        }
        return ResultUtil.success(teamIPage);
    }

    /**
     * 获取团队树形结构数据
     *
     * @return 所有数据
     */
    @GetMapping("/getTree")
    @ApiOperation("获取团队树形结构数据")
    public Result<?> getTree() {
        List<Node> teamTree = testTeamService.getTree();
        return ResultUtil.success(teamTree);
    }

    /**
     * 根据团队名称获取团队信息
     *
     * @return 所有数据
     */
    @GetMapping("/getTreeByName")
    @ApiOperation("根据团队名称获取团队信息")
    public Result<?> getTreeByName(String name) {
        return ResultUtil.success(testTeamService.getTreeByName(name));
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
    @Log(title = "新增团队", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ApiOperation("添加科室信息")
    public Result insert(@RequestBody TestTeam testTeam) {
        if (StrUtil.isEmptyIfStr(testTeam)) {
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
    @Log(title = "修改团队", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ApiOperation("修改科室信息")
    public Result update(@RequestBody TestTeam testTeam) {
        if (StrUtil.isEmptyIfStr(testTeam)) {
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
    @Log(title = "删除团队", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    @ApiOperation("删除科室信息")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testTeamService.delTestTeam(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }
}

