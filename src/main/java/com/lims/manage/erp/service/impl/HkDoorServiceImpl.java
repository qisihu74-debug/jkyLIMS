package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.mapper.HkDoorDao;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.util.HkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-09-14 11:16
 * @Copyright © 河南交科院
 */
@Service
public class HkDoorServiceImpl extends ServiceImpl<HkDoorDao, HkDoor> implements HkDoorService {
    @Autowired
    private HkDoorDao hkDoorDao;
    @Autowired
    private HkConfig hkConfig;

    @Override
    public PageInfo<HkDoor> doorList(Integer pageNum, Integer pageSize, String name, String position, String state) {
        PageHelper.startPage(pageNum,pageSize);
        List<HkDoor> list = hkDoorDao.doorList(name,position,state);
        PageInfo<HkDoor> pageInfo = new PageInfo(list);
        return pageInfo;
    }

    @Override
    public  Map<String, Object> doorDetails(DoorDetailReq doorDetailReq) {
        Map<String, Object> map = HkUtils.doorEvents(hkConfig.getDoorEvents(), doorDetailReq);
        return map;
    }
}
