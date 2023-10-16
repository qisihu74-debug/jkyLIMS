--预收样品编号表
CREATE TABLE `test_sample_pre_code`  (
                                         `pre_sample_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预收样样品编号',
                                         `month` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '月',
                                         `year` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年'
)
--设备使用记录增加任务单号字段
ALTER TABLE `test_instrument_use_record`
    ADD COLUMN `task_code`  VARCHAR(50) NULL COMMENT '任务单号';