package com.cn.bdth.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cn.bdth.common.ChatGptCommon;
import com.cn.bdth.common.StableDiffusionCommon;
import com.cn.bdth.common.WxSubscribe;
import com.cn.bdth.common.WxSubscribeTemplate;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.entity.Drawing;
import com.cn.bdth.entity.User;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.exceptions.ExceptionMessages;
import com.cn.bdth.mapper.DrawingMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.model.SdDrawingModel;
import com.cn.bdth.structure.DrawingSdQueueStructure;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.BaiduTranslationUtil;
import com.cn.bdth.utils.WeChatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.cn.bdth.utils.StringUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * 绘图任务监听器
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Component
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DrawingTaskListener {

    private final RedisTemplate<String, Object> redisTemplate;

    private final StableDiffusionCommon stableDiffusionCommon;

    private final ChatGptCommon chatGptCommon;

    private final WebClient.Builder webClientBuilder;
    private final Semaphore semaphoreSd = new Semaphore(1);

    private final WxSubscribeTemplate wxSubscribeTemplate;

    private final WxSubscribe wxSubscribe;

    private final BaiduTranslationUtil baiduTranslationUtil;

    private final AliUploadUtils aliUploadUtils;

    private final UserMapper userMapper;

    private final DrawingMapper drawingMapper;

    private final WeChatUtils weChatUtils;

    private final WebClient.Builder webClient;
    /**
     * 监听SD任务队列
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sdListening() {
        new Thread(() -> {
            while (true) {
                DrawingSdQueueStructure drawingSdQueueStructure;
                try {
                    semaphoreSd.acquire();
                    // 尝试获取信号量许可
                    drawingSdQueueStructure = (DrawingSdQueueStructure) redisTemplate.opsForList().rightPop(ServerConstant.DRAWING_SD_TASK_QUEUE, 2, TimeUnit.SECONDS);
                    if (drawingSdQueueStructure != null) {
                        final SdDrawingModel model = drawingSdQueueStructure.getSdDrawingModel();
                        //尝试翻译
                        try {
                            model.setPrompt(baiduTranslationUtil.englishTranslation(model.getPrompt()));
                        } catch (Exception e) {
                            log.warn("绘图时调用百度翻译翻译失败 本次绘图将采用原文提示词");
                        }
                        invokeSdDrawingApi(drawingSdQueueStructure);
                    }
                } catch (InterruptedException e) {
                    log.error("信号量异常 原因:{} 位置:{}", e.getMessage(), e.getClass());
                } finally {
                    semaphoreSd.release(); // 释放信号量许可
                }
            }
        }).start();
    }


    /**
     * 调用 SD绘图API
     */
    public void invokeSdDrawingApi(final DrawingSdQueueStructure structure) {
        String imageUri = null;
        final SdDrawingModel model = structure.getSdDrawingModel();
        final Map<String, Object> map = new HashMap<>();
//        final MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.put("prompt", model.getPrompt());
        if (model.getDrawModel().equals("DALL·E 3")) {
            map.put("model", "dall-e-3");
        } else {
            map.put("model", "dall-e-2");
        }
        map.put("n", 1);
        map.put("size", model.getWidth()+"x"+model.getHeight());
        try {
            final String block;
            String finalImageUri = null;
            if (Objects.equals(model.getDrawModel(), "DALL·E 3") || Objects.equals(model.getDrawModel(), "DALL·E 2")) {
//                block = webClient.baseUrl("https://aigc456.top/v1")
                block = webClient.baseUrl(chatGptCommon.getChatGptStructure().getOpenAiPlusUrl())
//                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + "sk-obmeSMQttY61Ockk30Cb78529d374a5188Eb466dF185E0B0")
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + chatGptCommon.getChatGptStructure().getOpenPlusKey())
                        .codecs(item -> item.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)).build()
                        .post()
                        .uri("/images/generations")
                        .body(BodyInserters.fromValue(map))
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    final String errorCode = parseObject(errorBody).getString("error");
                                    final JSONObject jsonObject = parseObject(errorCode);
                                    connectionException(jsonObject.toString(), "1");
                                    return Mono.error(new RuntimeException());
                                })))
                        .bodyToMono(String.class).block();
                final String string = Objects.requireNonNull(parseObject(block)).getJSONArray("data").getJSONObject(0).getString("url");
                //下载图片
                imageUri = aliUploadUtils.uploadImageFromUrl(string, model.getDrawModel(), UUID.randomUUID() + ".jpg");
                finalImageUri = imageUri;
            } else {
                block = webClientBuilder.build()
                        .post()
                        .uri(stableDiffusionCommon.getStableDiffusionStructure().getSdUrl() + (structure.getIsType() == 0 ? ServerConstant.SD_DRAWING_TEXT : ServerConstant.SD_DRAWING_IMAGE))
                        .body(BodyInserters.fromValue(structure.getSdDrawingModel()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                //下载图片
//                imageUri = aliUploadUtils.uploadBase64(Objects.requireNonNull(JSONObject.parseObject(block)).getJSONArray("images").getString(0), FileEnum.PAINTING.getDec());
//                System.out.println(Objects.requireNonNull(parseObject(block)).getJSONArray("images").getString(1));
                JSONObject jsonObject = Objects.requireNonNull(JSONObject.parseObject(block));
                JSONArray imagesArray = jsonObject.getJSONArray("images");
                StringBuilder imageUriBuilder = new StringBuilder();
                for (int i = 0; i < imagesArray.size(); i++) {
                    String imageBase64 = imagesArray.getString(i);
                    // 处理每个图片的逻辑，例如上传、保存等
                    String currentImageUri = aliUploadUtils.uploadBase64(imageBase64, FileEnum.PAINTING.getDec());
                    // 将当前 imageUri 添加到 StringBuilder
                    imageUriBuilder.append(currentImageUri);
                    // 如果不是最后一个图片，添加分隔符 "|"
                    if (i < imagesArray.size() - 1) {
                        imageUriBuilder.append("|");
                    }
                }
                // 获取最终的 imageUri 字符串
                finalImageUri = imageUriBuilder.toString();
                // 这里可以将 finalImageUri 保存或进行其他操作
//                System.out.println("Final Image Uri: " + finalImageUri);
            }
            if (structure.getEnv() == ServerConstant.DRAWING_WECHAT) {
                //查看 具体为小程序端绘图还是web端绘图 如web端则不做持久化处理 且不做敏感图绘制
                weChatUtils.filterImage(imageUri);
                //把制作结果发给用户
                final JSONObject jsonObject = wxSubscribeTemplate.drawingOutcomeNotice(structure.getOpenId(), true, ExceptionMessages.DRAWING_SUCCEED, LocalDateTime.now());
                wxSubscribe.wxSubscribeMessages(jsonObject);
            }
            //回调至用户数据
            drawingMapper.updateById(
                    new Drawing()
                            .setDrawingId(structure.getDrawingId())
                            .setGenerateUrl(finalImageUri)
            );

        } catch (Exception e) {
            //处理错误
            anErrorOccurred(imageUri, structure);
        }

    }
    private void connectionException(String e, final String key) {
        //记录对话异常
        String str = "当前GPT密钥:" + key + "貌似有问题,在绘图时出现了:" + e + " 错误,请根据OPENAI提供的错误码进行相关操作";
        System.out.println(str);
    }
    public static class TsException {

        /**
         * The Exception id.
         */
        @TableId(type = IdType.AUTO)
        private Long exceptionId;

        /**
         * The Server name.
         */
        private String serverName;

        /**
         * The Level.
         */
        private String level;

        /**
         * The Cause.
         */
        private String cause;

        /**
         * The Created time.
         */
        @TableField(fill = FieldFill.INSERT)
        private LocalDateTime createdTime;

        /**
         * The Update time.
         */
        @TableField(fill = FieldFill.INSERT_UPDATE)
        private LocalDateTime updateTime;


    }

    private void anErrorOccurred(final String imageUri, final DrawingSdQueueStructure sdQueueStructure) {
        if (imageUri != null) {
            //删除线上资源
            aliUploadUtils.deleteFile(imageUri);
        }
        final Drawing drawing = drawingMapper.selectOne(new QueryWrapper<Drawing>()
                .lambda()
                .eq(Drawing::getDrawingId, sdQueueStructure.getDrawingId())
                .select(Drawing::getOriginalUrl)
        );
        if (drawing != null && drawing.getOriginalUrl() != null) {
            aliUploadUtils.deleteFile(drawing.getOriginalUrl());
        }
        //删除图片
        drawingMapper.deleteById(sdQueueStructure.getDrawingId());
        final StableDiffusionCommon.StableDiffusionStructure structure = stableDiffusionCommon.getStableDiffusionStructure();
        final String openId = sdQueueStructure.getOpenId();
        userMapper.update(null, new UpdateWrapper<User>()
                .lambda()
                .eq(User::getOpenId, openId)
                .setSql("frequency = frequency +" + structure.getSdImageFrequency())
        );
        if (sdQueueStructure.getEnv() == ServerConstant.DRAWING_WECHAT) {
            final JSONObject jsonObject = wxSubscribeTemplate.drawingOutcomeNotice(openId, false, ExceptionMessages.DRAWING_MISTAKE, LocalDateTime.now());
            wxSubscribe.wxSubscribeMessages(jsonObject);
        }
    }

}
