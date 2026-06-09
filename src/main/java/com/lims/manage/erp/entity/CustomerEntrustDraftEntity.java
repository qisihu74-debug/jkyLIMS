package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("cus_entrust_draft")
public class CustomerEntrustDraftEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private Integer bindCompanyId;
    private Integer bindCustomerId;
    private String draftNo;
    private String status;
    private String entrustCompany;
    private String entrustPeople;
    private String entrustPhone;
    private String witnessUnit;
    private String witnessPerson;
    private String witnessPhone;
    private String projectName;
    private String projectPart;
    private String sampleNames;
    private String checkItems;
    private String requestDate;
    private Integer reportCount;
    private String reportType;
    private String address;
    private String remark;
    private Timestamp submitTime;
    private String reviewRemark;
    private Long reviewUserId;
    private String reviewUserName;
    private Timestamp reviewTime;
    private Long formalEntrustId;
    private Integer formalEntrustmentNo;
    private Timestamp createTime;
    private Timestamp updateTime;
}
