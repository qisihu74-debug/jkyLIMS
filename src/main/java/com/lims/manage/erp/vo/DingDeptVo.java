package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.DingDeptEntity;
import lombok.Data;

import java.util.List;

@Data
public class DingDeptVo {
    /**
     * 部门id
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 部门父级id
     */
    private Long parentId;
    /**
     * 子部门
     */
    private List<DingDeptEntity> children;
}
