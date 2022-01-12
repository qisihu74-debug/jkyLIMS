package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/1/11 16:45
 * 审批 实体类表
 */
@Data
public class ReportApprovalVo {
    /**
     * 任务单主键
     */
    private Long id;
    /**
     * 任务单编号
     */
    private String taskCode;
    /**
     *报告提交申请人
     */
    private String applicant;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 本单费用
     */
    private String cost;
    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requiredCompletionTime;
    /**
     * 审核人姓名
     */
    private String verifyer;
    /**
     * 审批时间
     */
    private Date verifyTime;
    /**
     * 报告单状态（0=未抢单 1=已抢单）
     */
    private Integer state;

}
