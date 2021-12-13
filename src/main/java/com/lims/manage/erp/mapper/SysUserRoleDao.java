package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
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
    Boolean removeOldRole(String userId);

    /**
     * 插入新角色
     * @param newRoles
     * @return
     */
    Boolean insertNewRole(SysUserRoleEntity newRoles);
	
}
