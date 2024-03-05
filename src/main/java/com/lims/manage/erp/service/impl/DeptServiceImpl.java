package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.UserDepartmentMiddleEntity;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.service.SysRoleService;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.UserDepartmentMiddleMapper;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.DingDeptVo;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.vo.PagingToolVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/19 15:17
 * @Copyright © 河南交科院
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptDao, DingDeptEntity> implements DeptService {
    Logger logger = LoggerFactory.getLogger(DeptServiceImpl.class);
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private DingUsertDao dingUsertDao;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private UserDepartmentMiddleMapper userDepartmentMiddleMapper;
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    public List<DingDeptVo> getAllDept() {
        return deptDao.getAllDept();
    }

    @Override
    public Boolean add(DingDeptEntity entity) {
        entity.setId(GenID.getID());
        try {
            entity.setTime(new Date());
            entity.setUpdateTime(new Date());
            this.baseMapper.insert(entity);
            return true;
        } catch (Exception e) {
            logger.error("部门新增失败:{}", e);
            return false;
        }
    }

    @Override
    public DingDeptEntity getDeptByName(String name) {
        LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(DingDeptEntity::getName, name));
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean getDeptCode(String code) {
        LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(DingDeptEntity::getCode, code));
        DingDeptEntity data = this.baseMapper.selectOne(queryWrapper);
        if(data!=null){
            return true;
        }
        return false;
    }

    @Override
    public Boolean getDeptExists(String code, Long id) {
        LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(DingDeptEntity::getCode, code));
        queryWrapper.and(wrapper -> wrapper.eq(DingDeptEntity::getId, id));
        DingDeptEntity data = this.baseMapper.selectOne(queryWrapper);
        if(data==null){
            return true;
        }
        return false;
    }

    @Override
    public DingDeptEntity selectByPid(long l) {
        LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DingDeptEntity::getParentId, 0L);
        DingDeptEntity deptEntity = deptDao.selectOne(queryWrapper);
        return deptEntity;
    }

    @Override
    public Boolean edit(DingDeptEntity entity) {
        try {
            entity.setUpdateTime(new Date());
            this.baseMapper.updateById(entity);
            return true;
        } catch (Exception e) {
            logger.error("更新部门失败:{}", e);
            return false;
        }
    }

    @Override
    public Boolean delete(Long id) {
        LambdaUpdateWrapper<DingDeptEntity> lambdaUpdate = Wrappers.lambdaUpdate();
//        lambdaUpdate.d(DingDeptEntity::getIsDelete,"1")
//                .eq(DingDeptEntity::getId,id);
        lambdaUpdate.eq(DingDeptEntity::getId, id);
        try {
            this.baseMapper.delete(lambdaUpdate);
            return true;
        } catch (Exception e) {
            logger.error("删除部门失败:{}", e);
            return false;
        }
    }


    @Override
    public PageInfo findList(Integer pageNum, Integer pageSize, String search) {

        PageHelper.startPage(pageNum, pageSize);
        List<DingDeptEntity> list = deptDao.getAllList(search);
        PageInfo<DingDeptEntity> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public List<DingDeptEntity> sonList(Long id) {
        if (id == null) {
            //获取顶级部门id
            LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DingDeptEntity::getParentId, 0L);
            DingDeptEntity deptEntity = deptDao.selectOne(queryWrapper);
            id = deptEntity.getId();
        }
        List<DingDeptEntity> deptEntities = deptDao.sonList(id);
//        List<Long> chirdDeptsByUser = deptDao.getChirdDeptsByUser(ShiroUtils.getUserInfo().getUserId());
        List<DingDeptEntity> list = Lists.newArrayList();
        for (DingDeptEntity deptEntity : deptEntities) {
            list.add(deptEntity);
        }
        return list;
    }

    @Override
    public List<DingDeptEntity> parentList(Long id) {
        return deptDao.parentList(id);
    }

    @Override
    public PagingToolVo personList(Long id, String isInclude, Integer pageNum, Integer pageSize, String search) {
        if (id == null) {
            //获取顶级部门id
            LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DingDeptEntity::getParentId, 0L);
            DingDeptEntity deptEntity = deptDao.selectOne(queryWrapper);
            id = deptEntity.getId();
        }
        List<Long> depts = Lists.newArrayList();
        if (isInclude.equals("0")) {
            depts.add(id);
        } else {
            List<DingDeptEntity> deptEntities = deptDao.sonList(id);
//            List<Long> chirdDeptsByUser = deptDao.getChirdDeptsByUser(ShiroUtils.getUserInfo().getUserId());
            for (DingDeptEntity deptEntity : deptEntities) {
                depts.add(deptEntity.getId());
            }
        }

        // 1、依据部门id 下 获取人员信息
        List<DingUserEntity> personList = dingUsertDao.getAllUserTerm(search);
        Iterator<DingUserEntity> it = personList.iterator();
        while (it.hasNext()) {
            DingUserEntity dingUserEntity = it.next();
            int flagbit = 0;
            StringBuilder stringBuilder = new StringBuilder();
            if (dingUserEntity.getDepartment() != null&&!dingUserEntity.getDepartment().equals("")) {
                String[] strings = dingUserEntity.getDepartment().split(",");
                for (int i = 0; i < strings.length; i++) {
                    for (Long deptId : depts) {
                        if (deptId.equals((Long.parseLong(strings[i].trim())))) {
                            flagbit++;
                            stringBuilder.append((Long.parseLong(strings[i].trim())) + ",");
                        }
                    }
                }
                if (flagbit == 0) {
                    it.remove();
                }
                if(stringBuilder.length()>0){
                    dingUserEntity.setDepartment(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                }
                else {
                    dingUserEntity.setDepartment(null);
                }
            }
        }
        List<DingDeptEntity> deptAllList = deptDao.getAllList(null);
        // 2、并对人员所属部门 拼接部门信息。
        for (DingUserEntity dingUserEntity : personList) {
            StringBuilder stringBuilder = new StringBuilder();
            if (dingUserEntity.getDepartment() != null&&!dingUserEntity.getDepartment().equals("")) {
                String[] strings = dingUserEntity.getDepartment().split(",");
                for (int i = 0; i < strings.length; i++) {
                    for (DingDeptEntity deptEntity : deptAllList) {
                        if (deptEntity.getId().equals((Long.parseLong(strings[i].trim())))) {
                            stringBuilder.append(deptEntity.getName() + "、");
                        }
                    }
                }
                if(stringBuilder.length()>0){
                    dingUserEntity.setWorkplace(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                }
                else {
                    dingUserEntity.setWorkplace(null);
                }
            }
        }
        PagingToolVo pagingVo = new PagingToolVo();
        // 总条数
        pagingVo.setTotal(personList.size());
        // 开始
        pagingVo.setPageNum(pageNum);
        // 页码
        pagingVo.setPageSize(pageSize);
        // 当前页展示数量
        pagingVo.setSize(personList.size() % pageNum);
        // 总页数
        pagingVo.setPages(personList.size() / pageSize);
        // 开始行数
        pagingVo.setStartRow(personList.size() / pageSize / pageNum);
        // 结束行数
        pagingVo.setEndRow(personList.size() / pageSize);
        List<DingUserEntity> subList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(personList)) {
            try {
                if (personList.size() > 10 && personList.size() / 10 >= pageNum) {
                    subList = personList.subList((pageNum - 1) * pageSize, pageNum * pageSize);
                } else {
                    subList = personList.subList((pageNum - 1) * pageSize, personList.size());
                }
            } catch (IndexOutOfBoundsException e) {
                subList = personList;
            } catch (IllegalArgumentException e) {
                subList = personList.subList(0, personList.size());
            } finally {
                // 返回数据
                pagingVo.setList(subList);
            }
        }
        return pagingVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updatePersonDetails(DingUserEntity personEntity) {
        // 查询 ding_user_id 与 部门id 关系
        LambdaQueryWrapper<UserDepartmentMiddleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDepartmentMiddleEntity::getDingUserId, personEntity.getUserid());
        List<UserDepartmentMiddleEntity> sqlDingUserIds = userDepartmentMiddleMapper.selectList(queryWrapper);
        // 处理部门id 使用逗号分开。
        if (StringUtils.isNotEmpty(personEntity.getDepartment())) {
            String[] departments = personEntity.getDepartment().split(",");
            // 前端页面传递的 部门信息
            List<Long> newDepartments = new ArrayList<>();
            for (int i = 0; i < departments.length; i++) {
                newDepartments.add(Long.valueOf(departments[i]));
            }
            // 进行比较部门信息：进行删除或新增操作
            if (CollectionUtil.isNotEmpty(sqlDingUserIds)) {
                List<Long> dingUserIds = new ArrayList<>();
                for (UserDepartmentMiddleEntity userDepartmentMiddleEntity : sqlDingUserIds) {
                    dingUserIds.add(userDepartmentMiddleEntity.getDeptId());
                }
                // 进行删除或新增操作
                List<Long> deleteList = dingUserIds.stream()
                        .filter(element -> !newDepartments.contains(element))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(deleteList)) {
                    LambdaQueryWrapper<UserDepartmentMiddleEntity> deleteWrapper = new LambdaQueryWrapper<>();
                    deleteWrapper.eq(UserDepartmentMiddleEntity::getDingUserId, personEntity.getUserid());
                    deleteWrapper.in(UserDepartmentMiddleEntity::getDeptId, deleteList);
                    userDepartmentMiddleMapper.delete(deleteWrapper);
                }
                // addList
                List<Long> addDeptList = newDepartments.stream()
                        .filter(element -> !dingUserIds.contains(element))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(addDeptList)) {
                    for (int x = 0; x < addDeptList.size(); x++) {
                        UserDepartmentMiddleEntity record = new UserDepartmentMiddleEntity();
                        // 判断 用户与 dingUserId
                        record.setUserId(sqlDingUserIds.get(0).getUserId());
                        record.setDingUserId(personEntity.getUserid());
                        record.setDeptId(Long.valueOf(addDeptList.get(x)));
                        userDepartmentMiddleMapper.insert(record);
                    }
                }
                // 更新 sys_user中 部门信息。
                if (sqlDingUserIds.get(0).getUserId() != null && (CollectionUtil.isNotEmpty(deleteList) || CollectionUtil.isNotEmpty(addDeptList))) {
                    /// 更新
                    LambdaQueryWrapper<SysUserEntity> updateWrapper = new LambdaQueryWrapper<>();
                    updateWrapper.eq(SysUserEntity::getUserId, sqlDingUserIds.get(0).getUserId());
                    SysUserEntity updateEntity = new SysUserEntity();
                    updateEntity.setDepartment("[" + personEntity.getDepartment() + ",]");
                    sysUserDao.update(updateEntity, updateWrapper);
                }
            } else {
                // 直接新增即可
                for (int x = 0; x < newDepartments.size(); x++) {
                    UserDepartmentMiddleEntity record = new UserDepartmentMiddleEntity();
                    // 判断 用户与 dingUserId
                    record.setUserId(null);
                    record.setDingUserId(personEntity.getUserid());
                    record.setDeptId(Long.valueOf(newDepartments.get(x)));
                    userDepartmentMiddleMapper.insert(record);
                }
            }
        } else {
            // 删除关于部门信息成员。
            if (CollectionUtil.isNotEmpty(sqlDingUserIds)) {
                List<Long> deptIds = new ArrayList<>();
                for (UserDepartmentMiddleEntity userDepartmentMiddleEntity : sqlDingUserIds) {
                    deptIds.add(userDepartmentMiddleEntity.getDeptId());
                }
                LambdaQueryWrapper<UserDepartmentMiddleEntity> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(UserDepartmentMiddleEntity::getDingUserId, personEntity.getUserid());
                deleteWrapper.in(UserDepartmentMiddleEntity::getDeptId, deptIds);
                userDepartmentMiddleMapper.delete(deleteWrapper);
                // 更新 sys_user中 部门信息。
                if (sqlDingUserIds.get(0).getUserId() != null) {
                    /// 更新
                    LambdaQueryWrapper<SysUserEntity> updateWrapper = new LambdaQueryWrapper<>();
                    updateWrapper.eq(SysUserEntity::getUserId, sqlDingUserIds.get(0).getUserId());
                    SysUserEntity updateEntity = new SysUserEntity();
                    updateEntity.setDepartment("[" + personEntity.getDepartment() + ",]");
                    sysUserDao.update(updateEntity, updateWrapper);
                }
            }
        }
        dingUsertDao.updatePerson(personEntity);
        return true;
    }

    @Override
    public Boolean getSelectOne(DingUserEntity personEntity) {
        LambdaQueryWrapper<DingUserEntity> lambdaWrapper = new LambdaQueryWrapper<>();
        lambdaWrapper.eq(DingUserEntity::getJobnumber, personEntity.getJobnumber());
        DingUserEntity tableData = dingUsertDao.selectOne(lambdaWrapper);
        if (tableData == null) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean getSelectOneEdit(DingUserEntity personEntity) {
        LambdaQueryWrapper<DingUserEntity> lambdaWrapper = new LambdaQueryWrapper<>();
        lambdaWrapper.eq(DingUserEntity::getJobnumber, personEntity.getJobnumber())
        .eq(DingUserEntity::getUserid, personEntity.getUserid());
        DingUserEntity tableData = dingUsertDao.selectOne(lambdaWrapper);
        if (tableData == null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addPersonDetails(DingUserEntity personEntity) {
        // String类型 主键规则
        personEntity.setUserid(GenID.getOrderNum());
        // 处理部门id 使用逗号分开。
        if (StringUtils.isNotEmpty(personEntity.getDepartment())) {
            String[] departments = personEntity.getDepartment().split(",");
            for (int i = 0; i < departments.length; i++) {
                // 员工创建时： 员工建立绑定部门信息,可以绑定多组
                UserDepartmentMiddleEntity record = new UserDepartmentMiddleEntity();
                record.setDingUserId(personEntity.getUserid());
                record.setDeptId(Long.valueOf(departments[i]));
                userDepartmentMiddleMapper.insert(record);
            }
        }
        dingUsertDao.insert(personEntity);
        return true;
    }

    @Override
    public Boolean deletePersonDetail(List<String> ids) {
        for (String id : ids) {
            dingUsertDao.deletePerson(id);
        }
        return true;
    }

    @Override
    public List<String> getUserIdsByDeptNames(List<String> deptNames) {
        return deptDao.getUserIdsByDeptNames(deptNames);
    }

    @Override
    public Boolean checkUserId() {
        Long userId = ShiroUtils.getUserInfo().getUserId();
        Long id = deptDao.checkUserId(userId);
        if (id != null) {
//            SysRoleEntity sysRoleEntity = sysRoleService.checkRole(userId);
//            if (sysRoleEntity != null) {
            return true;
//            } else {
//                return false;
//            }
        }
        return false;
    }

    /**
     * 通过账号id 和 钉钉用户id 返回部门信息
     *
     * @param userId
     * @param dingUserId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Long> getDepartmentIdLong(Long userId, String dingUserId) {
        // 查询 ding_user_id 与 部门id 关系
        LambdaQueryWrapper<UserDepartmentMiddleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDepartmentMiddleEntity::getDingUserId, dingUserId);
        List<UserDepartmentMiddleEntity> sqlDingUserIds = userDepartmentMiddleMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(sqlDingUserIds)) {
            // 更新账号id信息
            UserDepartmentMiddleEntity entity = new UserDepartmentMiddleEntity();
            entity.setUserId(userId);
            LambdaQueryWrapper<UserDepartmentMiddleEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserDepartmentMiddleEntity::getDingUserId, dingUserId);
            userDepartmentMiddleMapper.update(entity, lambdaQueryWrapper);
            List<Long> deptList = new ArrayList<>();
            for (int i = 0; i < sqlDingUserIds.size(); i++) {
                deptList.add(sqlDingUserIds.get(i).getDeptId());
            }
            return deptList;
        }
        return null;
    }

    /**
     * 通过 userId 查询钉钉用户id与部门信息 把userId更新为空。
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateDepartmentId(Long userId) {
        // 查询 ding_user_id 与 部门id 关系
        LambdaQueryWrapper<UserDepartmentMiddleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDepartmentMiddleEntity::getUserId, userId);
        List<UserDepartmentMiddleEntity> sqlDingUserIds = userDepartmentMiddleMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(sqlDingUserIds)) {
            // 把userId更新为空
            userDepartmentMiddleMapper.updateUserIsNull(sqlDingUserIds.get(0).getDingUserId());
        }
        return true;
    }
}
