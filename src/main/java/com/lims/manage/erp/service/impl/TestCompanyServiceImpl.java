package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.EntrustRemittanceRegistrationMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCompanyService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.EntrustAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: DLC
 * @Date: 2024/3/11 14:53
 */
@Service
public class TestCompanyServiceImpl implements TestCompanyService {

    @Autowired
    TestCompanyDao testCompanyDao;
    @Autowired
    TestCustomerDao testCustomerDao;
    @Autowired
    EntrustRemittanceRegistrationMapper registrationMapper;

    /**
     * 委托单位搜索查询
     *
     * @param entity
     * @return
     */
    @Override
    public Result searchCompanyInformation(TestCompanyEntity entity) {
        if (entity.getPageNum() == null || entity.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        if (StringUtils.isEmpty(entity.getType())) {
            return ResultUtil.error("单位类型不能为空");
        }
        PageHelper.clearPage();
        // 进行查询分页
        PageHelper.startPage(entity.getPageNum(), entity.getPageSize());
        if (StringUtils.isNotEmpty(entity.getOrder())) {
            // 判断参数限制为 "desc" or "asc"
            if (!"desc".equals(entity.getOrder()) && !"asc".equals(entity.getOrder())) {
                entity.setOrder(null);
            }
        }
        List<TestCompanyEntity> list = testCompanyDao.selectCompanyList(entity);
        // 通过in 查询 获取单位下 联系人信息。
        if (CollectionUtil.isNotEmpty(list)) {
            // 遍历输出 单位id集合
            List<Integer> companyIds = list.stream().map(TestCompanyEntity::getCompanyId).collect(Collectors.toList());
            // 获取单位下人员信息
            List<TestCustomerEntity> customerEntityList = testCustomerDao.selectCustomerList(companyIds);
            if (CollectionUtil.isNotEmpty(customerEntityList)) {
                // 把companyEntityList 存放至 对应的list集合中
                for (TestCompanyEntity companyEntity : list) {
                    List<TestCustomerEntity> testCustomerEntityList = new ArrayList<>();
                    for (TestCustomerEntity customerEntity : customerEntityList) {
                        if (companyEntity.getCompanyId().equals(customerEntity.getCompanyId())) {
                            testCustomerEntityList.add(customerEntity);
                        }
                    }
                    companyEntity.setTestCustomerEntityList(testCustomerEntityList);
                }
            }
        }
        PageInfo<TestCompanyEntity> pageInfo = new PageInfo<>(list);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 通过 companyId 获取委托单的信息。
     *
     * @param entity
     * @return
     */
    @Override
    public Result searchEntrustList(TestCustomerEntity entity) {
        if (entity.getPageNum() == null || entity.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        if (entity.getCompanyId() == null) {
            return ResultUtil.error("委托单id不能为空");
        }
        PageHelper.clearPage();
        // 进行查询分页
        PageHelper.startPage(entity.getPageNum(), entity.getPageSize());
        List<EntrustAddVo> entrusetList = testCustomerDao.selectEntrustList(entity);
        if (CollectionUtil.isNotEmpty(entrusetList)) {
            // 回款登记展示
            List<Long> entrustIds = entrusetList.stream().map(EntrustAddVo::getId).collect(Collectors.toList());
            LambdaQueryWrapper<EntrustRemittanceRegistrationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(EntrustRemittanceRegistrationEntity::getEntrustedId, entrustIds);
            // 排序方式
            queryWrapper.orderByAsc(EntrustRemittanceRegistrationEntity::getCreateTime);
            List<EntrustRemittanceRegistrationEntity> registrationList = registrationMapper.selectList(queryWrapper);
            if (CollectionUtil.isNotEmpty(registrationList)) {
                // 按创建时间 逆序 排列 分别对应委托单信息
                for (EntrustAddVo entrustAddVo : entrusetList) {
                    List<EntrustRemittanceRegistrationEntity> list = new ArrayList<>();
                    for (EntrustRemittanceRegistrationEntity data : registrationList) {
                        if (data.getEntrustedId().equals(entrustAddVo.getId())) {
                            list.add(data);
                        }
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("registrationList", list);
                    entrustAddVo.setJsonObject(jsonObject);
                }
            }
        }
        PageInfo<EntrustAddVo> pageInfo = new PageInfo<>(entrusetList);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 通过 companyId 获取委托单 实际应收总金额
     *
     * @param entity
     * @return
     */
    @Override
    public Result searchEntrusTotalMoney(TestCustomerEntity entity) {
        if (entity.getCompanyId() == null) {
            return ResultUtil.error("委托单id不能为空");
        }
        PageHelper.clearPage();
        String totalMoney = testCustomerDao.selectEntrusTotalMoney(entity);
        if (totalMoney == null) totalMoney = "";
        return ResultUtil.success(totalMoney);
    }

    /**
     * 委托单位新增
     *
     * @param entity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addCompany(TestCompanyEntity entity) {
        if (StringUtils.isEmpty(entity.getCompanyName())) {
            return ResultUtil.error("委托单位不能为空");
        }
        if (StringUtils.isEmpty(entity.getType())) {
            return ResultUtil.error("单位类型不能为空");
        }
        if (!"1".equals(entity.getType()) && !"2".equals(entity.getType())) {
            return ResultUtil.error("单位类型异常");
        }
        // 截取前后空格
        entity.setCompanyName(entity.getCompanyName().trim());
        // 效验委托单信息 是否存在
        LambdaQueryWrapper<TestCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCompanyEntity::getCompanyName, entity.getCompanyName());
        queryWrapper.eq(TestCompanyEntity::getType, entity.getType());
        List<TestCompanyEntity> list = testCompanyDao.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            return ResultUtil.error("操作失败  " + entity.getCompanyName() + "  委托单位已存在");
        }
        // 创建时间
        entity.setAddTime(new Date());
        testCompanyDao.insert(entity);
        return ResultUtil.success("操作成功");
    }

    /**
     * 添加联系人
     *
     * @param entity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addContacts(TestCustomerEntity entity) {
        // 效验 委托单位 不能为空
        if (entity == null) {
            return ResultUtil.error("操作失败:数据为空");
        }
        if (entity.getCompanyId() == null) {
            return ResultUtil.error("操作失败:单位id为空");
        }
        if (StringUtils.isEmpty(entity.getContacts())) {
            return ResultUtil.error("操作失败:联系人为空");
        }
        // entity.getCompanyId() 查询是否存在
        LambdaQueryWrapper<TestCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestCompanyEntity::getCompanyId, entity.getCompanyId());
        List<TestCompanyEntity> companyList = testCompanyDao.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(companyList)) {
            return ResultUtil.error("操作失败:单位id不存在");
        }
        // 去除空格
        entity.setContacts(entity.getContacts().trim());
        if (StringUtils.isNotEmpty(entity.getPhone())) {
            entity.setPhone(entity.getPhone().trim());
        } else {
            entity.setPhone(null);
        }
        // 查询单位id 对应的联系人信息
        List<TestCustomerJsonEntity> contactsList = testCompanyDao.selectPeopleInformation(entity.getCompanyId());
        // if contactsList 不为空 依次比对 联系人&联系方式 保证不重复录入。
        if (CollectionUtil.isNotEmpty(contactsList)) {
            for (TestCustomerJsonEntity jsonEntity : contactsList) {
                // 联系人一致 && 联系方式一致 则抛出 重复数据
                if ((jsonEntity.getClientName() + jsonEntity.getClientMobilePhone()).equals(entity.getContacts() + entity.getPhone())) {
                    return ResultUtil.error("操作失败:联系人 " + entity.getContacts() + " 联系方式 " + entity.getPhone() + "已存在");
                }
            }
        }
        testCustomerDao.insertTestCustomer(entity);
        return ResultUtil.success("操作成功:联系人添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addRegistrationEntity(EntrustRemittanceRegistrationEntity entity) {
        if (entity == null) {
            return ResultUtil.error("操作失败:数据不能为空");
        }
        if (entity.getEntrustedId() == null) {
            return ResultUtil.error("操作失败:委托单id不能为空");
        }
        if (StringUtils.isEmpty(entity.getEntrustmentNo())) {
            return ResultUtil.error("操作失败:委托编号不能为空");
        }
        List<Long> entrustIds = registrationMapper.selectEntrustedId(entity.getEntrustedId(), entity.getEntrustmentNo());
        if (CollectionUtil.isEmpty(entrustIds)) {
            return ResultUtil.error("操作失败:委托单信息不存在");
        }
        // 实现新增
        if (StringUtils.isEmpty(entity.getAmount())) {
            return ResultUtil.error("操作失败:金额不能为空");
        }
        entity.setId(null);
        // 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        entity.setRegistrant(userInfo.getName() + "&" + userInfo.getUserId());
        // 创建时间
        entity.setCreateTime(new Date());
        registrationMapper.insert(entity);
        return ResultUtil.success("操作成功");
    }
}
