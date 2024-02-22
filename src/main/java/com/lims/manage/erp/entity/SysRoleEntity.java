package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;


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
	@JsonSerialize(using = ToStringSerializer.class)
	@TableId
	private Long roleId;
	/**
	 * 角色名称
     */
    private String roleName;
    /**
     * 备注
     */
    private String roleRemark;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 是否删除-=1不可删除
     */
    private Integer isDelete;

}
