package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductTypeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 产品分类(TestProductType)表控制层
 *
 * @author makejava
 * @since 2022-03-02 10:03:09
 */
@RestController
@RequestMapping("testProductType")
public class TestProductTypeController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductTypeService testProductTypeService;

    @GetMapping("/getList")
    public Result getAll(TestProductType productType) {
        QueryWrapper<TestProductType> queryWrapper=new QueryWrapper<>(productType);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag",0);
        return ResultUtil.success(this.testProductTypeService.list(queryWrapper));
    }
    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductType 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestProductType> page, TestProductType testProductType) {
        QueryWrapper<TestProductType> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testProductType.getProductTypeName()!=null){
            queryWrapper.like("product_type_name",testProductType.getProductTypeName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testProductTypeService.page(page, queryWrapper));
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
            TestProductType testProductType=this.testProductTypeService.getOne(new QueryWrapper<TestProductType>().eq("product_type_id",id).eq("del_flag",0));
            return ResultUtil.success(testProductType);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testProductType 实体对象
     * @return 新增结果
     */
    @Log(title = "新增产品类别", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestBody TestProductType testProductType) {
        if (StrUtil.isEmptyIfStr(testProductType)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductTypeService.addProductType(testProductType);
    }

    /**
     * 修改数据
     *
     * @param testProductType 实体对象
     * @return 修改结果
     */
    @Log(title = "修改产品类别", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result update(@RequestBody TestProductType testProductType) {
        if (StrUtil.isEmptyIfStr(testProductType)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductTypeService.updProductType(testProductType);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "删除产品类别", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testProductTypeService.delProductType(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

