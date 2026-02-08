package com.ican.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ican.config.properties.DeepSeekProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 对话服务
 * <p>
 * 通过 OkHttp SSE 调用 DeepSeek API，将流式结果转发给前端 SseEmitter。
 *
 * @author ican
 */
@Slf4j
@Service
public class AiService {

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 流式对话
     *
     * @param messages 对话消息列表，每条包含 role 和 content
     * @return SseEmitter 用于向前端推送流式数据
     */
    public SseEmitter chatStream(List<Map<String, String>> messages) {
        // 超时设置为 3 分钟
        SseEmitter emitter = new SseEmitter(180_000L);

        // 构建请求体：在用户消息之前加上系统提示词
        JSONArray messagesArray = new JSONArray();
        // 系统提示词
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", deepSeekProperties.getSystemPrompt());
        messagesArray.add(systemMsg);
        // 用户消息
        for (Map<String, String> msg : messages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("role", msg.get("role"));
            jsonMsg.put("content", msg.get("content"));
            messagesArray.add(jsonMsg);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", messagesArray);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2048);

        Request request = new Request.Builder()
                .url(deepSeekProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        // 创建 SSE 事件源
        EventSource.Factory factory = EventSources.createFactory(httpClient);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                try {
                    if ("[DONE]".equals(data)) {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                        return;
                    }
                    // 解析 DeepSeek 返回的 SSE 数据
                    JSONObject jsonData = JSON.parseObject(data);
                    JSONArray choices = jsonData.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        if (delta != null && delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null) {
                                // 用 JSON 包装内容，避免换行符被 SSE 拆成多行导致前端丢失
                                JSONObject chunk = new JSONObject();
                                chunk.put("content", content);
                                emitter.send(SseEmitter.event().data(chunk.toJSONString()));
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("SSE 发送失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorMsg = "AI 服务调用失败";
                if (response != null) {
                    try {
                        errorMsg = "AI 服务返回错误: " + response.code() + " " + response.body().string();
                    } catch (Exception e) {
                        errorMsg = "AI 服务返回错误: " + response.code();
                    }
                }
                log.error(errorMsg, t);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR] " + errorMsg));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                // 连接关闭
            }
        });

        // 超时 & 完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        return emitter;
    }
}
