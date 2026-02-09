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
     */
    @ApiOperation(value = "AI 流式对话")
    @PostMapping(value = "/ai/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestBody List<Map<String, String>> messages) {
        return aiService.chatStream(messages);
    }

    /**
     * AI 生成文章摘要（100字）
     */
    @ApiOperation(value = "AI 生成文章摘要")
    @PostMapping("/admin/ai/summary")
    public Result<String> generateSummary(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        return Result.success(aiService.generateSummary(content));
    }

    /**
     * AI 快速阅读（200字概要，前台文章详情页使用）
     */
    @ApiOperation(value = "AI 快速阅读")
    @PostMapping("/ai/quick-read")
    public Result<String> quickRead(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        return Result.success(aiService.quickRead(content));
    }

    /**
     * AI 生成文章标题
     */
    @ApiOperation(value = "AI 生成文章标题")
    @PostMapping("/admin/ai/title")
    public Result<String> generateTitle(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        return Result.success(aiService.generateTitle(content));
    }

    /**
     * AI 自动选择分类
     */
    @ApiOperation(value = "AI 自动选择分类")
    @PostMapping("/admin/ai/category")
    public Result<String> autoCategory(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String categories = body.get("categories");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        return Result.success(aiService.autoCategory(content, categories));
    }

    /**
     * AI 自动选择标签
     */
    @ApiOperation(value = "AI 自动选择标签")
    @PostMapping("/admin/ai/tags")
    public Result<List<String>> autoTags(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String tags = body.get("tags");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("文章内容不能为空");
        }
        return Result.success(aiService.autoTags(content, tags));
    }

    /**
     * AI 一键优化文章（流式）
     */
    @ApiOperation(value = "AI 一键优化文章")
    @PostMapping(value = "/admin/ai/optimize", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter optimizeArticle(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        return aiService.optimizeArticle(content);
    }
}
