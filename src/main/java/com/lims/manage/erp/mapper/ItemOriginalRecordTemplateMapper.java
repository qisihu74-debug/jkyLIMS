package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TestItemOriginalRecordTemplateRel;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemOriginalRecordTemplateMapper extends BaseMapper<TestItemOriginalRecordTemplateRel> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestItemOriginalRecordTemplateRel record);

    int insertSelective(TestItemOriginalRecordTemplateRel record);

    TestItemOriginalRecordTemplateRel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestItemOriginalRecordTemplateRel record);

    int updateByPrimaryKey(TestItemOriginalRecordTemplateRel record);

    /**
     * 通过检测项id 获取原始记录信息
     * @param checkItemId
     * @return
     */
    List<TestOriginalRecordTemplate> selectOriginalRecordList(@Param("checkItemId") Integer checkItemId);
}