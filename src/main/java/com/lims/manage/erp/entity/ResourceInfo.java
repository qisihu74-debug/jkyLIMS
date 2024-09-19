package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-14 15:31
 * @Copyright © 河南交科院
 */
@Data
public class ResourceInfo {
    /**
     * 门禁点标识
     */
    private String resourceIndexCode;
    /**
     * 资源类型acsDevice门禁设备 ,door门禁点
     */
    private String resourceType;
    /**
     * 通道号
     */
    private List<Integer> channelNos;
}
