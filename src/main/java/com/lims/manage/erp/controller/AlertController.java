package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.AlertEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2022/5/31 15:59
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/alert/")
public class AlertController {
    @Autowired
    private AlertService alertService;

    /**
     * 获取报告的告警信息
     * @param entrustId
     * @return
     */
    @GetMapping("alertList")
    public Result delete(Long entrustId) {
        if (entrustId == null){
            return ResultUtil.error("缺少必要参数");
        }
        LambdaQueryWrapper<AlertEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertEntity::getEntrustId,entrustId);
        List<AlertEntity> list = alertService.list(queryWrapper);
        return ResultUtil.success(list);
    }

}
