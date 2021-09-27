package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtSampleInfo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/26 9:30
 * 样品关联关系1
 */
@Component
@Mapper
public interface JtSampleInfoMapper {
    int insertSelective(JtSampleInfo record);
}
