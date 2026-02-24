package com.ican.controller;

import com.ican.entity.AiPrompt;
import com.ican.model.vo.Result;
import com.ican.service.AiPromptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "AI提示词模块")
@RestController
public class AiPromptController {

    @Autowired
    private AiPromptService aiPromptService;

    @ApiOperation(value = "获取提示词列表")
    @GetMapping("/user/ai/prompt/list")
    public Result<List<AiPrompt>> listPrompts() {
        return Result.success(aiPromptService.listPrompts());
    }

    @ApiOperation(value = "保存提示词")
    @PostMapping("/user/ai/prompt")
    public Result<?> savePrompt(@RequestBody AiPrompt prompt) {
        aiPromptService.savePrompt(prompt);
        return Result.success();
    }

    @ApiOperation(value = "重置提示词为默认")
    @DeleteMapping("/user/ai/prompt/{promptKey}")
    public Result<?> resetPrompt(@PathVariable String promptKey) {
        aiPromptService.resetPrompt(promptKey);
        return Result.success();
    }

    @ApiOperation(value = "AI优化提示词")
    @PostMapping("/user/ai/prompt/optimize")
    public Result<String> optimizePrompt(@RequestBody Map<String, String> body) {
        String rawPrompt = body.get("content");
        return Result.success(aiPromptService.optimizePrompt(rawPrompt));
    }
}
