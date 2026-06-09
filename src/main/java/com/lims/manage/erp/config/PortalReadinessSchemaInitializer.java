package com.lims.manage.erp.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PortalReadinessSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public PortalReadinessSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `portal_readiness_review` (" +
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT 'readiness review id'," +
                "`risk_type` varchar(40) NOT NULL COMMENT 'risk type'," +
                "`target_id` varchar(64) NOT NULL COMMENT 'target id'," +
                "`review_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'CONFIRMED DEFERRED IGNORE'," +
                "`review_remark` varchar(500) DEFAULT NULL COMMENT 'review remark'," +
                "`review_user_id` bigint DEFAULT NULL COMMENT 'review user id'," +
                "`review_user_name` varchar(100) DEFAULT NULL COMMENT 'review user name'," +
                "`review_time` datetime DEFAULT NULL COMMENT 'review time'," +
                "`follow_owner` varchar(100) DEFAULT NULL COMMENT 'follow owner'," +
                "`follow_due_date` varchar(20) DEFAULT NULL COMMENT 'follow due date'," +
                "`follow_status` varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN DONE BLOCKED'," +
                "`follow_remark` varchar(500) DEFAULT NULL COMMENT 'follow remark'," +
                "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time'," +
                "`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time'," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_portal_readiness_target` (`risk_type`, `target_id`)," +
                "KEY `idx_portal_readiness_status` (`review_status`)," +
                "KEY `idx_portal_readiness_follow_status` (`follow_status`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='portal readiness manual review'");

        ensureColumn("portal_readiness_review", "follow_owner",
                "`follow_owner` varchar(100) DEFAULT NULL COMMENT 'follow owner'");
        ensureColumn("portal_readiness_review", "follow_due_date",
                "`follow_due_date` varchar(20) DEFAULT NULL COMMENT 'follow due date'");
        ensureColumn("portal_readiness_review", "follow_status",
                "`follow_status` varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN DONE BLOCKED'");
        ensureColumn("portal_readiness_review", "follow_remark",
                "`follow_remark` varchar(500) DEFAULT NULL COMMENT 'follow remark'");
        ensureIndex("portal_readiness_review", "idx_portal_readiness_follow_status",
                "KEY `idx_portal_readiness_follow_status` (`follow_status`)");
    }

    private void ensureColumn(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN " + definition);
        }
    }

    private void ensureIndex(String tableName, String indexName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                tableName,
                indexName);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD " + definition);
        }
    }
}
