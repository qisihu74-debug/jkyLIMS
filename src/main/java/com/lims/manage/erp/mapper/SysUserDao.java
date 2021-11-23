package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Description 系统用户DAO
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Component
@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {
	
}
