package com.cn.bdth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cn.bdth.dto.ShelvesProductDto;
import com.cn.bdth.vo.AlipayPagePayVo;
import com.cn.bdth.vo.AlipayPayCodeVo;
import com.cn.bdth.vo.OrderPageVo;
import com.cn.bdth.vo.ProductVo;
import com.cn.bdth.vo.WeixinPayCodeVo;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
public interface PayService {

    /**
     * 获取产品列表。
     *
     * @return the product list
     */
    List<ProductVo> getProductList();


    /**
     * 获取当前用户订单页。
     *
     * @param pageNum the page num
     * @return the current user order page
     */
    OrderPageVo getCurrentUserOrderPage(final int pageNum);


    /**
     * 获取枫叶订单数据
     *
     * @param pageNum the page num
     * @return the current user order page
     */
    OrderPageVo getUserOrderPage(final int pageNum,final String prompt, final String status);

    /**
     * 根据ID删除产品
     *
     * @param id the id
     */
    void deleteProductById(final Long id);


    /**
     * 上架产品
     *
     * @param dto the dto
     */
    void shelvesProduct(final ShelvesProductDto dto);




    /**
     * 分页获取产品数据
     */
    IPage<ProductVo> getProductPageVo(final int pageNum, final String prompt);


    /**
     * 生成支付宝支付二维码
     *
     * @param productId the product id
     * @return the alipay pay code vo
     */
    AlipayPayCodeVo generatePayQrCode(final Long productId);
    
    /**
     * 生成支付宝电脑网站支付
     *
     * @param productId the product id
     * @return the alipay page pay vo
     */
    AlipayPagePayVo generatePagePay(final Long productId);
    
    /**
     * 生成微信支付二维码
     *
     * @param productId the product id
     * @return the weixin pay code vo
     */
    WeixinPayCodeVo generateWeixinPayQrCode(final Long productId);


    /**
     * 支付宝回调
     *
     * @param request the request
     * @return the string
     */
    String alipayPullback(final HttpServletRequest request);
//  微信支付回调
    String wxpayPullback(final HttpServletRequest request) throws IOException;


    /**
     * Payment status string.
     *
     * @param orderNo the order no
     * @return the string
     */
    String paymentStatus(final String orderNo);
}
