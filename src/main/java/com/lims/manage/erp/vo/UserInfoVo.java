package com.lims.manage.erp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class UserInfoVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private Timestamp createTime;
    private String state;
    private String note;
    private String name;
    private String mobile;
    private String position;
    private String email;
    private String departmentId;
    private List<LabelValueVo> department;
    private String roleIds;
    private List<LabelValueVo> roles;

    /**
     * 部门id集合
     */
    private List<Long> departmentIdLong;
    /**
     * 角色id集合
     */
    private List<Long> roleIdsLong;
    /**
     * 钉钉用户id
     */
    private String dingUserId;
}
