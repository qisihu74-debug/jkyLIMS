package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 检验依据标准表(TestStandardFile)表服务接口
 *
 * @author makejava
 * @since 2022-03-09 10:22:55
 */
public interface TestStandardFileService extends IService<TestStandardFile> {


    Result addTestStandardFile(TestStandardFile testStandardFile);
    Result updTestStandardFile(TestStandardFile testStandardFile);
    Result delTestStandardFile(List<Long> idList);
}

