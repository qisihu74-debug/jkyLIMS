package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.HomeAfficheEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HomeService {

    /**
     * 发布公告
     * @param homeAfficheEntity
     * @param file
     * @return
     */
    Boolean postAnnounce(HomeAfficheEntity homeAfficheEntity, MultipartFile[] file);

    /**
     * 所有人员——展示公告
     * @return
     */
    List<HomeAfficheEntity> showAnnounce();
}
