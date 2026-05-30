package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class UserInfoParamVo {
    private Long deptId;
    private String username;
    private String mobile;
    private String state;
    private Integer pageNum;
    private Integer pageSize;
}
