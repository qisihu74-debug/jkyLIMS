package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.lims.manage.erp.entity.HomeAfficheEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HomeService;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/home/")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @RequestMapping("postAnnounce")
    public Result postAnnounce(@RequestParam(value = "json") String json, MultipartFile[] file)
    {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if(json==null){
            return ResultUtil.error("缺少必填参数！");
        }
        HomeAfficheEntity homeAfficheEntity =JSON.parseObject(json, HomeAfficheEntity.class);
        // 存储发布人姓名
        homeAfficheEntity.setIssuerName(userInfo.getName());
        homeAfficheEntity.setIssuerUserId(Long.parseLong(userInfo.getDingUserId()));
        boolean flag = homeService.postAnnounce(homeAfficheEntity,file);
        if(flag){
            return ResultUtil.success("发布成功！");
        }

        return ResultUtil.error("发布失败！");
    }

    /**
     * 所有人员——展示公告
     * @return
     */
    @GetMapping("showAnnounce")
    public Result showAnnounce(){
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        return ResultUtil.success(homeService.showAnnounce());
    }

}
