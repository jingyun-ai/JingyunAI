package com.cn.bdth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class ServerConfigDto {

    @NotBlank(message = "OPEN_AI_URL不能为空")
    private String openAiUrl;

    @NotBlank(message = "OPEN_AI_PLUS_URL不能为空")
    private String openAiPlusUrl;

    @NotBlank(message = "OPEN_KEY不能为空")
    private String openKey;

    @NotBlank(message = "OPEN_PLUS_KEY不能为空")
    private String openPlusKey;

    @NotNull(message = "GPT消耗次数不能为空")
    private Long gptFrequency;

    @NotNull(message = "GPT_PLUS消耗次数不能为空")
    private Long gptPlusFrequency;

    @NotNull(message = "第一次登录奖励次数不能为空")
    private Long incentiveFrequency;

    @NotNull(message = "签到赠送次数不能为空")
    private Long signInFrequency;

    // @NotBlank(message = "必应Cookie不能为空")
    private String newBingCookie;

    //@NotNull(message = "用户观看视频奖励不能为空")
    private Long videoFrequency;

    // @NotBlank(message = "SD_URL不能为空")
    private String sdUrl;

    //@NotNull(message = "图生图消耗次数不能为空")
    private Long sdImageFrequency;

    //@NotNull(message = "标准文生图消耗次数不能为空")
    private Long gptTextImageFrequency;

    // @NotBlank(message = "克劳德organizationUuid不能为空")
    private String organizationUuid;

    // @NotBlank(message = "克劳德conversationUuid不能为空")
    private String conversationUuid;

    // @NotBlank(message = "克劳德SessionKey不能为空")
    private String sessionKey;

    //@NotNull(message = "DrawingSimpleDebug不能为空")
    private Integer drawingSimpleDebug;

    // @NotBlank(message = "CCoUI_URL不能为空")
    private String ccoUrl;

    //@NotNull(message = "CCoUI_IMG消耗次数不能为空")
    private Long ccoImgFrequency;

    //@NotNull(message = "CCoUI_GPT消耗次数不能为空")
    private Long ccoGPTFrequency;

    //@NotNull(message = "CCoUI_SAM消耗次数不能为空")
    private Long ccoSamFrequency;

    // @NotBlank(message = "TuKeLi_Url不能为空")
    private String tuKeLi_Url;

    // @NotBlank(message = "TuKeLi_Key不能为空")
    private String tuKeLi_Key;

    //@NotNull(message = "图可丽-AI背景替换消耗次数不能为空")
    private Long tkloutPaintGenerateFrequency;

    //@NotNull(message = "图可丽-AI草图渲染消耗次数不能为空")
    private Long tklpromeaigenerateFrequency;

    //@NotNull(message = "图可丽-AI超模消耗次数不能为空")
    private Long tklsupermodelGenerateFrequency;
}
