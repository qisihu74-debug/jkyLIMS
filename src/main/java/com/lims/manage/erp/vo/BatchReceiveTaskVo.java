package com.lims.manage.erp.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class BatchReceiveTaskVo {
    /**
     * 任务id
     */
    private List<Long> id;
    /**
     * 任务编号
     */
    private String code;
    /**
     * 检测人
     */
    private String inspector;
    /**
     * 记录人
     */
    private String recorder;
    /**
     * 报告制作人
     */
    private String reportProducer;
    /**
     * 复核人
     */
    private String reviewer;
    /**
     * 领样时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date sampleReceivingTime;
    /**
     * 领样人
     */
    private String sampler;
    /**
     * 接单人
     */
    private String receiver;
    /**
     * 接单时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;
    /**
     * 状态
     * 状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成
     */
    private Integer state;

}
