package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestInstrumentType;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 仪器大类(TestInstrumentType)表服务接口
 *
 * @author makejava
 * @since 2022-03-01 09:14:39
 */
public interface TestInstrumentTypeService extends IService<TestInstrumentType> {
    Result addTestInstrumentType(TestInstrumentType testInstrumentType);
    Result updTestInstrumentType(TestInstrumentType testInstrumentType);
    Result delTestInstrumentType(List<Long> idList);
}

