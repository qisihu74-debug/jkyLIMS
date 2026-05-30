package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/3/4 11:11
 * @Copyright © 河南交科院
 */
@Data
public class QiYueSuoSeaLBean {
    /**
     * 委托单id
     */
    private Long entrustId;
    /**
     * 合同标识
     */
    private Long contractId;
    /**
     * 发起方名称
     */
    private String tenantName;
    /**
     * 签约主体类型：COMPANY（外部企业），PERSONAL（个人）
     */
    private String tenantType;
    /**
     * 接收人联系方式
     */
    private String contact;
    /**
     * 签署页过期时间
     */
    private int expireTime;
    /**
     *接收人姓名
     */
    private String receiverName;
    private String reportType;
}
