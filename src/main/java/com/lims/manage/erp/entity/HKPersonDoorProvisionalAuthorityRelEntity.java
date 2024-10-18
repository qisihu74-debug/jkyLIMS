package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("hk_person_door_provisional_authority_rel")
public class HKPersonDoorProvisionalAuthorityRelEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String personId;

    private String indexCode;

    private String startTime;

    private String endTime;

    private String laboratoryMessage;

    private String laboratoryName;

    private Date createTime;

    private Integer state;


}