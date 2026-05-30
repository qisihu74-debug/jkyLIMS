package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

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
     * 记录id集合
     */
    private List<Long> id;
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
    /**
     * 逐渐id
     */
    private Long key;
}
