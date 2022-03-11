package com.lims.manage.erp.service.impl;


import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS对象存储表(SysOss)表服务实现类
 *
 * @author makejava
 * @since 2022-03-10 16:13:34
 */
@Service("sysOssService")
public class SysOssServiceImpl implements SysOssService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> postAnnounce(MultipartFile[] file) {
        Map<String, Object> map = new HashMap<>();
        if (file != null) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringfileUrlStr = new StringBuilder();
            for (MultipartFile multipartFile : file) {
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");
                String upload = MinIoUtil.upload(BucketsConst.file_syn, multipartFile, GenID.getID() + "." + strings[strings.length - 1]);
                stringBuilder.append(upload);
                stringBuilder.append(",");
                // 存放上传文件的名称带后缀如：（委托文档资料.pdf,原始文档.docx）
                stringfileUrlStr.append(name);
                stringfileUrlStr.append(",");
            }
            String fileUrl = stringBuilder.toString();
            if (!StringUtils.isEmpty(fileUrl)) {
                String substring = fileUrl.substring(0, fileUrl.length() - 1);
                map.put("fileUrl", substring);
            }
            String fileUrlStr = stringfileUrlStr.toString();
            if (!StringUtils.isEmpty(fileUrlStr)) {
                String substring = fileUrlStr.substring(0, fileUrlStr.length() - 1);
                map.put("fileName", substring);
            }

        }
        return map;
    }

}

