package com.lims.manage.erp.http;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.http
 * @desc
 * @date 2022/2/25 11:38
 * @Copyright © 河南交科院
 */
@Data
public class QiYueSuoDocment {
    /**
     * 文档id
     */
    private Long documentId;
    /**
     * 部门id
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 租户类型
     */
    private String tenantType;
    /**
     * 状态
     */
    private String status;
    /**
     * 上级部门id
     */
    private Long parentId;
    /**
     *
     */
    private Boolean certified;

}
