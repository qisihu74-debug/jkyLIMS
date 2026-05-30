package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/13 16:01
 * 团队内容
 */
@Data
public class TaskTestTeamEntity {
    /**
     * 团队id
     */
    private Integer id;
    /**
     * 团队编号
     */
    private String code;
    /**
     * 团队下用户名称
     */
    private String userName;


}
