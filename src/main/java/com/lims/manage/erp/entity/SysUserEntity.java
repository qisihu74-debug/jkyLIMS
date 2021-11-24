package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
/**
 * @Description 系统用户实体
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Data
@TableName("sys_user")
public class SysUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 用户ID
	 */
	@TableId
	private Long userId;
	/**
	 * 用户名称
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 盐值
	 */
	private String salt;
	/**
	 * 状态:NORMAL正常  PROHIBIT禁用
	 */
	private String state;
	/**
	 * 用户创建时间
	 */
	private Timestamp createTime;
	/**
	 * 备注
	 */
	private String note;
	/**
	 * 用户所属组织
	 */
	@TableField(exist = false)
	private String userDept;

	public SysUserEntity() {
	}

	public SysUserEntity(RegisterUserInfoVo vo,String password, String salt,Timestamp createTime) {
		this.username = vo.getUsername();
		this.password = password;
		this.salt = salt;
		this.state = vo.getState();
		this.createTime = createTime;
		this.note = vo.getNote();
	}
}
