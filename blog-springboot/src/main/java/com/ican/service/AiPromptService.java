package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.AiPrompt;
import com.ican.mapper.AiPromptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AiPromptService
 *
 * @author Aric
 */
@Service
public class AiPromptService extends ServiceImpl<AiPromptMapper, AiPrompt> {

    @Autowired
    private AiPromptMapper aiPromptMapper;

    @Autowired
    private AiService aiService;

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    /**
     * 获取用户的提示词，如果用户未自定义则取系统默认(user_id=0)
     */
    public String getPromptContent(String promptKey) {
        Integer userId = currentUserId();
        AiPrompt userPrompt = aiPromptMapper.selectOne(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, userId)
                        .eq(AiPrompt::getPromptKey, promptKey));
        if (userPrompt != null) {
            return userPrompt.getContent();
        }
        AiPrompt defaultPrompt = aiPromptMapper.selectOne(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, 0)
                        .eq(AiPrompt::getPromptKey, promptKey));
        return defaultPrompt != null ? defaultPrompt.getContent() : "";
    }

    /**
     * 获取用户所有提示词配置（包含系统默认的）
     */
    public List<AiPrompt> listPrompts() {
        Integer userId = currentUserId();
        List<AiPrompt> userPrompts = aiPromptMapper.selectList(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, userId)
                        .orderByAsc(AiPrompt::getPromptKey));
        List<AiPrompt> defaultPrompts = aiPromptMapper.selectList(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, 0)
                        .orderByAsc(AiPrompt::getPromptKey));

        java.util.Set<String> userKeys = userPrompts.stream()
                .map(AiPrompt::getPromptKey)
                .collect(java.util.stream.Collectors.toSet());

        for (AiPrompt dp : defaultPrompts) {
            if (!userKeys.contains(dp.getPromptKey())) {
                userPrompts.add(dp);
            }
        }
        return userPrompts;
    }

    /**
     * 保存/更新用户提示词
     */
    public void savePrompt(AiPrompt prompt) {
        Integer userId = currentUserId();
        AiPrompt existing = aiPromptMapper.selectOne(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, userId)
                        .eq(AiPrompt::getPromptKey, prompt.getPromptKey()));
        if (existing != null) {
            existing.setName(prompt.getName());
            existing.setContent(prompt.getContent());
            this.updateById(existing);
        } else {
            prompt.setUserId(userId);
            this.save(prompt);
        }
    }

    /**
     * 重置为系统默认
     */
    public void resetPrompt(String promptKey) {
        Integer userId = currentUserId();
        aiPromptMapper.delete(
                new LambdaQueryWrapper<AiPrompt>()
                        .eq(AiPrompt::getUserId, userId)
                        .eq(AiPrompt::getPromptKey, promptKey));
    }

    /**
     * AI 优化提示词
     */
    public String optimizePrompt(String rawPrompt) {
        String systemPrompt = getPromptContent("optimize_prompt");
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "你是一位 AI 提示词工程专家。请优化以下提示词，使角色定位更清晰、输出要求更具体、格式语气更明确。保持原始意图不变，只输出优化后的提示词。";
        }
        return aiService.callAiSyncPublic(systemPrompt, "请优化以下提示词：\n\n" + rawPrompt, 0.7, 2000);
    }
}