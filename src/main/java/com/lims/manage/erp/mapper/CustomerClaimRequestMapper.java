package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.CustomerClaimRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface CustomerClaimRequestMapper extends BaseMapper<CustomerClaimRequestEntity> {
}
