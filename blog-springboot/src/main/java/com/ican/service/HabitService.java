package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.Habit;
import com.ican.entity.HabitRecord;
import com.ican.mapper.HabitMapper;
import com.ican.mapper.HabitRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class HabitService extends ServiceImpl<HabitMapper, Habit> {

    @Autowired
    private HabitMapper habitMapper;

    @Autowired
    private HabitRecordMapper habitRecordMapper;

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public List<Habit> listHabits() {
        return habitMapper.selectList(
                new LambdaQueryWrapper<Habit>()
                        .eq(Habit::getUserId, currentUserId())
                        .orderByAsc(Habit::getSort)
                        .orderByDesc(Habit::getCreateTime)
        );
    }

    public void addHabit(Habit habit) {
        habit.setUserId(currentUserId());
        if (habit.getIsActive() == null) habit.setIsActive(1);
        if (habit.getSort() == null) habit.setSort(0);
        this.save(habit);
    }

    public void updateHabit(Habit habit) {
        Assert.notNull(habit.getId(), "习惯ID不能为空");
        Habit existing = this.getById(habit.getId());
        Assert.notNull(existing, "习惯不存在");
        Assert.isTrue(existing.getUserId().equals(currentUserId()), "无权操作");
        existing.setName(habit.getName());
        existing.setIcon(habit.getIcon());
        existing.setColor(habit.getColor());
        existing.setCategory(habit.getCategory());
        existing.setUnit(habit.getUnit());
        existing.setSort(habit.getSort() != null ? habit.getSort() : existing.getSort());
        existing.setIsActive(habit.getIsActive() != null ? habit.getIsActive() : existing.getIsActive());
        this.updateById(existing);
    }

    public void deleteHabit(Integer id) {
        Habit habit = this.getById(id);
        Assert.notNull(habit, "习惯不存在");
        Assert.isTrue(habit.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
        habitRecordMapper.delete(
                new LambdaQueryWrapper<HabitRecord>().eq(HabitRecord::getHabitId, id)
        );
    }

    // ============= 记录相关 =============

    public List<HabitRecord> listRecords(Integer habitId, String startDate, String endDate) {
        return habitRecordMapper.selectRecordsByDateRange(currentUserId(), habitId, startDate, endDate);
    }

    public void addRecord(HabitRecord record) {
        Habit habit = this.getById(record.getHabitId());
        Assert.notNull(habit, "习惯不存在");
        Assert.isTrue(habit.getUserId().equals(currentUserId()), "无权操作");
        record.setUserId(currentUserId());
        if (record.getRecordDate() == null) record.setRecordDate(LocalDate.now());
        if (record.getValue() == null) record.setValue(BigDecimal.ONE);
        if (record.getRating() == null) record.setRating(3);
        habitRecordMapper.insert(record);
    }

    public void updateRecord(HabitRecord record) {
        Assert.notNull(record.getId(), "记录ID不能为空");
        HabitRecord existing = habitRecordMapper.selectById(record.getId());
        Assert.notNull(existing, "记录不存在");
        Assert.isTrue(existing.getUserId().equals(currentUserId()), "无权操作");
        existing.setValue(record.getValue() != null ? record.getValue() : existing.getValue());
        existing.setRating(record.getRating() != null ? record.getRating() : existing.getRating());
        existing.setNote(record.getNote());
        habitRecordMapper.updateById(existing);
    }

    public void deleteRecord(Integer id) {
        HabitRecord record = habitRecordMapper.selectById(id);
        Assert.notNull(record, "记录不存在");
        Assert.isTrue(record.getUserId().equals(currentUserId()), "无权操作");
        habitRecordMapper.deleteById(id);
    }

    public List<Map<String, Object>> getDailyStats(Integer habitId, String startDate, String endDate) {
        return habitRecordMapper.selectDailyStats(currentUserId(), habitId, startDate, endDate);
    }
}
