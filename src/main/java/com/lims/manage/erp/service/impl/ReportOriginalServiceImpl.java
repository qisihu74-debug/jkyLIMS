package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ReportOriginalEntity;
import com.lims.manage.erp.mapper.ReportOriginalEntityMapper;
import com.lims.manage.erp.service.ReportOriginalService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

@Service
public class ReportOriginalServiceImpl implements ReportOriginalService {
    @Autowired
    private ReportOriginalEntityMapper reportOriginalEntityMapper;

    @Override
    public int addReportOriginal(ReportOriginalEntity entity, MultipartFile file) throws UnsupportedEncodingException {
        String filename = file.getOriginalFilename();
        String upload = MinIoUtil.upload(BucketsConst.report_original, file, filename);
        String url = upload.substring(0, upload.indexOf("?"));
        String decode = URLDecoder.decode(url, "UTF-8");
        long id = GenID.getID();
        entity.setId(id);
        entity.setPid(id);
        entity.setCreateDate(new Date());
        entity.setUrl(decode);
        return reportOriginalEntityMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeReportOriginal(ReportOriginalEntity entity, MultipartFile file) throws UnsupportedEncodingException {
        Long oldId = entity.getId();
        ReportOriginalEntity reportOriginalEntity = reportOriginalEntityMapper.getDetail(oldId);
        reportOriginalEntity.setStatus("作废");
        reportOriginalEntity.setExpirationDate(new Date());
        reportOriginalEntityMapper.insertRecord(reportOriginalEntity);
        reportOriginalEntityMapper.deleteByPrimaryKey(oldId);
        Long pid = entity.getPid();
        String filename = file.getOriginalFilename();
        String upload = MinIoUtil.upload(BucketsConst.report_original, file, filename);
        String url = upload.substring(0, upload.indexOf("?"));
        String decode = URLDecoder.decode(url, "UTF-8");
        long id = GenID.getID();
        entity.setId(id);
        entity.setPid(pid);
        entity.setCreateDate(new Date());
        entity.setUrl(decode);
        return reportOriginalEntityMapper.insert(entity);
    }

    @Override
    public PageInfo<ReportOriginalEntity> getReportList(ReportOriginalEntity param) {
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<ReportOriginalEntity> reportList = reportOriginalEntityMapper.getReportList(param);
        return new PageInfo<>(reportList);
    }

    @Override
    public int updateReportOriginal(ReportOriginalEntity entity, MultipartFile file) {
        if (file != null) {
            //删除之前文件
            String oldUrl = entity.getUrl();
            String encodeFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
            String decode = null;
            try {
                decode = URLDecoder.decode(encodeFileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            MinIoUtil.deleteFile(BucketsConst.report_original, decode);
            //上传新文件
            String filename = file.getOriginalFilename();
            String upload = MinIoUtil.upload(BucketsConst.report_original, file, filename);
            String url = upload.substring(0, upload.indexOf("?"));
            String urlDecode = null;
            try {
                urlDecode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            entity.setUrl(urlDecode);
        }
        entity.setUpdateDate(new Date());
        return reportOriginalEntityMapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReportTemplate(List<Long> idList) {
        for (int j = 0; j < idList.size(); j++) {
            ReportOriginalEntity reportOriginalEntity = reportOriginalEntityMapper.selectByPrimaryKey(idList.get(j));
            String oldUrl = reportOriginalEntity.getUrl();
            String encodeFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
            String decode = null;
            try {
                decode = URLDecoder.decode(encodeFileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            MinIoUtil.deleteFile(BucketsConst.report_original, decode);
        }
        int i = reportOriginalEntityMapper.deleteByIds(idList);
        return i > 0;
    }

    @Override
    public List<LabelValueVo> getReportSelectList(String param) {
        return reportOriginalEntityMapper.getReportSelectList(param);
    }

    @Override
    public PageInfo getReportRecordList(Long pid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<ReportOriginalEntity> reportRecordList = reportOriginalEntityMapper.getReportRecordList(pid);
        return new PageInfo<>(reportRecordList);
    }
}
