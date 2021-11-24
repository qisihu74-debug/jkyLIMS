package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysRoleEntity;
import com.lims.manage.erp.mapper.SysRoleDao;
import com.lims.manage.erp.service.SysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 角色业务实现
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleDao, SysRoleEntity> implements SysRoleService {

    /**
     * 通过用户ID查询角色集合
     * @Author gjl
     * @CreateTime 2021/11/09 18:01
     * @Param  userId 用户ID
     * @Return List<SysRoleEntity> 角色名集合
     */
    @Override
    public List<SysRoleEntity> selectSysRoleByUserId(Long userId) {
        return this.baseMapper.selectSysRoleByUserId(userId);
    }

    @Override
    public List<SysRoleEntity> selectSysRoleList(SysRoleEntity sysRoleEntity) {
        return this.baseMapper.selectSysRoleList(sysRoleEntity);
    }

    @Override
    public int updateSysRoleByUserId(SysRoleEntity sysRoleEntity) {

        return baseMapper.updateById(sysRoleEntity);
    }

    @Override
    public SysRoleEntity addSysRoleByUserId(SysRoleEntity sysRoleEntity) {

        int statusNumber = baseMapper.insert(sysRoleEntity);
        if(statusNumber>=1){
            return  sysRoleEntity;
        }
        return null;
    }

    @Override
    public int deleteSysRoleByUserId(Long roleId) {
        return baseMapper.deleteById(roleId);
    }
}