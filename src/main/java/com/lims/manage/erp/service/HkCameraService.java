package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CameraInfo;
import com.lims.manage.erp.entity.HkDoor;

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
}
