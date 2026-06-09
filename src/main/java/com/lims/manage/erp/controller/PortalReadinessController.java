package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portalReadiness")
public class PortalReadinessController {

    private static final String ACTIVE_USER_FILTER = "COALESCE(u.state,'NORMAL') <> 'PROHIBIT'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/summary")
    public Result summary() {
        List<Map<String, Object>> warnings = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phase", "phase6-readiness");
        data.put("readOnly", true);
        data.put("generatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        data.put("metrics", buildMetrics(warnings));
        data.put("roleTypeDistribution", safeRows(roleTypeDistributionSql(), warnings, "roleTypeDistribution"));
        data.put("unassignedUsers", safeRows(unassignedUsersSql(), warnings, "unassignedUsers"));
        data.put("multiPortalUsers", safeRows(multiPortalUsersSql(), warnings, "multiPortalUsers"));
        data.put("customerClaimStatuses", safeRows("SELECT status, COUNT(*) AS count FROM cus_claim_request GROUP BY status ORDER BY status", warnings, "customerClaimStatuses"));
        data.put("customerDraftStatuses", safeRows("SELECT status, COUNT(*) AS count FROM cus_entrust_draft GROUP BY status ORDER BY status", warnings, "customerDraftStatuses"));
        data.put("validationMatrix", validationMatrix());
        data.put("warnings", warnings);
        return ResultUtil.success(data);
    }

    private List<Map<String, Object>> buildMetrics(List<Map<String, Object>> warnings) {
        List<Map<String, Object>> metrics = new ArrayList<>();
        metrics.add(metric("internalUserCount", safeCount("SELECT COUNT(*) FROM sys_user u WHERE " + ACTIVE_USER_FILTER, warnings, "internalUserCount"), "normal"));
        metrics.add(metric("roleTypeCount", safeCount("SELECT COUNT(DISTINCT role_type) FROM sys_role WHERE role_type IS NOT NULL AND role_type <> ''", warnings, "roleTypeCount"), "normal"));
        metrics.add(metric("unassignedPortalUserCount", safeCount(unassignedPortalUserCountSql(), warnings, "unassignedPortalUserCount"), "warning"));
        metrics.add(metric("multiPortalUserCount", safeCount(multiPortalUserCountSql(), warnings, "multiPortalUserCount"), "warning"));
        metrics.add(metric("customerAccountCount", safeCount("SELECT COUNT(*) FROM cus_account", warnings, "customerAccountCount"), "normal"));
        metrics.add(metric("claimedCustomerCount", safeCount("SELECT COUNT(*) FROM cus_account WHERE bind_company_id IS NOT NULL", warnings, "claimedCustomerCount"), "normal"));
        metrics.add(metric("pendingClaimCount", safeCount("SELECT COUNT(*) FROM cus_claim_request WHERE status = 'PENDING'", warnings, "pendingClaimCount"), "warning"));
        metrics.add(metric("pendingCustomerDraftCount", safeCount("SELECT COUNT(*) FROM cus_entrust_draft WHERE status = 'SUBMITTED'", warnings, "pendingCustomerDraftCount"), "warning"));
        return metrics;
    }

    private Map<String, Object> metric(String key, Long value, String level) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("value", value);
        item.put("level", level);
        return item;
    }

    private Long safeCount(String sql, List<Map<String, Object>> warnings, String key) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class);
            return value == null ? 0L : value;
        } catch (Exception e) {
            warnings.add(warning(key, e));
            return 0L;
        }
    }

    private List<Map<String, Object>> safeRows(String sql, List<Map<String, Object>> warnings, String key) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            warnings.add(warning(key, e));
            return new ArrayList<>();
        }
    }

    private Map<String, Object> warning(String key, Exception e) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("message", e.getMessage());
        return item;
    }

    private String unassignedPortalUserCountSql() {
        return "SELECT COUNT(*) FROM sys_user u " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM sys_user_role ur " +
                "  JOIN sys_role r ON r.role_id = ur.role_id " +
                "  WHERE ur.user_id = u.user_id " +
                "    AND r.role_type IS NOT NULL AND r.role_type <> ''" +
                ")";
    }

    private String multiPortalUserCountSql() {
        return "SELECT COUNT(*) FROM (" +
                "  SELECT u.user_id " +
                "  FROM sys_user u " +
                "  JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "  JOIN sys_role r ON r.role_id = ur.role_id " +
                "  WHERE " + ACTIVE_USER_FILTER + " " +
                "    AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "  GROUP BY u.user_id " +
                "  HAVING COUNT(DISTINCT r.role_type) > 1" +
                ") t";
    }

    private String roleTypeDistributionSql() {
        return "SELECT r.role_type AS roleType, COUNT(DISTINCT u.user_id) AS userCount, " +
                "MIN(COALESCE(r.priority,100)) AS priority " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "JOIN sys_role r ON r.role_id = ur.role_id " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "GROUP BY r.role_type " +
                "ORDER BY priority ASC, roleType ASC";
    }

    private String unassignedUsersSql() {
        return "SELECT CAST(u.user_id AS CHAR) AS userId, u.username, u.name, u.mobile " +
                "FROM sys_user u " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM sys_user_role ur " +
                "  JOIN sys_role r ON r.role_id = ur.role_id " +
                "  WHERE ur.user_id = u.user_id " +
                "    AND r.role_type IS NOT NULL AND r.role_type <> ''" +
                ") " +
                "ORDER BY u.user_id DESC LIMIT 20";
    }

    private String multiPortalUsersSql() {
        return "SELECT CAST(u.user_id AS CHAR) AS userId, u.username, u.name, " +
                "GROUP_CONCAT(DISTINCT r.role_type ORDER BY COALESCE(r.priority,100), r.role_type SEPARATOR ',') AS roleTypes " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "JOIN sys_role r ON r.role_id = ur.role_id " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "GROUP BY u.user_id, u.username, u.name " +
                "HAVING COUNT(DISTINCT r.role_type) > 1 " +
                "ORDER BY u.user_id DESC LIMIT 20";
    }

    private List<Map<String, Object>> validationMatrix() {
        return Arrays.asList(
                validation("loginRoleMatrix", "pending", "manual"),
                validation("multiRoleSwitch", "pending", "manual"),
                validation("funcPermissionRegression", "pending", "manual"),
                validation("customerReportOwnership", "passed", "automated"),
                validation("customerTokenGuard", "passed", "automated"),
                validation("softBlockPaths", "pending", "manual"),
                validation("customerClaimSampling", "pending", "manual")
        );
    }

    private Map<String, Object> validation(String key, String status, String method) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("status", status);
        item.put("method", method);
        return item;
    }
}
