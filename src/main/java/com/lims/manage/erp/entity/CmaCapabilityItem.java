package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("cma_capability_item")
public class CmaCapabilityItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String domain;
    private String standardName;
    private String standardCode;
    private String remarks;
    private String hcno;
    private String pdfUrl;
    private Integer pdfState;
    private Date createTime;
}
