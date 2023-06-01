package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

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
}
