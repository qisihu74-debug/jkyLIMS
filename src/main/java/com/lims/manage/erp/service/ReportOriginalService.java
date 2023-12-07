package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReportOriginalEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ReportOriginalService {
    /**
     * 新增报告原始记录模板
     *
     * @param entity
     * @return
     */
    int addReportOriginal(ReportOriginalEntity entity, MultipartFile file) throws UnsupportedEncodingException;
    int changeReportOriginal(ReportOriginalEntity entity, MultipartFile file) throws UnsupportedEncodingException;

    /**
     * 报告查询列表
     *
     * @param param
     * @return
     */
    PageInfo getReportList(ReportOriginalEntity param);

    /**
     * 修改报告模板
     *
     * @param entity
     * @param file
     * @return
     */
    int updateReportOriginal(ReportOriginalEntity entity, MultipartFile file);

    /**
     * 批量删除报告模板
     *
     * @param idList
     * @return
     */
    boolean deleteReportTemplate(List<Long> idList);

    /**
     * 查询报告模板下拉列表
     *
     * @param param
     * @return
     */
    List<LabelValueVo> getReportSelectList(String param);

    /**
     * 查询变更记录列表
     * @param pid
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo getReportRecordList(Long pid,Integer pageNum,Integer pageSize);
}
