package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("cus_account")
public class CustomerAccountEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "account_id", type = IdType.AUTO)
    private Long accountId;
    private String mobile;
    private String name;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;
    private String state;
    private Integer bindCompanyId;
    private Integer bindCustomerId;
    @JsonIgnore
    private String lastToken;
    @JsonIgnore
    private Timestamp tokenExpireTime;
    private Timestamp lastLoginTime;
    private Timestamp createTime;
    private Timestamp updateTime;
}
