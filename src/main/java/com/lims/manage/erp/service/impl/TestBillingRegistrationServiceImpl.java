package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.mapper.TestBillingRegistrationEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestBillingRegistrationService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: DLC
 * @Date: 2024/1/25 11:27
 */
@Service("testBillingRegistrationService")
public class TestBillingRegistrationServiceImpl extends ServiceImpl<TestBillingRegistrationEntityMapper, TestBillingRegistrationEntity>
        implements TestBillingRegistrationService {
    @Resource
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
        if (StringUtils.isNotEmpty(registrationEntity.getEntrustCompany())) {
            queryWrapper.like(TestBillingRegistrationEntity::getEntrustCompany, registrationEntity.getEntrustCompany());
        }
        // 登记时间
        if (registrationEntity.getRegistrationTime() != null) {

            SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            String startStr = startFormat.format(registrationEntity.getRegistrationTime());
            SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
            String endStr = endFormat.format(registrationEntity.getRegistrationTime());
            queryWrapper.between(TestBillingRegistrationEntity::getRegistrationTime, startStr, endStr);

        }
        // 登记人
        if (StringUtils.isNotEmpty(registrationEntity.getRegisteredName())) {
            queryWrapper.like(TestBillingRegistrationEntity::getRegisteredName, registrationEntity.getRegisteredName());
        }
        queryWrapper.orderByDesc(TestBillingRegistrationEntity::getUpdateTime);
        queryWrapper.orderByDesc(TestBillingRegistrationEntity::getCrateTime);
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(registrationEntity.getPageNum(), registrationEntity.getPageSize());
        List<TestBillingRegistrationEntity> list = testBillingRegistrationEntityMapper.selectList(queryWrapper);
        PageInfo<TestBillingRegistrationEntity> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(TestBillingRegistrationEntity registrationEntity) {
        if (registrationEntity.getId() == null) {
            return ResultUtil.error("id 不能为空");
        }
        if (registrationEntity.getRemark() == null) {
            return ResultUtil.error("备注不能为空");
        }
        TestBillingRegistrationEntity data = testBillingRegistrationEntityMapper.selectById(registrationEntity.getId());
        if (data == null) {
            return ResultUtil.error("id == " + registrationEntity.getId() + " 表中不存在");
        }
        // 更新信息
        TestBillingRegistrationEntity record = new TestBillingRegistrationEntity();
        record.setId(registrationEntity.getId());
        record.setRemark(registrationEntity.getRemark());
        record.setUpdateTime(new Date());
        testBillingRegistrationEntityMapper.updateByPrimaryKeySelective(record);
        return ResultUtil.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchAdd(List<TestBillingRegistrationEntity> list) {
        if (CollectionUtil.isEmpty(list)) {
            return ResultUtil.error("新增数据不能为空");
        }
        // 处理编号信息。 1、in 查询 获取委托编号 比较是否正确。
        List<EntrustEntity> entrustList = testBillingRegistrationEntityMapper.selectEntrustNoList(list);
        if (CollectionUtil.isEmpty(entrustList)) {
            // "所有单号不存在"
            Map<String, Object> map = new HashMap<>();
            for (TestBillingRegistrationEntity data : list) {
                data.setRemark("单号不存在");
            }
            map.put("list", list);
            return ResultUtil.success(map);
        }
        // 不正确的单号标记
        Set<Integer> entrustNoSet = new HashSet<>();
        // 遍历数据
        for (TestBillingRegistrationEntity data : list) {
            // 设置标记
            Boolean status = false;
            for (EntrustEntity entrustEntity : entrustList) {
                if (data.getEntrustmentNo().equals(entrustEntity.getEntrustmentNo())) {
                    entrustEntity.setRemark(data.getRemark());
                    status = true;
                }
            }
            // status = true 的话 ： 数据存入
            if (!status) {
                entrustNoSet.add(data.getEntrustmentNo());
            }
        }

        // 2、比较 test_billing_registration 保证唯一性。

        LambdaQueryWrapper<TestBillingRegistrationEntity> queryWrapper = new LambdaQueryWrapper<>();
        List<Integer> entrustNos = new ArrayList<>();
        for (TestBillingRegistrationEntity data : list) {
            entrustNos.add(data.getEntrustmentNo());
        }
        queryWrapper.in(TestBillingRegistrationEntity::getEntrustmentNo, entrustNos);
        List<TestBillingRegistrationEntity> billingRegistrations = testBillingRegistrationEntityMapper.selectList(queryWrapper);

        // test_billing_registration 中不存在的话
        Set<Integer> registrationNoSet = new HashSet<>();
        if (CollectionUtil.isNotEmpty(billingRegistrations)) {
            // 遍历数据
            for (TestBillingRegistrationEntity data : list) {
                // 设置标记
                Boolean status = false;
                for (TestBillingRegistrationEntity entity : billingRegistrations) {
                    if (data.getEntrustmentNo().equals(entity.getEntrustmentNo())) {
                        status = true;
                    }
                }
                // status = false的话 ： 数据存入
                if (status) {
                    registrationNoSet.add(data.getEntrustmentNo());
                }
            }
        }

        // 3、 数据不为空 则直接返回异常的抛出、获取list数据进行新增
        List<TestBillingRegistrationEntity> returnList = new ArrayList<>();
        // 3.1： 单号不正确
        if (CollectionUtil.isNotEmpty(entrustNoSet)) {
            for (Integer entrustNo : entrustNoSet) {
                TestBillingRegistrationEntity registrationEntity = new TestBillingRegistrationEntity();
                registrationEntity.setEntrustmentNo(entrustNo);
                registrationEntity.setRemark("委托单号不存在");
                returnList.add(registrationEntity);
            }
        }
        // 3.2： 比较 test_billing_registration 保证唯一性。
        if (CollectionUtil.isNotEmpty(registrationNoSet)) {
            for (Integer entrustNo : registrationNoSet) {
                TestBillingRegistrationEntity registrationEntity = new TestBillingRegistrationEntity();
                registrationEntity.setEntrustmentNo(entrustNo);
                registrationEntity.setRemark("发票单号已存在");
                returnList.add(registrationEntity);
            }
        }
        if (CollectionUtil.isNotEmpty(returnList)) {
            Map<String, Object> map = new HashMap<>();
            map.put("list", returnList);
            return ResultUtil.success(map);
        }
        // 执行批量新增：
        for (EntrustEntity entrustEntity : entrustList) {
            // 数据单个新增
            TestBillingRegistrationEntity data = new TestBillingRegistrationEntity();
            // 委托单ID
            data.setEntrustmentId(entrustEntity.getId());
            // 单号
            data.setEntrustmentNo(entrustEntity.getEntrustmentNo());
            // 公司信息
            data.setEntrustCompany(entrustEntity.getEntrustCompany());
            // 样品名称 = presentInformation
            data.setSampleName(entrustEntity.getPresentInformation());
            // 登记时间
            data.setRegistrationTime(new Date());
            // 登记人姓名
            data.setRegisteredName(ShiroUtils.getUserInfo().getName());
            // 创建时间
            data.setCrateTime(new Date());
            // 备注描述
            if (entrustEntity.getRemark() != null) {
                data.setRemark(entrustEntity.getRemark());
            }
            testBillingRegistrationEntityMapper.insert(data);
            // 更新委托单 是否开发发票 更新字段 isInvoice = "是"
            testBillingRegistrationEntityMapper.updateEntrustIsInvoice(data.getEntrustmentId());

        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", new ArrayList<>());
        return ResultUtil.success(map);
    }
}
