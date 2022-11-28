package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.InstrumentRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface InstrumentRecordEntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InstrumentRecordEntity record);

    int insertSelective(InstrumentRecordEntity record);

    InstrumentRecordEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InstrumentRecordEntity record);

    int updateByPrimaryKey(InstrumentRecordEntity record);
}
