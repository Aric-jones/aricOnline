"""
通用页面对象：归档、分类、标签、友链、关于、404 等
"""
import allure
from selenium.webdriver.common.by import By

from pages.base_page import BasePage


class ArchivePage(BasePage):
    """归档页 /archive"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '归档')]")
    ARCHIVE_ITEMS = (By.CSS_SELECTOR, ".archive-item, .post-item, .timeline-item")

    @allure.step("打开归档页")
    def open_archive(self):
        self.open("/archive")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)

    def get_article_count(self) -> int:
        return len(self.find_all(self.ARCHIVE_ITEMS))


class CategoryPage(BasePage):
    """分类页 /category"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '分类')]")
    CATEGORY_ITEMS = (By.CSS_SELECTOR, ".category-item, .category-card")

    @allure.step("打开分类页")
    def open_category(self):
        self.open("/category")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)

    def get_category_count(self) -> int:
        return len(self.find_all(self.CATEGORY_ITEMS))


class TagPage(BasePage):
    """标签页 /tag"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '标签')]")
    TAG_ITEMS = (By.CSS_SELECTOR, ".tag-item, .tag-cloud a, .article-tag")

    @allure.step("打开标签页")
    def open_tag(self):
        self.open("/tag")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)

    def get_tag_count(self) -> int:
        return len(self.find_all(self.TAG_ITEMS))


class FriendPage(BasePage):
    """友链页 /friend"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '友链') or contains(text(), '友情链接')]")
    FRIEND_ITEMS = (By.CSS_SELECTOR, ".friend-item, .friend-card, .link-item")

    @allure.step("打开友链页")
    def open_friend(self):
        self.open("/friend")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)


class AboutPage(BasePage):
    """关于页 /about"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '关于')]")
    ABOUT_CONTENT = (By.CSS_SELECTOR, ".about-content, .page-container, .markdown-body")

    @allure.step("打开关于页")
    def open_about(self):
        self.open("/about")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)


class TalkPage(BasePage):
    """说说页 /talk"""

    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '说说')]")
    TALK_ITEMS = (By.CSS_SELECTOR, ".talk-item, .talk-card")

    @allure.step("打开说说页")
    def open_talk(self):
        self.open("/talk")

    def is_page_loaded(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)

    def get_talk_count(self) -> int:
        return len(self.find_all(self.TALK_ITEMS))


class NotFoundPage(BasePage):
    """404 页面"""

    PAGE_CONTENT = (By.CSS_SELECTOR, ".not-found, .error-page, [class*='404']")

    @allure.step("打开404页面")
    def open_404(self):
        self.open("/404")

    @allure.step("打开不存在的页面")
    def open_random_page(self):
        self.open("/this-page-does-not-exist-12345")

    def is_404_displayed(self) -> bool:
        return self.is_element_present(self.PAGE_CONTENT, timeout=10)
