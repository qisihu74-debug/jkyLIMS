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

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `cus_entrust_draft` (" +
                "`id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户自助委托草稿ID'," +
                "`account_id` bigint NOT NULL COMMENT '客户账号ID'," +
                "`bind_company_id` int DEFAULT NULL COMMENT '提交时绑定的test_company.company_id'," +
                "`bind_customer_id` int DEFAULT NULL COMMENT '提交时绑定的test_customer.customer_id'," +
                "`draft_no` varchar(40) NOT NULL COMMENT '草稿编号'," +
                "`status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT草稿 SUBMITTED已提交 ACCEPTED已受理 REJECTED已退回 CANCELLED已取消'," +
                "`entrust_company` varchar(255) DEFAULT NULL COMMENT '委托单位'," +
                "`entrust_people` varchar(100) DEFAULT NULL COMMENT '委托联系人'," +
                "`entrust_phone` varchar(32) DEFAULT NULL COMMENT '联系人电话'," +
                "`witness_unit` varchar(255) DEFAULT NULL COMMENT '见证单位'," +
                "`witness_person` varchar(100) DEFAULT NULL COMMENT '见证人'," +
                "`witness_phone` varchar(32) DEFAULT NULL COMMENT '见证人电话'," +
                "`project_name` varchar(255) DEFAULT NULL COMMENT '工程名称'," +
                "`project_part` varchar(255) DEFAULT NULL COMMENT '工程部位'," +
                "`sample_names` text COMMENT '样品信息'," +
                "`check_items` text COMMENT '检测项目'," +
                "`request_date` varchar(20) DEFAULT NULL COMMENT '要求完成日期'," +
                "`report_count` int DEFAULT NULL COMMENT '报告份数'," +
                "`report_type` varchar(50) DEFAULT NULL COMMENT '取报告方式'," +
                "`address` varchar(500) DEFAULT NULL COMMENT '邮寄地址'," +
                "`remark` varchar(1000) DEFAULT NULL COMMENT '备注'," +
                "`submit_time` datetime DEFAULT NULL COMMENT '提交时间'," +
                "`review_remark` varchar(1000) DEFAULT NULL COMMENT '内部受理备注'," +
                "`review_user_id` bigint DEFAULT NULL COMMENT '内部受理用户ID'," +
                "`review_user_name` varchar(100) DEFAULT NULL COMMENT '内部受理用户姓名'," +
                "`review_time` datetime DEFAULT NULL COMMENT '内部受理时间'," +
                "`formal_entrust_id` bigint DEFAULT NULL COMMENT '转入test_entrusted_info.id'," +
                "`formal_entrustment_no` int DEFAULT NULL COMMENT '转入正式/预委托编号'," +
                "`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_cus_entrust_draft_no` (`draft_no`)," +
                "KEY `idx_cus_entrust_account_status` (`account_id`, `status`)," +
                "KEY `idx_cus_entrust_company` (`bind_company_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户自助委托草稿表'");

        ensureColumn("cus_entrust_draft", "review_remark",
                "`review_remark` varchar(1000) DEFAULT NULL COMMENT '内部受理备注'");
        ensureColumn("cus_entrust_draft", "review_user_id",
                "`review_user_id` bigint DEFAULT NULL COMMENT '内部受理用户ID'");
        ensureColumn("cus_entrust_draft", "review_user_name",
                "`review_user_name` varchar(100) DEFAULT NULL COMMENT '内部受理用户姓名'");
        ensureColumn("cus_entrust_draft", "review_time",
                "`review_time` datetime DEFAULT NULL COMMENT '内部受理时间'");
        ensureColumn("cus_entrust_draft", "formal_entrust_id",
                "`formal_entrust_id` bigint DEFAULT NULL COMMENT '转入test_entrusted_info.id'");
        ensureColumn("cus_entrust_draft", "formal_entrustment_no",
                "`formal_entrustment_no` int DEFAULT NULL COMMENT '转入正式/预委托编号'");
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
}
