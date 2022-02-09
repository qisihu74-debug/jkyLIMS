package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/2/9 9:07
 *
 */
@Data
public class SysRoleFunctionParent extends SysRoleFunction {
    /**
     * 菜单项父级 ID
     */
    private Long functionPid;
}
