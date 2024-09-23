package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.HkDoor;

import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-09-14 11:15
 * @Copyright © 河南交科院
 */
public interface HkDoorService extends IService<HkDoor> {
    PageInfo<HkDoor> doorList(Integer pageNum, Integer pageSize, String name, String position, String state);

    Map<String, Object> doorDetails(DoorDetailReq doorDetailReq);
}
