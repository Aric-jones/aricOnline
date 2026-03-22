"""
文章详情页面对象
"""
import allure
from selenium.webdriver.common.by import By

from pages.base_page import BasePage


class ArticlePage(BasePage):
    """文章详情页 /article/:id"""

    # ==================== 定位器 ====================

    ARTICLE_TITLE = (By.CSS_SELECTOR, ".article-title, .post-title, h1")
    ARTICLE_CONTENT = (By.CSS_SELECTOR, ".article-content, .markdown-body, .post-content")
    ARTICLE_META = (By.CSS_SELECTOR, ".article-meta, .post-meta")
    ARTICLE_TAGS = (By.CSS_SELECTOR, ".article-tag")

    # 目录
    TOC = (By.CSS_SELECTOR, ".toc, .catalog")

    # 评论区
    COMMENT_SECTION = (By.CSS_SELECTOR, ".comment, .comment-list")
    COMMENT_INPUT = (By.CSS_SELECTOR, ".reply-box-textarea, textarea")
    COMMENT_SUBMIT = (By.XPATH, "//div[contains(@class, 'send') or contains(text(), '评论')]")
    COMMENT_ITEMS = (By.CSS_SELECTOR, ".comment-item, .comment-card")

    # 点赞
    LIKE_BUTTON = (By.CSS_SELECTOR, ".like-btn, [class*='like']")

    # ==================== 操作 ====================

    @allure.step("打开文章: id={article_id}")
    def open_article(self, article_id: int):
        self.open(f"/article/{article_id}")

    @allure.step("获取文章标题")
    def get_article_title(self) -> str:
        return self.get_text(self.ARTICLE_TITLE)

    @allure.step("检查文章内容是否加载")
    def is_content_loaded(self) -> bool:
        return self.is_element_visible(self.ARTICLE_CONTENT, timeout=10)

    @allure.step("获取文章标签")
    def get_tags(self) -> list[str]:
        elements = self.find_all(self.ARTICLE_TAGS)
        return [el.text for el in elements if el.text.strip()]

    @allure.step("检查评论区是否存在")
    def is_comment_section_visible(self) -> bool:
        return self.is_element_present(self.COMMENT_SECTION, timeout=5)

    @allure.step("获取评论数量")
    def get_comment_count(self) -> int:
        return len(self.find_all(self.COMMENT_ITEMS))

    @allure.step("滚动到评论区")
    def scroll_to_comments(self):
        if self.is_comment_section_visible():
            element = self.find(self.COMMENT_SECTION)
            self.scroll_to(element)
