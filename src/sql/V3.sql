
CREATE TABLE `test_sample_pre_code`  (
                                         `pre_sample_code` int(0) NULL DEFAULT NULL COMMENT '预收样样品编号',
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

UPDATE `sys_function` SET `name`='检测任务' WHERE (`function_id`='40') LIMIT 1;



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
ADD COLUMN `operate_type`  int NULL DEFAULT 0 COMMENT '操作类型0线上编辑的报告，1线下编辑的报告' AFTER `seal_report_url`;

UPDATE `sys_function` SET `name`='报告制作' WHERE (`function_id`='43') LIMIT 1;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_item_sheet_rel_head_context
-- ----------------------------
DROP TABLE IF EXISTS `test_item_sheet_rel_head_context`;
CREATE TABLE `test_item_sheet_rel_head_context`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `sample_id` int(0) DEFAULT NULL COMMENT '样品ID',
  `sheet_index` int(0) NOT NULL COMMENT '报告模板sheet下标',
  `test_date_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '检测日期-Text',
  `test_condition_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '试验条件 - Text',
  `equipment_text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '主要仪器 - Text',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `test_sample_area`;
CREATE TABLE `test_sample_area` (
  `id` int NOT NULL COMMENT 'id',
  `pid` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT '名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO `sys_role`(`role_id`, `role_name`, `role_remark`, `create_time`) VALUES (88, '交通工程下领取员角色', NULL, '2023-10-30 15:06:55');

ALTER TABLE `test_instrument`
ADD COLUMN `is_show`  int NULL DEFAULT 0 COMMENT '0生成仪器使用记录，1不需要生成（不展示）' AFTER `device_state`;

ALTER TABLE `test_instrument`
ADD COLUMN `parallel`  int NULL DEFAULT 0 COMMENT '并线数量（不能并行的仪器默认为0，并行的仪器给出数量）' AFTER `is_show`;




SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_task_order_working_hours
-- ----------------------------
DROP TABLE IF EXISTS `test_task_order_working_hours`;
CREATE TABLE `test_task_order_working_hours`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(0) DEFAULT NULL COMMENT '任务单id',
  `task_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务单号',
  `sample_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '样品名称',
  `total_working_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '总工时',
  `detection_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '检测类型',
  `user_id` bigint(0) DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户名称',
  `working_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '使用工时',
  `proportion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '比例',
  `create_time` datetime(0) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) DEFAULT NULL COMMENT '更新时间',
  `add_operator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '新增操作人',
  `source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (49, 30, '检测人员', '30');
INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (50, 30, '记录人员', '20');
INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (51, 30, '复核人', '30');
INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (52, 30, '报告制作人', '20');


INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (200, 60, '工时统计', 0, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (201, 200, '我的工时', 3, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (202, 200, '按人员统计', 1, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (203, 200, '按授权签字人统计', 2, 0, NULL);

-- 工时统计
INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (53, 30, '签发人', '20');
INSERT INTO `test_init_data`(`id`, `type`, `name`, `remark`) VALUES (54, 30, '辅助人员', '0');

ALTER TABLE `test_product_item` ADD COLUMN `working_hours` VARCHAR ( 255 ) NULL COMMENT '检测项工时' AFTER `report_model_name`;

--设备队伍表
CREATE TABLE `test_instrument_group`  (
                                          `instrument_id` bigint(0) NULL DEFAULT NULL COMMENT '设备ID',
                                          `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用人',
                                          `task_id` bigint(0) NULL DEFAULT NULL COMMENT '任务ID',
                                          `task_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务单号',
                                          `state` int(0) NULL DEFAULT 0 COMMENT '队伍状态（0，等待试验；1，开始试验）',
                                          `parallel` int(0) NULL DEFAULT NULL COMMENT '并行任务占用数量',
                                          `esc_rel_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检测的检测项数据ID'
)

--设备使用记录增加 任务占用数量 字段
ALTER TABLE `test_instrument_use_record`
    ADD COLUMN `parallel`  int NULL DEFAULT 0 COMMENT '任务占用数量' AFTER `task_code`;



    -- 流水号任务单新增字段
ALTER TABLE `test_task_pool` ADD COLUMN `product_id` VARCHAR ( 255 ) NULL COMMENT '产品id 多个使用逗号间隔' AFTER `sample`;

ALTER TABLE `test_task_pool` ADD COLUMN `alias_name` VARCHAR ( 255 ) NULL COMMENT '产品别名' AFTER `product_id`;


ALTER TABLE `test_task` ADD COLUMN `working_hours_id` int NULL COMMENT '工时id存在则已添加 不存在则为空' AFTER `pool_id`;

--依据变更功能
ALTER TABLE `test_standard_file` ADD COLUMN `pid` int NULL COMMENT '依据关联ID' AFTER `implementation_date`;

CREATE TABLE `test_standard_file_record`  (
                                              `id` int(0) NOT NULL COMMENT '主键id',
                                              `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件类型1.检测依据，2判定依据，3既是检测又是判定，4其它',
                                              `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件编号',
                                              `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件名称',
                                              `file_url` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '文件地址',
                                              `standard_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标准类型，地标、国标、行标、企标、铁标、协会标准',
                                              `expiration_date` date NULL DEFAULT NULL COMMENT '失效日期',
                                              `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT ' 0,启用，1,冻结',
                                              `del_flag` int(0) NOT NULL DEFAULT 0 COMMENT '0默认未删除,1删除',
                                              `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '注册时间',
                                              `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
                                              `remark` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                                              `standard_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标准规范状态。\r\n数据来源：https://openstd.samr.gov.cn/bzgk/gb/gbMainQuery\r\n',
                                              `release_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布日期',
                                              `implementation_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实施日期/作废日期',
                                              `pid` int(0) NULL DEFAULT NULL COMMENT '系列ID',
                                              INDEX `id_index`(`id`) USING BTREE
)

CREATE TABLE `test_standard_method`  (
                                         `method_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '方法ID',
                                         `standard_id` int(0) NOT NULL COMMENT '依据ID',
                                         `chapter_num` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '章节号',
                                         `chapter_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '章节名称',
                                         `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                         PRIMARY KEY (`method_id`) USING BTREE
)

--报告变更
ALTER TABLE `test_report_original_template` ADD COLUMN `implementation_date` datetime NULL COMMENT '实施日期' AFTER `update_date`;
ALTER TABLE `test_report_original_template` ADD COLUMN `expiration_date` datetime NULL COMMENT '过期日期' AFTER `implementation_date`;
ALTER TABLE `test_report_original_template` ADD COLUMN `status` VARCHAR ( 255 ) NULL COMMENT '当前状态' AFTER `implementation_date`;
ALTER TABLE `test_report_original_template` ADD COLUMN `pid` int NULL COMMENT '报告变更关系ID' AFTER `status`;

CREATE TABLE `test_report_original_template_record`  (
                                                         `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                         `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '受控编号',
                                                         `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
                                                         `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板链接',
                                                         `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                                         `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建日期',
                                                         `update_date` datetime(0) NULL DEFAULT NULL COMMENT '修改日期',
                                                         `implementation_date` datetime(0) NULL DEFAULT NULL COMMENT '实施日期',
                                                         `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前状态',
                                                         `expiration_date` datetime(0) NULL DEFAULT NULL COMMENT '过期日期',
                                                         `pid` bigint(0) NULL DEFAULT NULL COMMENT '系列ID',
                                                         PRIMARY KEY (`id`) USING BTREE
)