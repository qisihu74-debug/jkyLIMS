package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.ReportTemplateEntity;

import java.util.List;
import lombok.Data;

@Data
public class ReportProductRelVo {
    /**
     * 样品ID
     */
    private Integer sampleId;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 报告信息
     */
    List<ReportTemplateEntity> reportTemplates;
}
