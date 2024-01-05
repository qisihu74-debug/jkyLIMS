package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.BatchReceiveTaskVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2021/12/13 14:58
 * 任务单 来自 test_task
 */
@Data
public class TaskTestEntity {
    /**
     * 任务id
     */
    private Long id;
    /**
     * 任务编号
     */
    private String code;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 下单人（委托单受理人）
     */
    private String orderer;
    /**
     * 下单时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date orderTime;
    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date requiredCompletionTime;
    /**
     * 团队id
     */
    private String teamId;
    /**
     * 接收人（团队中的副团长）
     */
    private String receiver;
    /**
     * 接收时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;
    /**
     * 团队名称+编号=任务编号
     */
    private String taskCode;
    /**
     * 领样人
     */
    private String sampler;
    /**
     * 领样时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss" , timezone ="GMT+8")
    private Date sampleReceivingTime;
    /**
     * 检测人
     */
    private String inspector;
    /**
     * 记录人
     */
    private String recorder;
    /**
     * 复核人
     */
    private String reviewer;
    /**
     * 复核时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date reviewTime;
    /**
     * 报告制作人
     */
    private String reportProducer;
    /**
     * 状态
     * 状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成
     */
    private Integer state;
    /**
     * 开始检测时间
     */
    private Date startDetectionTime;
    /**
     * 完成检测时间
     */
    private Date endDetectionTime;
    /**
     * 文件附件
     */
    private String fileUrlStr;
    /**
     * 是否出具报告：0、不出具；1、出具
     */
    private Integer issueReport;
    /**
     * 报告是否完成
     */
    private Integer reportComplete;

    /**
     * 审核人
     */
    private String verifyer;
    /**
     * 签发人
     */
    private String signer;
    /**
     * 样品状态描述
     */
    private String sampleStateDescription;
    /**
     * 提供资料
     */
    private String presentInformation;
    /**
     * 任务单价格
     */
    private Double taskPrice;
    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss" , timezone ="GMT+8")
    private Date creteTime;
    /**
     * 供删除委托单用
     * 废弃时间
     */
    @TableField(exist = false)
    private Date wasteTime;
    /**
     * 供删除委托单用
     * 废弃人
     */
    @TableField(exist = false)
    private String derelict;

    /**
     * 见习生：实习的新手
     */
    private String probationer;

    /**
     * 实习生
     */
    private String interns;

    /**
     * 辅助人员
     */
    private String auxiliaryPersonnel;

    /**
     * 工时id存在则已添加 不存在则为空
     *
     */
    @TableField(exist = false)
    private Integer workingHoursId;
    /**
     * 任务单状态：!=null 任务生成规则根据签发人所属团队走
     */
    @TableField(exist = false)
    private String taskListStatus;

    public TaskTestEntity() {
    }

    public TaskTestEntity(Long id, BatchReceiveTaskVo vo,Integer state,Date currentDate,String outward) {
        this.id = id;
        this.inspector = vo.getInspector();
        this.recorder = vo.getRecorder();
        this.reportProducer = vo.getReportProducer();
        this.reviewer = vo.getReviewer();
        this.sampleReceivingTime = vo.getSampleReceivingTime();
        this.sampler = vo.getSampler();
        this.receiver = vo.getReceiver();
        this.state=state;
        this.receiveTime = currentDate;
        this.sampleStateDescription = outward;
    }
}
