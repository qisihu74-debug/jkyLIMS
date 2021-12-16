package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestChItemInstrumentMiddleEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:30
 * 开始试验检测
 */
@Component
@Mapper
public interface TestDetectionDao {

    /**
     * 依据检测项 选择设备仪器
     * @param checkItemId
     * @return
     */
    List<TestInstrumentEntity> selectTheInstrument(Integer checkItemId);
    /**
     * 进行 修改检测项
     */
    int updateSampleItemInstrumentEntity(SampleItemInstrumentEntity sampleItemInstrumentEntity);
    /**
     * 新增 检测项id 多个仪器id
     */
    int addItemInstrumentMiddleRel(TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity);
    /**
     * 根据检测项 主键  获取（test_entrusted_sample_checkitem_rel）详情信息
     *
     */
    SampleItemInstrumentEntity getTestEntrustedSampleCheckitemRelDetail(Integer id);




}
