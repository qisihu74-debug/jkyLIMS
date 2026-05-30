package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReportTemplateEntity {
    private Integer id;

    private String reportCode;

    private String productId;

    private String reportName;

    private String reportFileUri;

    private String isAvailable;

    private List<String> productIds;
}
