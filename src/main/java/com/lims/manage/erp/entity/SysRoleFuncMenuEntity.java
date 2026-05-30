package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/11/24 10:31
 * @Copyright © 河南交科院
 */
@Data
public class SysRoleFuncMenuEntity {
    /**
     * 角色id
     */
    private Long roleId;

    private List<FunctionMenuEntity> list;
}
