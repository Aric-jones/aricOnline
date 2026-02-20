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
     * 生成文章摘要（100字）
     */
    public String generateSummary(String content) {
        String truncated = content.length() > 3000 ? content.substring(0, 3000) : content;
        String result = callAiSync(
                "你是一个文章摘要生成助手。请根据用户提供的文章内容，生成一段100字左右的中文摘要。" +
                "要求：简洁明了，提炼核心内容，不要使用 Markdown 格式，不要加任何前缀（如'摘要：'），直接输出摘要文本。",
                "请为以下文章生成100字左右的摘要：\n\n" + truncated,
                0.3, 200
        );
        if (result != null && result.length() > 100) {
            result = result.substring(0, 100);
        }
        return result;
    }

    /**
     * 快速阅读（200字概要）
     */
    public String quickRead(String content) {
        String truncated = content.length() > 4000 ? content.substring(0, 4000) : content;
        return callAiSync(
                "你是一个文章速读助手。请根据文章内容，生成一段不超过200字的中文概要。" +
                "要求：抓住文章核心要点，语言简洁流畅，不要使用 Markdown 格式，不要加前缀，直接输出概要。",
                "请为以下文章生成200字以内的快速阅读概要：\n\n" + truncated,
                0.3, 400
        );
    }

    /**
     * 生成文章标题
     */
    public String generateTitle(String content) {
        String truncated = content.length() > 3000 ? content.substring(0, 3000) : content;
        String result = callAiSync(
                "你是一个文章标题生成助手。请根据文章内容，生成一个简洁、有吸引力的中文标题。" +
                "要求：标题在5-25个字之间，不要加引号、书名号或其他标点包裹，直接输出标题文本。",
                "请为以下文章生成一个标题：\n\n" + truncated,
                0.7, 60
        );
        // 清理可能的引号包裹
        if (result != null) {
            result = result.trim().replaceAll("^[\"'《「【]|[\"'》」】]$", "");
        }
        return result;
    }

    /**
     * 自动选择分类
     */
    public String autoCategory(String content, String categories) {
        String truncated = content.length() > 2000 ? content.substring(0, 2000) : content;
        String prompt = "你是一个文章分类助手。根据文章内容，从给定的分类列表中选择最合适的一个分类。\n" +
                "如果没有合适的分类，可以建议一个新的分类名称（不超过10个字）。\n" +
                "要求：只输出分类名称，不要加任何解释。";
        String userMsg = "已有分类列表：" + (categories != null ? categories : "无") +
                "\n\n文章内容：\n" + truncated;
        return callAiSync(prompt, userMsg, 0.3, 30);
    }

    /**
     * 自动选择标签
     */
    public List<String> autoTags(String content, String tags) {
        String truncated = content.length() > 2000 ? content.substring(0, 2000) : content;
        String prompt = "你是一个文章标签生成助手。根据文章内容，选择或生成1-3个合适的标签。\n" +
                "优先从已有标签列表中选择，如果没有合适的可以创建新标签。\n" +
                "要求：只输出标签名，用逗号分隔，不要加解释。例如：Java,Spring Boot,数据库";
        String userMsg = "已有标签列表：" + (tags != null ? tags : "无") +
                "\n\n文章内容：\n" + truncated;
        String result = callAiSync(prompt, userMsg, 0.3, 60);
        if (result == null || result.trim().isEmpty()) {
            return List.of();
        }
        // 按逗号或中文逗号分割
        String[] tagArray = result.trim().split("[,，、]");
        List<String> tagList = new java.util.ArrayList<>();
        for (String t : tagArray) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                tagList.add(trimmed);
            }
        }
        return tagList.size() > 3 ? tagList.subList(0, 3) : tagList;
    }

    /**
     * 一键优化文章（流式输出）
     */
    public SseEmitter optimizeArticle(String content) {
        String truncated = content != null && content.length() > 8000 ? content.substring(0, 8000) : content;
        JSONArray messagesArray = new JSONArray();
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个专业的文章优化助手。请对用户提供的文章进行优化，包括：\n" +
                "1. 修正错别字和语法错误\n" +
                "2. 优化语句表达，使其更加流畅\n" +
                "3. 保持原文的 Markdown 格式\n" +
                "4. 保持原文的核心内容和结构不变\n" +
                "5. 直接输出优化后的完整文章，不要加任何解释或前缀");
        messagesArray.add(systemMsg);
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", "请优化以下文章：\n\n" + truncated);
        messagesArray.add(userMsg);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", messagesArray);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 8192);

        return doStreamCall(requestBody);
    }

    /**
     * 通用同步 AI 调用
     */
    /**
     * 供其他 Service 调用的同步 AI 方法
     */
    public String callAiSyncPublic(String systemPrompt, String userMessage, double temperature, int maxTokens) {
        return callAiSync(systemPrompt, userMessage, temperature, maxTokens);
    }

    private String callAiSync(String systemPrompt, String userMessage, double temperature, int maxTokens) {
        JSONArray messagesArray = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messagesArray.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messagesArray.add(userMsg);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", deepSeekProperties.getModel());
        requestBody.put("messages", messagesArray);
        requestBody.put("stream", false);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        Request request = new Request.Builder()
                .url(deepSeekProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                log.error("AI 调用失败: {} {}", response.code(), body);
                throw new RuntimeException("AI 调用失败: " + response.code());
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                if (message != null) {
                    String result = message.getString("content");
                    return result != null ? result.trim() : "";
                }
            }
            return "";
        } catch (IOException e) {
            log.error("AI 调用异常", e);
            throw new RuntimeException("AI 调用异常: " + e.getMessage());
        }
    }

    /**
     * 通用流式 AI 调用
     */
    private SseEmitter doStreamCall(JSONObject requestBody) {
        SseEmitter emitter = new SseEmitter(300_000L);

        Request request = new Request.Builder()
                .url(deepSeekProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

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
                    JSONObject jsonData = JSON.parseObject(data);
                    JSONArray choices = jsonData.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        if (delta != null && delta.containsKey("content")) {
                            String content = delta.getString("content");
                            if (content != null) {
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
                        errorMsg = "AI 服务返回错误: " + response.code() + " " + (response.body() != null ? response.body().string() : "");
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
            public void onClosed(EventSource eventSource) {}
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        return emitter;
    }

    /**
     * 流式对话
     */
    public SseEmitter chatStream(List<Map<String, String>> messages) {
        JSONArray messagesArray = new JSONArray();
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", deepSeekProperties.getSystemPrompt());
        messagesArray.add(systemMsg);
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

        return doStreamCall(requestBody);
    }
}
