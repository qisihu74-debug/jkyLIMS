package com.lims.manage.erp.entity;

import lombok.Data;

@Data
public class ProductReportRelEntity {
    private Long productId;
    private Long reportId;

    public ProductReportRelEntity(Long productId, Long reportId) {
        this.productId = productId;
        this.reportId = reportId;
    }
}
