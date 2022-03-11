package com.lims.manage.erp.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProductStandardFileRel;
import com.lims.manage.erp.service.TestProductStandardFileRelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 产品的判定依据(TestProductStandardFileRel)表控制层
 *
 * @author makejava
 * @since 2022-03-08 17:12:19
 */
@RestController
@RequestMapping("testProductStandardFileRel")
public class TestProductStandardFileRelController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductStandardFileRelService testProductStandardFileRelService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductStandardFileRel 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<TestProductStandardFileRel> page, TestProductStandardFileRel testProductStandardFileRel) {
        return success(this.testProductStandardFileRelService.page(page, new QueryWrapper<>(testProductStandardFileRel)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.testProductStandardFileRelService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param testProductStandardFileRel 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody TestProductStandardFileRel testProductStandardFileRel) {
        return success(this.testProductStandardFileRelService.save(testProductStandardFileRel));
    }

    /**
     * 修改数据
     *
     * @param testProductStandardFileRel 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody TestProductStandardFileRel testProductStandardFileRel) {
        return success(this.testProductStandardFileRelService.updateById(testProductStandardFileRel));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.testProductStandardFileRelService.removeByIds(idList));
    }
}

