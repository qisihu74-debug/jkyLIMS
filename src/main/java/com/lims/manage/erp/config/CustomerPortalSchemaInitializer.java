package com.lims.manage.erp.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CustomerPortalSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public CustomerPortalSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `cus_account` (" +
                "`account_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户账号ID'," +
                "`mobile` varchar(32) NOT NULL COMMENT '登录手机号'," +
                "`name` varchar(100) NOT NULL COMMENT '客户姓名'," +
                "`password` varchar(128) NOT NULL COMMENT '加盐密码'," +
                "`salt` varchar(50) NOT NULL COMMENT '盐值'," +
                "`state` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL正常 CLAIMED已认领 PROHIBIT停用'," +
                "`bind_company_id` int DEFAULT NULL COMMENT '认领绑定的test_company.company_id'," +
                "`bind_customer_id` int DEFAULT NULL COMMENT '认领绑定的test_customer.customer_id'," +
                "`last_token` varchar(80) DEFAULT NULL COMMENT '客户最近一次登录token'," +
                "`token_expire_time` datetime DEFAULT NULL COMMENT 'token过期时间'," +
                "`last_login_time` datetime DEFAULT NULL COMMENT '最近登录时间'," +
                "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "PRIMARY KEY (`account_id`)," +
                "UNIQUE KEY `uk_cus_account_mobile` (`mobile`)," +
                "KEY `idx_cus_account_token` (`last_token`)," +
                "KEY `idx_cus_account_bind_company` (`bind_company_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户账号表'");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `cus_claim_request` (" +
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '认领申请ID'," +
                "`account_id` bigint NOT NULL COMMENT '客户账号ID'," +
                "`candidate_company_id` int NOT NULL COMMENT '候选单位test_company.company_id'," +
                "`candidate_customer_id` int DEFAULT NULL COMMENT '候选联系人test_customer.customer_id'," +
                "`match_basis` varchar(255) DEFAULT NULL COMMENT '匹配依据说明'," +
                "`status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 APPROVED通过 REJECTED驳回'," +
                "`apply_remark` varchar(500) DEFAULT NULL COMMENT '客户申请备注'," +
                "`review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注'," +
                "`review_user_id` bigint DEFAULT NULL COMMENT '审核内部用户ID'," +
                "`review_time` datetime DEFAULT NULL COMMENT '审核时间'," +
                "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'," +
                "`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "PRIMARY KEY (`id`)," +
                "KEY `idx_cus_claim_account_status` (`account_id`, `status`)," +
                "KEY `idx_cus_claim_company` (`candidate_company_id`)," +
                "KEY `idx_cus_claim_status` (`status`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户历史客户认领申请表'");
    }
}
