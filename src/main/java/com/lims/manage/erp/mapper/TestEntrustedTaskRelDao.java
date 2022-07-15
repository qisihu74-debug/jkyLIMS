package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.TestEntrustedTaskRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/7/14 11:08
 */
@Component
@Mapper
public interface TestEntrustedTaskRelDao {


    /**
     * 根据委托单ID查询分配的任务科室ID,任务单主键
     */
    List<TestEntrustedTaskRelEntity> getDeptByEntrustIdList(@Param("entrustId") Long entrustId);

    /**
     * 批量增加
     * @return
     */
    int addList(@Param("lists")List<TestEntrustedTaskRelEntity> lists);

    /**
     *  根据任务id 查询 流转单 列表
     * @return
     */
    List<TestEntrustedTaskRelEntity> getTaskList(@Param("taskId") Long taskId);

    /**
     * 单个修改信息
     */
    int updateData(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity);

    /**
     * 进行删除 操作
     */
    int deletedData(@Param("id") Integer id);

    int addData(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity);

    /**
     * 通过委托单id 获取流转单信息集合
     * @param entrustId
     * @return
     */
    List<TestEntrustedTaskRelEntity> getEntrustTaskRelList(@Param("entrustId") Long entrustId);

    /**
     * 批量修改
     * @param list
     * @return
     */
    int updateEntrustedTaskRelEntityList(@Param("list") List<TestEntrustedTaskRelEntity> list);



}
