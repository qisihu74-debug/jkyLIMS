package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.mapper.AduditBaseDataDao;
import com.lims.manage.erp.mapper.QsAuditDao;
import com.lims.manage.erp.service.QsAuditService;
import com.lims.manage.erp.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-05 11:26
 * @Copyright © 河南交科院
 */
@Service
public class QsAuditServiceImpl implements QsAuditService {
    @Autowired
    private QsAuditDao qsAuditDao;
    @Autowired
    private AduditBaseDataDao aduditBaseDataDao;

    @Override
    public PageInfo<InternalAuditorActive> internalAuditorActiveList(Integer pageNum, Integer pageSize, String name, Long userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<InternalAuditorActive> list = qsAuditDao.internalAuditorActiveList(name,userId);
        PageInfo<InternalAuditorActive> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo<AduditBaseData> getCheckBaseDataList(Integer pageNum, Integer pageSize, String type, String dir, String content, String method, String subject,Integer divideId) {
        PageInfo<AduditBaseData> pageInfo = null;
        if (StringUtils.isEmpty(subject)){
            LambdaQueryWrapper<AduditBaseData> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.orderByAsc(AduditBaseData::getSort);
            queryWrapper.eq(AduditBaseData::getType,type);
            queryWrapper.like(StringUtils.isNotEmpty(dir),AduditBaseData::getDirectory,dir);
            queryWrapper.like(StringUtils.isNotEmpty(content),AduditBaseData::getContent,content);
            queryWrapper.like(StringUtils.isNotEmpty(method),AduditBaseData::getMethod,method);
            PageHelper.startPage(pageNum,pageSize);
            List<AduditBaseData> list = aduditBaseDataDao.selectList(queryWrapper);
            pageInfo = new PageInfo<>(list);
        }else {
            PageHelper.startPage(pageNum,pageSize);
            List<AduditBaseData> list = aduditBaseDataDao.selectListBySub(type,dir,content,method,subject,divideId);
            pageInfo = new PageInfo<>(list);
        }
        return pageInfo;
    }

    @Override
    public String getStateByActiveId(int activeId) {
        return qsAuditDao.getStateByActiveId(activeId);
    }

    @Override
    public String getUserIdByActiveId(int activeId) {
        return qsAuditDao.getUserIdByActiveId(activeId);
    }

    @Override
    public PageInfo<InternalAuditorActive> deptLeaderActiveList(Integer pageNum, Integer pageSize, String name, Long userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<InternalAuditorActive> list = qsAuditDao.deptLeaderActiveList(name,userId);
        PageInfo<InternalAuditorActive> pageInfo = new PageInfo<>(list);
        for (InternalAuditorActive active : pageInfo.getList()) {
            active.setAuditTimeCycle(DateUtil.formatDate(active.getStartTime())+"~"+DateUtil.formatDate(active.getEndTime()));
        }
        return pageInfo;
    }
}
