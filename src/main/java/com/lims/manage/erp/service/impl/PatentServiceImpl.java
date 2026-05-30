package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.PatentAuthorization;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.PatentDao;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.PatentAuthorizationService;
import com.lims.manage.erp.service.PatentService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private LogManagerService logManagerService;
    @Resource
    private TestProductDao productDao;
    @Resource
    private PatentAuthorizationService authorizationService;


    @Override
    public Result addPatent(Patent Patent) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (Patent.getPatentId() == null) {
            return ResultUtil.error("专利号不能为空");
        }
        if (Patent.getPatentName() == null) {
            return ResultUtil.error("专利名称不能为空");
        }

        Patent = savePatent(Patent);

        if (this.save(Patent)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" +
                            userInfo.getUsername() + "添加专利" + Patent.getId() + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" +
                            userInfo.getUsername() + "添加专利失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("添加失败，未知异常!");
        }

    }

    @Override
    public Result updPatent(Patent Patent) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (Patent.getPatentId() == null) {
            return ResultUtil.error("专利号不能为空");
        }
        if (Patent.getPatentName() == null) {
            return ResultUtil.error("专利名称不能为空");
        }
        if (this.getOne(new QueryWrapper<Patent>()
                .eq("patent_name", Patent.getPatentName())
                .eq("del_flag", 0).ne("id", Patent.getId())) != null) {
            return ResultUtil.error("检测专利名称重复");
        }

        Patent = updatePatent(Patent);

        if (this.updateById(Patent)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户："
                            + userInfo.getUsername() + "修改专利" + Patent.getId() + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("修改成功!");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户："
                            + userInfo.getUsername() + "修改专利" + Patent.getId() + "失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("修改失败，未知异常!");
        }

    }

    @Override
    public Result delPatent(String id) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        Patent patent = getById(id);
        patent.setDelFlag(1);

        if (this.updateById(patent)) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" +
                            userInfo.getUsername() + "删除专利" + id + "成功!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, true);
            return ResultUtil.success("删除成功");
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" +
                            userInfo.getUsername() + "删除专利" + id + "失败!",
                    Const.KNOWLEDGE_MANAGEMENT_LOG, false);
            return ResultUtil.error("删除失败");
        }
    }


    public Patent savePatent(Patent patent) {
        //产品
        if (StringUtils.isNotBlank(patent.getProductId())) {
            StringBuilder productStr = new StringBuilder();
            String[] strings = patent.getProductId().split(",");
            for (String str : strings) {
                productStr.append(productDao.getProductNameById(Integer.valueOf(str))).append(",");
            }
            patent.setProduct(productStr.deleteCharAt(productStr.length() - 1).toString());
        }
        patent.setDelFlag(0);
        patent.setCreateTime(new Date());

        List<PatentAuthorization> authorizationList = patent.getPatentAuthorizations();
        if (authorizationList != null && !authorizationList.isEmpty()) {
            for (PatentAuthorization authorization : authorizationList) {
                if (authorization.getAuthorizationName() != null) {
                    authorization.setPatentId(patent.getPatentId());
                    //对外授权情况存储
                    authorizationService.save(authorization);
                }
            }
        }
        return patent;
    }


    public Patent updatePatent(Patent patent) {
        //产品
        if (StringUtils.isNotBlank(patent.getProductId())) {
            StringBuilder productStr = new StringBuilder();
            String[] strings = patent.getProductId().split(",");
            for (String str : strings) {
                productStr.append(productDao.getProductNameById(Integer.valueOf(str))).append(",");
            }
            patent.setProduct(productStr.deleteCharAt(productStr.length() - 1).toString());
        }
        patent.setDelFlag(0);
        patent.setCreateTime(new Date());

        List<PatentAuthorization> authorizationList = patent.getPatentAuthorizations();
        if (authorizationList != null && !authorizationList.isEmpty()) {
            for (PatentAuthorization authorization : authorizationList) {
                if (authorization.getAuthorizationName() != null) {
                    if (authorization.getId() != null) {
                        //对外授权情况存储
                        authorizationService.updateById(authorization);
                    } else {
                        authorization.setPatentId(patent.getPatentId());
                        authorizationService.save(authorization);

                    }
                }
            }
        }
        return patent;
    }


}

