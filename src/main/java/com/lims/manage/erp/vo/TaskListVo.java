package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TaskListVo {
    private Long taskId;//任务主键
    // 委托单id
    private Long entrustmentId;
    private String taskCode;//任务编号
    private String orderer;//下单人
    private String correlationTaskCode;//关联任务编号
    private String inspector;
    private Integer productId;
    private String sampleName;
    private String cost;//本单费用
    private String requiredCompletionTime;//完成日期
    private Integer state;//完成日期
    private String recorder;
    private String reviewer;
    private String reportProducer;
    private String outward;
    /**
     * 是否出具报告
     */
    private String issueReport;
    /**
     * 样品集合。
     */
    private List<SamplePrivateInfoVo> sampleList;
    /**
     * 领样人姓名
     */
    private String sampler;
    /**
     * 领样时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date sampleReceivingTime;
    /**
     * 样品描述信息
     */
    private String sampleStateDescription;

    /**
     * 任务流转日期
     */
    private String taskFlowDate;

    /**
     * 下单时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderTime;
    /**
     * 创建时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
    /**
     * 设备使用记录 集合
     */
    private List<InstrumentRecordListVo> instrumentRecordListVos = new ArrayList<>();
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
}
