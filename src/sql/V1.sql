-- 2022-06-01 create --
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `test_alert`;
CREATE TABLE `test_alert` (
  `id` bigint NOT NULL,
  `entrust_id` bigint DEFAULT NULL,
  `check_item_name` varchar(255) DEFAULT NULL,
  `describ` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 6月1号
ALTER TABLE sys_function  DROP kanban_name;

ALTER TABLE sys_function ADD kanban_name VARCHAR(255);

UPDATE sys_function set kanban_name = "待发布任务" WHERE function_id = 37;
UPDATE sys_function set kanban_name = "待领取任务" WHERE function_id = 40;
UPDATE sys_function set kanban_name = "试验检测中" WHERE function_id = 41;
UPDATE sys_function set kanban_name = "待编制报告" WHERE function_id = 42;

UPDATE sys_function set kanban_name = "待审核" WHERE function_id = 44;
UPDATE sys_function set kanban_name = "待签发" WHERE function_id = 45;
UPDATE sys_function set kanban_name = "待盖章" WHERE function_id = 46;
UPDATE sys_function set kanban_name = "待发出报告" WHERE function_id = 47;
--6.2--
INSERT INTO `sys_function` (`function_id`, `function_pid`, `name`, `sort`) VALUES ('65', '39', '报告查询', '9')

ALTER TABLE `test_product`
ADD COLUMN `outward_describe`  varchar(255) NULL COMMENT '外观描述' AFTER `outward_describe`;
--增加 中间报告菜单
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (66, 39, '中间报告', 3, 0, NULL);
--修改任务管理下菜单排序
UPDATE sys_function SET sort=4 WHERE function_id= 42;
UPDATE sys_function SET sort=5 WHERE function_id= 43;
UPDATE sys_function SET sort=6 WHERE function_id= 44;
UPDATE sys_function SET sort=7 WHERE function_id= 45;
UPDATE sys_function SET sort=8 WHERE function_id= 46;
UPDATE sys_function SET sort=9 WHERE function_id= 47;
UPDATE sys_function SET sort=10 WHERE function_id= 65;

ALTER TABLE `test_report_record`
ADD COLUMN `type`  int NULL DEFAULT 0 COMMENT '0,最终报告，1中间报告' AFTER `type`;

--客户提供关联所有产品--
insert into test_product_standard_file_rel(product_id,standard_file_id) select DISTINCT  product_id,2692 from test_product where del_flag=0 and status=0;

ALTER TABLE `test_report_record`
ADD COLUMN `category`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '印章分类，PHYSICS(物理章),ELECTRONIC(电子章)' AFTER `type`;

--2022-06-15--
ALTER TABLE `test_team`
ADD COLUMN `sort`  int NULL COMMENT '排序字段' AFTER `check_item_id`;

--task表增加任务价格字段
ALTER TABLE `test_task`
ADD COLUMN `task_price`  double(10,2) COMMENT '任务单价格' AFTER `review_time`;


--task表增加任务价格字段
ALTER TABLE `test_report_record_detail`
    ADD COLUMN `origin_url`  text COMMENT '原始记录上传url' AFTER `coordinate`;

ALTER TABLE `test_report_record_detail`
    ADD COLUMN `task_id`  bigint COMMENT '任务ID' AFTER `origin_url`;

--  test_entrusted_info表增加委托单创建人所属部门
ALTER TABLE `test_entrusted_info`
    ADD COLUMN `department`  int DEFAULT NULL COMMENT '委托单创建人所属部门' AFTER `system_price`;
--增加报告合并时间
ALTER TABLE `test_report_record`
    ADD COLUMN `combine_time`  datetime DEFAULT NULL COMMENT '报告合并时间' AFTER `category`;
--增加sample_id字段
ALTER TABLE `test_report_record_detail`
    ADD COLUMN `sample_id`  int COMMENT '样品ID' AFTER `task_id`;
--task表增加（任务单提供资料相等委托单）字段
ALTER TABLE `test_task`
ADD COLUMN `present_information`  varchar(255) COMMENT '任务单提供资料相等委托单' AFTER `task_price`;

-- test_entrusted_info 文件中间表增加 test_entrust_file_rel
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_entrust_file_rel
-- ----------------------------
DROP TABLE IF EXISTS `test_entrust_file_rel`;
CREATE TABLE `test_entrust_file_rel`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `entrust_id` bigint(0) DEFAULT NULL COMMENT '委托单id',
  `file_url` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件链接',
  `file_url_str` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '文件名。',
  `create_time` datetime(0) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_entrusted_task_rel
-- ----------------------------
DROP TABLE IF EXISTS `test_entrusted_task_rel`;
CREATE TABLE `test_entrusted_task_rel`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '流转单id',
  `department` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门id&部门名称',
  `task_flow_date` datetime(0) DEFAULT NULL COMMENT '任务流转日期',
  `type` int(0) DEFAULT NULL COMMENT '报告类型（0,最终报告，1中间报告）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `user_id` bigint(0) DEFAULT NULL COMMENT '用户id',
  `address_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户姓名',
  `task_id` bigint(0) NOT NULL COMMENT '任务单id',
  `create_date` datetime(0) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建日期',
  `update_date` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `entrust_id` bigint(0) NOT NULL COMMENT '委托单id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `test_entrusted_task_rel`
    ADD COLUMN `state`  int COMMENT '任务流转状态（0，未完成；1，已完成）' AFTER `entrust_id`;

ALTER TABLE `test_entrusted_task_rel`
    ADD COLUMN `record_id`  BIGINT COMMENT '报告主键ID' AFTER `state`;

ALTER TABLE `test_report_record`
    ADD COLUMN `entrust_id`  BIGINT COMMENT '委托单ID用于中间报告查询' AFTER `combine_time`;

ALTER TABLE `test_report_record`
    ADD COLUMN `inspector`  VARCHAR(255) COMMENT '报告中签字检测人' AFTER `entrust_id`;

--新增test_report_record_mid转存中间报告数据
CREATE TABLE `test_report_record_mid`  (
                                           `id` bigint(0) NOT NULL COMMENT '主键id',
                                           `entrustment_id` bigint(0) NULL DEFAULT NULL COMMENT '委托单ID',
                                           `report_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告编号',
                                           `sample_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '样品名称',
                                           `price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '本单费用',
                                           `required_completion_time` datetime(0) NULL DEFAULT NULL COMMENT '要求完成日期',
                                           `task_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务编号',
                                           `state` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.报告合并完成待审批，4.已审批待签发5.签发已抢单（废弃），6已签发，7已盖章，8已邮寄',
                                           `report_url` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '模板地址',
                                           `number` int(0) NULL DEFAULT NULL COMMENT '报告份数',
                                           `report_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '取报告方式',
                                           `verifyer_time` datetime(0) NULL DEFAULT NULL COMMENT '审批时间',
                                           `issuer_time` datetime(0) NULL DEFAULT NULL COMMENT '签发时间',
                                           `seal_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '盖章类型',
                                           `seal_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '印章类型对应的图片url',
                                           `apply_reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审批驳回原因',
                                           `issu_reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '签发驳回原因',
                                           `verifyer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核人姓名',
                                           `verifyer_id` bigint(0) NULL DEFAULT NULL COMMENT '复核人id',
                                           `issuer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '签发人',
                                           `issuer_id` bigint(0) NULL DEFAULT NULL COMMENT '签发人id',
                                           `applicant` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告提交申请人',
                                           `sealer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '盖章人',
                                           `seal_time` datetime(0) NULL DEFAULT NULL COMMENT '盖章时间',
                                           `report_manager` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告发出人',
                                           `report_time` datetime(0) NULL DEFAULT NULL COMMENT '报告发出时间',
                                           `addressee` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '领取报告人员',
                                           `waybill` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '运单编号',
                                           `operate_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间，报告发出后录入数据时的时间',
                                           `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电子邮箱',
                                           `report_phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收件电话',
                                           `report_mailing_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告邮寄地址',
                                           `report_complete_time` datetime(0) NULL DEFAULT NULL COMMENT '报告生成时间（state=1时）',
                                           `template_name` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                           `qys_docment_id` bigint(0) NULL DEFAULT NULL COMMENT '契约锁文档id',
                                           `qys_state` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '1' COMMENT '契约锁报告状态1合同待发起,2合同待创建，3合同待签署，4合同待盖章，5合同待下载',
                                           `contract_id` bigint(0) NULL DEFAULT NULL COMMENT '合同id',
                                           `sign_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '契约锁合同盖章url地址',
                                           `task_id` bigint(0) NULL DEFAULT NULL COMMENT '任务ID',
                                           `type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '0,最终报告，1中间报告',
                                           `category` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '印章分类，PHYSICS(物理章),ELECTRONIC(电子章)',
                                           `combine_time` datetime(0) NULL DEFAULT NULL COMMENT '报告合并时间',
                                           `entrust_id` bigint(0) NULL DEFAULT NULL COMMENT '委托单ID用于中间报告查询',
                                           `inspector` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '报告中签字检测人',
                                           PRIMARY KEY (`id`) USING BTREE
)
