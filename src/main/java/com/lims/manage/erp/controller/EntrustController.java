package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.EntrustAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.service.EntrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entrust/")
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
    @GetMapping("get_Basics")
    public Map<String,Object> ReturnBasicsData()
    {
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("msg","查询数据成功");
        map.put("data",entrustService.returnEntrustData());
        return map;
    }
    @GetMapping("get_entrusted_unit")
    public Map<String,Object> methodDispay(Integer companyId)
    {
        Map<String,Object> map = new HashMap<>();
        List<TestCustomerJsonEntity> collectList = entrustService.returnTestCustomerEntityList(companyId);
        if(collectList.isEmpty()){
            map.put("code",201);
            map.put("msg","用户信息不存在");
            return map;
        }
        map.put("code",200);
        map.put("msg","查询数据成功");
        map.put("data",collectList);
        return map;
    }
    @PostMapping("add_new_company")
    public Map<String,Object> methodPost(@RequestBody TestCompanyJsonEntity testCompanyEntity)
    {
        Map<String,Object> map = new HashMap<>();
        System.out.println("展示数据\t"+testCompanyEntity);
        boolean BooleStatus  = entrustService.addCompanyData(testCompanyEntity);
        if(BooleStatus){
            map.put("code",200);
            map.put("msg","增加数据成功");
            return map;
        }
        map.put("code",201);
        map.put("msg","增加数据失败");
        return map;
    }




}
