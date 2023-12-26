package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;
import com.lims.manage.erp.entity.TestControlledDocumentsRecordEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.mapper.TestControlledDocumentsMapper;
import com.lims.manage.erp.mapper.TestControlledDocumentsRecordMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestControlledDocumentsService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (TestControlledDocuments)表服务接口
 *
 * @author 丁连春
 * @since 2023-12-25 10:30:10
 */
@Service("testControlledDocumentsService")
public class TestControlledDocumentsServiceImpl extends ServiceImpl<TestControlledDocumentsMapper, TestControlledDocumentsEntity>
        implements TestControlledDocumentsService {
    @Resource
    private TestControlledDocumentsMapper testControlledDocumentsMapper;
    @Resource
    private TestControlledDocumentsRecordMapper testControlledDocumentsRecordMapper;
    @Autowired
    private TestCompanyDao testCompanyDao;

    @Override
    public Result selectTestControlledDocumentsList(TestControlledDocumentsEntity testControlledDocumentsEntity) {
        if (testControlledDocumentsEntity.getPageNum() == null || testControlledDocumentsEntity.getPageSize() == null) {
            return ResultUtil.error("分页参数不能为空");
        }
        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        // 名称模糊查询
        if (testControlledDocumentsEntity.getDocumentsName() != null) {
            queryWrapper.like(TestControlledDocumentsEntity::getDocumentsName, testControlledDocumentsEntity.getDocumentsName());
        }
        // 编号模糊查询
        if (testControlledDocumentsEntity.getDocumentsCode() != null) {
            queryWrapper.like(TestControlledDocumentsEntity::getDocumentsCode, testControlledDocumentsEntity.getDocumentsCode());
        }
        queryWrapper.orderByDesc(TestControlledDocumentsEntity::getCreateTime);
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(testControlledDocumentsEntity.getPageNum(), testControlledDocumentsEntity.getPageSize());
        List<TestControlledDocumentsEntity> list = testControlledDocumentsMapper.selectList(queryWrapper);
        PageInfo<TestControlledDocumentsEntity> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }


    /**
     * 返回受控文件类型
     *
     * @return
     */
    @Override
    public Result getDocumentsBasic() {
        Map<String, List<LabelValueVo>> map = new HashMap<>();
        /***
         * 受控文件基础类型
         */
        List<LabelValueVo> documentsBasic = new ArrayList<>();
        PageHelper.clearPage();
        List<TestInitDataEntity> ReturnBasisData = testCompanyDao.selectEntrustBasis();
        for (TestInitDataEntity testInitDataEntity : ReturnBasisData) {
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 18:
                    documentsBasic.add(labelValueVo);
                    break;
                default:
                    break;
            }
        }
        map.put("documentsBasic", documentsBasic);
        return ResultUtil.success(map);
    }

    @Override
    public Result details(Integer id) {
        // 文件详情
        return ResultUtil.success(testControlledDocumentsMapper.selectByPrimaryKey(id));

    }

    /**
     * 新增 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addTestControlledDocuments(TestControlledDocumentsEntity
                                                     testControlledDocumentsEntity, MultipartFile[] file) {
        // 效验信息。
        if (testControlledDocumentsEntity == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("参数为空");
        }
        // 效验 编号唯一性。
        LambdaQueryWrapper<TestControlledDocumentsEntity> documentsWrapper = new LambdaQueryWrapper<>();
        documentsWrapper.eq(TestControlledDocumentsEntity::getDocumentsCode, testControlledDocumentsEntity.getDocumentsCode());
        documentsWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        Integer count = testControlledDocumentsMapper.selectCount(documentsWrapper);
        if (count > 0) {
            return ResultUtil.error("编号不能重复");
        }
        StringBuffer fileNameBuffer = new StringBuffer();
        //附件存在上传附件到服务器
        if (file.length != 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.controlled_documents, multipartFile, fileCode + "." + strings[strings.length - 1]);
                // 截取 \\? 前数据
                String filePath = upload.split("\\?")[0];
                fileNameBuffer.append(filePath);
                fileNameBuffer.append(",");
            }
        }
        // 补充文件附件
        if (fileNameBuffer.length() >= 1) {
            testControlledDocumentsEntity.setDocumentsFileUri(fileNameBuffer.deleteCharAt(fileNameBuffer.length() - 1).toString());
        } else {
            testControlledDocumentsEntity.setDocumentsFileUri(null);
        }
        System.out.println("testControlledDocumentsEntity" + testControlledDocumentsEntity);
        // 对数据新增
        testControlledDocumentsMapper.insert(testControlledDocumentsEntity);
        return ResultUtil.success("新增成功");
    }

    /**
     * 更新 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateTestControlledDocuments(TestControlledDocumentsEntity
                                                        testControlledDocumentsEntity, MultipartFile[] file) {
        // 效验信息。
        if (testControlledDocumentsEntity == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("参数为空");
        }
        if (testControlledDocumentsEntity.getId() == null) {
            return ResultUtil.error("参数id不能为空");
        }
        // 查询详情
        TestControlledDocumentsEntity oldData = testControlledDocumentsMapper.selectById(testControlledDocumentsEntity.getId());
        if (oldData == null) {
            return ResultUtil.error("受控文件数据不存在");
        }
        // 效验 编号唯一性。
        LambdaQueryWrapper<TestControlledDocumentsEntity> documentsWrapper = new LambdaQueryWrapper<>();
        documentsWrapper.eq(TestControlledDocumentsEntity::getDocumentsCode, testControlledDocumentsEntity.getDocumentsCode());
        documentsWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        List<TestControlledDocumentsEntity> list = testControlledDocumentsMapper.selectList(documentsWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            if (list.size() >= 2) {
                return ResultUtil.error("编号不能重复");
            }
            // 更新时：比较唯一性
            for (TestControlledDocumentsEntity entity : list) {
                if (!entity.getId().equals(testControlledDocumentsEntity.getId())) {
                    return ResultUtil.error("编号不能重复");
                }
            }
        }
        // 文件新增。
        StringBuffer fileNameBuffer = new StringBuffer();
        //附件存在上传附件到服务器
        if (file.length != 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.controlled_documents, multipartFile, fileCode + "." + strings[strings.length - 1]);
                // 截取 \\? 前数据
                String filePath = upload.split("\\?")[0];
                fileNameBuffer.append(filePath);
                fileNameBuffer.append(",");
            }
        }
        // 补充文件附件
        if (fileNameBuffer.length() >= 1) {
            testControlledDocumentsEntity.setDocumentsFileUri(fileNameBuffer.deleteCharAt(fileNameBuffer.length() - 1).toString());
        }
        // 旧文件移除
        if (oldData.getDocumentsFileUri() != null && fileNameBuffer.length() >= 1) {
            String[] fileUrls = oldData.getDocumentsFileUri().split("\\/");
            // 进行移除。
            String oldFileUrl = fileUrls[fileUrls.length - 1];
            MinIoUtil.deleteFile(BucketsConst.controlled_documents, oldFileUrl);
        }
        testControlledDocumentsMapper.updateByPrimaryKeySelective(testControlledDocumentsEntity);
        return ResultUtil.success("更新成功");
    }

    /**
     * 更新 受控文件信息
     *
     * @param testControlledDocumentsEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result changeRecord(TestControlledDocumentsEntity testControlledDocumentsEntity, MultipartFile[] file) {
        // 效验信息。
        if (testControlledDocumentsEntity == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("参数为空");
        }
        if (testControlledDocumentsEntity.getId() == null) {
            return ResultUtil.error("参数id不能为空");
        }
        // 查询详情
        TestControlledDocumentsEntity oldData = testControlledDocumentsMapper.selectById(testControlledDocumentsEntity.getId());
        if (oldData == null) {
            return ResultUtil.error("受控文件数据不存在");
        }
        // 效验 编号唯一性。
        LambdaQueryWrapper<TestControlledDocumentsEntity> documentsWrapper = new LambdaQueryWrapper<>();
        documentsWrapper.eq(TestControlledDocumentsEntity::getDocumentsCode, testControlledDocumentsEntity.getDocumentsCode());
        documentsWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        List<TestControlledDocumentsEntity> list = testControlledDocumentsMapper.selectList(documentsWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            if (list.size() >= 2) {
                return ResultUtil.error("编号不能重复");
            }
            // 更新时：比较唯一性
            for (TestControlledDocumentsEntity entity : list) {
                if (!entity.getId().equals(testControlledDocumentsEntity.getId())) {
                    return ResultUtil.error("编号不能重复");
                }
            }
        }
        // 文件新增。
        StringBuffer fileNameBuffer = new StringBuffer();
        //附件存在上传附件到服务器
        if (file.length != 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.controlled_documents, multipartFile, fileCode + "." + strings[strings.length - 1]);
                // 截取 \\? 前数据
                String filePath = upload.split("\\?")[0];
                fileNameBuffer.append(filePath);
                fileNameBuffer.append(",");
            }
        }
        // 补充文件附件
        if (fileNameBuffer.length() >= 1) {
            testControlledDocumentsEntity.setDocumentsFileUri(fileNameBuffer.deleteCharAt(fileNameBuffer.length() - 1).toString());
        }
        // 旧文件移除
        if (oldData.getDocumentsFileUri() != null && fileNameBuffer.length() >= 1) {
            String[] fileUrls = oldData.getDocumentsFileUri().split("\\/");
            // 进行移除。
            String oldFileUrl = fileUrls[fileUrls.length - 1];
            MinIoUtil.deleteFile(BucketsConst.controlled_documents, oldFileUrl);
        }
        testControlledDocumentsMapper.updateByPrimaryKeySelective(testControlledDocumentsEntity);
        return ResultUtil.success("更新成功");
    }

    /**
     * 变更记录 实现分页
     *
     * @param testControlledDocuments
     * @return
     */
    @Override
    public Result getList(TestControlledDocumentsEntity testControlledDocuments) {
        if (testControlledDocuments == null) {
            return ResultUtil.error("缺少必填参数");
        }
        if (testControlledDocuments.getId() == null) {
            return ResultUtil.error("受控文件id不能为空");
        }
        if (testControlledDocuments.getPageNum() == null || testControlledDocuments.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        // 查询变更记录 列表
        LambdaQueryWrapper<TestControlledDocumentsRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 进行 查询分页。
        PageHelper.clearPage();
        PageHelper.startPage(testControlledDocuments.getPageNum(), testControlledDocuments.getPageSize());
        queryWrapper.eq(TestControlledDocumentsRecordEntity::getPid, testControlledDocuments.getId());
        List<TestControlledDocumentsRecordEntity> list = testControlledDocumentsRecordMapper.selectList(queryWrapper);
        PageInfo<TestControlledDocumentsRecordEntity> result = new PageInfo<>(list);
        return ResultUtil.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delReportTemplate(Integer id) {
        TestControlledDocumentsEntity record = new TestControlledDocumentsEntity();
        record.setId(id);
        record.setDelFlag(1);
        testControlledDocumentsMapper.updateByPrimaryKeySelective(record);
        return ResultUtil.success("删除成功");
    }
}
