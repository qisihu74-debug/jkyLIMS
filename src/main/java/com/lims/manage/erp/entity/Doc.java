package com.lims.manage.erp.entity;


/**
 * @author gjl
 */
public interface Doc {
    /**
     *
     * @param imagePath 图片路径
     * @param pageNum   文档页码
     * @param x         添加图片的x坐标
     * @param y         添加图片的y坐标
     * @param width     图片的宽
     * @param height    图片的高
     * @throws Exception
     */
    void addImage(String imagePath, int pageNum, float x, float y, int width, int height) throws Exception;

    /**
     *
     * @param imagePath 图片路径
     * @param key       定位的关键字
     * @param width     图片的宽
     * @param height    图片的高
     * @throws Exception
     */
    void addImage(String imagePath, String key, float offsetX, float offsetY, int width, int height) throws Exception;
}
