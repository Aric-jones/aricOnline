package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.AiRecord;
import com.ican.entity.Diary;
import com.ican.entity.Todo;
import com.ican.mapper.AiRecordMapper;
import com.ican.mapper.DiaryMapper;
import com.ican.mapper.TodoMapper;
import com.ican.model.vo.PageResult;
import com.ican.model.vo.query.TodoQuery;
import com.ican.model.vo.request.TodoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TodoService extends ServiceImpl<TodoMapper, Todo> {

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private AiRecordMapper aiRecordMapper;

    @Autowired
    private AiService aiService;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public PageResult<Todo> listTodo(TodoQuery query) {
        Integer userId = currentUserId();
        Long count = todoMapper.selectTodoCount(query, userId);
        if (count == 0) {
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        List<Todo> list = todoMapper.selectTodoList(query, userId);
        return new PageResult<>(list, count);
    }

    public void addTodo(TodoReq req) {
        Todo todo = Todo.builder()
                .userId(currentUserId())
                .title(req.getTitle())
                .description(req.getDescription())
                .status(0)
                .priority(req.getPriority() != null ? req.getPriority() : 1)
                .category(req.getCategory())
                .startTime(parseDateTime(req.getStartTime()))
                .endTime(parseDateTime(req.getEndTime()))
                .build();
        this.save(todo);
    }

    public void updateTodo(TodoReq req) {
        Assert.notNull(req.getId(), "代办ID不能为空");
        Todo todo = this.getById(req.getId());
        Assert.notNull(todo, "代办不存在");
        Assert.isTrue(todo.getUserId().equals(currentUserId()), "无权操作");
        todo.setTitle(req.getTitle());
        todo.setDescription(req.getDescription());
        todo.setPriority(req.getPriority() != null ? req.getPriority() : todo.getPriority());
        todo.setCategory(req.getCategory());
        todo.setStartTime(parseDateTime(req.getStartTime()));
        todo.setEndTime(parseDateTime(req.getEndTime()));
        this.updateById(todo);
    }

    public void deleteTodo(Integer id) {
        Todo todo = this.getById(id);
        Assert.notNull(todo, "代办不存在");
        Assert.isTrue(todo.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
    }

    public void toggleStatus(Integer id) {
        Todo todo = this.getById(id);
        Assert.notNull(todo, "代办不存在");
        Assert.isTrue(todo.getUserId().equals(currentUserId()), "无权操作");
        if (todo.getStatus() == 0) {
            todo.setStatus(1);
            todo.setCompletedTime(LocalDateTime.now());
        } else {
            todo.setStatus(0);
            todo.setCompletedTime(null);
        }
        this.updateById(todo);
    }

    public List<Todo> getCalendarData(String startDate, String endDate) {
        return todoMapper.selectTodoByDateRange(currentUserId(), startDate, endDate);
    }

    /** 获取所有不重复的分类标签 */
    public List<String> getCategoryList() {
        Integer userId = currentUserId();
        return this.lambdaQuery()
                .eq(Todo::getUserId, userId)
                .isNotNull(Todo::getCategory)
                .select(Todo::getCategory)
                .list()
                .stream()
                .map(Todo::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** AI 日/周/月总结 */
    public String aiSummary(String type) {
        Integer userId = currentUserId();
        LocalDate now = LocalDate.now();
        String start, end;
        switch (type) {
            case "weekly":
                start = now.minusDays(6).toString();
                end = now.toString();
                break;
            case "monthly":
                start = now.withDayOfMonth(1).toString();
                end = now.toString();
                break;
            default:
                start = now.toString();
                end = now.toString();
        }
        List<Todo> todos = todoMapper.selectTodoByDateRange(userId, start + " 00:00:00", end + " 23:59:59");
        List<Todo> allInRange = this.lambdaQuery()
                .eq(Todo::getUserId, userId)
                .ge(Todo::getCreateTime, start + " 00:00:00")
                .le(Todo::getCreateTime, end + " 23:59:59")
                .list();
        if (allInRange.size() > todos.size()) {
            Set<Integer> ids = todos.stream().map(Todo::getId).collect(Collectors.toSet());
            allInRange.stream().filter(t -> !ids.contains(t.getId())).forEach(todos::add);
        }
        List<Diary> diaries = diaryMapper.selectDiaryByDateRange(userId, start, end);

        StringBuilder context = new StringBuilder();
        context.append("时间范围: ").append(start).append(" ~ ").append(end).append("\n\n");
        context.append("== 代办事项 ==\n");
        if (todos.isEmpty()) {
            context.append("无代办记录。\n");
        } else {
            for (Todo t : todos) {
                context.append("- [").append(t.getStatus() == 1 ? "已完成" : "未完成").append("] ")
                        .append(t.getTitle());
                if (t.getCategory() != null) context.append(" (").append(t.getCategory()).append(")");
                context.append("\n");
            }
        }
        context.append("\n== 日记 ==\n");
        if (diaries.isEmpty()) {
            context.append("无日记记录。\n");
        } else {
            for (Diary d : diaries) {
                context.append("[").append(d.getDiaryDate()).append("] ");
                if (d.getMood() != null) context.append("心情:").append(d.getMood()).append(" ");
                String content = d.getContent();
                if (content != null && content.length() > 200) content = content.substring(0, 200) + "...";
                context.append(content).append("\n");
            }
        }

        String typeLabel = "daily".equals(type) ? "日" : "weekly".equals(type) ? "周" : "月";
        String systemPrompt = "你是一位专注于个人能力提升的导师。请根据用户提供的代办事项和日记，生成一份" + typeLabel + "度总结报告。\n" +
                "报告要求：\n" +
                "1. 先总结本时段完成了什么、未完成什么\n" +
                "2. 分析时间利用效率和工作重点\n" +
                "3. 以「个人能力提升」为核心目标，给出 2-3 条具体的改进建议\n" +
                "4. 语气亲切专业，像一位关心你成长的导师\n" +
                "5. 使用 Markdown 格式，结构清晰";
        String result = aiService.callAiSyncPublic(systemPrompt, context.toString(), 0.7, 2000);
        saveAiRecord("summary_" + type, result);
        return result;
    }

    /** AI 改进建议 */
    public String aiSuggest() {
        Integer userId = currentUserId();
        LocalDate now = LocalDate.now();
        String start = now.minusDays(13).toString();
        String end = now.toString();
        List<Todo> todos = this.lambdaQuery()
                .eq(Todo::getUserId, userId)
                .ge(Todo::getCreateTime, start + " 00:00:00")
                .le(Todo::getCreateTime, end + " 23:59:59")
                .orderByDesc(Todo::getCreateTime)
                .last("LIMIT 50")
                .list();

        StringBuilder context = new StringBuilder();
        context.append("最近两周代办事项：\n");
        for (Todo t : todos) {
            context.append("- [").append(t.getStatus() == 1 ? "完成" : "待办").append("] ")
                    .append(t.getTitle())
                    .append(" 优先级:").append(t.getPriority() == 0 ? "低" : t.getPriority() == 1 ? "中" : "高");
            if (t.getCategory() != null) context.append(" 分类:").append(t.getCategory());
            context.append("\n");
        }

        String systemPrompt = "你是一位个人能力提升导师，请根据用户近两周的代办完成情况，从以下维度给出具体的改进建议：\n" +
                "1. 时间管理：是否有拖延、优先级分配是否合理\n" +
                "2. 技能提升：根据代办内容推测用户的发展方向，建议学习路径\n" +
                "3. 习惯养成：推荐有助于效率提升的小习惯\n" +
                "4. 目标设定：建议短期（1周）和中期（1月）目标\n" +
                "要求：语气亲切，建议具体可操作，使用 Markdown 格式";
        String result = aiService.callAiSyncPublic(systemPrompt, context.toString(), 0.7, 2000);
        saveAiRecord("suggest", result);
        return result;
    }

    /** 保存AI记录（按类型覆盖） */
    public void saveAiRecord(String recordType, String content) {
        Integer userId = currentUserId();
        AiRecord existing = aiRecordMapper.selectOne(
                new LambdaQueryWrapper<AiRecord>()
                        .eq(AiRecord::getUserId, userId)
                        .eq(AiRecord::getRecordType, recordType));
        if (existing != null) {
            existing.setContent(content);
            aiRecordMapper.updateById(existing);
        } else {
            AiRecord record = AiRecord.builder()
                    .userId(userId)
                    .recordType(recordType)
                    .content(content)
                    .build();
            aiRecordMapper.insert(record);
        }
    }

    /** 读取AI记录 */
    public AiRecord getAiRecord(String recordType) {
        return aiRecordMapper.selectOne(
                new LambdaQueryWrapper<AiRecord>()
                        .eq(AiRecord::getUserId, currentUserId())
                        .eq(AiRecord::getRecordType, recordType));
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(str, DT_FMT);
        } catch (Exception e) {
            try {
                return LocalDate.parse(str).atStartOfDay();
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
