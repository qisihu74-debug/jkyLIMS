package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private static final List<String> RISK_TYPES = Arrays.asList("UNASSIGNED_PORTAL", "MULTI_PORTAL");
    private static final List<String> REVIEW_QUERY_STATUSES = Arrays.asList("PENDING", "CONFIRMED", "DEFERRED", "IGNORE");
    private static final List<String> REVIEW_WRITE_STATUSES = Arrays.asList("CONFIRMED", "DEFERRED", "IGNORE");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/summary")
    public Result summary() {
        List<Map<String, Object>> warnings = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phase", "phase6-readiness");
        data.put("readOnly", true);
        data.put("reviewWriteEnabled", true);
        data.put("migrationWriteEnabled", false);
        data.put("generatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        data.put("metrics", buildMetrics(warnings));
        data.put("roleTypeDistribution", safeRows(roleTypeDistributionSql(), warnings, "roleTypeDistribution"));
        data.put("unassignedUsers", safeRows(unassignedUsersSql(), warnings, "unassignedUsers"));
        data.put("multiPortalUsers", safeRows(multiPortalUsersSql(), warnings, "multiPortalUsers"));
        data.put("riskReviewStats", safeRows(riskStatsSql(), warnings, "riskReviewStats"));
        data.put("customerClaimStatuses", safeRows("SELECT status, COUNT(*) AS count FROM cus_claim_request GROUP BY status ORDER BY status", warnings, "customerClaimStatuses"));
        data.put("customerDraftStatuses", safeRows("SELECT status, COUNT(*) AS count FROM cus_entrust_draft GROUP BY status ORDER BY status", warnings, "customerDraftStatuses"));
        data.put("validationMatrix", validationMatrix());
        data.put("warnings", warnings);
        return ResultUtil.success(data);
    }

    @GetMapping("/risk-accounts")
    public Result riskAccounts(@RequestParam(required = false, defaultValue = "ALL") String type,
                               @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                               @RequestParam(required = false, defaultValue = "20") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String reviewStatus) {
        String riskType = normalize(type);
        String normalizedStatus = normalize(reviewStatus);
        if (!"ALL".equals(riskType) && !RISK_TYPES.contains(riskType)) {
            return ResultUtil.error(400, "风险类型无效");
        }
        if (hasText(normalizedStatus) && !REVIEW_QUERY_STATUSES.contains(normalizedStatus)) {
            return ResultUtil.error(400, "确认状态无效");
        }

        int current = Math.max(pageNum == null ? 1 : pageNum, 1);
        int size = Math.min(Math.max(pageSize == null ? 20 : pageSize, 1), 100);
        int offset = (current - 1) * size;

        List<Object> params = new ArrayList<>();
        String whereSql = riskFilterSql(riskType, normalizedStatus, keyword, params);
        String baseSql = riskAccountBaseSql();

        try {
            Long total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM (" + baseSql + ") risk " + whereSql,
                    Long.class,
                    params.toArray());

            List<Object> rowParams = new ArrayList<>(params);
            rowParams.add(size);
            rowParams.add(offset);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT risk.* FROM (" + baseSql + ") risk " + whereSql +
                            "ORDER BY FIELD(risk.reviewStatus,'PENDING','DEFERRED','CONFIRMED','IGNORE'), " +
                            "risk.riskType ASC, CAST(risk.userId AS UNSIGNED) DESC " +
                            "LIMIT ? OFFSET ?",
                    rowParams.toArray());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pageNum", current);
            data.put("pageSize", size);
            data.put("total", total == null ? 0L : total);
            data.put("rows", rows);
            data.put("stats", jdbcTemplate.queryForList(riskStatsSql()));
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.error(500, "读取迁移清单失败：" + e.getMessage());
        }
    }

    @PostMapping("/risk-accounts/review")
    public Result reviewRiskAccount(@RequestBody Map<String, Object> payload) {
        String riskType = normalize(valueAsString(payload.get("riskType")));
        String targetId = valueAsString(payload.get("targetId"));
        String reviewStatus = normalize(valueAsString(payload.get("reviewStatus")));
        String reviewRemark = valueAsString(payload.get("reviewRemark"));

        if (!RISK_TYPES.contains(riskType)) {
            return ResultUtil.error(400, "风险类型无效");
        }
        if (!hasText(targetId)) {
            return ResultUtil.error(400, "确认对象不能为空");
        }
        if (!REVIEW_WRITE_STATUSES.contains(reviewStatus)) {
            return ResultUtil.error(400, "确认状态只能是 CONFIRMED、DEFERRED 或 IGNORE");
        }
        if (!riskExists(riskType, targetId)) {
            return ResultUtil.error(404, "风险账号不存在或已不在迁移清单中");
        }

        SysUserEntity currentUser = currentUser();
        Long reviewUserId = currentUser == null ? null : currentUser.getUserId();
        String reviewUserName = currentUserName(currentUser);

        jdbcTemplate.update(
                "INSERT INTO portal_readiness_review " +
                        "(risk_type, target_id, review_status, review_remark, review_user_id, review_user_name, review_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "review_status = VALUES(review_status), " +
                        "review_remark = VALUES(review_remark), " +
                        "review_user_id = VALUES(review_user_id), " +
                        "review_user_name = VALUES(review_user_name), " +
                        "review_time = NOW(), " +
                        "update_time = NOW()",
                riskType,
                targetId,
                reviewStatus,
                reviewRemark,
                reviewUserId,
                reviewUserName);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("riskType", riskType);
        data.put("targetId", targetId);
        data.put("reviewStatus", reviewStatus);
        data.put("reviewUserName", reviewUserName);
        data.put("reviewTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
                "GROUP_CONCAT(DISTINCT r.role_type ORDER BY r.role_type SEPARATOR ',') AS roleTypes " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "JOIN sys_role r ON r.role_id = ur.role_id " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "GROUP BY u.user_id, u.username, u.name " +
                "HAVING COUNT(DISTINCT r.role_type) > 1 " +
                "ORDER BY u.user_id DESC LIMIT 20";
    }

    private String riskAccountBaseSql() {
        return "SELECT 'UNASSIGNED_PORTAL' AS riskType, CAST(u.user_id AS CHAR) AS targetId, " +
                "CAST(u.user_id AS CHAR) AS userId, u.username, u.name, u.mobile, u.department AS departmentId, " +
                "NULL AS roleTypes, NULL AS roleNames, 'HIGH' AS riskLevel, " +
                "'assign_portal_role_or_confirm_block' AS suggestion, " +
                "COALESCE(pr.review_status,'PENDING') AS reviewStatus, pr.review_remark AS reviewRemark, " +
                "pr.review_user_name AS reviewUserName, DATE_FORMAT(pr.review_time, '%Y-%m-%d %H:%i:%s') AS reviewTime " +
                "FROM sys_user u " +
                "LEFT JOIN portal_readiness_review pr ON pr.risk_type = 'UNASSIGNED_PORTAL' AND pr.target_id = CAST(u.user_id AS CHAR) " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM sys_user_role ur " +
                "  JOIN sys_role r ON r.role_id = ur.role_id " +
                "  WHERE ur.user_id = u.user_id " +
                "    AND r.role_type IS NOT NULL AND r.role_type <> ''" +
                ") " +
                "UNION ALL " +
                "SELECT 'MULTI_PORTAL' AS riskType, CAST(u.user_id AS CHAR) AS targetId, " +
                "CAST(u.user_id AS CHAR) AS userId, u.username, u.name, u.mobile, u.department AS departmentId, " +
                "GROUP_CONCAT(DISTINCT r.role_type ORDER BY r.role_type SEPARATOR ',') AS roleTypes, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name SEPARATOR ',') AS roleNames, 'MEDIUM' AS riskLevel, " +
                "'confirm_primary_portal_and_switching_path' AS suggestion, " +
                "COALESCE(pr.review_status,'PENDING') AS reviewStatus, pr.review_remark AS reviewRemark, " +
                "pr.review_user_name AS reviewUserName, DATE_FORMAT(pr.review_time, '%Y-%m-%d %H:%i:%s') AS reviewTime " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "JOIN sys_role r ON r.role_id = ur.role_id " +
                "LEFT JOIN portal_readiness_review pr ON pr.risk_type = 'MULTI_PORTAL' AND pr.target_id = CAST(u.user_id AS CHAR) " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "GROUP BY u.user_id, u.username, u.name, u.mobile, u.department, pr.review_status, pr.review_remark, pr.review_user_name, pr.review_time " +
                "HAVING COUNT(DISTINCT r.role_type) > 1";
    }

    private String riskFilterSql(String riskType, String reviewStatus, String keyword, List<Object> params) {
        StringBuilder sql = new StringBuilder("WHERE 1 = 1 ");
        if (!"ALL".equals(riskType)) {
            sql.append("AND risk.riskType = ? ");
            params.add(riskType);
        }
        if (hasText(reviewStatus)) {
            sql.append("AND risk.reviewStatus = ? ");
            params.add(reviewStatus);
        }
        if (hasText(keyword)) {
            String likeKeyword = "%" + keyword.trim() + "%";
            sql.append("AND (risk.userId LIKE ? OR risk.username LIKE ? OR risk.name LIKE ? OR risk.mobile LIKE ? OR risk.roleTypes LIKE ? OR risk.roleNames LIKE ?) ");
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        return sql.toString();
    }

    private String riskStatsSql() {
        return "SELECT risk.riskType, risk.reviewStatus, COUNT(*) AS count " +
                "FROM (" + riskAccountBaseSql() + ") risk " +
                "GROUP BY risk.riskType, risk.reviewStatus " +
                "ORDER BY risk.riskType ASC, FIELD(risk.reviewStatus,'PENDING','DEFERRED','CONFIRMED','IGNORE')";
    }

    private boolean riskExists(String riskType, String targetId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" + riskAccountBaseSql() + ") risk WHERE risk.riskType = ? AND risk.targetId = ?",
                Long.class,
                riskType,
                targetId);
        return count != null && count > 0;
    }

    private List<Map<String, Object>> validationMatrix() {
        return Arrays.asList(
                validation("loginRoleMatrix", "pending", "manual"),
                validation("multiRoleSwitch", "pending", "manual"),
                validation("funcPermissionRegression", "pending", "manual"),
                validation("customerReportOwnership", "passed", "automated"),
                validation("customerTokenGuard", "passed", "automated"),
                validation("softBlockPaths", "pending", "manual"),
                validation("customerClaimSampling", "pending", "manual"),
                validation("riskAccountChecklist", "pending", "manual")
        );
    }

    private Map<String, Object> validation(String key, String status, String method) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("status", status);
        item.put("method", method);
        return item;
    }

    private SysUserEntity currentUser() {
        try {
            return ShiroUtils.getUserInfo();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentUserName(SysUserEntity user) {
        if (user == null) {
            return null;
        }
        if (hasText(user.getName())) {
            return user.getName().trim();
        }
        return hasText(user.getUsername()) ? user.getUsername().trim() : null;
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim().toUpperCase() : "";
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
