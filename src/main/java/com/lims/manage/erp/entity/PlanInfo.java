package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.PlanInfoImportVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 培训/考试计划
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_plan_info")
public class PlanInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 计划id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String planId;

    /**
     * 计划名称
     */
    private String planTitle;

    /**
     * 计划类型
     */
    private String planType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 计划开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date planBeginTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date planEndTime;

    /**
     * 面向对象
     */
    private String targetUser;

    /**
     * 计划地点
     */
    private String planPlace;

    /**
     * 计划简介内容
     */
    private String planContent;

    /**
     * 计划附件
     */
    private String enclosureUrl;

    /**
     * 备注内容
     */
    private String planRemarks;

    /**
     * 删除标记
     */
    private Integer delFlag;

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

    /**
     * 计划状态
     * 0：待开始
     * 1：进行中
     * 2：已结束
     */
    @TableField(exist = false)
    private Integer planStatus;

    /**
     * 用户参与状态
     *
     */
    @TableField(exist = false)
    private String partakeStatus;

    /**
     * 参与人用户id
     */
    @TableField(exist = false)
    private String partakeUserId;

    /**
     * 考试/培训计划excel导入实体
     */
    @TableField(exist = false)
    private List<PlanInfoImportVo> planImportList;
}
