package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;
import com.lims.manage.erp.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * (TestControlledDocuments)表服务接口
 *
 * @author 丁连春
 * @since 2023-12-25 10:30:10
 */
public interface TestControlledDocumentsService extends IService<TestControlledDocumentsEntity> {

    /**
     * 查询列表
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    Result selectTestControlledDocumentsList(TestControlledDocumentsEntity testControlledDocumentsEntity);

    /**
     * 返回受控文件类型
     *
     * @return
     */
    Result getDocumentsBasic();

    /**
     * 受控文件详情
     *
     * @param id
     * @return
     */
    Result details(Integer id);


    /**
     * 新增 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    Result addTestControlledDocuments(TestControlledDocumentsEntity testControlledDocumentsEntity, MultipartFile[] file);


    /**
     * 更新 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    Result updateTestControlledDocuments(TestControlledDocumentsEntity testControlledDocumentsEntity, MultipartFile[] file);

    /**
     * 变更 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    Result changeRecord(TestControlledDocumentsEntity testControlledDocumentsEntity, MultipartFile[] file);

    /**
     * 变更记录
     *
     * @param testControlledDocuments
     * @return
     */
    Result getList(TestControlledDocumentsEntity testControlledDocuments);

    /**
     * 动态删除-软删除
     *
     * @param id
     * @return
     */
    Result delReportTemplate(Integer id);

}

