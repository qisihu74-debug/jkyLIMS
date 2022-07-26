package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportCheckItemDetailVo {
    private Long id;
    private Long checkItemId;
    private Integer state;
    private Integer sampleId;
    private Long taskId;
    private String checkItemName;
    private String specsContent;
    private String checkResult;
    private String judgeResult;
    private String coordinate;
    private String originUrl;
    private List<ReportSpecsContentVo> specsContents;
}
