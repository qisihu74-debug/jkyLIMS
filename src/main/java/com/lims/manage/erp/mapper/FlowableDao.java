package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/19 16:52
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface FlowableDao extends BaseMapper {

    /**
     * 获取已部署的bpmn20.xml
     * @return
     */
    List<String> getDeployed();
}
