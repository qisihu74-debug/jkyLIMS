package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.SampleSimpleListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TestSampleEntityMapper extends BaseMapper<TestSampleEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestSampleEntity record);

    int insertSelective(TestSampleEntity record);

    TestSampleEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestSampleEntity record);

    int updateByPrimaryKeyWithBLOBs(TestSampleEntity record);

    int updateByPrimaryKey(TestSampleEntity record);

    int updateByPrimaryKeyNotAll(TestSampleEntity record);

    /**
     * 批量新增样品
     *
     * @param entities
     * @return
     */
    int insertBatch(@Param("entities") List<TestSampleEntity> entities);

    /**
     * 样品查询/打印列表
     * @param entity
     * @return
     */
    List<SampleSimpleListVo> getSimpleList(TestSampleEntity entity);
}