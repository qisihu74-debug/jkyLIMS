package com.stu.manage.demo.controller;

import com.stu.manage.demo.mapper.JtEntrustInfoMapper;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.EntrustService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

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

    /**
     * 根据委托单id查看委托单状态和所属样品检测进度
     * @param id
     * @return
     */
    @GetMapping("status")
    public Result status(Integer id){
        if (id == null){
            return ResultUtil.error(-1,"缺少必要参数");
        }
        return ResultUtil.success(entrustService.status(id));
    }




    @RequestMapping(value="/add",method = RequestMethod.POST)
    public Result add(@RequestBody HashMap<String,Object> map)
    {
        System.out.println("展示存储信息！   "+map);
        String status = entrustService.addEntrustInfo(map);
        if (status !=null)
        {
            return ResultUtil.success(status);
        }
         return ResultUtil.error(-1,"新增失败");
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
