package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.HomeAfficheEntity;
import com.lims.manage.erp.mapper.HomeAfficheDao;
import com.lims.manage.erp.service.HomeService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private HomeAfficheDao homeAfficheDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postAnnounce(HomeAfficheEntity homeAfficheEntity, MultipartFile[] file) {
       HomeAfficheEntity data = homeAfficheEntity;
        if(file!=null){
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.buckets_affiche_template, multipartFile, GenID.getID()+"."+strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（委托文档资料.pdf,原始文档.docx）
                stringfileUrlStr.append(name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                data.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                data.setFileUrlName(substring);
            }
        }
        data.setCreateTime(new Date());
        data.setUpdateTime(new Date());
        data.setId(GenID.getID());
        homeAfficheDao.addHomeAffiche(data);
        return true;
    }

    @Override
    public List<HomeAfficheEntity> showAnnounce() {
        List<HomeAfficheEntity> data = homeAfficheDao.announceHistory();
        return data;
    }
}
