package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductTypeDao;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductTypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 产品分类(TestProductType)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 10:03:13
 */
@Service("testProductTypeService")
public class TestProductTypeServiceImpl extends ServiceImpl<TestProductTypeDao, TestProductType> implements TestProductTypeService {

    @Override
    public Result addProductType(TestProductType type) {
        if (type.getProductTypeName()==null){
            return ResultUtil.error("分类名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductType>().eq("product_type_name",type.getProductTypeName()))!=null){
            return ResultUtil.error("分类名称重复");
        }
        type.setStatus("0");
        type.setDelFlag(0);
        type.setCreateTime(new Date());
        if (this.save(type)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updProductType(TestProductType type) {
        if (type.getProductTypeId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (type.getProductTypeName()==null){
            return ResultUtil.error("分类名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductType>().eq("product_type_name",type.getProductTypeName()).eq("del_flag",0).ne("product_type_id",type.getProductTypeId()))!=null){
            return ResultUtil.error("分类名称重复");
        }
        type.setUpdateTime(new Date());
        if (this.updateById(type)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delProductType(List<Long> idList) {
        List<TestProductType> testProductTypeList=new ArrayList<>();
        for (Long aLong : idList) {
            TestProductType testProductType=new TestProductType();
            testProductType.setUpdateTime(new Date());
            testProductType.setDelFlag(1);
            testProductType.setProductTypeId(aLong.intValue());
            testProductTypeList.add(testProductType);
        }
        if (this.updateBatchById(testProductTypeList)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }
}

