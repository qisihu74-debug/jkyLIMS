package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.ProblemComment;
import com.lims.manage.erp.entity.ProblemInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.ProblemCommentVo;
import com.lims.manage.erp.vo.ProblemInfoVo;

/**
 * 问答信息业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface ProblemInfoService extends IService<ProblemInfo> {

    /**
     * 问答列表
     * @param page 分页参数
     * @param problemInfo  查询条件
     * @return IPage<ProblemInfoVo>
     */
    IPage<ProblemInfoVo> pageList(Page<ProblemInfo> page, ProblemInfo problemInfo);

    /**
     * 添加问答信息
     * @param problemInfo 问答信息
     * @return Result
     */
    Result<?> addProblemInfo(ProblemInfo problemInfo);

    /**
     * 获取问题详情并增加对应问题的浏览量
     * @param problemId 问题id
     * @return Result
     */
    Result<?> getProblemInfo(String problemId);

    /**
     * 问题归类
     * @param problemInfo 问题信息
     * @return Result
     */
    Result<?> problemClassification(ProblemInfo problemInfo);

    /**
     * 根据问题id删除问题信息
     * @param problemId 问题id
     * @return Result
     */
    Result<?> delProblemInfo(String problemId);

    /**
     * 根据问题id获取评论列表
     * @param page 分页参数
     * @param problemId 问题id
     * @param commentId 评论id
     * @return IPage<ProblemCommentVo>
     */
    IPage<ProblemCommentVo> getProblemCommentList(Page<ProblemComment> page,String problemId,String commentId);

    /**
     * 添加评论内容
     * @param problemComment 评论信息
     * @return Result
     */
    Result<?> addCommentProblem(ProblemComment problemComment);

    /**
     * 根据问题评论id删除问题评论信息
     * @param commentId 问题评论id
     * @return Result
     */
    Result<?> delProblemComment(String commentId);

    /**
     * 根据评论id进行点赞
     * @param commentId 评论id
     * @return Result
     */
    Result<?> like(String commentId);

    /**
     * 根据评论id进行点踩
     * @param commentId 评论id
     * @return Result
     */
    Result<?> tap(String commentId);

    /**
     * 获取急待回答列表
     * @return Result
     */
    Result<?> getUrgentProblemList();

    /**
     * 获取问答推荐列表
     * @return Result
     */
    Result<?> getProblemRecommendList();

    /**
     * 知识广场-问答列表
     * @param page 分页参数
     * @param problemInfo  查询条件
     * @return IPage<ProblemInfoVo>
     */
    IPage<ProblemInfoVo> getProblemInfoList(Page<ProblemInfo> page, ProblemInfo problemInfo);
}
