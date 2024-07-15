package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DivideEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-07-10 11:31
 * @Copyright © 河南交科院
 */
@Mapper
@Repository
public interface DivideDao extends BaseMapper<DivideEntity> {
}
