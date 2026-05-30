package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.mapper.TestProductTypeDao;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.service.TestProductTypeService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.StringUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
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
    @Resource
    private LogManagerService logManagerService;

    @Resource
    private TestProductService testProductService;

    @Override
    public Result addProductType(TestProductType type) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (type.getProductTypeName()==null){
            return ResultUtil.error("分类名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductType>().eq("product_type_name",type.getProductTypeName()).eq("del_flag",0))!=null){
            return ResultUtil.error("分类名称重复");
        }
        type.setStatus("0");
        type.setDelFlag(0);
        type.setCreateTime(new Date());
        if (this.save(type)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品类型"+type.getProductTypeId()+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            return ResultUtil.success("添加成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品类型失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updProductType(TestProductType type) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
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
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品类型"+type.getProductTypeId()+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功!");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品类型"+type.getProductTypeId()+"失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delProductType(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestProductType> testProductTypeList=new ArrayList<>();
        //存储有产品的产品分类名称
        StringBuilder noDelProductType=new StringBuilder();
        for (Long aLong : idList) {
            //根据产品分类id获取产品信息
            LambdaQueryWrapper<TestProduct> wrapper= Wrappers.lambdaQuery();
            wrapper.eq(TestProduct::getProductTypeId,aLong);
            int count = testProductService.count(wrapper);
            if(count==0){
                TestProductType testProductType=new TestProductType();
                testProductType.setUpdateTime(new Date());
                testProductType.setDelFlag(1);
                testProductType.setProductTypeId(aLong.intValue());
                testProductTypeList.add(testProductType);
            }else{
                TestProductType productType = this.getById(aLong);
                if(productType!=null){
                    noDelProductType.append(productType.getProductTypeName()).append(",");
                }
            }
        }
        if(ListUtils.isEmpty(testProductTypeList)){
            if(StringUtils.isNotEmpty(noDelProductType)){
                return ResultUtil.error("产品分类："+noDelProductType+"下存在产品,请删除产品后再尝试！");
            }else{
                return ResultUtil.error("没有找到要删除的产品分类信息");
            }
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testProductTypeList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品类型"+idStr+"成功!", Const.PRODUCT_MANAGEMENT_LOG,true);
            if(StringUtils.isNotEmpty(noDelProductType)){
                return ResultUtil.error("产品分类："+noDelProductType+"下存在产品,请删除产品后再尝试！");
            }
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品类型"+idStr+"失败!", Const.PRODUCT_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }
}

