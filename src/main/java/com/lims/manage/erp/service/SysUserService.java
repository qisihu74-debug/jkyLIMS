package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @Description 系统用户业务接口
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysUserService extends IService<SysUserEntity> {

    /**
     * 获取所有用户名称列表
     * @return List<SysUserEntity>
     */
    List<SysUserEntity> getUserNameList();

    /**
     * 获取除了当前用户外所有用户名称列表
     * @return List<SysUserEntity>
     */
    List<SysUserEntity> getExceptUserNameList();

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

    /**
     * 重置密码
     * @param entity
     * @return
     */
    Boolean resetPassword(SysUserEntity entity);


    /**
     * 获取用户信息列表
     * @param vo
     * @return
     */
    List<UserInfoVo> getUserInfos(UserInfoParamVo vo);

    /**
     * 更新用户信息
     * @param vo
     * @return
     */
    Boolean updateUserInfo(UserInfoVo vo);

    /**
     * 验证 账号与使用人 是否变动
     * @param userId
     * @param dingUserId
     * @return
     */
    Boolean getTheUser(Long userId,String dingUserId);

    /**
     * 验证账号 是否拥有使用人
     * @param dingUserId
     * @return
     */
    Boolean getTheUserList(String dingUserId);

    /**
     * 查询人员信息
     * @param search
     * @return
     */
    List<DingUserEntity> personList(String search);

    /**
     * 上传个人签名
     * @param file
     * @return
     */
    boolean uploadSignature(MultipartFile file);

    /**
     * 根据当前登陆人用户id判断该用户是否有系统管理员或者超级管理员的角色
     * 如果有返回true否则fasle
     * @param userId
     * @return
     */
    Boolean checkSysAndAdmRole(Long userId);

    Integer getTechnicistIdByUserId(Long userId);

    List<SysUserEntity> auditUserList();

    List<String> getDingIdsByUserIds(Set<Long> userIds);
}

