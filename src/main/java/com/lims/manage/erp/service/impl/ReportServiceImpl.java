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
import com.lims.manage.erp.vo.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public ReportSampleDetailVo getReportList_history_details(Long id) {
        ReportSampleDetailVo reportSampleDetailVo = new ReportSampleDetailVo();
        // 获取报告头部信息
        List<ReportSampleDetailVo> list = reportMapper.getReportHeadDetails(id);
        // 获取样品编号
        Set<String> setSampleCode = new HashSet<>();
        // 获取外观
        Set<String> setOutwarde = new HashSet<>();
        // 规格等级
        Set<String> setSpecs = new HashSet<>();
        // 判定依据
        Set<String> setStandard = new HashSet<>();
        // 处理信息
        for(ReportSampleDetailVo reportSampleDetailVo1:list){
            if(reportSampleDetailVo1.getSampleCode()!=null&&!reportSampleDetailVo1.getSampleCode().equals("")){
                setSampleCode.add(reportSampleDetailVo1.getSampleCode());
            }
           if(reportSampleDetailVo1.getOutward()!=null&&!reportSampleDetailVo1.getOutward().equals("")){
               setOutwarde.add(reportSampleDetailVo1.getOutward());
           }
           if(reportSampleDetailVo1.getSpecs()!=null&&!reportSampleDetailVo1.getSpecs().equals("")){
               setSpecs.add(reportSampleDetailVo1.getSpecs());
           }
           if(reportSampleDetailVo1.getStandard()!=null&&!reportSampleDetailVo1.getStandard().equals("")){
               setStandard.add(reportSampleDetailVo1.getStandard());
           }
            reportSampleDetailVo.setSampleName(reportSampleDetailVo1.getSampleName());
        }
        for(String str1:setSampleCode){
            if(reportSampleDetailVo.getSampleCode()==null){
                reportSampleDetailVo.setSampleCode(str1+"、");
            }
            else {
                reportSampleDetailVo.setSampleCode(reportSampleDetailVo.getSampleCode()+str1+"、");
            }
        }
        for(String str2:setOutwarde){
            if(reportSampleDetailVo.getOutward()==null){
                reportSampleDetailVo.setOutward(str2+"、");
            }
            else{
                reportSampleDetailVo.setOutward(reportSampleDetailVo.getOutward()+str2+"、");
            }

        }
        for (String str3:setSpecs){
            if(reportSampleDetailVo.getSpecs()==null){
                reportSampleDetailVo.setSpecs(str3+"、");
            }
            else {
                reportSampleDetailVo.setSpecs(reportSampleDetailVo.getSpecs()+str3+"、");
            }
        }
        for(String str4:setStandard){
            if(reportSampleDetailVo.getStandard()==null){
                reportSampleDetailVo.setStandard(str4+"、");
            }
            else {
                reportSampleDetailVo.setStandard(reportSampleDetailVo.getStandard()+str4+"、");
            }

        }
        // 获取检测项
        List<ReportCheckItemDetailVo> checkItemList = reportMapper.getReportCheckItemList(id);
        reportSampleDetailVo.setCheckItems(checkItemList);
        return reportSampleDetailVo;
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
            if("1".equals(state)){
                reportRecordEntity1.setReportCompleteTime(new Timestamp(System.currentTimeMillis()));
            }
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
            if("1".equals(state)){
                reportRecordEntity1.setReportCompleteTime(new Timestamp(System.currentTimeMillis()));
            }
            //生成报告编号
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

    @Override
    public PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize,String type) {
        PageHelper.startPage(pageNum,pageSize);
        List<ReportRecordEntity> list = entityMapper.getSendList(search,reportType,type);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }
}
