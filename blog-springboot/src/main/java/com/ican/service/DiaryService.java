package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.Diary;
import com.ican.mapper.DiaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiaryService extends ServiceImpl<DiaryMapper, Diary> {

    @Autowired
    private DiaryMapper diaryMapper;

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
        } else {
            Diary diary = Diary.builder()
                    .userId(userId)
                    .diaryDate(LocalDate.parse(date))
                    .content(content)
                    .mood(mood)
                    .build();
            this.save(diary);
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
