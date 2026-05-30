package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.DictItem;
import com.lims.manage.erp.vo.DictItemTreeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据表数据库访问层
 *
 * @author zhq
 * @since 2023-01-03 15:14:49
 */
public interface DictItemDao extends BaseMapper<DictItem> {

    /**
     * 通过字典主键获取字典项树结构
     * @param parentId  字典项值父主键
     * @return List<DictItemTreeModel>  字典项树结构
     **/
    List<DictItemTreeVo> getDictItemTree(@Param("parentId") String parentId);
}

