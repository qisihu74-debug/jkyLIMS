package com.stu.manage.demo.entity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/30 16:20
 * @Copyright © 河南交科院
 */
public class FileHomeLocation {
    /**
     * 查找server.properties这个文件夹，通过键（key）的名字获取key对应的值
     * @param fileName key的值
     * @return key对应的value的值
     * @throws IOException 文件异常
     */
    public String fileLocation(String fileName) throws IOException {
        Properties properties = new Properties();
        InputStream in = new FileInputStream(FileHomeLocation.class.getClassLoader().getResource("server.properties").getPath());
        properties.load(in);
        Iterator<String> it = properties.stringPropertyNames().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.equals(fileName)) {
                String value = properties.get(key).toString();
                in.close();
                return value;
            }
        }
        in.close();
        return "";
    }

    /**
     * 通过文件夹路径 新建文件夹
     * @param folderPath 文件夹路径
     */
    public void newFolder(String folderPath) {
        try {
            String filePath = folderPath;
            java.io.File myFilePath = new java.io.File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据不同的操作系统获取不同的斜杠
     *
     * @return 斜杠
     */
    public String systemProperty() {
        String osName = System.getProperty("os.name");
        if (Pattern.matches("Linux.*", osName)) {
            return "/";
        } else {
            return "\\";
        }
    }

    /**
     * 上传文件
     *
     * @param file      文件流
     * @param accessory 文件要存储的路径
     * @return 文件名和文件路径的集合
     */
    public synchronized List uploadTemplate(CommonsMultipartFile file, String accessory) {
        OutputStream os = null;
        InputStream is = null;
        FileHomeLocation location = new FileHomeLocation();
        location.newFolder(accessory + systemProperty());
        String name = new Date().getTime() + file.getOriginalFilename();
        String realPath = accessory + systemProperty() + name;
        try {
            //获取输出流
            os = new FileOutputStream(realPath);
            //获取输入流 CommonsMultipartFile 中可以直接得到文件的流
            is = file.getInputStream();
            int temp;
            //一个一个字节的读取并写入
            while ((temp = is.read()) != (-1)) {
                os.write(temp);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<String> data = new ArrayList<String>();
        data.add(name);
        data.add(realPath);
        return data;
    }

    /**
     * 通过路径下载文件
     *
     * @param realPath 路径信息
     * @return 文件实体
     */
    public ResponseEntity<byte[]> documentDown(String realPath) {
        ResponseEntity<byte[]> entity = null;
        try {
            File file = new File(realPath);
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = file.getName();
            headers.setContentDispositionFormData("attachment", new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            entity = new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    public static void main(String[] args) {
        FileHomeLocation f = new FileHomeLocation();
        List l = f.findPackages("/home/aa/bb////cc/ccdd", "/");
    }

    /**
     * 通过字符串截取
     *
     * @param cname    字符串
     * @param findchar 字符
     * @return 截取后的字符数组
     */
    public List findPackages(String cname, String findchar) {
        List categoryName = new ArrayList();
        do {
            sum = 0;
            List sub = subString(cname, findchar);
            if (sub == null) {
                break;
            }
            categoryName.add(sub.get(0));
            if (Integer.parseInt(sub.get(1) + "") >= cname.length()) {
                break;
            }
            cname = cname.substring(Integer.parseInt(sub.get(1) + ""));
        } while (true);
        return categoryName;
    }

    int sum = 0;

    private List subString(String str, String findchar) {
        List list = new ArrayList();
        int i = str.indexOf(findchar);
        if (i == -1) {
            i = str.length();
        }
        String substring = str.substring(0, i);
        if (!substring.isEmpty()) {
            list.add(substring);
            list.add(i + sum);
            return list;
        } else {
            sum++;
            return subString(str.substring(1), findchar);
        }

    }

    /**
     * 下载文件 文件放入response 中
     *
     * @param response 请求返回信息
     * @param path     文件路径
     * @throws UnsupportedEncodingException 编码异常
     */
    public synchronized void responseFile(HttpServletResponse response, String path, String fileNam2) throws UnsupportedEncodingException {


        String fileName = fileNam2 != null ? fileNam2 : path.substring(path.lastIndexOf(systemProperty()) + 1);
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Connection", "close");
        response.setHeader("Content-Type", "application/octet-stream");

        OutputStream ops = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[8192];
        int bytesRead = 0;

        try {
            ops = response.getOutputStream();
            fis = new FileInputStream(path);
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                ops.write(buffer, 0, bytesRead);
            }
            ops.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (ops != null) {
                    ops.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
