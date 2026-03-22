"""
导航栏页面对象
"""
import allure
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

from pages.base_page import BasePage


class HeaderPage(BasePage):
    """全局导航栏组件"""

    # ==================== 定位器 ====================

    HEADER = (By.CSS_SELECTOR, "header, .header")
    NAV_BAR = (By.CSS_SELECTOR, ".nav-bar, nav")
    SITE_LOGO = (By.CSS_SELECTOR, ".site-name, .logo")

    # 导航链接
    NAV_LINK_HOME = (By.CSS_SELECTOR, "a[href='/']")
    NAV_LINK_FRIEND = (By.CSS_SELECTOR, "a[href='/friend']")
    NAV_LINK_MESSAGE = (By.CSS_SELECTOR, "a[href='/message']")
    NAV_LINK_ABOUT = (By.CSS_SELECTOR, "a[href='/about']")
    NAV_LINK_ARCHIVE = (By.CSS_SELECTOR, "a[href='/archive']")
    NAV_LINK_CATEGORY = (By.CSS_SELECTOR, "a[href='/category']")
    NAV_LINK_TAG = (By.CSS_SELECTOR, "a[href='/tag']")
    NAV_LINK_TALK = (By.CSS_SELECTOR, "a[href='/talk']")
    NAV_LINK_ALBUM = (By.CSS_SELECTOR, "a[href='/album']")

    # 下拉菜单（文章、娱乐）
    DROPDOWN_ARTICLE = (By.XPATH, "//*[contains(text(), '文章')]")
    DROPDOWN_ENTERTAINMENT = (By.XPATH, "//*[contains(text(), '娱乐')]")

    # 搜索
    SEARCH_INPUT = (By.CSS_SELECTOR, ".search-input, input[placeholder*='搜索']")

    # 用户区域
    LOGIN_BUTTON = (By.XPATH, "//*[contains(text(), '登录')]")
    USER_AVATAR = (By.CSS_SELECTOR, ".user-avatar, .nav-avatar")
    LOGOUT_BUTTON = (By.XPATH, "//*[contains(text(), '退出')]")

    # 主题切换
    THEME_TOGGLE = (By.CSS_SELECTOR, ".theme-toggle, .theme-btn")

    # 汉堡菜单（移动端）
    HAMBURGER = (By.CSS_SELECTOR, ".hamburger, .toggle-btn, .menu-toggle")

    # ==================== 操作 ====================

    @allure.step("点击导航链接: {name}")
    def click_nav_link(self, name: str):
        link_map = {
            "首页": self.NAV_LINK_HOME,
            "友链": self.NAV_LINK_FRIEND,
            "留言板": self.NAV_LINK_MESSAGE,
            "关于": self.NAV_LINK_ABOUT,
        }
        locator = link_map.get(name)
        if locator:
            self.click(locator)

    @allure.step("悬停文章下拉菜单")
    def hover_article_dropdown(self):
        self.hover(self.DROPDOWN_ARTICLE)

    @allure.step("点击归档")
    def click_archive(self):
        self.hover_article_dropdown()
        self.click(self.NAV_LINK_ARCHIVE)

    @allure.step("点击分类")
    def click_category(self):
        self.hover_article_dropdown()
        self.click(self.NAV_LINK_CATEGORY)

    @allure.step("点击标签")
    def click_tag(self):
        self.hover_article_dropdown()
        self.click(self.NAV_LINK_TAG)

    @allure.step("悬停娱乐下拉菜单")
    def hover_entertainment_dropdown(self):
        self.hover(self.DROPDOWN_ENTERTAINMENT)

    @allure.step("点击说说")
    def click_talk(self):
        self.hover_entertainment_dropdown()
        self.click(self.NAV_LINK_TALK)

    @allure.step("点击相册")
    def click_album(self):
        self.hover_entertainment_dropdown()
        self.click(self.NAV_LINK_ALBUM)

    @allure.step("在搜索框输入: {keyword}")
    def search(self, keyword: str):
        self.type_text(self.SEARCH_INPUT, keyword)
        self.press_enter(self.SEARCH_INPUT)

    @allure.step("点击登录按钮")
    def click_login(self):
        self.click(self.LOGIN_BUTTON)

    @allure.step("检查是否已登录")
    def is_logged_in(self) -> bool:
        return self.is_element_visible(self.USER_AVATAR, timeout=3)

    @allure.step("检查导航栏是否可见")
    def is_header_visible(self) -> bool:
        return self.is_element_present(self.HEADER)
