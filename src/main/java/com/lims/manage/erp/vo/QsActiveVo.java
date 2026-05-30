package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * 内审计划-指令 执行
 *
 * @Author: DLC
 * @Date: 2024/7/16 14:54
 */
@Data
public class QsActiveVo {
    /**
     * 内审ID
     */
    private int activeId;
    /**
     * 类型：1=开始操作，2=内审检查,3=完成整改
     */
    private String type;

    /**
     * hastenWork：催办工作 = 1
     */
    private String hastenWork;

    /**
     * operationComplete：操作完成 = 1
     */
    private String operationComplete;

}
