package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysUserDeptMiddleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface SysUserDeptMiddleEntityMapper extends BaseMapper<SysUserDeptMiddleEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(SysUserDeptMiddleEntity record);

    int insertSelective(SysUserDeptMiddleEntity record);

    SysUserDeptMiddleEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysUserDeptMiddleEntity record);

    int updateByPrimaryKey(SysUserDeptMiddleEntity record);
}