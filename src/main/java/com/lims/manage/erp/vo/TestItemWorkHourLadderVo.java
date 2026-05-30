package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * test_item_work_hour_ladder 检测项 工时阶梯
 */
@Data
public class TestItemWorkHourLadderVo {
    /**
     * 检测项主键
     */
    private Integer checkItemId;
    /**
     * 检测项名
     */
    private String checkItemName;
    /**
     * 开始次数
     */
    private Integer startTimes;
    /**
     * 结束次数
     */
    private Integer endTimes;
    /**
     * 比例信息
     */
    private String decimalPoint;
}
