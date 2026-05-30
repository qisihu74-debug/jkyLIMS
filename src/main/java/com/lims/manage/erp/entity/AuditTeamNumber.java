package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-09 17:37
 * @Copyright © 河南交科院
 */
@Data
@TableName("qs_audit_team_number")
public class AuditTeamNumber {
    private int activeId;
    private String userId;
    private String name;
}
