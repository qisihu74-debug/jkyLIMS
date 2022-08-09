package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * @Author: DLC
 * @Date: 2022/7/15 11:15
 * 返回至前端 展示信息
 */
@Data
public class TestEntrustedTaskRelVo {
    /**
     * 流转单id
     */
    private Integer id;

    /**
     * 任务流转日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date taskFlowDate;

    /**
     * 报告类型（0,最终报告，1中间报告）
     */
    private Integer type;

    /**
     * 备注
     */
    private String remark;

    /**
     * 业务受理人
     */
    private String addressName;

    /**
     * 任务单编号
     */
    private String taskCode;

    /**
     * 任务来源
     */
    private String taskSource;

    /**
     * 委托单编号
     */
    private Integer entrustNo;

    /**
     * 报告编号
     */
    private String reportCode;

    /**
     * 试验完成日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date testEndTime;

    /**
     * 报告完成日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date reportFinishTime;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 当前展示页数量
     */
    private Integer pageSize;

    /**
     * 委托日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceDate;
    /**
     * 报告信息主键
     */
    private Long recordId;
}
