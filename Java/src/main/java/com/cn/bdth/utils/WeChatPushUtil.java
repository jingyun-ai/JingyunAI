package com.cn.bdth.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 企业微信推送工具类
 */
@Component
public class WeChatPushUtil {

    @Value("${wechat.corp.id}")
    private String corpId;

    @Value("${wechat.corp.secret}")
    private String corpSecret;

    @Value("${wechat.corp.agent-id}")
    private String agentId;

    /**
     * 获取访问令牌
     * @return 访问令牌
     */
    public String getAccessToken() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + corpSecret);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonObject = JSONObject.parseObject(response.toString());
            if (jsonObject.getInteger("errcode") != 0) {
                throw new RuntimeException(jsonObject.getString("errmsg"));
            }
            return jsonObject.getString("access_token");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 发送消息
     * @param content 消息内容
     */
    public void wxSend(String content) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                throw new RuntimeException("Failed to get access token");
            }

            JSONObject map = new JSONObject();
            map.put("touser", "@all");
            map.put("totag", "测试发消息");
            map.put("msgtype", "text");
            map.put("agentid", agentId);
            JSONObject contentMap = new JSONObject();
            contentMap.put("content",getTime() + content);
            map.put("text", contentMap);

            String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(map.toJSONString().getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            System.out.println("成功推送企业微信通知:"+response);

            reader.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getTime() {
        // 获取当前时间的 Calendar 实例
        Calendar calendar = Calendar.getInstance();

        // 设置时区为东八区（北京时区）
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        calendar.setTimeZone(timeZone);

        // 将当前时间增加8小时
        // calendar.add(Calendar.HOUR_OF_DAY, 8);

        // 获取增加后的时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // 打印增加后的时间
        return String.format("[%02d:%02d:%02d]", hour, minute, second);
    }
}
