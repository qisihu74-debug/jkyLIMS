package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("hk_camera_laboratory_instrument_rel")
public class HKCameraLaboratoryInstrumentRelEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String camera;

    private Integer testLaboratoryId;

    private Integer testInstrumentId;
}