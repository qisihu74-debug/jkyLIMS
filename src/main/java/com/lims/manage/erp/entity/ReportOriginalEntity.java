package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ReportOriginalEntity {
    private Long id;
    private Long pid;

    private String code;

    private String name;

    private String url;
    private String status;

    private String remark;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date implementationDate;//实施日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expirationDate;//过期日期

    private Integer pageNum;
    private Integer pageSize;

    public ReportOriginalEntity() {
    }

    public ReportOriginalEntity(Long id, String code, String name, String url, String remark, Date createDate, Date updateDate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.url = url;
        this.remark = remark;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }
}