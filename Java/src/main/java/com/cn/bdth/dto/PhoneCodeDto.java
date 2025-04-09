package com.cn.bdth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PhoneCodeDto {

    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Size(min = 5, max = 20, message = "密码在5-20位之间")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
