package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/12/1 16:07
 * @Copyright © 河南交科院
 */
@Data
public class SampleItemEntity {
    /**
     * 检测项主键
     */
    private Integer id;
    /**
     * 样品id
     */
    private Integer sampleId;
    /**
     * 委托单id
     */
    private Long entrustId;
    /**
     * 检测项id
     */
    private Long checkItemId;
    /**
     * 检测项Pid
     */
    private Long checkItemPid;
    /**
     * 仪器id
     */
    private Integer instrumentId;
    /**
     * 检测依据
     */
    private Integer standardId;
    /**
     * 检测方法id
     */
    private Integer methodId;
    /**
     * 检测样次
     */
    private Integer times;
    /**
     * 单价
     */
    private Double unitPrice;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 结果描述
     */
    private String result;
    /**
     * 检测项检测人
     */
    private String testPeople;
    /**
     * 是否已审批1.是0否
     */
    private String isApproved;
    /**
     * 检测项名称
     */
    private String checkItemName;
    /**
     * 检测项坐标
     */
    private String coordinate;
    /**
     * 原始记录
     */
    private String originUrl;
    /**
     * 检测项状态
     */
    private Integer state;
    /**
     * 任务ID
     */
    private Long taskId;
    /**
     * 检测项下 对应的操作人员。
     */
    @TableField(exist = false)
    private List<TestCheckItemsTaskRel> itemsTaskRels;

}
