package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.StandardMethodEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface StandardMethodEntityMapper {
    int deleteByPrimaryKey(Integer standardId);

    int insert(StandardMethodEntity record);

    int insertSelective(StandardMethodEntity record);

    StandardMethodEntity selectByPrimaryKey(Integer standardId);

    int updateByPrimaryKeySelective(StandardMethodEntity record);

    int updateByPrimaryKey(StandardMethodEntity record);

    /**
     * 查询依据下的方法
     * @param standardId
     * @return
     */
    List<StandardMethodEntity> getByStandardId(Integer standardId);

    /**
     * 批量新增检测方法
     * @param records
     * @return
     */
    int batchInsert(@Param("records")List<StandardMethodEntity> records);
}