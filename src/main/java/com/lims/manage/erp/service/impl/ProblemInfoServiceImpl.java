package com.lims.manage.erp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.ProblemInfoDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.ProblemCommentVo;
import com.lims.manage.erp.vo.ProblemInfoVo;
import com.lims.manage.erp.vo.UserOperationStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.xpath.operations.Bool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 问答信息业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class ProblemInfoServiceImpl extends ServiceImpl<ProblemInfoDao, ProblemInfo> implements ProblemInfoService {

    @Resource
    ProblemCommentService problemCommentService;

    @Resource
    ProblemCommentRecordService problemCommentRecordService;

    @Resource
    UserOperationRecordService userOperationRecordService;

    @Resource
    UserIntegralRecordService userIntegralRecordService;

    @Resource
    LabelInfoService labelInfoService;

    @Resource
    SysUserRoleService sysUserRoleService;

    @Override
    public IPage<ProblemInfoVo> pageList(Page<ProblemInfo> page, ProblemInfo problemInfo) {
        //根据传入的标签名称获取对应的标签
        if(problemInfo!=null && StringUtils.isNotEmpty(problemInfo.getDataLabel())){
            //获取标签列表
            List<LabelInfo> labelInfos=labelInfoService.list(Wrappers.<LabelInfo>lambdaQuery().like(LabelInfo::getLabelContent,problemInfo.getDataLabel()).select(LabelInfo::getLabelId));
            if (ListUtils.isEmpty(labelInfos)){
                return null;
            }
            //根据标签和对应的查询条件查询
            problemInfo.setLabelIdList(labelInfos);
        }
        return baseMapper.pageList(page, problemInfo,false);
    }

    @Override
    public Result<?> addProblemInfo(ProblemInfo problemInfo) {
        //根据标签名称获取标签对应的id
        problemInfo.setDataLabel(labelInfoService.getLabelId(problemInfo.getDataLabel()));
        problemInfo.setProblemType(CommonConstant.PROBLEM_TYPE_COMMON);
        problemInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        problemInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        problemInfo.setCreateTime(new Date());
        this.save(problemInfo);
        return ResultUtil.success("添加问题成功");
    }

    @Override
    public Result<?> getProblemInfo(String problemId) {
        //增加对应问题的浏览量
        if (baseMapper.addProblemViewNum(problemId)) {

            //根据用户id获取最新的一条浏览记录
            LambdaQueryWrapper<UserOperationRecord> userOperationWrapper=Wrappers.lambdaQuery();
            userOperationWrapper.eq(UserOperationRecord::getUserId,ShiroUtils.getUserInfo().getUserId().toString());
            userOperationWrapper.eq(UserOperationRecord::getEventType,CommonConstant.USER_OPERATION_EVENT_TYPE_PROBLEM);
            userOperationWrapper.eq(UserOperationRecord::getEventId,problemId);
            userOperationWrapper.eq(UserOperationRecord::getOperationType,CommonConstant.USER_OPERATION_TYPE_VIEW);
            //userOperationWrapper.orderByDesc(UserOperationRecord::getCreateTime);
            //userOperationWrapper.last("limit 1");
            UserOperationRecord userOperationRecord =userOperationRecordService.getOne(userOperationWrapper);
            //如果浏览记录为空 则添加浏览记录
            if (userOperationRecord==null) {
                //添加用户浏览记录
                userOperationRecord = new UserOperationRecord();
                userOperationRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
                userOperationRecord.setEventType(CommonConstant.USER_OPERATION_EVENT_TYPE_PROBLEM);
                userOperationRecord.setEventId(problemId);
                userOperationRecord.setOperationType(CommonConstant.USER_OPERATION_TYPE_VIEW);
                userOperationRecord.setCreateBy(ShiroUtils.getUserInfo().getUsername());
                userOperationRecord.setCreateTime(new Date());
                userOperationRecordService.save(userOperationRecord);

            }else{
                userOperationRecord.setCreateTime(new Date());
                userOperationRecordService.updateById(userOperationRecord);
            }
            //获取问题详情
            return ResultUtil.success("获取问题详情", baseMapper.getProblemInfo(problemId));
        }
        //获取问题详情信息
        return ResultUtil.error("获取问题详情异常");
    }

    @Override
    public Result<?> problemClassification(ProblemInfo problemInfo) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity=sysUserRoleService.getOne(Wrappers.< SysUserRoleEntity >lambdaQuery().eq(SysUserRoleEntity::getUserId,ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId,CommonConstant.USER_ROLE_PROBLEM_CLASSIFICATION));
        if(sysUserRoleEntity==null){
            return ResultUtil.error(500,"该用户没有问题归类权限");
        }
        ProblemInfo info=new ProblemInfo();
        info.setProblemId(problemInfo.getProblemId());
        info.setProblemType(problemInfo.getProblemType());

        if (this.updateById(info)) {
            return ResultUtil.success("操作成功");
        }
        return ResultUtil.error("操作异常");
    }

    @Override
    public Result<?> delProblemInfo(String problemId) {
        //根据用户角色进行判断
        SysUserRoleEntity sysUserRoleEntity=sysUserRoleService.getOne(Wrappers.< SysUserRoleEntity >lambdaQuery().eq(SysUserRoleEntity::getUserId,ShiroUtils.getUserInfo().getUserId()).eq(SysUserRoleEntity::getRoleId,CommonConstant.USER_ROLE_PROBLEM_CLASSIFICATION));
        if(sysUserRoleEntity==null){
            return ResultUtil.error(500,"该用户没有删除权限");
        }
        ProblemInfo problemInfo=this.getById(problemId);
        if(problemInfo!=null){
            //根据记录标签来进行自定义标签的删除
            labelInfoService.delLabelInfo(Arrays.asList(problemInfo.getDataLabel().split(",")));
            if (this.removeById(problemId)) {
                return ResultUtil.success("问题删除成功");
            }
        }
        return ResultUtil.error("问题删除异常");
    }

    @Override
    public IPage<ProblemCommentVo> getProblemCommentList(Page<ProblemComment> page, String problemId,String commentId) {
        String userId = ShiroUtils.getUserInfo().getUserId().toString();
        IPage<ProblemCommentVo> pageList = baseMapper.getProblemCommentList(page, userId, CommonConstant.USER_OPERATION_EVENT_TYPE_COMMENT, problemId, commentId);
        /*if (pageList.getRecords().size() > 0) {
            pageList.getRecords().forEach(this::findAllChild);
        }*/
        return pageList;
    }

    /**
     * 根据父级评论id获取子集
     *
     * @param problemCommentVo 评论信息
     */
    public void findAllChild(ProblemCommentVo problemCommentVo) {
        String userId = ShiroUtils.getUserInfo().getUserId().toString();
        IPage<ProblemCommentVo> childrenList = baseMapper.getProblemCommentList(new Page<>(), userId, CommonConstant.USER_OPERATION_EVENT_TYPE_COMMENT, null, problemCommentVo.getCommentId());
        //problemCommentVo.setChildren(childrenList.getRecords());
        if (childrenList.getRecords().size() > 0) {
            childrenList.getRecords().forEach(this::findAllChild);
        }
    }

    @Override
    public Result<?> addCommentProblem(ProblemComment problemComment) {
        problemComment.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        problemComment.setCreateTime(new Date());
        problemComment.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        if (problemCommentService.save(problemComment)) {
            //设置默认评论点赞记录
            ProblemCommentRecord problemCommentRecord = new ProblemCommentRecord();
            problemCommentRecord.setCommentId(problemComment.getCommentId());
            problemCommentRecordService.save(problemCommentRecord);
            return ResultUtil.success("评论成功");
        }
        return ResultUtil.error("评论异常");
    }

    @Override
    public Result<?> delProblemComment(String commentId) {
        //根据评论id获取评论信息
        ProblemComment problemComment = problemCommentService.getById(commentId);
        if (problemComment != null) {
            //根据评论id获取问答id
            ProblemInfo problemInfo=this.getById(problemComment.getProblemId());
            if(problemInfo!=null){
                //如果不是自己的问答并且也不是自己的评论
                if(!problemInfo.getUserId().equals(ShiroUtils.getUserInfo().getUserId().toString())
                && !problemComment.getUserId().equals(ShiroUtils.getUserInfo().getUserId().toString())){
                    //不可以删除
                    return ResultUtil.error(500,"没有删除权限");
                }
                //删除当前评论的子信息
                problemCommentService.remove(Wrappers.<ProblemComment>lambdaQuery().eq(ProblemComment::getParentCommentId, commentId));
                //删除当前评论信息
                problemCommentService.removeById(commentId);
                return ResultUtil.success("删除成功");
            }
        }
        return ResultUtil.error("没有找到要删除的评论信息");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> like(String commentId) {
        return userOperationComment(commentId, CommonConstant.USER_OPERATION_TYPE_LIKE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> tap(String commentId) {
        return userOperationComment(commentId, CommonConstant.USER_OPERATION_TYPE_TAP);
    }

    /**
     * 用户对评论进行点赞或点踩操作
     *
     * @param commentId     评论id
     * @param operationType 操作类型
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<?> userOperationComment(String commentId, String operationType) {
        //获取当前用户的点赞或点踩状态
        UserOperationRecord userOperationRecord = new UserOperationRecord();
        userOperationRecord.setEventType(CommonConstant.USER_OPERATION_EVENT_TYPE_COMMENT);
        userOperationRecord.setEventId(commentId);
        UserOperationStatusVo userOperationStatus = userOperationRecordService.getUserOperationStatus(userOperationRecord);
        //如果有用户操作记录并且有点赞或点踩操作 则直接返回
        if (userOperationStatus != null) {
            if (userOperationStatus.getLikeStatus() != null || userOperationStatus.getTapStatus() != null) {
                //如果已经进行点赞则直接返回已点赞
                return ResultUtil.error(500, "用户已进行点赞或点踩操作");
            }
        }
        //根据不同的操作类型进行默认值
        boolean operationResult = false;
        //根据当前评论id获取评论点赞记录
        ProblemCommentRecord problemCommentRecord = problemCommentRecordService.getById(commentId);
        if (problemCommentRecord == null) {
            return ResultUtil.error(500, "评论记录异常");
        }
        if (CommonConstant.USER_OPERATION_TYPE_LIKE.equals(operationType)) {
            //添加点赞数量
            operationResult = baseMapper.addCommentLikeNum(commentId);
            //设置点赞或点踩数量及对应点赞、点踩状态
            problemCommentRecord.setLikeNum(problemCommentRecord.getLikeNum() + 1);
            problemCommentRecord.setLikeStatus(true);
        } else if (CommonConstant.USER_OPERATION_TYPE_TAP.equals(operationType)) {
            //添加点踩数量
            operationResult = baseMapper.addCommentTapNum(commentId);
            //设置点赞或点踩数量及对应点赞、点踩状态
            problemCommentRecord.setTapNum(problemCommentRecord.getTapNum() + 1);
            problemCommentRecord.setTapStatus(true);
        }
        //用户点赞或点踩操作成功后
        if (operationResult) {
            //用户添加点赞记录
            userOperationRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
            userOperationRecord.setOperationType(operationType);
            userOperationRecord.setCreateBy(ShiroUtils.getUserInfo().getUsername());
            userOperationRecord.setCreateTime(new Date());
            userOperationRecordService.save(userOperationRecord);

            /*//返回点赞或点踩数量及对应点赞、点踩状态
            ProblemCommentRecord commentRecord = problemCommentRecordService.getById(commentId);
            if (commentRecord == null) {
                //设置默认评论记录
                problemCommentRecord.setCommentId(commentId);
                problemCommentRecordService.save(problemCommentRecord);
            } else {
                //把查询结果复制到返回结果中
                problemCommentRecord.setCommentId(commentRecord.getCommentId());
                problemCommentRecord.setLikeNum(commentRecord.getLikeNum());
                problemCommentRecord.setTapNum(commentRecord.getTapNum());
            }
*/
            //根据点赞点踩类型进行积分规则区分
            if (CommonConstant.USER_OPERATION_TYPE_LIKE.equals(operationType)) {
                //判断点赞数量是否符合规则
                if (problemCommentRecord.getLikeNum() % CommonConstant.INTEGRAL_RULE_COMMENT_LIKE_NUM == 0) {
                    ProblemComment problemComment = problemCommentService.getById(commentId);
                    if (problemComment != null) {
                        //添加用户积分和获取积分记录
                        Integer resultInt = userIntegralRecordService.addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_LIKE_NUM, null, problemComment.getUserId(), CommonConstant.USER_OPERATION_EVENT_TYPE_COMMENT, commentId);
                    }
                }
            }
            //返回点赞或点踩数量及对应点赞、点踩状态
            return ResultUtil.success(problemCommentRecord);
        } else {
            return ResultUtil.error("用户操作失败");
        }
    }

    @Override
    public Result<?> getUrgentProblemList() {
        return ResultUtil.success(baseMapper.getUrgentProblemList());
    }

    @Override
    public Result<?> getProblemRecommendList() {
        return ResultUtil.success(baseMapper.getProblemRecommendList());
    }

    @Override
    public IPage<ProblemInfoVo> getProblemInfoList(Page<ProblemInfo> page, ProblemInfo problemInfo) {
        //根据传入的标题获取对应的标签
        if(problemInfo!=null && StringUtils.isNotEmpty(problemInfo.getProblemTitle())){
            //获取标签列表
            List<LabelInfo> labelInfos=labelInfoService.list(Wrappers.<LabelInfo>lambdaQuery().like(LabelInfo::getLabelContent,problemInfo.getProblemTitle()).select(LabelInfo::getLabelId));
            //根据标签和对应的查询条件查询
            problemInfo.setLabelIdList(labelInfos);
        }
        return baseMapper.pageList(page, problemInfo,true);
    }
}
