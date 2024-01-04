package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.ReserveCodeEntity;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReserveCodeService {
    /**
     * 新增预留编号
     * @param reserveCodeEntity
     * @return
     */
    Result addReserveCode(ReserveCodeEntity reserveCodeEntity);

    /**
     * 批量删除编号
     * @param idList
     * @return
     */
    Result del(List<Long> idList);

    /**
     * 删除预留编号
     * @param id
     * @return
     */
    Result delete(Long id);

    /**
     * 编辑
     * @param reserveCodeEntity
     * @return
     */
    Result updateReserveCode(ReserveCodeEntity reserveCodeEntity);

    /**
     * 查询留号列表
     * @param reserveCodeEntity
     * @return
     */
    Result list(ReserveCodeEntity reserveCodeEntity);

    /**
     * 查询委托单号下拉
     * @param entrustmentNo
     * @return
     */
    Result getEntrustmentNoList(String entrustmentNo);

    /**
     * 导入预留编号
     * @param file
     * @return
     */
    Result importEquipments(MultipartFile file);

    /**
     * 查询当前最大的报告编号
     * @return
     */
    Result getMaxReportCode();

}
