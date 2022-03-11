package com.lims.manage.erp.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProductItemStandardFileRel;
import com.lims.manage.erp.service.TestProductItemStandardFileRelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 检测项的检测依据(TestProductItemStandardFileRel)表控制层
 *
 * @author makejava
 * @since 2022-03-08 17:13:05
 */
@RestController
@RequestMapping("testProductItemStandardFileRel")
public class TestProductItemStandardFileRelController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductItemStandardFileRelService testProductItemStandardFileRelService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductItemStandardFileRel 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<TestProductItemStandardFileRel> page, TestProductItemStandardFileRel testProductItemStandardFileRel) {
        return success(this.testProductItemStandardFileRelService.page(page, new QueryWrapper<>(testProductItemStandardFileRel)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.testProductItemStandardFileRelService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param testProductItemStandardFileRel 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody TestProductItemStandardFileRel testProductItemStandardFileRel) {
        return success(this.testProductItemStandardFileRelService.save(testProductItemStandardFileRel));
    }

    /**
     * 修改数据
     *
     * @param testProductItemStandardFileRel 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody TestProductItemStandardFileRel testProductItemStandardFileRel) {
        return success(this.testProductItemStandardFileRelService.updateById(testProductItemStandardFileRel));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.testProductItemStandardFileRelService.removeByIds(idList));
    }
}

