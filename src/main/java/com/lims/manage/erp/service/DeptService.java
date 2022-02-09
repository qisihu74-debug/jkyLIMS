package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DingUserEntity;
import com.lims.manage.erp.vo.DingDeptVo;
import com.lims.manage.erp.vo.PagingToolVo;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/19 15:16
 * @Copyright © 河南交科院
 */
public interface DeptService extends IService<DingDeptEntity> {
    /**
     * 查询组织架构信息--树状
     * @return
     */
    List<DingDeptVo> getAllDept();

    /**
     * 查询部门列表
     * @return
     */
    PageInfo findList(Integer pageNum, Integer pageSize, String search);

    /**
     * 分页查询部门下人员
     * @return
     * @param id
     * @param isInclude
     * @param pageNum
     * @param pageSize
     */
    PagingToolVo personList(Long id, String isInclude, Integer pageNum, Integer pageSize, String search);

    /**
     * 更新人员
     * @param personEntity
     * @return
     */
    Boolean updatePersonDetails(DingUserEntity personEntity);
}
