package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.TechnicistFiles;
import com.lims.manage.erp.mapper.TechnicistFilesDao;
import com.lims.manage.erp.service.TechnicistFilesService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2023-12-19 10:21
 * @Copyright © 河南交科院
 */
@Service
public class TechnicistFilesServiceImpl extends ServiceImpl<TechnicistFilesDao, TechnicistFiles> implements TechnicistFilesService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadResume(Integer technicistId, MultipartFile file) {
        //查询技术人员履历表
        LambdaQueryWrapper<TechnicistFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TechnicistFiles::getTechnicistId,technicistId);
        queryWrapper.eq(TechnicistFiles::getType,1);
        TechnicistFiles selectOne = this.baseMapper.selectOne(queryWrapper);
        if (selectOne == null){
            //插入
            String upload = MinIoUtil.upload(BucketsConst.technicist_files, file, file.getOriginalFilename());
            if (!StringUtils.isEmpty(upload)){
                String uploadUrl = upload.substring(0, upload.indexOf("?"));
                TechnicistFiles technicistFiles = new TechnicistFiles();
                technicistFiles.setContent(file.getOriginalFilename().split("\\.")[0]);
                technicistFiles.setOperator(ShiroUtils.getUserInfo().getName());
                technicistFiles.setOperateTime(new Date(System.currentTimeMillis()));
                technicistFiles.setUpdateTime(new Date(System.currentTimeMillis()));
                technicistFiles.setTechnicistId(technicistId);
                technicistFiles.setType(1);
                technicistFiles.setFileUrl(uploadUrl);
                this.baseMapper.insert(technicistFiles);
                return true;
            }else {
                return false;
            }
        }else {
            //更新
            String fileUrl = selectOne.getFileUrl();
            if (!StringUtils.isEmpty(fileUrl)){
                delFileByUrl(fileUrl);
            }
            String upload = MinIoUtil.upload(BucketsConst.technicist_files, file, file.getOriginalFilename());
            if (!StringUtils.isEmpty(upload)){
                String uploadUrl = upload.substring(0, upload.indexOf("?"));
                selectOne.setContent(file.getOriginalFilename().split("\\.")[0]);
                selectOne.setOperator(ShiroUtils.getUserInfo().getName());
                selectOne.setUpdateTime(new Date(System.currentTimeMillis()));
                selectOne.setFileUrl(uploadUrl);
                this.baseMapper.updateById(selectOne);
                return true;
            }else {
                return false;
            }
        }
    }

    @Override
    public void delFileByUrl(String fileUrl) {
        String[] strings = fileUrl.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        MinIoUtil.deleteFile(bluckName,fileName);
    }
}
