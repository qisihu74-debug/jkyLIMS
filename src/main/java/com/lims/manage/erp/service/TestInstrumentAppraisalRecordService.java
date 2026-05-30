package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestInstrumentAppraisalRecord;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 设备仪器检定记录表(TestInstrumentAppraisalRecord)表服务接口
 *
 * @author makejava
 * @since 2022-03-01 11:44:18
 */
public interface TestInstrumentAppraisalRecordService extends IService<TestInstrumentAppraisalRecord> {
    Result addInstrumentAppraisalRecord(TestInstrumentAppraisalRecord testInstrumentAppraisalRecord);
    Result updInstrumentAppraisalRecord(TestInstrumentAppraisalRecord testInstrumentAppraisalRecord);
    Result delInstrumentAppraisalRecord(List<Long> idList);
}

