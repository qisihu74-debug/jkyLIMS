package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;
@Data
public class TaskDetailInfoVo {
    private Long taskId;//任务主键
    private String taskCode;//任务编号
    private String orderer;//下单人
    private String orderTime;//下单日期
    private String requiredCompletionTime;//完成日期
    private String cost;//本单费用
    private String samplingMethod;//取样方式
    private String checkPurpose;//检验目的
    private String presentInformation;//提供资料
    /**
     * 委托编号
     */
    private String entrustmentNo;
    /**
     * 委托id
     */
    private Long entrustmentId;
    /**
     * 判定依据
     */
    private String judgmentBasis;
    /**
     * 报告用章
     */
    private String sealType;
    private String fileUrl;//下载附件
    private List<SampleDetailVo> sampleDetailList;//样品信息

}
