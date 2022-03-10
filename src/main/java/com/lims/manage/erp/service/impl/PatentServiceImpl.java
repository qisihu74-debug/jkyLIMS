package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.mapper.PatentDao;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PatentService;
import com.lims.manage.erp.service.TestProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * (Patent)表服务实现类
 *
 * @author makejava
 * @since 2022-03-08 10:40:18
 */
@Service("patentService")
public class PatentServiceImpl extends ServiceImpl<PatentDao, Patent> implements PatentService {
    @Resource
    private TestProductService testProductService;
    @Override
    public Result addPatent(Patent Patent) {

        if (Patent.getPatentname()==null){
            return ResultUtil.error("检测专利名称不能为空");
        }
//        if (this.getOne(new QueryWrapper<Patent>().eq("Patentname",Patent.getPatentname()))!=null){
//            return ResultUtil.error("检测方法名称重复");
//        }
       Patent.setStart("0");
        Patent.setRemark("0");
//        Patent.setPatenttime(new Date());
        Patent.setUrl("");
        Patent.setProducname(testProductService.getById(Patent.getProducid()).getProductName());
        if (this.save(Patent)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updPatent(Patent Patent) {


        if (Patent.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (Patent.getPatentname()==null){
            return ResultUtil.error("检测方法名称不能为空");
        }
        if (this.getOne(new QueryWrapper<Patent>().eq("Patentname",Patent.getPatentname()).eq("del_flag",0).ne("id",Patent.getId()))!=null){
            return ResultUtil.error("检测专利名称重复");
        }
        Patent.setPatenttime(new Date());
        if (this.updateById(Patent)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }

    }

    @Override
    public Result delPatent(List<Long> idList) {
        List<Patent> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            Patent Patent=new Patent();
            Patent.setId(aLong.intValue());
            Patent.setStart("0");
            Patent.setRemark("0");
            Patent.setDelFlag(1);
            Patent.setPatenttime(new Date());
            testMethods.add(Patent);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }
}

