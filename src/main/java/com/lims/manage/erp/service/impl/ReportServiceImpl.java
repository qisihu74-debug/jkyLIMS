package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<ReportListVo> getReportList() {
        return reportMapper.getReportList();
    }

    @Override
    public ReportDetailVo getReportDetail(Long id) {
        return reportMapper.getReportDetail(id);
    }

    @Override
    public PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<ReportRecordEntity> list = reportMapper.getSealList(type,search);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }
}
