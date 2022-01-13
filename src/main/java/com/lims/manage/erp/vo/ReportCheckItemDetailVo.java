package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportCheckItemDetailVo {
    private Long checkItemId;
    private Integer state;
    private String checkItemName;
    private List<ReportSpecsContentVo> specsContents;
}
