package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.CmaCapabilityItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CmaCapabilityItemMapper extends BaseMapper<CmaCapabilityItem> {

    @Select("SELECT DISTINCT domain FROM cma_capability_item ORDER BY domain")
    List<String> selectDomains();

    @Select("SELECT id, standard_code FROM cma_capability_item WHERE standard_code LIKE 'GB%' AND hcno IS NULL LIMIT #{limit}")
    List<CmaCapabilityItem> selectGbItemsWithoutHcno(int limit);
}
