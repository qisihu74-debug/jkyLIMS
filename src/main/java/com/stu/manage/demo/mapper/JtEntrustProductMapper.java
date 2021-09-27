package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtEntrustProduct;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/26 14:35
 * 保存委托和产品关系
 */
@Component
@Mapper
public interface JtEntrustProductMapper {
    int insertSelective(JtEntrustProduct record);
}
