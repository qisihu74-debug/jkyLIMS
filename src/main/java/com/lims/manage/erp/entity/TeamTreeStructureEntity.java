package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/3/1 16:11
 * 树结构 ——团队名称
 */
@Data
public class TeamTreeStructureEntity {
    private Long id;
    private String name;
    private Long sId;
    private String sname;
}
