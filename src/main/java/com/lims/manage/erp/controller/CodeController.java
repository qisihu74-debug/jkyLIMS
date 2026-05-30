package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.CodeEntity;
import com.lims.manage.erp.entity.CodeUser;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.CodeService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2022-08-18 14:53
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("/code/")
public class CodeController {
    @Autowired
    private CodeService codeService;
    @Autowired
    private SysUserDao userDao;


    /**
     * 获取验证码下拉选择的人员列表
     * @return
     */
    @GetMapping("userList")
    public Result userList(){
        List<SysUserEntity> list = userDao.GetUserList();
        List<CodeUser> userList = Lists.newArrayList();
        for (SysUserEntity entity:list) {
            CodeUser user = new CodeUser();
            user.setUserId(entity.getUserId()+"");
            user.setName(entity.getName());
            user.setMobile(entity.getMobile());
            userList.add(user);
        }
        return ResultUtil.success(userList);
    }

    /**
     * 生成企业邀请码
     * @param entity
     * @return
     */
    @PostMapping("add")
    public Result add(@RequestBody CodeEntity entity){
        if (StringUtils.isEmpty(entity.getUserId()) || StringUtils.isEmpty(entity.getName()) || entity.getNumber() <=0){
            return ResultUtil.error("缺少参数");
        }
        List<CodeEntity> list = Lists.newArrayList();
        for (int i=0;i<entity.getNumber();i++){
            CodeEntity codeEntity = new CodeEntity();
            codeEntity.setUserId(entity.getUserId());
            codeEntity.setName(entity.getName());
            codeEntity.setMobile(entity.getMobile());
            codeEntity.setNumber(entity.getNumber());
            String randomNum = this.genRandomNum();
            codeEntity.setCode(randomNum);
            codeEntity.setCreateTime(System.currentTimeMillis()+"");
            codeEntity.setId(GenID.getUUID());
            codeEntity.setState("0");
            list.add(codeEntity);
        }
        boolean batch = codeService.saveBatch(list);
        if (batch){
            return ResultUtil.success("企业邀请码生成成功");
        }else {
            return ResultUtil.error("操作失败");
        }
    }

    /**
     * 获取验证码列表
     * @param name
     * @param state
     * @param usedCompany
     * @return
     */
    @GetMapping("list")
    public Result list(String name, String state, String usedCompany,Integer pageNum,Integer pageSize){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        LambdaQueryWrapper<CodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(org.apache.commons.lang3.StringUtils.isNotEmpty(state),CodeEntity::getState,state);
        queryWrapper.like(org.apache.commons.lang3.StringUtils.isNotEmpty(name),CodeEntity::getName,name);
        queryWrapper.like(org.apache.commons.lang3.StringUtils.isNotEmpty(usedCompany),CodeEntity::getUsedCompany,usedCompany);
        queryWrapper.orderByDesc(CodeEntity::getCreateTime);
        PageHelper.startPage(pageNum,pageSize);
        List<CodeEntity> list = codeService.list(queryWrapper);
        PageInfo<CodeEntity> pageInfo = new PageInfo<>(list);
        for (CodeEntity codeEntity:pageInfo.getList()) {
            codeEntity.setCreateTime(DateUtil.conversionTime(Long.parseLong(codeEntity.getCreateTime())));
            if (codeEntity.getUseTime() != null){
                codeEntity.setUseTime(DateUtil.conversionTime(Long.parseLong(codeEntity.getUseTime())));
            }
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 删除企业邀请码
     * @param id
     * @return
     */
    @GetMapping("delete")
    public Result delete(String id){
        if (org.apache.commons.lang.StringUtils.isEmpty(id)){
            return ResultUtil.error("缺少必要参数");
        }
        boolean b = codeService.removeById(id);
        if (b){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }

    public String genRandomNum() {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < 8) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }
}
