package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/22 9:43
 * 树结构
 */
@Data
public class TreeFunction {
    private Integer functionId;
    private String treeName;
    private Integer functionPid;
    private String pidName;
    private Integer sort;
    private boolean catesFlag;
    private List Children;
}
