package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 用户参加计划信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_user_plan_info")
public class UserPlanInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 记录id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 报名时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date enrollTime;

    /**
     * 参与状态
     */
    private String partakeStatus;

    /**
     * 计划id
     */
    private String planId;

    /**
     * 考试成绩
     */
    private double examinationScores;

    /**
     * 培训/考试个人表现
     */
    private String examinationRemarks;

    /**
     * 培训/考试积分数量
     */
    private Integer examinationIntegral;

    /**
     * 考试结果
     */
    private String examinationResult;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}
