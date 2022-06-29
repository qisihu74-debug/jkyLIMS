package com.lims.manage.erp.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class TaskVo {
    /**
     * 任务id
     */
    private Long id;
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


//    private Integer issueReport;
    private String issueReport;

    private String orderer;


    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date requiredCompletionTime;

    /**
     * 下单时间=orderTime (委托单转任务单的时间)
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    /**
     * 检测项分配的科室、分配时间、是否需要出具报告
     */
    List<CheckItemDeptVo> checkItemDeptVoList;
    /**
     * 出报告科室ID集合
     */
    private List<Long> deptIds;
    /**
     * 折扣率
     */
    private Double discount;
    /**
     * 任务单价格
     */
    private Double taskPrice;
}
