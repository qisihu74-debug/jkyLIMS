package com.stu.manage.demo.controller;

import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.jtEntrustType;
import com.stu.manage.demo.mapper.JtEntrustInfoMapper;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.EntrustService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/entrust/")
public class EntrustController {
    Logger logger = LoggerFactory.getLogger(EntrustController.class);
    @Autowired
    private EntrustService entrustService;
    @Autowired
    private JtEntrustInfoMapper jtEntrustInfoMapper;

    @GetMapping("once_more")
    public Result onceMore(int entrustId){
        return ResultUtil.success(entrustService.onceMore(entrustId));
    }

    @GetMapping("get_check_items")
    public Result getCheckItems(int productId){
        return ResultUtil.success(entrustService.getCheckItemsByProductId(productId));
    }
    @GetMapping("get_check_basis")
    public Result getCheckBasis(int productId){
        return ResultUtil.success(entrustService.getCheckBasisByProductId(productId));
    }



    @RequestMapping(value="/add",method = RequestMethod.POST)
    public void add(@RequestBody HashMap<String,Object> map)
    {
        System.out.println("展示存储信息！   "+map);
        int status = entrustService.addEntrustInfo(map);
    }

    /**
     * 返回委托方式
     * @return
     */
    @GetMapping("get_select_lists")
    public Result getEntrustTheWay() {
        return ResultUtil.success(entrustService.getSelectLists());
    }
}
