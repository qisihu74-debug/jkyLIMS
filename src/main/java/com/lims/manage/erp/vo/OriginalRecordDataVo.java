package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.EntrustEntity;
import lombok.Data;

@Data
public class OriginalRecordDataVo {
    /**
     * 记录编号
     */
    private String recordNumber;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 工程部位/用途
     */
    private String projectLocation;
    /**
     * 样品信息
     */
    private TemplateSampleVo sample;
    /**
     * 检测日期
     */
    private String testDate;
    /**
     * 试验条件
     */
    private String testCondition;
    /**
     * 检测依据
     */
    private String testBasis;
    /**
     * 判定依据
     */
    private String judgeBasis;
    /**
     * 主要仪器
     */
    private String equipment;

    public OriginalRecordDataVo(String recordNumber, EntrustEntity entrustBaseInfo, TemplateSampleVo sampleVo, String checkBasis, String judgeBasis) {
        this.recordNumber = recordNumber;
        this.projectName = entrustBaseInfo.getProjectName() == null ? "/" : entrustBaseInfo.getProjectName();
        this.projectLocation = entrustBaseInfo.getProjectPart() == null ? "/" : entrustBaseInfo.getProjectPart();
        this.sample = sampleVo;
        this.testDate = testDate;
        this.testCondition = testCondition;
        this.testBasis = testBasis;
        this.judgeBasis = judgeBasis;
        this.equipment = equipment;
    }
}
