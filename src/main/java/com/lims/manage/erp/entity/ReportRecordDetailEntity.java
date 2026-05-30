package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test_report_record_detail")
public class ReportRecordDetailEntity {
    private Long id;

    private Long recordId;

    private Long checkItemId;

    private String checkItemName;

    private String specsContent;

    private String checkResult;

    private String judgeResult;

    private String coordinate;

    private String originUrl;

    private Long taskId;

    private Integer sampleId;

}