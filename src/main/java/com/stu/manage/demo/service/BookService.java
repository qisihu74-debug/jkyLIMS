package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.Book;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service
 * @desc
 * @date 2021/8/22 11:30
 * @Copyright © jky
 */
public interface BookService {

    /**
     * 插入图书
     * @param book
     * @return
     */
    int insertBook(Book book);

    /**
     * 获取图书列表
     * @param search
     * @return
     */
    List<Book> getList(String search);
}
