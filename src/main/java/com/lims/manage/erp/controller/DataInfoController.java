package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataAuditRecord;
import com.lims.manage.erp.entity.DataExperience;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DataInfoService;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * 学习资料/视频控制层
 *
 * @author: zhq
 * @date: 2023-01-05
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataInfo")
public class DataInfoController extends ApiController {

    @Resource
    DataInfoService dataInfoService;

    /**
     * 获取优质分享者
     *
     * @return Result
     */
    @GetMapping(value = "getQualityShare")
    public Result<?> getQualityShare() {
        return ResultUtil.success("获取优质分享者", dataInfoService.getQualityShare());
    }

    /**
     * 我分享的学习资料/视频
     *
     * @param dataInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "pageList")
    public Result<?> pageList(DataInfo dataInfo,
                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<DataInfo> page = new Page<>(pageNo, pageSize);
        dataInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        return ResultUtil.success(dataInfoService.pageList(page, dataInfo));
    }


    /**
     * 分享学习资料/视频
     *
     * @param dataInfoJson 资料信息
     * @param file     文件信息
     * @return Result
     */
    @PostMapping(value = "addDataInfo")
    public Result<?> addDataInfo(@RequestParam(value = "dataInfo") String dataInfoJson, @RequestParam("file") MultipartFile file) {
        DataInfo dataInfo = JSON.parseObject(dataInfoJson, DataInfo.class);
        return dataInfoService.addDataInfo(dataInfo, file);
    }

    /**
     * 修改学习资料/视频
     * @param dataInfoJson 学习资料信息
     * @param file 学习资料文件
     * @return Result
     */
    @PutMapping("editDataInfo")
    public Result<?> editDataInfo(@RequestParam(value = "dataInfo") String dataInfoJson, @RequestParam(value = "file",required = false) MultipartFile file){
        DataInfo dataInfo = JSON.parseObject(dataInfoJson, DataInfo.class);
        return dataInfoService.editDataInfo(dataInfo, file);
    }

    /**
     * 根据学习资料id删除学习资料
     *
     * @param dataId 学习资料id
     * @return Result<?>
     */
    @DeleteMapping(value = "delDataInfo/{dataId}")
    public Result<?> delDataInfo(@PathVariable String dataId) {
       return dataInfoService.delDataInfo(dataId);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result<?> selectOne(@PathVariable String id) {
        if (!StringUtils.isEmpty(id)){
            return ResultUtil.success(dataInfoService.getDataInfoById(id));
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 根据资料id获取资料信息-带有审核信息
     *
     * @param dataId 主键
     * @return 单条数据
     */
    @GetMapping("getDataAuditInfo/{dataId}")
    public Result<?> getDataAuditInfo(@PathVariable String dataId){
        return dataInfoService.getDataAuditInfo(dataId);
    }

    /**
     * 审核操作
     * @param dataAuditRecord 审核信息
     * @return Result
     */
    @PostMapping(value = "auditData")
    public Result<?> auditData(@RequestBody DataAuditRecord dataAuditRecord){
        return dataInfoService.auditData(dataAuditRecord);
    }

    /**
     * 根据资料id获取资料详情-带有资料点赞信息
     * @param dataId 资料id
     * @return Result
     */
    @GetMapping("getDataInfoDetail/{dataId}")
    public Result<?> getDataInfoDetail(@PathVariable String dataId){
        return dataInfoService.getDataInfoDetail(dataId);
    }

    /**
     * 根据资料id进行点赞
     * @param dataId 评论id
     * @return Result
     */
    @PostMapping(value = "like")
    public Result<?> like(String dataId){
        return dataInfoService.like(dataId);
    }

    /**
     * 根据资料id进行点踩
     * @param dataId 评论id
     * @return Result
     */
    @PostMapping(value = "tap")
    public Result<?> tap(String dataId){
        return dataInfoService.tap(dataId);
    }

    /**
     * 根据资料id进行收藏
     * @param dataId 评论id
     * @return Result
     */
    @PostMapping(value = "collect")
    public Result<?> collect(String dataId){
        return dataInfoService.collect(dataId);
    }

    /**
     * 根据资料id进行完成
     * @param dataId 评论id
     * @return Result
     */
    @PostMapping(value = "complete")
    public Result<?> complete(String dataId){
        return dataInfoService.complete(dataId);
    }

    /**
     * 根据资料id获取心得列表
     *
     * @param dataId 资料id
     * @param pageNo    页码
     * @param pageSize  页数
     * @return Result
     */
    @GetMapping(value = "getDataExperienceList")
    public Result<?> getDataExperienceList(@RequestParam(name = "dataId") String dataId,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<DataExperience> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(dataInfoService.getProblemCommentList(page, dataId));
    }

    /**
     * 添加学习心得
     *
     * @param dataExperience 学习心得信息
     * @return Result
     */
    @PostMapping(value = "addDataExperience")
    public Result<?> addDataExperience(@RequestBody DataExperience dataExperience) {
        return dataInfoService.addDataExperience(dataExperience);
    }

    /**
     * 根据学习心得id删除学习心得信息
     *
     * @param id 学习心得id
     * @return Result<?>
     */
    @DeleteMapping(value = "delDataExperience/{id}")
    public Result<?> delDataExperience(@PathVariable String id) {
        return dataInfoService.delDataExperience(id);
    }
}
