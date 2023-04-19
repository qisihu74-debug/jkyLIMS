package com.lims.manage.erp.service.impl;

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
        int insert = reportOriginalEntityMapper.insert(entity);
        return insert;
    }
}
