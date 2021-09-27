package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtSampleObject;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/24 15:12
 * 增加样品基本信息1
 */
@Component
@Mapper
public interface JtSampleObjectMapper {

    int insertSelective(JtSampleObject record);

}
