package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface TestSampleMixInfoEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TestSampleMixInfoEntity record);

    int insertSelective(TestSampleMixInfoEntity record);

    TestSampleMixInfoEntity selectByPrimaryKey(Integer id);

    TestSampleMixInfoEntity selectBySampleId(Integer sampleId);

    int updateByPrimaryKeySelective(TestSampleMixInfoEntity record);

    int updateByPrimaryKey(TestSampleMixInfoEntity record);

    int updateBySampleId(TestSampleMixInfoEntity record);

    @Update("update test_sample_mix_info set design_strength=#{item.designStrength},\n" +
            "intensity_configuration=#{item.intensityConfiguration},\n" +
            "antifreeze_level=#{item.antifreezeLevel},\n" +
            "water_binder_ratio=#{item.waterBinderRatio},\n" +
            "unit_water_use=#{item.unitWaterUse},\n" +
            "sand_ratio=#{item.sandRatio},\n" +
            "design_slump=#{item.designSlump},\n" +
            "mixing_way=#{item.mixingWay} where entrustment_id = #{entrustId}")
    void updateByEntrustId(@Param("entrustId") String entrustId, @Param("item") TestSampleMixInfoEntity item);
}