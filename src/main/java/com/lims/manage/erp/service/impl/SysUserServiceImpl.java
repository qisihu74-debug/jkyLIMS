package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.mapper.DingUsertDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.SysUserRoleDao;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.UserInfoParamVo;
import com.lims.manage.erp.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.wml.U;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description 系统用户业务实现
 * @Author gjl
 * @CreateTime 2021/11/09 15:57
 */
@Service("sysUserService")
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {

    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Autowired
    private DingUsertDao dingUsertDao;

    @Override
    public List<SysUserEntity> getUserNameList() {
        return this.list(Wrappers.<SysUserEntity>lambdaQuery().select(SysUserEntity::getUserId, SysUserEntity::getName));
    }

    @Override
    public List<SysUserEntity> getExceptUserNameList() {
        return this.list(Wrappers.<SysUserEntity>lambdaQuery().ne(SysUserEntity::getUserId,ShiroUtils.getUserInfo().getUserId()).select(SysUserEntity::getUserId, SysUserEntity::getName));
    }

    /**
     * 根据用户名查询实体
     *
     * @Author gjl
     * @CreateTime 2021/11/09 16:30
     * @Param username 用户名
     * @Return SysUserEntity 用户实体
     */
    @Override
    public SysUserEntity selectUserByName(String username) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysUserEntity::getUsername, username);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<SysUserTreeEntity> selectUserList(String deptId) {
        List<SysUserTreeEntity> returnList = new ArrayList<>();
        // 依据部门id
        if (deptId != null && deptId != "") {
            List<SysUserTreeEntity> datalist = sysUserDao.selectUserinfoList(null);
            DingDeptEntity dingDeptEntity = deptDao.selectById(deptId);
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
        if (sysUserTreeEntity.getState() != null) {
            if (!sysUserTreeEntity.getState().equals("NORMAL") && !sysUserTreeEntity.getState().equals("PROHIBIT")) {
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
        if (!userInfos.isEmpty()) {
            for (UserInfoVo userInfoVo : userInfos) {
                List<LabelValueVo> department = Lists.newArrayList();
                String departmentId = userInfoVo.getDepartmentId();
                String replace;
                if (departmentId != null && departmentId.contains("[")) {
                    replace = departmentId.replace("[", "").replace("]", "");
                } else {
                    replace = departmentId;
                }
                if (replace != null && replace.contains(",")) {
                    String[] split = replace.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String deptId = split[i].trim();
                        LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(deptId));
                        if(departmentInfo!=null){
                            department.add(departmentInfo);
                        }
                    }
                } else if (replace != null && !replace.contains(",")&&!"".equals(replace)) {
                    LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(replace));
                    if(departmentInfo!=null){
                        department.add(departmentInfo);
                    }
                }
                userInfoVo.setDepartment(department);
            }
        }
        return userInfos;
    }

    /**
     * 获取用户信息列表——二次开发
     * @param vo
     * @return
     */
    @Override
    public List<UserInfoVo> getUserInfos_two(UserInfoParamVo vo) {
        List<UserInfoVo> userInfos = sysUserDao.getUserInfos(vo);
        if (!userInfos.isEmpty()) {
            for (UserInfoVo userInfoVo : userInfos) {
                List<LabelValueVo> department = Lists.newArrayList();
                String replace = userInfoVo.getDepartmentId();
                if (replace != null && replace.contains(",")) {
                    String[] split = replace.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String deptId = split[i].trim();
                        LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(deptId));
                        if(departmentInfo!=null){
                            department.add(departmentInfo);
                        }
                    }
                } else if (replace != null && !replace.contains(",")&&!"".equals(replace)) {
                    LabelValueVo departmentInfo = deptDao.getRoleInfoById(Long.parseLong(replace));
                    if(departmentInfo!=null){
                        department.add(departmentInfo);
                    }
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
        // 处理部门信息
        if(vo.getDepartmentIdLong()!=null&&vo.getDepartmentIdLong().size()>0){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for(Long deptId:vo.getDepartmentIdLong())
            {
                stringBuilder.append(""+deptId+",");
            }
            stringBuilder.append("]");
            vo.setDepartmentId(stringBuilder.toString());
        }
        //更新sys_user
        sysUserDao.updateUserInfo(vo);
//        //更新sys_ding_user
//        dingUsertDao.updateDingUserInfo(vo);
        //删除旧权限
        sysUserRoleDao.removeOldRole(vo.getUserId());
        if (vo.getRoleIdsLong() != null && vo.getRoleIdsLong().size()>0) {
            List<SysUserRoleEntity> newRoles = Lists.newArrayList();
            //增加新权限
            for (Long roleId:vo.getRoleIdsLong()){
                SysUserRoleEntity entity = new SysUserRoleEntity();
                entity.setUserId(vo.getUserId());
                entity.setRoleId(roleId);
                newRoles.add(entity);
            }
            for (SysUserRoleEntity entity : newRoles) {
                sysUserRoleDao.insertNewRole(entity);
            }
        }
        flag = true;
        return flag;
    }

    @Override
    public Boolean getTheUser(Long userId,String dingUserId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysUserEntity::getDingUserId, dingUserId).eq(SysUserEntity::getUserId, userId);
        SysUserEntity data = this.baseMapper.selectOne(queryWrapper);
        if(data==null){
            return true;
        }
        return false;
    }

    @Override
    public Boolean getTheUserList(String dingUserId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysUserEntity::getDingUserId, dingUserId);
        List<SysUserEntity> data = this.baseMapper.selectList(queryWrapper);
        if(data!=null&&data.size()>0){
            return false;
        }
        return true;
    }

    @Override
    public  List<DingUserEntity> personList(String search) {
        // 获取全部部门信息 主键
        List<Long> depts = deptDao.getDeptLong();
// 1、依据部门id 下 获取人员信息
        List<DingUserEntity> personList = dingUsertDao.getAllUserTerm(search);
        Iterator<DingUserEntity> it = personList.iterator();
        while (it.hasNext()) {
            DingUserEntity dingUserEntity = it.next();
            int flagbit = 0;
            StringBuilder stringBuilder = new StringBuilder();
            if (dingUserEntity.getDepartment() != null&&!dingUserEntity.getDepartment().equals("")) {
                String[] strings = dingUserEntity.getDepartment().split(",");
                for (int i = 0; i < strings.length; i++) {
                    for (Long deptId : depts) {
                        if (deptId.equals((Long.parseLong(strings[i].trim())))) {
                            flagbit++;
                            stringBuilder.append((Long.parseLong(strings[i].trim())) + ",");
                        }
                    }
                }
                if (flagbit == 0) {
                    it.remove();
                }
                if(stringBuilder.length()>0){
                    dingUserEntity.setDepartment(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                }
                else {
                    dingUserEntity.setDepartment(null);
                }
            }
        }
        List<DingDeptEntity> deptAllList = deptDao.getAllList(null);
        // 2、并对人员所属部门 拼接部门信息。
        for (DingUserEntity dingUserEntity : personList) {
            StringBuilder stringBuilder = new StringBuilder();
            if (dingUserEntity.getDepartment() != null&&!dingUserEntity.getDepartment().equals("")) {
                String[] strings = dingUserEntity.getDepartment().split(",");
                for (int i = 0; i < strings.length; i++) {
                    for (DingDeptEntity deptEntity : deptAllList) {
                        if (deptEntity.getId().equals((Long.parseLong(strings[i].trim())))) {
                            stringBuilder.append(deptEntity.getName() + "、");
                        }
                    }
                }
                if(stringBuilder.length()>0){
                    dingUserEntity.setWorkplace(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
                }
                else {
                    dingUserEntity.setWorkplace(null);
                }
            }
        }
        return personList;
    }

    @Override
    public boolean uploadSignature(MultipartFile file) {
        String url = "";
        String originalFilename = file.getOriginalFilename();
        String[] strings = originalFilename.split("\\.");
        Long userId = ShiroUtils.getUserInfo().getUserId();
        try {
            url = MinIoUtil.upload("personal-signature",file,userId+"."+strings[1]);
        }catch (Exception e){
            log.error("上传个人签名失败:{}",e);
            return false;
        }
        if (StringUtils.isNotEmpty(url)){
            try {
                url = url.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                String decode = URLDecoder.decode(url, "utf-8");
                decode = decode.substring(0,decode.indexOf("?"));
                LambdaUpdateWrapper<SysUserEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(true,SysUserEntity::getUserId,userId);
                updateWrapper.set(true,SysUserEntity::getSignatureUrl,decode);
                this.baseMapper.update(null,updateWrapper);
                return true;
            }catch (Exception e){
                log.error("上传个人签名信息失败:{}",e);
                //删除文件
                MinIoUtil.deleteFile("personal-signature",userId+"."+strings[1]);
                return false;
            }
        }
        return true;
    }

    @Override
    public Boolean checkSysAndAdmRole(Long userId) {
        String s = sysUserDao.checkSysAndAdmRole(userId);
        if (StringUtils.isEmpty(s)){
            return false;
        }else {
            return true;
        }
    }
}
