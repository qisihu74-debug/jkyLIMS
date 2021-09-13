package com.stu.manage.demo.controller;

import com.stu.manage.demo.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.controller
 * @desc
 * @date 2021/9/3 15:32
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("redis")
@Slf4j
public class RedisController {
    @Autowired
    private RedisUtils redisUtils;
    public void test(){
        redisUtils.add("name","张三");
    }

}
