package com.lims.manage.erp.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProductItemInstrumentTypeRel;
import com.lims.manage.erp.service.TestProductItemInstrumentTypeRelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 检测项使用的设备(TestProductItemInstrumentTypeRel)表控制层
 *
 * @author makejava
 * @since 2022-03-08 17:13:36
 */
@RestController
@RequestMapping("testProductItemInstrumentTypeRel")
public class TestProductItemInstrumentTypeRelController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductItemInstrumentTypeRelService testProductItemInstrumentTypeRelService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductItemInstrumentTypeRel 查询实体
     * @return 所有数据
     */
    @GetMapping
    public R selectAll(Page<TestProductItemInstrumentTypeRel> page, TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel) {
        return success(this.testProductItemInstrumentTypeRelService.page(page, new QueryWrapper<>(testProductItemInstrumentTypeRel)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public R selectOne(@PathVariable Serializable id) {
        return success(this.testProductItemInstrumentTypeRelService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param testProductItemInstrumentTypeRel 实体对象
     * @return 新增结果
     */
    @PostMapping
    public R insert(@RequestBody TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel) {
        return success(this.testProductItemInstrumentTypeRelService.save(testProductItemInstrumentTypeRel));
    }

    /**
     * 修改数据
     *
     * @param testProductItemInstrumentTypeRel 实体对象
     * @return 修改结果
     */
    @PutMapping
    public R update(@RequestBody TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel) {
        return success(this.testProductItemInstrumentTypeRelService.updateById(testProductItemInstrumentTypeRel));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public R delete(@RequestParam("idList") List<Long> idList) {
        return success(this.testProductItemInstrumentTypeRelService.removeByIds(idList));
    }
}

