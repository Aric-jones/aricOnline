package com.ican.controller;

import com.ican.entity.TimeBlock;
import com.ican.model.vo.Result;
import com.ican.model.vo.request.TimeBlockReq;
import com.ican.service.TimeBlockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "时间管理模块")
@RestController
public class TimeBlockController {

    @Autowired
    private TimeBlockService timeBlockService;

    @ApiOperation(value = "查询某日时间块")
    @GetMapping("/user/timeblock/list")
    public Result<List<TimeBlock>> listByDate(@RequestParam String date) {
        return Result.success(timeBlockService.listByDate(date));
    }

    @ApiOperation(value = "新增时间块")
    @PostMapping("/user/timeblock")
    public Result<?> addBlock(@Validated @RequestBody TimeBlockReq req) {
        timeBlockService.addBlock(req);
        return Result.success();
    }

    @ApiOperation(value = "修改时间块")
    @PutMapping("/user/timeblock")
    public Result<?> updateBlock(@Validated @RequestBody TimeBlockReq req) {
        timeBlockService.updateBlock(req);
        return Result.success();
    }

    @ApiOperation(value = "删除时间块")
    @DeleteMapping("/user/timeblock/{id}")
    public Result<?> deleteBlock(@PathVariable Integer id) {
        timeBlockService.deleteBlock(id);
        return Result.success();
    }

    @ApiOperation(value = "历史事件去重列表")
    @GetMapping("/user/timeblock/events")
    public Result<List<Map<String, Object>>> distinctEvents() {
        return Result.success(timeBlockService.getDistinctEvents());
    }

    @ApiOperation(value = "分类统计")
    @GetMapping("/user/timeblock/stats")
    public Result<List<Map<String, Object>>> categoryStats(@RequestParam(defaultValue = "daily") String type) {
        return Result.success(timeBlockService.getCategoryStats(type));
    }
}
