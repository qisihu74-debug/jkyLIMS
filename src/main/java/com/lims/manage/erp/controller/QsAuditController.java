package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.AuditTeamNumber;
import com.lims.manage.erp.entity.BaseTreeBuild;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.entity.DivideAuditDetail;
import com.lims.manage.erp.entity.DivideAuditDetailRel;
import com.lims.manage.erp.entity.DivideEntity;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AduditBaseDataService;
import com.lims.manage.erp.service.AuditTeamNumberService;
import com.lims.manage.erp.service.DeptService;
import com.lims.manage.erp.service.DivideAuditDetailRelService;
import com.lims.manage.erp.service.DivideAuditDetailService;
import com.lims.manage.erp.service.DivideRectificationRecordService;
import com.lims.manage.erp.service.DivideService;
import com.lims.manage.erp.service.QsAuditService;
import com.lims.manage.erp.service.SysUserService;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.DingNotifyUtils;
import com.lims.manage.erp.util.MinIoUtil;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Autowired
    private DivideService divideService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private ActiveService activeService;

    /**
     * 技术质量部内审活动列表
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("internalAuditorActiveList")
    public Result internalAuditorActiveList(Integer pageNum, Integer pageSize, String name) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少参数");
        }
        Long userId = ShiroUtils.getUserInfo().getUserId();
        PageInfo<InternalAuditorActive> pageInfo = qsAuditService.internalAuditorActiveList(pageNum, pageSize, name, userId);
        //处理内审员
        List<Integer> ids = Lists.newArrayList();
        for (InternalAuditorActive active : pageInfo.getList()) {
            ids.add(active.getDivideId());
        }
        //查询活动下的人员信息
        LambdaQueryWrapper<DivideEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(DivideEntity::getDivideId,ids);
        List<DivideEntity> list = divideService.list(queryWrapper);
        for (InternalAuditorActive active :pageInfo.getList()){
            List<AuditTeamNumber> userList = Lists.newArrayList();
            for (DivideEntity divideEntity :list){
                if (active.getDivideId() == divideEntity.getDivideId()){
                    AuditTeamNumber teamNumber = new AuditTeamNumber();
                    teamNumber.setUserId(divideEntity.getAuditorId());
                    teamNumber.setName(divideEntity.getAuditorName());
                    userList.add(teamNumber);
                }
            }
            active.setUserList(userList);
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 继续检查数据操作、检查完成修改操作回显
     * @param divideId
     * @return
     */
    @GetMapping("checkEcho")
    public Result checkEcho(int divideId){
        if (divideId == 0){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<DivideAuditDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DivideAuditDetail::getDivideId,divideId);
        List<DivideAuditDetail> list = divideAuditDetailService.list(queryWrapper);
        LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(DivideAuditDetailRel::getDivideId,divideId);
        DivideAuditDetailRel one = divideAuditDetailRelService.getOne(queryWrapper1);
        one.setList(list);
        return ResultUtil.success(one);
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
    @Log(title = "检查暂存", businessType = BusinessType.INSERT)
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
            rel.setDivideId(detailRel.getDivideId());
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
    @Log(title = "检查提交", businessType = BusinessType.INSERT)
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
            return ResultUtil.success("提交成功");
        }
        return ResultUtil.error("无效的内审活动类型："+state);
    }

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

    /**
     * 部门负责人查看内审活动列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("deptLeaderActiveList")
    public Result deptLeaderActiveList(Integer pageNum, Integer pageSize, String name) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少参数");
        }
        Long userId = ShiroUtils.getUserInfo().getUserId();
        PageInfo<InternalAuditorActive> pageInfo = qsAuditService.deptLeaderActiveList(pageNum,pageSize,name,userId);
        //处理内审员
        List<Integer> fgIds = Lists.newArrayList();

        for (InternalAuditorActive active :pageInfo.getList()){
            fgIds.add(active.getDivideId());
        }
        //查询活动下的人员信息
        if (CollectionUtils.isNotEmpty(fgIds)){
            LambdaQueryWrapper<DivideEntity> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(DivideEntity::getDivideId,fgIds);
            List<DivideEntity> list = divideService.list(queryWrapper);
            for (InternalAuditorActive active :pageInfo.getList()){
                List<AuditTeamNumber> userList = Lists.newArrayList();
                for (DivideEntity divideEntity :list){
                    if (active.getDivideId() == divideEntity.getDivideId()){
                        AuditTeamNumber teamNumber = new AuditTeamNumber();
                        teamNumber.setUserId(divideEntity.getAuditorId());
                        teamNumber.setName(divideEntity.getAuditorName());
                        userList.add(teamNumber);
                    }
                }
                active.setUserList(userList);
            }
            //查询基本信息qs_audit_divide_rel
            LambdaQueryWrapper<DivideRectificationRecord> queryWrapper1 = new LambdaQueryWrapper();
            queryWrapper1.in(DivideRectificationRecord::getDivideId,fgIds);
            List<DivideRectificationRecord> records = divideRectificationRecordService.list(queryWrapper1);
            for (InternalAuditorActive active :pageInfo.getList()){
                for (DivideRectificationRecord record :records){
                    if (active.getDivideId() == record.getDivideId()){
                        active.setRectificationRecord(record);
                    }
                }
            }
            //基本信息
            LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.in(DivideAuditDetailRel::getDivideId,fgIds);
            List<DivideAuditDetailRel> auditDetailRels = divideAuditDetailRelService.list(queryWrapper2);
            for (InternalAuditorActive active :pageInfo.getList()){
                for (DivideAuditDetailRel detailRel :auditDetailRels){
                    if (active.getDivideId() == detailRel.getDivideId()){
                        active.setDivideAuditDetailRel(detailRel);
                    }
                }
            }

        }
        return ResultUtil.success(pageInfo);
    }



    /**
     * 部门负责人接收通知提交
     * @param record
     * @return
     */
    @Log(title = "部门负责人接收整改通知", businessType = BusinessType.INSERT)
    @PostMapping("acceptNotice")
    public Result acceptNotice(@RequestBody DivideRectificationRecord record) {
        if (StringUtils.isEmpty(record.getAnalysisAndCorrectiveMeasures()) || record.getRequiredCompletionDate() == null
                || StringUtils.isEmpty(record.getDeptLeader()) || record.getReceivedDate() == null) {
            return ResultUtil.error("缺少检查结果相关信息");
        }
        //查询活动状态，判断管理员是否完成检查
        String state = qsAuditService.getStateByActiveId(record.getActiveId());
        if ("待总结，已完成".contains(state)) {
            return ResultUtil.error("操作失败，管理员已将内审活动完成整改");
        }
        //判断内审员操作状态
        LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DivideAuditDetailRel::getDivideId,record.getDivideId());
        DivideAuditDetailRel one = divideAuditDetailRelService.getOne(queryWrapper);
        if ("已完成".equals(one.getState())){
            return ResultUtil.error("操作失败，内审员已将措施验证完成");
        }
        //更新状态为等待纠正
        record.setState("等待纠正");
        divideRectificationRecordService.updateById(record);
        return ResultUtil.success("已接收整改通知");
    }

    /**
     * 部门负责人完成纠正
     * @param json
     * @param files
     * @return
     */
    @Log(title = "部门负责人完成纠正", businessType = BusinessType.INSERT)
    @PostMapping("completeCorrection")
    public Result completeCorrection(@RequestParam("json") String json, MultipartFile[] files) {
        DivideRectificationRecord record = JSON.parseObject(json, DivideRectificationRecord.class);
        if (StringUtils.isEmpty(record.getCorrectionCompletionStatus()) || record.getActualFinishingDate() == null
                || StringUtils.isEmpty(record.getDeptLeader())) {
            return ResultUtil.error("缺少检查结果相关信息");
        }
        if (files == null){
            return ResultUtil.error("请上传整改报告");
        }
        //查询活动状态，判断管理员是否完成检查
        String state = qsAuditService.getStateByActiveId(record.getActiveId());
        if ("待总结，已完成".contains(state)) {
            return ResultUtil.error("操作失败，管理员已将内审活动完成整改");
        }
        //判断内审员操作状态
        LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DivideAuditDetailRel::getDivideId,record.getDivideId());
        DivideAuditDetailRel one = divideAuditDetailRelService.getOne(queryWrapper);
        if ("已完成".equals(one.getState())){
            return ResultUtil.error("操作失败，内审员已将措施验证完成");
        }
        //文件上传，要求是pdf文档
        StringBuilder stringBuilder = new StringBuilder();
        for (MultipartFile file: files){
            if (file != null) {
                String filename = file.getOriginalFilename();
                String[] split = filename.split("\\.");
                if (!"pdf".equals(split[split.length - 1])) {
                    return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
                }
                //上传附件
                String upload = MinIoUtil.upload(BucketsConst.internal_audit, file, file.getOriginalFilename());
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String uploadUrl = upload.substring(0, upload.indexOf("?"));
                    stringBuilder.append(uploadUrl);
                    stringBuilder.append(",");
                }
            }
        }
        record.setUrl(stringBuilder.toString().substring(0,stringBuilder.length()-1));
        //更新状态为等待纠正
        record.setState("等待验证");
        divideRectificationRecordService.updateById(record);
        //钉钉通知内审员,根据活动id查询内审员id集合
        Set<Long> ids = new HashSet<>();
        LambdaQueryWrapper<AuditTeamNumber> numberLambdaQueryWrapper = new LambdaQueryWrapper();
        numberLambdaQueryWrapper.eq(AuditTeamNumber::getActiveId,record.getActiveId());
        List<AuditTeamNumber> list = auditTeamNumberService.list(numberLambdaQueryWrapper);
        for (AuditTeamNumber auditTeamNumber :list){
            ids.add(Long.parseLong(auditTeamNumber.getUserId()));
        }
        //获取钉钉id
        List<String> idsByUserIds = sysUserService.getDingIdsByUserIds(ids);
        for (String dingId :idsByUserIds){
            try {
                dingNotifyUtils.OAWorkNotice(dingId,"部门负责人纠正完成通知",record.getDeptLeader(),"部门负责人："+record.getDeptLeader()+" 已完成纠正，请相关内审员及时处理");
            }catch (Exception e){
                log.error("部门负责人完成整改纠正发送通知给内审员失败:{}",e);
            }
        }
        return ResultUtil.success("已接收整改通知");
    }

    /**
     * 部门负责人修改
     * @param json
     * @param files
     * @return
     */
    @Log(title = "部门负责人修改整改休息", businessType = BusinessType.INSERT)
    @PostMapping("editCorrection")
    public Result editCorrection(@RequestParam("json") String json, MultipartFile[] files) {
        DivideRectificationRecord record = JSON.parseObject(json, DivideRectificationRecord.class);
        if (StringUtils.isEmpty(record.getCorrectionCompletionStatus()) || record.getActualFinishingDate() == null
                || StringUtils.isEmpty(record.getDeptLeader())) {
            return ResultUtil.error("缺少检查结果相关信息");
        }
        //查询活动状态，判断管理员是否完成检查
        String state = qsAuditService.getStateByActiveId(record.getActiveId());
        if ("待总结，已完成".contains(state)) {
            return ResultUtil.error("操作失败，管理员已将内审活动完成整改");
        }
        //判断内审员操作状态
        LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DivideAuditDetailRel::getDivideId,record.getDivideId());
        DivideAuditDetailRel one = divideAuditDetailRelService.getOne(queryWrapper);
        if ("已完成".equals(one.getState())){
            return ResultUtil.error("操作失败，内审员已将措施验证完成");
        }
        //文件上传，要求是pdf文档
        StringBuilder stringBuilder = new StringBuilder();
        if (files != null){
            //删除旧文件
            DivideRectificationRecord byId = divideRectificationRecordService.getById(record.getDivideId());
            if (StringUtils.isNotEmpty(byId.getUrl())){
                String[] split = byId.getUrl().split(",");
                for (String url :split){
                    String[] strings = url.split("\\/");
                    String bluckName = strings[3];
                    String fileName = strings[4];
                    MinIoUtil.deleteFile(bluckName,fileName);
                }
            }
            for (MultipartFile file: files){
                if (file != null) {
                    String filename = file.getOriginalFilename();
                    String[] split = filename.split("\\.");
                    if (!"pdf".equals(split[split.length - 1])) {
                        return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
                    }
                    //上传附件
                    String upload = MinIoUtil.upload(BucketsConst.internal_audit, file, file.getOriginalFilename());
                    if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                        String uploadUrl = upload.substring(0, upload.indexOf("?"));
                        stringBuilder.append(uploadUrl);
                        stringBuilder.append(",");
                    }
                }
            }
            record.setUrl(stringBuilder.toString().substring(0,stringBuilder.length()-1));
        }
        //更新状态为等待纠正
        record.setState("等待验证");
        divideRectificationRecordService.updateById(record);
        //钉钉通知内审员,根据活动id查询内审员id集合
        Set<Long> ids = new HashSet<>();
        LambdaQueryWrapper<AuditTeamNumber> numberLambdaQueryWrapper = new LambdaQueryWrapper();
        numberLambdaQueryWrapper.eq(AuditTeamNumber::getActiveId,record.getActiveId());
        List<AuditTeamNumber> list = auditTeamNumberService.list(numberLambdaQueryWrapper);
        for (AuditTeamNumber auditTeamNumber :list){
            ids.add(Long.parseLong(auditTeamNumber.getUserId()));
        }
        //获取钉钉id
        List<String> idsByUserIds = sysUserService.getDingIdsByUserIds(ids);
        for (String dingId :idsByUserIds){
            try {
                dingNotifyUtils.OAWorkNotice(dingId,"部门负责人纠正修改完成通知",record.getDeptLeader(),"部门负责人："+record.getDeptLeader()+" 已完成纠正修改，请相关内审员及时处理");
            }catch (Exception e){
                log.error("部门负责人完成整改纠正发送通知给内审员失败:{}",e);
            }
        }
        return ResultUtil.success("已接收整改通知");
    }

    /**
     * 内审员列表措施验证，数据回显
     * @param divideId
     * @return
     */
    @GetMapping("rectificationEcho")
    public Result rectificationEcho(int divideId){
        if (divideId == 0){
            return ResultUtil.error("缺少参数");
        }
        InternalAuditorActive internalAuditorActive = new InternalAuditorActive();
        //查询活动下的人员信息
        List<AuditTeamNumber> userList = Lists.newArrayList();
        LambdaQueryWrapper<DivideEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DivideEntity::getDivideId,divideId);
        List<DivideEntity> list = divideService.list(queryWrapper);
        for (DivideEntity divideEntity :list){
            AuditTeamNumber teamNumber = new AuditTeamNumber();
            teamNumber.setUserId(divideEntity.getAuditorId());
            teamNumber.setName(divideEntity.getAuditorName());
            userList.add(teamNumber);
        }
        internalAuditorActive.setUserList(userList);
        //基本信息
        LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(DivideAuditDetailRel::getDivideId,divideId);
        DivideAuditDetailRel one = divideAuditDetailRelService.getOne(queryWrapper1);
        internalAuditorActive.setDivideAuditDetailRel(one);
        //纠错信息
        LambdaQueryWrapper<DivideRectificationRecord> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(DivideRectificationRecord::getDivideId,divideId);
        DivideRectificationRecord record = divideRectificationRecordService.getOne(queryWrapper2);
        internalAuditorActive.setRectificationRecord(record);
        return ResultUtil.success(internalAuditorActive);
    }

    /**
     * 内审员措施验证
     * @param record
     * @return
     */
    @PostMapping("internalAuditorRectification")
    public Result internalAuditorRectification(@RequestBody DivideRectificationRecord record){
        if (StringUtils.isEmpty(record.getVerificationOfCorrectiveMeasures()) || StringUtils.isEmpty(record.getAuditorId())
                || StringUtils.isEmpty(record.getAuditorName()) || record.getVerificationDate() == null
                || record.getActiveId() == 0 || record.getDivideId() == 0) {
            return ResultUtil.error("缺少参数");
        }
        //更新数据，更新状态为已完成（前提判断管理员是否完成整改）
        String state = qsAuditService.getStateByActiveId(record.getActiveId());
        if ("待总结，已完成".contains(state)) {
            return ResultUtil.error("操作失败，管理员已将内审活动完成整改");
        }
        record.setState("已完成");
        divideRectificationRecordService.updateById(record);
        LambdaUpdateWrapper<DivideAuditDetailRel> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(DivideAuditDetailRel::getDivideId, record.getDivideId());
        updateWrapper.set(DivideAuditDetailRel::getState, "已完成");
        divideAuditDetailRelService.update(null, updateWrapper);
        return ResultUtil.success("措施验证完成");
    }


    /**
     * 内审管理列表
     *
     * @param qsActiveEntity
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("internalAuditManagementList")
    public Result internalAuditManagementList(QsActiveEntity qsActiveEntity, @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        // 效验登录人角色 是否能进行访问：

        // 根据查询条件 进行搜索：
        LambdaQueryWrapper<QsActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(QsActiveEntity::getActiveId, QsActiveEntity::getName, QsActiveEntity::getStartTime, QsActiveEntity::getEndTime, QsActiveEntity::getState, QsActiveEntity::getEditorDate);
        if (qsActiveEntity != null && StringUtils.isNotEmpty(qsActiveEntity.getName())) {
            queryWrapper.like(QsActiveEntity::getName, qsActiveEntity.getName());
        }
        Page<QsActiveEntity> page = new Page<QsActiveEntity>(pageNum, pageSize);
        IPage<QsActiveEntity> pageList = activeService.page(page, queryWrapper);
        return ResultUtil.success(pageList);
    }


    /**
     * 创建内审管理
     *
     * @param qsActiveEntity
     * @return
     */
    @GetMapping("createInternalAuditManagement")
    public Result createInternalAuditManagement(@RequestBody QsActiveEntity qsActiveEntity) {

        return activeService.addQsActiveData(qsActiveEntity);
    }

}
