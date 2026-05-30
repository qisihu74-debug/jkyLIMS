package com.lims.manage.erp.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class CombineFilesToZip {

    /**
     * A目录复制到B目录作为子目录
     * @param source
     * @param directory
     */
    public static void mergeDirectories(String source, String directory) throws Exception {
        Path directoryA = Paths.get(source);
        Path directoryB = Paths.get(directory);
        // 确保目录B存在，如果不存在则创建它
        if (!Files.exists(directoryB)) {
            Files.createDirectories(directoryB);
        }

        // 遍历目录A中的文件和子目录
        Files.walk(directoryA)
                .forEach(entry -> {
                    // 获取目录A中当前路径相对于目录A的根的相对路径
                    Path relativePath = directoryA.relativize(entry);
                    // 构建目录B中对应的路径
                    Path targetPath = directoryB.resolve(relativePath);
                    // 如果目录B中对应的路径不存在，则创建它
                    if (!Files.exists(targetPath.getParent())) {
                        try {
                            Files.createDirectories(targetPath.getParent());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // 将目录A中的文件或子目录复制到目录B中
                    try {
                        Files.copy(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("Error copying " + entry + " to " + targetPath);
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 将文件添加到，目录
     * @param fileList
     * @param dirName
     */
    public static void addFileToDir(List<File> fileList,String dirName) {
        try {
            // 创建一个临时文件夹来存储zip文件
            File tempDir = new File(dirName);
            tempDir.mkdir();
            // 遍历fileList，将每个文件添加到临时文件夹中
            for (File file : fileList) {
                File destFile = new File(tempDir, file.getName());
                FileUtils.copyFile(file, destFile);
            }
        }catch (Exception e){
            log.error("文件添加到目录失败:{}",e);
        }
    }

    /**
     * 下载压缩包
     * @param dirList
     * @param zipName
     * @throws IOException
     */
    public static void downloadZip(List<String> dirList, String zipName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (int i=0;i<dirList.size();i++){
                String name = dirList.get(i).split("-")[1];
                addDirectoryToZip(name, dirList.get(i), zos);
            }

        }
    }

    private static void zipDirectories(List<String> directories, ZipOutputStream zos) throws IOException {
        for (String directory : directories) {
            File dir = new File(directory);
            zipDirectory(dir, zos, "");
        }
    }

    private static void zipDirectory(File dir, ZipOutputStream zos, String parentPath) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        zipDirectory(file, zos, parentPath + file.getName() + "/");
                    } else {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            zos.putNextEntry(new ZipEntry(parentPath + file.getName()));
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                zos.write(buffer, 0, bytesRead);
                            }
                            zos.closeEntry();
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("The specified path is not a directory.");
        }
    }

    private static void addDirectoryToZip(String entryName, String sourceDirectory, ZipOutputStream zos) throws IOException {
        Path sourceDirPath = Paths.get(sourceDirectory);
        Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(entryName + "/" + path.toString().substring(sourceDirPath.toString().length()));
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
