package com.lims.manage.erp.controller;


import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.service.TestBillingRegistrationService;
import com.lims.manage.erp.vo.TestBillingRegistrationJSONVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


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

    /**
     * 编辑
     *
     * @param registrationEntity
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody TestBillingRegistrationEntity registrationEntity) {
        return testBillingRegistrationService.update(registrationEntity);
    }


    /**
     * 批量新增
     *
     * @param list
     * @return
     */
    @PostMapping("/batchAdd")
    public Result batchAdd(@RequestBody TestBillingRegistrationJSONVo registrationJSONVo) {
        return testBillingRegistrationService.batchAdd(registrationJSONVo.getList());
    }


}

