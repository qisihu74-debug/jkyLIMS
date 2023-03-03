package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataExperience;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.DataExperienceVo;
import com.lims.manage.erp.vo.DataInfoDetailVo;
import com.lims.manage.erp.vo.DataInfoVo;
import com.lims.manage.erp.vo.KsDataInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学习资料信息dao层接口
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface DataInfoDao extends BaseMapper<DataInfo> {

    /**
     * 获取优质分享者
     * @return 用户名称列表
     */
    List<Map<String,Object>> getQualityShare();

    /**
     * 我分享的学习资料/视频
     *
     * @param page     分页参数
     * @param dataInfo 查询条件
     * @return IPage<DataInfo>
     */
    IPage<DataInfoVo> pageList(Page page, @Param("dataInfo") DataInfo dataInfo);

    /**
     * 根据资料id获取资料信息
     * @param dataId 资料id
     * @return DataInfoVo
     */
    DataInfoVo getDataAuditInfo(@Param("dataId") String dataId);

    /**
     * 根据资料id获取资料信息详情
     * @param userId 用户id
     * @param dataId 资料id
     * @return DataInfoDetailVo
     */
    DataInfoDetailVo getDataInfoDetail(@Param("userId")String userId,@Param("dataId")String dataId);

    /**
     * 知识广场-学习资料列表
     * @param page 分页参数
     * @param dataInfo 查询条件
     * @return IPage<KsDataInfoVo>
     */
    IPage<KsDataInfoVo> getDataInfoList(Page page,@Param("dataInfo") DataInfo dataInfo);

    /**
     * 根据资料id增加资料点赞数量
     *
     * @param dataId 资料id
     * @return Boolean
     */
    Boolean addDataLikeNum(@Param("dataId") String dataId);

    /**
     * 根据资料id增加资料点踩数量
     *
     * @param dataId 资料id
     * @return Boolean
     */
    Boolean addDataTapNum(@Param("dataId") String dataId);

    /**
     * 根据资料id增加资料收藏数量
     *
     * @param dataId 资料id
     * @return Boolean
     */
    Boolean addDataCollectNum(@Param("dataId") String dataId);

    /**
     * 根据资料id增加资料完成数量
     *
     * @param dataId 资料id
     * @return Boolean
     */
    Boolean addDataCompleteNum(@Param("dataId") String dataId);

    /**
     * 根据资料id获取学习心得列表
     * @param page 分页参数
     * @param dataId 资料id
     * @return IPage<DataExperienceVo>
     */
    IPage<DataExperienceVo> getProblemCommentList(Page<DataExperience> page,@Param("dataId") String dataId);
}
