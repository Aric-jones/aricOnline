package com.ican.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * AC自动机敏感词过滤器
 * 基于AC自动机算法实现，性能优于DFA
 *
 * @author Aric
 */
public class SensitiveWordFilter {

    private final AcNode root;

    public SensitiveWordFilter(List<String> sensitiveWords) {
        this.root = new AcNode();
        buildTrie(sensitiveWords);
        buildFailPointer();
    }

    /**
     * 过滤敏感词，替换为指定字符
     *
     * @param text 待过滤文本
     * @param replacement 替换字符，如 "*"
     * @return 过滤后的文本
     */
    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        char[] chars = text.toCharArray();
        char[] result = chars.clone();
        AcNode node = root;
        List<int[]> positions = new ArrayList<>();
        // 存储需要替换的位置范围 [start, end]

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // 忽略大小写
            c = Character.toLowerCase(c);

            // 跳转到失败指针，直到找到匹配或回到根节点
            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }

            // 移动到下一个节点
            if (node.children.containsKey(c)) {
                node = node.children.get(c);
            } else {
                node = root;
            }

            // 检查是否匹配到敏感词
            AcNode temp = node;
            while (temp != root) {
                if (temp.isEnd) {
                    // 找到敏感词，记录位置
                    int start = i - temp.length + 1;
                    int end = i;
                    positions.add(new int[]{start, end});
                    break; // 只替换最长匹配
                }
                temp = temp.fail;
            }
        }

        // 替换敏感词
        for (int[] pos : positions) {
            for (int i = pos[0]; i <= pos[1]; i++) {
                result[i] = replacement.charAt(0);
            }
        }

        return new String(result);
    }

    /**
     * 构建Trie树
     */
    private void buildTrie(List<String> words) {
        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            AcNode node = root;
            char[] chars = word.toLowerCase().toCharArray();
            for (char c : chars) {
                node.children.putIfAbsent(c, new AcNode());
                node = node.children.get(c);
            }
            node.isEnd = true;
            node.length = word.length();
        }
    }

    /**
     * 构建失败指针（BFS）
     */
    private void buildFailPointer() {
        Queue<AcNode> queue = new LinkedList<>();
        root.fail = null;

        // 根节点的子节点失败指针指向根节点
        for (AcNode child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }

        while (!queue.isEmpty()) {
            AcNode node = queue.poll();
            for (Map.Entry<Character, AcNode> entry : node.children.entrySet()) {
                char c = entry.getKey();
                AcNode child = entry.getValue();
                AcNode fail = node.fail;

                // 找到失败指针
                while (fail != null && !fail.children.containsKey(c)) {
                    fail = fail.fail;
                }

                if (fail != null) {
                    child.fail = fail.children.get(c);
                } else {
                    child.fail = root;
                }

                queue.offer(child);
            }
        }
    }


    /**
     * AC自动机节点
     */
    private static class AcNode {
        Map<Character, AcNode> children = new HashMap<>();
        AcNode fail;
        boolean isEnd = false;
        int length = 0;
    }
}
