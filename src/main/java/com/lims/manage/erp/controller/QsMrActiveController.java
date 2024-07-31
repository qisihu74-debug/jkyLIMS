package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.ActiveContentEntity;
import com.lims.manage.erp.entity.ActiveDetailsEntity;
import com.lims.manage.erp.entity.QsMrActiveEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.mapper.DeptDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.LabelValueTeamVo;
import com.lims.manage.erp.vo.QsActiveVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-07-26 11:02
 * @Copyright © 河南交科院
 */
@Slf4j
@RestController
@RequestMapping("/mrActive/")
public class QsMrActiveController {
    @Autowired
    private QsMrActiveService qsMrActiveService;
    @Autowired
    private ActiveContentService activeContentService;
    @Autowired
    private DeptService deptService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;
    @Autowired
    private ActiveDetailsService activeDetailsService;
    @Autowired
    private DeptDao deptDao;

    /**
     * 分页查询管理评审列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("list")
    public Result list(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数");
        }
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByDesc(QsMrActiveEntity::getTime);
        List<QsMrActiveEntity> list = qsMrActiveService.list(queryWrapper);
        PageInfo<QsMrActiveEntity> pageInfo = new PageInfo<>(list);
        //查询内容纲要
        List<Integer> ids = Lists.newArrayList();
        for (QsMrActiveEntity entity : pageInfo.getList()) {
            ids.add(entity.getActiveId());
        }
        if (CollectionUtils.isNotEmpty(ids)){
            LambdaQueryWrapper<ActiveContentEntity> queryWrapper1 = new LambdaQueryWrapper();
            queryWrapper1.in(ActiveContentEntity::getActiveId,ids);
            List<ActiveContentEntity> list1 = activeContentService.list(queryWrapper1);
            if (CollectionUtils.isNotEmpty(list1)){
                for (QsMrActiveEntity entity :pageInfo.getList()){
                    List<ActiveContentEntity> entityList = Lists.newArrayList();
                    for (ActiveContentEntity contentEntity :list1){
                        if (entity.getActiveId().intValue() == contentEntity.getActiveId().intValue()){
                            entityList.add(contentEntity);
                        }
                    }
                }
            }
        }
        return ResultUtil.success(pageInfo);
    }

    /**
     * 创建管理评审
     * @param entity
     * @return
     */
    @Log(title = "创建管理评审", businessType = BusinessType.INSERT)
    @PostMapping("add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody QsMrActiveEntity entity){
        if (StringUtils.isEmpty(entity.getApproverName()) || StringUtils.isEmpty(entity.getReviewPurpose())
                || entity.getReviewTime() == null || StringUtils.isEmpty(entity.getReviewPlace())
                || StringUtils.isEmpty(entity.getReviewHost()) || StringUtils.isEmpty(entity.getParticipants())
                || StringUtils.isEmpty(entity.getEditorName()) || StringUtils.isEmpty(entity.getApproverName())
                || CollectionUtils.isEmpty(entity.getList())){
            return ResultUtil.error("缺少参数");
        }
        LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsMrActiveEntity::getReviewName,entity.getReviewName());
        QsMrActiveEntity one = qsMrActiveService.getOne(queryWrapper);
        if (one != null){
            return ResultUtil.error("此管理评审已存在");
        }
        entity.setTime(new Date(System.currentTimeMillis()));
        qsMrActiveService.save(entity);
        List<ActiveContentEntity> list = entity.getList();
        for (ActiveContentEntity activeContentEntity :list){
            activeContentEntity.setActiveId(entity.getActiveId());
        }
        activeContentService.saveBatch(list);
        //通知所有部门负责人
        Set<Long> ids = deptService.getDingIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<String> idsByUserIds = sysUserService.getDingIdsByUserIds(ids);
            for (String dingId : idsByUserIds) {
                try {
                    dingNotifyUtils.OAWorkNotice(dingId, entity.getReviewName(), entity.getReviewHost(), "管理评审已发布，请相关人员在" + DateUtil.formatDate(entity.getReviewTime()) + "前提交材料");
                } catch (Exception e) {
                    log.error("管理评审创建后钉钉通知失败：" + dingId);
                }
            }
        }
        // 进行批量新增 管理评审部门
        List<LabelValueTeamVo> deptList = deptDao.selectmrActiveDepartmentList();
        List<ActiveDetailsEntity> activeDetailsEntities = new ArrayList<>();
        for (LabelValueTeamVo teamVo : deptList) {
            ActiveDetailsEntity activeDetailsEntity = new ActiveDetailsEntity();
            activeDetailsEntity.setDeptId(teamVo.getValue());
            activeDetailsEntity.setDeptName(teamVo.getLabel());
            activeDetailsEntity.setActiveId(entity.getActiveId());
            activeDetailsEntities.add(activeDetailsEntity);
        }
        activeDetailsService.saveBatch(activeDetailsEntities);

        return ResultUtil.success("添加成功", null);
    }

    /**
     * 删除
     * @param activeId
     * @return
     */
    @Log(title = "删除管理评审", businessType = BusinessType.DELETE)
    @PostMapping("delete")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(Integer activeId){
        if (activeId == null){
            return ResultUtil.error("缺少参数");
        }
        qsMrActiveService.removeById(activeId);
        Map<String,Object> map = new HashMap<>();
        map.put("active_id",activeId);
        activeContentService.removeByMap(map);
        return ResultUtil.success("删除成功");
    }

    /**
     * 变更管理评审
     * @param entity
     * @return
     */
    @Log(title = "变更管理评审", businessType = BusinessType.UPDATE)
    @PostMapping("edit")
    @Transactional(rollbackFor = Exception.class)
    public Result edit(@RequestBody QsMrActiveEntity entity){
        if (StringUtils.isEmpty(entity.getApproverName()) || StringUtils.isEmpty(entity.getReviewPurpose())
                || entity.getReviewTime() == null || StringUtils.isEmpty(entity.getReviewPlace())
                || StringUtils.isEmpty(entity.getReviewHost()) || StringUtils.isEmpty(entity.getParticipants())
                || StringUtils.isEmpty(entity.getEditorName()) || StringUtils.isEmpty(entity.getApproverName())
                || CollectionUtils.isEmpty(entity.getList()) || entity.getActiveId() == null){
            return ResultUtil.error("缺少参数");
        }
        QsMrActiveEntity byId = qsMrActiveService.getById(entity.getActiveId());
        if (byId != null){
            if (!entity.getReviewName().equals(byId.getReviewName())){
                LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(QsMrActiveEntity::getReviewName,entity.getReviewName());
                QsMrActiveEntity one = qsMrActiveService.getOne(queryWrapper);
                if (one != null) {
                    return ResultUtil.error("此管理评审已存在");
                }
            }
        }
        qsMrActiveService.updateById(entity);
        List<ActiveContentEntity> list = entity.getList();
        for (ActiveContentEntity activeContentEntity : list) {
            activeContentEntity.setActiveId(entity.getActiveId());
        }
        //删除旧数据
        Map<String, Object> map = new HashMap<>();
        map.put("active_id", entity.getActiveId());
        activeContentService.removeByMap(map);
        activeContentService.saveBatch(list);
        return ResultUtil.success("变更成功", null);
    }

    /**
     * 管理评审详情
     *
     * @return
     */
    @GetMapping("details")
    public Result details(String activeId) {
        // 获取 管理计划详情
        QsMrActiveEntity byId = qsMrActiveService.getById(activeId);
        if (byId == null) {
            return ResultUtil.error(null);
        }
        // 获取 内容纲要 列表
        LambdaQueryWrapper<ActiveContentEntity> contentQueryWrapper = new LambdaQueryWrapper<>();
        contentQueryWrapper.eq(ActiveContentEntity::getActiveId, activeId);
        List<ActiveContentEntity> activeContentList = activeContentService.list(contentQueryWrapper);
        byId.setList(activeContentList);

        //  部门信息 上传附件详情
        LambdaQueryWrapper<ActiveDetailsEntity> detailsQueryWrapper = new LambdaQueryWrapper<>();
        detailsQueryWrapper.eq(ActiveDetailsEntity::getActiveId, activeId);
        List<ActiveDetailsEntity> activeDetailsList = activeDetailsService.list(detailsQueryWrapper);
        byId.setDepartmentDetails(activeDetailsList);

        return ResultUtil.success(byId);
    }

    /**
     * 管理评审-开始
     *
     * @param qsActiveVo
     * @return
     */
    @PostMapping("startInternalAuditPlan")
    @Transactional(rollbackFor = Exception.class)
    public Result startInternalAuditPlan(@RequestBody QsActiveVo qsActiveVo) {

        // 搜索 内审id1 状态 及 是否存在
        LambdaQueryWrapper<QsMrActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsMrActiveEntity::getActiveId, qsActiveVo.getActiveId());
        QsMrActiveEntity activeDetails = qsMrActiveService.getOne(queryWrapper);
        if (activeDetails == null) {
            return ResultUtil.error("计划不存在");
        }
        if (!activeDetails.getState().equals("待开始")) {
            return ResultUtil.error("状态 为 " + activeDetails.getState());
        }
        activeDetails.setState("进行中");
        qsMrActiveService.updateById(activeDetails);
        return ResultUtil.success("操作成功");
    }

    /**
     * 提交管理评审总结
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("submitManagementReviewDocuments")
    public Result submitInternalAuditDocument(@RequestParam("json") String json, MultipartFile[] file) {
        QsMrActiveEntity qsAuditScheduleRel = JSON.parseObject(json, QsMrActiveEntity.class);
        return qsMrActiveService.submitInternalAuditDocument(qsAuditScheduleRel, file);
    }

    /**
     * 部门负责人 上传材料
     *
     * @param json
     * @param file
     * @return
     */
    @PostMapping("uploadMaterial")
    public Result uploadMaterial(@RequestParam("json") String json, MultipartFile[] file) {
        ActiveDetailsEntity qsAuditScheduleRel = JSON.parseObject(json, ActiveDetailsEntity.class);
        return activeDetailsService.uploadMaterial(qsAuditScheduleRel, file);
    }

    /**
     * 催办
     *
     * @param activeDetailsEntity
     * @return
     */
    @PostMapping("hastenWork")
    public Result hastenWork(@RequestBody ActiveDetailsEntity activeDetailsEntity) {
        return activeDetailsService.hastenWork(activeDetailsEntity);
    }

    /**
     * 获取 管理评审部门
     *
     * @return
     */
    @GetMapping("getActiveDepartmentList")
    public Result getActiveDepartmentList() {
        List<LabelValueTeamVo> deptList = deptDao.selectmrActiveDepartmentList();
        return ResultUtil.success(deptList);
    }

    /**
     * 根据 url 预览数据
     *
     * @param url
     * @param response
     */
    @GetMapping("previewEntrust")
    public void previewEntrust(String url, HttpServletResponse response) {

        String[] arrays = url.split("/");
        // 读取文件后缀：
        try {
            String nameSuffix = arrays[arrays.length - 1].split("\\.")[1];
            MinioClient client = MinIoUtil.minioClient;
            InputStream inputStream = client.getObject(arrays[arrays.length - 2], arrays[arrays.length - 1]);
            OutputStream outputStream = response.getOutputStream();
            switch (nameSuffix) {
                case "pdf":
                    int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
                    inputStream.close();
                    outputStream.close();
                    break;
                case "xlsx":
                    //相应pdf
                    ByteArrayOutputStream b1 = PDFHelper3.excel2pdf2(inputStream, arrays[arrays.length - 1]);
                    InputStream inputStreamxls = FileAndFolderUtil.parseOut(b1);
                    int i1 = IOUtils.copy(inputStreamxls, outputStream);   // copy流数据,i为字节数
                    inputStream.close();
                    outputStream.close();
                    inputStreamxls.close();
                    b1.close();
                    break;
                case "docx":
                    XWPFDocument document = new XWPFDocument(inputStream);
                    //相应pdf
                    ByteArrayOutputStream b2 = AsposeUtil.word2pdf4(document);
                    InputStream inputStreamdoc = FileAndFolderUtil.parseOut(b2);
                    ServletOutputStream outputStreamdoc = response.getOutputStream();
                    int i2 = IOUtils.copy(inputStreamdoc, outputStreamdoc);   // copy流数据,i为字节数
                    inputStream.close();
                    outputStream.close();
                    b2.close();
                    inputStreamdoc.close();
                    outputStreamdoc.close();
                    break;
                default:
                    int in = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
                    inputStream.close();
                    outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最高管理者 账号
     *
     * @return
     */
    @GetMapping("getTopManagement")
    public Result getTopManagement() {

        return sysUserService.getTopManagement();
    }

}
