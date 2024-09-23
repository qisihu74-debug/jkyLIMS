package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("hk_door_laboratory_instrument_rel")
public class HKDoorLaboratoryInstrumentRelEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String indexCode;

    private Integer testLaboratoryId;

    private Integer testInstrumentId;
}