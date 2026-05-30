package com.lims.manage.erp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.ManageReviewInformationEntity;
import com.lims.manage.erp.entity.ManageReviewPlanEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.ManageReviewInformationEntityMapper;
import com.lims.manage.erp.mapper.ManageReviewPlanEntityMapper;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ManagementReviewService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/1/2 16:59
 */
@Service("managementReviewService")
public class ManagementReviewServiceImpl extends ServiceImpl<ManageReviewPlanEntityMapper, ManageReviewPlanEntity> implements ManagementReviewService {

    @Resource
    private ManageReviewPlanEntityMapper manageReviewPlanEntityMapper;
    @Resource
    private ManageReviewInformationEntityMapper manageReviewInformationEntityMapper;
    @Resource
    private TaskMapper taskMapper;
    @Autowired
    private SysUserDao sysUserDao;
//    @Autowired
//    private DingNotifyUtils dingNotifyUtils;

    /**
     * 管理评审 列表展示
     *
     * @param manageReviewPlanEntity
     * @return
     */
    @Override
    public Result getList(ManageReviewPlanEntity manageReviewPlanEntity) {

        if (manageReviewPlanEntity.getPageNum() == null || manageReviewPlanEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        PageHelper.clearPage();
        PageHelper.startPage(manageReviewPlanEntity.getPageNum(), manageReviewPlanEntity.getPageSize());
        LambdaQueryWrapper<ManageReviewPlanEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 登录人信息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 判断当前登录人 是否是 体系管理员
        List<SysUserEntity> userList = sysUserDao.systemManagementList();
        Integer sign = 1;
        for (SysUserEntity sysUserEntity : userList) {
            if (sysUserEntity.getUserId().equals(userInfo.getUserId())) {
                // sign = 0 表示当前用户为体系管理员
                sign = 0;
            }
        }

        // 当前登录用户 不是体系管理员
        if (sign == 1) {
            // or查询列表 当前登录人 是否包含 创建人、主持人、参与人员
            // 创建人
            lambdaQueryWrapper.or().like(ManageReviewPlanEntity::getPlanCreator, userInfo.getName() + "&" + userInfo.getUserId());
            // 主持人
            lambdaQueryWrapper.or().like(ManageReviewPlanEntity::getReviewHost, userInfo.getName() + "&" + userInfo.getUserId());
            // 参与人员
            lambdaQueryWrapper.or().like(ManageReviewPlanEntity::getParticipant, userInfo.getName() + "&" + userInfo.getUserId());
        }
        // 模糊查询 管理评审 创建人信息
        if (org.apache.commons.lang.StringUtils.isNotEmpty(manageReviewPlanEntity.getPlanCreator())) {
            lambdaQueryWrapper.like(ManageReviewPlanEntity::getPlanCreator, manageReviewPlanEntity.getPlanCreator());
        }
        lambdaQueryWrapper.eq(ManageReviewPlanEntity::getDelFlag, 0);
        lambdaQueryWrapper.orderByDesc(ManageReviewPlanEntity::getCreateTime);

        List<ManageReviewPlanEntity> list = manageReviewPlanEntityMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            // 截取数据：当前登录人 name&userId 跟创建匹配的话可操作。
            for (ManageReviewPlanEntity manageReviewPlanEntity1 : list) {
                // 当前登录人 是体系管理员 sign=0、否则 sign=1。
                if (sign == 0) {
                    manageReviewPlanEntity1.setSign(0);
                } else {
                    manageReviewPlanEntity1.setSign(1);
                }
            }
        }
        PageInfo<ManageReviewPlanEntity> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result addManageReviewPlanEntity(ManageReviewPlanEntity manageReviewPlanEntity, MultipartFile[] file) {
        //TODO:1月12日  查询基础表信息 - 管理评审：人员职位信息
        List<TestInitDataEntity> personnelPosition = taskMapper.selectEntrustBasis(32);
        //TODO:1月12日  查询基础表信息 - 管理评审：文件后缀效验
        List<TestInitDataEntity> suffixSet = taskMapper.selectEntrustBasis(33);
        // 效验数据 评审目的
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewPurpose())) {
            return ResultUtil.error("评审目的不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewStartTime()) || StringUtils.isEmpty(manageReviewPlanEntity.getReviewEndTime())) {
            return ResultUtil.error("评审时间不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewHost())) {
            return ResultUtil.error("评审主持不能为空");
        }
        // 参加人员不能为空
        if (StringUtils.isEmpty(manageReviewPlanEntity.getParticipant())) {
            return ResultUtil.error("参加人员不能为空");
        }
        // 文件名与后缀名参与效验
        String msg = validationFileSuffix(file, suffixSet);
        if (msg != null) {
            return ResultUtil.error(msg);
        }
        // 创建人：
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        // 判断当前登录人 是否是 体系管理员
        List<SysUserEntity> userList = sysUserDao.systemManagementList();
        Integer sign = 1;
        for (SysUserEntity sysUserEntity : userList) {
            if (sysUserEntity.getUserId().equals(userInfo.getUserId())) {
                // sign = 0 表示当前用户为体系管理员
                sign = 0;
            }
        }
        if (sign == 1) {
            return ResultUtil.error(userInfo.getName() + "不是体系管理员");
        }
        // 上述为 ↑↑↑↑↑ 验证信息 ↑↑↑↑↑

        //创建人信息
        manageReviewPlanEntity.setPlanCreator(userInfo.getName() + "&" + userInfo.getUserId());
        manageReviewPlanEntity.setDelFlag(0);
        // 计划创建时间
        manageReviewPlanEntity.setPlanCreationTime(new Date());
        manageReviewPlanEntity.setCreateTime(new Date());
        manageReviewPlanEntity.setId(null);
        // 计划新建时：
        manageReviewPlanEntity.setReviewReportStatus("待上传");
        // 创建人 新增附件 - 单个
        if (file != null && file.length >= 1) {
            for (MultipartFile multipartFile : file) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(multipartFile.getOriginalFilename())) {
                    Long fileCode = GenID.getID();
                    String name = multipartFile.getOriginalFilename();
                    String[] strings = name.split("\\.");
                    String upload = MinIoUtil.upload(BucketsConst.manage_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                    // 截取 \\? 前数据
                    String filePath = upload.split("\\?")[0];
                    manageReviewPlanEntity.setFileUrl(filePath);
                    manageReviewPlanEntity.setOriginalFileName(name);
                }
            }
        }
        // 新增： 创建计划 并返回id
        manageReviewPlanEntityMapper.insertSelective(manageReviewPlanEntity);
        // 创建人：职位信息，新增附件
        dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(0).getName(), null, userInfo);
        // 参加人员: 职位信息，数据截取后新增
        String[] participants = manageReviewPlanEntity.getParticipant().split(",");
        for (int i = 0; i < participants.length; i++) {
            manageReviewPlanEntity.setPlanCreator(participants[i]);
            // 参与人：新增附件
            dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(2).getName(), null, userInfo);
        }
        // 主持人：职位信息，新增附件
        manageReviewPlanEntity.setPlanCreator(manageReviewPlanEntity.getReviewHost());
        dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(1).getName(), null, userInfo);
        return ResultUtil.success("创建成功");
    }

    /**
     * 管理评审:钉钉 计划通知
     *
     * @param planCreator
     * @param publisher
     */
    void methodDingTalkNotification(String planCreator, String publisher) {

        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 通知时间
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String nowString = simpleDateFormat.format(now);
        // 当前年份
        SimpleDateFormat yyyyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String yyyyString = yyyyFormat.format(now);
        // 查询钉钉id
        String dingId = "";
        try {
            // 获取 任务单下检测人信息 userId
            LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserEntity::getUserId, planCreator.split("&")[1]);
            SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
            dingId = userDetails.getDingUserId();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(nowString + "评审计划通知");
            StringBuffer contextBuffer = new StringBuffer();
            contextBuffer.append(yyyyString + " 管理评审计划已经发布：" + "请登录公司系统提交相关资料，特此通知！");
            dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), publisher, contextBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 动态处理：新增或者更新根据计划信息 职位、附件。
     *
     * @param manageReviewPlanEntity 计划信息
     * @param duties                 职务
     * @param file                   附件
     * @param userInfo               当前登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void dynamicHandlingFileAddORUpdate(ManageReviewPlanEntity manageReviewPlanEntity, String duties, MultipartFile[] file, SysUserEntity userInfo) {
        // 创建人信息新增
        ManageReviewInformationEntity informationEntity = new ManageReviewInformationEntity();
        // 创建人
        informationEntity.setPlanCreator(manageReviewPlanEntity.getPlanCreator());
        // 职务
        informationEntity.setDuties(duties);
        // 创建时间
        informationEntity.setCreateTime(new Date());
        // 状态
        informationEntity.setDelFlag(0);
        informationEntity.setPid(manageReviewPlanEntity.getId());
        // 附件URL链接
        StringBuffer fileNameBuffer = new StringBuffer();
        // 附件原始文件名称
        StringBuffer originalFileNameBuffer = new StringBuffer();
        //附件存在上传附件到服务器
        if (file != null && file.length >= 1) {
            for (MultipartFile multipartFile : file) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(multipartFile.getOriginalFilename())) {
                    Long fileCode = GenID.getID();
                    String name = multipartFile.getOriginalFilename();
                    String[] strings = name.split("\\.");
                    String upload = MinIoUtil.upload(BucketsConst.manage_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                    // 截取 \\? 前数据
                    String filePath = upload.split("\\?")[0];
                    fileNameBuffer.append(filePath);
                    fileNameBuffer.append(",");
                    originalFileNameBuffer.append(name);
                    originalFileNameBuffer.append(",");
                }
            }
        }
        // 补充文件附件
        if (fileNameBuffer.length() >= 1) {
            informationEntity.setFileUrl(fileNameBuffer.deleteCharAt(fileNameBuffer.length() - 1).toString());
        }
        if (originalFileNameBuffer.length() >= 1) {
            informationEntity.setOriginalFileName(originalFileNameBuffer.deleteCharAt(originalFileNameBuffer.length() - 1).toString());
        }
        // 通过查询条件 判断是 存在 则update，不存在 则add
        LambdaQueryWrapper<ManageReviewInformationEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 创建人信息
        queryWrapper.eq(ManageReviewInformationEntity::getPlanCreator, informationEntity.getPlanCreator());
        // 职位
        queryWrapper.eq(ManageReviewInformationEntity::getDuties, duties);
        // pid
        queryWrapper.eq(ManageReviewInformationEntity::getPid, informationEntity.getPid());
        // delFlag = 0
        queryWrapper.eq(ManageReviewInformationEntity::getDelFlag, 0);
        // 查询 创建人、职位、pid
        ManageReviewInformationEntity oldData = manageReviewInformationEntityMapper.selectOne(queryWrapper);
        if (oldData == null) {
            // 钉钉通知 ：
            methodDingTalkNotification(manageReviewPlanEntity.getPlanCreator(), userInfo.getName());
            // 不存在的话 则add
            manageReviewInformationEntityMapper.insert(informationEntity);
        } else {
            // 更新数据
            informationEntity.setId(oldData.getId());
            // 新附件不等于null && 旧附件不为空 则删除旧附件：始终保持最新一组数据
            if (!StringUtils.isEmpty(informationEntity.getFileUrl()) && !StringUtils.isEmpty(oldData.getFileUrl())) {
                String[] strings = oldData.getFileUrl().split(",");
                for (int i = 0; i < strings.length; i++) {
                    String[] urls = strings[i].split("/");
                    String url = urls[urls.length - 1];
                    // 删除附件信息
                    MinIoUtil.deleteFile(BucketsConst.manage_audit, url);
                }
            }
            manageReviewInformationEntityMapper.updateByPrimaryKeySelective(informationEntity);
        }
    }

    /**
     * 动态处理：删除附件
     *
     * @param manageReviewPlanEntity 计划信息
     * @param duties                 职务
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperation(ManageReviewPlanEntity manageReviewPlanEntity, String duties) {
        // 创建人信息新增
        ManageReviewInformationEntity informationEntity = new ManageReviewInformationEntity();
        // 创建人
        informationEntity.setPlanCreator(manageReviewPlanEntity.getPlanCreator());
        // 职务
        informationEntity.setDuties(duties);
        // 更新时间
        informationEntity.setUpdateTime(new Date());
        // 状态
//        informationEntity.setDelFlag(0);
        informationEntity.setPid(manageReviewPlanEntity.getId());
        // 通过查询条件 判断是 存在 则update，不存在 则 结束
        LambdaQueryWrapper<ManageReviewInformationEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 创建人信息
        queryWrapper.eq(ManageReviewInformationEntity::getPlanCreator, manageReviewPlanEntity.getPlanCreator());
        // 职位
        queryWrapper.eq(ManageReviewInformationEntity::getDuties, duties);
        // pid
        queryWrapper.eq(ManageReviewInformationEntity::getPid, informationEntity.getPid());
        // delFlag = 0
        queryWrapper.eq(ManageReviewInformationEntity::getDelFlag, 0);
        // 查询 创建人、职位、pid
        ManageReviewInformationEntity oldData = manageReviewInformationEntityMapper.selectOne(queryWrapper);
        if (oldData != null) {
            // 附件删除
            if (!StringUtils.isEmpty(oldData.getFileUrl())) {
                String[] strings = oldData.getFileUrl().split(",");
                for (int i = 0; i < strings.length; i++) {
                    String[] urls = strings[i].split("/");
                    String url = urls[urls.length - 1];
                    // 删除附件信息
                    MinIoUtil.deleteFile(BucketsConst.manage_audit, url);
                }
            }
            // 删除数据
            manageReviewInformationEntityMapper.deleteByPrimaryKey(oldData.getId());
        }
    }

    /**
     * 计划详情
     *
     * @param id
     * @return
     */
    @Override
    public Result details(Integer id) {

        // 查询计划数据详情
        LambdaQueryWrapper<ManageReviewPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManageReviewPlanEntity::getId, id);
        queryWrapper.eq(ManageReviewPlanEntity::getDelFlag, 0);
        ManageReviewPlanEntity data = manageReviewPlanEntityMapper.selectOne(queryWrapper);
        if (data == null) {
            return ResultUtil.error("计划不存在");
        }

        //TODO:1月12日  查询基础表信息 - 管理评审：人员职位信息
        List<TestInitDataEntity> personnelPosition = taskMapper.selectEntrustBasis(32);

        // 汇总集合数据
        List<ManageReviewInformationEntity> summarySet = new ArrayList<>();
        // 体系管理员附件信息
        List<ManageReviewInformationEntity> planCreatorList = methodCallDataQuery(id, personnelPosition.get(0).getName(), data.getPlanCreator());
        if (CollectionUtil.isNotEmpty(planCreatorList)) {
            summarySet.addAll(planCreatorList);
        }
        // 主持人员附件信息
        List<ManageReviewInformationEntity> reviewHostList = methodCallDataQuery(id, personnelPosition.get(1).getName(), data.getReviewHost());
        if (CollectionUtil.isNotEmpty(reviewHostList)) {
            summarySet.addAll(reviewHostList);
        }
        // 参加人员附件信息
        if (org.apache.commons.lang.StringUtils.isNotEmpty(data.getParticipant())) {
            String[] arrays = data.getParticipant().split(",");
            for (int i = 0; i < arrays.length; i++) {
                String participant = arrays[i];
                // 参与人员
                List<ManageReviewInformationEntity> participantList = methodCallDataQuery(id, personnelPosition.get(2).getName(), participant);
                if (CollectionUtil.isNotEmpty(participantList)) {
                    summarySet.addAll(participantList);
                }
            }
        }
        // 人员信息不为空的话
        if (CollectionUtil.isNotEmpty(summarySet)) {
            data.setManageReviewInformationEntities(summarySet);
        }
        return ResultUtil.success(data);
    }

    /**
     * 根据计划id 查询对应的附件信息及附件信息
     *
     * @param id          计划id
     * @param duties      职位
     * @param planCreator 人员信息
     * @return
     */
    public List<ManageReviewInformationEntity> methodCallDataQuery(Integer id, String duties, String planCreator) {
        // 接下来是 拼接人员与对应的附件信息。
        LambdaQueryWrapper<ManageReviewInformationEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 创建人信息
        lambdaQueryWrapper.eq(ManageReviewInformationEntity::getPlanCreator, planCreator);
        // 职务
        lambdaQueryWrapper.eq(ManageReviewInformationEntity::getDuties, duties);
        // pid信息
        lambdaQueryWrapper.eq(ManageReviewInformationEntity::getPid, id);
        lambdaQueryWrapper.eq(ManageReviewInformationEntity::getDelFlag, 0);

        // 创建人信息
        List<ManageReviewInformationEntity> planCreatorList = manageReviewInformationEntityMapper.selectList(lambdaQueryWrapper);
        // 需要 截取数据URL
        if (CollectionUtil.isNotEmpty(planCreatorList)) {
            for (ManageReviewInformationEntity informationEntity : planCreatorList) {
                // 判断附件不为空的话
                if (org.apache.commons.lang.StringUtils.isNotEmpty(informationEntity.getFileUrl())) {
                    // 截取信息 = url链接
                    String[] urls = informationEntity.getFileUrl().split(",");
                    // 截取信息 = url原始文件名称
                    String[] filaNames = informationEntity.getOriginalFileName().split(",");
                    // 附件数组
                    List<LabelValueVo> urlList = new ArrayList<>();
                    for (int i = 0; i < urls.length; i++) {
                        LabelValueVo valueVo = new LabelValueVo();
                        valueVo.setLabel(filaNames[i]);
                        valueVo.setText(urls[i]);
                        urlList.add(valueVo);
                    }
                    informationEntity.setUrls(urlList);
                }
            }
        }
        return planCreatorList;
    }

    /**
     * 管理评审：文件信息后缀名效验
     *
     * @param file      附件
     * @param suffixSet 后缀名符合的集合
     * @return
     */
    public String validationFileSuffix(MultipartFile[] file, List<TestInitDataEntity> suffixSet) {
        if (file != null && file.length >= 1) {
            for (MultipartFile multipartFile : file) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(multipartFile.getOriginalFilename())) {
                    String name = multipartFile.getOriginalFilename();
                    String[] strings = name.split("\\.");
                    String nameSuffix = strings[strings.length - 1];

                    // 当前附件标记
                    Boolean suffixTag = false;
                    // 遍历 : 后缀名符合的集合
                    for (TestInitDataEntity dataEntity : suffixSet) {
                        if (dataEntity.getName().equals(nameSuffix)) {
                            suffixTag = true;
                        }
                    }
                    if (!suffixTag) {
                        return "文件上传失败：  文件名 " + name + " 不支持存储 ";
                    }
                }
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updateManageReviewPlanEntity(ManageReviewPlanEntity manageReviewPlanEntity, MultipartFile[] file) {
        //TODO:1月12日  查询基础表信息 - 管理评审：人员职位信息
        List<TestInitDataEntity> personnelPosition = taskMapper.selectEntrustBasis(32);
        //TODO:1月12日  查询基础表信息 - 管理评审：文件后缀效验
        List<TestInitDataEntity> suffixSet = taskMapper.selectEntrustBasis(33);
        // 效验数据 评审目的
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewPurpose())) {
            return ResultUtil.error("评审目的不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewStartTime()) || StringUtils.isEmpty(manageReviewPlanEntity.getReviewEndTime())) {
            return ResultUtil.error("评审时间不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getReviewHost())) {
            return ResultUtil.error("评审主持不能为空");
        }
        // 参加人员不能为空
        if (StringUtils.isEmpty(manageReviewPlanEntity.getParticipant())) {
            return ResultUtil.error("参加人员不能为空");
        }
        if (StringUtils.isEmpty(manageReviewPlanEntity.getId())) {
            return ResultUtil.error("id不能为空");
        }
        // 文件名与后缀名参与效验
        String msg = validationFileSuffix(file, suffixSet);
        if (msg != null) {
            return ResultUtil.error(msg);
        }
        // 查询旧数据
        LambdaQueryWrapper<ManageReviewPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManageReviewPlanEntity::getId, manageReviewPlanEntity.getId());
        queryWrapper.eq(ManageReviewPlanEntity::getDelFlag, 0);
        ManageReviewPlanEntity oldManageReviewPlanEntity = manageReviewPlanEntityMapper.selectOne(queryWrapper);
        if (oldManageReviewPlanEntity == null) {
            return ResultUtil.error("更新失败，数据不存在");
        }
        // 创建人：
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        // 判断当前登录人 是否是 体系管理员
        List<SysUserEntity> userList = sysUserDao.systemManagementList();
        Integer sign = 1;
        for (SysUserEntity sysUserEntity : userList) {
            if (sysUserEntity.getUserId().equals(userInfo.getUserId())) {
                // sign = 0 表示当前用户为体系管理员
                sign = 0;
            }
        }
        if (sign == 1) {
            return ResultUtil.error(userInfo.getName() + "不是体系管理员");
        }

        // TODO: ---- ↑↑↑↑ 上述信息 均参与效验  ↑↑↑↑ ----
        //创建人信息
        manageReviewPlanEntity.setPlanCreator(null);
        manageReviewPlanEntity.setDelFlag(0);
        // 计划更新时间
        manageReviewPlanEntity.setUpdateTime(new Date());
        // 创建人 新增附件 - 单个
        if (file != null && file.length >= 1) {
            for (MultipartFile multipartFile : file) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(multipartFile.getOriginalFilename())) {
                    // 移除 旧附件信息 ：  附件删除
                    if (!StringUtils.isEmpty(oldManageReviewPlanEntity.getFileUrl())) {
                        String[] strings = oldManageReviewPlanEntity.getFileUrl().split(",");
                        for (int i = 0; i < strings.length; i++) {
                            String[] urls = strings[i].split("/");
                            String url = urls[urls.length - 1];
                            // 删除附件信息
                            MinIoUtil.deleteFile(BucketsConst.manage_audit, url);
                        }
                    }
                    Long fileCode = GenID.getID();
                    String name = multipartFile.getOriginalFilename();
                    String[] strings = name.split("\\.");
                    String upload = MinIoUtil.upload(BucketsConst.manage_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                    // 截取 \\? 前数据
                    String filePath = upload.split("\\?")[0];
                    manageReviewPlanEntity.setFileUrl(filePath);
                    manageReviewPlanEntity.setOriginalFileName(name);
                }
            }
        }
        // 更新： 创建计划
        manageReviewPlanEntityMapper.updateByPrimaryKeySelective(manageReviewPlanEntity);
        // 参加人员: 数据截取后新增
        String[] participants = manageReviewPlanEntity.getParticipant().split(",");
        for (int i = 0; i < participants.length; i++) {
            manageReviewPlanEntity.setPlanCreator(participants[i]);
            // 参加人员：更新附件
            dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(2).getName(), null, userInfo);
        }
        // 旧参加人员
        String[] oldParticipants = oldManageReviewPlanEntity.getParticipant().split(",");

        // deleteUserList ： 删除用户列表信息： 获取string[] 差集
        String[] deleteUserList = StringToolUtil.minus(participants, oldParticipants);
        // 比较参加人员 获取得到 被删除用户操作 : deleteUserList
        for (String string : deleteUserList) {
            ManageReviewPlanEntity deleteManageReviewPlanEntity = new ManageReviewPlanEntity();
            deleteManageReviewPlanEntity.setPlanCreator(string);
            deleteManageReviewPlanEntity.setId(manageReviewPlanEntity.getId());
            // 删除文件及附件信息
            deleteOperation(deleteManageReviewPlanEntity, personnelPosition.get(2).getName());
        }
        // 比较主持人是否变更：
        if (!manageReviewPlanEntity.getReviewHost().equals(oldManageReviewPlanEntity.getReviewHost())) {
            // 删除文件及附件信息
            ManageReviewPlanEntity deleteManageReviewPlanEntity = new ManageReviewPlanEntity();
            deleteManageReviewPlanEntity.setPlanCreator(oldManageReviewPlanEntity.getReviewHost());
            deleteManageReviewPlanEntity.setId(manageReviewPlanEntity.getId());
            deleteOperation(deleteManageReviewPlanEntity, personnelPosition.get(1).getName());
            // 主持人：更新附件
            manageReviewPlanEntity.setPlanCreator(manageReviewPlanEntity.getReviewHost());
            dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(1).getName(), null, userInfo);
        }
        return ResultUtil.success("更新成功");
    }

    @Override
    public Result getSystemManagementList() {

        // 查询体系管理员信息
        List<SysUserEntity> userList = sysUserDao.systemManagementList();

        if (CollectionUtil.isNotEmpty(userList)) {
            return ResultUtil.success(userList);
        } else {
            return ResultUtil.success(new ArrayList<>());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(Integer id) {
        if (StringUtils.isEmpty(id)) {
            return ResultUtil.error("删除失败：id 不能为空");
        }
        // 查询旧数据
        LambdaQueryWrapper<ManageReviewPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManageReviewPlanEntity::getId, id);
        queryWrapper.eq(ManageReviewPlanEntity::getDelFlag, 0);
        ManageReviewPlanEntity oldManageReviewPlanEntity = manageReviewPlanEntityMapper.selectOne(queryWrapper);
        if (oldManageReviewPlanEntity == null) {
            return ResultUtil.error("删除失败：数据不存在");
        }
        // 创建人：
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        // 判断当前登录人 是否是 体系管理员
        List<SysUserEntity> userList = sysUserDao.systemManagementList();
        Integer sign = 1;
        for (SysUserEntity sysUserEntity : userList) {
            if (sysUserEntity.getUserId().equals(userInfo.getUserId())) {
                // sign = 0 表示当前用户为体系管理员
                sign = 0;
            }
        }
        if (sign == 1) {
            return ResultUtil.error(userInfo.getName() + "删除失败,不是体系管理员");
        }
        //TODO:1月12日  查询基础表信息 - 管理评审：人员职位信息
        List<TestInitDataEntity> personnelPosition = taskMapper.selectEntrustBasis(32);
        // 创建人附件
        if (oldManageReviewPlanEntity.getPlanCreator() != null) {
            // 删除文件及人员
            ManageReviewPlanEntity deleteManageReviewPlanEntity = new ManageReviewPlanEntity();
            deleteManageReviewPlanEntity.setPlanCreator(oldManageReviewPlanEntity.getPlanCreator());
            deleteManageReviewPlanEntity.setId(oldManageReviewPlanEntity.getId());
            deleteOperation(deleteManageReviewPlanEntity, personnelPosition.get(0).getName());
        }
        // 旧参与人员
        if (oldManageReviewPlanEntity.getParticipant() != null) {
            // 参加人员: 数据截取后新增
            String[] participants = oldManageReviewPlanEntity.getParticipant().split(",");
            for (int i = 0; i < participants.length; i++) {
                // 删除文件及附件信息
                ManageReviewPlanEntity deleteManageReviewPlanEntity = new ManageReviewPlanEntity();
                deleteManageReviewPlanEntity.setPlanCreator(participants[i]);
                deleteManageReviewPlanEntity.setId(oldManageReviewPlanEntity.getId());
                // 删除文件及人员
                deleteOperation(deleteManageReviewPlanEntity, personnelPosition.get(2).getName());
            }
        }
        // 删除主持人信息
        // 创建人附件
        if (oldManageReviewPlanEntity.getReviewHost() != null) {
            // 删除文件及人员
            ManageReviewPlanEntity deleteManageReviewPlanEntity = new ManageReviewPlanEntity();
            deleteManageReviewPlanEntity.setPlanCreator(oldManageReviewPlanEntity.getReviewHost());
            deleteManageReviewPlanEntity.setId(oldManageReviewPlanEntity.getId());
            deleteOperation(deleteManageReviewPlanEntity, personnelPosition.get(1).getName());
        }
        // 移除 旧附件信息 ：  附件删除
        if (!StringUtils.isEmpty(oldManageReviewPlanEntity.getFileUrl())) {
            String[] strings = oldManageReviewPlanEntity.getFileUrl().split(",");
            for (int i = 0; i < strings.length; i++) {
                String[] urls = strings[i].split("/");
                String url = urls[urls.length - 1];
                // 删除附件信息
                MinIoUtil.deleteFile(BucketsConst.manage_audit, url);
            }
        }
        // 删除创建计划信息
        manageReviewPlanEntityMapper.deleteByPrimaryKey(oldManageReviewPlanEntity.getId());

        return ResultUtil.success("删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result uploadFile(Integer type, Integer id, MultipartFile[] file) {

        // 获取当前登录人：
        // 创建人：
        SysUserEntity userInfo = ShiroUtils.getUserInfo();

        //TODO:1月12日  查询基础表信息 - 管理评审：人员职位信息
        List<TestInitDataEntity> personnelPosition = taskMapper.selectEntrustBasis(32);

        // 查询旧数据
        LambdaQueryWrapper<ManageReviewPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManageReviewPlanEntity::getId, id);
        queryWrapper.eq(ManageReviewPlanEntity::getDelFlag, 0);
        ManageReviewPlanEntity oldManageReviewPlanEntity = manageReviewPlanEntityMapper.selectOne(queryWrapper);
        if (oldManageReviewPlanEntity == null) {
            return ResultUtil.error("上传附件失败，计划不存在");
        }
        // 附件效验
        for (MultipartFile multipartFile : file) {
            if (StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
                return ResultUtil.error(userInfo.getName() + "： 上传失败,附件不能为空");
            }
        }

        // 登录人 比较 创建人 信息
        if (type == 2) {
            // 判断当前登录人 是否是 体系管理员
            List<SysUserEntity> userList = sysUserDao.systemManagementList();
            Integer sign = 1;
            for (SysUserEntity sysUserEntity : userList) {
                if (sysUserEntity.getUserId().equals(userInfo.getUserId())) {
                    // sign = 0 表示当前用户为体系管理员
                    sign = 0;
                }
            }
            if (sign == 1) {
                return ResultUtil.error(userInfo.getName() + "上传失败,不是体系管理员");
            }

            ManageReviewPlanEntity manageReviewPlanEntity = new ManageReviewPlanEntity();
            manageReviewPlanEntity.setPlanCreator(userInfo.getName() + "&" + userInfo.getUserId());
            manageReviewPlanEntity.setId(oldManageReviewPlanEntity.getId());
            dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(0).getName(), file, userInfo);
            // 更新计划信息：评审报告状态
            ManageReviewPlanEntity manageReviewPlanEntity1 = new ManageReviewPlanEntity();
            manageReviewPlanEntity1.setId(oldManageReviewPlanEntity.getId());
            // 上传评审总结时：
            manageReviewPlanEntity1.setReviewReportStatus("已完成");
            manageReviewPlanEntityMapper.updateByPrimaryKeySelective(manageReviewPlanEntity1);
            return ResultUtil.success("上传附件成功");
        }

        // 登录人 比较 参与人员 信息
        if (oldManageReviewPlanEntity.getParticipant().contains(userInfo.getName() + "&" + userInfo.getUserId()) && type == 1) {
            ManageReviewPlanEntity manageReviewPlanEntity = new ManageReviewPlanEntity();
            manageReviewPlanEntity.setPlanCreator(userInfo.getName() + "&" + userInfo.getUserId());
            manageReviewPlanEntity.setId(oldManageReviewPlanEntity.getId());
            dynamicHandlingFileAddORUpdate(manageReviewPlanEntity, personnelPosition.get(2).getName(), file, userInfo);
            return ResultUtil.success("上传附件成功");
        }
        return ResultUtil.error("上传附件失败");
    }


}
