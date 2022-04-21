package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.SampleJudgeBasisVo;
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

    List<TestSampleEntity> selectByPid(Integer id);

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
     * 批量新增配合比样品
     *
     * @param entities
     * @return
     */
    int insertBatchMixSamples(@Param("entities") List<TestSampleEntity> entities);

    /**
     * 样品查询/打印列表
     * @param entity
     * @return
     */
    List<SampleSimpleListVo> getSimpleList(TestSampleEntity entity);

    /**
     * 新增委托--导入样品列表--带判定依据
     * @param entity
     * @return
     */
    List<SampleJudgeBasisVo> querySampleJudgeBasisList(TestSampleEntity entity);

    /**
     * 查询样品表当前最大ID
     * @return
     */
    Integer getMaxId();

    List<Integer> getAllNodeIds(Integer sampleId);
}