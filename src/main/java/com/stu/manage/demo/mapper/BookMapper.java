package com.stu.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.manage.demo.entity.Book;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.mapper
 * @desc
 * @date 2021/8/22 11:31
 * @Copyright © jky
 */
@Component
@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
