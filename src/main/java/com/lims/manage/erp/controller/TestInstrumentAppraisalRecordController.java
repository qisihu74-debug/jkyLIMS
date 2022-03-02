package com.lims.manage.erp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestInstrumentAppraisalRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestInstrumentAppraisalRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 设备仪器检定记录表(TestInstrumentAppraisalRecord)表控制层
 *
 * @author makejava
 * @since 2022-03-01 11:44:15
 */
@RestController
@RequestMapping("testInstrumentAppraisalRecord")
public class TestInstrumentAppraisalRecordController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestInstrumentAppraisalRecordService testInstrumentAppraisalRecordService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testInstrumentAppraisalRecord 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestInstrumentAppraisalRecord> page, TestInstrumentAppraisalRecord testInstrumentAppraisalRecord) {
        QueryWrapper<TestInstrumentAppraisalRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testInstrumentAppraisalRecord.getName()!=null){
            queryWrapper.like("name",testInstrumentAppraisalRecord.getName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testInstrumentAppraisalRecordService.page(page, queryWrapper));
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
            TestInstrumentAppraisalRecord testInstrumentAppraisalRecord=this.testInstrumentAppraisalRecordService.getOne(new QueryWrapper<TestInstrumentAppraisalRecord>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(testInstrumentAppraisalRecord);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testInstrumentAppraisalRecord 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestInstrumentAppraisalRecord testInstrumentAppraisalRecord) {
        if (StrUtil.isEmptyIfStr(testInstrumentAppraisalRecord)){
            return ResultUtil.error("数据为空");
        }
        testInstrumentAppraisalRecord.setFileUrl("ceshi");
        return this.testInstrumentAppraisalRecordService.addInstrumentAppraisalRecord(testInstrumentAppraisalRecord);

    }

    /**
     * 修改数据
     *
     * @param testInstrumentAppraisalRecord 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestInstrumentAppraisalRecord testInstrumentAppraisalRecord) {
        if (StrUtil.isEmptyIfStr(testInstrumentAppraisalRecord)){
            return ResultUtil.error("数据为空");
        }
        testInstrumentAppraisalRecord.setFileUrl("ceshi");
        return this.testInstrumentAppraisalRecordService.updInstrumentAppraisalRecord(testInstrumentAppraisalRecord);
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
            return this.testInstrumentAppraisalRecordService.delInstrumentAppraisalRecord(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

