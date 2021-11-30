package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.EntrustAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("entrust")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;

    @RequestMapping("/addEntrust")
    public Result addEntrust(@RequestBody EntrustAddVo entrust){
        System.out.println(entrust);
        Boolean isSuccess = entrustService.addEntrust(entrust);
        if(isSuccess){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(678,"新增委托失败！");
        }
    }

    @RequestMapping("/getAllItem")
    public Result getAllItemByProductId(Integer productId){
        if(productId == null){
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(),ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }else{
            return ResultUtil.success(entrustService.getAllItemByProductId(productId));
        }
    }


}
