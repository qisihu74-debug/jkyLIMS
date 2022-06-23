package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/6/23 10:04
 * @Copyright © 河南交科院
 */
@Data
public class SealEntity {
    /**
     * 记录id
     */
    private Long id;
    /**
     * 印章类型
     */
    private String sealType;
    /**
     * 盖章人
     */
    private String sealer;
    /**
     * 盖章时间
     */
    private Date sealTime;
}
