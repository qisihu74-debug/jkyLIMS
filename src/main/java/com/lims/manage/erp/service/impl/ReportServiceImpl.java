package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.vo.EntrustAddVo;
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
    private ReportTemplateEntityMapper templateEntityMapper;

    @Autowired
    private ReportRecordEntityMapper recordEntityMapper;
    @Autowired
    private ReportRecordDetailEntityMapper recordDetailEntityMapper;

    @Override
    public List<ReportListVo> getReportList() {
        return reportMapper.getReportList();
    }

    @Override
    public List<ReportListVo> getReportList_history(String search) {

        ReportListVo reportListVo = new ReportListVo();
        reportListVo.setTaskCode(search);
        return reportMapper.getReportList_history(reportListVo);
    }

    @Override
    public ReportDetailVo getReportDetail(Long id) {
        return reportMapper.getReportDetail(id);
    }

    @Transactional
    @Override
    public Boolean preserve(ReportPreserveVo vo) {
        ReportRecordEntity reportRecordEntity1 = recordEntityMapper.selectByEntrustId(vo.getEntrustmentId());
        if (reportRecordEntity1 != null) {
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(reportRecordEntity1.getId());
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                int insert1 = recordDetailEntityMapper.updateByRecordIdSelective(e);
                if (insert1 < 1) {
                    return false;
                }
            }
            reportRecordEntity1.setState(state);
            int update = recordEntityMapper.updateByEntrustIdSelective(reportRecordEntity1);
            if (update < 1) {
                return false;
            }
            return true;
        } else {
            long recordId = GenID.getID();
            String state = "1";
            List<ReportRecordDetailEntity> checkInfos = vo.getCheckInfos();
            for (ReportRecordDetailEntity e : checkInfos) {
                e.setRecordId(recordId);
                if (e.getJudgeResult() == null) {
                    state = "2";
                }
                int insert1 = recordDetailEntityMapper.insert(e);
                if (insert1 < 1) {
                    return false;
                }

            }
            ReportRecordEntity reportRecordEntity = new ReportRecordEntity(vo);
            reportRecordEntity.setState(state);
            reportRecordEntity.setReportCode("ZX-2021-SW-1471");

            reportRecordEntity.setId(recordId);
            int insert = recordEntityMapper.insert(reportRecordEntity);
            if (insert < 1) {
                return false;
            }
            return true;
        }
    }

    @Override
    public PageInfo sealList(String type, String search, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSealList(type, search);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Boolean seal(List<String> list, Long id) {
        //TODO 根据印章类型，请求契约所的印章

        //上传印章图片到文件服务器
        String img1 = "http://192.168.2.35:9000/seal-cns-cma/cns.jpg";
        String img2 = "http://192.168.2.35:9000/seal-cns-cma/cma.jpg";
        String url = img1 + "," + img2;
        //更新url数据到表test_report_record
        entityMapper.updateImgByid(id, url);
        return true;
    }

    @Override
    public XWPFDocument preview(List<ReportRecordDetailEntity> detailEntityList, EntrustAddVo detail, InputStream object, String[] sealUrls) {


        return null;
    }

    @Override
    public ReportRecordEntity getUrlByCode(String reportCode) {
        return entityMapper.getUrlByCode(reportCode);
    }

    @Override
    public Long getEntrustIdByCode(String reportCode) {
        return entityMapper.getEntrustIdByCode(reportCode);
    }

    @Override
    public List<ReportRecordDetailEntity> getReportDetailByCode(String reportCode) {
        return recordDetailEntityMapper.getReportDetailByCode(reportCode);
    }

    @Override
    public List<ReportTemplateEntity> getReportTemplateList(String productId) {
        return templateEntityMapper.getReportTemplateList(productId);
    }

    @Override
    public ReportRecordEntity selectByEntrustId(Long entrustId) {
        return recordEntityMapper.selectByEntrustId(entrustId);
    }

    @Override
    public List<ReportRecordDetailEntity> getCheckInfoByRecordId(Long recordId) {
        return recordDetailEntityMapper.getCheckInfoByRecordId(recordId);
    }
}
