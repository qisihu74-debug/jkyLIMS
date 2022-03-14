package com.lims.manage.erp.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import com.lims.manage.erp.vo.TestReportQualifcationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestReportQualifcation;

/**
 * (TestReportQualifcation)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
@Mapper
public interface TestReportQualifcationDao extends BaseMapper<TestReportQualifcation> {
    IPage<TestReportQualifcationVo> getListPage(IPage<TestReportQualifcationVo> page, @Param(Constants.WRAPPER) Wrapper<TestReportQualifcation> queryWrapper);
}

