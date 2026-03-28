package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.AiRecord;
import com.ican.entity.Diary;
import com.ican.entity.Todo;
import com.ican.entity.Habit;
import com.ican.entity.HabitRecord;
import com.ican.entity.Thinking;
import com.ican.entity.TimeBlock;
import com.ican.mapper.AiRecordMapper;
import com.ican.mapper.DiaryMapper;
import com.ican.mapper.HabitMapper;
import com.ican.mapper.HabitRecordMapper;
import com.ican.mapper.ThinkingMapper;
import com.ican.mapper.TodoMapper;
import com.ican.model.vo.PageResult;
import com.ican.model.vo.query.TodoQuery;
import com.ican.model.vo.request.TodoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TodoService
 *
 * @author Aric
 */
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

    @Autowired
    private AiPromptService aiPromptService;

    @Autowired
    private HabitMapper habitMapper;

    @Autowired
    private HabitRecordMapper habitRecordMapper;

    @Autowired
    private ThinkingMapper thinkingMapper;

    @Autowired
    private TimeBlockService timeBlockService;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public PageResult<Todo> listTodo(TodoQuery query) {
        if (query.getType() == null) {
            query.setType(0);
        }
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
                .type(req.getType() != null ? req.getType() : 0)
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
        // category 直接设置，允许空字符串
        todo.setCategory(req.getCategory());
        if (req.getType() != null) {
            todo.setType(req.getType());
        }
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
                .and(w -> w.isNull(Todo::getType).or().eq(Todo::getType, 0))
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
                .and(w -> w.isNull(Todo::getType).or().eq(Todo::getType, 0))
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

        List<Thinking> thinkings = thinkingMapper.selectList(
                new LambdaQueryWrapper<Thinking>()
                        .eq(Thinking::getUserId, userId)
                        .ge(Thinking::getCreateTime, start + " 00:00:00")
                        .le(Thinking::getCreateTime, end + " 23:59:59")
                        .orderByDesc(Thinking::getCreateTime));
        context.append("\n== 思考沉淀 ==\n");
        if (thinkings.isEmpty()) {
            context.append("无思考记录。\n");
        } else {
            for (Thinking tk : thinkings) {
                context.append("- 主题: ").append(tk.getTopic());
                String harvest = tk.getHarvest();
                if (harvest != null && harvest.length() > 200) harvest = harvest.substring(0, 200) + "...";
                context.append(" | 收获: ").append(harvest);
                if (tk.getRemark() != null && !tk.getRemark().isEmpty()) {
                    context.append(" | 备注: ").append(tk.getRemark());
                }
                context.append("\n");
            }
        }

        List<TimeBlock> timeBlocks = timeBlockService.listByDateRange(userId, start, end);
        context.append("\n== 时间安排 ==\n");
        if (timeBlocks.isEmpty()) {
            context.append("无时间安排记录。\n");
        } else {
            for (TimeBlock tb : timeBlocks) {
                context.append("- [").append(tb.getBlockDate()).append("] ")
                        .append(tb.getStartTime()).append("-").append(tb.getEndTime())
                        .append(" ").append(tb.getName())
                        .append(" (").append(tb.getCategory()).append(")\n");
            }
        }

        String typeLabel = "daily".equals(type) ? "日" : "weekly".equals(type) ? "周" : "月";
        String promptContent = aiPromptService.getPromptContent("summary");
        if (promptContent == null || promptContent.isEmpty()) {
            promptContent = "你是一个时间管理助手，请根据用户的代办事项、日记、思考沉淀和时间安排，生成一份{period}度总结报告。";
        }
        String systemPrompt = promptContent.replace("{period}", typeLabel);
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
                .and(w -> w.isNull(Todo::getType).or().eq(Todo::getType, 0))
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

        List<Thinking> thinkings = thinkingMapper.selectList(
                new LambdaQueryWrapper<Thinking>()
                        .eq(Thinking::getUserId, userId)
                        .ge(Thinking::getCreateTime, start + " 00:00:00")
                        .le(Thinking::getCreateTime, end + " 23:59:59")
                        .orderByDesc(Thinking::getCreateTime)
                        .last("LIMIT 30"));
        if (!thinkings.isEmpty()) {
            context.append("\n最近两周思考沉淀：\n");
            for (Thinking tk : thinkings) {
                context.append("- 主题: ").append(tk.getTopic());
                String harvest = tk.getHarvest();
                if (harvest != null && harvest.length() > 150) harvest = harvest.substring(0, 150) + "...";
                context.append(" | 收获: ").append(harvest).append("\n");
            }
        }

        List<TimeBlock> recentBlocks = timeBlockService.listByDateRange(userId, start, end);
        if (!recentBlocks.isEmpty()) {
            context.append("\n最近两周时间安排：\n");
            for (TimeBlock tb : recentBlocks) {
                context.append("- [").append(tb.getBlockDate()).append("] ")
                        .append(tb.getStartTime()).append("-").append(tb.getEndTime())
                        .append(" ").append(tb.getName())
                        .append(" (").append(tb.getCategory()).append(")\n");
            }
        }

        String systemPrompt = aiPromptService.getPromptContent("suggest");
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "你是一个时间管理助手，请根据用户的代办事项、思考沉淀和时间安排，给出具体的改进建议和时间管理技巧。";
        }
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

    private static final Map<String, Long> RANGE_DAYS = Map.of(
            "7d", 6L, "1m", 29L, "3m", 89L, "6m", 179L, "1y", 364L
    );

    private static final Map<String, String> RANGE_LABELS = Map.of(
            "7d", "近7天", "1m", "近1个月", "3m", "近3个月", "6m", "近6个月", "1y", "近1年"
    );

    /**
     * 构建用户数据上下文，用于对话时让 AI 了解用户真实数据
     */
    private String buildUserContext(String range) {
        Integer userId = currentUserId();
        LocalDate now = LocalDate.now();
        long days = RANGE_DAYS.getOrDefault(range, 6L);
        String label = RANGE_LABELS.getOrDefault(range, "近7天");
        String start = now.minusDays(days).toString();
        String end = now.toString();

        StringBuilder ctx = new StringBuilder();
        ctx.append("【以下是当前用户").append(label).append("的真实数据，今天是 ")
                .append(now).append("，回答问题时请参考】\n\n");

        // 待办：量大时只取最近100条
        int todoLimit = days <= 30 ? 50 : 100;
        List<Todo> recentTodos = this.lambdaQuery()
                .eq(Todo::getUserId, userId)
                .and(w -> w.isNull(Todo::getType).or().eq(Todo::getType, 0))
                .ge(Todo::getCreateTime, start + " 00:00:00")
                .le(Todo::getCreateTime, end + " 23:59:59")
                .orderByDesc(Todo::getCreateTime)
                .last("LIMIT " + todoLimit)
                .list();
        long completed = recentTodos.stream().filter(t -> t.getStatus() == 1).count();
        ctx.append("== ").append(label).append("待办事项 (共").append(recentTodos.size())
                .append("条, 已完成").append(completed).append("条) ==\n");
        if (recentTodos.isEmpty()) {
            ctx.append("无待办记录。\n");
        } else {
            for (Todo t : recentTodos) {
                ctx.append("- [").append(t.getStatus() == 1 ? "✓" : "○").append("] ")
                        .append(t.getTitle());
                if (t.getCategory() != null && !t.getCategory().isEmpty()) {
                    ctx.append(" #").append(t.getCategory());
                }
                ctx.append(" P").append(t.getPriority() == 0 ? "低" : t.getPriority() == 1 ? "中" : "高");
                if (t.getEndTime() != null) {
                    ctx.append(" 截止:").append(t.getEndTime().toLocalDate());
                }
                ctx.append("\n");
            }
        }

        // 日记：优先用 summary 字段，没有则截取纯文本
        List<Diary> recentDiaries = diaryMapper.selectDiaryByDateRange(userId, start, end);
        ctx.append("\n== ").append(label).append("日记 (共").append(recentDiaries.size()).append("篇) ==\n");
        if (recentDiaries.isEmpty()) {
            ctx.append("无日记记录。\n");
        } else {
            for (Diary d : recentDiaries) {
                ctx.append("[").append(d.getDiaryDate()).append("]");
                if (d.getMood() != null && !d.getMood().isEmpty()) {
                    ctx.append(" 心情:").append(d.getMood());
                }
                String text = d.getSummary();
                if (text == null || text.trim().isEmpty()) {
                    text = d.getContent();
                    if (text != null) {
                        text = text.replaceAll("<[^>]+>", "").trim();
                        if (text.length() > 80) text = text.substring(0, 80) + "...";
                    }
                }
                if (text != null && !text.isEmpty()) {
                    ctx.append(" ").append(text);
                }
                ctx.append("\n");
            }
        }

        // 思考沉淀
        List<Thinking> thinkingList = thinkingMapper.selectList(
                new LambdaQueryWrapper<Thinking>()
                        .eq(Thinking::getUserId, userId)
                        .ge(Thinking::getCreateTime, start + " 00:00:00")
                        .le(Thinking::getCreateTime, end + " 23:59:59")
                        .orderByDesc(Thinking::getCreateTime)
                        .last("LIMIT 50"));
        ctx.append("\n== ").append(label).append("思考沉淀 (共").append(thinkingList.size()).append("条) ==\n");
        if (thinkingList.isEmpty()) {
            ctx.append("无思考记录。\n");
        } else {
            for (Thinking tk : thinkingList) {
                ctx.append("- 主题: ").append(tk.getTopic());
                String harvest = tk.getHarvest();
                if (harvest != null) {
                    harvest = harvest.replaceAll("<[^>]+>", "").trim();
                    if (harvest.length() > 100) harvest = harvest.substring(0, 100) + "...";
                }
                ctx.append(" | 收获: ").append(harvest);
                if (tk.getRemark() != null && !tk.getRemark().isEmpty()) {
                    String remark = tk.getRemark();
                    if (remark.length() > 60) remark = remark.substring(0, 60) + "...";
                    ctx.append(" | 备注: ").append(remark);
                }
                ctx.append("\n");
            }
        }

        // 时间安排
        List<TimeBlock> timeBlockList = timeBlockService.listByDateRange(userId, start, end);
        ctx.append("\n== ").append(label).append("时间安排 (共").append(timeBlockList.size()).append("条) ==\n");
        if (timeBlockList.isEmpty()) {
            ctx.append("无时间安排记录。\n");
        } else {
            Map<String, Long> categoryMinutes = new LinkedHashMap<>();
            for (TimeBlock tb : timeBlockList) {
                ctx.append("- [").append(tb.getBlockDate()).append("] ")
                        .append(tb.getStartTime()).append("-").append(tb.getEndTime())
                        .append(" ").append(tb.getName())
                        .append(" (").append(tb.getCategory()).append(")\n");
                int startMin = parseMinutes(tb.getStartTime());
                int endMin = parseMinutes(tb.getEndTime());
                categoryMinutes.merge(tb.getCategory(), (long)(endMin - startMin), Long::sum);
            }
            ctx.append("分类耗时统计: ");
            categoryMinutes.forEach((cat, mins) ->
                    ctx.append(cat).append("=").append(mins / 60).append("h").append(mins % 60).append("m "));
            ctx.append("\n");
        }

        // 习惯：统计打卡天数
        List<Habit> habits = habitMapper.selectList(
                new LambdaQueryWrapper<Habit>()
                        .eq(Habit::getUserId, userId)
                        .eq(Habit::getIsActive, 1));
        ctx.append("\n== 活跃习惯 (").append(label).append("打卡统计) ==\n");
        if (habits.isEmpty()) {
            ctx.append("无活跃习惯。\n");
        } else {
            for (Habit h : habits) {
                ctx.append("- ").append(h.getIcon() != null ? h.getIcon() + " " : "")
                        .append(h.getName());
                if (h.getCategory() != null && !h.getCategory().isEmpty()) {
                    ctx.append(" (").append(h.getCategory()).append(")");
                }
                List<HabitRecord> records = habitRecordMapper.selectRecordsByDateRange(
                        userId, h.getId(), start, end);
                long totalDays = days + 1;
                ctx.append(" 打卡").append(records.size()).append("/").append(totalDays).append("天");
                if (!records.isEmpty()) {
                    double avgRating = records.stream()
                            .filter(r -> r.getRating() != null)
                            .mapToInt(HabitRecord::getRating)
                            .average().orElse(0);
                    if (avgRating > 0) {
                        ctx.append(" 平均评分:").append(String.format("%.1f", avgRating));
                    }
                }
                ctx.append("\n");
            }
        }

        return ctx.toString();
    }

    /**
     * 上下文感知的 AI 对话（自动注入用户数据）
     */
    public SseEmitter contextAwareChat(List<Map<String, String>> messages, String range) {
        String chatPrompt = aiPromptService.getPromptContent("chat");
        String userContext = buildUserContext(range);

        String systemPrompt = chatPrompt + "\n\n" + userContext
                + "\n\n【重要规则】\n"
                + "1. 用户的问题如果和待办、日记、思考沉淀、时间安排、习惯、时间管理、个人成长相关，请结合上面的真实数据来回答，给出有针对性的建议。\n"
                + "2. 如果用户询问的是完全无关的话题（如编程技术、天气、新闻、娱乐等），请友善地提醒：「这个问题超出了我的专业范围哦～我是你的个人效能助手，擅长帮你分析待办、日记、思考沉淀、时间安排、习惯数据，给出时间管理和个人成长建议。有什么关于你的计划或习惯想聊的吗？😊」\n"
                + "3. 回答要具体、有条理、语气亲切专业，使用 Markdown 格式。";

        List<Map<String, String>> userMessages = new ArrayList<>();
        for (Map<String, String> msg : messages) {
            if (!"system".equals(msg.get("role"))) {
                userMessages.add(msg);
            }
        }

        return aiService.chatStream(systemPrompt, userMessages);
    }

    private static int parseMinutes(String hhmm) {
        try {
            String[] parts = hhmm.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
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