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
     * 管理评审信息
     */
    @Resource
    private ManagementReviewService managementReviewService;


    /**
     * 获取具有管理评审角色的人员列表
     *
     * @return 返回人员列表
     */
    @GetMapping("/auditUserList")
    public Result auditUserList() {

        return this.managementReviewService.getSystemManagementList();
    }

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


    /**
     * 更新数据
     *
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result update(@RequestParam("json") String json, MultipartFile[] file) {
        ManageReviewPlanEntity testControlledDocumentsEntity = JSON.parseObject(json, ManageReviewPlanEntity.class);
        if (StrUtil.isEmptyIfStr(testControlledDocumentsEntity)) {
            return ResultUtil.error("数据为空");
        }
        return this.managementReviewService.updateManageReviewPlanEntity(testControlledDocumentsEntity, file);
    }

    /**
     * 详情
     *
     * @return 详情
     */
    @GetMapping("/details")
    public Result details(Integer id) {

        return this.managementReviewService.details(id);
    }

    /**
     * 删除
     *
     * @return 详情
     */
    @GetMapping("/delete")
    public Result delete(Integer id) {

        return this.managementReviewService.delete(id);
    }

    /**
     * 上传报告
     *
     * @param id
     * @param file
     * @param type 1内审员上传，2体系管理员上传
     * @return
     */
    @RequestMapping("/uploadFile")
    public Result uploadFile(@RequestParam("type") Integer type, @RequestParam("id") Integer id, MultipartFile[] file) {
        if (type == null || id == null || file == null) {
            return ResultUtil.error("缺少参数");
        }
        if (type.intValue() > 2 || type.intValue() < 1) {
            return ResultUtil.error("上传类型未定义");
        }
        return this.managementReviewService.uploadFile(type, id, file);
    }


}

