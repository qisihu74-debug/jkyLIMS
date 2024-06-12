package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SnEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @Description: 自定义编号
 * @Author: zhq
 * @Date:   2023-06-13
 * @Version: V1.0
 */
public interface SnEntityMapper extends BaseMapper<SnEntity> {

    @Update("update sys_serial_number_record set status=#{status} where type=#{receiptType}")
    void updateResetByTypeAndTenantId(@Param("receiptType") String receiptType,@Param("status") int status);
}
