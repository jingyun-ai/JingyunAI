package com.cn.bdth.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.core.http.HttpClient;
import com.aliyun.core.http.HttpMethod;
import com.aliyun.core.http.ProxyOptions;
import com.aliyun.httpcomponent.httpclient.ApacheAsyncHttpClientBuilder;
import com.aliyun.sdk.service.dysmsapi20170525.models.*;
import com.aliyun.sdk.service.dysmsapi20170525.*;
import com.cn.bdth.exceptions.ExceptionMessages;
import com.cn.bdth.exceptions.RegistrationException;
import com.google.gson.Gson;
import darabonba.core.RequestConfiguration;
import darabonba.core.client.ClientOverrideConfiguration;
import darabonba.core.utils.CommonUtil;
import darabonba.core.TeaPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//import javax.net.ssl.KeyManager;
//import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.io.*;

/**
 * @author: JasonV
 * @createTime: 2024/03/04 16:25
 */
@Component
public class AliSendSmsUtils {
    @Value("${ali-sms.accessKey}")
    private String accessKey;
    @Value("${ali-sms.accessSecret}")
    private String accessSecret;
    @Value("${ali-sms.signName}")
    private String signName;
    @Value("${ali-sms.templateCode}")
    private String templateCode;
    // 用于存储手机号和上一次发送时间的映射
    private static final HashMap<String, Long> phoneLastSentTime = new HashMap<>();

    public void send(String phone, String code) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        Long lastSentTime = phoneLastSentTime.get(phone);
        // 检查这个手机号是否发送过短信，以及是否已经过了60秒
        if (lastSentTime == null || (currentTimeMillis - lastSentTime) >= 60000) {
            phoneLastSentTime.put(phone, currentTimeMillis); // 更新发送时间
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(accessKey)
                    .accessKeySecret(accessSecret)
                    .build());

            AsyncClient client = AsyncClient.builder()
                    .region("cn-beijing-finance-1") // Region ID
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
                                    .setEndpointOverride("dysmsapi.aliyuncs.com")
                    )
                    .build();

            SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                    .signName(signName)
                    .templateCode(templateCode)
                    .phoneNumbers(phone)
                    .templateParam("{\"code\":\"" + code + "\"}")
                    .build();

            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            SendSmsResponse resp = response.get();
            client.close();

            // 记录发送时间
            phoneLastSentTime.put(phone, System.currentTimeMillis());
        } else {
            // 如果没有达到60秒，抛出一个异常
            throw new RegistrationException("请勿频繁发送短信", 500);
        }
    }

}