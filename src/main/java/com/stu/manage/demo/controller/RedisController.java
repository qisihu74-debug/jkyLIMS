package com.stu.manage.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stu.manage.demo.entity.Login;
import com.stu.manage.demo.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/redis/")
@Slf4j
public class RedisController {
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 测试
     */
    @GetMapping("test")
    public void test(){
        Login login = new Login();
        login.setAdminName("张三");
        login.setPassWord("11111");
        redisTemplate.opsForValue().set("user", login);
        System.out.println("-------------"+redisTemplate.opsForValue().get("user"));


        redisUtils.set("李四","11233");
        System.out.println("========"+redisUtils.get("李四"));
    }

}
