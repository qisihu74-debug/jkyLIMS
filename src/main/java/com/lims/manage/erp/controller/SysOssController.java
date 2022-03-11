package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.util.ShiroUtils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * OSS对象存储表(SysOss)表控制层
 *
 * @author makejava
 * @since 2022-03-10 16:13:34
 */
@RestController
@RequestMapping("sysOss")
public class SysOssController  {
    /**
     * 服务对象
     */
    @Resource
    private SysOssService sysOssService;


    @PostMapping("postAnnounce")
    public Result postAnnounce(@RequestPart("file") MultipartFile file) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        Map<String,Object> map = sysOssService.postAnnounce(file);
        map.put("userName",userInfo.getName());
        return ResultUtil.success(map);
    }
}

