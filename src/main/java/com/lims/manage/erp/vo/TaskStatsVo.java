package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.util.List;

/**
 * 任务统计实体类 供 返回至前端交互。
 */
@Data
public class TaskStatsVo {
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 科室ID
     */
    private Long deptId;
    /**
     * 任务编号纯数字
     */
    private String code;
    /**
     * 团队名称+编号=任务编号
     */
    private String taskCode;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 任务状态
     */
    private Integer state;
    private Integer reportComplete;


    private String issueReport;

    private String orderer;


    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date requestDate;

    /**
     * 实际完成日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date finishDate;
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 截止日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date stopDate;

    /**
     * 费用
     */
    private String cost;

    /**
     * 报告编号
     */
    private String reportCode;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 样品名称
     */
    private String sampleName;

    /**
     * 任务状态 复核/未复核。
     */
    private String taskStatus;
    private Integer pageNum;
    private Integer pageSize;

    /**
     * 检测项集合
     */
    private List<TaskStatsItemVo> list;
    /**
     * 任务进度
     */
    private String taskProgress;

    private List<SampleDetailVo> sampleDetailList;//样品信息

    /**
     * 委托单折扣率
     */
    private String discount;

    /**
     * 备注
     */
    private String remark;

    private Integer[] integers;

}
