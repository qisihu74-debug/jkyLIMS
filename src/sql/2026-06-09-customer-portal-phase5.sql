CREATE TABLE IF NOT EXISTS `cus_account` (
  `account_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户账号ID',
  `mobile` varchar(32) NOT NULL COMMENT '登录手机号',
  `name` varchar(100) NOT NULL COMMENT '客户姓名',
  `password` varchar(128) NOT NULL COMMENT '加盐密码',
  `salt` varchar(50) NOT NULL COMMENT '盐值',
  `state` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL正常 CLAIMED已认领 PROHIBIT停用',
  `bind_company_id` int DEFAULT NULL COMMENT '认领绑定的test_company.company_id',
  `bind_customer_id` int DEFAULT NULL COMMENT '认领绑定的test_customer.customer_id',
  `last_token` varchar(80) DEFAULT NULL COMMENT '客户最近一次登录token',
  `token_expire_time` datetime DEFAULT NULL COMMENT 'token过期时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最近登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_cus_account_mobile` (`mobile`),
  KEY `idx_cus_account_token` (`last_token`),
  KEY `idx_cus_account_bind_company` (`bind_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户账号表';

CREATE TABLE IF NOT EXISTS `cus_claim_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认领申请ID',
  `account_id` bigint NOT NULL COMMENT '客户账号ID',
  `candidate_company_id` int NOT NULL COMMENT '候选单位test_company.company_id',
  `candidate_customer_id` int DEFAULT NULL COMMENT '候选联系人test_customer.customer_id',
  `match_basis` varchar(255) DEFAULT NULL COMMENT '匹配依据说明',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待审 APPROVED通过 REJECTED驳回',
  `apply_remark` varchar(500) DEFAULT NULL COMMENT '客户申请备注',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `review_user_id` bigint DEFAULT NULL COMMENT '审核内部用户ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cus_claim_account_status` (`account_id`, `status`),
  KEY `idx_cus_claim_company` (`candidate_company_id`),
  KEY `idx_cus_claim_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户历史客户认领申请表';

CREATE TABLE IF NOT EXISTS `cus_entrust_draft` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户自助委托草稿ID',
  `account_id` bigint NOT NULL COMMENT '客户账号ID',
  `bind_company_id` int DEFAULT NULL COMMENT '提交时绑定的test_company.company_id',
  `bind_customer_id` int DEFAULT NULL COMMENT '提交时绑定的test_customer.customer_id',
  `draft_no` varchar(40) NOT NULL COMMENT '草稿编号',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT草稿 SUBMITTED已提交 ACCEPTED已受理 REJECTED已退回 CANCELLED已取消',
  `entrust_company` varchar(255) DEFAULT NULL COMMENT '委托单位',
  `entrust_people` varchar(100) DEFAULT NULL COMMENT '委托联系人',
  `entrust_phone` varchar(32) DEFAULT NULL COMMENT '联系人电话',
  `witness_unit` varchar(255) DEFAULT NULL COMMENT '见证单位',
  `witness_person` varchar(100) DEFAULT NULL COMMENT '见证人',
  `witness_phone` varchar(32) DEFAULT NULL COMMENT '见证人电话',
  `project_name` varchar(255) DEFAULT NULL COMMENT '工程名称',
  `project_part` varchar(255) DEFAULT NULL COMMENT '工程部位',
  `sample_names` text COMMENT '样品信息',
  `check_items` text COMMENT '检测项目',
  `request_date` varchar(20) DEFAULT NULL COMMENT '要求完成日期',
  `report_count` int DEFAULT NULL COMMENT '报告份数',
  `report_type` varchar(50) DEFAULT NULL COMMENT '取报告方式',
  `address` varchar(500) DEFAULT NULL COMMENT '邮寄地址',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `review_remark` varchar(1000) DEFAULT NULL COMMENT '内部受理备注',
  `review_user_id` bigint DEFAULT NULL COMMENT '内部受理用户ID',
  `review_user_name` varchar(100) DEFAULT NULL COMMENT '内部受理用户姓名',
  `review_time` datetime DEFAULT NULL COMMENT '内部受理时间',
  `formal_entrust_id` bigint DEFAULT NULL COMMENT '转入test_entrusted_info.id',
  `formal_entrustment_no` int DEFAULT NULL COMMENT '转入正式/预委托编号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cus_entrust_draft_no` (`draft_no`),
  KEY `idx_cus_entrust_account_status` (`account_id`, `status`),
  KEY `idx_cus_entrust_company` (`bind_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户门户自助委托草稿表';

-- 已有 cus_entrust_draft 表的环境，由 CustomerPortalSchemaInitializer 启动时自动补列。
-- 如需手工迁移，可按需执行以下语句（重复执行前需先检查字段是否已存在）：
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `review_remark` varchar(1000) DEFAULT NULL COMMENT '内部受理备注';
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `review_user_id` bigint DEFAULT NULL COMMENT '内部受理用户ID';
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `review_user_name` varchar(100) DEFAULT NULL COMMENT '内部受理用户姓名';
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `review_time` datetime DEFAULT NULL COMMENT '内部受理时间';
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `formal_entrust_id` bigint DEFAULT NULL COMMENT '转入test_entrusted_info.id';
-- ALTER TABLE `cus_entrust_draft` ADD COLUMN `formal_entrustment_no` int DEFAULT NULL COMMENT '转入正式/预委托编号';
