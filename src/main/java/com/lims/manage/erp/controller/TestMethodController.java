package com.lims.manage.erp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestMethodService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 检测方法(TestMethod)表控制层
 *
 * @author makejava
 * @since 2022-03-02 10:04:05
 */
@RestController
@RequestMapping("testMethod")
public class TestMethodController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestMethodService testMethodService;

    @GetMapping("/getList")
    public Result getAll(TestMethod testMethod) {
        QueryWrapper<TestMethod> queryWrapper=new QueryWrapper<>(testMethod);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag",0);
        return ResultUtil.success(this.testMethodService.list(queryWrapper));
    }
    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testMethod 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestMethod> page, TestMethod testMethod) {
        QueryWrapper<TestMethod> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testMethod.getName()!=null){
            queryWrapper.like("name",testMethod.getName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testMethodService.page(page, queryWrapper));
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
            TestMethod testMethod=this.testMethodService.getOne(new QueryWrapper<TestMethod>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(testMethod);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testMethod 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestMethod testMethod) {
        if (StrUtil.isEmptyIfStr(testMethod)){
            return ResultUtil.error("数据为空");
        }
        return this.testMethodService.addTestMethod(testMethod);
    }

    /**
     * 修改数据
     *
     * @param testMethod 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestMethod testMethod) {
        if (StrUtil.isEmptyIfStr(testMethod)){
            return ResultUtil.error("数据为空");
        }
        return this.testMethodService.updTestMethod(testMethod);
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
            return this.testMethodService.delTestMethod(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

