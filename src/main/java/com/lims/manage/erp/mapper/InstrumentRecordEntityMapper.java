package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.InstrumentRecordEntity;
import com.lims.manage.erp.entity.InstrumentUseGroup;
import com.lims.manage.erp.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Mapper
@Component
public interface InstrumentRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InstrumentRecordEntity record);

    int insertSelective(InstrumentRecordEntity record);

    InstrumentRecordEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InstrumentRecordEntity record);

    int updateByPrimaryKey(InstrumentRecordEntity record);

    /**
     * 查询指定设备的使用记录
     * @param vo
     * @return
     */
    List<InstrumentRecordVo> getInstrumentRecordByTime(DeviceUseTimeVo vo);

    /**
     * 校验时间是否冲突
     * @param vo
     * @return
     */
    List<InstrumentRecordEntity> checkTime(DeviceUseTimeVo vo);

    /**
     * 查询设备仪器使用记录
     * @param paramVo
     * @return
     */
    List<InstrumentRecordListVo> getInstrumentRecord(InstrumentRecordParamVo paramVo);

    /**
     * 获取设备的code,name,model
     * @param instrumentId
     * @return
     */
    String getInstrumentInfo(Long instrumentId);

    /**
     * 删除检测项的设备使用记录
     * @param escRelId
     * @return
     */
    int deleteByEscRelId(Integer escRelId);

    /***
     * 通过记录id 查询仪器详情
     * @param id
     * @return
     */
    @Select("SELECT\n" +
            "\tt1.id as recordId,\n" +
            "\t t2.device_state as deviceState,\n" +
            "\tt2.id,\n" +
            "\tt2.name,\n" +
            "\tt2.code,\n" +
            "\tt1.temperature as environmentTemperature, \n" +
            "\tt1.humidity as ambientHumidity,\n" +
            "\tt1.user\n" +
            "FROM\n" +
            "\ttest_instrument_use_record as t1 \n" +
            "\tLEFT JOIN test_instrument as t2 ON t1.instrument_id = t2.id \n" +
            "WHERE\n" +
            "\tt1.id = #{id} ")
    InstrumentAppVo selectRecordDetails(@Param("id")Long id);

    /**
     *  获取记录id、仪器id、检测项id、taskId
     * @param list 记录id集合
     * @return
     */
    List<InstrumentAppVo> getIds(@Param("list") List<InstrumentRecordListVo> list);

    /**
     * 校验设备使用状态
     * @param instrumentId
     * @return
     */
    InstrumentRecordEntity checkDeviceStatus(Long instrumentId);

    /**
     * 校验设备开始使用时间
     * @param instrumentId
     * @return
     */
    InstrumentRecordEntity checkDeviceStartTime(Long instrumentId,Date startTime);

    /**
     * 查询设备的使用情况
     * @param instrumentId
     * @return
     */
    InstrumentUseGroup checkDeviceUseInfo(Long instrumentId);

    /**
     * 当前设备的组队信息
     * @param instrumentId
     * @return
     */
    List<InstrumentUseGroup> getGroupInfo(Long instrumentId);

    /**
     * 设备是否已经开始使用
     * @param instrumentId
     * @return
     */
    int taskStatus(Long instrumentId);

    /**
     * 设备当前占用并行数
     * @param instrumentId
     * @return
     */
    Integer useSize(Long instrumentId);

    /**
     * 新增组队信息
     * @param group
     * @return
     */
    int insertGroup(InstrumentUseGroup group);

    /**
     * 批量新增组队信息
     * @param records
     * @return
     */
    int batchInsertGroup(@Param("records") List<InstrumentUseGroup> records);

    /**
     * 退出组队
     * @param group
     * @return
     */
    int deleteGroup(InstrumentUseGroup group);

    /**
     * 删除队伍
     * @param instrumentId
     * @return
     */
    int deleteGroupByInstrumentId(Long instrumentId);

    /**
     * 批量新增设备使用记录
     * @param records
     * @return
     */
    int batchInsert(@Param("records")List<InstrumentRecordEntity> records);

    /**
     * 更新队伍试验状态
     * @param instrumentId
     * @return
     */
    int updateGroupState(Long instrumentId);

    /**
     * 更新使用记录完成时间，设备状态
     * @param escRelIds
     * @param intrusmentId
     * @param endTime
     * @param deviceState
     * @return
     */
    int updateRecordEndTime(@Param("escRelIds") List<Long> escRelIds,@Param("intrusmentId") Long intrusmentId,
                            @Param("endTime") Date endTime,@Param("deviceState") String deviceState);

    /**
     * 查询指定时间内使用的时间信息
     * @param instrumentVo
     * @return
     */
    List<InstrumentRecordVo> getInstrumentUseTime(InstrumentVo instrumentVo);

    /**
     * 查询插单的记录信息
     * @param instrumentVo
     * @return
     */
    InstrumentRecordEntity getRecordInfo(InstrumentVo instrumentVo);

    /**
     * 查询队伍信息
     * @param group
     * @return
     */
    InstrumentUseGroup getGroupInfoDetail(InstrumentUseGroup group);

    /**
     * 批量更新组队
     * @param list
     * @return
     */
    int batchUpdateGroup(@Param("list") List<InstrumentUseGroup> list);
}
