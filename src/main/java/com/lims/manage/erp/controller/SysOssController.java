package com.lims.manage.erp.controller;

import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS对象存储表(SysOss)表控制层
 *
 * @author makejava
 * @since 2022-03-10 16:13:34
 */
@Slf4j
@RestController
@RequestMapping("sysOss")
public class SysOssController  {
    /**
     * 服务对象
     */
    @Resource
    private SysOssService sysOssService;
    @Resource
    private LogManagerService logManagerService;

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

    @PostMapping("delAnnounce")
    public Result delFile(@RequestBody String fileUrl) {
        log.info("管理员:"+ShiroUtils.getUserInfo().getUsername()+"删除文件："+fileUrl);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("userName",userInfo.getName());
        Boolean flag=sysOssService.delAnnounce(fileUrl);
        map.put("delIsOk",flag);
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除报告模板"+fileUrl+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
        return ResultUtil.success(map);
    }
}

