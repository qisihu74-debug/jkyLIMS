package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/11/23 14:39
 * 用户树结构
 */
@Data
public class SysUserTreeEntity {
    /**
     * 用户ID
     */
    @TableId
    private Long userId;
    /**
     * 登录名
     */
    private String loginName;

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
     * 手机号
     */
    private String mobile;
    /**
     * 状态:NORMAL正常  PROHIBIT禁用
     */
    private String state;
    /**
     * 用户所属组织
     */
    private String userDept;

    /**
     * 部门id
     */
    private String department;
}
