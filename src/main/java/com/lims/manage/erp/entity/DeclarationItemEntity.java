package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class DeclarationItemEntity {
    private Long planId;

    private Long productId;

    private Long checkItemId;

    private String checkItemName;

    private String attribute;

    private String createUser;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    private List<DeclarationParamEntity> paramEntity;

    private Integer pageNum;
    private Integer pageSize;
}
