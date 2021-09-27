package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.JtReportInfo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: DLC
 * @Date: 2021/9/27 10:28
 * 补充样品关联关系1
 */
@Component
@Mapper
public interface JtReportInfoMapper {

    int insertSelective(JtReportInfo record);

}
