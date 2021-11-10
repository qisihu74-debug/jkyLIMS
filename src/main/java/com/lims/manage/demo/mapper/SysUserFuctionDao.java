package com.lims.manage.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.demo.entity.SysFunction;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.mapper
 * @desc
 * @date 2021/11/10 14:12
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface SysUserFuctionDao extends BaseMapper {

    /**
     * 根据用户id获取当前管理员菜单
     * @param userId
     * @return
     */
    List<SysFunction> getFunctionByuserId(Long userId);
}
