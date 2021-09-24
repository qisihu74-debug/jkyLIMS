package com.stu.manage.demo.controller;

import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.EntrustService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/entrust/")
public class EntrustController {
    Logger logger = LoggerFactory.getLogger(EntrustController.class);
    @Autowired
    private EntrustService entrustService;

    @GetMapping("once_more")
    public Result onceMore(int entrustId){
        return ResultUtil.success(entrustService.onceMore(entrustId));
    }

    @GetMapping("get_check_items")
    public Result getCheckItems(int productId){
        return ResultUtil.success(entrustService.getCheckItemsByProductId(productId));
    }
}
