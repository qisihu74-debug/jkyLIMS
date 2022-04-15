package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleCollectionJSON;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TestSampleEntityMapper;
import com.lims.manage.erp.service.TestSampleEntityService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import com.lims.manage.erp.vo.SampleJudgeBasisVo;
import com.lims.manage.erp.vo.SampleSimpleListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TestSampleEntityServiceImpl extends ServiceImpl<TestSampleEntityMapper, TestSampleEntity> implements TestSampleEntityService {
    @Autowired
    private TestSampleEntityMapper testSampleEntityMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;

    @Override
    public Integer batchInsertSample(List<SampleDetailAddVo> samples) {
        List<TestSampleEntity> entities = Lists.newArrayList();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = sampleEntityMapper.getMaxNumber(sdf.format(now));
        int newMax;
        if (maxNumber == null) {
            newMax = 0;
        } else {
            newMax = Integer.parseInt(maxNumber);
        }
        for (int i = 0; i < samples.size(); i++) {
            int code = newMax + i + 1;
            String codeStr = new DecimalFormat("0000").format(code);
            String sampleCode;
            if (samples.get(i).getQuantityPerGroup() > 1) {
                String numStr = new DecimalFormat("00").format(samples.get(i).getQuantityPerGroup());
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr + "（01~" + numStr + "）";
            } else {
                sampleCode = "YP-" + sdf.format(now) + "-" + codeStr;
            }
            TestSampleEntity entity = new TestSampleEntity(samples.get(i), sampleCode);
            entities.add(entity);
        }
        return testSampleEntityMapper.insertBatch(entities);
    }

    @Override
    public PageInfo querySampleList(TestSampleEntity sampleEntity) {
        PageHelper.startPage(sampleEntity.getPageNum(), sampleEntity.getPageSize());
        if (sampleEntity.getReceivedDate() != null) {
            String[] split = sampleEntity.getReceivedDate().split("~");
            sampleEntity.setBeginDate(split[0]);
            sampleEntity.setEndDate(split[1]);
        }
        List<SampleSimpleListVo> simpleList = testSampleEntityMapper.getSimpleList(sampleEntity);
        PageInfo<SampleSimpleListVo> pageInfo = new PageInfo<>(simpleList);
        return pageInfo;
    }

    @Override
    public Boolean uploading(Integer id, MultipartFile[] file) {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(id);
        //附件存在上传附件到服务器
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            // 根据file文件数量 规定文件名存储编号规则
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.buckets_sample_enclosure, multipartFile, fileCode + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（文件编号&委托文档资料.pdf,文件编号&原始文档.docx）
                stringfileUrlStr.append(fileCode + "&" + name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                sampleEntity.setFileUrl(substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                sampleEntity.setFileUrlStr(substring);
            }
        }
        // 根据样品id 更新附件url。
        sampleEntityMapper.updateSampleInfoFileUrl(sampleEntity);
        return true;
    }

    @Override
    public TestSampleEntity sampleDetail(Integer id) {
        TestSampleEntity entity = testSampleEntityMapper.selectByPrimaryKey(id);
        if (entity != null) {
            String outward = entity.getOutward();
            if (outward != null) {
                String replace = outward.replace("[", "");
                String replace1 = replace.replace("]", "");
                String replace2 = replace1.replace("\"", "");
                String[] split = replace2.split(",");
                List<String> outwardArr = Lists.newArrayList();
                for (String s : split) {
                    outwardArr.add(s.trim());
                }
                entity.setOutwardArr(outwardArr);
            }
            List<TestSampleCollectionJSON> fileArrays = new ArrayList<>();
            // 处理原始记录名称
            if(entity.getFile()!=null&&entity.getFileUrlStr()!=null){
               String[] file = entity.getFile().split(",");
               String[] fileUrlStr = entity.getFileUrlStr().split(",");
               for(int i = 0;i<file.length;i++){
                   TestSampleCollectionJSON testSampleCollectionJSON = new TestSampleCollectionJSON();
                   testSampleCollectionJSON.setLable(fileUrlStr[i]);
                   testSampleCollectionJSON.setValue(file[i]);
                   fileArrays.add(testSampleCollectionJSON);
               }
            }
            entity.setFileArrays(fileArrays);
        }

        return entity;
    }

    @Override
    public int updateSample(TestSampleEntity sampleEntity) {
        return testSampleEntityMapper.updateByPrimaryKeyNotAll(sampleEntity);
    }
}
