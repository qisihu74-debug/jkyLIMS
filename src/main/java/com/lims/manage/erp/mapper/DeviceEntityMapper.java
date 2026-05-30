package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeviceEntityMapper extends BaseMapper<DeviceEntity> {
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
    /**
     * 查询设备仪器列表
     * @param deviceEntity
     * @return
     */
    List<DeviceEntity> getAllDevice(DeviceEntity deviceEntity);

    /**
     * 设备新ID
     * @return
     */
    Integer getNewId();

    /**
     * 批量删除设备
     * @param idList
     * @return
     */
    int deleteByIds(@Param("idList") List<Long> idList);

    /**
     * 查询设备下拉列表
     * @return
     */
    List<LabelValueVo> getDeviceList(@Param("search") String search);

    @Select("SELECT id from test_instrument where del_flag=0")
    List<Integer> getAllIds();

    void updateByIf(DeviceEntity deviceEntity);
}
