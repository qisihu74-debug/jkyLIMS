package com.lims.manage.erp.entity;

/**
 * @author gjl
 */
public class KeyPosition {
    /**
     * 关键字所在的页码
     */
    private int pageNum;
    /**
     * 关键字x坐标
     */
    private float x;
    /**
     * 关键字y坐标
     */
    private float y;

    public KeyPosition(int pageNum, float x,float y){
        this.pageNum = pageNum;
        this.x = x;
        this.y = y;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
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

    @Override
    public String toString() {
        return "KeyPosition{" +
                "pageNum=" + pageNum +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
