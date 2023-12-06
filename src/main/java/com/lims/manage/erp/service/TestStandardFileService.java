package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.StandardFileEntity;
import com.lims.manage.erp.entity.StandardMethodEntity;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 检验依据标准表(TestStandardFile)表服务接口
 *
 * @author makejava
 * @since 2022-03-09 10:22:55
 */
public interface TestStandardFileService extends IService<TestStandardFile> {


    Result addTestStandardFile(TestStandardFile testStandardFile);
    Result updTestStandardFile(TestStandardFile testStandardFile);
    Result delTestStandardFile(List<Long> idList);

    /**
     * 新增依据
     * @param standardFileEntity
     * @param standardFile
     * @return
     */
    Result addStandardFile(StandardFileEntity standardFileEntity, MultipartFile standardFile);

    /**
     * 新增检测方法
     * @param standardMethodEntity
     * @return
     */
    Result addStandardMethod(StandardMethodEntity standardMethodEntity);

    /**
     * 变更依据
     * @param standardFileEntity
     * @param standardFile
     * @return
     */
    Result updateStandard(StandardFileEntity standardFileEntity, MultipartFile standardFile);

    /**
     * 查看变更记录
     * @param pid
     * @return
     */
    Result getRecords(Integer pid);

    /**
     * 查询依据下方法列表
     * @param id
     * @return
     */
    Result getMethodList(Integer id);

    /**
     * 删除检测方法
     * @param id
     * @return
     */
    Result deleteMethod(Integer id);

}

