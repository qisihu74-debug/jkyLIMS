package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.lims.manage.erp.entity.ReportRecordDetailEntity;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.ReportTemplateEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.mapper.ReportRecordEntityMapper;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.vo.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportServiceImpl implements ReportService {
    Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
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
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;

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
        for (ReportSampleDetailVo reportSampleDetailVo1 : list) {
            if (reportSampleDetailVo1.getSampleCode() != null && !reportSampleDetailVo1.getSampleCode().equals("")) {
                setSampleCode.add(reportSampleDetailVo1.getSampleCode());
            }
            if (reportSampleDetailVo1.getOutward() != null && !reportSampleDetailVo1.getOutward().equals("")) {
                setOutwarde.add(reportSampleDetailVo1.getOutward());
            }
            if (reportSampleDetailVo1.getSpecs() != null && !reportSampleDetailVo1.getSpecs().equals("")) {
                setSpecs.add(reportSampleDetailVo1.getSpecs());
            }
            if (reportSampleDetailVo1.getStandard() != null && !reportSampleDetailVo1.getStandard().equals("")) {
                setStandard.add(reportSampleDetailVo1.getStandard());
            }
            reportSampleDetailVo.setSampleName(reportSampleDetailVo1.getSampleName());
        }
        for (String str1 : setSampleCode) {
            if (reportSampleDetailVo.getSampleCode() == null) {
                reportSampleDetailVo.setSampleCode(str1 + "、");
            } else {
                reportSampleDetailVo.setSampleCode(reportSampleDetailVo.getSampleCode() + str1 + "、");
            }
        }
        for (String str2 : setOutwarde) {
            if (reportSampleDetailVo.getOutward() == null) {
                reportSampleDetailVo.setOutward(str2 + "、");
            } else {
                reportSampleDetailVo.setOutward(reportSampleDetailVo.getOutward() + str2 + "、");
            }

        }
        for (String str3 : setSpecs) {
            if (reportSampleDetailVo.getSpecs() == null) {
                reportSampleDetailVo.setSpecs(str3 + "、");
            } else {
                reportSampleDetailVo.setSpecs(reportSampleDetailVo.getSpecs() + str3 + "、");
            }
        }
        for (String str4 : setStandard) {
            if (reportSampleDetailVo.getStandard() == null) {
                reportSampleDetailVo.setStandard(str4 + "、");
            } else {
                reportSampleDetailVo.setStandard(reportSampleDetailVo.getStandard() + str4 + "、");
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

    @Override
    public ReportRecordEntity getDetail(Long id) {
        // 查询报告单信息
        ReportRecordEntity reportDetail = recordEntityMapper.getDetail(id);
        EntrustAddVo entrustData = new EntrustAddVo();
        if (reportDetail.getReportType() == null) {
            // 根据委托单id 获取 委托单信息下 报告方式、领报告人、收件电话、邮寄地址、邮箱
            entrustData = entrustEntityMapper.selectByKeyId(reportDetail.getEntrustmentId());
            // 报告发出方式
            if (entrustData.getReportType() != null && !entrustData.getReportType().equals("")) {
                reportDetail.setReportType(entrustData.getReportType());
            }
            // 领报告人
            if (entrustData.getAddressee() != null && !entrustData.getAddressee().equals("")) {
                reportDetail.setAddressee(entrustData.getAddressee());
            }
            // 收件电话
            if (entrustData.getMobile() != null && !entrustData.getMobile().equals("")) {
                reportDetail.setReportPhone(entrustData.getMobile());
            }
            // 邮寄地址
            if (entrustData.getAddress() != null && !entrustData.getAddress().equals("")) {
                reportDetail.setReportMailingAddress(entrustData.getAddress());
            }
            // 邮箱
            if (entrustData.getMailbox() != null && !entrustData.getMailbox().equals("")) {
                reportDetail.setEmail(entrustData.getMailbox());
            }
        }
        return reportDetail;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveMessage(ReportRecordEntity reportRecordEntity) {
        Integer status = entityMapper.updateByPrimaryKeySelective(reportRecordEntity);
        if(status==1){
            return true;
        }
        return false;
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
            if ("1".equals(state)) {
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
            if ("1".equals(state)) {
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
    public XWPFDocument preview(String reportCode,List<ReportRecordDetailEntity> detailEntityList, EntrustAddVo detail, InputStream object, String[] sealUrls) {
        XWPFDocument doc = null;
        Map<String,Object> map = new HashMap();
        try {
            doc = new XWPFDocument(object);
            //检测项所属table定义集合
            Map<Integer,List<ReportRecordDetailEntity>> listMap = new HashMap();
            for (ReportRecordDetailEntity entity:detailEntityList) {
                String[] split = entity.getCoordinate().split(",");
                Integer key = Integer.valueOf(split[0]);
                if (listMap.get(key) == null){
                    List<ReportRecordDetailEntity> list = Lists.newArrayList();
                    list.add(entity);
                    listMap.put(key,list);
                }else {
                    List<ReportRecordDetailEntity> list = listMap.get(key);
                    list.add(entity);
                    listMap.put(key,list);
                }
                map.put(split[0],"index");//map作为下面的循环table的索引
            }
            int size = map.keySet().size();
            List<XWPFTable> tables = doc.getTables();
            List<XWPFTableRow> rows;
            for (int i=0;i<size;i++) {
                XWPFTable table = tables.get(i);
                //第一个table包含表头、后面的table只有检测项数据
                //获取表格对应的行
                rows = table.getRows();
                List<ReportRecordDetailEntity> list = listMap.get(i + 1);//检测项
                if (i==0){
                    //设置模板数据
                    rows.get(3).getTableCells().get(1).setText("河南省公路工程实验检测中心有限公司");//检测单位
                    rows.get(3).getTableCells().get(2).setText(reportCode);//报告编号
                    rows.get(4).getTableCells().get(1).setText(detail.getEntrustCompany());//委托单位
                    rows.get(4).getTableCells().get(2).setText(detail.getProjectName());//工程名称
                    rows.get(5).getTableCells().get(1).setText(detail.getProjectPart());//工程部位
                    //设置样品信息
                    List<SampleEntity> samples = detail.getSamples();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (SampleEntity sampleEntity :samples) {
                        stringBuilder.append("样品名称：");
                        stringBuilder.append(sampleEntity.getSampleName());
                        stringBuilder.append(";");
                        stringBuilder.append("样品编号：");
                        stringBuilder.append(sampleEntity.getSampleCode());
                        stringBuilder.append(";");
                        stringBuilder.append("样品数量:");
                        stringBuilder.append(sampleEntity.getSampleGroups());
                        stringBuilder.append("片;");
                        stringBuilder.append("样品状态:");
                        stringBuilder.append(sampleEntity.getState());
                        stringBuilder.append(";");
                        stringBuilder.append("收样时间:");
                        stringBuilder.append(sampleEntity.getReceivedDate());
                        stringBuilder.append(";");
                    }
                    String substring = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                    rows.get(6).getTableCells().get(1).setText(substring);
                    //检测依据

                    //判定依据

                    //检测日期

                    //主要仪器设备名称及编号

                    //委托编号

                    //检测类别

                    //批号

                    //生成厂家

                    //规格等级

                    //代表数量


                    //根据坐标设置检测项
                    for (ReportRecordDetailEntity entity :list) {
                        //获取坐标
                        String[] split = entity.getCoordinate().split(",");
                        Integer x = Integer.valueOf(split[1]);
                        Integer y = Integer.valueOf(split[2]);
                        //设置技术指标
                        rows.get(x).getTableCells().get(y+1).setText(entity.getSpecsContent());
                        //设置检测结果
                        rows.get(x).getTableCells().get(y+2).setText(entity.getCheckResult());
                        //设置判定结果
                        rows.get(x).getTableCells().get(y+3).setText(entity.getJudgeResult());
                    }
                }else {
                    //根据坐标设置检测项
                    for (ReportRecordDetailEntity entity :list) {
                        //获取坐标
                        String[] split = entity.getCoordinate().split(",");
                        Integer x = Integer.valueOf(split[1]);
                        Integer y = Integer.valueOf(split[2]);
                        //设置技术指标
                        rows.get(x).getTableCells().get(y+1).setText(entity.getSpecsContent());
                        //设置检测结果
                        rows.get(x).getTableCells().get(y+2).setText(entity.getCheckResult());
                        //设置判定结果
                        rows.get(x).getTableCells().get(y+3).setText(entity.getJudgeResult());
                    }
                }
            }
        }catch (Exception e){
            logger.error("报告查看异常:{}",e);
        }
        return doc;
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
    public PageInfo getSendList(String search, String reportType, Integer pageNum, Integer pageSize, String type) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReportRecordEntity> list = entityMapper.getSendList(search, reportType, type);
        PageInfo<ReportRecordEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }
}
