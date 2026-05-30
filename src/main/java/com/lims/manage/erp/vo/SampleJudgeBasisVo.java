package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class SampleJudgeBasisVo extends SampleSimpleListVo {
    public List<LabelValueVo> judgeBasisList;
}
