package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-01-10 16:28
 * @Copyright © 河南交科院
 */
@Data
public class HourCount {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 姓名
     */
    private String userName;
    /**
     * 工时积分
     */
    private String hours;
    private Double doubleHours;
    /**
     * 团队
     */
    private Integer teamId;
    /**
     * 团队父级id
     */
    private Integer pid;
    /**
     * 团队名称
     */
    private String teamName;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 部门总积分
     */
    private Double teamHours;
    /**
     * 部门总产值
     */
    private Integer teamPrice;
    /**
     * 个人占比部门
     */
    private String percentage;
    /**
     * 个人占部门绩效
     */
    private Double performance;

    /**
     * 已经接任务量
     */
    @TableField(exist = false)
    private String receivedTaskVolume;
    /**
     * 已经接任务量-产值
     */
    @TableField(exist = false)
    private String receivedTaskVolumeTaskPrice;
}
