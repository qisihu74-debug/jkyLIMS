package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/5/20 11:36
 * 任务单统计  查询检测项详情
 */
@Data
public class TaskStatsItemVo {
    /**
     * 检测项id
     */
    private Integer itemId;

    /**
     * 检测项名称
     */
    private String checkItemName;

    /**
     * 试验开始
     */
    private Boolean testBegun;

    /**
     * 原始记录上传
     */
    private Boolean uploadRecords;

    /**
     * 已复核
     */
    private Boolean review;

    /**
     * 状态
     */
    private Integer state;
    /**
     * 描述信息
     */
    private String remark;
}
