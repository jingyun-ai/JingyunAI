package com.cn.bdth.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.constants.OrderConstant;
import com.cn.bdth.constants.lock.LockPrefix;
import com.cn.bdth.dto.ShelvesProductDto;
import com.cn.bdth.entity.Orders;
import com.cn.bdth.entity.Product;
import com.cn.bdth.entity.User;
import com.cn.bdth.exceptions.ExceptionMessages;
import com.cn.bdth.exceptions.OrdersException;
import com.cn.bdth.mapper.OrdersMapper;
import com.cn.bdth.mapper.ProductMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.service.PayService;
import com.cn.bdth.structure.AlipayCacheStructure;
import com.cn.bdth.structure.WeixinpayCacheStructure;
import com.cn.bdth.task.UnpaidOrderQueue;
import com.cn.bdth.utils.*;
import com.cn.bdth.vo.AlipayPagePayVo;
import com.cn.bdth.vo.AlipayPayCodeVo;
import com.cn.bdth.vo.OrderPageVo;
import com.cn.bdth.vo.ProductVo;
import com.cn.bdth.vo.WeixinPayCodeVo;
import com.google.zxing.WriterException;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wechat.pay.java.core.http.Constant.*;

/**
 * 支付服务impl
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayServiceImpl implements PayService {

    private final ProductMapper productMapper;

    private final RedisUtils redisUtils;

    private final RedisLockHelper lockHelper;

    private final IdGeneratorUtils idGeneratorUtils;

    private final OrdersMapper ordersMapper;

    private final UserMapper userMapper;

    @Value("${ali-pay.appId}")
    private String appId;

    @Value("${ali-pay.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${ali-pay.privateKey}")
    private String privateKey;

    @Value("${ali-pay.domain}")
    private String domain;

    @Value("${we-chat.appId}")
    private String wxappId;

    @Value("${wx-pay.merchantId}")
    private String merchantId;

    @Value("${wx-pay.merchantSerialNumber}")
    private String merchantSerialNumber;

    @Value("${wx-pay.apiV3Key}")
    private String apiV3Key;

    private final UnpaidOrderQueue unpaidOrderQueue;

    /**
     * 得到产品列表
     * 产品列表
     *
     * @return {@link List}<{@link ProductVo}>
     */
    @Override
    public List<ProductVo> getProductList() {
        return productMapper.selectList(
                new QueryWrapper<Product>()
                        .lambda()
                        .select(Product::getProductId, Product::getProductPrice, Product::getProductName, Product::getFrequency)
        ).stream().map(c -> new ProductVo().setProductId(c.getProductId()).setProductPrice(c.getProductPrice()).setFrequency(c.getFrequency()).setProductName(c.getProductName())).collect(Collectors.toList());
    }

    /**
     * 得到当前用户订单页面
     *
     * @param pageNum 页面num
     * @return {@link OrderPageVo}
     */
    @Override
    public OrderPageVo getCurrentUserOrderPage(final int pageNum) {
        final Long userId = UserUtils.getCurrentLoginId();
        return getOrdersPage(userId, null, null, pageNum);
    }

    /**
     * 得到用户订单页面
     *
     * @param pageNum 页面num
     * @param status  状态
     * @return {@link OrderPageVo}
     */
    @Override
    public OrderPageVo getUserOrderPage(int pageNum, String prompt, String status) {
        return getOrdersPage(null, prompt, status, pageNum);
    }

    /**
     * 得到订单页面
     *
     * @param userId  用户id
     * @param state   状态
     * @param pageNum 页面num
     * @return {@link OrderPageVo}
     */
    private OrderPageVo getOrdersPage(final Long userId, final String prompt, final String state, final int pageNum) {
        final OrderPageVo orderPageVo = new OrderPageVo();
        final IPage<OrderPageVo.Orders> convert = ordersMapper.selectPage(new Page<>(pageNum, 15), new QueryWrapper<Orders>()
                .lambda()
                .eq(userId != null, Orders::getUserId, userId)
                .eq(StringUtils.notEmpty(state), Orders::getState, state)
                .eq(StringUtils.notEmpty(prompt), Orders::getOrdersId, prompt)
                .select(
                        Orders::getOrdersId,
                        Orders::getPayTime,
                        Orders::getState,
                        Orders::getProductPrice,
                        Orders::getProductName,
                        Orders::getFrequency,
                        Orders::getReasonFailure
                ).orderByDesc(Orders::getCreatedTime)
        ).convert(o -> new OrderPageVo.Orders()
                .setOrdersId(o.getOrdersId())
                .setReasonFailure(o.getReasonFailure())
                .setState(o.getState())
                .setFrequency(o.getFrequency())
                .setPayTime(o.getPayTime())
                .setProductName(o.getProductName())
                .setProductPrice(o.getProductPrice()));
        orderPageVo.setOrders(convert);
        // 查询总金额
        double totalAmount;
        try {
            totalAmount = (Double) ordersMapper.selectObjs(
                    new QueryWrapper<Orders>()
                            .eq(userId != null, "user_id", userId)
                            .eq("state", 1)
                            .select("sum(product_price)")
            ).stream().findFirst().orElseThrow();
        } catch (Exception e) {
            totalAmount = 0;
        }
        // 将字符串转换为Double类型
        return orderPageVo.setTotalAmount(totalAmount);
    }

    /**
     * 删除产品通过id
     *
     * @param id id
     */
    @Override
    public void deleteProductById(final Long id) {
        productMapper.deleteById(id);
    }


    /**
     * 货架上产品
     *
     * @param dto dto
     */
    @Override
    public void shelvesProduct(final ShelvesProductDto dto) {
        productMapper.insert(BeanUtils.copyClassProperTies(dto, Product.class));
    }

    @Override
    public IPage<ProductVo> getProductPageVo(int pageNum, String prompt) {
        return productMapper.selectPage(new Page<>(pageNum, 15), new QueryWrapper<Product>()
                        .lambda()
                        .like(StringUtils.notEmpty(prompt), Product::getProductName, prompt)
                        .select(Product::getProductId, Product::getProductName, Product::getProductPrice, Product::getFrequency, Product::getCreatedTime)
                )
                .convert(c -> new ProductVo().setProductId(c.getProductId()).setProductName(c.getProductName()).setProductPrice(c.getProductPrice()).setFrequency(c.getFrequency()).setCreatedTime(c.getCreatedTime()));
    }

    /**
     * 生成二维码支付
     *
     * @param productId 产品id
     * @return {@link AlipayPayCodeVo}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlipayPayCodeVo generatePayQrCode(final Long productId) {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        //当前登录用户ID
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        //锁前缀
        final String lockPrefix = LockPrefix.ORDERS_PAY + currentLoginId;
        //上锁
        final boolean lock = lockHelper.lock(lockPrefix, timestamp);

        try {
            if (!lock) {
                throw new OrdersException(ExceptionMessages.PLACE_AN_ORDER_REPEATEDLY_ERR, 500);
            }
            final String key = OrderConstant.ALI_ORDER_PAY + currentLoginId + productId;
            if (redisUtils.doesItExist(key)) {
                final AlipayCacheStructure cache = (AlipayCacheStructure) redisUtils.getValue(key);
                //生成BASE64图片给前端
                return new AlipayPayCodeVo()
                        .setOrdersId(cache.getOrdersId())
                        .setQrCode(QRCodeGenerator.generateQRCode(cache.getUrl()))
                        .setProductPrice(cache.getProductPrice())
                        .setProductName(cache.getProductName())
                        .setCreatedTime(cache.getCreatedTime());
            }
            //商品是否存在
            final Product product = productMapper.selectOne(new QueryWrapper<Product>()
                    .lambda().eq(Product::getProductId, productId)
                    .select(Product::getProductId, Product::getProductPrice, Product::getFrequency, Product::getProductName)
            );
            if (product == null) {
                throw new OrdersException(ExceptionMessages.PRODUCT_NULL_ERR, 500);
            }
            //生成单号
            final String orderNo = idGeneratorUtils.getOrderNo();

            final Orders orders = new Orders()
                    .setOrdersId(orderNo)
                    // 0 待支付 1已支付 2 已回收
                    .setState(0)
                    .setProductPrice(product.getProductPrice())
                    .setProductName(product.getProductName())
                    .setProductId(productId)
                    .setFrequency(product.getFrequency())
                    .setUserId(currentLoginId);
            ordersMapper.insert(orders);
            //装载配置
            final AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
            alipayConfig.setFormat("json");
            alipayConfig.setCharset("UTF8");
            alipayConfig.setSignType("RSA2");
            alipayConfig.setAppId(appId);
            alipayConfig.setAlipayPublicKey(alipayPublicKey);
            alipayConfig.setPrivateKey(privateKey);
            //构建支付宝订单
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            //预构建请求
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(orderNo);
            //支付金额
            model.setTotalAmount(String.valueOf(product.getProductPrice()));
            //商品名称
            model.setSubject(product.getProductName());
            //5分钟过期
            model.setTimeoutExpress("5m");
            request.setBizModel(model);
            //支付宝回调地址
            request.setNotifyUrl(domain + "/public/callback/order");
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
//            log.info("支付宝生成信息:{}", response.getBody());
            //是否构建成功？ 构建成功则 创建二维码
            if (response.isSuccess()) {
                final AlipayCacheStructure cache = new AlipayCacheStructure()
                        .setCreatedTime(orders.getCreatedTime())
                        .setProductName(product.getProductName())
                        .setUrl(response.getQrCode())
                        .setProductPrice(product.getProductPrice())
                        .setOrdersId(orderNo);
                //缓存订单数据
                redisUtils.setValueTimeout(key, cache, 300);
                //添加至 待支付 队列中
                unpaidOrderQueue.addOrder(orderNo);
                //生成BASE64图片给前端
                //返回base64编码支付二维码图片
                return new AlipayPayCodeVo()
                        .setOrdersId(cache.getOrdersId())
                        .setQrCode(QRCodeGenerator.generateQRCode(cache.getUrl()))
                        .setProductPrice(cache.getProductPrice())
                        .setProductName(cache.getProductName())
                        .setCreatedTime(cache.getCreatedTime());
            } else {
                log.error("创建订单失败 订单号:{}, 错误信息：{}", orderNo, response.getBody());
                throw new AlipayApiException();
            }
        } catch (IOException | AlipayApiException | WriterException e) {
            throw new OrdersException(ExceptionMessages.BUILD_FAILED_PAY_ERR, 500);
        } finally {
            lockHelper.unlock(lockPrefix, timestamp);
        }

    }
//  double类型的元转为int类型分 用于微信支付
    public static int yuanToFenUsingBigDecimal(Double yuan) {
        BigDecimal yuanBD = new BigDecimal(Double.toString(yuan));
        BigDecimal fenBD = yuanBD.multiply(new BigDecimal(100));
        return fenBD.intValue();
    }
    public String getResourcesPath(String filePath) {
        String resourcePath;
        File file = new File(filePath);
        try {
            Resource resource = new ClassPathResource(filePath);
            InputStream inputStream = resource.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, file);
            resourcePath=file.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resourcePath;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeixinPayCodeVo generateWeixinPayQrCode(final Long productId){
        //装载微信支付配置
        Config weixinPayConfig =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
//                        .privateKeyFromPath(privateKeyPath)
                        .privateKeyFromPath(getResourcesPath("apiclient_key.pem"))
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3Key)
                        .build();
        final String timestamp = String.valueOf(System.currentTimeMillis());
        //当前登录用户ID
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        //锁前缀
        final String lockPrefix = LockPrefix.ORDERS_PAY + currentLoginId;
        //上锁
        final boolean lock = lockHelper.lock(lockPrefix, timestamp);

        try {
            if (!lock) {
                throw new OrdersException(ExceptionMessages.PLACE_AN_ORDER_REPEATEDLY_ERR, 500);
            }
            final String key = OrderConstant.WX_ORDER_PAY + currentLoginId + productId;
            if (redisUtils.doesItExist(key)) {
                final WeixinpayCacheStructure cache = (WeixinpayCacheStructure) redisUtils.getValue(key);
                //生成BASE64图片给前端
                return new WeixinPayCodeVo()
                        .setOrdersId(cache.getOrdersId())
                        .setQrCode(QRCodeGenerator.generateQRCode(cache.getUrl()))
                        .setProductPrice(cache.getProductPrice())
                        .setProductName(cache.getProductName())
                        .setCreatedTime(cache.getCreatedTime());
            }
            //商品是否存在
            final Product product = productMapper.selectOne(new QueryWrapper<Product>()
                    .lambda().eq(Product::getProductId, productId)
                    .select(Product::getProductId, Product::getProductPrice, Product::getFrequency, Product::getProductName)
            );
            if (product == null) {
                throw new OrdersException(ExceptionMessages.PRODUCT_NULL_ERR, 500);
            }
            //生成单号
            final String orderNo = idGeneratorUtils.getOrderNo();

            final Orders orders = new Orders()
                    .setOrdersId(orderNo)
                    // 0 待支付 1已支付 2 已回收
                    .setState(0)
                    .setProductPrice(product.getProductPrice())
                    .setProductName(product.getProductName())
                    .setProductId(productId)
                    .setFrequency(product.getFrequency())
                    .setUserId(currentLoginId);
            ordersMapper.insert(orders);
            // 构建service
            NativePayService service = new NativePayService.Builder().config(weixinPayConfig).build();
            PrepayRequest request = new PrepayRequest();
            Amount amount = new Amount();
            amount.setTotal(yuanToFenUsingBigDecimal(product.getProductPrice()));
            amount.setCurrency("CNY");
            request.setAmount(amount);
            request.setAppid(wxappId);
            request.setMchid(merchantId);
            request.setDescription(product.getProductName());
            request.setTimeExpire(java.time.OffsetDateTime.now().plusMinutes(5).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));
            request.setNotifyUrl(domain + "/public/wxcallback/order");
            request.setOutTradeNo(orderNo);
            // 调用下单方法，得到应答
            PrepayResponse response = service.prepay(request);
            // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
//            System.out.println(response.getCodeUrl());
            //是否构建成功？ 构建成功则 创建二维码
//            if (response.isSuccess()) {
            final WeixinpayCacheStructure cache = new WeixinpayCacheStructure()
                    .setCreatedTime(orders.getCreatedTime())
                    .setProductName(product.getProductName())
                    .setUrl(response.getCodeUrl())
                    .setProductPrice(product.getProductPrice())
                    .setOrdersId(orderNo);
            //缓存订单数据
            redisUtils.setValueTimeout(key, cache, 300);
            //添加至 待支付 队列中
            unpaidOrderQueue.addOrder(orderNo);
            //生成BASE64图片给前端
            //返回base64编码支付二维码图片
            return new WeixinPayCodeVo()
                    .setOrdersId(cache.getOrdersId())
                    .setQrCode(QRCodeGenerator.generateQRCode(cache.getUrl()))
                    .setProductPrice(cache.getProductPrice())
                    .setProductName(cache.getProductName())
                    .setCreatedTime(cache.getCreatedTime());
//            } else {
//                log.error("创建订单失败 订单号:{}, 错误信息：{}", orderNo, response.getBody());
//                throw new AlipayApiException();
//            }
        } catch (IOException | WriterException e) {
            throw new OrdersException(ExceptionMessages.BUILD_FAILED_PAY_ERR, 500);
        } finally {
            lockHelper.unlock(lockPrefix, timestamp);
        }

    }

    /**
     * 支付宝回调
     *
     * @param request 请求
     * @return {@link String}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String alipayPullback(final HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append((i == values.length - 1) ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }
        // 调用SDK验证签名
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF8", "RSA2");
        } catch (AlipayApiException e) {

            throw new RuntimeException(e);
        }
        // 验证成功
        if (signVerified) {

            String tradeStatus = request.getParameter("trade_status");
            log.info("回调结果:{}", tradeStatus);
            // 支付成功
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                final String outTradeNo = request.getParameter("out_trade_no");
                final Orders orders = ordersMapper.selectOne(new QueryWrapper<Orders>()
                        .lambda()
                        .eq(Orders::getOrdersId, outTradeNo)
                        .select(Orders::getFrequency, Orders::getUserId, Orders::getProductId)
                );
                if (orders != null) {
                    ordersMapper
                            .updateById(new Orders()
                                    .setOrdersId(outTradeNo)
                                    //已支付
                                    .setState(1)
                                    .setPayTime(LocalDateTime.now())
                            );
                    userMapper.update(null, new UpdateWrapper<User>()
                            .lambda()
                            .eq(User::getUserId, orders.getUserId())
                            .setSql("frequency = frequency +" + orders.getFrequency()));

                    redisUtils.delKey(OrderConstant.ALI_ORDER_PAY + orders.getUserId().toString() + orders.getProductId());
                }

                return "success";
            }
        } else {
            log.error("支付失败");
            return "fail";
        }
        return "fail";
    }

    //  微信支付回调
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String wxpayPullback(final HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        String line;
        StringBuilder requestBody = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        // 请求头Wechatpay-nonce
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        // 请求头Wechatpay-Timestamp
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        // 请求头Wechatpay-Signature
        String signature = request.getHeader(WECHAT_PAY_SIGNATURE);
        // 微信支付证书序列号
        String serial = request.getHeader(WECHAT_PAY_SERIAL);
        // 签名方式
        String signType = request.getHeader("Wechatpay-Signature-Type");

        // 构造 RequestParam
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .signType(signType)
                .body(String.valueOf(requestBody))
                .build();

        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(getResourcesPath("apiclient_key.pem"))
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();

        // 初始化 NotificationParser
        NotificationParser parser = new NotificationParser(config);

        // 以支付通知回调为例，验签、解密并转换成 Transaction
        Transaction transaction = parser.parse(requestParam, Transaction.class);
//        RefundNotification refundNotification = parser.parse(requestParam, RefundNotification.class);
        // 修改订单之前，主动去微信支付主动查询订单是否已经支付成功，这一步不能少，防止有人假冒主动post接口。

        /*
          收到支付成功的回调：这里进行处理业务
         */
        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            final String outTradeNo = transaction.getOutTradeNo();
            final Orders orders = ordersMapper.selectOne(new QueryWrapper<Orders>()
                    .lambda()
                    .eq(Orders::getOrdersId, outTradeNo)
                    .select(Orders::getFrequency, Orders::getUserId, Orders::getProductId)
            );
            if (orders != null) {
                ordersMapper
                        .updateById(new Orders()
                                .setOrdersId(outTradeNo)
                                //已支付
                                .setState(1)
                                .setPayTime(LocalDateTime.now())
                        );
                userMapper.update(null, new UpdateWrapper<User>()
                        .lambda()
                        .eq(User::getUserId, orders.getUserId())
                        .setSql("frequency = frequency +" + orders.getFrequency()));

                redisUtils.delKey(OrderConstant.WX_ORDER_PAY + orders.getUserId().toString() + orders.getProductId());
            }
                return "success";
        } else {
            log.info("支付未成功：" + transaction.getTradeState());
            return "fail";
        }
    }


    /**
     * 付款状态
     *
     * @param orderNo 订单没有
     * @return {@link String}
     */
    @Override
    public String paymentStatus(final String orderNo) {
        final Orders orders = ordersMapper.selectOne(new QueryWrapper<Orders>()
                .lambda()
                .eq(Orders::getOrdersId, orderNo)
                .select(Orders::getState)
        );
        if (orders != null) {
            if (orders.getState() == 0) {
                return OrderConstant.BE_PAID;
            } else if (orders.getState() == 1) {
                return OrderConstant.PAID;
            } else {
                return OrderConstant.IS_CLOSED;
            }
        } else {
            return OrderConstant.IS_CLOSED;
        }

    }

    /**
     * 生成支付宝电脑网站支付
     *
     * @param productId 产品id
     * @return {@link AlipayPagePayVo}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlipayPagePayVo generatePagePay(final Long productId) {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        //当前登录用户ID
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        //锁前缀
        final String lockPrefix = LockPrefix.ORDERS_PAY + currentLoginId;
        //上锁
        final boolean lock = lockHelper.lock(lockPrefix, timestamp);

        try {
            if (!lock) {
                throw new OrdersException(ExceptionMessages.PLACE_AN_ORDER_REPEATEDLY_ERR, 500);
            }
            final String key = OrderConstant.ALI_ORDER_PAY + currentLoginId + productId;
            if (redisUtils.doesItExist(key)) {
                final AlipayCacheStructure cache = (AlipayCacheStructure) redisUtils.getValue(key);
                //返回支付表单
                return new AlipayPagePayVo()
                        .setOrdersId(cache.getOrdersId())
                        .setProductPrice(cache.getProductPrice())
                        .setProductName(cache.getProductName())
                        .setCreatedTime(cache.getCreatedTime())
                        .setPayForm(cache.getUrl());
            }
            //商品是否存在
            final Product product = productMapper.selectOne(new QueryWrapper<Product>()
                    .lambda().eq(Product::getProductId, productId)
                    .select(Product::getProductId, Product::getProductPrice, Product::getFrequency, Product::getProductName)
            );
            if (product == null) {
                throw new OrdersException(ExceptionMessages.PRODUCT_NULL_ERR, 500);
            }
            //生成单号
            final String orderNo = idGeneratorUtils.getOrderNo();

            final Orders orders = new Orders()
                    .setOrdersId(orderNo)
                    // 0 待支付 1已支付 2 已回收
                    .setState(0)
                    .setProductPrice(product.getProductPrice())
                    .setProductName(product.getProductName())
                    .setProductId(productId)
                    .setFrequency(product.getFrequency())
                    .setUserId(currentLoginId);
            ordersMapper.insert(orders);
            //装载配置
            final AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
            alipayConfig.setFormat("json");
            alipayConfig.setCharset("UTF8");
            alipayConfig.setSignType("RSA2");
            alipayConfig.setAppId(appId);
            alipayConfig.setAlipayPublicKey(alipayPublicKey);
            alipayConfig.setPrivateKey(privateKey);
            //构建支付宝订单
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            //构建电脑网站支付请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(orderNo);
            //支付金额
            model.setTotalAmount(String.valueOf(product.getProductPrice()));
            //商品名称
            model.setSubject(product.getProductName());
            //5分钟过期
            model.setTimeoutExpress("5m");
            //设置产品码
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);
            //支付宝回调地址
            request.setNotifyUrl(domain + "/public/callback/order");
            //设置跳转地址
            request.setReturnUrl(domain + "/public/pay/result");
            
            try {
                //调用SDK生成表单
                String form = alipayClient.pageExecute(request).getBody();
                final AlipayCacheStructure cache = new AlipayCacheStructure()
                        .setCreatedTime(orders.getCreatedTime())
                        .setProductName(product.getProductName())
                        .setUrl(form)
                        .setProductPrice(product.getProductPrice())
                        .setOrdersId(orderNo);
                //缓存订单数据
                redisUtils.setValueTimeout(key, cache, 300);
                //添加至 待支付 队列中
                unpaidOrderQueue.addOrder(orderNo);
                //返回支付表单
                return new AlipayPagePayVo()
                        .setOrdersId(cache.getOrdersId())
                        .setProductPrice(cache.getProductPrice())
                        .setProductName(cache.getProductName())
                        .setCreatedTime(cache.getCreatedTime())
                        .setPayForm(form);
            } catch (AlipayApiException e) {
                log.error("创建订单失败 订单号:{}, 错误信息：{}", orderNo, e.getMessage());
                throw new OrdersException(ExceptionMessages.BUILD_FAILED_PAY_ERR, 500);
            }
        } catch (Exception e) {
            throw new OrdersException(ExceptionMessages.BUILD_FAILED_PAY_ERR, 500);
        } finally {
            lockHelper.unlock(lockPrefix, timestamp);
        }
    }

}
