package com.ican.controller;

import com.ican.model.vo.Result;
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

    /**
     * AI 生成文章摘要
     *
     * @param body 请求体，包含 content 字段（文章内容）
     * @return 100字左右的摘要
     */
    @ApiOperation(value = "AI 生成文章摘要")
    @PostMapping("/admin/ai/summary")
    public Result<String> generateSummary(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        String summary = aiService.generateSummary(content);
        return Result.success(summary);
    }
}
