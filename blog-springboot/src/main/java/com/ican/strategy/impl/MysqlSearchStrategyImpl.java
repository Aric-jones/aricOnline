package com.ican.strategy.impl;

import com.ican.constant.ElasticConstant;
import com.ican.mapper.ArticleMapper;
import com.ican.model.vo.response.ArticleSearchResp;
import com.ican.strategy.SearchStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL搜索策略
 *
 * @author ican
 */
@Service("mySqlSearchStrategyImpl")
public class MysqlSearchStrategyImpl implements SearchStrategy {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<ArticleSearchResp> searchArticle(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return new ArrayList<>();
        }
        List<ArticleSearchResp> articleSearchRespList = articleMapper.searchArticle(keyword);
        return articleSearchRespList.stream()
                .map(article -> {
                    // 去除HTML标签
                    String content = article.getArticleContent().replaceAll("<[^>]*>", "");
                    // 获取关键词第一次出现的位置
                    int index = content.indexOf(keyword);
                    if (index != -1) {
                        // 获取关键词前面的文字
                        int preIndex = index > 25 ? index - 25 : 0;
                        String preText = content.substring(preIndex, index);
                        // 获取关键词到后面的文字
                        int last = index + keyword.length();
                        int postLength = content.length() - last;
                        int postIndex = postLength > 175 ? last + 175 : last + postLength;
                        String postText = content.substring(index, postIndex);
                        // 文章内容高亮
                        content = (preText + postText).replaceAll(keyword, ElasticConstant.PRE_TAG + keyword + ElasticConstant.POST_TAG);
                    }
                    // 文章标题高亮
                    String articleTitle = article.getArticleTitle().replaceAll(keyword, ElasticConstant.PRE_TAG + keyword + ElasticConstant.POST_TAG);
                    article.setArticleTitle(articleTitle);
                    article.setArticleContent(content);
                    return article;
                })
                .collect(Collectors.toList());
    }
}
