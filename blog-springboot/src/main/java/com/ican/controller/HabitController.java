package com.ican.controller;

import com.ican.entity.Habit;
import com.ican.entity.HabitRecord;
import com.ican.model.vo.Result;
import com.ican.service.HabitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "习惯追踪模块")
@RestController
public class HabitController {

    @Autowired
    private HabitService habitService;

    // ============= 习惯定义 =============

    @ApiOperation(value = "查询习惯列表")
    @GetMapping("/user/habit/list")
    public Result<List<Habit>> listHabits() {
        return Result.success(habitService.listHabits());
    }

    @ApiOperation(value = "新增习惯")
    @PostMapping("/user/habit")
    public Result<?> addHabit(@RequestBody Habit habit) {
        habitService.addHabit(habit);
        return Result.success();
    }

    @ApiOperation(value = "修改习惯")
    @PutMapping("/user/habit")
    public Result<?> updateHabit(@RequestBody Habit habit) {
        habitService.updateHabit(habit);
        return Result.success();
    }

    @ApiOperation(value = "删除习惯")
    @DeleteMapping("/user/habit/{id}")
    public Result<?> deleteHabit(@PathVariable Integer id) {
        habitService.deleteHabit(id);
        return Result.success();
    }

    // ============= 习惯记录 =============

    @ApiOperation(value = "查询记录列表")
    @GetMapping("/user/habit/record/list")
    public Result<List<HabitRecord>> listRecords(
            @RequestParam(required = false) Integer habitId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(habitService.listRecords(habitId, startDate, endDate));
    }

    @ApiOperation(value = "新增记录")
    @PostMapping("/user/habit/record")
    public Result<?> addRecord(@RequestBody HabitRecord record) {
        habitService.addRecord(record);
        return Result.success();
    }

    @ApiOperation(value = "修改记录")
    @PutMapping("/user/habit/record")
    public Result<?> updateRecord(@RequestBody HabitRecord record) {
        habitService.updateRecord(record);
        return Result.success();
    }

    @ApiOperation(value = "删除记录")
    @DeleteMapping("/user/habit/record/{id}")
    public Result<?> deleteRecord(@PathVariable Integer id) {
        habitService.deleteRecord(id);
        return Result.success();
    }

    @ApiOperation(value = "每日统计数据")
    @GetMapping("/user/habit/stats")
    public Result<List<Map<String, Object>>> getDailyStats(
            @RequestParam(required = false) Integer habitId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(habitService.getDailyStats(habitId, startDate, endDate));
    }
}
