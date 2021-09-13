package com.stu.manage.demo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stu.manage.demo.entity.Book;
import com.stu.manage.demo.mapper.BookMapper;
import com.stu.manage.demo.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.service.impl
 * @desc
 * @date 2021/8/22 11:30
 * @Copyright © jky
 */
@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private BookMapper bookMapper;
    Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    @Override
    public int insertBook(Book book) {
        QueryWrapper<Book> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("isbn",book.getIsbn());
        Book selectOne = bookMapper.selectOne(queryWrapper);
        if (selectOne != null){
            return -1;
        }else {
            return bookMapper.insert(book);
        }
    }

    @Override
    public List<Book> getList(String search) {
        QueryWrapper<Book> queryWrapper=new QueryWrapper<>();
        //指定条件
        queryWrapper.eq(StringUtils.isEmpty(search)?false:true,"isbn",search)
                .or()
                .eq(StringUtils.isEmpty(search)?false:true,"publisher",search)
                .or()
                .eq(StringUtils.isEmpty(search)?false:true,"author",search);
        return bookMapper.selectList(queryWrapper);
    }



}
