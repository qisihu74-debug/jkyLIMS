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
--task表增加（任务单提供资料相等委托单）字段
ALTER TABLE `test_task`
ADD COLUMN `present_information`  varchar(255) COMMENT '任务单提供资料相等委托单' AFTER `task_price`;



