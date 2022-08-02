package com.lims.manage.erp.entity;

import lombok.Data;

@Data
public class ReportTemplateEntity {
    private Integer id;

    private String reportCode;

    private String productId;

    private String reportName;

    private String reportFileUri;

    private String isAvailable;
}
