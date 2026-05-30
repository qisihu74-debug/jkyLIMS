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
    /**
     * 菜单名。
     */
    private String treeName;
    /**
     * 菜单名——— 任务看板名
     */
    private String kanbanName;
    /**
     * 序号
     */
    private Integer sort;
    /**
     * 菜单类型
     */
    private String dataType;
}
