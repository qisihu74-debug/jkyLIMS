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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

@Service
public class ReportOriginalServiceImpl implements ReportOriginalService {
    @Autowired
    private ReportOriginalEntityMapper reportOriginalEntityMapper;

    @Override
    public int addReportOriginal(ReportOriginalEntity entity, MultipartFile file) {
        String filename = file.getOriginalFilename();
//        String suffix = filename.substring(filename.indexOf("."));
//        String fileName = entity.getName() + suffix;
        String upload = MinIoUtil.upload(BucketsConst.report_original, file, filename);
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

    @Override
    public int updateReportOriginal(ReportOriginalEntity entity, MultipartFile file) {
        if(file != null){
            //删除之前文件
            String oldUrl = entity.getUrl();
            String encodeFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
            String decode = null;
            try {
                decode = URLDecoder.decode(encodeFileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            MinIoUtil.deleteFile(BucketsConst.report_original,decode);
            //上传新文件
            String filename = file.getOriginalFilename();
            String upload = MinIoUtil.upload(BucketsConst.report_original, file, filename);
            String url = upload.substring(0, upload.indexOf("?"));
            entity.setUrl(url);
        }
        entity.setUpdateDate(new Date());
        return reportOriginalEntityMapper.updateByPrimaryKeySelective(entity);
    }
}
