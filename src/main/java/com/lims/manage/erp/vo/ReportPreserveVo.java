package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReportPreserveVo extends ReportRecordEntity {
    private List<ReportRecordDetailEntity> checkInfos;
}
