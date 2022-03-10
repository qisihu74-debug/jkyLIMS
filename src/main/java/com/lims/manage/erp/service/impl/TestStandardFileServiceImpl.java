package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.mapper.TestStandardFileDao;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestStandardFileService;
import org.springframework.stereotype.Service;

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

//添加
    @Override
    public Result addTestStandardFile(TestStandardFile testStandardFile) {
        if (testStandardFile.getName()==null){
            return ResultUtil.error("文件名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestStandardFile>().eq("name",testStandardFile.getName()))!=null){
            return ResultUtil.error("检测方法名称重复");
        }
        testStandardFile.setStatus("0");

        testStandardFile.setCreateTime(new Date());

        if (this.save(testStandardFile)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

//    修改
    @Override
    public Result updTestStandardFile(TestStandardFile testStandardFile) {
        if (testStandardFile.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testStandardFile.getName()==null){
            return ResultUtil.error("名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestStandardFile>().eq("name",testStandardFile.getName()).eq("del_flag",0).ne("id",testStandardFile.getId()))!=null){
            return ResultUtil.error("检测专利名称重复");
        }

        if (this.updateById(testStandardFile)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }

    }
//删除
    @Override
    public Result delTestStandardFile(List<Long> idList) {
        List<TestStandardFile> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestStandardFile testStandardFile=new TestStandardFile();
            testStandardFile.setId(aLong.intValue());
            testStandardFile.setStatus("0");
            testStandardFile.setFileUrl("");
            testStandardFile.setDelFlag(1);
            testMethods.add(testStandardFile);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }

    }
}

