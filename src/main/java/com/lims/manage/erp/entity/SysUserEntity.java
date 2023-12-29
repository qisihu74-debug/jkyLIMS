package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.lims.manage.erp.vo.RegisterUserInfoVo;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

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
	@JsonSerialize(using = ToStringSerializer.class)
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
	/**
	 * 手机号
	 */
	private String mobile;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 部门
	 */
	private String department;
	/**
	 * 中文名
	 */
	private String name;
	/**
	 * 职位
	 */
	@TableField(exist = false)
	private String position;
	/**
	 * 钉钉用户id
	 */
	private String dingUserId;
	/**
	 * 更新时间
	 */
	private Timestamp time;

	private String signatureUrl;

	@TableField(exist = false)
	private Integer technicistId;

	/**
	 * 用户角色id列表
	 */
	@TableField(exist = false)
	private List<String> roleList;

	public SysUserEntity() {
	}

	public SysUserEntity(RegisterUserInfoVo vo,String password, String salt,Timestamp createTime) {
		this.username = vo.getUsername();
		this.password = password;
		this.salt = salt;
		this.state = vo.getState();
		this.createTime = createTime;
		this.note = vo.getNote();
		this.mobile = vo.getMobile();
		this.email = vo.getEmail();
		this.department = vo.getDeptId();
		this.name = vo.getName();
	}

	public SysUserEntity(Long userId, String username, String password, String salt) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.salt = salt;
	}
}
