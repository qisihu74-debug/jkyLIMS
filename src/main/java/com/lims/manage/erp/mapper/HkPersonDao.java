package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.HkPerson;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-09-13 15:12
 * @Copyright © 河南交科院
 */
public interface HkPersonDao extends BaseMapper<HkPerson> {

    List<HkPerson> personList(@Param("name") String name, @Param("mobile") String mobile, @Param("state") String state);
}
