package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DeclarationProductEntity {
    private Long planId;

    private String productType;

    private String productIndex;

    private Long productId;

    private String productName;

    private String attribute;

    private String createUser;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    private Integer pageNum;
    private Integer pageSize;
}
