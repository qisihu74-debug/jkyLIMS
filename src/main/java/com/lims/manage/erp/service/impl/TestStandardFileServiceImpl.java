package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.StandardFileEntityMapper;
import com.lims.manage.erp.mapper.StandardMethodEntityMapper;
import com.lims.manage.erp.mapper.TestStandardFileDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestStandardFileService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 检验依据标准表(TestStandardFile)表服务实现类
 *
 * @author makejava
 * @since 2022-03-09 10:22:55
 */
@Service("testStandardFileService")
public class TestStandardFileServiceImpl extends ServiceImpl<TestStandardFileDao, TestStandardFile> implements TestStandardFileService {
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private SysOssService sysOssService;
    @Autowired
    private StandardFileEntityMapper standardFileEntityMapper;
    @Autowired
    private StandardMethodEntityMapper standardMethodEntityMapper;
    //添加
    @Override
    public Result addTestStandardFile(TestStandardFile testStandardFile) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testStandardFile.getName()==null){
            return ResultUtil.error("文件名称不能为空");
        }
        testStandardFile.setStatus("0");
        testStandardFile.setCreateTime(new Date());
        if (this.save(testStandardFile)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测标准"+testStandardFile.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加检测标准失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }
    //修改
    @Override
    public Result updTestStandardFile(TestStandardFile testStandardFile) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testStandardFile.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testStandardFile.getName()==null){
            return ResultUtil.error("名称不能为空");
        }
        if (this.updateById(testStandardFile)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测标准"+testStandardFile.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改检测标准"+testStandardFile.getId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }

    }
    //删除
    @Override
    public Result delTestStandardFile(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestStandardFile> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestStandardFile testStandardFile=new TestStandardFile();
            testStandardFile.setId(aLong.intValue());
            testStandardFile.setStatus("0");
            String url=this.getById(aLong).getFileUrl();
            if (url!=null){
                sysOssService.delAnnounce(url);
            }
            sysOssService.delAnnounce(this.getById(aLong).getFileUrl());
            testStandardFile.setDelFlag(1);
            testMethods.add(testStandardFile);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测标准"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除检测标准"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }

    }

    /**############################**/

    @Override
    public Result addStandardFile(StandardFileEntity standardFileEntity, MultipartFile standardFile) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (standardFileEntity.getName()==null){
            return ResultUtil.error("文件名称不能为空");
        }
        //上传文件
        if(standardFile != null){
            String file = standardFileEntity.getCode() + standardFileEntity.getName();
            String filename = standardFile.getOriginalFilename();
            String extension = filename.substring(filename.lastIndexOf("."));
            String upload = MinIoUtil.upload("standard-file", standardFile, file + extension);
            String[] url = upload.split("\\?");
            standardFileEntity.setFileUrl(url[0]);
        }
        standardFileEntity.setType("3");
        standardFileEntity.setStatus("0");
        standardFileEntity.setDelFlag(1);
        standardFileEntity.setCreateTime(new Date());
        int maxId = standardFileEntityMapper.getMaxId();
        standardFileEntity.setId(maxId);
        standardFileEntity.setPid(maxId);
        int insert = standardFileEntityMapper.insert(standardFileEntity);
        if (insert > 0){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测标准"+standardFileEntity.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测标准失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result addStandardMethod(StandardMethodEntity standardMethodEntity) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        standardMethodEntity.setCreateTime(new Date());
        int insert = standardMethodEntityMapper.insert(standardMethodEntity);
        if (insert > 0){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测方法"+standardMethodEntity.getChapterName()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测方法失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateStandard(StandardFileEntity standardFileEntity, MultipartFile standardFile) {
        //作废旧依据
        Integer pid = standardFileEntity.getPid();
        Integer oldId = standardFileEntity.getId();
        StandardFileEntity old = standardFileEntityMapper.getDetail(oldId);
        old.setStandardStatus("作废");
        old.setExpirationDate(new Date());
        standardFileEntityMapper.insertRecord(old);
        //保存新依据
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (standardFileEntity.getName()==null){
            return ResultUtil.error("文件名称不能为空");
        }
        //上传文件
        if(standardFile != null){
            String file = standardFileEntity.getCode() + standardFileEntity.getName();
            String filename = standardFile.getOriginalFilename();
            String extension = filename.substring(filename.lastIndexOf("."));
            String upload = MinIoUtil.upload("standard-file", standardFile, file + extension);
            String[] url = upload.split("\\?");
            standardFileEntity.setFileUrl(url[0]);
        }
        standardFileEntity.setType("3");
        standardFileEntity.setStatus("0");
        standardFileEntity.setDelFlag(1);
        standardFileEntity.setCreateTime(new Date());
        int maxId = standardFileEntityMapper.getMaxId();
        standardFileEntity.setId(maxId);
        standardFileEntity.setPid(pid);
        int insert = standardFileEntityMapper.insert(standardFileEntity);
        standardFileEntityMapper.deleteByPrimaryKey(oldId);
        //继承旧方法
        List<StandardMethodEntity> methods = standardMethodEntityMapper.getByStandardId(oldId);
        if(!CollectionUtils.isEmpty(methods)){
            for (int i = 0; i < methods.size(); i++) {
                methods.get(i).setStandardId(maxId);
            }
            standardMethodEntityMapper.batchInsert(methods);
        }
        if (insert > 0){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测标准"+standardFileEntity.getId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加检测标准失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result getRecords(Integer pid) {
        return ResultUtil.success("查询变更记录成功！",standardFileEntityMapper.getRecords(pid));
    }

    @Override
    public Result getMethodList(Integer id) {
        return ResultUtil.success("查询检测方法成功！",standardMethodEntityMapper.getByStandardId(id));
    }

    @Override
    public Result deleteMethod(Integer id) {
        return ResultUtil.success("删除成功！",standardMethodEntityMapper.deleteByPrimaryKey(id));
    }

}

