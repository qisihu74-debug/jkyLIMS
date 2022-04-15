package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleFileTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/4/15 10:02
 * 文件中间表 test_sample_file_table 操作
 */
@Component
@Mapper
public interface SampleFileTableDao {

    int insertSampleFileTableEntity(SampleFileTableEntity sampleFileTableEntity);

    /**
     * 根据样品id 查询文件集合。
     * @param sampleId
     * @return
     */
    List<SampleFileTableEntity> getSampleFileTableEntityList(Integer sampleId);

    /**
     * 删除单个文件id
     * @param id
     * @return
     */
    int deleteSampleFileTableEntity(Integer id);
}
