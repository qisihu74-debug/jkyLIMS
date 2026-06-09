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
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '上线验证确认ID'," +
                "`risk_type` varchar(40) NOT NULL COMMENT '风险类型'," +
                "`target_id` varchar(64) NOT NULL COMMENT '确认对象ID'," +
                "`review_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'CONFIRMED已确认 DEFERRED暂缓 IGNORE忽略'," +
                "`review_remark` varchar(500) DEFAULT NULL COMMENT '确认说明'," +
                "`review_user_id` bigint DEFAULT NULL COMMENT '确认人用户ID'," +
                "`review_user_name` varchar(100) DEFAULT NULL COMMENT '确认人姓名'," +
                "`review_time` datetime DEFAULT NULL COMMENT '确认时间'," +
                "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_portal_readiness_target` (`risk_type`, `target_id`)," +
                "KEY `idx_portal_readiness_status` (`review_status`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门户上线验证人工确认记录表'");
    }
}
