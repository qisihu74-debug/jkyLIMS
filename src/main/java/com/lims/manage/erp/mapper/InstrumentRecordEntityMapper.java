package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.InstrumentRecordEntity;
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

}
