package com.lims.manage.erp.controller;

import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.AuditTeamNumber;
import com.lims.manage.erp.entity.BaseTreeBuild;
import com.lims.manage.erp.entity.DivideAuditDetail;
import com.lims.manage.erp.entity.DivideAuditDetailRel;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AduditBaseDataService;
import com.lims.manage.erp.service.AuditTeamNumberService;
import com.lims.manage.erp.service.DivideAuditDetailRelService;
import com.lims.manage.erp.service.DivideAuditDetailService;
import com.lims.manage.erp.service.DivideRectificationRecordService;
import com.lims.manage.erp.service.QsAuditService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.util.DingNotifyUtils;
import com.lims.manage.erp.util.ShiroUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc 部门负责人（部长/所长/主任）；内审员（具有内审角色的账户）
 * @date 2024-07-04 15:56
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/audit/")
public class QsAuditController {
    @Autowired
    private QsAuditService qsAuditService;
    @Autowired
    private AduditBaseDataService aduditBaseDataService;
    @Autowired
    private DivideAuditDetailService divideAuditDetailService;
    @Autowired
    private DivideAuditDetailRelService divideAuditDetailRelService;
    @Autowired
    private DivideRectificationRecordService divideRectificationRecordService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;
    @Autowired
    private AuditTeamNumberService auditTeamNumberService;

    /**
     * 技术质量部内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("internalAuditorActiveList")
    public Result internalAuditorActiveList(Integer pageNum, Integer pageSize, String name){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少参数");
        }
        Long userId = ShiroUtils.getUserInfo().getUserId();
        PageInfo<InternalAuditorActive> pageInfo = qsAuditService.internalAuditorActiveList(pageNum,pageSize,name,userId);
        //处理内审员
        List<Integer> ids = Lists.newArrayList();
        for (InternalAuditorActive active :pageInfo.getList()){
            ids.add(active.getActiveId());
        }
        //查询活动下的人员信息
        LambdaQueryWrapper<AuditTeamNumber> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(AuditTeamNumber::getActiveId,ids);
        List<AuditTeamNumber> list = auditTeamNumberService.list(queryWrapper);
        for (InternalAuditorActive active :pageInfo.getList()){
            List<AuditTeamNumber> userList = Lists.newArrayList();
            for (AuditTeamNumber auditTeamNumber :list){
                if (active.getActiveId() == auditTeamNumber.getActiveId()){
                    AuditTeamNumber teamNumber = new AuditTeamNumber();
                    teamNumber.setUserId(auditTeamNumber.getUserId());
                    teamNumber.setName(auditTeamNumber.getName());
                    userList.add(teamNumber);
                }
            }
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 开始检查前获取基础数据
     * @return
     */
    @GetMapping("getCheckBaseDataList")
    public Result getCheckBaseDataList(){
        List<AduditBaseData> list = qsAuditService.getCheckBaseDataList();
        //根据类型处理数据
        List<AduditBaseData> cnasList = Lists.newArrayList();
        List<AduditBaseData> cmaList = Lists.newArrayList();
        for (AduditBaseData baseData :list){
            if ("CNAS".equals(baseData.getType())){
                cnasList.add(baseData);
            }
            if ("CMA".equals(baseData.getType())){
                cmaList.add(baseData);
            }
        }
        List<AduditBaseData> cnasLis = BaseTreeBuild.buildTree(cnasList);
        List<AduditBaseData> cmaLis = BaseTreeBuild.buildTree(cmaList);
        Map<String,List<AduditBaseData>> map = new HashMap<>();
        map.put("CNAS",cnasLis);
        map.put("CMA",cmaLis);
        return ResultUtil.success(map);
    }

    /**
     * 检查暂存
     * @param detailRel
     * @return
     */
    @PostMapping("stagingCheck")
    public Result stagingCheck(@RequestBody DivideAuditDetailRel detailRel){
        if (CollectionUtils.isEmpty(detailRel.getList())){
            return ResultUtil.error("暂无填写审核项请重试！");
        }
        if (detailRel.getActiveId() == 0 || detailRel.getDivideId() == 0
                ||detailRel.getList().get(0).getDirectoryId() == 0 || StringUtils.isEmpty(detailRel.getList().get(0).getOpinion())){
            return ResultUtil.error("缺少必要参数");
        }
        //查询活动状态，判断管理员是否完成检查
        String state = qsAuditService.getStateByActiveId(detailRel.getActiveId());
        if ("待开始，首次会议".contains(state)){
            return ResultUtil.error("操作失败，内审活动的首次会议还未完成");
        }
        if ("末次会议，问题整改，待总结，已完成".contains(state)){
            return ResultUtil.error("操作失败，管理员已将内审活动完成检查");
        }
        //暂存
        if ("内审检查".contains(state)){
            for (DivideAuditDetail detail:detailRel.getList()){
                detail.setDivideId(detailRel.getDivideId());
            }
            divideAuditDetailService.saveBatch(detailRel.getList());
            //插入或者更新qs_audit_divide_rel
            DivideAuditDetailRel rel = new DivideAuditDetailRel();
            rel.setDivideId(detailRel.getActiveId());
            rel.setState("检查中");
            divideAuditDetailRelService.saveOrUpdate(rel);
            return ResultUtil.success("暂存成功");
        }
        return ResultUtil.error("无效的内审活动类型："+state);
    }

