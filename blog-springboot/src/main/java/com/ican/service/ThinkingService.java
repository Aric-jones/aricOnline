package com.ican.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ican.entity.Thinking;
import com.ican.mapper.ThinkingMapper;
import com.ican.model.vo.request.ThinkingReq;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ThinkingService
 *
 * @author Aric
 */
@Service
public class ThinkingService extends ServiceImpl<ThinkingMapper, Thinking> {

    private Integer currentUserId() {
        return StpUtil.getLoginIdAsInt();
    }

    public List<Thinking> listThinking(String keyword, String category) {
        LambdaQueryWrapper<Thinking> wrapper = new LambdaQueryWrapper<Thinking>()
                .eq(Thinking::getUserId, currentUserId());
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Thinking::getCategory, category.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like(Thinking::getTopic, kw)
                    .or().like(Thinking::getHarvest, kw)
                    .or().like(Thinking::getRemark, kw)
            );
        }
        wrapper.orderByDesc(Thinking::getCreateTime);
        return this.list(wrapper);
    }

    public List<String> listCategories() {
        LambdaQueryWrapper<Thinking> wrapper = new LambdaQueryWrapper<Thinking>()
                .eq(Thinking::getUserId, currentUserId())
                .isNotNull(Thinking::getCategory)
                .ne(Thinking::getCategory, "")
                .select(Thinking::getCategory)
                .groupBy(Thinking::getCategory)
                .orderByAsc(Thinking::getCategory);
        return this.list(wrapper).stream()
                .map(Thinking::getCategory)
                .collect(java.util.stream.Collectors.toList());
    }

    public void addThinking(ThinkingReq req) {
        Thinking thinking = Thinking.builder()
                .userId(currentUserId())
                .topic(req.getTopic().trim())
                .category(req.getCategory() != null ? req.getCategory().trim() : "")
                .harvest(req.getHarvest().trim())
                .remark(req.getRemark() != null ? req.getRemark().trim() : "")
                .build();
        this.save(thinking);
    }

    public void updateThinking(ThinkingReq req) {
        Assert.notNull(req.getId(), "ID不能为空");
        Thinking thinking = this.getById(req.getId());
        Assert.notNull(thinking, "记录不存在");
        Assert.isTrue(thinking.getUserId().equals(currentUserId()), "无权操作");
        thinking.setTopic(req.getTopic().trim());
        thinking.setCategory(req.getCategory() != null ? req.getCategory().trim() : "");
        thinking.setHarvest(req.getHarvest().trim());
        thinking.setRemark(req.getRemark() != null ? req.getRemark().trim() : "");
        this.updateById(thinking);
    }

    public void deleteThinking(Integer id) {
        Thinking thinking = this.getById(id);
        Assert.notNull(thinking, "记录不存在");
        Assert.isTrue(thinking.getUserId().equals(currentUserId()), "无权操作");
        this.removeById(id);
    }
}