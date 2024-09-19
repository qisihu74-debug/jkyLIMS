package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.HKDoorLaboratoryInstrumentRelEntity;
import com.lims.manage.erp.entity.HKDoorLaboratoryRelEntity;
import com.lims.manage.erp.entity.HKPersonUserRelEntity;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.http.RestUtil;
import com.lims.manage.erp.mapper.HKDoorLaboratoryInstrumentRelEntityMapper;
import com.lims.manage.erp.mapper.HKDoorLaboratoryRelEntityMapper;
import com.lims.manage.erp.mapper.HKPersonUserRelEntityMapper;
import com.lims.manage.erp.mapper.HkDoorDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    /**
     * 编辑门禁与实验室id 进行关联
     *
     * @param hkDoorLaboratoryRelEntity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result editDoorLaboratoryRel(HKDoorLaboratoryRelEntity hkDoorLaboratoryRelEntity) {
        // 先删除 再新增关系
        LambdaQueryWrapper<HKDoorLaboratoryRelEntity> deteWapper = new LambdaQueryWrapper<>();
        deteWapper.eq(HKDoorLaboratoryRelEntity::getIndexCode, hkDoorLaboratoryRelEntity.getIndexCode());
        hkDoorLaboratoryRelEntityMapper.delete(deteWapper);

        // 新增
        hkDoorLaboratoryRelEntityMapper.insert(hkDoorLaboratoryRelEntity);

        return ResultUtil.success("操作成功");
    }

    /**
     * 编辑人员与userid 进行关联
     *
     * @param hkPersonUserRelEntity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result editPersonUserRel(HKPersonUserRelEntity hkPersonUserRelEntity) {
        // 先删除 再新增关系
        LambdaQueryWrapper<HKPersonUserRelEntity> deteWapper = new LambdaQueryWrapper<>();
        deteWapper.eq(HKPersonUserRelEntity::getPersonId, hkPersonUserRelEntity.getPersonId());
        hkPersonUserRelEntityMapper.delete(deteWapper);

        // 新增
        hkPersonUserRelEntityMapper.insert(hkPersonUserRelEntity);

        return ResultUtil.success("操作成功");
    }


}
