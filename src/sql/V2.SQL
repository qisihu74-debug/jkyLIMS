-- 仪器使用记录 增加字段（task_id）
ALTER TABLE `test_instrument_use_record`
ADD COLUMN `task_id`  bigint NULL COMMENT '任务单id 防止任务单id被删除' AFTER `time`;

-- 仪器 增加字段（device_state）设备状态
ALTER TABLE `test_instrument`
ADD COLUMN `device_state`  VARCHAR(50) NULL COMMENT '设备状态' AFTER `update_time`;
-- 修改 中间报告-->中间报告制作
UPDATE `sys_function` SET `name` = '中间报告制作' WHERE `function_id` = 66
-- 修改 报告制作-->最终报告制作
UPDATE `sys_function` SET `name` = '最终报告制作' WHERE `function_id` = 42

ALTER TABLE `test_task`
    ADD COLUMN `create_time`  datetime DEFAULT NULL COMMENT '任务单创建时间' AFTER `present_information`;

ALTER TABLE `test_entrusted_info`
    ADD COLUMN `operating_personnel`  VARCHAR(50) NULL COMMENT '经营人员' AFTER `create_time`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_linked_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_linked_data`;
CREATE TABLE `sys_linked_data`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '名字',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '链接',
  `sys_news_id` bigint(0) NOT NULL COMMENT 'sys_news中id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- 上述为版本2的SQL

-- 以下为 版本3的SQL


-- 2023年4月19日
-- 新版报告模板表
CREATE TABLE `test_report_original_template`  (
                                                  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '受控编号',
                                                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
                                                  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板链接',
                                                  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                                  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
                                                  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改日期',
                                                  PRIMARY KEY (`id`) USING BTREE
)
--新版报告与产品关系表
CREATE TABLE `product_report_original_rel`  (
                                                `product_id` bigint(0) NOT NULL COMMENT '产品ID',
                                                `report_original_id` bigint(0) NOT NULL COMMENT '报告原始记录模板ID'
)

-- 样品与委托单中间表时，加入产品Excel 数据加入。

ALTER TABLE `test_entrusted_sample_details_rel`
ADD COLUMN `product_excel_url`  VARCHAR(255) NULL COMMENT '产品excel模板数据源' AFTER `entrustment_id`;

-- 产品模板Excel
	ALTER TABLE `test_product`
ADD COLUMN `product_excel_url`  VARCHAR(255) NULL COMMENT '产品excel模板数据源' AFTER `outward_describe`;




show COLUMNS FROM test_task_used


SELECT * FROM sys_function WHERE function_id in (66,42)


	ALTER TABLE `test_entrusted_sample_checkitem_rel`
ADD COLUMN `edit_data`  VARCHAR(255) NULL COMMENT '数据' AFTER `task_id`;

177


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_item_sheet_rel
-- ----------------------------
DROP TABLE IF EXISTS `test_item_sheet_rel`;
CREATE TABLE `test_item_sheet_rel`  (
  `check_item_id` bigint(0) NOT NULL COMMENT '检测项ID',
  `sheet_index` int(0) NOT NULL COMMENT '报告模板sheet下标'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `test_entrusted_sample_details_rel`
ADD COLUMN `report_edit_url`  VARCHAR(255) NULL COMMENT '报告编辑后的文件' AFTER `product_excel_url`;


ALTER TABLE `test_entrusted_sample_details_rel`
ADD COLUMN `report_type`  int(0) DEFAULT 1 COMMENT '报告类型0最终，1中间' AFTER `report_edit_url`;

ALTER TABLE `test_entrusted_sample_details_rel`
ADD COLUMN `completion_status`  int(0) DEFAULT 0 COMMENT '报告完成状态0未完成，1已完成' AFTER `report_type`;

	ALTER TABLE `test_entrusted_sample_checkitem_rel`
ADD COLUMN `mid_state`  int(10) UNSIGNED ZEROFILL DEFAULT NULL COMMENT '0未出具中间报告 1 出具中间报告' AFTER `edit_data`;

ALTER TABLE `test_entrusted_sample_checkitem_rel`
ADD COLUMN `instrument_status`  VARCHAR(25) NULL COMMENT '仪器设备使用记录是否填写' AFTER `mid_state`;

ALTER TABLE `test_entrusted_sample_checkitem_rel`
ADD COLUMN `original_record_status`  VARCHAR(25) NULL COMMENT '原始记录是否填写完整/
电子记录是否备份' AFTER `instrument_status`;

ALTER TABLE `test_entrusted_sample_checkitem_rel`
ADD COLUMN `original_data`  VARCHAR(25) NULL COMMENT '数据处理是否正确' AFTER `original_record_status`;


-- 新增表 test_task_used

/*
 Navicat Premium Data Transfer

 Source Server         : 202erp
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : 192.168.2.202:43306
 Source Schema         : ERP_69

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 02/06/2023 10:43:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_task_used
-- ----------------------------
DROP TABLE IF EXISTS `test_task_used`;
CREATE TABLE `test_task_used`  (
  `id` bigint(0) NOT NULL COMMENT '任务id',
  `code` int(0) NOT NULL COMMENT '任务编号',
  `entrustment_id` bigint(0) NOT NULL COMMENT '对应委托单',
  `orderer` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '下单人',
  `order_time` datetime(0) DEFAULT NULL COMMENT '下单日期',
  `required_completion_time` datetime(0) DEFAULT NULL COMMENT '要求完成时间',
  `team_id` int(0) DEFAULT NULL COMMENT '检测团队id',
  `receiver` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '接单人',
  `receive_time` datetime(0) DEFAULT NULL COMMENT '接单时间',
  `sampler` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '领样人',
  `sample_receiving_time` datetime(0) DEFAULT NULL COMMENT '领样时间',
  `inspector` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '检测人',
  `start_detection_time` datetime(0) DEFAULT NULL COMMENT '开始检测时间',
  `end_detection_time` datetime(0) DEFAULT NULL COMMENT '完成检测时间',
  `recorder` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '记录人',
  `reviewer` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '复核人',
  `verifyer` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '审核人',
  `verify_time` datetime(0) DEFAULT NULL COMMENT '审核时间',
  `signer` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '签发人',
  `sign_time` datetime(0) DEFAULT NULL COMMENT '签发时间',
  `state` int(0) DEFAULT 0 COMMENT '状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成，5原始记录已上传，6.原始记录已符合，',
  `report_producer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '报告制作人',
  `task_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '团队名称+编号=任务编号',
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '驳回原因',
  `report_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '盖完章的报告文档',
  `issue_report` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否出具报告：0、不出具；1、出具',
  `dept_id` int(0) DEFAULT NULL COMMENT '部门ID',
  `report_complete` int(0) DEFAULT NULL COMMENT '1,报告已完成；2,报告未完成',
  `sample_state_description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '样品状态描述',
  `review_time` datetime(0) DEFAULT NULL COMMENT '复核时间',
  `task_price` decimal(10, 2) DEFAULT NULL COMMENT '本单价格',
  `present_information` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '任务单提供资料相等委托单',
  `create_time` datetime(0) DEFAULT NULL COMMENT '任务单创建时间',
  `waste_time` datetime(0) DEFAULT NULL COMMENT '废弃时间',
  `derelict` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '废弃人',
  INDEX `Index_task_trustbillid`(`entrustment_id`) USING BTREE,
  INDEX `code_index`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '任务单' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
