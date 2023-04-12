package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestInstrumentVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * 仪器设备(TestInstrument)表服务接口
 *
 * @author makejava
 * @since 2022-02-25 10:05:51
 */
public interface TestInstrumentService extends IService<TestInstrument> {
    Result addInstrument_old(TestInstrument testInstrument);

    Result updInstrument(TestInstrument testInstrument);

    Result delInstruments(List<Long> idList);

    IPage<TestInstrumentVo> getPageList(Page<TestInstrumentVo> page, QueryWrapper<TestInstrument> queryWrapper);

    /**
     * 查询设备使用记录
     *
     * @param paramVo
     * @return
     */
    PageInfo getInstrumentRecord(InstrumentRecordParamVo paramVo);

    /**
     * 导出单个设备的使用记录
     *
     * @param paramVo
     * @return
     */
    HashMap<String, Object> exportInstrumentRecord(InstrumentRecordParamVo paramVo);

    /**
     * 导出多个设备使用记录
     *
     * @param instrumentIds
     * @return
     */
    HashMap<String, Object> batchExportInstrumentRecord(List<Long> instrumentIds);

    /**
     * 整理设备
     */
    void checkDevice();
    /**********************************设备仪器202303****************************************/
    /**
     * 查询设备仪器列表
     * @param deviceEntity
     * @return
     */
    PageInfo<DeviceEntity> getAllDevice(DeviceEntity deviceEntity);

    /**
     * 新增设备
     * @param record
     * @param picture
     * @param contract
     * @param invoice
     * @return
     */
    boolean addDevice(DeviceEntity record, MultipartFile picture, MultipartFile contract, MultipartFile invoice);

    /**
     * 修改设备仪器
     * @param record
     * @return
     */
    boolean update(DeviceEntity record);

    /**
     * 删除设备
     * @param idList
     * @return
     */
    boolean deleteDevice(List<Long> idList);

    ServletOutputStream printDeviceLable(Integer id, TestInstrument testInstrument, HttpServletResponse response) throws Exception;

    /**
     * 查询设备下拉列表
     * @param search
     * @return
     */
    List<LabelValueVo> getDeviceList(String search);
}

