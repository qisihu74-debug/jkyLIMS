package com.lims.manage.erp.util;

import java.util.Arrays;
import java.util.List;

/**
 * Excel计划结构
 * @author : zhq
 * @date :   2023-03-06
 * @version : V1.0
 */
public class ExcelPlanStructure {

    /** 培训考试计划Excel导出模板列名称 */
    public static final List<String> PLAN_COLUMN_NAME = Arrays.asList(
        "姓名", "账号", "个人表现", "成绩", "积分");

    /** 培训考试计划Excel导出模板是否为空 */
    public static final List<Boolean> PLAN_ISNULL_ABLE = Arrays.asList(
        true, true,true, true, true);
}
