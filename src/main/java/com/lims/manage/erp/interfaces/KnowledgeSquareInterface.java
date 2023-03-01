package com.lims.manage.erp.interfaces;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.entity.PlanInfo;
import com.lims.manage.erp.entity.ProblemInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.DataInfoService;
import com.lims.manage.erp.service.IntegralInfoService;
import com.lims.manage.erp.service.PlanInfoService;
import com.lims.manage.erp.service.ProblemInfoService;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.UserIntegralRankingListVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 知识广场接口列表
 * @author: zhq
 * @date: 2023-02-01
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/knowledgeSquare")
public class KnowledgeSquareInterface {

    @Resource
    DataInfoService dataInfoService;

    @Resource
    PlanInfoService planInfoService;

    @Resource
    IntegralInfoService integralInfoService;

    @Resource
    ProblemInfoService problemInfoService;

    /**
     * 知识广场-学习资料、视频列表
     *
     * @param dataInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "getDataInfoList")
    public Result<?> getDataInfoList(DataInfo dataInfo,
                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<DataInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(dataInfoService.getDataInfoList(page, dataInfo));
    }

    /**
     * 知识广场-有问有答列表
     *
     * @param problemInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "getProblemInfoList")
    public Result<?> getProblemInfoList(ProblemInfo problemInfo,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<ProblemInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(problemInfoService.getProblemInfoList(page, problemInfo));
    }

    /**
     * 知识广场-培训计划
     *
     * @param planInfo 查询条件
     * @param pageNo   页码
     * @param pageSize 页数
     * @return Result<?>
     */
    @GetMapping(value = "getPlanInfoList")
    public Result<?> getPlanInfoList(PlanInfo planInfo,
                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<PlanInfo> page = new Page<>(pageNo, pageSize);
        return ResultUtil.success(planInfoService.pageList(page, planInfo));
    }

    /**
     * 知识广场-学习积分排行榜
     *
     * @param pageNo   页码
     * @param pageSize 条数
     * @return Result<?>
     */
    @GetMapping(value = "/getIntegralRankingList")
    public Result<?> getIntegralRankingList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        String integralType = "1";
        Page<UserIntegralRankingListVo> page = new Page<>(pageNo, pageSize);
        IPage<UserIntegralRankingListVo> pageList = integralInfoService.getIntegralRankingList(page, integralType);
        return ResultUtil.success("获取积分排行榜", pageList);
    }
}
