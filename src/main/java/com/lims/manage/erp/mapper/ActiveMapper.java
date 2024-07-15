package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.QsActiveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Description: 内审基础信息
 * @Author: DLC
 * @Date: 2024/7/10 16:17
 */
@Mapper
@Repository
public interface ActiveMapper extends BaseMapper<QsActiveEntity> {
}
