package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.constant.CommonConstant;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.DataInfoDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.DataExperienceVo;
import com.lims.manage.erp.vo.DataInfoVo;
import com.lims.manage.erp.vo.KsDataInfoVo;
import com.lims.manage.erp.vo.UserOperationStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * 学习资料信息业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class DataInfoServiceImpl extends ServiceImpl<DataInfoDao, DataInfo> implements DataInfoService {

    @Resource
    DataAuditRecordService dataAuditRecordService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    DataRecordService dataRecordService;

    @Resource
    UserOperationRecordService userOperationRecordService;

    @Resource
    DataExperienceService dataExperienceService;

    @Resource
    LabelInfoService labelInfoService;

    @Override
    public List<Map<String, Object>> getQualityShare() {
        return baseMapper.getQualityShare();
    }

    @Override
    public IPage<DataInfoVo> pageList(Page page, DataInfo dataInfo) {
        //根据传入的标签名称获取对应的标签
        if (dataInfo != null && StringUtils.isNotEmpty(dataInfo.getDataLabel())) {
            //获取标签列表
            List<LabelInfo> labelInfos = labelInfoService.list(Wrappers.<LabelInfo>lambdaQuery().like(LabelInfo::getLabelContent, dataInfo.getDataLabel()).select(LabelInfo::getLabelId));
            if (ListUtils.isEmpty(labelInfos)) {
                return null;
            }
            //根据标签和对应的查询条件查询
            dataInfo.setLabelIdList(labelInfos);
        }
        return baseMapper.pageList(page, dataInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addDataInfo(DataInfo dataInfo, MultipartFile file) {
        //附件存在上传附件到服务器
        if (file != null) {
            //上传文件
            Map<String, Object> fileMap = uploadFile(file);
            if (MapUtils.isNotEmpty(fileMap)) {
                dataInfo.setDataUrl(MapUtils.getString(fileMap, "fileUrl"));
                dataInfo.setFileName(MapUtils.getString(fileMap, "fileName"));
                dataInfo.setFileSuffix(MapUtils.getString(fileMap, "fileSuffix"));
                //设置默认文件类型是学习资料
                dataInfo.setDataType(CommonConstant.DATA_TYPE_LEARN_DATA);
                //如果有视频封面图地址
                if (fileMap.containsKey("videoImgUrl")) {
                    //更改文件类型
                    dataInfo.setDataType(CommonConstant.DATA_TYPE_LEARN_VIDEO);
                    //设置视频封面图地址
                    dataInfo.setDataVideoUrl(MapUtils.getString(fileMap, "videoImgUrl"));
                }
                if (fileMap.containsKey("fileImgUrl")) {
                    dataInfo.setDataImgUrl(MapUtils.getString(fileMap, "fileImgUrl"));
                }
                dataInfo.setUpTime(new Date());
            } else {
                return ResultUtil.error("上传资料保存出错");
            }
        }
        //根据标签名称获取标签对应的id
        dataInfo.setDataLabel(labelInfoService.getLabelId(dataInfo.getDataLabel()));
        dataInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        dataInfo.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        dataInfo.setCreateTime(new Date());
        this.save(dataInfo);

        //新增默认审核信息
        DataAuditRecord dataAuditRecord = new DataAuditRecord();
        //dataAuditRecord.setUserId(dataInfo.getUserId());
        dataAuditRecord.setDataId(dataInfo.getDataId());
        dataAuditRecord.setAuditStatus(0);
        dataAuditRecord.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        dataAuditRecord.setCreateTime(new Date());
        dataAuditRecordService.save(dataAuditRecord);
        return ResultUtil.success("分享学习资料成功");
    }

    @Override
    public DataInfo getDataInfoById(String dataId) {
        return baseMapper.getDataInfoById(dataId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> editDataInfo(DataInfo dataInfo, MultipartFile file) {
        //根据资料id获取资料审核结果
        DataAuditRecord dataAuditRecord = dataAuditRecordService.getOne(Wrappers.<DataAuditRecord>lambdaQuery().eq(DataAuditRecord::getDataId, dataInfo.getDataId()));
        if (dataAuditRecord != null && dataAuditRecord.getAuditStatus() != 0) {
            return ResultUtil.error(500, "该资料已审核，无法修改");
        }
        //附件存在上传附件到服务器
        if (file != null) {
            //上传文件
            Map<String, Object> fileMap = uploadFile(file);
            if (MapUtils.isNotEmpty(fileMap)) {
                dataInfo.setDataUrl(MapUtils.getString(fileMap, "fileUrl"));
                dataInfo.setFileName(MapUtils.getString(fileMap, "fileName"));
                dataInfo.setFileSuffix(MapUtils.getString(fileMap, "fileSuffix"));
                //设置默认文件类型是学习资料
                dataInfo.setDataType(CommonConstant.DATA_TYPE_LEARN_DATA);
                //如果有视频封面图地址
                if (fileMap.containsKey("videoImgUrl")) {
                    //更改文件类型
                    dataInfo.setDataType(CommonConstant.DATA_TYPE_LEARN_VIDEO);
                    //设置视频封面图地址
                    dataInfo.setDataVideoUrl(MapUtils.getString(fileMap, "videoImgUrl"));
                }
                if (fileMap.containsKey("fileImgUrl")) {
                    dataInfo.setDataImgUrl(MapUtils.getString(fileMap, "fileImgUrl"));
                }
                dataInfo.setUpTime(new Date());
            } else {
                return ResultUtil.error("上传资料保存出错");
            }
        }

        //获取资料原本的资料标签
        DataInfo oldDataInfo = this.getById(dataInfo.getDataId());
        //从旧标签列表删除新的标签列表 得到旧标签中无用的标签
        List<String> oldList = new ArrayList<>(Arrays.asList(oldDataInfo.getDataLabel().split(",")));
        String dataLabel = labelInfoService.getLabelId(dataInfo.getDataLabel());
        oldList.removeAll(Arrays.asList(dataLabel.split(",")));

        //根据新的标签id列表进行转换为标签id
        labelInfoService.delLabelInfo(oldList);

        //根据标签名称获取标签对应的id
        dataInfo.setDataLabel(dataLabel);
        dataInfo.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        dataInfo.setUpdateBy(ShiroUtils.getUserInfo().getUsername());
        dataInfo.setUpdateTime(new Date());
        this.updateById(dataInfo);
        return ResultUtil.success("修改学习资料成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> delDataInfo(String dataId) {
        //根据学习资料id查询学习资料信息
        DataInfo dataInfo = this.getById(dataId);
        if (dataInfo != null) {
            //删除上传的学习资料
            if (!StringUtils.isEmpty(dataInfo.getDataUrl())) {
                String fileName = dataInfo.getDataUrl().substring(dataInfo.getDataUrl().lastIndexOf("/") + 1, dataInfo.getDataUrl().indexOf("." + dataInfo.getFileSuffix())) + "." + dataInfo.getFileSuffix();
                MinIoUtil.deleteFile(BucketsConst.data_file, fileName);
            }
            //删除上传的学习视频封面图
            if (!StringUtils.isEmpty(dataInfo.getDataVideoUrl())) {
                String fileName = dataInfo.getDataVideoUrl().substring(dataInfo.getDataVideoUrl().lastIndexOf("/") + 1, dataInfo.getDataVideoUrl().indexOf(".jpg")) + ".jpg";
                MinIoUtil.deleteFile(BucketsConst.data_file, fileName);
            }
            //删除上传的学习资料图
            if (!StringUtils.isEmpty(dataInfo.getDataImgUrl())) {
                String fileName = dataInfo.getDataImgUrl().substring(dataInfo.getDataImgUrl().lastIndexOf("/") + 1, dataInfo.getDataImgUrl().indexOf(".jpg")) + ".jpg";
                MinIoUtil.deleteFile(BucketsConst.data_file, fileName);
            }
            this.removeById(dataId);

            //根据记录标签来进行自定义标签的删除
            labelInfoService.delLabelInfo(Arrays.asList(dataInfo.getDataLabel().split(",")));
            return ResultUtil.success("学习资料删除成功");
        } else {
            return ResultUtil.error("没有找到对应学习资料");
        }
    }

    @Override
    public Result<?> getDataAuditInfo(String dataId) {
        return ResultUtil.success(baseMapper.getDataAuditInfo(dataId));
    }

    @Override
    public Result<?> getDataInfoDetail(String dataId) {
        //根据用户id获取最新的一条浏览记录
        LambdaQueryWrapper<UserOperationRecord> userOperationWrapper = Wrappers.lambdaQuery();
        userOperationWrapper.eq(UserOperationRecord::getUserId, ShiroUtils.getUserInfo().getUserId().toString());
        userOperationWrapper.eq(UserOperationRecord::getEventType, CommonConstant.USER_OPERATION_EVENT_TYPE_DATA);
        userOperationWrapper.eq(UserOperationRecord::getEventId, dataId);
        userOperationWrapper.eq(UserOperationRecord::getOperationType, CommonConstant.USER_OPERATION_TYPE_VIEW);
        //userOperationWrapper.orderByDesc(UserOperationRecord::getCreateTime);
        //userOperationWrapper.last("limit 1");
        UserOperationRecord userOperationRecord = userOperationRecordService.getOne(userOperationWrapper);
        //如果浏览记录为空 则添加浏览记录
        if (userOperationRecord == null) {
            //添加用户浏览记录
            userOperationRecord = new UserOperationRecord();
            userOperationRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
            userOperationRecord.setEventType(CommonConstant.USER_OPERATION_EVENT_TYPE_DATA);
            userOperationRecord.setEventId(dataId);
            userOperationRecord.setOperationType(CommonConstant.USER_OPERATION_TYPE_VIEW);
            userOperationRecord.setCreateBy(ShiroUtils.getUserInfo().getUsername());
            userOperationRecord.setCreateTime(new Date());
            userOperationRecordService.save(userOperationRecord);

        } else {
            //更新资料浏览时间
            userOperationRecord.setCreateTime(new Date());
            userOperationRecordService.updateById(userOperationRecord);
        }
        //返回资料详情信息
        return ResultUtil.success(baseMapper.getDataInfoDetail(ShiroUtils.getUserInfo().getUserId().toString(), dataId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> auditData(DataAuditRecord dataAuditRecord) {

        //根据学习资料id查询学习资料信息
        DataInfo dataInfo = this.getById(dataAuditRecord.getDataId());
        if (dataInfo != null) {
            LambdaQueryWrapper<DataAuditRecord> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(DataAuditRecord::getDataId, dataInfo.getDataId());
            //wrapper.eq(DataAuditRecord::getUserId, ShiroUtils.getUserInfo().getUserId().toString());
            DataAuditRecord record = dataAuditRecordService.getOne(wrapper);
            if (record != null) {
                if (record.getAuditStatus() != 0) {
                    return ResultUtil.error(500, "该记录已审核");
                }
                dataAuditRecord.setId(record.getId());
            } else {
                return ResultUtil.error(500, "该记录没有找到未审核记录");
            }
            dataAuditRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
            dataAuditRecordService.updateById(dataAuditRecord);
            //如果是审核通过
            if (CommonConstant.AUDIT_STATUS_1.equals(dataAuditRecord.getAuditStatus())) {
                //给对应的用户增加学习积分
                String ruleId;
                String ruleCacheId;
                //根据对应资料类型设置对应规则id和缓存id
                if (CommonConstant.DATA_TYPE_LEARN_DATA.equals(dataInfo.getDataType())) {
                    ruleId = CommonConstant.INTEGRAL_RULE_UPLOAD_LEARN_DATA;
                    ruleCacheId = CommonConstant.CACHE_INTEGRAL_RULE_UPLOAD_LEARN_DATA;
                } else {
                    ruleId = CommonConstant.INTEGRAL_RULE_UPLOAD_LEARN_VIDEO;
                    ruleCacheId = CommonConstant.CACHE_INTEGRAL_RULE_UPLOAD_LEARN_VIDEO;
                }
                //添加用户积分和获取积分记录
                Integer resultInt = userIntegralRecordService.addUserIntegralRecord(ruleId, ruleCacheId, dataInfo.getUserId(), CommonConstant.USER_OPERATION_EVENT_TYPE_DATA, dataAuditRecord.getDataId());

                //添加默认学习资料动态记录
                DataRecord dataRecord = new DataRecord();
                dataRecord.setDataId(dataInfo.getDataId());
                dataRecordService.save(dataRecord);
            } else {
                //审核驳回删除用户上传的学习资料
                //删除上传的学习资料
                if (!StringUtils.isEmpty(dataInfo.getDataUrl())) {
                    String fileName = dataInfo.getDataUrl().substring(dataInfo.getDataUrl().lastIndexOf("/") + 1, dataInfo.getDataUrl().indexOf("." + dataInfo.getFileSuffix())) + "." + dataInfo.getFileSuffix();
                    MinIoUtil.deleteFile(BucketsConst.data_file, fileName);
                    dataInfo.setDataUrl(null);
                }
                //删除上传的学习视频封面图
                if (!StringUtils.isEmpty(dataInfo.getDataVideoUrl())) {
                    String fileName = dataInfo.getDataVideoUrl().substring(dataInfo.getDataVideoUrl().lastIndexOf("/") + 1, dataInfo.getDataVideoUrl().indexOf(".jpg")) + ".jpg";
                    MinIoUtil.deleteFile(BucketsConst.data_file, fileName);
                    dataInfo.setDataVideoUrl(null);
                }
                //学习资料文件删除后 更新学习资料对应的url信息
                this.updateById(dataInfo);
            }
            return ResultUtil.success("操作成功");
        } else {
            return ResultUtil.error("没有找到对应学习资料");
        }
    }

    /**
     * 上传学习资料
     *
     * @param multipartFile 文件信息
     * @return 文件url
     */
    public Map<String, Object> uploadFile(MultipartFile multipartFile) {
        Map<String, Object> map = new HashMap<>(5);
        if (multipartFile != null) {
            String name = multipartFile.getOriginalFilename();
            String[] strings = name.split("\\.");
            String upload = MinIoUtil.upload(BucketsConst.data_file, multipartFile, GenID.getID() + "." + strings[strings.length - 1]);

            map.put("fileName", name);
            //判断是否是视频
            String fileSuffix = strings[strings.length - 1].toUpperCase();
            map.put("fileSuffix", fileSuffix.toLowerCase());
            if (!StringUtils.isEmpty(upload)) {
                String uploadUrl = upload.substring(0, upload.indexOf("?"));
                map.put("fileUrl", uploadUrl);
            }
            try {
                InputStream inStream = multipartFile.getInputStream();
                List<BufferedImage> wordToImg = null;
                switch (fileSuffix) {
                    case "DOC":
                    case "DOCX":
                    case "TXT":
                        //word文档和txt文本转换
                        wordToImg = FileToImgUtils.wordToImg(inStream);
                        break;
                    case "PPT":
                    case "PPTX":
                        wordToImg = FileToImgUtils.pptToImg(inStream);
                        break;
                    case "PDF":
                        wordToImg = FileToImgUtils.pdfToImg(inStream);
                        break;
                    case "XLS":
                    case "XLSX":
                        wordToImg = FileToImgUtils.excelToImg(inStream);
                        break;
                    default:
                        break;
                }
                if (wordToImg != null) {
                    BufferedImage mergeImage = FileToImgUtils.mergeImage(false, wordToImg);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();//io流

                    ImageIO.write(mergeImage, "jpg", baos);
                    //转换为MultipartFile
                    MultipartFile multipartFile1 = new MockMultipartFile(GenID.getID() + ".jpg", baos.toByteArray());
                    String url = MinIoUtil.upload(BucketsConst.data_file, multipartFile1, GenID.getID() + ".jpg");
                    String uploadUrl = url.substring(0, url.indexOf("?"));
                    map.put("fileImgUrl", uploadUrl);
                }
                if ("MP4".equals(fileSuffix)) {
                    //视频截图
                    MultipartFile imgFile = FileImageUtil.executeCodecs(upload, String.valueOf(System.currentTimeMillis()));
                    //视频封面图上传minio服务器
                    Map<String, Object> imgMap = uploadFile(imgFile);
                    if (MapUtils.isNotEmpty(imgMap)) {
                        map.put("videoImgUrl", MapUtils.getString(imgMap, "fileUrl"));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    @Override
    public IPage<KsDataInfoVo> getDataInfoList(Page page, DataInfo dataInfo) {
        //根据传入的标题获取对应的标签
        if (dataInfo != null && StringUtils.isNotEmpty(dataInfo.getDataTitle())) {
            //获取标签列表
            List<LabelInfo> labelInfos = labelInfoService.list(Wrappers.<LabelInfo>lambdaQuery().like(LabelInfo::getLabelContent, dataInfo.getDataTitle()).select(LabelInfo::getLabelId));
            //根据标签和对应的查询条件查询
            dataInfo.setLabelIdList(labelInfos);
        }
        return baseMapper.getDataInfoList(page, dataInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> like(String dataId) {
        return userOperationComment(dataId, CommonConstant.USER_OPERATION_TYPE_LIKE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> tap(String dataId) {
        return userOperationComment(dataId, CommonConstant.USER_OPERATION_TYPE_TAP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> collect(String dataId) {
        return userOperationComment(dataId, CommonConstant.USER_OPERATION_TYPE_COLLECT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> complete(String dataId) {
        return userOperationComment(dataId, CommonConstant.USER_OPERATION_TYPE_COMPLETE);
    }

    /**
     * 用户对资料进行点赞或点踩操作
     *
     * @param dataId        资料id
     * @param operationType 操作类型
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<?> userOperationComment(String dataId, String operationType) {
        //获取当前用户的点赞或点踩状态
        UserOperationRecord userOperationRecord = new UserOperationRecord();
        userOperationRecord.setEventType(CommonConstant.USER_OPERATION_EVENT_TYPE_DATA);
        userOperationRecord.setEventId(dataId);
        UserOperationStatusVo userOperationStatus = userOperationRecordService.getUserOperationStatus(userOperationRecord);
        //如果有用户操作记录并且有点赞或点踩操作 则直接返回
        boolean likeStatus = false;
        boolean collectStatus = false;
        boolean completeStatus = false;
        if (userOperationStatus != null) {
            if (userOperationStatus.getLikeStatus() != null || userOperationStatus.getTapStatus() != null) {
                //如果已经进行点赞则直接返回已点赞
                likeStatus = true;
            }
            if (userOperationStatus.getCollectStatus() != null) {
                collectStatus = true;
            }
            if (userOperationStatus.getCompleteStatus() != null) {
                completeStatus = true;
            }
        }
        //根据不同的操作类型进行默认值
        boolean operationResult;
        //根据当前评论id获取评论点赞记录
        DataRecord dataRecord = dataRecordService.getById(dataId);
        if (dataRecord == null) {
            return ResultUtil.error(500, "资料记录异常");
        }
        switch (operationType) {
            case CommonConstant.USER_OPERATION_TYPE_LIKE:
                if (likeStatus) {
                    //如果已经进行点赞则直接返回已点赞
                    return ResultUtil.error(500, "用户已进行点赞或点踩操作");
                }
                //添加点赞数量
                operationResult = baseMapper.addDataLikeNum(dataId);
                //设置点赞或点踩数量及对应点赞、点踩状态
                dataRecord.setLikeNum(dataRecord.getLikeNum() + 1);
                dataRecord.setLikeStatus(true);
                break;
            case CommonConstant.USER_OPERATION_TYPE_TAP:
                if (likeStatus) {
                    //如果已经进行点赞则直接返回已点赞
                    return ResultUtil.error(500, "用户已进行点赞或点踩操作");
                }
                //添加点踩数量
                operationResult = baseMapper.addDataTapNum(dataId);
                //设置点赞或点踩数量及对应点赞、点踩状态
                dataRecord.setTapNum(dataRecord.getTapNum() + 1);
                dataRecord.setTapStatus(true);
                break;
            case CommonConstant.USER_OPERATION_TYPE_COLLECT:
                if (collectStatus) {
                    return ResultUtil.error(500, "用户已进行收藏操作");
                }
                //添加收藏数量
                operationResult = baseMapper.addDataCollectNum(dataId);
                //设置收藏数量及对应收藏状态
                dataRecord.setCollectNum(dataRecord.getCollectNum() + 1);
                dataRecord.setCollectStatus(true);
                break;
            case CommonConstant.USER_OPERATION_TYPE_COMPLETE:
                if (completeStatus) {
                    return ResultUtil.error(500, "不可重复完成同一资料");
                }
                //添加完成数量
                operationResult = baseMapper.addDataCompleteNum(dataId);
                //设置完成数量及对应完成状态
                dataRecord.setCompleteNum(dataRecord.getCompleteNum() + 1);
                dataRecord.setCompleteStatus(true);
                break;
            default:
                return ResultUtil.error(500, "无相应操作");
        }
        //用户点赞或点踩操作成功后
        if (operationResult) {
            //用户添加点赞记录
            userOperationRecord.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
            userOperationRecord.setOperationType(operationType);
            userOperationRecord.setCreateBy(ShiroUtils.getUserInfo().getUsername());
            userOperationRecord.setCreateTime(new Date());
            userOperationRecordService.save(userOperationRecord);

            //根据点赞点踩类型进行积分规则区分
            if (CommonConstant.USER_OPERATION_TYPE_LIKE.equals(operationType)) {
                //判断点赞数量是否符合规则
                if (dataRecord.getLikeNum() % CommonConstant.INTEGRAL_RULE_COMMENT_LIKE_NUM == 0) {
                    DataInfo dataInfo = baseMapper.selectById(dataId);
                    if (dataInfo != null) {
                        //添加用户积分和获取积分记录
                        Integer resultInt = userIntegralRecordService.addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_LIKE_NUM, null, dataInfo.getUserId(), CommonConstant.USER_OPERATION_EVENT_TYPE_DATA, dataId);
                        log.info("用户资料点赞积分获取结果:{}", resultInt);
                    }
                }
            } else if (CommonConstant.USER_OPERATION_TYPE_COMPLETE.equals(operationType)) {
                //如果是完成学习资料
                DataInfo dataInfo = baseMapper.selectById(dataId);
                if (dataInfo != null) {
                    //根据学习资料类型执行不同的积分规则
                    if (CommonConstant.DATA_TYPE_LEARN_DATA.equals(dataInfo.getDataType())) {
                        Integer resultInt = userIntegralRecordService.addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_READ_LEARN_DATA, CommonConstant.CACHE_INTEGRAL_RULE_READ_LEARN_DATA, ShiroUtils.getUserInfo().getUserId().toString(), CommonConstant.USER_OPERATION_EVENT_TYPE_DATA, dataId);
                        log.info("用户阅读学习资料积分获取结果:{}", resultInt);
                    } else {
                        Integer resultInt = userIntegralRecordService.addUserIntegralRecord(CommonConstant.INTEGRAL_RULE_READ_LEARN_DATA, CommonConstant.CACHE_INTEGRAL_RULE_READ_LEARN_VIDEO, ShiroUtils.getUserInfo().getUserId().toString(), CommonConstant.USER_OPERATION_EVENT_TYPE_DATA, dataId);
                        log.info("用户阅读学习视频积分获取结果:{}", resultInt);
                    }
                }
            }
            //返回点赞或点踩数量及对应点赞、点踩状态
            return ResultUtil.success(dataRecord);
        } else {
            return ResultUtil.error("用户操作失败");
        }
    }

    @Override
    public IPage<DataExperienceVo> getProblemCommentList(Page<DataExperience> page, String dataId) {
        return baseMapper.getProblemCommentList(page, dataId);
    }

    @Override
    public Result<?> addDataExperience(DataExperience dataExperience) {
        dataExperience.setCreateBy(ShiroUtils.getUserInfo().getUsername());
        dataExperience.setCreateTime(new Date());
        dataExperience.setUserId(ShiroUtils.getUserInfo().getUserId().toString());
        if (dataExperienceService.save(dataExperience)) {
            return ResultUtil.success("添加学习心得成功");
        }
        return ResultUtil.error("添加学习心得异常");
    }

    @Override
    public Result<?> delDataExperience(String experienceId) {
        //根据心得id获取心得信息
        DataExperience dataExperience = dataExperienceService.getById(experienceId);
        if (dataExperience != null) {
            //根据资料id获取资料信息
            DataInfo dataInfo = this.getById(dataExperience.getDataId());
            if (dataInfo != null) {
                //如果不是自己的资料并且也不是自己的心得
                if (!dataInfo.getUserId().equals(ShiroUtils.getUserInfo().getUserId().toString())
                        && !dataExperience.getUserId().equals(ShiroUtils.getUserInfo().getUserId().toString())) {
                    //不可以删除
                    return ResultUtil.success("没有删除权限");
                }
                dataExperienceService.removeById(experienceId);
                return ResultUtil.success("删除成功");
            }
        }
        return ResultUtil.error(500, "没有找到要删除的学习心得");
    }
}
