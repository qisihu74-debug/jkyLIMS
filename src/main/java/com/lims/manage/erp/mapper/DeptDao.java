package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.vo.DingDeptVo;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/19 15:19
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface DeptDao extends BaseMapper<DingDeptEntity> {
    /**
     * 查询组织架构信息--树状递归
     * @return
     */
    List<DingDeptVo> getAllDept();
}
