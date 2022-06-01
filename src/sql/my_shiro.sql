/*
 Navicat MySQL Data Transfer

 Source Server         : 我的腾讯云
 Source Server Type    : MySQL
 Source Server Version : 50725
 Source Host           : 140.143.244.170:3306
 Source Schema         : my_shiro

 Target Server Type    : MySQL
 Target Server Version : 50725
 File Encoding         : 65001

 Date: 20/06/2019 11:05:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `perms` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '查看用户列表', 'sys:user:info');
INSERT INTO `sys_menu` VALUES (2, '查看角色列表', 'sys:role:info');
INSERT INTO `sys_menu` VALUES (3, '查看权限列表', 'sys:menu:info');
INSERT INTO `sys_menu` VALUES (4, '查看所有数据', 'sys:info:all');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN');
INSERT INTO `sys_role` VALUES (2, 'USER');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint(11) NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` bigint(11) NULL DEFAULT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色与权限关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1, 2);
INSERT INTO `sys_role_menu` VALUES (3, 1, 3);
INSERT INTO `sys_role_menu` VALUES (4, 2, 1);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '盐值',
  `state` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态:NORMAL正常  PROHIBIT禁用',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1457656933753110530, 'admin', '2f16b9fe690122b2ad4396533d87377d735f2fc80f5411dee7ed50c105ed0c23', 'HcnXvMnHYNXRU28Tbeyz', 'NORMAL');
INSERT INTO `sys_user` VALUES (1457656966263160833, 'user', '3ba5c66ad9159514391219a60602655b67304d005796a191a7ad8c1148f27b9d', 'UEWQ6iEodmXAZbLk22Xy', 'NORMAL');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(11) NULL DEFAULT NULL COMMENT '用户ID',
  `role_id` bigint(11) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户与角色关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1457656933753110530, 1);
INSERT INTO `sys_user_role` VALUES (2, 1457656966263160833, 2);

SET FOREIGN_KEY_CHECKS = 1;

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
