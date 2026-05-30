package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TechnicistFiles;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2023-12-19 10:23
 * @Copyright © 河南交科院
 */
@Mapper
@Component
public interface TechnicistFilesDao extends BaseMapper<TechnicistFiles> {
}
