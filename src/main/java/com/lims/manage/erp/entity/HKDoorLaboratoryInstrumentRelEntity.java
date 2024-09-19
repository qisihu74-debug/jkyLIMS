package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("hk_door_laboratory_instrument_rel")
public class HKDoorLaboratoryInstrumentRelEntity implements Serializable {
    private Integer id;

    private String indexCode;

    private Integer testLaboratoryId;

    private Integer testInstrumentId;
}