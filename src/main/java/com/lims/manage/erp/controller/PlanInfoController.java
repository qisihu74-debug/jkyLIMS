package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PlanInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 考试/培训计划控制层
 *
 * @author: zhq
 * @date: 2023-01-13
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/planInfo")
public class PlanInfoController extends ApiController {

    @Resource
    PlanInfoService planInfoService;

    /**
     * 考试/培训计划列表
     *
     * @param planInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "pageList")
    public Result<?> pageList(PlanInfo planInfo,
                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<PlanInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(planInfoService.pageList(page, planInfo));
    }

    /**
     * 添加考试/培训计划
     *
     * @param planInfoJson 考试/培训计划信息
     * @param file         附件
     * @return Result
     */
    @PostMapping(value = "addPlanInfo")
    public Result<?> addPlanInfo(@RequestParam(value = "planInfo") String planInfoJson, MultipartFile file) {
        PlanInfo planInfo = JSON.parseObject(planInfoJson, PlanInfo.class);
        return planInfoService.addPlanInfo(planInfo, file);
    }

    /**
     * 获取计划详情和报名列表
     *
     * @param planId 计划id
     * @return Result
     */
    @GetMapping(value = "getPlanInfoDetail")
    public Result<?> getPlanInfoDetail(@RequestParam(value = "planId") String planId) {
        return ResultUtil.success(planInfoService.getPlanInfoDetail(planId));
    }

    /**
     * 用户报名计划
     *
     * @param planId 计划id
     * @return Result
     */
    @PostMapping(value = "enrollPlanInfo")
    public Result<?> enrollPlanInfo(String planId) {
        return planInfoService.enrollPlanInfo(planId);
    }

    /**
     * 根据计划id删除计划信息
     *
     * @param planId 计划id
     * @return Result<?>
     */
    @DeleteMapping(value = "delPlanInfo/{planId}")
    public Result<?> delPlanInfo(@PathVariable String planId) {
        return planInfoService.delPlanInfo(planId);
    }
}
