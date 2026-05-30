package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Description 角色权限关系DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Component
@Mapper
public interface SysRoleMenuDao extends BaseMapper<SysRoleMenuEntity> {
	
}
