package com.cn.bdth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
public class StarPresetDto {

    @NotBlank(message = "标题不能为空")
    private String title;

}
