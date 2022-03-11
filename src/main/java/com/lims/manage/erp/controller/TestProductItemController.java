package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductItemService;
import com.lims.manage.erp.vo.TestProductItemParamVo;
import com.lims.manage.erp.vo.TestProductItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 产品检测项(TestProductItem)表控制层
 *
 * @author makejava
 * @since 2022-03-02 15:14:49
 */
@RestController
@RequestMapping("testProductItem")
public class TestProductItemController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductItemService testProductItemService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductItem 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestProductItem> page, TestProductItem testProductItem) {
        QueryWrapper<TestProductItem> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testProductItem.getCheckItemName()!=null){
            queryWrapper.like("check_item_name",testProductItem.getCheckItemName());
        }
        if (testProductItem.getProductId()!=null){
            queryWrapper.like("product_id",testProductItem.getProductId());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testProductItemService.page(page, queryWrapper));
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
            TestProductItem testMethod=this.testProductItemService.getOne(new QueryWrapper<TestProductItem>().eq("check_item_id",id).eq("del_flag",0));
            TestProductItemParamVo testProductItemParamVo=this.testProductItemService.getItemParamVo(testMethod);
            return ResultUtil.success(testProductItemParamVo);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testProductItem 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestProductItemParamVo testProductItem) {
        if (StrUtil.isEmptyIfStr(testProductItem)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductItemService.addTestProductItem(testProductItem);
    }

    /**
     * 修改数据
     *
     * @param testProductItem 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestProductItemParamVo testProductItem) {
        if (StrUtil.isEmptyIfStr(testProductItem)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductItemService.updTestProductItem(testProductItem);
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
            return this.testProductItemService.delTestProductItem(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

