package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.InternalAudit;
import com.lims.manage.erp.entity.InternalAuditInfo;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.InternalAuditDao;
import com.lims.manage.erp.mapper.InternalAuditInfoDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.service.InternalAuditService;
import com.lims.manage.erp.util.MinIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-01-02 14:51
 * @Copyright © 河南交科院
 */
@Service
@Slf4j
public class InternalAuditServiceImpl extends ServiceImpl<InternalAuditDao, InternalAudit> implements InternalAuditService {
    @Autowired
    private InternalAuditInfoDao auditInfoDao;
    @Autowired
    private InternalAuditDao auditDao;
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    public PageInfo<InternalAudit> planList(Integer pageSize, Integer pageNum, String search, SysUserEntity userEntity) {
        PageHelper.startPage(pageNum,pageSize);
        String byId = sysUserDao.checkTxRoleById(userEntity.getUserId());
        if (StringUtils.isNotEmpty(byId)){
            LambdaQueryWrapper<InternalAudit> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(StringUtils.isNotEmpty(search),InternalAudit::getOperateName,search);
            List<InternalAudit> auditList = this.baseMapper.selectList(queryWrapper);
            PageInfo<InternalAudit> pageInfo = new PageInfo<>(auditList);
            handerList(pageInfo);
            return pageInfo;
        }else {
            List<InternalAudit> list = auditDao.getListByNsRole(userEntity.getUserId());
            PageInfo<InternalAudit> pageInfo = new PageInfo<>(list);
            handerList(pageInfo);
            return pageInfo;
        }
    }

    @Override
    public void delFileByUrl(String fileUrl) {
        String[] strings = fileUrl.split("\\/");
        String bluckName = strings[3];
        String fileName = strings[4];
        MinIoUtil.deleteFile(bluckName,fileName);
    }

    /**
     * 处理列表查询数据
     * @param pageInfo
     */
    public void handerList(PageInfo<InternalAudit> pageInfo){
        List<Integer> ids = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(pageInfo.getList())){
            for (InternalAudit internalAudit :pageInfo.getList()){
                ids.add(internalAudit.getId());
            }
        }
        LambdaQueryWrapper<InternalAuditInfo> queryWrapper1 = new LambdaQueryWrapper();
        queryWrapper1.in(CollectionUtils.isNotEmpty(ids), InternalAuditInfo::getAuditId, ids);
        List<InternalAuditInfo> internalAuditInfos = auditInfoDao.selectList(queryWrapper1);
        for (InternalAudit internalAudit :pageInfo.getList()){
            List<InternalAuditInfo> auditInfos = Lists.newArrayList();
            for (InternalAuditInfo auditInfo :internalAuditInfos){
                if (internalAudit.getId().equals(auditInfo.getAuditId())){
                    auditInfos.add(auditInfo);
                }
            }
            internalAudit.setList(auditInfos);
        }
    }
}
