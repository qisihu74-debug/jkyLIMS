package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.HKPersonDoorProvisionalAuthorityRelEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.mapper.HKPersonDoorProvisionalAuthorityRelEntityMapper;
import com.lims.manage.erp.mapper.TestInstrumentDao;
import com.lims.manage.erp.mapper.TestLaboratoryDao;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 实验室管理(TestLaboratory)表服务实现类
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
@Service("testLaboratoryService")
public class TestLaboratoryServiceImpl extends ServiceImpl<TestLaboratoryDao, TestLaboratory> implements TestLaboratoryService {
    @Resource
    private TestLaboratoryDao testLaboratoryDao;
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;
    @Resource
    private TestInstrumentDao testInstrumentDao;
    @Resource
    private HKPersonDoorProvisionalAuthorityRelEntityMapper hkPersonDoorProvisionalAuthorityRelEntityMapper;

    @Override
    public Result addLaboratory(TestLaboratory testLaboratory, MultipartFile file) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (testLaboratory.getName() == null) {
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name", testLaboratory.getName()).eq("del_flag", 0)) != null) {
            return ResultUtil.error("实验室名称重复");
        }
        if (file != null) {
            Map<String, Object> mapObject = sysOssService.postAnnounce(file);
            String fileUrl = (String) mapObject.get("fileUrl");
            testLaboratory.setPicture(fileUrl);
        }
        testLaboratory.setStatus("0");
        testLaboratory.setDelFlag(0);
        testLaboratory.setCreateTime(new Date());
        testLaboratory.setUpdateTime(null);
        if (this.save(testLaboratory)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "添加实验室" + testLaboratory.getId() + "成功!", Const.TEAM_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "添加实验室失败!", Const.TEAM_MANAGEMENT_LOG, false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updLaboratory(TestLaboratory testLaboratory, MultipartFile file) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (testLaboratory.getId() == null) {
            return ResultUtil.error("修改对象ID为空");
        }
        if (testLaboratory.getName() == null) {
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name", testLaboratory.getName()).eq("del_flag", 0).ne("id", testLaboratory.getId())) != null) {
            return ResultUtil.error("实验室名称重复");
        }

        if (file != null) {
            Map<String, Object> mapObject = sysOssService.postAnnounce(file);
            String fileUrl = (String) mapObject.get("fileUrl");
            testLaboratory.setPicture(fileUrl);
            // 获取详情
            TestLaboratory data = this.getById(testLaboratory.getId());
            if (StringUtils.isNotEmpty(data.getPicture())) {
                // 进行移除附件
                sysOssService.delAnnounce(data.getPicture());
            }
        }

        testLaboratory.setUpdateTime(new Date());
        if (this.updateById(testLaboratory)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "修改实验室" + testLaboratory.getId() + "成功!", Const.TEAM_MANAGEMENT_LOG, true);
            return ResultUtil.success("修改成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "修改实验室" + testLaboratory.getId() + "失败!", Const.TEAM_MANAGEMENT_LOG, false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delLaboratory(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        for (Long id : idList) {
            // 查询实验室与仪器存在关联信息
            LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TestInstrument::getLaboratoryId, id);
            List<TestInstrument> instrumentList = testInstrumentDao.selectList(queryWrapper);
            if (CollectionUtil.isNotEmpty(instrumentList)) {
                return ResultUtil.error("删除失败，当前实验室 下存在关联设备");
            }
        }
        List<TestLaboratory> testLaboratoryList = new ArrayList<>();
        for (Long aLong : idList) {
            TestLaboratory testLaboratory = new TestLaboratory();
            testLaboratory.setUpdateTime(new Date());
            testLaboratory.setDelFlag(1);
            testLaboratory.setId(aLong.intValue());
            String url = this.getById(aLong).getPicture();
            if (url != null) {
                sysOssService.delAnnounce(url);
            }
            testLaboratoryList.add(testLaboratory);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testLaboratoryList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除实验室"+idStr+"成功!", Const.TEAM_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除实验室"+idStr+"失败!", Const.TEAM_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public Result getPageList(TestLaboratoryVo testLaboratory) {

        if (testLaboratory.getPageNum() == null || testLaboratory.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }

        PageHelper.clearPage();
        // 设置分页
        PageHelper.startPage(testLaboratory.getPageNum(), testLaboratory.getPageSize());

        LambdaQueryWrapper<TestLaboratory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestLaboratory::getDelFlag, 0);
        if (StringUtils.isNotEmpty(testLaboratory.getName())) {
            queryWrapper.like(TestLaboratory::getName, testLaboratory.getName());
        }
        if (StringUtils.isNotEmpty(testLaboratory.getCode())) {
            queryWrapper.like(TestLaboratory::getCode, testLaboratory.getCode());
        }
        if (StringUtils.isNotEmpty(testLaboratory.getPosition())) {
            queryWrapper.like(TestLaboratory::getPosition, testLaboratory.getPosition());
        }
        if (testLaboratory.getPageNum() == null || testLaboratory.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        // 排序
        queryWrapper.orderByDesc(TestLaboratory::getId);

        List<TestLaboratory> testLaboratorylist = testLaboratoryDao.selectList(queryWrapper);
        PageInfo<TestLaboratory> pageInfo = new PageInfo<>(testLaboratorylist);

        return ResultUtil.success(pageInfo);
    }
}

