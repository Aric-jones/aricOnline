package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.Diary;
import com.ican.mapper.DiaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class DiaryService extends ServiceImpl<DiaryMapper, Diary> {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private AiService aiService;

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public Diary getDiary(String date) {
        return diaryMapper.selectByUserAndDate(currentUserId(), date);
    }

    public void saveDiary(Integer id, String date, String content, String mood) {
        Integer userId = currentUserId();
        Diary existing = diaryMapper.selectByUserAndDate(userId, date);
        if (existing != null) {
            existing.setContent(content);
            existing.setMood(mood);
            this.updateById(existing);
            generateSummaryAsync(existing.getId(), content);
        } else {
            Diary diary = Diary.builder()
                    .userId(userId)
                    .diaryDate(LocalDate.parse(date))
                    .content(content)
                    .mood(mood)
                    .build();
            this.save(diary);
            generateSummaryAsync(diary.getId(), content);
        }
    }

    @Async
    public void generateSummaryAsync(Integer diaryId, String htmlContent) {
        try {
            String plainText = htmlContent.replaceAll("<[^>]+>", "").trim();
            if (plainText.length() < 20) {
                diaryMapper.updateSummary(diaryId, plainText);
                return;
            }
            String truncated = plainText.length() > 2000 ? plainText.substring(0, 2000) : plainText;
            String summary = aiService.callAiSyncPublic(
                    "你是一个日记摘要助手。请用200-300字概括日记的核心内容、关键事件和情绪变化。" +
                    "直接输出摘要文本，不要加前缀，不要用 Markdown。",
                    truncated, 0.3, 500);
            if (summary != null && !summary.trim().isEmpty()) {
                diaryMapper.updateSummary(diaryId, summary.trim());
            }
        } catch (Exception e) {
            log.warn("生成日记摘要失败 diaryId={}: {}", diaryId, e.getMessage());
            String fallback = htmlContent.replaceAll("<[^>]+>", "").trim();
            if (fallback.length() > 100) fallback = fallback.substring(0, 100) + "...";
            diaryMapper.updateSummary(diaryId, fallback);
        }
    }

    public void deleteDiary(Integer id) {
        Diary diary = this.getById(id);
        Assert.notNull(diary, "日记不存在");
        Assert.isTrue(diary.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
    }

    public List<Diary> getDiariesInRange(String startDate, String endDate) {
        return diaryMapper.selectDiaryByDateRange(currentUserId(), startDate, endDate);
    }
}
