package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DictItem;
import com.lims.manage.erp.vo.DictItemTreeVo;

import java.util.List;

/**
 * @Description 字典数据业务接口
 * @Author zhq
 * @CreateTime 2023-01-03 14:46
 */
public interface DictItemService extends IService<DictItem> {

    /**
     * 通过字典主键获取字典项树结构
     * @param parentId  字典项值父主键
     * @return List<DictItemTreeModel>  字典项树结构
     **/
    List<DictItemTreeVo> getDictItemTree(String parentId);
}

