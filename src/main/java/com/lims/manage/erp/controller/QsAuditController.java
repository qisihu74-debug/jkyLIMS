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
import com.lims.manage.erp.entity.DivideAuditDetail;
import com.lims.manage.erp.entity.DivideAuditDetailRel;
import com.lims.manage.erp.entity.DivideEntity;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import com.lims.manage.erp.entity.InternalAuditorActive;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.job.DingUserJob;
import com.lims.manage.erp.mapper.AduditBaseDataDao;
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
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.QsActiveVo;
import com.lims.manage.erp.vo.TaskDetailInfoVo;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private QsAuditScheduleRelService qsAuditScheduleRelService;
    @Autowired
    private AduditBaseDataDao aduditBaseDataDao;

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
            active.setAuditTimeCycle(DateUtil.formatDate(active.getStartTime())+"~"+DateUtil.formatDate(active.getEndTime()));
            ids.add(active.getDivideId());
        }
        //查询活动下的人员信息
        if (CollectionUtils.isNotEmpty(ids)){
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
        //受审部门，内审员
        LambdaQueryWrapper<DivideEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DivideEntity::getDivideId,divideId);
        List<DivideEntity> entityList = divideService.list(lambdaQueryWrapper);
        StringBuilder stringBuilder = new StringBuilder();
        for (DivideEntity divideEntity :entityList){
            stringBuilder.append(divideEntity.getAuditorName());
            stringBuilder.append(",");
        }
        if (one == null){
            one = new DivideAuditDetailRel();
        }
        one.setDeptName(entityList.get(0).getDeptName());
        one.setUserName(stringBuilder.toString().substring(0,stringBuilder.length()-1));
        one.setList(list);
        return ResultUtil.success(one);
    }

    /**
     * 开始检查前获取基础数据
     * @param pageNum
     * @param pageSize
     * @param type
     * @param dir
     * @param content
     * @param method
     * @param subject
     * @return
     */
    @GetMapping("getCheckBaseDataList")
    public Result getCheckBaseDataList(Integer pageNum,Integer pageSize,String type,
                                       String dir,String content,String method,String subject,Integer divideId){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        if (StringUtils.isEmpty(type) || divideId == null){
            return ResultUtil.error("缺少参数");
        }
        if (!"CMA,CNAS".contains(type)){
            return ResultUtil.error("类型参数不合法");
        }
        PageInfo<AduditBaseData> pageInfo = qsAuditService.getCheckBaseDataList(pageNum,pageSize,type,dir,content,method,subject,divideId);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 检查结果获取
     *
     * @param divideId
     * @return
     */
    @GetMapping("getCheckResult")
    public Result getCheckResult(Integer divideId) {
        if (divideId == null) {
            return ResultUtil.error("缺少参数");
        }
        Map<String, String> map = new HashMap<>();
        LambdaQueryWrapper<DivideAuditDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DivideAuditDetail::getDivideId, divideId);
        queryWrapper.last("\tand( opinion = \"不符合\"\n" +
                "\tor opinion = \"基本符合\"\n" +
                "\tor opinion = \"缺此项\"\n" +
                "\tor opinion = \"Y`\"\n" +
                "\tor opinion = \"N\"\n" +
                "\tor opinion = \"N/A\")");

        List<DivideAuditDetail> list = divideAuditDetailService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            map.put("result", "需整改");
            return ResultUtil.success(map);
        } else {
            map.put("result", "合格");
            return ResultUtil.success(map);
        }
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
                ||detailRel.getList().get(0).getDirectoryId() == 0){
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
            LambdaQueryWrapper<DivideAuditDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DivideAuditDetail::getDivideId,detailRel.getList().get(0).getDivideId());
            queryWrapper.eq(DivideAuditDetail::getDirectoryId,detailRel.getList().get(0).getDirectoryId());
            DivideAuditDetail one = divideAuditDetailService.getOne(queryWrapper);
            if (one == null){
                divideAuditDetailService.saveBatch(detailRel.getList());
            }else {
                LambdaUpdateWrapper<DivideAuditDetail> updateWrapper = new LambdaUpdateWrapper();
                updateWrapper.eq(DivideAuditDetail::getDivideId,detailRel.getList().get(0).getDivideId());
                updateWrapper.eq(DivideAuditDetail::getDirectoryId,detailRel.getList().get(0).getDirectoryId());
                updateWrapper.set(DivideAuditDetail::getFindings,detailRel.getList().get(0).getFindings());
                updateWrapper.set(DivideAuditDetail::getOpinion,detailRel.getList().get(0).getOpinion());
                updateWrapper.set(DivideAuditDetail::getRecord,detailRel.getList().get(0).getRecord());
                divideAuditDetailService.update(updateWrapper);
            }
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
        if (detailRel.getActiveId() == 0 || detailRel.getDivideId() == 0){
            return ResultUtil.error("缺少必要参数");
        }
        //判断
        LambdaQueryWrapper<DivideAuditDetail> auditDetailLambdaQueryWrapper = new LambdaQueryWrapper();
        auditDetailLambdaQueryWrapper.eq(DivideAuditDetail::getDivideId,detailRel.getDivideId());
        List<DivideAuditDetail> details = divideAuditDetailService.list(auditDetailLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(details)){
            return ResultUtil.error("检查项还未操作，请先完成检查");
        }
        //判断是否合格
        LambdaQueryWrapper<DivideAuditDetail> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(DivideAuditDetail::getDivideId,detailRel.getDivideId());
        queryWrapper1.last("\tand( opinion = \"不符合\"\n" +
                "\tor opinion = \"基本符合\"\n" +
                "\tor opinion = \"缺此项\"\n" +
                "\tor opinion = \"Y`\"\n" +
                "\tor opinion = \"N\"\n" +
                "\tor opinion = \"N/A\")");
        List<DivideAuditDetail> list = divideAuditDetailService.list(queryWrapper1);
        if (CollectionUtils.isNotEmpty(list)){
            if (StringUtils.isEmpty(detailRel.getNonComplianceDegree()) ||StringUtils.isEmpty(detailRel.getNonConformance())
                    || StringUtils.isEmpty(detailRel.getNonConformanceProgram()) || StringUtils.isEmpty(detailRel.getSubstandard())
                    ||StringUtils.isEmpty(detailRel.getCheckResult())){
                return ResultUtil.error("缺少检查结果相关信息");
            }
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
            boolean flag = true;
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
                divideRectificationRecordService.saveOrUpdate(record);
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

        return ResultUtil.success(pageInfo);
    }

    /**
     * 获取整改详情
     * @param divideId
     * @return
     */
    @GetMapping("getDetail")
    public Result getDetail(int divideId){
        if (divideId == 0){
            return ResultUtil.error("缺少参数");
        }else {
            InternalAuditorActive internalAuditorActive = new InternalAuditorActive();
            //处理内审员
            List<Integer> fgIds = Lists.newArrayList();
            fgIds.add(divideId);
            //查询活动下的人员信息
            LambdaQueryWrapper<DivideEntity> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(DivideEntity::getDivideId,fgIds);
            List<DivideEntity> list = divideService.list(queryWrapper);
            List<AuditTeamNumber> userList = Lists.newArrayList();
            for (DivideEntity divideEntity :list){
                AuditTeamNumber teamNumber = new AuditTeamNumber();
                teamNumber.setUserId(divideEntity.getAuditorId());
                teamNumber.setName(divideEntity.getAuditorName());
                userList.add(teamNumber);
            }
            internalAuditorActive.setUserList(userList);
            //查询基本信息qs_audit_divide_rel
            LambdaQueryWrapper<DivideRectificationRecord> queryWrapper1 = new LambdaQueryWrapper();
            queryWrapper1.in(DivideRectificationRecord::getDivideId,fgIds);
            List<DivideRectificationRecord> records = divideRectificationRecordService.list(queryWrapper1);
            for (DivideRectificationRecord record :records){
                internalAuditorActive.setRectificationRecord(record);
            }
            //基本信息
            LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.in(DivideAuditDetailRel::getDivideId,fgIds);
            List<DivideAuditDetailRel> auditDetailRels = divideAuditDetailRelService.list(queryWrapper2);
            for (DivideAuditDetailRel detailRel :auditDetailRels){
                internalAuditorActive.setDivideAuditDetailRel(detailRel);
            }
            return ResultUtil.success(internalAuditorActive);
        }
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
     *
     * @param json
     * @param file
     * @return
     */
    @Log(title = "部门负责人完成纠正", businessType = BusinessType.INSERT)
    @PostMapping("completeCorrection")
    public Result completeCorrection(@RequestParam("json") String json, MultipartFile[] file) {
        DivideRectificationRecord record = JSON.parseObject(json, DivideRectificationRecord.class);
        if (StringUtils.isEmpty(record.getCorrectionCompletionStatus()) || record.getActualFinishingDate() == null
                || StringUtils.isEmpty(record.getDeptLeader())) {
            return ResultUtil.error("缺少检查结果相关信息");
        }
        if (file == null) {
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
        for (MultipartFile multipartFile : file) {
            if (file != null) {
                String filename = multipartFile.getOriginalFilename();
                String[] split = filename.split("\\.");
                if (!"pdf、png、jpg、jpeg".contains(split[split.length - 1])) {
                    return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
                }
                //上传附件
                String upload = MinIoUtil.upload(BucketsConst.internal_audit, multipartFile, multipartFile.getOriginalFilename());
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String uploadUrl = upload.substring(0, upload.indexOf("?"));
                    stringBuilder.append(uploadUrl);
                    stringBuilder.append(",");
                }
            }
        }
        if (stringBuilder.length()>=1){
            record.setUrl(stringBuilder.toString().substring(0,stringBuilder.length()-1));
        }
        //更新状态为等待纠正
        record.setState("等待验证");
        divideRectificationRecordService.updateById(record);
        //内审员措施验证
        LambdaUpdateWrapper<DivideAuditDetailRel> lambdaUpdateWrapper = new LambdaUpdateWrapper();
        lambdaUpdateWrapper.eq(DivideAuditDetailRel::getDivideId,record.getDivideId());
        lambdaUpdateWrapper.set(DivideAuditDetailRel::getState,"措施验证");
        divideAuditDetailRelService.update(null,lambdaUpdateWrapper);
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
     * @param file
     * @return
     */
    @Log(title = "部门负责人修改整改休息", businessType = BusinessType.INSERT)
    @PostMapping("editCorrection")
    public Result editCorrection(@RequestParam("json") String json, MultipartFile[] file) {
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
        if (file != null){
            String recordUrl = record.getUrl();
            //编辑后的url
            String[] split1 = recordUrl.split(",");
            List<String> newist = Arrays.asList(split1);
            //删除旧文件
            DivideRectificationRecord byId = divideRectificationRecordService.getById(record.getDivideId());
            if (StringUtils.isNotEmpty(byId.getUrl())){
                //原url
                String[] split = byId.getUrl().split(",");
                List<String> oldList = Arrays.asList(split);
                //编辑后少了做删除
                List<String> stringList = oldList.stream()
                        .filter(element -> !newist.contains(element))
                        .collect(Collectors.toList());
                //删除取消的文件
                for (String url:stringList){
                    String[] strings = url.split("\\/");
                    String bluckName = strings[3];
                    String fileName = strings[4];
                    MinIoUtil.deleteFile(bluckName,fileName);
                }
                //处理url
                List<String> collect = oldList.stream()
                        .filter(newist::contains)
                        .collect(Collectors.toList());
                for (String s:collect){
                    stringBuilder.append(s);
                    stringBuilder.append(",");
                }

            }
            for (MultipartFile file1: file){
                if (file1 != null) {
                    String filename = file1.getOriginalFilename();
                    String[] split = filename.split("\\.");
                    if (!"pdf、png、jpg、jpeg".equals(split[split.length - 1])) {
                        return ResultUtil.error("文件类型不正确，请上传正确的文件类型");
                    }
                    //上传附件
                    String upload = MinIoUtil.upload(BucketsConst.internal_audit, file1, file1.getOriginalFilename());
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
        // 内审排序-逆序
        queryWrapper.orderByDesc(QsActiveEntity::getActiveId);
        Page<QsActiveEntity> page = new Page<QsActiveEntity>(pageNum, pageSize);
        IPage<QsActiveEntity> pageList = activeService.page(page, queryWrapper);
        if (CollectionUtils.isNotEmpty(pageList.getRecords())) {
            for (QsActiveEntity qsActiveEntity1 : pageList.getRecords()) {
                // 进行 审核周期 拆分: auditTimeCycle
                if (qsActiveEntity1.getStartTime() != null && qsActiveEntity1.getEndTime() != null) {
                    // Date 转 "2024-07-16" 格式
                    String startTime = DateUtil.formatDate(qsActiveEntity1.getStartTime());
                    String endTime = DateUtil.formatDate(qsActiveEntity1.getEndTime());
                    qsActiveEntity1.setAuditTimeCycle(startTime + "~" + endTime);
                }
            }
        }
        return ResultUtil.success(pageList);
    }


    /**
     * 创建内审管理
     *
     * @param qsActiveEntity
     * @return
     */
    @PostMapping("createInternalAuditManagement")
    public Result createInternalAuditManagement(@RequestBody QsActiveEntity qsActiveEntity) {

        return activeService.addQsActiveData(qsActiveEntity);
    }

    /**
     * 返回内审基础信息
     *
     * @return
     */
    @GetMapping("getInternalAuditBasics")
    public Result getInternalAuditBasics() {

        return activeService.getInternalAuditBasics();
    }

    /**
     * 查询详情内审活动
     *
     * @param activeId
     * @return
     */
    @GetMapping("queryDetailsQsActiveData")
    public Result queryDetailsQsActiveData(String activeId) {

        return activeService.queryDetailsQsActiveData(activeId);
    }


    /**
     * 查询审核组长集合
     *
     * @return
     */
    @GetMapping("getAuditTeamLeaderList")
    public Result getAuditTeamLeaderList() {

        return sysUserService.getAuditTeamLeaderList();
    }

    /**
     * 查询审核组员集合
     *
     * @return
     */
    @GetMapping("getCrewAssemblyList")
    public Result getCrewAssemblyList() {

        return sysUserService.getCrewAssemblyList();
    }

    /**
     * 编制人集合
     *
     * @return
     */
    @GetMapping("getAssemblerPool")
    public Result getAssemblerPool() {

        return sysUserService.getAssemblerPool();
    }

    /**
     * 受审部门 集合
     *
     * @return
     */
    @GetMapping("getTrialDepartmentList")
    public Result getTrialDepartmentList() {

        return deptService.getTrialDepartmentList();
    }

    /**
     * 更新内审管理
     *
     * @param qsActiveEntity
     * @return
     */
    @PostMapping("updateQsActiveData")
    public Result updateQsActiveData(@RequestBody QsActiveEntity qsActiveEntity) {

        return activeService.updateQsActiveData(qsActiveEntity);
    }

    /**
     * 内审管理-开始
     *
     * @param qsActiveVo
     * @return
     */
    @PostMapping("startInternalAuditPlan")
    public Result startInternalAuditPlan(@RequestBody QsActiveVo qsActiveVo) {

        return activeService.startInternalAuditPlan(qsActiveVo);
    }

    /**
     * 发起会议：首次会议、末次会议
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("initiateAMeeting")
    public Result initiateAMeeting(@RequestParam("json") String json, MultipartFile[] file) {
        QsAuditScheduleRelEntity qsAuditScheduleRel = JSON.parseObject(json, QsAuditScheduleRelEntity.class);
        return activeService.initiateAMeeting(qsAuditScheduleRel, file);
    }

    /**
     * 获取 会议信息
     *
     * @param activeId
     * @param type
     * @return
     */
    @GetMapping("getMeetingList")
    public Result getMeetingList(String activeId, String type) {
        LambdaQueryWrapper<QsAuditScheduleRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsAuditScheduleRelEntity::getActiveId, activeId);
        if (type.equals("1")) {
            queryWrapper.eq(QsAuditScheduleRelEntity::getMeetingType, "首次会议");
        } else if (type.equals("2")) {
            queryWrapper.eq(QsAuditScheduleRelEntity::getMeetingType, "末次会议");
        }
        queryWrapper.last("limit 1");
        QsAuditScheduleRelEntity qsAuditScheduleRelEntity = qsAuditScheduleRelService.getOne(queryWrapper);
        if (qsAuditScheduleRelEntity != null) {
            // 处理 多组信息：
            List<AuditTeamNumber> auditTeamList = new ArrayList<>();
            if (StringUtils.isNotEmpty(qsAuditScheduleRelEntity.getAttendance())) {
                String[] arrays = qsAuditScheduleRelEntity.getAttendance().split(",");
                for (int i = 0; i < arrays.length; i++) {
                    String[] userinfo = arrays[i].split("&");
                    AuditTeamNumber auditTeamNumber = new AuditTeamNumber();
                    auditTeamNumber.setUserId(userinfo[0]);
                    auditTeamNumber.setName(userinfo[1]);
                    auditTeamList.add(auditTeamNumber);
                }
            }
            qsAuditScheduleRelEntity.setAuditTeamList(auditTeamList);

            if (qsAuditScheduleRelEntity.getStartTime() != null && qsAuditScheduleRelEntity.getEndTime() != null) {
                // Date 转 "2024-07-16 23:59:59" 格式
                String startTime = DateUtil.formatDate(qsAuditScheduleRelEntity.getStartTime());
                String endTime = DateUtil.formatDate(qsAuditScheduleRelEntity.getEndTime());
                qsAuditScheduleRelEntity.setMeetingCycle(startTime + "~" + endTime);
            }

            return ResultUtil.success(qsAuditScheduleRelEntity);
        }
        return ResultUtil.success(new QsAuditScheduleRelEntity());
    }

    /**
     * 会议：查询主持人集合、记录人集合、出席人集合
     *
     * @param type
     * @return
     */
    @GetMapping("getConferenceAssembly")
    public Result getHostAssembly(String type) {
        if (StringUtils.isEmpty(type)) {
            return ResultUtil.error("缺少必填参数");
        }
        if (type.equals("1")) {
            return sysUserService.getAuditTeamLeaderList();
        } else {
            return sysUserService.getCrewAssemblyList();
        }
    }


    /**
     * 提交内审总结附件
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("submitInternalAuditDocument")
    public Result submitInternalAuditDocument(@RequestParam("json") String json, MultipartFile[] file) {
        QsAuditScheduleRelEntity qsAuditScheduleRel = JSON.parseObject(json, QsAuditScheduleRelEntity.class);
//        return activeService.initiateAMeeting(qsAuditScheduleRel, file);
        return activeService.submitInternalAuditDocument(qsAuditScheduleRel, file);
    }

    @Autowired
    private DingUserJob job;
    @GetMapping("test")
    public void test(){
        job.sync();
    }
    /**
     * 获取 会议信息 -- 签到表
     *
     * @param type
     * @return
     */
    @GetMapping("getConferenceUrl")
    public Result getConferenceUrl(String type) {
        return ResultUtil.success("https://minio.lims.design/internal-audit/008签到表.doc");
    }

    /**
     * 内审检查 根据内审ID 展示 详情
     *
     * @param activeId
     * @return
     */
    @GetMapping("getInternalAuditInspectionDetails")
    public Result getInternalAuditInspectionDetails(String activeId) {

        return activeService.getInternalAuditInspectionDetails(activeId, 1);
    }

    /**
     * 内审检查 根据内审ID 展示详情 - 包括 整改结果
     *
     * @param activeId
     * @return
     */
    @GetMapping("getDetailsOfRectificationResults")
    public Result getDetailsOfRectificationResults(String activeId) {

        return activeService.getInternalAuditInspectionDetails(activeId, 2);
    }

    /**
     * 根据 分组id 进行下载 检测记录表
     *
     * @param divideId
     * @param response
     */
    @GetMapping("downloadEntrust")
    public void downloadEntrust(String divideId, HttpServletResponse response) {
        try {
            MinioClient client = MinIoUtil.minioClient;
            // 进行 测试数据
            // 获取 分组Id 填报的 检查记录信息
            List<AduditBaseData> aduditBaseDataList = aduditBaseDataDao.selectmergingList(Integer.valueOf(divideId));
            String objectName = "";
            if (aduditBaseDataList.get(0).getType().equals("CMA")) {
                objectName = "037-RBT214-2017内部审核检查表-2019.10.docx";
            } else {
                objectName = "038-核查表 CNAS-CL01（认可准则）-2019.10.doc";
            }
            InputStream object = client.getObject("internal-audit", objectName);
            XWPFDocument doc = null;
            doc = activeService.downloadEntrust(divideId, object, aduditBaseDataList);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            // 获取 分组列表
            LambdaQueryWrapper<AduditBaseData> queryWrapper = new LambdaQueryWrapper<>();
            response.setHeader("Content-Disposition", "attachment;fileName=" + "检测记录表" + ".docx");
            OutputStream outputStream = response.getOutputStream();
            doc.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据 分组id 进行下载 检测记录表
     *
     * @param divideId
     * @param response
     */
    @GetMapping("previewEntrust")
    public void previewEntrust(String divideId, HttpServletResponse response) {
        try {
            MinioClient client = MinIoUtil.minioClient;
            // 进行 测试数据
            // 获取 分组Id 填报的 检查记录信息
            List<AduditBaseData> aduditBaseDataList = aduditBaseDataDao.selectmergingList(Integer.valueOf(divideId));
            String objectName = "";
            if (aduditBaseDataList.get(0).getType().equals("CMA")) {
                objectName = "037-RBT214-2017内部审核检查表-2019.10.docx";
            } else {
                objectName = "038-核查表 CNAS-CL01（认可准则）-2019.10.doc";
            }
            InputStream object = client.getObject("internal-audit", objectName);
            XWPFDocument doc = null;
            doc = activeService.downloadEntrust(divideId, object, aduditBaseDataList);
            //相应pdf
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(doc);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 不符合项聚合视图 — 跨内审活动汇总整改记录及状态
     */
    @GetMapping("nonconformityList")
    public Result nonconformityList(@RequestParam(required = false) String state,
                                    @RequestParam(required = false) String deptName) {
        return ResultUtil.success("不符合项列表", divideRectificationRecordService.nonconformityList(state, deptName));
    }
}
