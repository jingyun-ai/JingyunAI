package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * 添加图生图
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@SuppressWarnings("all")
public class DrawingSimpleDto {
    //(API信息1)交互变量映射
    // -----------------APIInfo-----------------
    private String urlType;
    private String mediaType;
    // -----------------ccoui-----------------
    private String API;
    private MultipartFile img;
    private MultipartFile mask;
    private String prompt_mode;
    private String positive;
    private String negative;
    private Integer width;
    private Integer height;
    private Integer seed;
    private Short batch_size;
    private Integer env;

    // -----------------图可丽
    private String imgBase64;
    private String imgUrl;
    private String text;
    private String prompt;
    private String style;
    private Double strength;
    private String negativePrompt;
    private String imageUrl;
    private String maskUrl;
    private String mode;
}
