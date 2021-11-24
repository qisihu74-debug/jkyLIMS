package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;

import java.util.List;

/**
 * @Description 系统用户业务接口
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysUserService extends IService<SysUserEntity> {

    /**
     * 根据用户名查询实体
     * @Author gjl
     * @CreateTime 2021/11/09 16:30
     * @Param  username 用户名
     * @Return SysUserEntity 用户实体
     */
    SysUserEntity selectUserByName(String username);

    /**
     * 用户展示
     * @return
     */
    List<SysUserTreeEntity> selectUserList(String deptId);

    /**
     * 模糊查询信息
     * @param sysUserTreeEntity
     * @return
     */
    List<SysUserTreeEntity> selectUserLikeList(SysUserTreeEntity sysUserTreeEntity);

    /**
     * 查询全部
     * @return
     */
    List<SysUserTreeEntity> selectUserAllList();

    /**
     * 更新账号状态
     * @param entity
     * @return
     */
    Boolean updateUserState(SysUserEntity entity);

}

