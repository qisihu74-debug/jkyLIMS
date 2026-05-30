package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * SOP标准作业指导书
 */
@Data
@TableName("sop_standard_instruction")
public class SopStandardInstruction implements Serializable {

    //ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    //SOP编号
    private String sopId;
    //SOP名称
    private String sopName;
    //适用产品/检测项
    private String product;
    //产品ID
    private String productId;
    //适用参数
    private String productParameter;
    //适用参数id
    private String productParameterId;

    //所属团队
    private String position;
    //所属团队ID
    private String positionId;
    //文件地址
    private String fileUrl;

    @TableField(exist = false)
    private String fileName;

    //0默认未删除,1删除
    private Integer delFlag;

    //发布日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date releaseDate;
    //实施日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date implementationDate;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //备注
    private String remarks;
    //仪器设备
    private String facility;
    //设备ID
    private String facilityId;


}
