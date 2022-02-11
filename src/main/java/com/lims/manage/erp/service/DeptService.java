package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.vo.DingDeptVo;
import com.lims.manage.erp.vo.PagingToolVo;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/19 15:16
 * @Copyright © 河南交科院
 */
public interface DeptService extends IService<DingDeptEntity> {
    /**
     * 查询组织架构信息--树状
     * @return
     */
    List<DingDeptVo> getAllDept();

    /**
     * 添加部门
     * @param entity
     * @return
     */
    Boolean add(DingDeptEntity entity);

    /**
     * 根据部门名称查询部门信息
     * @param name
     * @return
     */
    DingDeptEntity getDeptByName(String name);

    /**
     * 获取顶级部门信息
     * @param l
     * @return
     */
    DingDeptEntity selectByPid(long l);

    /**
     * 部门编辑
     * @param entity
     * @return
     */
    Boolean edit(DingDeptEntity entity);

    /**
     * 部门删除
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 查询部门列表
     * @return
     */
    PageInfo findList(Integer pageNum, Integer pageSize, String search);

    /**
     * 获取子集部门列表
     * @param id
     * @return
     */
    List<DingDeptEntity> sonList(Long id);

    /**
     * 获取父级部门列表
     * @param id
     * @return
     */
    List<DingDeptEntity> parentList(Long id);

    /**
     * 分页查询部门下人员
     * @return
     * @param id
     * @param isInclude
     * @param pageNum
     * @param pageSize
     */
    PagingToolVo personList(Long id, String isInclude, Integer pageNum, Integer pageSize, String search);

    /**
     * 更新人员
     * @param personEntity
     * @return
     */
    Boolean updatePersonDetails(DingUserEntity personEntity);

    /**
     * 用户新增时 查询 当前工号是否存在
     * @param personEntity
     * @return
     */
    Boolean getSelectOne(DingUserEntity personEntity);

    /**
     * 用户编辑时 查询 当前工号是否变动
     * @param personEntity
     * @return true变动 false没变
     */
    Boolean getSelectOneEdit(DingUserEntity personEntity);

    /**
     * 保存人员
     * @param personEntity
     * @return
     */
    Boolean addPersonDetails(DingUserEntity personEntity);

    /**
     * 人员软删除
     * @param
     * @return
     */
    Boolean deletePersonDetail(List<String> ids);
}
