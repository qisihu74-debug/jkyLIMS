package com.lims.manage.erp.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestSkillList;
import com.lims.manage.erp.service.TestSkillListService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 技术人员技能表(TestSkillList)表控制层
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
@RestController
@RequestMapping("skillList")
public class TestSkillListController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestSkillListService testSkillListService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testSkillList 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public R selectAll(Page<TestSkillList> page, TestSkillList testSkillList) {
        return success(this.testSkillListService.page(page, new QueryWrapper<>(testSkillList)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.testSkillListService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param testSkillList 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public R insert(@RequestBody TestSkillList testSkillList) {
        return success(this.testSkillListService.save(testSkillList));
    }

    /**
     * 修改数据
     *
     * @param testSkillList 实体对象
     * @return 修改结果
     */
    @PutMapping("/edit")
    public R update(@RequestBody TestSkillList testSkillList) {
        return success(this.testSkillListService.updateById(testSkillList));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.testSkillListService.removeByIds(idList));
    }
}

