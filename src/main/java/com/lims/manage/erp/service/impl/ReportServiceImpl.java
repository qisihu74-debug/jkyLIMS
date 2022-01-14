package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.mapper.ReportMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordDetailEntityMapper;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.vo.ReportDetailVo;
import com.lims.manage.erp.vo.ReportListVo;
import com.lims.manage.erp.vo.ReportPreserveVo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private ReportRecordEntityMapper entityMapper;

    @Autowired
    private ReportRecordEntityMapper recordEntityMapper;
    @Autowired
    private ReportRecordDetailEntityMapper recordDetailEntityMapper;

    @Override
    public List<ReportListVo> getReportList() {
        return reportMapper.getReportList();
    }

    @Override
    public ReportDetailVo getReportDetail(Long id) {
        return reportMapper.getReportDetail(id);
    }

    @Transactional
    @Override
    public Boolean preserve(ReportPreserveVo vo) {
        int insert = recordEntityMapper.insert(new ReportRecordEntity(vo));
        if (insert < 1) {
            return false;
        }
        List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
        for (ReportRecordDetailEntity e : checkInfos) {
            int insert1 = recordDetailEntityMapper.insert(e);
            if (insert1 < 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<ReportRecordEntity> list = entityMapper.getSealList(type,search);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Boolean seal(List<String> list,Long id) {
        //TODO 根据印章类型，请求契约所的印章

        //上传印章图片到文件服务器
        String img1 = "http://192.168.2.35:9000/seal-cns-cma/cns.jpg";
        String img2 = "http://192.168.2.35:9000/seal-cns-cma/cma.jpg";
        String url = img1+","+img2;
        //更新url数据到表test_report_record
        entityMapper.updateImgByid(id,url);
        return true;
    }

    @Override
    public XWPFDocument preview(Map<String, Object> map, InputStream object) {

        return null;
    }
}
