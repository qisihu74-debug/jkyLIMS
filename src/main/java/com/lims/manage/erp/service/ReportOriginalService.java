package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.ReportOriginalEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ReportOriginalService {
    /**
     * 新增报告原始记录模板
     *
     * @param entity
     * @return
     */
    int addReportOriginal(ReportOriginalEntity entity, MultipartFile file);
}
