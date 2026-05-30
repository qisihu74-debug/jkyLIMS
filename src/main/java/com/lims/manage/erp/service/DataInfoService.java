package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DataAuditRecord;
import com.lims.manage.erp.entity.DataExperience;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.DataExperienceVo;
import com.lims.manage.erp.vo.DataInfoVo;
import com.lims.manage.erp.vo.KsDataInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 学习资料信息业务层接口
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface DataInfoService extends IService<DataInfo> {

    /**
     * 获取优质分享者
     *
     * @return 用户名称列表
     */
    List<Map<String, Object>> getQualityShare();

    /**
     * 我分享的学习资料/视频
     *
     * @param page     分页参数
     * @param dataInfo 查询条件
     * @return IPage<DataInfoVo>
     */
    IPage<DataInfoVo> pageList(Page page, DataInfo dataInfo);

    /**
     * 分享学习资料/视频
     *
     * @param dataInfo 学习资料信息
     * @param file     学习资料文件
     * @return Result
     */
    Result<?> addDataInfo(DataInfo dataInfo, MultipartFile file);

    /**
     * 获取资料详情-用于修改
     * @param dataId 资料id
     * @return DataInfo
     */
    DataInfo getDataInfoById(String dataId);

    /**
     * 修改学习资料/视频
     *
     * @param dataInfo 学习资料信息
     * @param file     学习资料文件
     * @return Result
     */
    Result<?> editDataInfo(DataInfo dataInfo, MultipartFile file);

    /**
     * 根据学习资料id删除学习资料
     *
     * @param dataId 学习资料id
     * @return
     */
    Result<?> delDataInfo(String dataId);

    /**
     * 根据资料id获取资料信息
     *
     * @param dataId 资料id
     * @return Result
     */
    Result<?> getDataAuditInfo(String dataId);

    /**
     * 根据资料id获取资料信息详情
     * @param dataId 资料id
     * @return Result
     */
    Result<?> getDataInfoDetail(String dataId);

    /**
     * 审核学习资料
     *
     * @param dataAuditRecord 审核数据
     * @return Result
     */
    Result<?> auditData(DataAuditRecord dataAuditRecord);

    /**
     * 知识广场-学习资料列表
     * @param page 分页参数
     * @param dataInfo 查询条件
     * @return IPage<KsDataInfoVo>
     */
    IPage<KsDataInfoVo> getDataInfoList(Page page, DataInfo dataInfo);

    /**
     * 根据资料id进行点赞操作
     * @param dataId 资料id
     * @return Result
     */
    Result<?> like(String dataId);

    /**
     * 根据资料id进行点踩操作
     * @param dataId 资料id
     * @return Result
     */
    Result<?> tap(String dataId);

    /**
     * 根据资料id进行收藏操作
     * @param dataId 资料id
     * @return Result
     */
    Result<?> collect(String dataId);

    /**
     * 根据资料id进行完成操作
     * @param dataId 资料id
     * @return Result
     */
    Result<?> complete(String dataId);

    /**
     * 根据资料id获取学习心得列表
     * @param page 分页参数
     * @param dataId 资料id
     * @return IPage<DataExperienceVo>
     */
    IPage<DataExperienceVo> getProblemCommentList(Page<DataExperience> page, String dataId);

    /**
     * 添加学习心得
     * @param dataExperience 学习心得信息
     * @return Result
     */
    Result<?> addDataExperience(DataExperience dataExperience);

    /**
     * 根据学习心得id删除学习心得
     * @param experienceId 学习心得id
     * @return Result
     */
    Result<?> delDataExperience(String experienceId);
}
