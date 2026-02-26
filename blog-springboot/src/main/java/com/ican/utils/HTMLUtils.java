package com.ican.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML工具
 *
 * @author ican
 */
@SuppressWarnings(value = "all")
public class HTMLUtils {

    // AC自动机敏感词过滤器（方案1：性能最优）
    private static final SensitiveWordFilter WORD_FILTER = new SensitiveWordFilter(getSensitiveWords());

    /**
     * 获取敏感词库
     * 从文件加载敏感词（文件位置：src/main/resources/sensitive-words.txt）
     */
    private static List<String> getSensitiveWords() {
        // 方式2：从文件加载（当前使用）
        try {
            InputStream inputStream = HTMLUtils.class.getClassLoader()
                .getResourceAsStream("sensitive-words.txt");
            if (inputStream != null) {
                List<String> words = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                ).lines()
                    .filter(line -> line != null && !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .collect(Collectors.toList());
                inputStream.close();
                if (!words.isEmpty()) {
                    return words;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 文件不存在或加载失败时，返回默认敏感词（兜底方案）
        return Arrays.asList("你妈", "傻逼");
        
        // 方式1：硬编码敏感词（已禁用，改用文件加载）
        /*
        return Arrays.asList(
            "敏感词1", "敏感词2", "敏感词3"
        );
        */
    }

    /**
     * 删除标签
     *
     * @param source 需要进行剔除HTML的文本
     * @return 过滤后的内容
     */
    public static String filter(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        
        // AC自动机敏感词过滤（替换为 ***）
        source = WORD_FILTER.filter(source, "*");
        
        // 保留图片标签
        source = source.replaceAll("(?!<(img).*?>)<.*?>", "")
                .replaceAll("(onload(.*?)=)", "")
                .replaceAll("(onerror(.*?)=)", "");
        return deleteHtmlTag(source);
    }

    /**
     * 删除标签
     *
     * @param source 文本
     * @return 过滤后的文本
     */
    public static String deleteHtmlTag(String source) {
        // 删除转义字符
        source = source.replaceAll("&.{2,6}?;", "");
        // 删除script标签
        source = source.replaceAll("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", "");
        // 删除style标签
        source = source.replaceAll("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", "");
        return source;
    }

}
