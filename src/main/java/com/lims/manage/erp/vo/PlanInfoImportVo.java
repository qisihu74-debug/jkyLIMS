package com.lims.manage.erp.vo;

import lombok.Data;

import java.io.Serializable;


/**
 * @Description 考试/培训计划excel导入实体
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class PlanInfoImportVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 个人表现
     */
    private String expression;

    /**
     * 成绩
     */
    private double score;

    /**
     * 积分数量
     */
    private Integer integralNum;
}
