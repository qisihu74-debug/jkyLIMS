package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.entity.ManageReviewPlanEntity;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ManagementReviewService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * (ManagementReviewController)表服务接口
 *
 * @author 丁连春
 * @since 2024-01-02 10:30:10
 */
@RestController
@RequestMapping("managementReview")
public class ManagementReviewController extends ApiController {
    /**
     * 受控文件信息
     */
    @Resource
    private ManagementReviewService managementReviewService;

    /**
     * 分页查询所有数据
     *
     * @param manageReviewPlanEntity 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(ManageReviewPlanEntity manageReviewPlanEntity) {
        return managementReviewService.getList(manageReviewPlanEntity);
    }

    /**
     * 新增数据
     *
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestParam("json") String json, MultipartFile[] file) {
        ManageReviewPlanEntity testControlledDocumentsEntity = JSON.parseObject(json, ManageReviewPlanEntity.class);
        if (StrUtil.isEmptyIfStr(testControlledDocumentsEntity)) {
            return ResultUtil.error("数据为空");
        }
        return this.managementReviewService.addManageReviewPlanEntity(testControlledDocumentsEntity, file);
    }


}

