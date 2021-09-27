package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/9/24 15:35
 * 产品下所属检测项1
 */
@Data
public class JtEntrustCheckItem {
    private Integer entrustCheckItemId;

    private Integer entrustId;

    private Integer productId;

    private Integer checkItemId;

    private Integer standardId;

    private Integer sampleId;

    private Integer checkTimes;

    private String proceedsPrice;

    private String totalProceedsPrice;

    private String receivablePrice;

    private String totalReceivablePrice;
    /**
     * 数据传输需要和表结构无关
     */
    private String productName;
    private String checkItemName;
    private String checkBasisCode;
    private String checkBasisDocName;
    private Integer depId;
    private String depName;

    // 检测项 价格信息
    private Integer cost;
}
