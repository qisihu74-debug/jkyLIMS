package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestInstrumentType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestInstrumentTypeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 仪器大类(TestInstrumentType)表控制层
 *
 * @author makejava
 * @since 2022-03-01 09:14:39
 */
@RestController
@RequestMapping("testInstrumentType")
public class TestInstrumentTypeController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestInstrumentTypeService testInstrumentTypeService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testInstrumentType 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestInstrumentType> page, TestInstrumentType testInstrumentType) {
        QueryWrapper<TestInstrumentType> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testInstrumentType.getName()!=null){
            queryWrapper.like("name",testInstrumentType.getName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testInstrumentTypeService.page(page, queryWrapper));
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
            TestInstrumentType testInstrumentType=this.testInstrumentTypeService.getOne(new QueryWrapper<TestInstrumentType>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(testInstrumentType);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testInstrumentType 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestInstrumentType testInstrumentType) {
        if (StrUtil.isEmptyIfStr(testInstrumentType)){
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentTypeService.addTestInstrumentType(testInstrumentType);
    }

    /**
     * 修改数据
     *
     * @param testInstrumentType 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestInstrumentType testInstrumentType) {
        if (StrUtil.isEmptyIfStr(testInstrumentType)){
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentTypeService.updTestInstrumentType(testInstrumentType);
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
            return this.testInstrumentTypeService.delTestInstrumentType(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

