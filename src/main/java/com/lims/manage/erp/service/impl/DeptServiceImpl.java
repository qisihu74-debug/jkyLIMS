package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.DingDeptVo;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.vo.PagingToolVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2021/11/19 15:17
 * @Copyright © 河南交科院
 */
@Service
public class DeptServiceImpl  extends ServiceImpl<DeptDao, DingDeptEntity> implements DeptService {
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private DingUsertDao dingUsertDao;

    @Override
    public List<DingDeptVo> getAllDept() {
        return deptDao.getAllDept();
    }


    @Override
    public PageInfo findList(Integer pageNum, Integer pageSize, String search) {

        PageHelper.startPage(pageNum, pageSize);
        List<DingDeptEntity> list = deptDao.getAllList(search);
        PageInfo<DingDeptEntity> result = new PageInfo<>(list);
        return result;
    }

    @Override
    public PagingToolVo personList(Long id, String isInclude, Integer pageNum, Integer pageSize, String search) {
        if (id == null){
            //获取顶级部门id
            LambdaQueryWrapper<DingDeptEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DingDeptEntity::getParentId,0L);
            DingDeptEntity deptEntity = deptDao.selectOne(queryWrapper);
            id = deptEntity.getId();
        }
        List<Long> depts = Lists.newArrayList();
        if (isInclude.equals("0")){
            depts.add(id);
        }else {
            List<DingDeptEntity> deptEntities = deptDao.sonList(id);
//            List<Long> chirdDeptsByUser = deptDao.getChirdDeptsByUser(ShiroUtils.getUserInfo().getUserId());
            for (DingDeptEntity deptEntity:deptEntities) {
                        depts.add(deptEntity.getId());
            }
        }

        // 1、依据部门id 下 获取人员信息
        List<DingUserEntity> personList = dingUsertDao.getAllUserTerm(search);
        Iterator<DingUserEntity> it  = personList.iterator();
        while (it.hasNext()) {
            DingUserEntity dingUserEntity = it.next();
            int flagbit=0;
            if(dingUserEntity.getDepartment()!=null){
                String[] strings = dingUserEntity.getDepartment().split(",");
                for(int i=0;i<strings.length;i++){
                    for(Long deptId:depts){
                        if(deptId.equals((Long.parseLong(strings[i].trim())))){
                            flagbit++;
                        }
                    }
                }
                if(flagbit==0){
                    it.remove();
                }
            }
            else {
                it.remove();
            }
        }
        List<DingDeptEntity> deptAllList = deptDao.getAllList(null);
        // 2、并对人员所属部门 拼接部门信息。
        for(DingUserEntity dingUserEntity:personList)
        {
            StringBuilder stringBuilder = new StringBuilder();
            if(dingUserEntity.getDepartment()!=null){
                String[] strings = dingUserEntity.getDepartment().split(",");
                for(int i=0;i<strings.length;i++){
                    for(DingDeptEntity deptEntity:deptAllList){
                        if(deptEntity.getId().equals((Long.parseLong(strings[i].trim())))){
                            stringBuilder.append(deptEntity.getName()+"、");
                        }
                    }
                }
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                dingUserEntity.setWorkplace(stringBuilder.toString());
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
        pagingVo.setSize(personList.size()%pageNum);
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
}
