package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.util.HkUtils;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 实验室管理(TestLaboratory)表控制层
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
@RestController
@RequestMapping("testLaboratory")
@Api(value = "实验室管理", tags = {"实验室管理"})
public class TestLaboratoryController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestLaboratoryService testLaboratoryService;



    @GetMapping("/getList")
    public Result getAll(TestLaboratory testLaboratory) {

        LambdaQueryWrapper<TestLaboratory> queryWrapper = new LambdaQueryWrapper<>(testLaboratory);
        if (testLaboratory != null && StringUtils.isNotEmpty(testLaboratory.getSerch())) {
            queryWrapper.like(TestLaboratory::getName, testLaboratory.getSerch())
                    .or().like(TestLaboratory::getCode, testLaboratory.getSerch())
                    .or().like(TestLaboratory::getPosition, testLaboratory.getSerch());
        }
        queryWrapper.eq(TestLaboratory::getDelFlag, 0);
        queryWrapper.orderByDesc(TestLaboratory::getCreateTime);
        return ResultUtil.success(this.testLaboratoryService.list(queryWrapper));
    }

    /**
     * 分页查询所有数据
     *
     * @param testLaboratory 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询实验室信息")
    public Result selectAll(TestLaboratoryVo testLaboratory) {

        return this.testLaboratoryService.getPageList(testLaboratory);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @ApiOperation("根据ID查询实验室信息")
    public Result selectOne(@PathVariable Serializable id) {
        if (id != null && id != "") {
            return ResultUtil.success(this.testLaboratoryService.getOne(new QueryWrapper<TestLaboratory>().eq("id", id).eq("del_flag", 0)));
        } else {
            return ResultUtil.error("参数为空!");
        }
    }

    /**
     * 新增数据
     *
     * @param testLaboratory 实体对象
     * @return 新增结果
     */
    @Log(title = "新增实验室", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ApiOperation("添加实验室信息")
    public Result insert(@RequestBody TestLaboratory testLaboratory) {
        if (StrUtil.isEmptyIfStr(testLaboratory)) {
            return ResultUtil.error("数据为空");
        }
        return this.testLaboratoryService.addLaboratory(testLaboratory, null);
    }

    /**
     * 修改数据
     *
     * @param testLaboratory 实体对象
     * @return 修改结果
     */
    @Log(title = "修改实验室信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ApiOperation("修改实验室信息")
    public Result update(@RequestBody TestLaboratory testLaboratory) {
        if (StrUtil.isEmptyIfStr(testLaboratory)) {
            return ResultUtil.error("数据为空");
        }
        return this.testLaboratoryService.updLaboratory(testLaboratory, null);
    }

    /**
     * 新增数据
     *
     * @param json 实体对象
     * @param file 实体对象
     * @return 新增结果
     */
    @Log(title = "新增实验室", businessType = BusinessType.INSERT)
    @PostMapping("/addFile")
    @ApiOperation("添加实验室信息")
    public Result addFile(@RequestParam("json") String json, MultipartFile file) {
        if (StrUtil.isEmptyIfStr(json)) {
            return ResultUtil.error("数据为空");
        }
        TestLaboratory testLaboratory = JSON.parseObject(json, TestLaboratory.class);
        return this.testLaboratoryService.addLaboratory(testLaboratory, file);
    }

    /**
     * 修改数据
     *
     * @param json 实体对象
     * @param file 文件
     * @return 修改结果
     */
    @Log(title = "修改实验室信息", businessType = BusinessType.UPDATE)
    @PostMapping("/editFile")
    @ApiOperation("修改实验室信息")
    public Result update(@RequestParam("json") String json, MultipartFile file) {
        if (StrUtil.isEmptyIfStr(json)) {
            return ResultUtil.error("数据为空");
        }
        TestLaboratory testLaboratory = JSON.parseObject(json, TestLaboratory.class);
        return this.testLaboratoryService.updLaboratory(testLaboratory, file);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "删除实验室信息", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    @ApiOperation("删除实验室信息")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testLaboratoryService.delLaboratory(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

