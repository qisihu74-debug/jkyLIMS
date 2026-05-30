package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 检测方法(TestMethod)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 10:04:05
 */
public interface TestMethodService extends IService<TestMethod> {
    Result addTestMethod(TestMethod TestMethod);
    Result updTestMethod(TestMethod TestMethod);
    Result delTestMethod(List<Long> idList);
}

