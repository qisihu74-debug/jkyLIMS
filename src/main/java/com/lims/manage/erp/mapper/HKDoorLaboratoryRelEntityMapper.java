package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.HKDoorLaboratoryRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HKDoorLaboratoryRelEntityMapper extends BaseMapper<HKDoorLaboratoryRelEntity> {

    /**
     * 获取门禁标识去重后的 层级门禁code表示
     *
     * @return
     */
    List<String> selectIndexCodeHierarchys(@Param("indexCodes") List<String> indexCodes);

}