package com.cn.bdth.api;

import com.cn.bdth.msg.Result;
import com.cn.bdth.service.PayService;
import com.cn.bdth.service.ServerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * 公开
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {


    private final ServerService serverService;

    private final PayService payService;

    /**
     * 获取公告
     *
     * @return the announcement
     */
    @GetMapping(value = "/get/announcement", name = "获取公告", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getAnnouncement() {
        return Result.data(serverService.getAnnouncement());
    }

    /**
     * 支付宝授权
     *
     * @param request the request
     * @return the string
     */
    @PostMapping(value = "/callback/order", name = "支付宝授权回调地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public String alipayPullback(HttpServletRequest request) {
        return payService.alipayPullback(request);
    }
    
    /**
     * 支付宝支付结果页面
     *
     * @param request the request
     * @return the result
     */
    @GetMapping(value = "/pay/result", name = "支付宝支付结果页面", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result payResult(HttpServletRequest request) {
        // 获取支付宝GET过来反馈信息
        String tradeStatus = request.getParameter("trade_status");
        String outTradeNo = request.getParameter("out_trade_no");
        
        log.info("支付宝支付结果页面 - 订单号: {}, 交易状态: {}", outTradeNo, tradeStatus);
        
        // 查询订单状态
        String status = payService.paymentStatus(outTradeNo);
        
        return Result.data(status);
    }
    
//  微信支付回调
    @PostMapping(value = "/wxcallback/order", name = "微信支付授权回调地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public String wxpayPullback(HttpServletRequest request) throws IOException {
        return payService.wxpayPullback(request);
    }

    /**
     * 获取小程序状态
     */
    @GetMapping(value = "/get/wechat/status", name = "获取小程序状态", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getWechatAppStatus() {
        return Result.data(serverService.wechatStatus());
    }

}
