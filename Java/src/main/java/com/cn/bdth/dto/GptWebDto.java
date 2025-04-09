package com.cn.bdth.dto;

import com.cn.bdth.model.GptModel;
import com.cn.bdth.model.GptVisionModel;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class GptWebDto {

    @NotEmpty(message = "消息数据不能为空")
    private List<GptModel.Messages> messages;
    
    // 新增字段，可选，用于接收前端在消息体中传递的count值
    private Integer count;
}
