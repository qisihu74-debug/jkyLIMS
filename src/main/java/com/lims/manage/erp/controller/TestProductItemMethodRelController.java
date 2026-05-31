package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.entity.TestProductItemMethodRel;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductItemMethodRelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 检测项的检测方法(TestProductItemMethodRel)表控制层
 *
 * @author makejava
 * @since 2022-03-02 15:15:27
 */
@RestController
@RequestMapping("testProductItemMethodRel")
public class TestProductItemMethodRelController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestProductItemMethodRelService testProductItemMethodRelService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testProductItemMethodRel 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestProductItemMethodRel> page, TestProductItemMethodRel testProductItemMethodRel) {
        QueryWrapper<TestProductItemMethodRel> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return ResultUtil.success(this.testProductItemMethodRelService.page(page, queryWrapper));
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
            TestProductItemMethodRel testMethod=this.testProductItemMethodRelService.getOne(new QueryWrapper<TestProductItemMethodRel>().eq("id",id));
            return ResultUtil.success(testMethod);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testProductItemMethodRel 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestProductItemMethodRel testProductItemMethodRel) {
        if (StrUtil.isEmptyIfStr(testProductItemMethodRel)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductItemMethodRelService.addTestMethodRel(testProductItemMethodRel);
    }

    /**
     * 修改数据
     *
     * @param testProductItemMethodRel 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestProductItemMethodRel testProductItemMethodRel) {
        if (StrUtil.isEmptyIfStr(testProductItemMethodRel)){
            return ResultUtil.error("数据为空");
        }
        return this.testProductItemMethodRelService.updTestMethodRel(testProductItemMethodRel);
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
            return this.testProductItemMethodRelService.delTestMethodRel(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

