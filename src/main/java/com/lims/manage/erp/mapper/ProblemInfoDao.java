package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.ProblemComment;
import com.lims.manage.erp.entity.ProblemInfo;
import com.lims.manage.erp.vo.ProblemCommentVo;
import com.lims.manage.erp.vo.ProblemInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 问答信息dao层接口
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface ProblemInfoDao extends BaseMapper<ProblemInfo> {

    /**
     * 问答列表
     *
     * @param page        分页参数
     * @param problemInfo 查询条件
     * @param searchType 是否根据全类型搜索
     * @return IPage<ProblemInfoVo>
     */
    IPage<ProblemInfoVo> pageList(Page<ProblemInfo> page, @Param("problemInfo") ProblemInfo problemInfo,
                                  @Param("searchType") boolean searchType);

    /**
     * 根据问题id增加问题浏览量
     *
     * @param problemId 问题id
     * @return Boolean
     */
    Boolean addProblemViewNum(@Param("problemId") String problemId);

    /**
     * 根据问题id获取问题详情
     *
     * @param problemId 问题id
     * @return ProblemInfoVo
     */
    ProblemInfoVo getProblemInfo(@Param("problemId") String problemId);

    /**
     * 根据问题id获取评论列表
     *
     * @param page      分页参数
     * @param userId 用户id
     * @param eventType 用户操作事件类型
     * @param problemId 问题id
     * @param parentCommentId 父级评论id
     * @return IPage<ProblemCommentVo>
     */
    IPage<ProblemCommentVo> getProblemCommentList(Page<ProblemComment> page, @Param("userId")String userId,
                                                  @Param("eventType") String eventType,
                                                  @Param("problemId") String problemId,
                                                  @Param("parentCommentId")String parentCommentId);

    /**
     * 根据评论id增加评论点赞数量
     *
     * @param commentId 评论id
     * @return Boolean
     */
    Boolean addCommentLikeNum(@Param("commentId") String commentId);

    /**
     * 根据评论id增加评论点踩数量
     *
     * @param commentId 评论id
     * @return Boolean
     */
    Boolean addCommentTapNum(@Param("commentId") String commentId);

    /**
     * 获取急待回答列表
     * @return List
     */
    List<Map<String,Object>> getUrgentProblemList();

    /**
     * 获取问答推荐列表
     * @return List
     */
    List<Map<String,Object>> getProblemRecommendList();
}
