package com.ican.controller;

import com.ican.entity.TaskPool;
import com.ican.model.vo.Result;
import com.ican.model.vo.request.TaskPoolAssignReq;
import com.ican.model.vo.request.TaskPoolReq;
import com.ican.service.TaskPoolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务池模块")
@RestController
public class TaskPoolController {

    @Autowired
    private TaskPoolService taskPoolService;

    @ApiOperation(value = "查询未分配任务")
    @GetMapping("/user/taskpool/unassigned")
    public Result<List<TaskPool>> listUnassigned(@RequestParam(required = false) String keyword) {
        return Result.success(taskPoolService.listUnassigned(keyword));
    }

    @ApiOperation(value = "查询某周任务")
    @GetMapping("/user/taskpool/week")
    public Result<List<TaskPool>> listByWeek(@RequestParam String weekStart) {
        return Result.success(taskPoolService.listByWeek(weekStart));
    }

    @ApiOperation(value = "查询日期范围内的任务")
    @GetMapping("/user/taskpool/range")
    public Result<List<TaskPool>> listByRange(@RequestParam String start, @RequestParam String end) {
        return Result.success(taskPoolService.listByRange(start, end));
    }

    @ApiOperation(value = "新增任务")
    @PostMapping("/user/taskpool")
    public Result<?> addTask(@Validated @RequestBody TaskPoolReq req) {
        taskPoolService.addTask(req);
        return Result.success();
    }

    @ApiOperation(value = "修改任务")
    @PutMapping("/user/taskpool")
    public Result<?> updateTask(@Validated @RequestBody TaskPoolReq req) {
        taskPoolService.updateTask(req);
        return Result.success();
    }

    @ApiOperation(value = "删除任务")
    @DeleteMapping("/user/taskpool/{id}")
    public Result<?> deleteTask(@PathVariable Integer id) {
        taskPoolService.deleteTask(id);
        return Result.success();
    }

    @ApiOperation(value = "分配任务到周")
    @PutMapping("/user/taskpool/assign")
    public Result<?> assignToWeek(@Validated @RequestBody TaskPoolAssignReq req) {
        taskPoolService.assignToWeek(req);
        return Result.success();
    }

    @ApiOperation(value = "取消分配")
    @PutMapping("/user/taskpool/unassign/{id}")
    public Result<?> unassign(@PathVariable Integer id) {
        taskPoolService.unassign(id);
        return Result.success();
    }

    @ApiOperation(value = "切换完成状态")
    @PutMapping("/user/taskpool/status/{id}")
    public Result<?> toggleStatus(@PathVariable Integer id) {
        taskPoolService.toggleStatus(id);
        return Result.success();
    }
}
