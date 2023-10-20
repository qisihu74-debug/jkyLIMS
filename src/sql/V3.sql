
CREATE TABLE `test_sample_pre_code`  (
                                         `pre_sample_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预收样样品编号',
                                         `month` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '月',
                                         `year` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年'
);

ALTER TABLE `test_instrument_use_record`
    ADD COLUMN `task_code`  VARCHAR(50) NULL COMMENT '任务单号';

SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS `test_task_pool`;
CREATE TABLE `test_task_pool` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '任务池任务id',
  `sn` varchar(255) CHARACTER SET utf32 COLLATE utf32_general_ci NOT NULL COMMENT '任务流水号',
  `sample` varchar(255) DEFAULT NULL COMMENT '样品信息',
  `entrustment_id` bigint NOT NULL COMMENT '对应委托单',
  `price` varchar(50) DEFAULT NULL COMMENT '本单费用',
  `required_completion_time` datetime DEFAULT NULL COMMENT '要求完成时间',
  `task_flow_req` varchar(255) DEFAULT NULL COMMENT '任务流转要求',
  `task_code` varchar(255) DEFAULT NULL COMMENT '任务单号',
  `publisher` varchar(255) DEFAULT NULL COMMENT '发布人',
  `publish_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '发布时间',
  `receive_id` bigint DEFAULT NULL COMMENT '领取人id',
  `receive_name` varchar(255) DEFAULT NULL COMMENT '领取人（当前登陆人员）',
  `receive_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (`id`),
  KEY `Index_task_trustbillid` (`entrustment_id`) USING BTREE,
  KEY `code_index` (`sn`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COMMENT='任务单';


SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS `test_technicist_capacity`;
CREATE TABLE `test_technicist_capacity` (
  `technicist_id` int NOT NULL COMMENT '技术人员id',
  `product_type_id` int DEFAULT NULL,
  `product_type_name` varchar(255) DEFAULT NULL COMMENT '产品大类名称',
  `product_id` int DEFAULT NULL COMMENT '产品id',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `test_task`
ADD COLUMN `pool_id`  int NULL ;

ALTER TABLE `test_entrusted_task_rel`
MODIFY COLUMN `task_id`  bigint NULL COMMENT '任务单id' ;


SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS `test_check_items_task_rel`;
CREATE TABLE `test_check_items_task_rel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL COMMENT '检测项主键',
  `check_item_name` varchar(255) DEFAULT NULL COMMENT '检测项名称',
  `sample_name` varchar(255) DEFAULT NULL COMMENT '样品名称',
  `sample_code` varchar(255) DEFAULT NULL COMMENT '样品编号',
  `sample_id` int DEFAULT NULL COMMENT '样品主键',
  `entrust_id` bigint DEFAULT NULL COMMENT '委托单主键',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'name人名',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'user_id',
  `user_type` int DEFAULT NULL COMMENT '0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生',
  `task_id` bigint DEFAULT NULL COMMENT '任务单id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=353 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `sys_function` (`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES ('96', '39', '任务大厅', '0', '0', '待领取任务')

UPDATE `sys_function` SET `name`='检测任务' WHERE (`function_id`='40') LIMIT 1



DROP TABLE IF EXISTS `test_user_proportion`;
CREATE TABLE `test_user_proportion`  (
                                         `id` int(0) NOT NULL,
                                         `user_type` int(0) NULL DEFAULT NULL COMMENT '0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生',
                                         `user_proportion` double NULL DEFAULT NULL COMMENT '工时占比',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人员工时占比信息' ROW_FORMAT = Dynamic;


INSERT INTO `test_user_proportion` VALUES (1, 0, 0.25);
INSERT INTO `test_user_proportion` VALUES (2, 1, 0.25);
INSERT INTO `test_user_proportion` VALUES (3, 2, 0.25);
INSERT INTO `test_user_proportion` VALUES (4, 3, 0.25);

INSERT INTO `sys_function` (`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES ('150', '60', '工时统计', '15', '0', '');



ALTER TABLE `test_entrusted_info`
    ADD COLUMN `is_reserve`  VARCHAR(50) NULL COMMENT '是否保留';

INSERT INTO `sys_function` (`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES ('96', '39', '任务大厅', '0', '0', '待领取任务');

UPDATE `sys_function` SET `name`='检测任务' WHERE (`function_id`='40') LIMIT 1;

UPDATE sys_function SET name='审核发布' WHERE function_id = 37;

INSERT INTO `sys_role`(`role_id`, `role_name`, `role_remark`, `create_time`) VALUES (66, '授权签字人', NULL, '2023-09-13 19:42:29');


ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `reviewed_by_set_url` VARCHAR ( 255 ) NULL COMMENT '审核人url' AFTER `record_set_url`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `check_item_code` VARCHAR ( 255 ) NULL COMMENT '审核人url' AFTER `reviewed_by_set_url`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `category` VARCHAR ( 255 ) NULL COMMENT '是否印章' AFTER `check_item_code`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `qys_docment_id` VARCHAR ( 255 ) NULL COMMENT 'qys_docment_id' AFTER `category`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `contract_id` VARCHAR ( 255 ) NULL COMMENT 'contract_id' AFTER `qys_docment_id`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `sign_url` VARCHAR ( 255 ) NULL COMMENT 'sign_url' AFTER `contract_id`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `qys_state` VARCHAR ( 255 ) NULL COMMENT 'qys_state' AFTER `sign_url`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `sealer` VARCHAR ( 255 ) NULL COMMENT 'sealer' AFTER `qys_state`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `seal_time` datetime NULL COMMENT 'seal_time' AFTER `sealer`;
ALTER TABLE `test_entrusted_sample_checkitem_rel` ADD COLUMN `origin_url_pdf` VARCHAR ( 255 ) NULL COMMENT '试验完成后生成pdf文件' AFTER `seal_time`;
-- 任务单增加参数
ALTER TABLE `test_task` ADD COLUMN `probationer` VARCHAR ( 255 ) NULL COMMENT '见习生：实习的新手' AFTER `create_time`;
ALTER TABLE `test_task` ADD COLUMN `interns` VARCHAR ( 255 ) NULL COMMENT '实习生' AFTER `probationer`;
ALTER TABLE `test_task` ADD COLUMN `auxiliary_personnel` VARCHAR ( 255 ) NULL COMMENT '辅助人员' AFTER `interns`;

delete from sys_function WHERE name in('中间报告制作','最终报告制作','报告合成(旧)');

UPDATE `sys_function` SET `name`='报告合成' WHERE (`function_id`='95') LIMIT 1;

UPDATE `sys_function` SET `name`='在线报告制作' WHERE (`function_id`='43') LIMIT 1;

ALTER TABLE `test_report_record`
DROP COLUMN `operate_type`,
ADD COLUMN `operate_type`  int NULL DEFAULT 0 COMMENT '操作类型0线上编辑的报告，1线下编辑的报告' AFTER `seal_report_url`;


