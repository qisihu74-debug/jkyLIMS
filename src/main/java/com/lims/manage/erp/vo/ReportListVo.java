package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportListVo {
    private Long id;
    private Long taskId;
    private Long recordId;
    private String taskCode;
    private List<String> taskCodes;
    private String reportCode;
    private String sampleName;
    private List<LabelValueVo> sampleInfos;
    private List<LabelValueVo> makeReportSampleInfos;
    private String countPrice;
    private String requestDate;
    private String reportUrl;
    private String entrustTestType;
    private Integer taskState;
    /**
     * 报告生成时间
     */
    private String reportCompleteTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * state 状态
     */
    private Integer state;
    private Integer productId;
    /**
     * 任务流转ID
     */
    private Integer taskFlowId;
    private String entrustmentNo;

    private List<Long> deptIds;
    /**
     * 中间报告是否可以编辑
     */
    private Boolean flag;
    /**
     * state 状态
     */
    private Integer reportState;
    /**
     * 复核完的检测项数量
     */
    private Integer number;

    private String contractId;//合同id
    private String category;//印章分类，PHYSICS(物理章),ELECTRONIC(电子章)
    /**
     * 报告类型 0最终报告，1中间报告
     */
    private String reportType;
    private String verifyer;
    private String issuer;
    /**
     * 操作类型0线上编辑的报告，1线下编辑的报告
     */
    private Integer operateType;
    private Long userId;
}
