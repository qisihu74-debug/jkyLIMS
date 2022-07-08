package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.EntrustFileTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/7/8 9:59
 * 文件中间表 test_entrust_file_rel
 */
@Component
@Mapper
public interface EntrustFileTableDao {

    /**
     * add 委托单附件
     * @param entrustFileTableEntity
     * @return
     */
    int insertEntrustFileTableEntity(EntrustFileTableEntity entrustFileTableEntity);

    /**
     * 根据委托单id 查询文件集合。
     * @param entrustId
     * @return
     */
    List<EntrustFileTableEntity> getEntrustFileTableEntityList(Long entrustId);

    /**
     * 根据附件id 查询详情。
     * @param id
     * @return
     */
    EntrustFileTableEntity getEntrustFileTableEntityId(Integer id);

    /**
     * 删除单个文件id
     * @param id
     * @return
     */
    int deleteEntrustFileTableEntity(Integer id);
}
