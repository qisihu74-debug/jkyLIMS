package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品委员会表
 */
@SuppressWarnings("serial")
@TableName("test_product_committee")
@Data
public class TestProductCommitteeEntity implements Serializable {
    //产品委员会ID
    @TableId(type = IdType.AUTO)
    private Integer councilId;
    //委员名称
    private String councilName;
    //用户ID
    private String userId;
    //所属部门
    private String department;
    //所属团队
    private String position;
    //委员身份
    private String councilIdentity;

    //0默认未删除,1删除
    private Integer delFlag;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
