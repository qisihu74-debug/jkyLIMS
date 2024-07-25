package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 用户与角色关系DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Component
@Mapper
public interface SysUserRoleDao extends BaseMapper<SysUserRoleEntity> {
    /**
     * 移除旧角色
     * @param userId
     * @return
     */
    Boolean removeOldRole(Long userId);

    /**
     * 插入新角色
     *
     * @param newRoles
     * @return
     */
    Boolean insertNewRole(SysUserRoleEntity newRoles);

    @Select("select distinct role_id from sys_user_role where user_id=#{userId}")
    List<Long> getRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT\n" +
            "\tt2.role_id AS VALUE,\n" +
            "\tt2.role_name AS label \n" +
            "FROM\n" +
            "\tsys_user_role AS t1\n" +
            "\tLEFT JOIN sys_role t2 ON t1.role_id = t2.role_id \n" +
            "WHERE\n" +
            "\tt1.user_id = #{userId}")
    List<LabelValueVo> getRolesByUserId(@Param("userId") Long userId);

    /**
     * 查询最高 管理者
     *
     * @return
     */
    @Select("SELECT DISTINCT\n" +
            "\tuser_id \n" +
            "FROM\n" +
            "\tsys_user_role \n" +
            "WHERE\n" +
            "\trole_id = 999")
    List<Long> selectTopManagement();
}
