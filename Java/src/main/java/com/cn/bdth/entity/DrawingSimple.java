package com.cn.bdth.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 绘图实体类
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@TableName(value = "drawingsimple")
@Accessors(chain = true)
public class DrawingSimple {
    private String taskid;

    private Long userid;

    private Long Frequency;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;

}
