package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.mapper.PartenrDao;
import com.lims.manage.erp.entity.Partenr;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PartenrService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 合作伙伴（用户）(Partenr)表服务实现类
 *
 * @author makejava
 * @since 2022-03-09 16:01:29
 */
@Service("partenrService")
public class PartenrServiceImpl extends ServiceImpl<PartenrDao, Partenr> implements PartenrService {

    @Override
    public Result addpartenr(Partenr partenr) {

        if (partenr.getPartnername()==null){
            return ResultUtil.error("检测公司不能为空");
        }
        if (this.getOne(new QueryWrapper<Partenr>().eq("partnername",partenr.getPartnername()))!=null){
            return ResultUtil.error("检测公司名称重复");
        }
        partenr.setStatus("0");
        partenr.setRemark("0");
        partenr.setCreateTime(new Date());
        if (this.save(partenr)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updpartenr(Partenr partenr) {
        if (partenr.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (partenr.getPartnername()==null){
            return ResultUtil.error("检测公司名称不能为空");
        }
        if (this.getOne(new QueryWrapper<Partenr>().eq("partnername",partenr.getPartnername()).eq("del_flag",0).ne("id",partenr.getId()))!=null){
            return ResultUtil.error("检测公司名称重复");
        }
        partenr.setUpdateTime(new Date());
        if (this.updateById(partenr)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }

    }

    @Override
    public Result delPatent(List<Long> idList) {
        List<Partenr> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            Partenr partenr=new Partenr();
            partenr.setId(aLong.intValue());
            partenr.setStatus("0");
            partenr.setRemark("0");
            partenr.setDelFlag(1);
            partenr.setUpdateTime(new Date());
            testMethods.add(partenr);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }

    }
}

