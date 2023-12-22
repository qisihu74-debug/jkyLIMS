package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


/**
 * 工时统计-返回至前端交互。
 */
@Data
public class TaskStatisticsVo {
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 团队名称+编号=任务编号
     */
    private String taskCode;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 接收任务时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderTime;
    /**
     * 完成时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date requiredCompletionTime;
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date startDate;
    /**
     * 截止日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date stopDate;
    /**
     * 工时
     */
    @TableField(exist = false)
    private String workingHours;
    /**
     * 样品名称
     */
    @TableField(exist = false)
    private String sampleName;
    /**
     * 来源
     */
    @TableField(exist = false)
    private String source;
    /**
     * 下单人-userID
     */
    private Long receiverUserId;
    /**
     * 下单人名称
     */
    private String receiver;
    /**
     * 开始页
     */
    @TableField(exist = false)
    private Integer pageNum;
    /**
     * 每页最大数
     */
    @TableField(exist = false)
    private Integer pageSize;

    /**
     * 团队id
     */
    private Long teamId;
    /**
     * 团队名称
     */
    private String teamName;
    /**
     * 部门id集合
     */
    @TableField(exist = false)
    private List<Long> longList;
    /**
     * state
     */
    @TableField(exist = false)
    private Integer state;
    /**
     * status = true , status = false
     */
    @TableField(exist = false)
    private Boolean status;
    /**
     * 已经接任务量
     */
    @TableField(exist = false)
    private String receivedTaskVolume;
    /**
     * 完成工作量
     */
    @TableField(exist = false)
    private String completedTaskVolume;

    /**
     * 工时id
     */
    private Long workingHoursId;

}
