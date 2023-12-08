package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.vo.TorttpiVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 原始记录模板(TestOriginalRecordTemplate)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-16 14:12:38
 */
@Mapper
public interface TestOriginalRecordTemplateDao extends BaseMapper<TestOriginalRecordTemplate> {

  IPage<TorttpiVo> getPageList(IPage<TorttpiVo> page, @Param(Constants.WRAPPER) Wrapper<TestOriginalRecordTemplate> queryWrapper);

  @Select("select IFNULL(MAX(id),0)+1 from test_original_record_template")
  Integer getMaxId();

  TestOriginalRecordTemplate getDetail(Integer id);

  int insertRecord(TestOriginalRecordTemplate testOriginalRecordTemplate);

  int deleteById(Integer id);

  List<TestOriginalRecordTemplate> getRecordList(Integer pid);
}

