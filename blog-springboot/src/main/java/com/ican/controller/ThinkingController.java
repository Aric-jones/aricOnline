package com.ican.controller;

import com.ican.entity.Thinking;
import com.ican.model.vo.Result;
import com.ican.model.vo.request.ThinkingReq;
import com.ican.service.ThinkingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "思考沉淀模块")
@RestController
public class ThinkingController {

    @Autowired
    private ThinkingService thinkingService;

    @ApiOperation(value = "查询思考列表")
    @GetMapping("/user/thinking/list")
    public Result<List<Thinking>> listThinking(@RequestParam(required = false) String keyword) {
        return Result.success(thinkingService.listThinking(keyword));
    }

    @ApiOperation(value = "新增思考")
    @PostMapping("/user/thinking")
    public Result<?> addThinking(@Validated @RequestBody ThinkingReq req) {
        thinkingService.addThinking(req);
        return Result.success();
    }

    @ApiOperation(value = "修改思考")
    @PutMapping("/user/thinking")
    public Result<?> updateThinking(@Validated @RequestBody ThinkingReq req) {
        thinkingService.updateThinking(req);
        return Result.success();
    }

    @ApiOperation(value = "删除思考")
    @DeleteMapping("/user/thinking/{id}")
    public Result<?> deleteThinking(@PathVariable Integer id) {
        thinkingService.deleteThinking(id);
        return Result.success();
    }
}
