package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class TaskListParamVo {
    /**
     * 报告单 主键
     */
    private Long taskId;
    private String taskCode;
    private String sampleName;
    private Integer state;
    private Integer teamId;
    private String receiveTime;

    private String beginDate;
    private String endDate;
    /**
     * 科室id集合
     */
    private List<Long> deptIds;
    private Integer pageNum;
    private Integer pageSize;

    /**
     * 检测人
     */
    private String inspector;
    /**
     * 复核人
     */
    private String reviewer;
    /**
     * 签名URL
     */
    private String signatureUrl;
    /**
     * 记录人
     */
    private String recorder;
    /**
     * 报告制作人
     */
    private String reportProducer;
    /**
     * 领样人
     */
    private String sampler;
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
     * 实现标记信息
     */
    private Integer flag;
    /**
     * 流转日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date taskFlowDate;


}
