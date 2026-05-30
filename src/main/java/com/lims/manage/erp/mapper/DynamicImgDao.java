package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DynamicImg;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2022-08-16 10:30
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface DynamicImgDao extends BaseMapper<DynamicImg> {

    @Delete("delete from sys_csos_img")
    void deleteImg();
}
