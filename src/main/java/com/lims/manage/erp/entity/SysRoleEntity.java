package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * @Description 角色实体
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Data
@TableName("sys_role")
public class SysRoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 角色ID
	 */
	@TableId
	private Long roleId;
	/**
	 * 角色名称
	 */
	private String roleName;
}
