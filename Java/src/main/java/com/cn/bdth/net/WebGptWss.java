package com.cn.bdth.net;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.common.ChatGptCommon;
import com.cn.bdth.common.ControlCommon;
import com.cn.bdth.constants.AiModelConstant;
import com.cn.bdth.constants.AiTypeConstant;
import com.cn.bdth.dto.GptWebDto;
import com.cn.bdth.dto.GptVisionWebDto;
import com.cn.bdth.exceptions.CloseException;
import com.cn.bdth.exceptions.DrawingException;
import com.cn.bdth.exceptions.ExceptionMessages;
import com.cn.bdth.exceptions.FrequencyException;
import com.cn.bdth.model.GptVisionModel;
import com.cn.bdth.service.GptService;
import com.cn.bdth.utils.ChatUtils;
import com.cn.bdth.utils.SpringContextUtil;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.utils.WeChatPushUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * 长连接响应. (标准)
 *
 * @author bdth
 */
@Slf4j
@ServerEndpoint("/gpt-web/api/{token}/{model}")
@SuppressWarnings("all")
@Service
public class WebGptWss {

    private Session session;
    private static ChatUtils chatUtils;
    private static GptService gptService;
    private static ChatGptCommon chatGptCommon;
    private static ControlCommon controlCommon;
    private static WeChatPushUtil weChatPushUtil;
    
    // 思考内容状态跟踪
    private boolean isFirstThinkingContent = true;  // 是否是第一条思考内容
    private boolean hasSentThinkingContent = false; // 是否已经发送过思考内容
    private boolean hasSeenContent = false; // 是否已经看到普通内容
    private StringBuilder thinkingBuffer = new StringBuilder(); // 缓存思考内容
    private int continuousEmptyDelta = 0; // 连续空delta计数器


    @OnOpen
    public void onOpen(final Session session, @PathParam("token") String token) {
        try {
            assert session.getId() != null;
            assert StpUtil.getLoginIdByToken(token) != null;
        } catch (Exception e) {
            return;
        }
        this.session = session;
        if (gptService == null) {
            initDependencies();
        }
    }

    private void initDependencies() {
        controlCommon = (ControlCommon) SpringContextUtil.getBean("controlCommon");
        chatUtils = (ChatUtils) SpringContextUtil.getBean("chatUtils");
        gptService = (GptService) SpringContextUtil.getBean("gptServiceImpl");
        chatGptCommon = (ChatGptCommon) SpringContextUtil.getBean("chatGptCommon");
        weChatPushUtil = (WeChatPushUtil) SpringContextUtil.getBean("weChatPushUtil");
    }

