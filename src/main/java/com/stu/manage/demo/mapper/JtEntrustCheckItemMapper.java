package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtEntrustCheckItem;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/24 16:10
 * 委托单位下检测项1
 */
@Component
@Mapper
public interface JtEntrustCheckItemMapper {

    int insertSelective(JtEntrustCheckItem record);

    JtEntrustCheckItem selectCheckItem(@Param("checkItemId")Integer checkItemId);

}
