package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Mapper
public interface SampleEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SampleEntity record);

    int insertSelective(SampleEntity record);

    SampleEntity selectByPrimaryKey(Integer id);

    List<SampleEntity> selectSampleListGroup(Integer entrustmentId);

    List<SampleEntity> selectSampleList(SampleEntity record);
    /**
     * 样品下检测依据
     * @param sampleId
     * @return
     */
    List<Integer> selectdardFileIds(Integer sampleId);
    /**
     *  检测依据信息
     * @param sampleId
     * @return
     */
    List<CheckItemInfoVo> selectSampleCheckItem(Integer sampleId);


    int updateByPrimaryKeySelective(SampleEntity record);

    int updateByPrimaryKey(SampleEntity record);

}