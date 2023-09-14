package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/5/10 17:05
 * @Copyright © 河南交科院
 */
@Data
public class Location {
    private String documentId;
    /**
     * 签章类型： SEAL_PERSONAL（个人签名）, SEAL_CORPORATE（公司公章）,TIMESTAMP（时间戳），ACROSS_PAGE_ODD（奇数页骑缝章），ACROSS_PAGE（骑缝章）；
     * 骑缝章类型时只需要指定合同文档ID和Y轴坐标
     */
    private String rectType;
    /**
     * 签署页码，坐标指定位置时必须，0:全部页，-1:最后一页，其他:第page页
     */
    private int page;
    private double offsetX;
    private double offsetY;
    private String actionName;
    private String keyword;
}
