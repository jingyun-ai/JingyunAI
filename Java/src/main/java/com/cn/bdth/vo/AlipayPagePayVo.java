package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付宝电脑网站支付返回结果
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class AlipayPagePayVo implements Serializable {

    /**
     * 订单ID
     */
    private String ordersId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 商品价格
     */
    private Double productPrice;

    /**
     * 支付表单HTML
     */
    private String payForm;
}