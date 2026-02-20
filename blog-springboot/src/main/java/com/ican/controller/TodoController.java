package com.ican.controller;

import com.ican.entity.AiRecord;
import com.ican.entity.Todo;
import com.ican.model.vo.PageResult;
import com.ican.model.vo.Result;
import com.ican.model.vo.query.TodoQuery;
import com.ican.model.vo.request.TodoReq;
import com.ican.service.AiService;
import com.ican.service.TodoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Api(tags = "代办事项模块")
@RestController
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private AiService aiService;

    @ApiOperation(value = "查询代办列表")
    @GetMapping("/admin/todo/list")
    public Result<PageResult<Todo>> listTodo(TodoQuery query) {
        return Result.success(todoService.listTodo(query));
    }

    @ApiOperation(value = "新增代办")
    @PostMapping("/admin/todo")
    public Result<?> addTodo(@Validated @RequestBody TodoReq req) {
        todoService.addTodo(req);
        return Result.success();
    }

    @ApiOperation(value = "修改代办")
    @PutMapping("/admin/todo")
    public Result<?> updateTodo(@Validated @RequestBody TodoReq req) {
        todoService.updateTodo(req);
        return Result.success();
    }

    @ApiOperation(value = "删除代办")
    @DeleteMapping("/admin/todo/{id}")
    public Result<?> deleteTodo(@PathVariable Integer id) {
        todoService.deleteTodo(id);
        return Result.success();
    }

    @ApiOperation(value = "切换完成状态")
    @PutMapping("/admin/todo/status/{id}")
    public Result<?> toggleStatus(@PathVariable Integer id) {
        todoService.toggleStatus(id);
        return Result.success();
    }

    @ApiOperation(value = "日历视图数据")
    @GetMapping("/admin/todo/calendar")
    public Result<List<Todo>> calendarData(@RequestParam String startDate, @RequestParam String endDate) {
        return Result.success(todoService.getCalendarData(startDate, endDate));
    }

    @ApiOperation(value = "获取分类列表")
    @GetMapping("/admin/todo/categories")
    public Result<List<String>> getCategoryList() {
        return Result.success(todoService.getCategoryList());
    }

    @ApiOperation(value = "AI总结")
    @PostMapping("/admin/todo/ai/summary")
    public Result<String> aiSummary(@RequestBody Map<String, String> body) {
        String type = body.getOrDefault("type", "daily");
        return Result.success(todoService.aiSummary(type));
    }

    @ApiOperation(value = "AI改进建议")
    @PostMapping("/admin/todo/ai/suggest")
    public Result<String> aiSuggest() {
        return Result.success(todoService.aiSuggest());
    }

    @ApiOperation(value = "AI代办对话")
    @PostMapping(value = "/admin/todo/ai/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter aiChat(@RequestBody List<Map<String, String>> messages) {
        return aiService.chatStream(messages);
    }

    @ApiOperation(value = "获取AI记录")
    @GetMapping("/admin/todo/ai/record/{type}")
    public Result<AiRecord> getAiRecord(@PathVariable("type") String type) {
        return Result.success(todoService.getAiRecord(type));
    }

    @ApiOperation(value = "保存AI记录（对话等）")
    @PostMapping("/admin/todo/ai/record")
    public Result<?> saveAiRecord(@RequestBody Map<String, String> body) {
        String type = body.get("recordType");
        String content = body.get("content");
        todoService.saveAiRecord(type, content);
        return Result.success();
    }
}
