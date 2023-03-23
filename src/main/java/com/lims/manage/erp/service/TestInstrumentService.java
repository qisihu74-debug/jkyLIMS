package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.InstrumentRecordListVo;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.TestInstrumentVo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 仪器设备(TestInstrument)表服务接口
 *
 * @author makejava
 * @since 2022-02-25 10:05:51
 */
public interface TestInstrumentService extends IService<TestInstrument> {
    Result addInstrument(TestInstrument testInstrument);
    Result updInstrument(TestInstrument testInstrument);
    Result delInstruments(List<Long> idList);
    IPage<TestInstrumentVo> getPageList(Page<TestInstrumentVo> page, QueryWrapper<TestInstrument> queryWrapper);

    /**
     * 查询设备使用记录
     * @param paramVo
     * @return
     */
    PageInfo getInstrumentRecord(InstrumentRecordParamVo paramVo);

    /**
     * 导出单个设备的使用记录
     * @param paramVo
     * @return
     */
    HashMap<String,Object> exportInstrumentRecord(InstrumentRecordParamVo paramVo);

    /**
     * 导出多个设备使用记录
     * @param instrumentIds
     * @return
     */
    HashMap<String,Object> batchExportInstrumentRecord(List<Long> instrumentIds);

    /**
     * 整理设备
     */
    void checkDevice();
}

