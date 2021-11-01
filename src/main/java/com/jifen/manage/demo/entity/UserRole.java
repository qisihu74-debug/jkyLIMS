package com.jifen.manage.demo.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.entity
 * @desc
 * @date 2021/11/1 15:30
 * @Copyright © 河南交科院
 */
@Data
public class UserRole {
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 角色类型id
     */
    private Long roleId;
}
