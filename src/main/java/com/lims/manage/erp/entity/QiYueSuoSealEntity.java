package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc 印章bean
 * @date 2022/3/1 10:18
 * @Copyright © 河南交科院
 */
@Data
public class QiYueSuoSealEntity {
    /**
     * 印章id
     */
    private String id;
    /**
     * 印章所属部门id
     */
    private String owner;
    /**
     * 印章名称
     */
    private String name;
    /**
     * 别名
     */
    private String otherName;
    private String type;
    private Spec spec;
    private String sealKey;
    private String createTime;
    private Status status;
    private int useCount;
    private String category;
    @Data
    public static class Spec {
        private int height;
        private int width;
        private String type;
        private String key;
        private double pixelWidth;
        private double pixelHeight;
        private String label;
    }
    @Data
    public static class Status {
        private String description;
        private String key;
    }
}

