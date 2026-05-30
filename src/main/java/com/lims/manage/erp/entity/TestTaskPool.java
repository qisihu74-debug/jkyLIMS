package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 任务单
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("test_task_pool")
public class TestTaskPool implements Serializable {

//    private static final long serialVersionUID=1L;

    /**
     * 任务池任务id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务流水号
     */
    private String sn;

    /**
     * 样品信息
     */
    private String sample;

    /**
     * 产品id 多个使用逗号间隔
     */
    private String productId;

    /**
     * 样品别名
     */
    private String aliasName;

    /**
     * 对应委托单
     */
    private Long entrustmentId;

    /**
     * 本单费用
     */
    private String price;

    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requiredCompletionTime;

    /**
     * 任务流转要求
     */
    private String taskFlowReq;

    /**
     * 任务单号
     */
    private String taskCode;

    /**
     * 发布人
     */
    private String publisher;

    /**
     * 发布时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date publishDate;

    /**
     * 领取人id
     */
    private String receiveId;

    /**
     * 领取人（当前登陆人员）
     */
    private String receiveName;

    /**
     * 领取时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date receiveDate;
    @TableField(exist = false)
    private List<TaskRes> list;
    /**
     * 委托单号
     */
    @TableField(exist = false)
    private String entrustmentNo;

    /**
     * 任务单状态：!=null 任务生成规则根据签发人所属团队走
     */
    private String taskListStatus;
}
