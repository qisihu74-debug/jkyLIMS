package com.stu.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.manage.demo.entity.CrmEntity;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.mapper
 * @desc
 * @date 2021/9/18 9:51
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface CrmMapper extends BaseMapper<CrmEntity> {
    /**
     * 获取最大序号
     * @return
     */
    String getMaxIndex();

    /**
     * 批量保存crm数据
     * @param list
     */
    void BatchSave(@Param("list") List<CrmEntity> list);
}