    /**
     * 检查提交
     * @param detailRel
     * @return
     */
    @PostMapping("submitCheck")
    @Transactional(rollbackFor = Exception.class)
    public Result submitCheck(@RequestBody DivideAuditDetailRel detailRel){
        if (CollectionUtils.isEmpty(detailRel.getList())){
            return ResultUtil.error("暂无填写审核项请重试！");
        }
        if (StringUtils.isEmpty(detailRel.getNonComplianceDegree()) ||StringUtils.isEmpty(detailRel.getNonConformance())
                || StringUtils.isEmpty(detailRel.getNonConformanceProgram()) || StringUtils.isEmpty(detailRel.getSubstandard())
                ||StringUtils.isEmpty(detailRel.getCheckResult())){
            return ResultUtil.error("缺少检查结果相关信息");
        }
        if (detailRel.getActiveId() == 0 || detailRel.getDivideId() == 0
                ||detailRel.getList().get(0).getDirectoryId() == 0 || StringUtils.isEmpty(detailRel.getList().get(0).getOpinion())){
            return ResultUtil.error("缺少必要参数");
        }
        //查询活动状态，判断管理员是否完成检查
        String state = qsAuditService.getStateByActiveId(detailRel.getActiveId());
        if ("待开始，首次会议".contains(state)){
            return ResultUtil.error("操作失败，内审活动的首次会议还未完成");
        }
        if ("末次会议，问题整改，待总结，已完成".contains(state)){
            return ResultUtil.error("操作失败，管理员已将内审活动完成检查");
        }
        //内审检查提交，推送消息给管理员
        if ("内审检查".contains(state)){
            for (DivideAuditDetail detail:detailRel.getList()){
                detail.setDivideId(detailRel.getDivideId());
            }
            //删除旧的qs_audit_divide_detail数据
            Map<String,Object> map = new HashMap<>();
            map.put("divide_id",detailRel.getDivideId());
            divideAuditDetailService.removeByMap(map);
            //插入新的数据
            divideAuditDetailService.saveBatch(detailRel.getList());
            //更新qs_audit_divide_rel，如果不存在不符合项状态为已完成，存在为检查完成
            if ("合格".equals(detailRel.getCheckResult())){
                detailRel.setState("已完成");
            }else {
                detailRel.setState("检查完成");
            }
            divideAuditDetailRelService.saveOrUpdate(detailRel);
            //获取内审活动管理人员的钉钉id，通知管理员检查已完成
            String userId = qsAuditService.getUserIdByActiveId(detailRel.getActiveId());
            LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserEntity::getUserId, userId);
            SysUserEntity userDetails = sysUserService.getOne(queryWrapper);
            try {
                dingNotifyUtils.OAWorkNotice(userDetails.getDingUserId(),"内审完成通知",detailRel.getUserName(),"部门："+detailRel.getDeptName()+"的内审已完成");
            } catch (Exception e) {
                log.error("内审完成时发送给管理员通知失败:{}",e);
            }
            //如果存在不符合项生成整改记录
            if ("需整改".equals(detailRel.getCheckResult())){
                DivideRectificationRecord record = new DivideRectificationRecord();
                record.setDivideId(detailRel.getDivideId());
                record.setState("整改通知");
                divideRectificationRecordService.save(record);
                //获取部门负责人（部长/所长/主任）,通知部门负责人接收整改通知
                String dingId = sysUserService.getPositionByDeptName(detailRel.getDeptName());
                if (StringUtils.isNotEmpty(dingId)){
                    try {
                        dingNotifyUtils.OAWorkNotice(dingId,"内审整改通知",detailRel.getUserName(),"部门："+detailRel.getDeptName()+"的内审存在不符合项需整改");
                    } catch (Exception e) {
                        log.error("内审完成时发送给管理员通知失败:{}",e);
                    }
                }
            }
            return ResultUtil.success("暂存成功");
        }
        return ResultUtil.error("无效的内审活动类型："+state);
    }

    //继续检查数据回显，

    //检查完成修改数据回显

    @SneakyThrows
    @GetMapping("importBaseData")
    public Result importBaseData(){
        List<AduditBaseData> list = Lists.newArrayList();
        //读取excel
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\CMA.xlsx");
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        //遍历excel，根据编号查询
        int num = 2;

        while (num<=242){
            AduditBaseData baseData = new AduditBaseData(null,null,null,null,null,null,null,0);
            baseData.setType(cells.get("A"+num).getValue().toString());
            baseData.setDirectory(cells.get("B"+num).getValue().toString());
            String string = cells.get("C" + num).getValue().toString();
            String[] split = string.split("\\.");
            baseData.setId(Integer.parseInt(split[0]));
            String string1 = cells.get("D" + num).getValue().toString();
            String[] split1 = string1.split("\\.");
            baseData.setPid(Integer.parseInt(split1[0]));
            baseData.setContent(cells.get("E"+num).getValue().toString());
            Object value = cells.get("F" + num).getValue();
            if (value != null){
                baseData.setMethod(value.toString());
            }
            Object value1 = cells.get("G" + num).getValue();
            if (value1 != null){
                baseData.setNote(value1.toString());
            }
            list.add(baseData);
            num++;
        }
        aduditBaseDataService.saveBatch(list);
        return ResultUtil.success("导入成功");
    }
}
