package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.SampleDetailParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
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

    List<SampleEntity> selectSampleListGroup(Long entrustmentId);

    /**
     * 查询样品信息
     * @param record
     * @return
     */
    List<SampleEntity> selectSampleList(SampleEntity record);

    /**
     * 根据查询条件查询样品列表
     * @param record
     * @return
     */
    List<SampleDetailVo> selectSampleList2(SampleEntity paramVo);
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