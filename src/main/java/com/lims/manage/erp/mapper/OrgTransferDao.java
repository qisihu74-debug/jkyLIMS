package com.lims.manage.erp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 组织架构一键导入导出 DAO（部门 / 角色 / 用户）。
 * 显式 SQL，避免实体字段映射歧义；ID 列按字符串处理以保留 bigint 精度。
 */
@Component
@Mapper
public interface OrgTransferDao {

    // ---------------- 部门 ----------------
    @Select("SELECT id, parent_id parentId, name, code, user_name userName, remark, serial_number serialNumber " +
            "FROM sys_dept ORDER BY parent_id, serial_number, id")
    List<Map<String, Object>> listDepts();

    @Select("SELECT id, parent_id parentId, name, code, user_name userName, remark, serial_number serialNumber " +
            "FROM sys_dept WHERE id = #{id}")
    Map<String, Object> getDeptById(@Param("id") Long id);

    @Update("UPDATE sys_dept SET parent_id=#{parentId}, name=#{name}, code=#{code}, user_name=#{userName}, " +
            "remark=#{remark}, serial_number=#{serialNumber}, update_time=NOW() WHERE id=#{id}")
    int updateDept(Map<String, Object> m);

    @Insert("INSERT INTO sys_dept(id, parent_id, name, code, user_name, remark, serial_number, time, update_time) " +
            "VALUES(#{id}, #{parentId}, #{name}, #{code}, #{userName}, #{remark}, #{serialNumber}, NOW(), NOW())")
    int insertDept(Map<String, Object> m);

    // ---------------- 角色 ----------------
    @Select("SELECT role_id roleId, role_name roleName, role_remark roleRemark, role_type roleType, priority " +
            "FROM sys_role ORDER BY priority, role_id")
    List<Map<String, Object>> listRoles();

    @Select("SELECT role_id roleId, role_name roleName, role_remark roleRemark, role_type roleType, priority " +
            "FROM sys_role WHERE role_id = #{roleId}")
    Map<String, Object> getRoleById(@Param("roleId") Long roleId);

    @Update("UPDATE sys_role SET role_name=#{roleName}, role_remark=#{roleRemark}, role_type=#{roleType}, " +
            "priority=#{priority} WHERE role_id=#{roleId}")
    int updateRole(Map<String, Object> m);

    @Insert("INSERT INTO sys_role(role_name, role_remark, role_type, priority, create_time) " +
            "VALUES(#{roleName}, #{roleRemark}, #{roleType}, #{priority}, NOW())")
    int insertRole(Map<String, Object> m);

    @Select("SELECT role_id FROM sys_role WHERE role_name = #{roleName} LIMIT 1")
    Long roleIdByName(@Param("roleName") String roleName);

    // ---------------- 用户 ----------------
    @Select("SELECT u.user_id userId, u.username, u.name, u.mobile, u.position, u.email, " +
            "GROUP_CONCAT(r.role_name ORDER BY r.role_id SEPARATOR ',') roles " +
            "FROM sys_user u LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.role_id " +
            "GROUP BY u.user_id, u.username, u.name, u.mobile, u.position, u.email ORDER BY u.username")
    List<Map<String, Object>> listUsersWithRoles();

    @Select("SELECT user_id userId, username, name, mobile, position, email " +
            "FROM sys_user WHERE username = #{username} LIMIT 1")
    Map<String, Object> getUserByUsername(@Param("username") String username);

    @Update("UPDATE sys_user SET name=#{name}, mobile=#{mobile}, position=#{position}, email=#{email} " +
            "WHERE user_id=#{userId}")
    int updateUserBasic(Map<String, Object> m);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int countUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int addUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
