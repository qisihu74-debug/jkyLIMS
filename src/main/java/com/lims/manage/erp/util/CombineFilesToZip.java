package com.lims.manage.erp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2023-12-20 10:16
 * @Copyright © 河南交科院
 */
public class CombineFilesToZip {

    /**
     * 处理files
     * @param files
     * @throws Exception
     */
    public static void handerFiles(List<File> files,String pathName) throws Exception {
        // 创建一个临时文件夹来存储合并后的文件
        File tempFolder = File.createTempFile("temp", "");
        tempFolder.deleteOnExit();

        // 将每个文件添加到临时文件夹中
        for (File file : files) {
            Files.copy(file.toPath(), new File(tempFolder, file.getName()).toPath());
        }

        // 创建一个ZIP文件并将临时文件夹中的所有文件添加到其中
        File zipFile = new File(pathName);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : tempFolder.listFiles()) {
                addToZipFile(file, zos);
            }
        }

        // 删除临时文件夹
        Files.walk(tempFolder.toPath())
                .sorted((o1, o2) -> -o1.compareTo(o2))
                .forEach(path -> path.toFile().delete());
    }

    private static void addToZipFile(File file, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
        }
    }
}

