package com.lims.manage.erp.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.service.TestTechnicistService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 技术人员(TestTechnicist)表控制层
 *
 * @author makejava
 * @since 2022-02-23 09:14:41
 */
@RestController
@RequestMapping("testTechnicist")
public class TestTechnicistController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestTechnicistService testTechnicistService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testTechnicist 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<TestTechnicist> page, TestTechnicist testTechnicist) {
        return success(this.testTechnicistService.page(page, new QueryWrapper<>(testTechnicist)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.testTechnicistService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param testTechnicist 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody TestTechnicist testTechnicist) {
        return success(this.testTechnicistService.save(testTechnicist));
    }

    /**
     * 修改数据
     *
     * @param testTechnicist 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody TestTechnicist testTechnicist) {
        return success(this.testTechnicistService.updateById(testTechnicist));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.testTechnicistService.removeByIds(idList));
    }
}

