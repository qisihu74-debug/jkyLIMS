package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lims.manage.erp.entity.PlanFileInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 考试/培训计划列表
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 计划id
     */
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
     * 发起人
     */
    private String name;

    /**
     * 面向对象
     */
    private String targetUser;

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
     * 计划地点
     */
    private String planPlace;

    /**
     * 计划简介
     */
    private String planContent;

    /**
     * 计划备注信息
     */
    private String planRemarks;

    /**
     * 参与状态
     */
    private String partakeStatus;

    /**
     * 计划结果状态:0:未上传;1:已上传
     */
    private Integer resultStatus;

    /**
     * 用户报名列表
     */
    private List<SysUserEntity> partakeUserList;

    /**
     * 完成人员信息列表
     */
    private List<PlanInfoImportVo> planInfoImportList;

    /**
     * 计划附件列表
     */
    private List<PlanFileInfo> planFileInfoList;
}
