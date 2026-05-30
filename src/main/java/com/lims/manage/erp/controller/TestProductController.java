package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.vo.TestProductItemVo;
import com.lims.manage.erp.vo.TestProductVo;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 产品信息(TestProduct)表控制层
 *
 * @author makejava
 * @since 2022-03-02 15:00:06
 */
@RestController
@RequestMapping("testProduct")
public class TestProductController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductService testProductService;

    @GetMapping("/getList")
    public Result getAll(TestProduct testProduct) {
        QueryWrapper<TestProduct> queryWrapper = new QueryWrapper<>(testProduct);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag", 0);
        return ResultUtil.success(this.testProductService.list(queryWrapper));
    }
//    /**
//     * 通过主键查询单条数据
//     *
//     * @param id 主键
//     * @return 单条数据
//     */
//    @GetMapping("/selProduct/{id}")
//    public Result selectProductVo(@PathVariable Serializable id) {
//        if (id!=null&&id!=""){
//            TestProduct testProduct=this.testProductService.getOne(new QueryWrapper<TestProduct>().eq("product_id",id));
//            return ResultUtil.success(this.testProductService.getTestProductSelVo(testProduct));
//        }else {
//            return ResultUtil.error("参数为空");
//        }
//    }

    /**
     * 分页查询所有数据
     *
     * @param page        分页对象
     * @param testProduct 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestProductVo> page, TestProduct testProduct) {
        QueryWrapper<TestProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("p.del_flag", 0);
        if (testProduct.getProductName() != null) {
            queryWrapper.like("p.product_name", testProduct.getProductName());
        }
        if (testProduct.getProductTypeId() != null) {
            queryWrapper.eq("p.product_type_id", testProduct.getProductTypeId());
        }
        if (testProduct.getStatus() != null) {
            queryWrapper.eq("p.status", testProduct.getStatus());
        }
        queryWrapper.orderByDesc("p.create_time");
        return ResultUtil.success(this.testProductService.getPageList(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
//    public Result selectOne(@PathVariable Serializable id) {
    public Result selectOne(@PathVariable Integer id) {
        if (id!=null){
//            TestProduct testMethod=this.testProductService.getOne(new QueryWrapper<TestProduct>().eq("product_id",id).eq("del_flag",0));
            TestProduct testMethod = testProductService.getProductInfo(id);
            return ResultUtil.success(this.testProductService.getTestProductItemVo(testMethod));
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @return 新增结果
     */
    @Log(title = "新增产品", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestBody TestProductItemVo testProductItemVo) {
        if (StrUtil.isEmptyIfStr(testProductItemVo)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductService.addTestProduct(testProductItemVo);
    }

    /**
     * 修改数据
     *
     * @return 修改结果
     */
    @Log(title = "修改产品", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result update(@RequestBody TestProductItemVo testProductItemVo) {
        if (StrUtil.isEmptyIfStr(testProductItemVo)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductService.updTestProduct(testProductItemVo);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "删除产品", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testProductService.delTestProduct(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }


    /**
     * 更新产品状态
     *
     * @return 更新产品状态
     */
    @PostMapping("/updateProductStatus")
    public Result updateProductStatus(@RequestBody TestProductItemVo testProductItemVo) {
        if (StrUtil.isEmptyIfStr(testProductItemVo)) {
            return ResultUtil.error("数据为空");
        }
        return this.testProductService.updateProductStatus(testProductItemVo);
    }
}

