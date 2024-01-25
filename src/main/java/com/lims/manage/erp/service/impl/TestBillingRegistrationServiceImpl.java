package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.mapper.TestBillingRegistrationEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestBillingRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/1/25 11:27
 */
public class TestBillingRegistrationServiceImpl extends ServiceImpl<TestBillingRegistrationEntityMapper, TestBillingRegistrationEntity>
        implements TestBillingRegistrationService {
    @Autowired
    private TestBillingRegistrationEntityMapper testBillingRegistrationEntityMapper;

    @Override
    public Result list(TestBillingRegistrationEntity registrationEntity) {

        if (registrationEntity.getPageNum() == null || registrationEntity.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        LambdaQueryWrapper<TestBillingRegistrationEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 委托单号
        if (registrationEntity.getEntrustmentNo() != null) {
            queryWrapper.like(TestBillingRegistrationEntity::getEntrustmentNo, registrationEntity.getEntrustmentNo());
        }
        // 单位信息
        if (registrationEntity.getEntrustCompany() != null) {
            queryWrapper.like(TestBillingRegistrationEntity::getEntrustCompany, registrationEntity.getEntrustCompany());
        }
        // 登记时间
        if (registrationEntity.getRegistrationTime() != null) {
            queryWrapper.like(TestBillingRegistrationEntity::getEntrustCompany, registrationEntity.getEntrustCompany());
        }
        // 登记人
        if (registrationEntity.getRegisteredName() != null) {
            queryWrapper.like(TestBillingRegistrationEntity::getRegisteredName, registrationEntity.getRegisteredName());
        }
        queryWrapper.orderByAsc(TestBillingRegistrationEntity::getCrateTime);
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(registrationEntity.getPageNum(), registrationEntity.getPageSize());
        List<TestBillingRegistrationEntity> list = testBillingRegistrationEntityMapper.selectList(queryWrapper);
        PageInfo<TestBillingRegistrationEntity> result = new PageInfo<>(list);
//        testBillingRegistrationEntityMapper.selectList();
        return null;
    }
}