    @OnMessage
    public void onMessage(final String message, final @PathParam("token") String token, @PathParam("model") String model) {
        try {
            final GptWebDto gptWebDto = JSONObject.parseObject(message, GptWebDto.class);

            final Long userId = UserUtils.getLoginIdByToken(token);

            chatUtils.lastOperationTime(userId);

            final ChatGptCommon.ChatGptStructure chatGptStructure = chatGptCommon.getChatGptStructure();

            final String s = chatUtils.drawingCueWord(gptWebDto.getMessages());

            if (s == null) {
                boolean isAdvancedModel = !AiTypeConstant.BASIC.equals(model);

                final Long frequency = controlCommon.getControl().getEnableGptPlus() ? (isAdvancedModel ? chatGptStructure.getGptPlusFrequency() : chatGptStructure.getGptFrequency()) : chatGptStructure.getGptFrequency();

                chatUtils.deplete(frequency, userId);
                model = switch (model) {
                    case AiTypeConstant.BASIC -> AiModelConstant.BASIC;
                    case AiTypeConstant.BASIC_ALL -> AiModelConstant.BASIC_ALL;
                    case AiTypeConstant.ADVANCED -> AiModelConstant.ADVANCED;
                    case AiTypeConstant.VISION -> AiModelConstant.VISION;
                    case AiTypeConstant.PLUS -> AiModelConstant.PLUS;
                    case AiTypeConstant.MOONSHOT -> AiModelConstant.MOONSHOT;
                    default -> model; // 如果model不匹配任何常量，则保持不变
                };
                if (model.equals(AiModelConstant.VISION)) {
                    final GptVisionWebDto visionGptWebDto = JSONObject.parseObject(message, GptVisionWebDto.class);
                    //与识图GPT建立通信
//                    GptVisionModel gptVisionModel = chatUtils.conversionStructureToVision(visionGptWebDto);
                    GptVisionModel gptVisionModel = chatUtils.conversionStructureToVision(visionGptWebDto);
                    String finalModel = model;
                    gptService.concatenationVisionGpt(gptVisionModel, model, isAdvancedModel, chatGptStructure)
                            .doFinally(signal -> handleWebSocketCompletion())
                            .subscribe(data -> {
                                if (JSON.isValid(data)) {
                                    JSONObject jsonObject = JSONObject.parseObject(data);
                                    JSONArray choices = jsonObject.getJSONArray("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                        if (delta.containsKey("content")) {
                                            // 可能会抛出关闭异常
                                            try {
                                                this.session.getBasicRemote().sendText(delta.getString("content"));
                                            } catch (Exception e) {
                                                //用户可能手动端口连接
                                                throw new CloseException();
                                            }
                                        }
                                    }
                                }
                            }, throwable -> {
                                //为 Close异常时 过滤
                                if (!(throwable instanceof CloseException)) {
                                    chatUtils.compensate(frequency, userId);
                                    log.error("TSGPT调用GPT时出现异常 异常信息:{} ", throwable.getMessage());
                                    weChatPushUtil.wxSend("TSGPT调用GPT时出现异常 异常信息: " + throwable.getMessage()+"模型:"+ finalModel);
                                    appointSendingSystem(ExceptionMessages.GPT_TIMEOUT);
                                }
                            });
                } else {
                    //与GPT建立通信
                    String finalModel1 = model;
                    gptService.concatenationDiyGpt(chatUtils.conversionStructure(gptWebDto), model, isAdvancedModel, chatGptStructure)
                            .doFinally(signal -> handleWebSocketCompletion())
                            .subscribe(data -> {
                                if (JSON.isValid(data)) {
                                    JSONObject jsonObject = JSONObject.parseObject(data);
                                    JSONArray choices = jsonObject.getJSONArray("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                        // 检测是否有reasoning_content
                                        boolean hasThinkingContent = delta.containsKey("reasoning_content") && delta.getString("reasoning_content").length() > 0;
                                        // 检测是否有content
                                        boolean hasContent = delta.containsKey("content") && delta.getString("content").length() > 0;
                                        
                                        // 处理思考内容
                                        if (hasThinkingContent) {
                                            continuousEmptyDelta = 0; // 重置空delta计数
                                            try {
                                                // 如果是第一条思考内容，添加<think>标签
                                                if (isFirstThinkingContent) {
                                                    isFirstThinkingContent = false;
                                                    hasSentThinkingContent = true;
                                                    // 发送带标签的思考内容
                                                    this.session.getBasicRemote().sendText("<think>\n" + delta.getString("reasoning_content"));
                                                    // 缓存思考内容
                                                    thinkingBuffer.append(delta.getString("reasoning_content"));
                                                } else {
                                                    // 发送思考内容
                                                    this.session.getBasicRemote().sendText(delta.getString("reasoning_content"));
                                                    // 缓存思考内容
                                                    thinkingBuffer.append(delta.getString("reasoning_content"));
                                                }
                                            } catch (Exception e) {
                                                throw new CloseException();
                                            }
                                        } else if (hasContent) {
                                            // 有普通内容，判断是否需要结束思考标签
                                            try {
                                                if (hasSentThinkingContent && !hasSeenContent) {
                                                    // 如果之前有思考内容，且这是第一个普通内容
                                                    if (thinkingBuffer.length() > 20) {
                                                        // 只有当思考内容足够长时，才认为思考真正结束
                                                        hasSeenContent = true;
                                                        this.session.getBasicRemote().sendText("\n</think>\n" + delta.getString("content"));
                                                    } else {
                                                        // 思考内容太短，可能是false positive，直接发送普通内容
                                                        this.session.getBasicRemote().sendText(delta.getString("content"));
                                                    }
                                                } else {
                                                    // 正常发送普通内容
                                                    this.session.getBasicRemote().sendText(delta.getString("content"));
                                                }
                                            } catch (Exception e) {
                                                throw new CloseException();
                                            }
                                        } else {
                                            // 空delta处理
                                            continuousEmptyDelta++;
                                            
                                            // 如果连续3个空delta，并且有思考内容但还没结束思考标签
                                            if (continuousEmptyDelta >= 3 && hasSentThinkingContent && !hasSeenContent && thinkingBuffer.length() > 10) {
                                                try {
                                                    // 标记已看到内容，添加结束标签
                                                    hasSeenContent = true;
                                                    this.session.getBasicRemote().sendText("\n</think>\n");
                                                } catch (Exception e) {
                                                    throw new CloseException();
                                                }
                                            }
                                            
                                            // 处理finish_reason
                                            if (delta.containsKey("finish_reason") && delta.getString("finish_reason") != null 
                                                && hasSentThinkingContent && !hasSeenContent) {
                                                try {
                                                    hasSeenContent = true;
                                                    this.session.getBasicRemote().sendText("\n</think>\n");
                                                } catch (Exception e) {
                                                    throw new CloseException();
                                                }
                                            }
                                        }
                                    }
                                }
                            }, throwable -> {
                                //为 Close异常时 过滤
                                if (!(throwable instanceof CloseException)) {
                                    chatUtils.compensate(frequency, userId);
                                    log.error("TSGPT调用GPT时出现异常 异常信息:{} ", throwable.getMessage());
                                    weChatPushUtil.wxSend("TSGPT调用GPT时出现异常 异常信息: " + throwable.getMessage()+"模型:"+ finalModel1);
                                    appointSendingSystem(ExceptionMessages.GPT_TIMEOUT);
                                }
                            });
                }
            } else {
                Long frequency = null;
                try {
                    frequency = chatGptStructure.getGptTextImageFrequency();
                    chatUtils.deplete(frequency, userId);
                    final String context = gptService.drawAccordingGpt(s, chatGptStructure);
                    this.session.getBasicRemote().sendText(context);
                } catch (DrawingException e) {
                    chatUtils.compensate(frequency, userId);
                    appointSendingSystem(ExceptionMessages.GPT_TIMEOUT);
                } finally {
                    handleWebSocketCompletion();
                }
            }


        } catch (FrequencyException e) {
            appointSendingSystem(e.getMessage());
            handleWebSocketCompletion();
        } catch (Exception e) {
            log.error(" 与 OPEN Ai建立连接失败 原因:{}", e.getMessage());
            appointSendingSystem(ExceptionMessages.GPT_ERR);
            handleWebSocketCompletion();
        }
    }

    @OnClose
    public void handleWebSocketCompletion() {
        try {
            // 重置状态
            this.isFirstThinkingContent = true;
            this.hasSentThinkingContent = false;
            this.hasSeenContent = false;
            this.thinkingBuffer = new StringBuilder();
            this.continuousEmptyDelta = 0;
            this.session.close();
        } catch (IOException e) {
            log.error("关闭 GPT WebSocket 会话失败.", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.warn("GPT websocket出现异常 原因:{}", throwable.getMessage());
        //打印堆栈
        //      throwable.printStackTrace();
    }

    public void appointSendingSystem(final String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (Exception e) {

        }
    }


}
