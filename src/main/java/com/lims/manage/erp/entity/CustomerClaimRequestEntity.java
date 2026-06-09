package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("cus_claim_request")
public class CustomerClaimRequestEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private Integer candidateCompanyId;
    private Integer candidateCustomerId;
    private String matchBasis;
    private String status;
    private String applyRemark;
    private String reviewRemark;
    private Long reviewUserId;
    private Timestamp reviewTime;
    private Timestamp createTime;
    private Timestamp updateTime;
}
