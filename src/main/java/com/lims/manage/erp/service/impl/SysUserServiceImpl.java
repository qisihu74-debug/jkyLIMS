package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.SysUserRoleEntity;
import com.lims.manage.erp.entity.SysUserTreeEntity;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.SysUserRoleDao;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private DeptDao deptDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Autowired
    private DingUsertDao dingUsertDao;

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
        List<SysUserTreeEntity> returnDataList = new ArrayList<>();
        List<DingDeptEntity> deptList = deptDao.selectList(null);
        return dataList;
    }

    @Override
    public List<SysUserTreeEntity> selectUserAllList() {
        List<SysUserTreeEntity> dataList = sysUserDao.selectUserinfoList(null);
        return dataList;
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
    public List<UserInfoVo> getUserInfos(UserInfoParamVo vo) {
        List<UserInfoVo> userInfos = sysUserDao.getUserInfos(vo);
        if(!userInfos.isEmpty()){
            for (UserInfoVo userInfoVo: userInfos) {
                List<LabelValueVo> department = Lists.newArrayList();
                String departmentId = userInfoVo.getDepartmentId();
                String replace;
                if(departmentId != null && departmentId.contains("[")){
                    replace = departmentId.replace("[", "").replace("]", "");
                }else{
                    replace = departmentId;
                }
                if(replace != null && replace.contains(",")){
                    String[] split = replace.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String deptId = split[i].trim();
                        LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(deptId));
                        department.add(departmentInfo);
                    }
                }else if(replace != null && !replace.contains(",")){
                    LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(replace));
                    department.add(departmentInfo);
                }
                userInfoVo.setDepartment(department);
            }
        }
        return userInfos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserInfo(UserInfoVo vo) {
        Boolean flag = false;
        System.out.println("用户信息："+vo.toString());
        //更新sys_user
        sysUserDao.updateUserInfo(vo);
//        //更新sys_ding_user
//        dingUsertDao.updateDingUserInfo(vo);
        //删除旧权限
        sysUserRoleDao.removeOldRole(vo.getUserId());
        //增加新权限
        List<SysUserRoleEntity> newRoles = Lists.newArrayList();
        if(vo.getRoleIds().contains(",")){
            String[] split = vo.getRoleIds().split(",");
            for (int i = 0; i < split.length; i++) {
                SysUserRoleEntity entity = new SysUserRoleEntity();
                entity.setUserId(Long.parseLong(vo.getUserId()));
                entity.setRoleId(Long.parseLong(split[i].trim()));
                newRoles.add(entity);
            }
        }else{
            SysUserRoleEntity entity = new SysUserRoleEntity();
            entity.setUserId(Long.parseLong(vo.getUserId()));
            entity.setRoleId(Long.parseLong(vo.getRoleIds().trim()));
            newRoles.add(entity);
        }
        for (SysUserRoleEntity entity: newRoles) {
            sysUserRoleDao.insertNewRole(entity);
        }
        flag = true;
        return flag;
    }

}
