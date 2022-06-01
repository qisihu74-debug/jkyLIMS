-- 2022-06-01 create --
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `test_alert`;
CREATE TABLE `test_alert` (
  `id` bigint NOT NULL,
  `entrust_id` bigint DEFAULT NULL,
  `check_item_name` varchar(255) DEFAULT NULL,
  `describ` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

