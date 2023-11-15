package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;
@Data
public class InstrumentParamVo extends InstrumentVo{
    private List<InstrumentVo> instrumentVoList;
    /**
     * 插单类型：0：正常插单；1：自定义插单
     */
    private Integer insertType;
}
