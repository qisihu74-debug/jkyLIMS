package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ReportOriginalEntity;
import com.lims.manage.erp.mapper.ReportOriginalEntityMapper;
import com.lims.manage.erp.service.ReportOriginalService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class ReportOriginalServiceImpl implements ReportOriginalService {
    @Autowired
    private ReportOriginalEntityMapper reportOriginalEntityMapper;

    @Override
    public int addReportOriginal(ReportOriginalEntity entity, MultipartFile file) {
        String upload = MinIoUtil.upload(BucketsConst.report_original, file, entity.getName());
        String url = upload.substring(0, upload.indexOf("?"));
        entity.setId(GenID.getID());
        entity.setCreateDate(new Date());
        entity.setUrl(url);
        return reportOriginalEntityMapper.insert(entity);
    }

    @Override
    public PageInfo<ReportOriginalEntity> getReportList(ReportOriginalEntity param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<ReportOriginalEntity> reportList = reportOriginalEntityMapper.getReportList(param);
        return new PageInfo<>(reportList);
    }
}
