package com.lims.manage.erp.controller;


import com.baomidou.mybatisplus.extension.api.ApiController;
import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.TestBillingRegistrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (TestControlledDocuments)表服务接口
 *
 * @author 丁连春
 * @since 2023-12-25 10:30:10
 */
@RestController
@RequestMapping("billing")
public class TestBillingRegistrationController {
    /**
     * 受控文件信息
     */
    @Resource
    private TestBillingRegistrationService testBillingRegistrationService;

    @GetMapping("/list")
    public Result getList(TestBillingRegistrationEntity registrationEntity) {
        return testBillingRegistrationService.list(registrationEntity);
    }


    @PostMapping("/update")
    public Result update(TestBillingRegistrationEntity registrationEntity) {
        return testBillingRegistrationService.list(registrationEntity);
    }


}

