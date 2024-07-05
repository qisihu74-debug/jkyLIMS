package com.lims.manage.erp.controller;

import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.result.Result;
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

    /**
     * 技术质量部内审活动列表
     * @param name
     * @param time
     * @return
     */
    @GetMapping("internalAuditorActiveList")
    public Result internalAuditorActiveList(String name,String time){


        return null;
    }

}
