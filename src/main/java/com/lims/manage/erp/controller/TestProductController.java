package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.vo.TestProductVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
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

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProduct 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestProductVo> page, TestProduct testProduct) {
        QueryWrapper<TestProduct> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("p.del_flag",0);
        if (testProduct.getProductName()!=null){
            queryWrapper.like("p.product_name",testProduct.getProductName());
        }
        if (testProduct.getProductTypeId()!=null){
            queryWrapper.eq("p.product_type_id",testProduct.getProductTypeId());
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
    public Result selectOne(@PathVariable Serializable id) {
        if (id!=null&&id!=""){
            TestProduct testMethod=this.testProductService.getOne(new QueryWrapper<TestProduct>().eq("product_id",id).eq("del_flag",0));
            return ResultUtil.success(testMethod);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testProduct 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestProduct testProduct) {
        if (StrUtil.isEmptyIfStr(testProduct)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductService.addTestProduct(testProduct);
    }

    /**
     * 修改数据
     *
     * @param testProduct 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestProduct testProduct) {
        if (StrUtil.isEmptyIfStr(testProduct)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductService.updTestProduct(testProduct);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testProductService.delTestProduct(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

