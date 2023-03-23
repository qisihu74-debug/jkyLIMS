package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeviceEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DeviceEntity record);

    int insertSelective(DeviceEntity record);

    DeviceEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DeviceEntity record);

    int updateByPrimaryKey(DeviceEntity record);

    /**
     * 整理设备
     * @return
     */
    List<DeviceEntity> getNewDevices();
    List<DeviceEntity> getOldDevices();
    List<DeviceEntity> getOldDevicesByCode(String code);
    int batchInsert(@Param("items") List<DeviceEntity> items);
    /************************************************************/
}