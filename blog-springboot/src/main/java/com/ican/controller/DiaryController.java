package com.ican.controller;

import com.ican.entity.Diary;
import com.ican.model.vo.Result;
import com.ican.model.vo.request.DiaryReq;
import com.ican.service.DiaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "日记模块")
@RestController
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @ApiOperation(value = "获取某日日记")
    @GetMapping("/admin/diary/{date}")
    public Result<Diary> getDiary(@PathVariable String date) {
        return Result.success(diaryService.getDiary(date));
    }

    @ApiOperation(value = "保存日记")
    @PostMapping("/admin/diary")
    public Result<?> saveDiary(@Validated @RequestBody DiaryReq req) {
        diaryService.saveDiary(req.getId(), req.getDiaryDate(), req.getContent(), req.getMood());
        return Result.success();
    }

    @ApiOperation(value = "删除日记")
    @DeleteMapping("/admin/diary/{id}")
    public Result<?> deleteDiary(@PathVariable Integer id) {
        diaryService.deleteDiary(id);
        return Result.success();
    }

    @ApiOperation(value = "日期范围内日记")
    @GetMapping("/admin/diary/range")
    public Result<List<Diary>> getDiaryRange(@RequestParam String startDate, @RequestParam String endDate) {
        return Result.success(diaryService.getDiariesInRange(startDate, endDate));
    }
}
