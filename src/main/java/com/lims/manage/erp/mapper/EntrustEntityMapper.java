package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustSampleEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface EntrustEntityMapper extends BaseMapper {
    /**
     * 获取最大委托单编号
     * @return
     */
    Integer selectMaxNo();

    /**
     * 保存委托单下样品信息
     * @param list
     */
    void BatchSaveEntrustSample(List<EntrustSampleEntity> list);

    /**
     * 保存委托单样品，判定依据信息
     * @param list1
     */
    void BatchSaveSampleStandard(List<EntrustSampleEntity> list1);
}