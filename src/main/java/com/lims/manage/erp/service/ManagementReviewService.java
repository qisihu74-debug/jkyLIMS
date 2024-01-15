package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.ManageReviewPlanEntity;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: DLC
 * @Date: 2024/1/2 16:59
 */
public interface ManagementReviewService extends IService<ManageReviewPlanEntity> {

    /**
     * 查询列表
     *
     * @param manageReviewPlanEntity
     * @return
     */
    Result getList(ManageReviewPlanEntity manageReviewPlanEntity);

    /**
     * 新增数据
     *
     * @param manageReviewPlanEntity
     * @return
     */
    Result addManageReviewPlanEntity(ManageReviewPlanEntity manageReviewPlanEntity, MultipartFile[] file);

    /**
     * 新增数据
     *
     * @param id
     * @return
     */
    Result details(Integer id);


    /**
     * 更新数据
     *
     * @param manageReviewPlanEntity
     * @return
     */
    Result updateManageReviewPlanEntity(ManageReviewPlanEntity manageReviewPlanEntity, MultipartFile[] file);

    /**
     * 体系管理员列表
     *
     * @return
     */
    Result getSystemManagementList();

    /**
     * 删除计划
     *
     * @param id
     * @return
     */
    Result delete(Integer id);

}
