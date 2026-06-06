package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.CmaSyncLog;
import org.apache.ibatis.annotations.Select;

public interface CmaSyncLogMapper extends BaseMapper<CmaSyncLog> {

    @Select("SELECT * FROM cma_sync_log WHERE status = 'SUCCESS' ORDER BY sync_time DESC LIMIT 1")
    CmaSyncLog selectLastSuccess();
}
