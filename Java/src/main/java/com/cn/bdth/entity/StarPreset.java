package com.cn.bdth.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * The type Star.
 *
 * @author 20740
 */
@Data
@TableName(value = "star_preset")
@Accessors(chain = true)
public class StarPreset {
    private String starId;

    private Long userId;

    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
