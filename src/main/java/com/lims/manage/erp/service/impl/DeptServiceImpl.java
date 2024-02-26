package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
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
        // 处理部门id 使用逗号分开。

        // String类型 主键规则
        personEntity.setUserid(GenID.getOrderNum());
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
        if (id != null){
            return true;
        }else {
            return false;
        }
    }
}
