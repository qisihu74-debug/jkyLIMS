package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.ProblemComment;
import com.lims.manage.erp.entity.ProblemInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ProblemInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 问答列表控制层
 *
 * @author: zhq
 * @date: 2023-01-16
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/problemInfo")
public class ProblemInfoController extends ApiController {

    @Resource
    ProblemInfoService problemInfoService;

    /**
     * 问答列表
     *
     * @param problemInfo 查询条件
     * @param pageNo      页码
     * @param pageSize    页数
     * @return Result<?>
     */
    @GetMapping(value = "pageList")
    public Result<?> pageList(ProblemInfo problemInfo,
                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<ProblemInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(problemInfoService.pageList(page, problemInfo));
    }

    /**
     * 添加问答信息
     *
     * @param problemInfo 问答信息
     * @return Result
     */
    @PostMapping(value = "addProblemInfo")
    public Result<?> addProblemInfo(@RequestBody ProblemInfo problemInfo) {
        return problemInfoService.addProblemInfo(problemInfo);
    }

    /**
     * 获取问题详情并增加对应问题的浏览量
     *
     * @param problemId 问题id
     * @return Result
     */
    @GetMapping(value = "getProblemInfo/{problemId}")
    public Result<?> getProblemInfo(@PathVariable String problemId) {
        return problemInfoService.getProblemInfo(problemId);
    }

    /**
     * 修改问题信息
     *
     * @param problemInfo 问题信息
     * @return Result
     */
    @PutMapping(value = "editProblemInfo")
    public Result<?> editProblemInfo(@RequestBody ProblemInfo problemInfo) {
        if (problemInfoService.updateById(problemInfo)) {
            return ResultUtil.success("操作成功");
        }
        return ResultUtil.error("操作异常");
    }

    /**
     * 问题归类
     *
     * @param problemInfo 问题信息
     * @return Result
     */
    @PutMapping(value = "problemClassification")
    public Result<?> problemClassification(@RequestBody ProblemInfo problemInfo) {
       return problemInfoService.problemClassification(problemInfo);
    }

    /**
     * 根据问题id删除问题内容
     *
     * @param problemId 问题id
     * @return Result<?>
     */
    @DeleteMapping(value = "delProblemInfo/{problemId}")
    public Result<?> delProblemInfo(@PathVariable String problemId) {
        return problemInfoService.delProblemInfo(problemId);
    }

    /**
     * 根据问题id获取评论列表
     *
     * @param problemId 问题id
     * @param pageNo    页码
     * @param pageSize  页数
     * @return Result
     */
    @GetMapping(value = "getProblemCommentList")
    public Result<?> getProblemCommentList(@RequestParam(name = "problemId") String problemId,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "commentId",required = false) String commentId) {
        Page<ProblemComment> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(problemInfoService.getProblemCommentList(page, problemId,commentId));
    }

    /**
     * 问题与回答评论
     *
     * @param problemComment 评论信息
     * @return Result
     */
    @PostMapping(value = "addCommentProblem")
    public Result<?> addCommentProblem(@RequestBody ProblemComment problemComment) {
        return problemInfoService.addCommentProblem(problemComment);
    }

    /**
     * 根据问题评论id删除问题评论信息
     *
     * @param commentId 问题评论id
     * @return Result<?>
     */
    @DeleteMapping(value = "delProblemComment/{commentId}")
    public Result<?> delProblemComment(@PathVariable String commentId) {
        return problemInfoService.delProblemComment(commentId);
    }

    /**
     * 根据评论id进行点赞
     * @param commentId 评论id
     * @return Result
     */
    @PostMapping(value = "like")
    public Result<?> like(@RequestParam(name = "commentId") String commentId){
        return problemInfoService.like(commentId);
    }

    /**
     * 根据评论id进行点踩
     * @param commentId 评论id
     * @return Result
     */
    @PostMapping(value = "tap")
    public Result<?> tap(String commentId){
        return problemInfoService.tap(commentId);
    }

    /**
     * 获取急待回答列表
     * @return Result
     */
    @GetMapping(value = "getUrgentProblemList")
    public Result<?> getUrgentProblemList(){
        return problemInfoService.getUrgentProblemList();
    }

    /**
     * 获取问答推荐列表
     * @return Result
     */
    @GetMapping(value = "getProblemRecommendList")
    public Result<?> getProblemRecommendList(){
        return problemInfoService.getProblemRecommendList();
    }
}
