package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.entity
 * @desc
 * @date 2021/11/10 11:41
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_role_function")
public class SysRoleFunction implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 菜单id
     */
    private Long functionId;
    /**
     * 角色id
     */
    private Long roleId;
}
