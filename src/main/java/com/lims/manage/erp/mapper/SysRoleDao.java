package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description 角色DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
public interface SysRoleDao extends BaseMapper<SysRoleEntity> {

    /**
     * 通过用户ID查询角色集合
     * @Author gjl
     * @CreateTime 2021/11/09 18:01
     * @Param  userId 用户ID
     * @Return List<SysRoleEntity> 角色名集合
     */
    List<SysRoleEntity> selectSysRoleByUserId(Long userId);

    List<SysRoleEntity> selectSysRoleList(SysRoleEntity sysRoleEntity);

    /**
     *  根据角色id 获取人员信息
     * @return
     */
    List<LabelValueVo> selectSysyRoleName(Long roleId);

    @Select("SELECT sr.* FROM sys_role sr\n" +
            "        LEFT JOIN sys_user_role se ON sr.role_id=se.role_id\n" +
            "        WHERE se.user_id = #{userId} AND se.role_id=100")
    SysRoleEntity checkRole(@Param("userId") Long userId);
}
