package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.InstrumentRecordEntity;
import com.lims.manage.erp.vo.DeviceUseTimeVo;
import com.lims.manage.erp.vo.InstrumentRecordListVo;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.InstrumentRecordVo;
import org.apache.ibatis.annotations.Mapper;
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
}
