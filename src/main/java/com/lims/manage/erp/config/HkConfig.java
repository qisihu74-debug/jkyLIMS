package com.lims.manage.erp.config;

import com.hikvision.artemis.sdk.config.ArtemisConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.config
 * @desc
 * @date 2024-09-09 16:37
 * @Copyright © 河南交科院
 */
@Data
@Configuration
public class HkConfig {
    @Value("${hikvision.api.person}")
    private String person;
    @Value("${hikvision.api.personBandDoor}")
    private String personBandDoor;
    @Value("${hikvision.api.cancelBandDoor}")
    private String cancelBandDoor;
    @Value("${hikvision.api.grant}")
    private String grant;

    @Value("${hikvision.sdk.tagId}")
    private String tagId;
    @Value("${hikvision.sdk.host}")
    private String hkHost;
    @Value("${hikvision.sdk.key}")
    private String hkKey;
    @Value("${hikvision.sdk.secret}")
    private String hkSecret;
    @Value("${hikvision.api.door.events}")
    private String doorEvents;
    @Value("${hikvision.api.door.pictures}")
    private String doorPictures;
    @Value("${hikvision.api.door.search}")
    private String doorSearch;
    @Value("${hikvision.api.door.state}")
    private String doorState;
    @Value("${hikvision.api.camera.search}")
    private String cameraSearch;
    @Value("${hikvision.api.camera.cameras}")
    private String cameraCameras;
    @Value("${hikvision.api.camera.indexCode}")
    private String cameraIndexCode;
    @Value("${hikvision.api.video.previewURLs}")
    private String videoPreviewURLs;
    @Value("${hikvision.api.video.playbackURLs}")
    private String videoPlaybackURLs;
    @Value("${hikvision.api.video.manualCapture}")
    private String videoManualCapture;



    @Bean
    public ArtemisConfig setArtemisConfig(){
        ArtemisConfig artemisConfig = new ArtemisConfig();
        artemisConfig.setHost(hkHost);
        artemisConfig.setAppKey(hkKey);
        artemisConfig.setAppSecret(hkSecret);
        return artemisConfig;
    }

 }
