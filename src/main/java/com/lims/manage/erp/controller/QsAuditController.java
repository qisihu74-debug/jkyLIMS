package com.lims.manage.erp.controller;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.QsAuditService;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 部门负责人（部长/所长/主任）；内审员（具有内审角色的账户）
 * @date 2024-07-04 15:56
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/audit/")
public class QsAuditController {
    @Autowired
    private QsAuditService qsAuditService;

    /**
     * 技术质量部内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("internalAuditorActiveList")
    public Result internalAuditorActiveList(Integer pageNum, Integer pageSize, String name){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少参数");
        }
        Long userId = ShiroUtils.getUserInfo().getUserId();
        PageInfo<InternalAuditorActive> pageInfo = qsAuditService.internalAuditorActiveList(pageNum,pageSize,name,userId);
        return ResultUtil.success(pageInfo);
    }

}
