package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 系统用户业务实现
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    DeptDao deptDao;

    /**
     * 根据用户名查询实体
     * @Author gjl
     * @CreateTime 2021/11/09 16:30
     * @Param  username 用户名
     * @Return SysUserEntity 用户实体
     */
    @Override
    public SysUserEntity selectUserByName(String username) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysUserEntity::getUsername,username);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean updateUserState(SysUserEntity entity) {
        return sysUserDao.updateUserState(entity);
    }

    @Override
    public Boolean resetPassword(SysUserEntity entity) {
        return sysUserDao.resetPassword(entity);
    }

    @Override
    public List<SysUserTreeEntity> selectUserList(String deptId) {
        List<SysUserTreeEntity> returnList = new ArrayList<>();
        // 依据部门id
        if (deptId != null && deptId!="") {
            List<SysUserTreeEntity> datalist = sysUserDao.selectUserinfoList(null);
            DingDeptEntity dingDeptEntity  = deptDao.selectById(deptId);
            for (SysUserTreeEntity data : datalist) {
                data.setUserDept(dingDeptEntity.getName());
                StringBuffer stringBuffer = new StringBuffer("");
                for (int i = 0; i < data.getDepartment().length(); i++) {
                    if (data.getDepartment().charAt(i) != '[' && data.getDepartment().charAt(i) != ']') {
                        stringBuffer.append(data.getDepartment().charAt(i));
                    }
                }
                String str1 = stringBuffer.toString();
                String[] arr = str1.split(","); // 用,分割
                for (int j = 0; j < arr.length; j++) {
                    if (deptId.equals(arr[j])) {
                        returnList.add(data);
                    }
                }
            }
            return returnList;
        }
        return null;
    }

    @Override
    public List<SysUserTreeEntity> selectUserLikeList(SysUserTreeEntity sysUserTreeEntity) {
        if(sysUserTreeEntity.getState()!=null){
            if(!sysUserTreeEntity.getState().equals("NORMAL")&&!sysUserTreeEntity.getState().equals("PROHIBIT")) {
                sysUserTreeEntity.setState(null);
            }
        }
        List<SysUserTreeEntity> dataList = sysUserDao.selectUserinfoList(sysUserTreeEntity);
        return dataList;
    }

    @Override
    public List<SysUserTreeEntity> selectUserAllList() {
        List<SysUserTreeEntity> dataList = sysUserDao.selectUserinfoList(null);
        return dataList;
    }
}
