package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class DeclarationParamEntity {
    private Long planId;

    private Long productId;

    private Long checkItemId;

    private String checkItemName;

    private String attribute;

    private String createUser;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    private Integer standardId;

    private String standardName;

    private Integer methodId;

    private String methodName;

    private Integer instrumentId;

    private String instrumentName;

    private String limitRange;

    private String description;

    private Integer pageNum;
    private Integer pageSize;
    private Integer num;
}
