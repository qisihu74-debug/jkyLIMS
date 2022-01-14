package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ReportService {
    /**
     * 查询可制作报告列表
     *
     * @return
     */
    List<ReportListVo> getReportList();

    /**
     * 查询委托单--报告制作详情
     *
     * @param id
     * @return
     */
    ReportDetailVo getReportDetail(Long id);

    /**
     * 保存信息
     *
     * @param vo
     * @return
     */
    Boolean preserve(ReportPreserveVo vo);

    /**
     * 待盖章和历史盖章列表查询
     *
     * @param type
     * @param search
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize);

    /**
     * 获取要盖的印章
     * @param list
     * @param id
     * @return
     */
    Boolean seal(List<String> list,Long id);

    /**
     * 报告预览
     * @param map
     * @param object
     * @return
     */
    XWPFDocument preview(Map<String, Object> map, InputStream object);
}
