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
    private static final List<String> FOLLOW_STATUSES = Arrays.asList("OPEN", "DONE", "BLOCKED");
    private static final List<String> WORKLIST_SCOPES = Arrays.asList("ACTIONABLE", "ALL", "PENDING", "DEFERRED", "CONFIRMED", "IGNORE");

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

    @GetMapping("/review-worklist")
    public Result reviewWorklist(@RequestParam(required = false, defaultValue = "ACTIONABLE") String scope,
                                 @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                 @RequestParam(required = false, defaultValue = "20") Integer pageSize,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String followOwner,
                                 @RequestParam(required = false) String followStatus) {
        String normalizedScope = normalize(scope);
        if (!hasText(normalizedScope)) {
            normalizedScope = "ACTIONABLE";
        }
        String normalizedFollowStatus = normalize(followStatus);
        if (!WORKLIST_SCOPES.contains(normalizedScope)) {
            return ResultUtil.error(400, "worklist scope invalid");
        }
        if (hasText(normalizedFollowStatus) && !FOLLOW_STATUSES.contains(normalizedFollowStatus)) {
            return ResultUtil.error(400, "follow status invalid");
        }

        int current = Math.max(pageNum == null ? 1 : pageNum, 1);
        int size = Math.min(Math.max(pageSize == null ? 20 : pageSize, 1), 100);
        int offset = (current - 1) * size;

        List<Object> params = new ArrayList<>();
        String whereSql = worklistFilterSql(normalizedScope, followOwner, normalizedFollowStatus, keyword, params);
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
                            "FIELD(risk.followStatus,'BLOCKED','OPEN','DONE'), risk.riskType ASC, CAST(risk.userId AS UNSIGNED) DESC " +
                            "LIMIT ? OFFSET ?",
                    rowParams.toArray());

            List<Map<String, Object>> stats = jdbcTemplate.queryForList(
                    "SELECT risk.reviewStatus, risk.followStatus, COUNT(*) AS count " +
                            "FROM (" + baseSql + ") risk " + whereSql +
                            "GROUP BY risk.reviewStatus, risk.followStatus " +
                            "ORDER BY FIELD(risk.reviewStatus,'PENDING','DEFERRED','CONFIRMED','IGNORE'), " +
                            "FIELD(risk.followStatus,'BLOCKED','OPEN','DONE')",
                    params.toArray());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("scope", normalizedScope);
            data.put("pageNum", current);
            data.put("pageSize", size);
            data.put("total", total == null ? 0L : total);
            data.put("rows", rows);
            data.put("stats", stats);
            data.put("readOnlyPermissionTables", true);
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.error(500, "read review worklist failed: " + e.getMessage());
        }
    }

    @PostMapping("/review-worklist/assign")
    public Result assignReviewWork(@RequestBody Map<String, Object> payload) {
        String riskType = normalize(valueAsString(payload.get("riskType")));
        String targetId = valueAsString(payload.get("targetId"));
        String followOwner = limit(valueAsString(payload.get("followOwner")), 100);
        String followDueDate = limit(valueAsString(payload.get("followDueDate")), 20);
        String followStatus = normalize(valueAsString(payload.get("followStatus")));
        String followRemark = limit(valueAsString(payload.get("followRemark")), 500);
        if (!hasText(followStatus)) {
            followStatus = "OPEN";
        }

        if (!RISK_TYPES.contains(riskType)) {
            return ResultUtil.error(400, "risk type invalid");
        }
        if (!hasText(targetId)) {
            return ResultUtil.error(400, "target id required");
        }
        if (!FOLLOW_STATUSES.contains(followStatus)) {
            return ResultUtil.error(400, "follow status invalid");
        }
        if (!riskExists(riskType, targetId)) {
            return ResultUtil.error(404, "risk account not found");
        }

        jdbcTemplate.update(
                "INSERT INTO portal_readiness_review " +
                        "(risk_type, target_id, review_status, follow_owner, follow_due_date, follow_status, follow_remark) " +
                        "VALUES (?, ?, 'PENDING', ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "follow_owner = VALUES(follow_owner), " +
                        "follow_due_date = VALUES(follow_due_date), " +
                        "follow_status = VALUES(follow_status), " +
                        "follow_remark = VALUES(follow_remark), " +
                        "update_time = NOW()",
                riskType,
                targetId,
                followOwner,
                followDueDate,
                followStatus,
                followRemark);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("riskType", riskType);
        data.put("targetId", targetId);
        data.put("followOwner", followOwner);
        data.put("followDueDate", followDueDate);
        data.put("followStatus", followStatus);
        data.put("followRemark", followRemark);
        data.put("permissionWriteEnabled", false);
        return ResultUtil.success(data);
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

    @GetMapping("/migration-draft")
    public Result migrationDraft() {
        try {
            String generatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fileTimestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT risk.* FROM (" + riskAccountBaseSql() + ") risk " +
                            "ORDER BY risk.riskType ASC, FIELD(risk.reviewStatus,'PENDING','DEFERRED','CONFIRMED','IGNORE'), CAST(risk.userId AS UNSIGNED) DESC");
            List<Map<String, Object>> stats = jdbcTemplate.queryForList(riskStatsSql());

            long riskTotal = rows.size();
            long pendingCount = countRows(rows, null, "PENDING");
            long deferredCount = countRows(rows, null, "DEFERRED");
            long confirmedCount = countRows(rows, null, "CONFIRMED");
            long ignoredCount = countRows(rows, null, "IGNORE");
            long confirmedUnassignedCount = countRows(rows, "UNASSIGNED_PORTAL", "CONFIRMED");
            long confirmedMultiCount = countRows(rows, "MULTI_PORTAL", "CONFIRMED");
            long blockingCount = pendingCount + deferredCount;
            boolean readyForRehearsal = riskTotal > 0 && blockingCount == 0;

            String sqlDraft = buildMigrationSqlDraft(rows, generatedAt, readyForRehearsal);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("phase", "phase6-migration-draft");
            data.put("generatedAt", generatedAt);
            data.put("dryRun", true);
            data.put("executable", false);
            data.put("migrationWriteEnabled", false);
            data.put("readyForRehearsal", readyForRehearsal);
            data.put("riskTotal", riskTotal);
            data.put("blockingRiskCount", blockingCount);
            data.put("pendingRiskCount", pendingCount);
            data.put("deferredRiskCount", deferredCount);
            data.put("confirmedRiskCount", confirmedCount);
            data.put("ignoredRiskCount", ignoredCount);
            data.put("confirmedUnassignedCount", confirmedUnassignedCount);
            data.put("confirmedMultiCount", confirmedMultiCount);
            data.put("stats", stats);
            data.put("checklist", migrationChecklist(readyForRehearsal, riskTotal, blockingCount));
            data.put("sqlDraft", sqlDraft);
            data.put("sqlDraftLineCount", sqlDraft.split("\\n").length);
            data.put("fileName", "portal-migration-draft-" + fileTimestamp + ".sql");
            data.put("notes", Arrays.asList(
                    "本接口只生成迁移预案和 SQL 草稿，不执行任何权限表写入。",
                    "草稿中的 INSERT/UPDATE/DELETE/CREATE TABLE 语句均以注释形式输出。",
                    "PENDING 或 DEFERRED 风险项清零前，不建议进入 staging 迁移演练。"
            ));
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.error(500, "生成迁移预案失败：" + e.getMessage());
        }
    }

    @GetMapping("/rehearsal-task-draft")
    public Result rehearsalTaskDraft() {
        try {
            String generatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String fileTimestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT risk.* FROM (" + riskAccountBaseSql() + ") risk " +
                            "ORDER BY FIELD(risk.reviewStatus,'PENDING','DEFERRED','CONFIRMED','IGNORE'), " +
                            "FIELD(risk.followStatus,'BLOCKED','OPEN','DONE'), risk.riskType ASC, CAST(risk.userId AS UNSIGNED) DESC");

            long riskTotal = rows.size();
            long pendingCount = countRows(rows, null, "PENDING");
            long deferredCount = countRows(rows, null, "DEFERRED");
            long confirmedCount = countRows(rows, null, "CONFIRMED");
            long ignoredCount = countRows(rows, null, "IGNORE");
            long blockingCount = pendingCount + deferredCount;
            boolean readyForRehearsal = riskTotal > 0 && blockingCount == 0;

            List<Map<String, Object>> taskItems = buildRehearsalTaskItems(rows);
            List<Map<String, Object>> ownerSummary = buildRehearsalOwnerSummary(taskItems);
            List<Map<String, Object>> signoffChecklist = rehearsalSignoffChecklist(taskItems, readyForRehearsal, blockingCount);
            String csvDraft = buildRehearsalCsvDraft(taskItems);
            String textDraft = buildRehearsalTextDraft(taskItems, signoffChecklist, generatedAt, readyForRehearsal,
                    riskTotal, pendingCount, deferredCount, confirmedCount, ignoredCount);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("phase", "phase6-rehearsal-task-draft");
            data.put("generatedAt", generatedAt);
            data.put("dryRun", true);
            data.put("executable", false);
            data.put("migrationWriteEnabled", false);
            data.put("permissionWriteEnabled", false);
            data.put("readyForRehearsal", readyForRehearsal);
            data.put("riskTotal", riskTotal);
            data.put("blockingRiskCount", blockingCount);
            data.put("pendingRiskCount", pendingCount);
            data.put("deferredRiskCount", deferredCount);
            data.put("confirmedRiskCount", confirmedCount);
            data.put("ignoredRiskCount", ignoredCount);
            data.put("taskItems", taskItems);
            data.put("ownerSummary", ownerSummary);
            data.put("signoffChecklist", signoffChecklist);
            data.put("csvDraft", csvDraft);
            data.put("textDraft", textDraft);
            data.put("csvFileName", "portal-rehearsal-task-draft-" + fileTimestamp + ".csv");
            data.put("textFileName", "portal-rehearsal-signoff-draft-" + fileTimestamp + ".md");
            data.put("notes", Arrays.asList(
                    "This endpoint only generates rehearsal task and signoff drafts.",
                    "It does not update sys_user_role or any permission table.",
                    "Keep all PENDING/DEFERRED rows blocked until business signoff is complete."
            ));
            return ResultUtil.success(data);
        } catch (Exception e) {
            return ResultUtil.error(500, "generate rehearsal task draft failed: " + e.getMessage());
        }
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
                "pr.review_user_name AS reviewUserName, DATE_FORMAT(pr.review_time, '%Y-%m-%d %H:%i:%s') AS reviewTime, " +
                "pr.follow_owner AS followOwner, pr.follow_due_date AS followDueDate, " +
                "COALESCE(pr.follow_status,'OPEN') AS followStatus, pr.follow_remark AS followRemark " +
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
                "pr.review_user_name AS reviewUserName, DATE_FORMAT(pr.review_time, '%Y-%m-%d %H:%i:%s') AS reviewTime, " +
                "pr.follow_owner AS followOwner, pr.follow_due_date AS followDueDate, " +
                "COALESCE(pr.follow_status,'OPEN') AS followStatus, pr.follow_remark AS followRemark " +
                "FROM sys_user u " +
                "JOIN sys_user_role ur ON ur.user_id = u.user_id " +
                "JOIN sys_role r ON r.role_id = ur.role_id " +
                "LEFT JOIN portal_readiness_review pr ON pr.risk_type = 'MULTI_PORTAL' AND pr.target_id = CAST(u.user_id AS CHAR) " +
                "WHERE " + ACTIVE_USER_FILTER + " " +
                "AND r.role_type IS NOT NULL AND r.role_type <> '' " +
                "GROUP BY u.user_id, u.username, u.name, u.mobile, u.department, pr.review_status, pr.review_remark, pr.review_user_name, pr.review_time, " +
                "pr.follow_owner, pr.follow_due_date, pr.follow_status, pr.follow_remark " +
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

    private String worklistFilterSql(String scope,
                                     String followOwner,
                                     String followStatus,
                                     String keyword,
                                     List<Object> params) {
        StringBuilder sql = new StringBuilder("WHERE 1 = 1 ");
        if ("ACTIONABLE".equals(scope)) {
            sql.append("AND risk.reviewStatus IN ('PENDING','DEFERRED') ");
        } else if (!"ALL".equals(scope)) {
            sql.append("AND risk.reviewStatus = ? ");
            params.add(scope);
        }
        if (hasText(followStatus)) {
            sql.append("AND risk.followStatus = ? ");
            params.add(followStatus);
        }
        if (hasText(followOwner)) {
            sql.append("AND risk.followOwner LIKE ? ");
            params.add("%" + followOwner.trim() + "%");
        }
        if (hasText(keyword)) {
            String likeKeyword = "%" + keyword.trim() + "%";
            sql.append("AND (risk.userId LIKE ? OR risk.username LIKE ? OR risk.name LIKE ? OR risk.mobile LIKE ? " +
                    "OR risk.roleTypes LIKE ? OR risk.roleNames LIKE ? OR risk.followOwner LIKE ? OR risk.followRemark LIKE ?) ");
            params.add(likeKeyword);
            params.add(likeKeyword);
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
                validation("riskAccountChecklist", "pending", "manual"),
                validation("migrationDraftExport", "pending", "manual"),
                validation("riskReviewWorklist", "pending", "manual"),
                validation("rehearsalTaskDraft", "pending", "manual")
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

    private String limit(String value, int maxLength) {
        if (!hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private long countRows(List<Map<String, Object>> rows, String riskType, String reviewStatus) {
        long count = 0L;
        for (Map<String, Object> row : rows) {
            if (hasText(riskType) && !riskType.equals(valueAsString(row.get("riskType")))) {
                continue;
            }
            if (hasText(reviewStatus) && !reviewStatus.equals(valueAsString(row.get("reviewStatus")))) {
                continue;
            }
            count++;
        }
        return count;
    }

    private List<Map<String, Object>> migrationChecklist(boolean readyForRehearsal, long riskTotal, long blockingCount) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(checkItem("riskListExists", riskTotal > 0, "风险清单已生成：" + riskTotal + " 条"));
        items.add(checkItem("allRisksReviewed", blockingCount == 0, blockingCount == 0 ? "风险项已无待确认/暂缓" : "仍有 " + blockingCount + " 条待确认或暂缓"));
        items.add(checkItem("sqlDraftOnly", true, "仅生成 SQL 草稿，不执行权限表写入"));
        items.add(checkItem("businessSignoff", false, "需业务负责人确认风险清单和迁移口径"));
        items.add(checkItem("backupPlan", false, "正式演练前需人工准备 sys_user_role 备份"));
        items.add(checkItem("stagingRehearsal", readyForRehearsal, readyForRehearsal ? "可进入 staging 演练评审" : "风险清单确认完成后再进入 staging 演练"));
        return items;
    }

    private Map<String, Object> checkItem(String key, boolean passed, String message) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("passed", passed);
        item.put("status", passed ? "passed" : "pending");
        item.put("message", message);
        return item;
    }

    private String buildMigrationSqlDraft(List<Map<String, Object>> rows, String generatedAt, boolean readyForRehearsal) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- jkyLIMS phase6 portal migration draft\n");
        sql.append("-- generated_at: ").append(generatedAt).append("\n");
        sql.append("-- dry_run_only: true\n");
        sql.append("-- executable: false\n");
        sql.append("-- IMPORTANT: all mutating statements are intentionally commented out.\n");
        sql.append("-- Running this file as-is must not change sys_user_role or any permission table.\n\n");

        sql.append("SELECT 'risk_total' AS item, COUNT(*) AS value FROM (\n");
        sql.append(riskAccountBaseSql()).append("\n");
        sql.append(") risk;\n\n");

        sql.append("SELECT risk.riskType, risk.reviewStatus, COUNT(*) AS count FROM (\n");
        sql.append(riskAccountBaseSql()).append("\n");
        sql.append(") risk GROUP BY risk.riskType, risk.reviewStatus ORDER BY risk.riskType, risk.reviewStatus;\n\n");

        sql.append("-- Gate: ready_for_staging_rehearsal = ").append(readyForRehearsal).append("\n");
        sql.append("-- Do not uncomment any mutating SQL before all PENDING/DEFERRED rows are resolved and signed off.\n\n");

        sql.append("-- 1. Manual backup draft. Keep commented until the rehearsal window is approved.\n");
        sql.append("-- CREATE TABLE sys_user_role_bak_phase6_").append(new SimpleDateFormat("yyyyMMdd").format(new Date())).append(" AS SELECT * FROM sys_user_role;\n\n");

        appendBlockers(sql, rows);
        appendConfirmedUnassignedDraft(sql, rows);
        appendConfirmedMultiPortalDraft(sql, rows);
        appendIgnoredDraft(sql, rows);

        sql.append("-- End of dry-run draft. No executable mutation is included.\n");
        return sql.toString();
    }

    private void appendBlockers(StringBuilder sql, List<Map<String, Object>> rows) {
        sql.append("-- 2. Blocking rows: PENDING or DEFERRED must be handled before rehearsal.\n");
        for (Map<String, Object> row : rows) {
            String status = valueAsString(row.get("reviewStatus"));
            if (!"PENDING".equals(status) && !"DEFERRED".equals(status)) {
                continue;
            }
            sql.append("-- BLOCKED ")
                    .append("status=").append(status)
                    .append(", riskType=").append(commentValue(row.get("riskType")))
                    .append(", user_id=").append(commentValue(row.get("userId")))
                    .append(", username=").append(commentValue(row.get("username")))
                    .append(", name=").append(commentValue(row.get("name")))
                    .append(", roleTypes=").append(commentValue(row.get("roleTypes")))
                    .append(", remark=").append(commentValue(row.get("reviewRemark")))
                    .append("\n");
        }
        sql.append("\n");
    }

    private void appendConfirmedUnassignedDraft(StringBuilder sql, List<Map<String, Object>> rows) {
        sql.append("-- 3. Confirmed UNASSIGNED_PORTAL rows.\n");
        sql.append("-- Each row needs a business-selected target role_id before any migration rehearsal.\n");
        for (Map<String, Object> row : rows) {
            if (!"UNASSIGNED_PORTAL".equals(valueAsString(row.get("riskType"))) || !"CONFIRMED".equals(valueAsString(row.get("reviewStatus")))) {
                continue;
            }
            String userId = commentValue(row.get("userId"));
            sql.append("-- user_id=").append(userId)
                    .append(", username=").append(commentValue(row.get("username")))
                    .append(", name=").append(commentValue(row.get("name")))
                    .append(", reviewed_by=").append(commentValue(row.get("reviewUserName")))
                    .append("\n");
            sql.append("-- INSERT INTO sys_user_role (user_id, role_id) VALUES (")
                    .append(userId)
                    .append(", <target_role_id>);\n");
        }
        sql.append("\n");
    }

    private void appendConfirmedMultiPortalDraft(StringBuilder sql, List<Map<String, Object>> rows) {
        sql.append("-- 4. Confirmed MULTI_PORTAL rows.\n");
        sql.append("-- Default action is no sys_user_role mutation; verify portal switch behavior and default portal separately.\n");
        for (Map<String, Object> row : rows) {
            if (!"MULTI_PORTAL".equals(valueAsString(row.get("riskType"))) || !"CONFIRMED".equals(valueAsString(row.get("reviewStatus")))) {
                continue;
            }
            sql.append("-- MULTI_PORTAL no-op user_id=").append(commentValue(row.get("userId")))
                    .append(", username=").append(commentValue(row.get("username")))
                    .append(", name=").append(commentValue(row.get("name")))
                    .append(", roleTypes=").append(commentValue(row.get("roleTypes")))
                    .append(", roleNames=").append(commentValue(row.get("roleNames")))
                    .append("\n");
        }
        sql.append("\n");
    }

    private void appendIgnoredDraft(StringBuilder sql, List<Map<String, Object>> rows) {
        sql.append("-- 5. IGNORE rows. These are explicitly excluded from migration draft mutations.\n");
        for (Map<String, Object> row : rows) {
            if (!"IGNORE".equals(valueAsString(row.get("reviewStatus")))) {
                continue;
            }
            sql.append("-- IGNORE riskType=").append(commentValue(row.get("riskType")))
                    .append(", user_id=").append(commentValue(row.get("userId")))
                    .append(", username=").append(commentValue(row.get("username")))
                    .append(", name=").append(commentValue(row.get("name")))
                    .append(", remark=").append(commentValue(row.get("reviewRemark")))
                    .append("\n");
        }
        sql.append("\n");
    }

    private List<Map<String, Object>> buildRehearsalTaskItems(List<Map<String, Object>> rows) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        int itemNo = 1;
        for (Map<String, Object> row : rows) {
            String riskType = valueAsString(row.get("riskType"));
            String reviewStatus = valueAsString(row.get("reviewStatus"));
            String followStatus = valueAsString(row.get("followStatus"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemNo", itemNo++);
            item.put("riskType", riskType);
            item.put("targetId", valueAsString(row.get("targetId")));
            item.put("userId", valueAsString(row.get("userId")));
            item.put("username", valueAsString(row.get("username")));
            item.put("name", valueAsString(row.get("name")));
            item.put("mobile", valueAsString(row.get("mobile")));
            item.put("roleTypes", valueAsString(row.get("roleTypes")));
            item.put("roleNames", valueAsString(row.get("roleNames")));
            item.put("reviewStatus", reviewStatus);
            item.put("reviewRemark", valueAsString(row.get("reviewRemark")));
            item.put("reviewUserName", valueAsString(row.get("reviewUserName")));
            item.put("reviewTime", valueAsString(row.get("reviewTime")));
            item.put("followOwner", valueAsString(row.get("followOwner")));
            item.put("followDueDate", valueAsString(row.get("followDueDate")));
            item.put("followStatus", hasText(followStatus) ? followStatus : "OPEN");
            item.put("followRemark", valueAsString(row.get("followRemark")));
            item.put("action", rehearsalAction(riskType, reviewStatus));
            item.put("verification", rehearsalVerification(riskType, reviewStatus));
            item.put("signoffRole", rehearsalSignoffRole(riskType, reviewStatus));
            item.put("blocking", "PENDING".equals(reviewStatus) || "DEFERRED".equals(reviewStatus));
            item.put("permissionWriteEnabled", false);
            tasks.add(item);
        }
        return tasks;
    }

    private List<Map<String, Object>> buildRehearsalOwnerSummary(List<Map<String, Object>> taskItems) {
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> item : taskItems) {
            String owner = valueAsString(item.get("followOwner"));
            if (!hasText(owner)) {
                owner = "UNASSIGNED";
            }
            Map<String, Object> summary = grouped.get(owner);
            if (summary == null) {
                summary = new LinkedHashMap<>();
                summary.put("followOwner", owner);
                summary.put("total", 0L);
                summary.put("pending", 0L);
                summary.put("deferred", 0L);
                summary.put("confirmed", 0L);
                summary.put("ignored", 0L);
                summary.put("open", 0L);
                summary.put("done", 0L);
                summary.put("blocked", 0L);
                grouped.put(owner, summary);
            }
            increment(summary, "total");
            String reviewStatus = valueAsString(item.get("reviewStatus"));
            if ("PENDING".equals(reviewStatus)) {
                increment(summary, "pending");
            } else if ("DEFERRED".equals(reviewStatus)) {
                increment(summary, "deferred");
            } else if ("CONFIRMED".equals(reviewStatus)) {
                increment(summary, "confirmed");
            } else if ("IGNORE".equals(reviewStatus)) {
                increment(summary, "ignored");
            }
            String followStatus = valueAsString(item.get("followStatus"));
            if ("DONE".equals(followStatus)) {
                increment(summary, "done");
            } else if ("BLOCKED".equals(followStatus)) {
                increment(summary, "blocked");
            } else {
                increment(summary, "open");
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private List<Map<String, Object>> rehearsalSignoffChecklist(List<Map<String, Object>> taskItems,
                                                               boolean readyForRehearsal,
                                                               long blockingCount) {
        long blockingWithoutOwner = 0L;
        long confirmedWithoutReviewer = 0L;
        for (Map<String, Object> item : taskItems) {
            String reviewStatus = valueAsString(item.get("reviewStatus"));
            if (("PENDING".equals(reviewStatus) || "DEFERRED".equals(reviewStatus)) && !hasText(valueAsString(item.get("followOwner")))) {
                blockingWithoutOwner++;
            }
            if (("CONFIRMED".equals(reviewStatus) || "IGNORE".equals(reviewStatus)) && !hasText(valueAsString(item.get("reviewUserName")))) {
                confirmedWithoutReviewer++;
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(checkItem("permissionWriteDisabled", true, "Draft only; permission table write switches remain disabled."));
        items.add(checkItem("allRisksReviewed", readyForRehearsal, blockingCount == 0 ? "No PENDING/DEFERRED risk remains." : blockingCount + " PENDING/DEFERRED risks still block rehearsal."));
        items.add(checkItem("blockingOwnersAssigned", blockingWithoutOwner == 0, blockingWithoutOwner == 0 ? "All blocking rows have follow owners or no blocking row exists." : blockingWithoutOwner + " blocking rows have no follow owner."));
        items.add(checkItem("reviewerTraceComplete", confirmedWithoutReviewer == 0, confirmedWithoutReviewer == 0 ? "Reviewed rows have reviewer trace or no reviewed row exists." : confirmedWithoutReviewer + " reviewed rows have no reviewer trace."));
        items.add(checkItem("businessSignoff", false, "Business owner must sign the task list and exclusion list."));
        items.add(checkItem("backupConfirmed", false, "DBA/operator must confirm sys_user_role backup before any rehearsal."));
        items.add(checkItem("rehearsalWindowConfirmed", false, "Owner, time window, rollback contact and communication plan must be confirmed."));
        return items;
    }

    private String buildRehearsalCsvDraft(List<Map<String, Object>> taskItems) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine(Arrays.asList("itemNo", "riskType", "userId", "username", "name", "mobile", "roleTypes",
                "reviewStatus", "followStatus", "followOwner", "followDueDate", "action", "verification",
                "signoffRole", "reviewRemark", "followRemark")));
        for (Map<String, Object> item : taskItems) {
            csv.append(csvLine(Arrays.asList(
                    item.get("itemNo"),
                    item.get("riskType"),
                    item.get("userId"),
                    item.get("username"),
                    item.get("name"),
                    item.get("mobile"),
                    item.get("roleTypes"),
                    item.get("reviewStatus"),
                    item.get("followStatus"),
                    item.get("followOwner"),
                    item.get("followDueDate"),
                    item.get("action"),
                    item.get("verification"),
                    item.get("signoffRole"),
                    item.get("reviewRemark"),
                    item.get("followRemark")
            )));
        }
        return csv.toString();
    }

    private String buildRehearsalTextDraft(List<Map<String, Object>> taskItems,
                                           List<Map<String, Object>> signoffChecklist,
                                           String generatedAt,
                                           boolean readyForRehearsal,
                                           long riskTotal,
                                           long pendingCount,
                                           long deferredCount,
                                           long confirmedCount,
                                           long ignoredCount) {
        StringBuilder text = new StringBuilder();
        text.append("# jkyLIMS portal migration rehearsal task draft\n\n");
        text.append("- generated_at: ").append(generatedAt).append("\n");
        text.append("- dry_run_only: true\n");
        text.append("- executable: false\n");
        text.append("- permission_write_enabled: false\n");
        text.append("- ready_for_rehearsal: ").append(readyForRehearsal).append("\n\n");
        text.append("## Summary\n\n");
        text.append("- risk_total: ").append(riskTotal).append("\n");
        text.append("- pending: ").append(pendingCount).append("\n");
        text.append("- deferred: ").append(deferredCount).append("\n");
        text.append("- confirmed: ").append(confirmedCount).append("\n");
        text.append("- ignored: ").append(ignoredCount).append("\n\n");
        text.append("## Signoff Checklist\n\n");
        for (Map<String, Object> item : signoffChecklist) {
            boolean passed = Boolean.TRUE.equals(item.get("passed"));
            text.append("- [").append(passed ? "x" : " ").append("] ")
                    .append(commentValue(item.get("key")))
                    .append(" - ")
                    .append(commentValue(item.get("message")))
                    .append("\n");
        }
        text.append("\n## Task Items\n\n");
        text.append("| # | riskType | userId | username | name | reviewStatus | followStatus | owner | due | action | signoff |\n");
        text.append("|---|---|---|---|---|---|---|---|---|---|---|\n");
        for (Map<String, Object> item : taskItems) {
            text.append("| ")
                    .append(markdownCell(item.get("itemNo"))).append(" | ")
                    .append(markdownCell(item.get("riskType"))).append(" | ")
                    .append(markdownCell(item.get("userId"))).append(" | ")
                    .append(markdownCell(item.get("username"))).append(" | ")
                    .append(markdownCell(item.get("name"))).append(" | ")
                    .append(markdownCell(item.get("reviewStatus"))).append(" | ")
                    .append(markdownCell(item.get("followStatus"))).append(" | ")
                    .append(markdownCell(item.get("followOwner"))).append(" | ")
                    .append(markdownCell(item.get("followDueDate"))).append(" | ")
                    .append(markdownCell(item.get("action"))).append(" | ")
                    .append(markdownCell(item.get("signoffRole"))).append(" |\n");
        }
        text.append("\nNo SQL execution entry is provided by this draft.\n");
        return text.toString();
    }

    private String rehearsalAction(String riskType, String reviewStatus) {
        if ("PENDING".equals(reviewStatus)) {
            return "complete_review_before_rehearsal";
        }
        if ("DEFERRED".equals(reviewStatus)) {
            return "resolve_deferred_decision_or_keep_blocked";
        }
        if ("IGNORE".equals(reviewStatus)) {
            return "document_exclusion_reason";
        }
        if ("UNASSIGNED_PORTAL".equals(riskType)) {
            return "select_target_portal_role_for_rehearsal";
        }
        if ("MULTI_PORTAL".equals(riskType)) {
            return "verify_multi_portal_switching_no_role_mutation";
        }
        return "manual_review_required";
    }

    private String rehearsalVerification(String riskType, String reviewStatus) {
        if ("PENDING".equals(reviewStatus) || "DEFERRED".equals(reviewStatus)) {
            return "business_owner_must_confirm_or_keep_blocked";
        }
        if ("IGNORE".equals(reviewStatus)) {
            return "exclusion_reason_signed";
        }
        if ("UNASSIGNED_PORTAL".equals(riskType)) {
            return "target_role_selected_and_sql_still_commented";
        }
        if ("MULTI_PORTAL".equals(riskType)) {
            return "portal_switch_regression_passed";
        }
        return "manual_verification_required";
    }

    private String rehearsalSignoffRole(String riskType, String reviewStatus) {
        if ("PENDING".equals(reviewStatus) || "DEFERRED".equals(reviewStatus)) {
            return "business_owner";
        }
        if ("IGNORE".equals(reviewStatus)) {
            return "business_owner";
        }
        if ("UNASSIGNED_PORTAL".equals(riskType)) {
            return "business_owner,system_admin";
        }
        if ("MULTI_PORTAL".equals(riskType)) {
            return "business_owner,test_owner";
        }
        return "business_owner";
    }

    private void increment(Map<String, Object> summary, String key) {
        Object value = summary.get(key);
        long current = value instanceof Number ? ((Number) value).longValue() : 0L;
        summary.put(key, current + 1L);
    }

    private String csvLine(List<?> values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(",");
            }
            line.append(csvCell(values.get(i)));
        }
        line.append("\n");
        return line.toString();
    }

    private String csvCell(Object value) {
        return "\"" + commentValue(value).replace("\"", "\"\"") + "\"";
    }

    private String markdownCell(Object value) {
        return commentValue(value).replace("|", "\\|");
    }

    private String commentValue(Object value) {
        if (value == null) {
            return "-";
        }
        return String.valueOf(value).replace("\r", " ").replace("\n", " ").replace("--", "- -").trim();
    }
}
