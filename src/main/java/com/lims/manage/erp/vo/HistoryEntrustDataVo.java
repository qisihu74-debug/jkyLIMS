package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import lombok.Data;

import java.util.List;

@Data
public class HistoryEntrustDataVo {
    private String projectName;
    private String projectPart;
    /**
     * 单位对象
     */
    List<TestCompanyJsonEntity> unitData;
}
