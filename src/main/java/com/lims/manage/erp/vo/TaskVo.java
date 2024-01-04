package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lims.manage.erp.entity.TestEntrustedTaskRelEntity;
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
    /**
     * 任务单提供资料相等委托单
     */
    private String presentInformation;

    /**
     * 任务单流转 需要业务员提供信息
     */
    List<TestEntrustedTaskRelEntity> taskRelEntities;

    /**
     * 任务单创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 检测人
     */
    @TableField(exist = false)
    private String inspector;

    /**
     * 记录人
     */
    @TableField(exist = false)
    private String recorder;

    /**
     * 复核人
     */
    @TableField(exist = false)
    private String reviewer;

    /**
     * 报告制作人
     */
    @TableField(exist = false)
    private String reportProducer;

    /**
     * 辅助人员
     */
    @TableField(exist = false)
    private String auxiliaryPersonnel;

    /**
     * 见习生：实习的新手
     */
    @TableField(exist = false)
    private String probationer;

    /**
     * 实习生
     */
    @TableField(exist = false)
    private String interns;

    /**
     * 领样人
     */
    @TableField(exist = false)
    private String sampler;
    /**
     * 领样时间
     */
    @TableField(exist = false)
    private Date sampleReceivingTime;
    /**
     * 流水号任务单id
     */
    @TableField(exist = false)
    private Long poolId;
    /**
     * 接单人(授权签字人，报告签发人)
     */
    @TableField(exist = false)
    private String receiver;
    /**
     * 接单时间
     */
    @TableField(exist = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date receiverTime;

    /**
     * 指定检测团队
     */
    private Long teamId;

    /**
     * 样品状态描述
     */
    @TableField(exist = false)
    private String outwardDescribe;
    /**
     * 任务单状态：!=null 任务生成规则根据签发人所属团队走
     */
    @TableField(exist = false)
    private String taskListStatus;
}
