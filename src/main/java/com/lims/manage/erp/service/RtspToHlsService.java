package com.lims.manage.erp.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-10-23 11:24
 * @Copyright © 河南交科院
 */
@Service
public class RtspToHlsService {

    /**
     * rtsp转换http
     * @param rtspUrl
     * @param hlsOutputPath
     * @throws IOException
     * @throws InterruptedException
     */
    public String startTranscoding(String rtspUrl, String hlsOutputPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", rtspUrl,
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-c:a", "aac",
                "-f", "hls",
                hlsOutputPath
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 读取FFmpeg的输出
        InputStream inputStream = process.getInputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            System.out.write(buffer, 0, length);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg process exited with code " + exitCode);
        }
        //获取链接

        return "";
    }
}
