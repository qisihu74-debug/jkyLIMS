package com.jifen.manage.demo.controller;

import com.jifen.manage.demo.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.controller
 * @desc
 * @date 2021/10/25 14:16
 * @Copyright © 河南交科院
 */

@RestController
@Slf4j
@RequestMapping("/admin/")
public class IntegralController {

    @GetMapping
    public Result test(){
        return null;
    }
}
