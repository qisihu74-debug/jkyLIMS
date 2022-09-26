package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2022/9/26 16:30
 * 适用于 返回委托编号信息
 */
@Data
public class EntrustCategoryVo {
    /**
     * 委托编号
     */
    private Integer entrustmentNo;
    /**
     * 委托编号类别： null 常规原材试验、MN模拟试验、BD比对试验
     */
    private String entrustCategoryType;
    /**
     * 委托编号类别string
     */
    private String entrustCategory;
    /**
     * 拼接后委托编号
     */
    private String entrustmentNoStr;
}
