package com.cn.bdth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Component
@Data
public class DrawingSimpleDefaultConfig {
    private Integer debug;
    //-------CCoUI-----------
    @Value("${config.CCoUIUrl}")
    private String ccoUrl;

    private Long ccoImgFrequency = 0L;
    private Long ccoGPTFrequency = 0L;
    private Long ccoSamFrequency = 0L;
    //-------图可丽-----------
    @Value("${config.TuKeLi_Url}")
    private String TuKeLi_Url;
    @Value("${config.TuKeLi_Key}")
    private String TuKeLi_Key;
    private Long tkloutPaintGenerateFrequency = 10L;
    private Long tklpromeaigenerateFrequency = 10L;
    private Long tklsupermodelGenerateFrequency = 10L;

}
