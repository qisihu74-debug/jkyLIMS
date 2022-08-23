package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.CodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2022-08-18 14:55
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface CodeDao extends BaseMapper<CodeEntity> {
}
