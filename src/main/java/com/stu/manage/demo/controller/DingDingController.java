package com.stu.manage.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dingding/")
public class DingDingController {

    @RequestMapping("step1")
    public String getHello() {
        return "step1";
    }

}
