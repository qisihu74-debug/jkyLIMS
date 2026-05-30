package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.UserOperationRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.UserOperationRecordService;
import com.lims.manage.erp.vo.BrowseFootstepsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户操作记录控制类
 * @author: zhq
 * @date: 2023-01-29
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/userOperationRecord")
public class UserOperationRecordController extends ApiController {

    @Resource
    UserOperationRecordService userOperationRecordService;

    /**
     * 根据事件类型和事件id获取用户的操作状态
     * @param userOperationRecord 事件类型及id
     * @return Result
     */
    @GetMapping(value = "getUserOperationStatus")
    public Result<?> getUserOperationStatus(UserOperationRecord userOperationRecord){
        return ResultUtil.success(userOperationRecordService.getUserOperationStatus(userOperationRecord));
    }

    /**
     * 获取用户浏览足迹
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return Result
     */
    @GetMapping(value = "getBrowseFootsteps")
    public Result<?> getBrowseFootsteps(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Page<BrowseFootstepsVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(userOperationRecordService.getBrowseFootsteps(page));
    }

    /**
     * 获取用户点赞列表
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return Result
     */
    @GetMapping(value = "getUserLikeList")
    public Result<?> getUserLikeList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Page<BrowseFootstepsVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(userOperationRecordService.getUserLikeList(page));
    }

    /**
     * 获取用户收藏列表
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return Result
     */
    @GetMapping(value = "getUserCollectList")
    public Result<?> getUserCollectList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Page<BrowseFootstepsVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(userOperationRecordService.getUserCollectList(page));
    }

    /**
     * 获取所有用户操作记录
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return Result
     */
    @GetMapping(value = "getUserOperationList")
    public Result<?> getUserOperationList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Page<BrowseFootstepsVo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(userOperationRecordService.getBrowseFootsteps(page));
    }
}
