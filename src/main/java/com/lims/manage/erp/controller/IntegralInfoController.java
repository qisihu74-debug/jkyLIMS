package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.IntegralInfoService;
import com.lims.manage.erp.vo.UserIntegralRankingListVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 积分对应称号徽章信息
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/integralInfo")
public class IntegralInfoController extends ApiController {

    @Resource
    private IntegralInfoService integralInfoService;

    /**
     * 获取当前登录用户的称号与徽章信息
     *
     * @return Result
     */
    @GetMapping(value = "/getUserIntegralInfo")
    public Result<?> getUserIntegralInfo() {
        return integralInfoService.getUserIntegralInfo();
    }

    /**
     * 获取用户积分排行榜信息
     *
     * @param pageNo   页码
     * @param pageSize 条数
     * @return Result<?>
     */
    @GetMapping(value = "/getIntegralRankingList")
    public Result<?> getIntegralRankingList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        String integralType = "1";
        Page<UserIntegralRankingListVo> page = new Page<>(pageNo, pageSize);
        IPage<UserIntegralRankingListVo> pageList = integralInfoService.getIntegralRankingList(page, integralType);
        return ResultUtil.success("获取积分排行榜", pageList);
    }

    /**
     * 获取分享者简介
     * @param userId 分享者ID
     * @return Result
     */
    @GetMapping(value = "getShareUserInfo")
    public Result<?> getShareUserInfo(@RequestParam(name = "userId")String userId){
        return ResultUtil.success(integralInfoService.getShareUserInfo(userId));
    }
}
