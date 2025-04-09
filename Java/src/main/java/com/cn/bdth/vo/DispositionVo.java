package com.cn.bdth.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
public class DispositionVo implements Serializable {

    private String openAiUrl;

    private String openAiPlusUrl;

    private String openKey;

    private String openPlusKey;

    private Long gptTextImageFrequency;

    private Long gptFrequency;

    private Long gptPlusFrequency;

    private String organizationUuid;

    private String conversationUuid;

    private String sessionKey;

    private String newBingCookie;

    private Long sdImageFrequency;

    private String sdUrl;

    private Long incentiveFrequency;

    private Long signInFrequency;

    private Long videoFrequency;
    // DrawingSimple
    private Integer drawingSimpleDebug;

    private String ccoUrl;

    private Long ccoImgFrequency;

    private Long ccoGPTFrequency;

    private Long ccoSamFrequency;

    private String tuKeLi_Url;

    private String tuKeLi_Key;

    private Long tkloutPaintGenerateFrequency;

    private Long tklpromeaigenerateFrequency;

    private Long tklsupermodelGenerateFrequency;
}
