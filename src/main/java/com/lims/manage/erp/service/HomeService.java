package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.HomeAfficheEntity;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface HomeService {

    Map<String, Integer> taskStatistics();

    List<LabelValueVo> outputValueStatistics(Integer flag);

    /**
     * 发布公告
     *
     * @param homeAfficheEntity
     * @param file
     * @return
     */
    Boolean postAnnounce(HomeAfficheEntity homeAfficheEntity, MultipartFile[] file);

    /**
     * 所有人员——展示公告
     *
     * @return
     */
    List<HomeAfficheEntity> showAnnounce();

    /**
     * 任务看板
     * @param userId
     * @return
     */
    List<LabelValueVo> taskKanban(Long userId) throws ParseException;
}
