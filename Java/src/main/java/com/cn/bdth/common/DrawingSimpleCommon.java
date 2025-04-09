package com.cn.bdth.common;

import com.cn.bdth.config.DrawingSimpleDefaultConfig;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.utils.RedisUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DrawingSimpleCommon {

    private final RedisUtils redisUtils;

    private final DrawingSimpleDefaultConfig drawingSimpleDefaultConfig;

    public DrawingSimpleStructure getDrawingSimpleStructure() {
        final Object value = redisUtils.getValue(ServerConstant.DRAWINGSIMPLE_CONFIG);
        if (value == null) {
            return getDefault();
        }
        try {
            return (DrawingSimpleStructure) value;
        } catch (Exception e) {
            log.warn("已清除上一个版本的DrawingSimple配置,请前往控制台重新配置DrawingSimple参数配置");
            redisUtils.delKey(ServerConstant.DRAWINGSIMPLE_CONFIG);
            return getDefault();
        }
    }

    private DrawingSimpleStructure getDefault() {
        //log.warn("请前往控制台配置CCoUI参数配置");
        return new DrawingSimpleStructure()
                .setDebug(drawingSimpleDefaultConfig.getDebug())
                .setCcoUrl(drawingSimpleDefaultConfig.getCcoUrl())
                .setCcoImgFrequency(drawingSimpleDefaultConfig.getCcoImgFrequency())
                .setCcoGPTFrequency(drawingSimpleDefaultConfig.getCcoGPTFrequency())
                .setCcoSamFrequency(drawingSimpleDefaultConfig.getCcoSamFrequency())
                .setTuKeLi_Url(drawingSimpleDefaultConfig.getTuKeLi_Url())
                .setTuKeLi_Key(drawingSimpleDefaultConfig.getTuKeLi_Key())
                .setTkloutPaintGenerateFrequency(drawingSimpleDefaultConfig.getTkloutPaintGenerateFrequency())
                .setTklpromeaigenerateFrequency(drawingSimpleDefaultConfig.getTklpromeaigenerateFrequency())
                .setTklsupermodelGenerateFrequency(drawingSimpleDefaultConfig.getTklsupermodelGenerateFrequency());
    }

    @Data
    @Accessors(chain = true)
    public static class DrawingSimpleStructure {
        public Integer debug;
        //-------CCoUI-----------
        private String ccoUrl;
        private Long ccoImgFrequency;
        private Long ccoGPTFrequency;
        private Long ccoSamFrequency;
        //-------图可丽-----------
        private String TuKeLi_Url;
        private String TuKeLi_Key;
        //AI背景重绘
        private Long tkloutPaintGenerateFrequency;
        private Long tklpromeaigenerateFrequency;
        private Long tklsupermodelGenerateFrequency;

    }
}
