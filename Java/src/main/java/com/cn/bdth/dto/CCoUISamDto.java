package com.cn.bdth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CCoUISamDto {

    @NotBlank(message = "分割模型不能为空")
    private String SAMModel;
    private String prompt;
    private MultipartFile img;

    @NotNull(message = "环境不能为空")
    private Integer env;

}
