package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/6/14 14:21
 * 用以返回 各项id
 */
@Data
public class TaskIdEntity {

    /**
     * 任务单id
     */
    private Long taskId;

    /**
     * 样品id
     */
    private Integer sampleId;

    /**
     * 检测项id
     */
    private Integer checkItemId;

    /**
     * 检测项详情表 主键
     */
    private Integer idItem;

    /**
     * 文件file检测项远程模板
     */
    private String fileUrl;

    /**
     * 检测项名称
     */
    private String checkItemName;

    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 原始记录名称
     */
    private String originalName;
    /**
     * 检测项状态
     */
    private Integer state;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 值1 = 表头数据填充
     */
    @TableField(exist = false)
    private Integer editData;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 检测日期-Text
     */
    @TableField(exist = false)
    private String testDateText;
    /**
     * 试验条件 - Text
     */
    @TableField(exist = false)
    private String testConditionText;
    /**
     * 主要仪器 - Text
     */
    @TableField(exist = false)
    private String equipmentText;

    /**
     * 检测项 sheet下标
     */
    @TableField(exist = false)
    private Integer sheetIndex;

}
