package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/6 11:48
 * 判定依据集合
 */
@Data
public class JudgmentBasisVo {
    /**
     * 依据名称
     */
    private String standardName;
    /**
     * 依据id
     */
    private Integer standardId;
    /**
     * 检测名称
     */
    private String checkItemName;
    /**
     * 检测id
     */
    private Integer checkItemId;
    /**
     *检测次数
     */
    private Integer times;
    /**
     * 检测单价
     */
    private String checkParice;
    /**
     * 总价
     */
    private String totalPrice;
    /**
     * 方法id
     */
    private Integer methodId;
    /**
     * 方法名
     */
    private String methodName;
}
