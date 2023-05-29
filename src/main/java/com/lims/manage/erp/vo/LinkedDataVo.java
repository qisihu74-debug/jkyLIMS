package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2023/5/22 15:48
 * 动态简报 中 链接数据层
 */
@Data
public class LinkedDataVo {
    /**
     * id
     */
    private Integer id;
    /**
     * 序号
     */
    private Integer serialNumber;
    /**
     * 名称
     */
    private String name;
    /**
     * 对应链接
     */
    private String url;
    /**
     * sys_news_id
     */
    private Long sysNewsId;
}
