package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CameraInfo;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.mapper.HkCameraDao;
import com.lims.manage.erp.service.HkCameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-09-26 11:01
 * @Copyright © 河南交科院
 */
@Service
public class HkCameraServiceImpl extends ServiceImpl<HkCameraDao, CameraInfo> implements HkCameraService {
    @Autowired
    private HkCameraDao hkCameraDao;

    @Override
    public PageInfo<HkDoor> cameraList(Integer pageNum, Integer pageSize, String name, String position, String state) {
        PageHelper.startPage(pageNum, pageSize);
        List<HkDoor> list = hkCameraDao.cameraList(name, position, state);
        PageInfo<HkDoor> pageInfo = new PageInfo(list);
        return pageInfo;
    }
}
