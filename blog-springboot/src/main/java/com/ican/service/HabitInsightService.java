package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ican.entity.Habit;
import com.ican.entity.HabitRecord;
import com.ican.mapper.HabitMapper;
import com.ican.mapper.HabitRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * HabitInsightService
 *
 * @author Aric
 */
@Service
public class HabitInsightService {

    @Autowired
    private HabitMapper habitMapper;

    @Autowired
    private HabitRecordMapper habitRecordMapper;

    @Autowired
    private AiService aiService;

    @Autowired
    private AiPromptService aiPromptService;

    @Autowired
    private TodoService todoService;

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public String generateInsight(List<Integer> habitIds) {
        Integer userId = currentUserId();
        LocalDate now = LocalDate.now();
        String start = now.minusDays(29).toString();
        String end = now.toString();

        List<Habit> habits;
        if (habitIds != null && !habitIds.isEmpty()) {
            habits = habitMapper.selectList(
                    new LambdaQueryWrapper<Habit>()
                            .eq(Habit::getUserId, userId)
                            .in(Habit::getId, habitIds));
        } else {
            habits = habitMapper.selectList(
                    new LambdaQueryWrapper<Habit>()
                            .eq(Habit::getUserId, userId)
                            .eq(Habit::getIsActive, 1));
        }

        if (habits.isEmpty()) {
            return "暂无习惯数据，请先创建并记录习惯。";
        }

        StringBuilder context = new StringBuilder();
        context.append("分析时间范围: ").append(start).append(" ~ ").append(end).append("\n\n");

        for (Habit habit : habits) {
            List<HabitRecord> records = habitRecordMapper.selectRecordsByDateRange(
                    userId, habit.getId(), start, end);
            context.append("== 习惯: ").append(habit.getName());
            if (habit.getCategory() != null) context.append(" (").append(habit.getCategory()).append(")");
            if (habit.getUnit() != null) context.append(" 单位:").append(habit.getUnit());
            context.append(" ==\n");
            context.append("近30天记录数: ").append(records.size()).append("次\n");

            if (!records.isEmpty()) {
                double avgRating = records.stream()
                        .filter(r -> r.getRating() != null)
                        .mapToInt(HabitRecord::getRating)
                        .average().orElse(0);
                double totalValue = records.stream()
                        .filter(r -> r.getValue() != null)
                        .mapToDouble(r -> r.getValue().doubleValue())
                        .sum();
                context.append("总量: ").append(String.format("%.1f", totalValue));
                if (habit.getUnit() != null) context.append(habit.getUnit());
                context.append("  平均评分: ").append(String.format("%.1f", avgRating)).append("/5\n");

                context.append("详细记录:\n");
                for (HabitRecord r : records) {
                    context.append("  - ").append(r.getRecordDate())
                            .append(" 数值:").append(r.getValue())
                            .append(" 评分:").append(r.getRating());
                    if (r.getNote() != null && !r.getNote().isEmpty()) {
                        context.append(" 备注:").append(r.getNote());
                    }
                    context.append("\n");
                }
            } else {
                context.append("近30天无记录\n");
            }
            context.append("\n");
        }

        String systemPrompt = aiPromptService.getPromptContent("habit_insight");
        String result = aiService.callAiSyncPublic(systemPrompt, context.toString(), 0.7, 2000);
        todoService.saveAiRecord("habit_insight", result);
        return result;
    }
}