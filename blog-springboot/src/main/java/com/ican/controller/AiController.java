package com.ican.controller;

import com.ican.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI 对话控制器
 *
 * @author ican
 */
@Api(tags = "AI 模块")
@RestController
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI 流式对话（SSE）
     *
     * @param messages 消息列表，每条包含 role（user/assistant）和 content
     * @return SSE 事件流
     */
    @ApiOperation(value = "AI 流式对话")
    @PostMapping(value = "/ai/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestBody List<Map<String, String>> messages) {
        return aiService.chatStream(messages);
    }
}
