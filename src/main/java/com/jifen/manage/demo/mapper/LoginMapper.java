package com.jifen.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jifen.manage.demo.entity.Login;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 */

@Component
@Mapper
public interface LoginMapper extends BaseMapper<Login> {

}
