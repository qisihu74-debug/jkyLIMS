package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

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

    @Select("SELECT user_id,username,name,mobile FROM sys_user WHERE state = 'NORMAL'")
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
    @Select("SELECT user_id,username,mobile,salt,password FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUserEntity getUserInformation(String username);

    @Select("SELECT mobile FROM sys_user WHERE name = #{name} LIMIT 1")
    SysUserEntity getMobileByName(String name);

    /**
     * 判断 username 是否改变
     * @param username
     * @param userId
     * @return
     */
    @Select("SELECT username FROM sys_user WHERE username = #{username} and user_id = #{userId} LIMIT 1")
    SysUserEntity getOldData(String username,Long userId);

    /**
     * 通过用户ID 获取使用人名称
     * @param userId
     * @return
     */
    @Select("SELECT name FROM sys_user WHERE user_id = #{userId}")
    String getSysUserName(Long userId);

    /**
     * 查询个人签名
     * @param userId
     * @return
     */
    @Select("select signature_url from sys_user where user_id = #{userId}")
    String getSignatureById(@Param("userId") Long userId);

    @Select("select signature_url from sys_user where name = #{name} LIMIT 1;")
    String getInspectorByName(@Param("name") String name);

    @Select("select user_id from sys_user where name = #{name}")
    SysUserEntity getUserIdByName(@Param("name") String name);

    @Select("select department from sys_user where user_id = #{userId}")
    String getDeptByUserId(@Param("userId") Long userId);

    /**
     * 根据角色id 查询用户id集合
     *
     * @param roleId
     * @return
     */
    @Select("SELECT\n" +
            "\tuser_id \n" +
            "FROM\n" +
            "\tsys_user_role \n" +
            "WHERE\n" +
            "\trole_id = #{roleId}")
    List<Long> selectUserIds(@Param("roleId") Long roleId);

    @Select("SELECT\n" +
            "\tt3.`name`,\n" +
            "\tt3.mobile \n" +
            "FROM\n" +
            "\tsys_role t1\n" +
            "\tLEFT JOIN sys_user_role t2 ON t1.role_id = t2.role_id\n" +
            "\tLEFT JOIN sys_user t3 ON t2.user_id = t3.user_id \n" +
            "WHERE\n" +
            "\tt1.role_name = '盖章人'")
    SysUserEntity getNameByRolName();

    @Select("SELECT\n" +
            "\tt3.`user_id`\n" +
            "FROM\n" +
            "\tsys_role t1\n" +
            "\tLEFT JOIN sys_user_role t2 ON t1.role_id = t2.role_id\n" +
            "\tLEFT JOIN sys_user t3 ON t2.user_id = t3.user_id \n" +
            "WHERE\n" +
            "\tt1.role_name = '盖章人' and t3.user_id=#{userId}")
    String checkRoleById(@Param("userId") Long userId);

    @Select("SELECT\n" +
            "\tt3.ding_user_id \n" +
            "FROM\n" +
            "\tsys_role t1\n" +
            "\tLEFT JOIN sys_user_role t2 ON t1.role_id = t2.role_id\n" +
            "\tLEFT JOIN sys_user t3 ON t2.user_id = t3.user_id \n" +
            "WHERE\n" +
            "\tt1.role_name = '盖章人'")
    List<String> getDingIdByRoleName();
}
