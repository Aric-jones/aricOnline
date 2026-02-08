package com.ican.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek AI 配置属性
 *
 * @author ican
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekProperties {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API 地址
     */
    private String apiUrl = "https://api.deepseek.com/chat/completions";

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * 系统提示词
     */
    private String systemPrompt = "你是一个博客智能助手，帮助用户解答技术问题。请用简洁、专业的中文回答，支持 Markdown 格式。";
}
