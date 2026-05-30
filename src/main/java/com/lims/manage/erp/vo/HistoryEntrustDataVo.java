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
    /**
     * 收件人
     */
    private String addressee;
    /**
     * 电话
     */
    private String mobile;
    /**
     * 地址
     */
    private String address;
    /**
     * 收报告单位
     */
    private String reportReceivingUnit;
}
