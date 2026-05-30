package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;

import java.util.List;
@Data
public class SampleEntrustAddVo {
    private String code;
    private String sampleName;
    List<SampleEntity> samples;
}
