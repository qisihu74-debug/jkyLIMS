package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.entity.TestControlledDocumentsEntity;
import com.lims.manage.erp.entity.TestControlledDocumentsRecordEntity;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestControlledDocumentsService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private EntrustEntityMapper entityMapper;

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
        if (testControlledDocumentsEntity.getDocumentsCode() == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("编号参数不能为空");
        }
        // 效验 编号唯一性。
        LambdaQueryWrapper<TestControlledDocumentsEntity> documentsWrapper = new LambdaQueryWrapper<>();
        documentsWrapper.eq(TestControlledDocumentsEntity::getDocumentsCode, testControlledDocumentsEntity.getDocumentsCode());
        documentsWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        Integer count = testControlledDocumentsMapper.selectCount(documentsWrapper);
        if (count > 0) {
            return ResultUtil.error("编号不能重复");
        }
        // 获取数据type值:1、塞入 context 2、处理条数问题 （限制条数 <= 实际拥有条数）。
        if (testControlledDocumentsEntity.getFileType() != null) {
            // 返回字典数据
            List<TestInitDataEntity> entrustBasis = testCompanyDao.selectEntrustBasis();
            for (TestInitDataEntity dataEntity : entrustBasis) {
                if (testControlledDocumentsEntity.getFileType().equals(dataEntity.getId())) {
                    // 塞入字典Context
                    testControlledDocumentsEntity.setFileTypeContent(dataEntity.getName());
                    // 获取 dataEntity.getRemark(); 条数进行判断
                    if (dataEntity.getRemark() != null) {
                        // 限制条数
                        int items = Integer.parseInt(dataEntity.getRemark());
                        // 根据 类型 获取条数。
                        // 效验 编号唯一性。
                        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(TestControlledDocumentsEntity::getFileType, testControlledDocumentsEntity.getFileType());
                        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
                        // 实际拥有条数
                        Integer countType = testControlledDocumentsMapper.selectCount(queryWrapper);
                        // 限制条数 <= 实际拥有条数
                        if (items <= countType) {
                            return ResultUtil.error("创建失败、受控文件类型限制为 " + items);
                        }
                    }
                }
            }
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
        // 对数据新增补充
        testControlledDocumentsEntity.setIsAvailable("1");
        testControlledDocumentsEntity.setCreateTime(new Date());
        testControlledDocumentsEntity.setDelFlag(0);
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
        if (testControlledDocumentsEntity.getDocumentsCode() == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("编号参数不能为空");
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
        // 获取数据type值:1、塞入 context 2、处理条数问题 （限制条数 <= 实际拥有条数）。
        if (testControlledDocumentsEntity.getFileType() != null && !testControlledDocumentsEntity.getFileType().equals(oldData.getFileType())) {
            // 返回字典数据
            List<TestInitDataEntity> entrustBasis = testCompanyDao.selectEntrustBasis();
            for (TestInitDataEntity dataEntity : entrustBasis) {
                if (testControlledDocumentsEntity.getFileType().equals(dataEntity.getId())) {
                    // 塞入字典Context
                    testControlledDocumentsEntity.setFileTypeContent(dataEntity.getName());
                    // 获取 dataEntity.getRemark(); 条数进行判断
                    if (dataEntity.getRemark() != null) {
                        // 限制条数
                        int items = Integer.parseInt(dataEntity.getRemark());
                        // 根据 类型 获取条数。
                        // 效验 编号唯一性。
                        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(TestControlledDocumentsEntity::getFileType, testControlledDocumentsEntity.getFileType());
                        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
                        // 实际拥有条数
                        Integer countType = testControlledDocumentsMapper.selectCount(queryWrapper);
                        // 限制条数 <= 实际拥有条数
                        if (items <= countType) {
                            return ResultUtil.error("操作失败：当前类型限制为 " + countType);
                        }
                    }
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
        // 补充信息
        testControlledDocumentsEntity.setUpdateTime(new Date());
        testControlledDocumentsMapper.updateByPrimaryKeySelective(testControlledDocumentsEntity);
        return ResultUtil.success("更新成功");
    }

    /**
     * 变更 受控文件信息
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
        if (testControlledDocumentsEntity.getDocumentsCode() == null) {
            // 抛出 必填项不为空
            return ResultUtil.error("编号参数不能为空");
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
        // 获取数据type值:1、塞入 context 2、处理条数问题 （限制条数 <= 实际拥有条数）。
        if (testControlledDocumentsEntity.getFileType() != null && !testControlledDocumentsEntity.getFileType().equals(oldData.getFileType())) {
            // 返回字典数据
            List<TestInitDataEntity> entrustBasis = testCompanyDao.selectEntrustBasis();
            for (TestInitDataEntity dataEntity : entrustBasis) {
                if (testControlledDocumentsEntity.getFileType().equals(dataEntity.getId())) {
                    // 塞入字典Context
                    testControlledDocumentsEntity.setFileTypeContent(dataEntity.getName());
                    // 获取 dataEntity.getRemark(); 条数进行判断
                    if (dataEntity.getRemark() != null) {
                        // 限制条数
                        int items = Integer.parseInt(dataEntity.getRemark());
                        // 根据 类型 获取条数。
                        // 效验 编号唯一性。
                        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(TestControlledDocumentsEntity::getFileType, testControlledDocumentsEntity.getFileType());
                        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
                        // 实际拥有条数
                        Integer countType = testControlledDocumentsMapper.selectCount(queryWrapper);
                        // 限制条数 <= 实际拥有条数
                        if (items <= countType) {
                            return ResultUtil.error("操作失败：当前类型限制为 " + countType);
                        }
                    }
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
        // 点击变更按钮：文件发生变动（旧的基础数据 移到变更记录中）
        if (oldData.getDocumentsFileUri() != null && fileNameBuffer.length() >= 1) {
            TestControlledDocumentsRecordEntity testControlledDocumentsRecordEntity = new TestControlledDocumentsRecordEntity(oldData);
            testControlledDocumentsRecordEntity.setId(null);
            testControlledDocumentsRecordEntity.setPid(oldData.getId());
            // 文件过期时间 等于变更 实施时间。
            if (testControlledDocumentsEntity.getUsageTime() != null) {
                testControlledDocumentsRecordEntity.setExpirationTime(testControlledDocumentsEntity.getUsageTime());
            }
            testControlledDocumentsRecordMapper.insert(testControlledDocumentsRecordEntity);
        }
        // 基础受控文件的 实施时间 = 变更记录中 最后一条的 实施时间。
        if (testControlledDocumentsEntity.getUsageTime() != null) {
            // 查询变更记录 列表
            LambdaQueryWrapper<TestControlledDocumentsRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            // 进行 查询分页。
            PageHelper.clearPage();
            queryWrapper.eq(TestControlledDocumentsRecordEntity::getPid, testControlledDocumentsEntity.getId());
            queryWrapper.orderByDesc(TestControlledDocumentsRecordEntity::getId);
            queryWrapper.last("limit 1");
            TestControlledDocumentsRecordEntity entity = testControlledDocumentsRecordMapper.selectOne(queryWrapper);
            if (entity != null) {
                // 变更记录不为空 : 基础受控文件的 实施时间 = 变更记录中 最后一条的 实施时间
                entity.setExpirationTime(testControlledDocumentsEntity.getUsageTime());
                testControlledDocumentsRecordMapper.updateByPrimaryKeySelective(entity);
            }
        }
        testControlledDocumentsMapper.updateByPrimaryKeySelective(testControlledDocumentsEntity);
        return ResultUtil.success("变更成功");
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
        queryWrapper.orderByAsc(TestControlledDocumentsRecordEntity::getId);
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

    /**
     * 返回桶信息 与 附件内容。
     *
     * @param id
     * @param typeId
     * @return
     */
    @Override
    public HashMap<String, String> returnBucketInformation(Long id, Integer typeId) throws ParseException {
        HashMap<String, String> map = new HashMap<>();
        if (typeId == 58) {
            // 任务单模板
        }
        switch (typeId) {
            case 58: // 调用方法处理任务单模板
                map = methodTaskTemplate(id, typeId);
                break;
            case 59: // 调用方法处理任务单模板
                map = methodEntrustTemplate(id, typeId);
                break;
            default:
                break;
        }
        return map;
    }

    /**
     * 任务单模板返回信息
     *
     * @param id
     * @return
     */
    public HashMap<String, String> methodTaskTemplate(Long id, Integer typeId) throws ParseException {
        // 查询任务单详情信息
        // 获取任务单下单时间 进行比较
        TaskTestEntity taskDetails = taskMapper.getTaskOrderTime(id);
        if (taskDetails == null) {
            return null;
        }
        // 获取下单时间
        if (taskDetails.getOrderTime() == null) {
            return null;
        }
        //实现将字符串转成⽇期类型
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 获取下单时间 年月日即可。
        Date orderTime = dateFormat.parse(dateFormat.format(taskDetails.getOrderTime()));
        // 查询受控文件数据
        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestControlledDocumentsEntity::getFileType, typeId);
        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        // 根据 typeId 类型返回的 基础受控文件集合
        List<TestControlledDocumentsEntity> controlledDocuments = testControlledDocumentsMapper.selectList(queryWrapper);
        // 根据 typeId 类型返回的变更记录信息
        List<TestControlledDocumentsRecordEntity> changeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(controlledDocuments)) {
            for (TestControlledDocumentsEntity entity : controlledDocuments) {
                // 获取变更记录
                LambdaQueryWrapper<TestControlledDocumentsRecordEntity> queryWrapperRecord = new LambdaQueryWrapper<>();
                queryWrapperRecord.eq(TestControlledDocumentsRecordEntity::getPid, entity.getId());
                List<TestControlledDocumentsRecordEntity> list = testControlledDocumentsRecordMapper.selectList(queryWrapperRecord);
                if (CollectionUtil.isNotEmpty(list)) {
                    changeList.addAll(list);
                }
            }
            // 优先选择 基础受控文件
            for (TestControlledDocumentsEntity entity : controlledDocuments) {
                //  测试此日期是否在指定日期之后。 :
                //      任务单下单时间 在 基础受控文件实施时间 后面 符合条件 || 实施时间 = 下单时间
                //      任务单下单时间 2023-12-28  受控文件实施时间 2023-08-01
                // 获取实施时间 年月日即可。
                Date usageTime = dateFormat.parse(dateFormat.format(entity.getUsageTime()));
                if ((orderTime.after(usageTime) || orderTime.equals(usageTime)) && entity.getDocumentsFileUri() != null) {
                    // 截取数据
                    String[] strings = entity.getDocumentsFileUri().split("\\/");
                    String fileContext = strings[strings.length - 1];
                    HashMap<String, String> map = new HashMap<>();
                    map.put("bucketName", BucketsConst.controlled_documents);
                    map.put("content", fileContext);
                    // 最新的受控文件
                    map.put("status", "0");
                    return map;
                }
            }

        }
        // 其次处理 变更记录中 实施时间 与 过期时间的参数内容
        if (CollectionUtil.isNotEmpty(changeList)) {
            for (TestControlledDocumentsRecordEntity entity : changeList) {
                // 下单时间 在 实施时间之后 && 下单时间 在 过期时间 之前
                // 获取实施时间 年月日即可。 || 实施时间 = 下单时间
                Date usageTime = dateFormat.parse(dateFormat.format(entity.getUsageTime()));
                // 过期时间 年月日即可。
                Date expirationTime = dateFormat.parse(dateFormat.format(entity.getExpirationTime()));
                if ((orderTime.after(usageTime) || orderTime.equals(usageTime)) && orderTime.before(expirationTime)) {
                    // 截取数据
                    String[] strings = entity.getDocumentsFileUri().split("\\/");
                    String fileContext = strings[strings.length - 1];
                    HashMap<String, String> map = new HashMap<>();
                    map.put("bucketName", BucketsConst.controlled_documents);
                    map.put("content", fileContext);
                    // 最新的受控文件
                    map.put("status", "1");
                    return map;
                }
            }
        }
        return null;
    }

    /**
     * 委托单模板处理
     *
     * @param id
     * @return
     */
    public HashMap<String, String> methodEntrustTemplate(Long id, Integer typeId) throws ParseException {
        PageHelper.clearPage();
        // 通过委托ID 委托单信息 → test_entrusted_info
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(id);
        if (entrustAddVo == null) {
            return null;
        }
        // 获取受理时间
        if (entrustAddVo.getAcceptanceDate() == null) {
            return null;
        }
        //实现将字符串转成⽇期类型
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 获取受理时间 年月日即可。
        Date acceptanceDate = dateFormat.parse(dateFormat.format(entrustAddVo.getAcceptanceDate()));
        // 查询受控文件数据
        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestControlledDocumentsEntity::getFileType, typeId);
        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        // 根据 typeId 类型返回的 基础受控文件集合
        List<TestControlledDocumentsEntity> controlledDocuments = testControlledDocumentsMapper.selectList(queryWrapper);
        // 根据 typeId 类型返回的变更记录信息
        List<TestControlledDocumentsRecordEntity> changeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(controlledDocuments)) {
            for (TestControlledDocumentsEntity entity : controlledDocuments) {
                // 获取变更记录
                LambdaQueryWrapper<TestControlledDocumentsRecordEntity> queryWrapperRecord = new LambdaQueryWrapper<>();
                queryWrapperRecord.eq(TestControlledDocumentsRecordEntity::getPid, entity.getId());
                List<TestControlledDocumentsRecordEntity> list = testControlledDocumentsRecordMapper.selectList(queryWrapperRecord);
                if (CollectionUtil.isNotEmpty(list)) {
                    changeList.addAll(list);
                }
            }
            // 优先选择 基础受控文件
            for (TestControlledDocumentsEntity entity : controlledDocuments) {
                //  测试此日期是否在指定日期之后。 :
                //      受理时间 在 基础受控文件实施时间 后面 符合条件 || 实施时间 = 下单时间
                //      受理时间 2023-12-28  受控文件实施时间 2023-08-01
                // 获取实施时间 年月日即可。
                Date usageTime = dateFormat.parse(dateFormat.format(entity.getUsageTime()));
                if ((acceptanceDate.after(usageTime) || acceptanceDate.equals(usageTime)) && entity.getDocumentsFileUri() != null) {
                    // 截取数据
                    String[] strings = entity.getDocumentsFileUri().split("\\/");
                    String fileContext = strings[strings.length - 1];
                    HashMap<String, String> map = new HashMap<>();
                    map.put("bucketName", BucketsConst.controlled_documents);
                    map.put("content", fileContext);
                    // 最新的受控文件
                    map.put("status", "0");
                    return map;
                }
            }

        }
        // 其次处理 变更记录中 实施时间 与 过期时间的参数内容
        if (CollectionUtil.isNotEmpty(changeList)) {
            for (TestControlledDocumentsRecordEntity entity : changeList) {
                // 受理时间 在 实施时间之后 && 下单时间 在 过期时间 之前
                // 获取实施时间 年月日即可。 || 实施时间 = 下单时间
                Date usageTime = dateFormat.parse(dateFormat.format(entity.getUsageTime()));
                // 过期时间 年月日即可。
                Date expirationTime = dateFormat.parse(dateFormat.format(entity.getExpirationTime()));
                if ((acceptanceDate.after(usageTime) || acceptanceDate.equals(usageTime)) && acceptanceDate.before(expirationTime)) {
                    // 截取数据
                    String[] strings = entity.getDocumentsFileUri().split("\\/");
                    String fileContext = strings[strings.length - 1];
                    HashMap<String, String> map = new HashMap<>();
                    map.put("bucketName", BucketsConst.controlled_documents);
                    map.put("content", fileContext);
                    // 最新的受控文件
                    map.put("status", "1");
                    return map;
                }
            }
        }
        return null;
    }

    /**
     * 在线模板数据查看
     *
     * @param type
     * @return
     */
    @Override
    public Result getTemplateData(String type) {
        // 获取受控文件数据
        LambdaQueryWrapper<TestControlledDocumentsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestControlledDocumentsEntity::getDelFlag, 0);
        // 类型名称查询
        queryWrapper.eq(TestControlledDocumentsEntity::getFileTypeContent, type);
        queryWrapper.orderByDesc(TestControlledDocumentsEntity::getCreateTime);
        // 查看指定数据
        queryWrapper.select(TestControlledDocumentsEntity::getDocumentsName, TestControlledDocumentsEntity::getDocumentsFileUri);
        // 进行 查询分页。
        PageHelper.clearPage();
        List<TestControlledDocumentsEntity> list = testControlledDocumentsMapper.selectList(queryWrapper);
        return ResultUtil.success(list);
    }
}
