package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtEntrustCheckItem;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/9/24 16:10
 * 委托单位下检测项1
 */
@Component
@Mapper
public interface JtEntrustCheckItemMapper {

    int insertSelective(JtEntrustCheckItem record);

    // 实现批量增加检测项
    int insertSelectiveList(List<JtEntrustCheckItem> list );

    JtEntrustCheckItem selectCheckItem(@Param("checkItemId")Integer checkItemId);

}
