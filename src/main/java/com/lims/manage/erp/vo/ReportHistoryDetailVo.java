package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import lombok.Data;

import java.util.List;
@Data
public class ReportHistoryDetailVo {
    private String sampleName;
    private String sampleCode;
    private List<ReportRecordDetailEntity> checkItemInfo;
}
