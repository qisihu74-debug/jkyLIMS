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
     * 依据
     */
    private String standard;
    /**
     * 名称
     */
    private String name;
    /**
     * 检测名称
     */
    private String checkItemName;
    /**
     *检测次数
     */
    private Integer times;
    /**
     * 检测单价
     */
    private String unitPrice;
    /**
     * 总价
     */
    private String totalPrice;
}
