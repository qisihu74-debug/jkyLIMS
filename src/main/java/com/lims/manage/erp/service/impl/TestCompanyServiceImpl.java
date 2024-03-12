package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.entity.TestCustomerEntity;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestCustomerDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCompanyService;
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
        PageInfo<EntrustAddVo> pageInfo = new PageInfo<>(entrusetList);
        // 回款登记展示
        return ResultUtil.success(pageInfo);
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
        // 执行新增：单位下联系人信息
        if (CollectionUtil.isNotEmpty(entity.getTestCustomerEntityList())) {
            for (TestCustomerEntity customerEntity : entity.getTestCustomerEntityList()) {
                customerEntity.setCompanyId(entity.getCompanyId());
                testCustomerDao.insertTestCustomer(customerEntity);
            }
        }
        return ResultUtil.success("操作成功");
    }
}
