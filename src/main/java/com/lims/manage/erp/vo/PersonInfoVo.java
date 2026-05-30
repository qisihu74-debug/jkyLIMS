package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class PersonInfoVo {
    private Long taskId;
    private String inspector;
    private String recorder;
    private String reviewer;
    private String reportProducer;
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
}
