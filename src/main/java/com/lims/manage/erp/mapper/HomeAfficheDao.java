package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.HomeAfficheEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/2/22 10:04
 */
@Component
@Mapper
public interface HomeAfficheDao {

    /**
     * 公告增加
     * @param homeAfficheEntity
     * @return
     */
    int addHomeAffiche(HomeAfficheEntity homeAfficheEntity);
    /**
     * 查询发布公告
     */
    List<HomeAfficheEntity> announceHistory();
}
