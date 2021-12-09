package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.vo.*;
import org.mapstruct.Mapper;
import org.springframework.data.repository.query.Param;
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
     *
     * @param record
     * @return
     */
    List<SampleEntity> selectSampleList(SampleEntity record);

    /**
     * 查询样品组基本信息
     *
     * @param insertFlag
     * @return
     */
    SampleDetailVo getSampleGroupInfo(String insertFlag);

    /**
     * 根据查询条件查询样品列表
     *
     * @param paramVo
     * @return
     */
    List<SampleDetailVo> selectSampleList2(SampleEntity paramVo);

    /**
     * 样品下检测依据
     */
    List<JudgmentBasisVo> selectTestStandardList(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 检测依据信息
     *
     * @param sampleId
     * @return
     */
    List<SampleItemEntity> selectSampleCheckItem(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);


    int updateByPrimaryKeySelective(SampleEntity record);

    int updateByPrimaryKey(SampleEntity record);

    /**
     * 更新样品基础信息
     *
     * @param record
     * @return
     */
    int updateSampleInfo(SampleEntity record);

    /**
     * 查询样品标签信息
     *
     * @param sampleId
     * @return
     */
    SampleDetailVo getSampleTagInfo(Integer sampleId);

    /**
     * 删除test_entrusted_sample_details_rel
     *
     * @return
     */
    int removeSamplesId(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 保存test_entrusted_sample_details_rel
     *
     * @return
     */
    int addSampleEntity(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    List<Integer> getSampleBasisSet(@Param(value = "sampleId") Integer sampleId, @Param(value = "entrustmentId") Long entrustmentId);

    /**
     * 查询样品公用信息
     *
     * @param paramVo
     * @return
     */
    List<SamplePublicInfoVo> getSamplePublicInfos(SampleDetailVo paramVo);

    /**
     * 查询当前年份最大样品编号
     *
     * @param year
     * @return
     */
    String getMaxNumber(String year);
}