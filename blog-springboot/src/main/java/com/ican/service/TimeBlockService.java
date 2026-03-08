package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.TimeBlock;
import com.ican.mapper.TimeBlockMapper;
import com.ican.model.vo.request.TimeBlockReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TimeBlockService extends ServiceImpl<TimeBlockMapper, TimeBlock> {

    @Autowired
    private TimeBlockMapper timeBlockMapper;

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public List<TimeBlock> listByDate(String date) {
        return this.lambdaQuery()
                .eq(TimeBlock::getUserId, currentUserId())
                .eq(TimeBlock::getBlockDate, date)
                .orderByAsc(TimeBlock::getStartTime)
                .list();
    }

    public void addBlock(TimeBlockReq req) {
        validateTime(req.getStartTime(), req.getEndTime());
        checkOverlap(req.getBlockDate(), req.getStartTime(), req.getEndTime(), null);
        TimeBlock block = TimeBlock.builder()
                .userId(currentUserId())
                .blockDate(LocalDate.parse(req.getBlockDate()))
                .name(req.getName().trim())
                .category(req.getCategory().trim())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .remark(req.getRemark() != null ? req.getRemark().trim() : "")
                .color(req.getColor() != null ? req.getColor() : "#10b981")
                .build();
        this.save(block);
    }

    public void updateBlock(TimeBlockReq req) {
        Assert.notNull(req.getId(), "ID不能为空");
        TimeBlock block = this.getById(req.getId());
        Assert.notNull(block, "记录不存在");
        Assert.isTrue(block.getUserId().equals(currentUserId()), "无权操作");
        validateTime(req.getStartTime(), req.getEndTime());
        checkOverlap(req.getBlockDate(), req.getStartTime(), req.getEndTime(), req.getId());
        block.setName(req.getName().trim());
        block.setCategory(req.getCategory().trim());
        block.setBlockDate(LocalDate.parse(req.getBlockDate()));
        block.setStartTime(req.getStartTime());
        block.setEndTime(req.getEndTime());
        block.setRemark(req.getRemark() != null ? req.getRemark().trim() : "");
        block.setColor(req.getColor() != null ? req.getColor() : block.getColor());
        this.updateById(block);
    }

    public void deleteBlock(Integer id) {
        TimeBlock block = this.getById(id);
        Assert.notNull(block, "记录不存在");
        Assert.isTrue(block.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
    }

    public List<Map<String, Object>> getDistinctEvents() {
        return timeBlockMapper.selectDistinctEvents(currentUserId());
    }

    public List<Map<String, Object>> getCategoryStats(String type) {
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
            case "yearly":
                start = now.withDayOfYear(1).toString();
                end = now.toString();
                break;
            default:
                start = now.toString();
                end = now.toString();
        }
        return timeBlockMapper.selectCategoryStats(userId, start, end);
    }

    /** 获取日期范围内的时间块(供AI使用) */
    public List<TimeBlock> listByDateRange(Integer userId, String startDate, String endDate) {
        return this.lambdaQuery()
                .eq(TimeBlock::getUserId, userId)
                .ge(TimeBlock::getBlockDate, startDate)
                .le(TimeBlock::getBlockDate, endDate)
                .orderByAsc(TimeBlock::getBlockDate)
                .orderByAsc(TimeBlock::getStartTime)
                .list();
    }

    private void validateTime(String startTime, String endTime) {
        Assert.isTrue(startTime.compareTo(endTime) < 0, "结束时间必须晚于开始时间");
    }

    private void checkOverlap(String date, String startTime, String endTime, Integer excludeId) {
        Integer userId = currentUserId();
        LambdaQueryWrapper<TimeBlock> wrapper = new LambdaQueryWrapper<TimeBlock>()
                .eq(TimeBlock::getUserId, userId)
                .eq(TimeBlock::getBlockDate, date)
                .lt(TimeBlock::getStartTime, endTime)
                .gt(TimeBlock::getEndTime, startTime);
        if (excludeId != null) {
            wrapper.ne(TimeBlock::getId, excludeId);
        }
        long count = this.count(wrapper);
        Assert.isTrue(count == 0, "该时间段已有事件，不能重复添加");
    }
}
