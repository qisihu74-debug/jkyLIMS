package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.HKDoorLaboratoryInstrumentRelEntity;
import com.lims.manage.erp.entity.HKDoorLaboratoryRelEntity;
import com.lims.manage.erp.entity.HKPersonUserRelEntity;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.mapper.HKDoorLaboratoryInstrumentRelEntityMapper;
import com.lims.manage.erp.mapper.HKDoorLaboratoryRelEntityMapper;
import com.lims.manage.erp.mapper.HKPersonUserRelEntityMapper;
import com.lims.manage.erp.mapper.HkDoorDao;
import com.lims.manage.erp.service.HkDoorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private HKDoorLaboratoryInstrumentRelEntityMapper hkDoorLaboratoryInstrumentRelEntityMapper;

    @Autowired
    private HKDoorLaboratoryRelEntityMapper hkDoorLaboratoryRelEntityMapper;

    @Autowired
    private HKPersonUserRelEntityMapper hkPersonUserRelEntityMapper;


}
