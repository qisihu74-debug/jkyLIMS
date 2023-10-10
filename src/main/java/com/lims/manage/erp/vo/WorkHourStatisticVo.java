package com.lims.manage.erp.vo;

import lombok.Data;

import java.io.Serializable;


/**
 * @Description 工时统计信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class WorkHourStatisticVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 工时信息
     */
    private Double workHour;
}
