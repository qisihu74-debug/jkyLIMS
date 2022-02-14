package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 系统用户DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Component
@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {
    /**
     * 更改用户状态
     * @param entity
     * @return
     */
    Boolean updateUserState(SysUserEntity entity);

    /**
     * 修改密码
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
     * 更新用户基本信息
     * @param vo
     * @return
     */
    Boolean updateUserInfo(UserInfoVo vo);

    @Select("SELECT user_id,username FROM sys_user WHERE state = 'NORMAL'")
    List<SysUserEntity> GetUserList();

    List<SysUserTreeEntity> selectUserinfoList(SysUserTreeEntity sysUserTreeEntity);

    /**
     * 查询用户名称是否存在
     * @param username
     * @return
     */
    @Select("SELECT username FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUserEntity getOne(String username);

    /**
     * 根据用户名 获取密码 秘钥 和用户名
     * @param username
     * @return
     */
    @Select("SELECT user_id,username,salt,password FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUserEntity getUserInformation(String username);

    /**
     * 判断 username 是否改变
     * @param username
     * @param userId
     * @return
     */
    @Select("SELECT username FROM sys_user WHERE username = #{username} and user_id = #{userId} LIMIT 1")
    SysUserEntity getOldData(String username,Long userId);


}
