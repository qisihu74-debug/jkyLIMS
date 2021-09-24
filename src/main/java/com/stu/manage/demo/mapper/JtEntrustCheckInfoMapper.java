package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtEntrustCheckInfo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/24 9:21
 * 委托价格信息
 */
@Component
@Mapper
public interface JtEntrustCheckInfoMapper {

    int insertSelective(JtEntrustCheckInfo record);


}
