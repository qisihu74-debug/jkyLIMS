package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class QualificationRateVo {
    private Integer typeId;
    private String typeName;
    private Integer qualified;
    private Integer unqualified;
    private Integer total;
}