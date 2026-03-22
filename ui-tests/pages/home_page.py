"""
首页页面对象
"""
import allure
from selenium.webdriver.common.by import By

from pages.base_page import BasePage


class HomePage(BasePage):
    """首页 /"""

    # ==================== 定位器 ====================

    # Brand 区域
    BRAND_SECTION = (By.CSS_SELECTOR, ".brand")
    SITE_NAME = (By.CSS_SELECTOR, ".site-name")
    HITOKOTO_TEXT = (By.CSS_SELECTOR, ".hitokoto")
    SCROLL_DOWN_ARROW = (By.CSS_SELECTOR, ".scroll-down")

    # 说说轮播
    TALK_SWIPER = (By.CSS_SELECTOR, ".talk-swiper")

    # 文章推荐轮播
    RECOMMEND_SWIPER = (By.CSS_SELECTOR, ".recommend")
    SWIPER_SLIDES = (By.CSS_SELECTOR, ".swiper-slide")
    SWIPER_PREV = (By.CSS_SELECTOR, ".swiper-button-prev")
    SWIPER_NEXT = (By.CSS_SELECTOR, ".swiper-button-next")

    # 最新文章
    LATEST_ARTICLES = (By.CSS_SELECTOR, ".article-card, .article-item")
    ARTICLE_LINKS = (By.CSS_SELECTOR, "a[href*='/article/']")

    # 侧边栏
    SIDEBAR = (By.CSS_SELECTOR, ".right-container")
    AUTHOR_AVATAR = (By.CSS_SELECTOR, ".author-avatar")
    TAG_CLOUD = (By.CSS_SELECTOR, ".tag-cloud")

    # ==================== 操作 ====================

    @allure.step("打开首页")
    def open_home(self):
        self.open("/")

    @allure.step("检查 Brand 区域是否可见")
    def is_brand_visible(self) -> bool:
        return self.is_element_visible(self.BRAND_SECTION)

    @allure.step("获取站点名称")
    def get_site_name(self) -> str:
        return self.get_text(self.SITE_NAME)

    @allure.step("点击向下滚动箭头")
    def click_scroll_down(self):
        if self.is_element_visible(self.SCROLL_DOWN_ARROW, timeout=5):
            self.js_click(self.SCROLL_DOWN_ARROW)

    @allure.step("获取文章列表")
    def get_article_links(self) -> list:
        return self.find_all(self.ARTICLE_LINKS)

    @allure.step("点击第 {index} 篇文章")
    def click_article(self, index: int = 0):
        articles = self.get_article_links()
        if articles and index < len(articles):
            self.scroll_to(articles[index])
            articles[index].click()

    @allure.step("检查侧边栏是否可见")
    def is_sidebar_visible(self) -> bool:
        return self.is_element_visible(self.SIDEBAR, timeout=5)

    @allure.step("检查推荐轮播是否存在")
    def is_recommend_visible(self) -> bool:
        return self.is_element_visible(self.RECOMMEND_SWIPER, timeout=5)
