package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/5/6 10:13
 * @Copyright © 河南交科院
 */
public class TextLocal {

    /**
     * 关键字在PDF中的X坐标
     */
    private float x;

    /**
     * 关键字在PDF中的Y坐标
     */
    private float y;

    /**
     * 关键字在PDF中的页码
     */
    private int pageNum;

    /**
     * 关键字在PDF中的显示出来的长度
     */
    private float length;

    /**
     * 具体内容
     */
    private String content;

    public TextLocal(){}

    public TextLocal(float x, float y, int pageNum) {
        this.x = x;
        this.y = y;
        this.pageNum = pageNum;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
