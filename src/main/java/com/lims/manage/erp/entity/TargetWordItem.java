package com.lims.manage.erp.entity;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/5/6 11:05
 * @Copyright © 河南交科院
 */
public class TargetWordItem extends TextLocal {

    //需要替换的关键字，替换后的内容和位置

    //文档中搜索的关键字
    private String key;

    //写入的内容
    private String value;

    //偏移的位置 1:上  2：右  3：下面   4：左边
    private int site = 2;

    //偏移的量
    private float size = 1;

    public TargetWordItem(String key, String value, float size) {
        super();
        this.key = key;
        this.value = value;
        this.size = size;
    }

    public TargetWordItem() {
        super();
    }

    public TargetWordItem(String key, String value, int site, float size) {
        super();
        this.key = key;
        this.value = value;
        this.site = site;
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
