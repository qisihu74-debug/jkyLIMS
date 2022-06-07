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

ALTER TABLE `test_report_record`
ADD COLUMN `type`  int NULL DEFAULT 0 COMMENT '0,最终报告，1中间报告' AFTER `type`;

