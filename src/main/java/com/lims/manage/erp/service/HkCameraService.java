package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CameraInfo;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.result.Result;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-09-26 11:01
 * @Copyright © 河南交科院
 */
public interface HkCameraService extends IService<CameraInfo> {
    PageInfo<HkDoor> cameraList(Integer pageNum, Integer pageSize, String name, String position, String state);

    /**
     * 监控详情列表
     *
     * @param pageNum
     * @param pageSize
     * @param indexCode
     * @param taskCode
     * @param timeCycle
     * @param user
     * @return
     */
    Result cameraDetailsList(Integer pageNum, Integer pageSize, String indexCode, String taskCode, String timeCycle, String user);

    /**
     * 获取实验室下 监控信息
     *
     * @param testLaboratoryId
     * @return
     */
    Result getCameraList(String testLaboratoryId);

}
