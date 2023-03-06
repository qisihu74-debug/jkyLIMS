/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : erp_69

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 01/03/2023 09:51:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_function
-- ----------------------------
DROP TABLE IF EXISTS `sys_function`;
CREATE TABLE `sys_function`  (
  `function_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单id',
  `function_pid` bigint(0) NOT NULL DEFAULT 0 COMMENT '菜单父id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '菜单顺序',
  `is_valid` int(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '0：有效；1无效',
  `kanban_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`function_id`) USING BTREE,
  UNIQUE INDEX `id_index`(`function_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 171 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单功能' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_function
-- ----------------------------
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (72, 0, '知识管理', 5, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (73, 72, '知识广场', 1, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (74, 72, '有问有答', 2, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (75, 72, '分享学习资料/视频', 3, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (76, 72, '培训/考试计划', 4, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (77, 11, '知识审核', 8, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (78, 72, '积分排行榜', 5, 0, NULL);
INSERT INTO `sys_function`(`function_id`, `function_pid`, `name`, `sort`, `is_valid`, `kanban_name`) VALUES (79, 72, '浏览足迹', 6, 0, NULL);
-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `role_remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `id_index`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4528691056498530 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------

INSERT INTO `sys_role`(`role_id`, `role_name`, `role_remark`, `create_time`) VALUES (1676864215192100, '知识管理-管理员', '知识管理系统的管理员，主要针对知识审核，计划新增，问题归类和删除', '2023-02-20 11:36:55');


-- ----------------------------
-- Table structure for sys_role_function
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_function`;
CREATE TABLE `sys_role_function`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `function_id` bigint(0) NOT NULL COMMENT '菜单id',
  `role_id` bigint(0) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fun_index`(`function_id`) USING BTREE,
  INDEX `role_index`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4706 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_function
-- ----------------------------

INSERT INTO `sys_role_function` VALUES (4666, 1, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4667, 77, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4668, 72, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4669, 73, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4670, 74, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4671, 75, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4672, 76, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4673, 78, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4674, 79, 1676864215192100);
INSERT INTO `sys_role_function` VALUES (4693, 40, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4694, 41, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4695, 1, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4696, 67, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4697, 63, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4698, 65, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4699, 72, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4700, 73, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4701, 74, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4702, 75, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4703, 76, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4704, 78, 1652402227183111);
INSERT INTO `sys_role_function` VALUES (4705, 79, 1652402227183111);

-- ----------------------------
-- Table structure for t_data_audit_record
-- ----------------------------
DROP TABLE IF EXISTS `t_data_audit_record`;
CREATE TABLE `t_data_audit_record`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `data_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核人id',
  `audit_status` tinyint(0) NOT NULL COMMENT '审核状态(0:未审核；1:审核通过,2:审核驳回)',
  `audit_content` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核内容',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_t_data_audit_record_data_id`(`data_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习资料审核记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_audit_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_experience
-- ----------------------------
DROP TABLE IF EXISTS `t_data_experience`;
CREATE TABLE `t_data_experience`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `data_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `experience_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '心得内容',
  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_t_data_experience_data_id`(`data_id`, `del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习资料-学习心得' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_experience
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_info
-- ----------------------------
DROP TABLE IF EXISTS `t_data_info`;
CREATE TABLE `t_data_info`  (
  `data_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `data_title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料名称',
  `data_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料标签',
  `data_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料类型',
  `data_video_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视频封面图地址',
  `data_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资料地址',
  `data_img_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资料转换的图片地址',
  `file_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上传文件名称',
  `file_suffix` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传文件后缀',
  `up_time` datetime(0) NULL DEFAULT NULL COMMENT '上传时间',
  `original` tinyint(0) NOT NULL COMMENT '是否原创,1:是；0：否',
  `data_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资料来源',
  `partake_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参与人员',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`data_id`) USING BTREE,
  INDEX `index_t_data_info_user_id`(`user_id`, `data_title`, `data_label`, `data_type`, `del_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习资料信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_info
-- ----------------------------

-- ----------------------------
-- Table structure for t_data_record
-- ----------------------------
DROP TABLE IF EXISTS `t_data_record`;
CREATE TABLE `t_data_record`  (
  `data_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料id',
  `like_num` int(0) NULL DEFAULT 0 COMMENT '点赞数量',
  `tap_num` int(0) NULL DEFAULT 0 COMMENT '点踩数量',
  `collect_num` int(0) NULL DEFAULT 0 COMMENT '收藏数量',
  `complete_num` int(0) NULL DEFAULT 0 COMMENT '完成人数',
  PRIMARY KEY (`data_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习资料动态(点赞、点踩、收藏、完成)记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_data_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_dict`;
CREATE TABLE `t_dict`  (
  `dict_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典id',
  `dict_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典code',
  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict
-- ----------------------------
INSERT INTO `t_dict` VALUES ('1', '积分类型', 'integral_type', 0, 'admin', '2022-12-28 10:14:07', NULL, NULL);
INSERT INTO `t_dict` VALUES ('2', '学习资料类型', 'data_type', 0, 'admin', '2022-12-28 10:35:39', NULL, NULL);
INSERT INTO `t_dict` VALUES ('3', '计划类型', 'plan_type', 0, 'admin', '2023-01-13 09:47:23', NULL, NULL);
INSERT INTO `t_dict` VALUES ('4', '用户报名状态', 'partake_status', 0, 'admin', '2023-01-13 14:58:50', NULL, NULL);
INSERT INTO `t_dict` VALUES ('5', '问题类型', 'problem_type', 0, 'admin', '2023-01-28 16:11:36', NULL, NULL);
INSERT INTO `t_dict` VALUES ('6', '用户操作事件类型', 'event_type', 0, 'admin', '2023-01-29 15:52:54', NULL, NULL);
INSERT INTO `t_dict` VALUES ('7', '用户操作类型', 'operation_type', 0, 'admin', '2023-01-29 15:53:20', NULL, NULL);

-- ----------------------------
-- Table structure for t_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_item`;
CREATE TABLE `t_dict_item`  (
  `item_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项id',
  `dict_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典id',
  `parent_item_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级字典项id',
  `item_text` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项文本',
  `item_sort` int(0) NOT NULL COMMENT '排序',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`item_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典值' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict_item
-- ----------------------------
INSERT INTO `t_dict_item` VALUES ('1', '1', NULL, '学习积分', 0, 'admin', '2022-12-28 10:15:01', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('10', '5', NULL, '普通提问', 0, 'admin', '2023-01-28 16:11:57', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('11', '5', NULL, '精选问题', 1, 'admin', '2023-01-28 16:12:18', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('12', '5', NULL, '新手必看', 2, 'admin', '2023-01-28 16:12:38', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('13', '6', NULL, '资料类型', 0, 'admin', '2023-01-29 15:54:07', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('14', '6', NULL, '问答类型', 1, 'admin', '2023-01-29 15:54:30', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('15', '6', NULL, '评论类型', 2, 'admin', '2023-01-29 15:54:45', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('16', '7', NULL, '浏览', 0, 'admin', '2023-01-29 15:55:06', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('17', '7', NULL, '点赞', 1, 'admin', '2023-01-29 15:55:18', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('18', '7', NULL, '点踩', 2, 'admin', '2023-01-29 15:55:29', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('19', '7', NULL, '收藏', 3, 'admin', '2023-01-29 15:55:42', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('2', '1', NULL, '专业积分', 0, 'admin', '2022-12-28 10:15:17', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('20', '7', NULL, '完成', 4, 'admin', '2023-02-01 15:51:49', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('21', '4', NULL, '未参与', 0, 'admin', '2023-02-20 16:25:09', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('3', '2', NULL, '学习资料', 0, 'admin', '2022-12-28 10:36:02', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('4', '2', NULL, '学习视频', 0, 'admin', '2022-12-28 10:36:14', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('5', '3', NULL, '考试', 0, 'admin', '2023-01-13 09:46:37', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('6', '3', NULL, '培训', 0, 'admin', '2023-01-13 09:46:54', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('7', '4', NULL, '已报名', 1, 'admin', '2023-01-13 14:59:06', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('8', '4', NULL, '已完成', 2, 'admin', '2023-01-13 14:59:21', NULL, NULL);
INSERT INTO `t_dict_item` VALUES ('9', '4', NULL, '未完成', 3, 'admin', '2023-01-13 14:59:34', NULL, NULL);

-- ----------------------------
-- Table structure for t_integral_info
-- ----------------------------
DROP TABLE IF EXISTS `t_integral_info`;
CREATE TABLE `t_integral_info`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `integral_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分类型',
  `integral_num` int(0) NOT NULL COMMENT '积分数量',
  `integral_title` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分对应称号',
  `integral_badge` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '积分对应徽章',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分对应称号信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_integral_info
-- ----------------------------
INSERT INTO `t_integral_info` VALUES ('1', '1', 100, '一心一意', NULL);
INSERT INTO `t_integral_info` VALUES ('10', '1', 1000000, '十年磨剑', NULL);
INSERT INTO `t_integral_info` VALUES ('11', '2', 100, '123', NULL);
INSERT INTO `t_integral_info` VALUES ('12', '2', 1000, '222', NULL);
INSERT INTO `t_integral_info` VALUES ('13', '2', 15000, '333', NULL);
INSERT INTO `t_integral_info` VALUES ('14', '2', 8000, '444', NULL);
INSERT INTO `t_integral_info` VALUES ('2', '1', 200, '再接再励', NULL);
INSERT INTO `t_integral_info` VALUES ('3', '1', 500, '三省吾身', NULL);
INSERT INTO `t_integral_info` VALUES ('4', '1', 1000, '名扬四海', NULL);
INSERT INTO `t_integral_info` VALUES ('5', '1', 2000, '学富五车', NULL);
INSERT INTO `t_integral_info` VALUES ('6', '1', 4000, '六韬三略', NULL);
INSERT INTO `t_integral_info` VALUES ('7', '1', 8000, '七步才华', NULL);
INSERT INTO `t_integral_info` VALUES ('8', '1', 15000, '才高八斗', NULL);
INSERT INTO `t_integral_info` VALUES ('9', '1', 30000, '九天揽月', NULL);

-- ----------------------------
-- Table structure for t_integral_rule
-- ----------------------------
DROP TABLE IF EXISTS `t_integral_rule`;
CREATE TABLE `t_integral_rule`  (
  `rule_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则类型id',
  `integral_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分类型',
  `complete_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '完成类型(点赞、上传、学习、签到等)',
  `frequency` int(0) NULL DEFAULT NULL COMMENT '每日完成频次',
  `integral_num` int(0) NOT NULL COMMENT '积分数量',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分规则' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_integral_rule
-- ----------------------------
INSERT INTO `t_integral_rule` VALUES ('1', '1', '签到', 1, 1);
INSERT INTO `t_integral_rule` VALUES ('2', '1', '上传学习资料', NULL, 3);
INSERT INTO `t_integral_rule` VALUES ('3', '1', '上传学习视频', NULL, 5);
INSERT INTO `t_integral_rule` VALUES ('4', '1', '阅读学习资料', 6, 1);
INSERT INTO `t_integral_rule` VALUES ('5', '1', '观看学习视频', 10, 1);
INSERT INTO `t_integral_rule` VALUES ('6', '1', '参加学习培训', 2, 5);
INSERT INTO `t_integral_rule` VALUES ('7', '1', '授课人员', NULL, 20);
INSERT INTO `t_integral_rule` VALUES ('8', '1', '培训考试', 1, 10);
INSERT INTO `t_integral_rule` VALUES ('9', '1', '点赞10个', NULL, 2);

-- ----------------------------
-- Table structure for t_label_info
-- ----------------------------
DROP TABLE IF EXISTS `t_label_info`;
CREATE TABLE `t_label_info`  (
  `label_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签id',
  `label_content` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签信息',
  `label_type` tinyint(0) NOT NULL DEFAULT 0 COMMENT '标签类型:0:系统标签;1:用户自定义标签',
  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`label_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标签信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_label_info
-- ----------------------------
INSERT INTO `t_label_info` VALUES ('1', '土木试验', 0, 0, 'admin', '2022-12-28 10:30:09');
INSERT INTO `t_label_info` VALUES ('2', '绿色建材', 0, 0, 'admin', '2022-12-28 10:30:23');
INSERT INTO `t_label_info` VALUES ('3', '水泥混泥土', 0, 0, 'admin', '2022-12-28 10:33:04');
INSERT INTO `t_label_info` VALUES ('4', '沥青试验', 0, 0, 'admin', '2022-12-28 10:33:31');

-- ----------------------------
-- Table structure for t_plan_info
-- ----------------------------
DROP TABLE IF EXISTS `t_plan_info`;
CREATE TABLE `t_plan_info`  (
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划Id',
  `plan_title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划名称',
  `plan_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '计划类型',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起人',
  `plan_begin_time` datetime(0) NOT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime(0) NOT NULL COMMENT '计划结束时间',
  `target_user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '面向对象',
  `plan_place` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '计划地点',
  `plan_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简介内容',
  `enclosure_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件地址',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`plan_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '培训/考试计划' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_plan_info
-- ----------------------------

-- ----------------------------
-- Table structure for t_problem_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_problem_comment`;
CREATE TABLE `t_problem_comment`  (
  `comment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论id',
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `parent_comment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级评论id',
  `comment_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`comment_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '问答评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_problem_comment
-- ----------------------------

-- ----------------------------
-- Table structure for t_problem_comment_record
-- ----------------------------
DROP TABLE IF EXISTS `t_problem_comment_record`;
CREATE TABLE `t_problem_comment_record`  (
  `comment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论id',
  `like_num` int(0) NULL DEFAULT 0 COMMENT '点赞数量',
  `tap_num` int(0) NULL DEFAULT 0 COMMENT '点踩数量',
  PRIMARY KEY (`comment_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论动态(点赞、点踩)记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_problem_comment_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_problem_info
-- ----------------------------
DROP TABLE IF EXISTS `t_problem_info`;
CREATE TABLE `t_problem_info`  (
  `problem_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `problem_title` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题名称',
  `data_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据标签',
  `problem_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题类型',
  `problem_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '问题内容',
  `is_top` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否置顶(0:默认不置顶；1：置顶)',
  `view_num` int(0) NULL DEFAULT 0 COMMENT '浏览数量',
  `del_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`problem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '问答列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_problem_info
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_integral_info
-- ----------------------------
DROP TABLE IF EXISTS `t_user_integral_info`;
CREATE TABLE `t_user_integral_info`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户Id',
  `integral_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分类型',
  `integral_num` int(0) NOT NULL DEFAULT 0 COMMENT '积分数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户积分信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_integral_info
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_integral_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_integral_record`;
CREATE TABLE `t_user_integral_record`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `integral_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分类型',
  `integral_num` int(0) NOT NULL COMMENT '积分数量',
  `rule_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则id',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '事件类型,详细查看字典',
  `event_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资料/评论id',
  `gain_time` datetime(0) NOT NULL COMMENT '获取时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户获取积分记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_integral_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_operation_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_operation_record`;
CREATE TABLE `t_user_operation_record`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件类型,详细查看字典',
  `event_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资料id、问答id、评论id',
  `operation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户操作类型（浏览、赞、踩、收藏）',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_t_user_operation_record_event_type`(`user_id`, `event_type`, `event_id`, `operation_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户动态操作记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_operation_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_operation_record1
-- ----------------------------
DROP TABLE IF EXISTS `t_user_operation_record1`;
CREATE TABLE `t_user_operation_record1`  (
  `record_id` int(0) NOT NULL COMMENT '记录id',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名称',
  `record_type` int(0) NULL DEFAULT NULL COMMENT '记录类型',
  `record_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型中文名称',
  `record_type_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录类型id',
  `record_type_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录类型标题',
  `record_type_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录类型评论内容',
  `record_type_integral` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录类型积分',
  `record_type_time` datetime(0) NULL DEFAULT NULL COMMENT '记录类型时间',
  PRIMARY KEY (`record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_operation_record1
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_plan_info
-- ----------------------------
DROP TABLE IF EXISTS `t_user_plan_info`;
CREATE TABLE `t_user_plan_info`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '记录id',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `enroll_time` datetime(0) NOT NULL COMMENT '报名时间',
  `partake_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参与状态',
  `plan_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '培训/考试id',
  `examination_scores` int(0) NULL DEFAULT NULL COMMENT '培训/考试成绩',
  `examination_result` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '培训/考试结果',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_t_user_plan_info`(`user_id`, `plan_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户参加计划信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_plan_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
