package com.stu.manage.demo.mapper;

import com.stu.manage.demo.entity.EntrustInfo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface EntrustMapper {

    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);
}
