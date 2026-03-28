package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.TaskPool;
import com.ican.mapper.TaskPoolMapper;
import com.ican.model.vo.request.TaskPoolAssignReq;
import com.ican.model.vo.request.TaskPoolReq;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskPoolService
 *
 * @author Aric
 */
@Service
public class TaskPoolService extends ServiceImpl<TaskPoolMapper, TaskPool> {

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public List<TaskPool> listUnassigned(String keyword) {
        return this.lambdaQuery()
                .eq(TaskPool::getUserId, currentUserId())
                .isNull(TaskPool::getWeekStartDate)
                .and(keyword != null && !keyword.isBlank(), q ->
                        q.like(TaskPool::getTitle, keyword)
                                .or().like(TaskPool::getDescription, keyword)
                                .or().like(TaskPool::getCategory, keyword))
                .orderByDesc(TaskPool::getPriority)
                .orderByDesc(TaskPool::getCreateTime)
                .list();
    }

    public List<TaskPool> listByWeek(String weekStart) {
        return this.lambdaQuery()
                .eq(TaskPool::getUserId, currentUserId())
                .eq(TaskPool::getWeekStartDate, weekStart)
                .orderByDesc(TaskPool::getPriority)
                .orderByAsc(TaskPool::getCreateTime)
                .list();
    }

    public List<TaskPool> listByRange(String start, String end) {
        return this.lambdaQuery()
                .eq(TaskPool::getUserId, currentUserId())
                .isNotNull(TaskPool::getWeekStartDate)
                .ge(TaskPool::getWeekStartDate, start)
                .le(TaskPool::getWeekStartDate, end)
                .orderByAsc(TaskPool::getWeekStartDate)
                .orderByDesc(TaskPool::getPriority)
                .list();
    }

    public void addTask(TaskPoolReq req) {
        TaskPool task = TaskPool.builder()
                .userId(currentUserId())
                .title(req.getTitle().trim())
                .description(req.getDescription() != null ? req.getDescription().trim() : "")
                .category(req.getCategory() != null ? req.getCategory().trim() : "")
                .priority(req.getPriority() != null ? req.getPriority() : 0)
                .status(0)
                .build();
        this.save(task);
    }

    public void updateTask(TaskPoolReq req) {
        Assert.notNull(req.getId(), "ID不能为空");
        TaskPool task = this.getById(req.getId());
        Assert.notNull(task, "记录不存在");
        Assert.isTrue(task.getUserId().equals(currentUserId()), "无权操作");
        task.setTitle(req.getTitle().trim());
        task.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
        task.setCategory(req.getCategory() != null ? req.getCategory().trim() : "");
        task.setPriority(req.getPriority() != null ? req.getPriority() : task.getPriority());
        this.updateById(task);
    }

    public void deleteTask(Integer id) {
        TaskPool task = this.getById(id);
        Assert.notNull(task, "记录不存在");
        Assert.isTrue(task.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
    }

    public void assignToWeek(TaskPoolAssignReq req) {
        TaskPool task = this.getById(req.getId());
        Assert.notNull(task, "记录不存在");
        Assert.isTrue(task.getUserId().equals(currentUserId()), "无权操作");
        task.setWeekStartDate(LocalDate.parse(req.getWeekStartDate()));
        this.updateById(task);
    }

    public void unassign(Integer id) {
        TaskPool task = this.getById(id);
        Assert.notNull(task, "记录不存在");
        Assert.isTrue(task.getUserId().equals(currentUserId()), "无权操作");
        // 使用 LambdaUpdateWrapper 显式设置 null，因为 updateById 默认忽略 null 值
        this.update(new LambdaUpdateWrapper<TaskPool>()
                .eq(TaskPool::getId, id)
                .set(TaskPool::getWeekStartDate, null));
    }

    public void toggleStatus(Integer id) {
        TaskPool task = this.getById(id);
        Assert.notNull(task, "记录不存在");
        Assert.isTrue(task.getUserId().equals(currentUserId()), "无权操作");
        int newStatus = task.getStatus() == 0 ? 1 : 0;
        LocalDateTime completedTime = newStatus == 1 ? LocalDateTime.now() : null;
        // 使用 LambdaUpdateWrapper 确保 null 值也能正确更新
        this.update(new LambdaUpdateWrapper<TaskPool>()
                .eq(TaskPool::getId, id)
                .set(TaskPool::getStatus, newStatus)
                .set(TaskPool::getCompletedTime, completedTime));
    }
}