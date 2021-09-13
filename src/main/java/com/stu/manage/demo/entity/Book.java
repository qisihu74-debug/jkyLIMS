package com.stu.manage.demo.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/8/22 11:04
 * @Copyright © jky
 */
@Data
public class Book {
    /**
     * 版本
     */
    private String edition;
    /**
     * 发布时间
     */
    private String pubdate;
    /**
     * 纸质类型
     */
    private String paper;
    /**
     * 封面
     */
    private String img;
    /**
     * 简介
     */
    private String gist;
    /**
     * 格式
     */
    private String format;
    /**
     * 出版社
     */
    private String publisher;
    /**
     * 作者
     */
    private String author;
    /**
     * 书名
     */
    private String title;
    /**
     * 价钱
     */
    private String price;
    /**
     * 页数
     */
    private String page;
    /**
     * 条形码
     */
    private String isbn;
    private String binding;
    private String produce;
    /**
     * 图书室
     */
    private String library;
    /**
     * 责任人
     */
    private String liableer;
    /**
     * 图书资料贡献人
     */
    private String devoteer;
    /**
     * 备注
     */
    private String note;
    /**
     * 图书资料状态
     */
    private String stat;
    /**
     * 借阅次数
     */
    private String num;
    /**
     * 评论
     */
    private String comment;
    /**
     * 标签
     */
    private String label;
    /**
     * 启用日期
     */
    private String date;
    /**
     * 实例标题
     */
    private String strengthTitle;
    /**
     * 提交人组织
     */
    private String organization;
    /**
     * 提交人
     */
    private String submitter;
    /**
     * 打分
     */
    private String score;
    /**
     * 当前借阅人
     */
    private String borrower;


}
