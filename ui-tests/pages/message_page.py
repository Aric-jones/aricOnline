"""
留言板页面对象
"""
import allure
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

from pages.base_page import BasePage


class MessagePage(BasePage):
    """留言板 /message"""

    # ==================== 定位器 ====================

    PAGE_COVER = (By.CSS_SELECTOR, ".page-cover, .page-header")
    PAGE_TITLE = (By.XPATH, "//*[contains(text(), '留言板')]")
    MESSAGE_INPUT = (By.CSS_SELECTOR, "input[placeholder*='说点什么']")
    SEND_BUTTON = (By.XPATH, "//button[contains(., '发送')]")
    DANMAKU_CONTAINER = (By.CSS_SELECTOR, ".vue-danmaku, .danmaku")
    DANMAKU_ITEMS = (By.CSS_SELECTOR, ".danmaku-item, .dm")

    # ==================== 操作 ====================

    @allure.step("打开留言板")
    def open_message(self):
        self.open("/message")

    @allure.step("检查留言板标题是否可见")
    def is_title_visible(self) -> bool:
        return self.is_element_visible(self.PAGE_TITLE, timeout=10)

    @allure.step("输入留言: {text}")
    def input_message(self, text: str):
        self.type_text(self.MESSAGE_INPUT, text)

    @allure.step("点击发送")
    def click_send(self):
        self.click(self.SEND_BUTTON)

    @allure.step("发送留言: {text}")
    def send_message(self, text: str):
        self.input_message(text)
        self.click_send()

    @allure.step("通过回车发送留言: {text}")
    def send_message_by_enter(self, text: str):
        self.input_message(text)
        self.press_enter(self.MESSAGE_INPUT)

    @allure.step("获取弹幕数量")
    def get_danmaku_count(self) -> int:
        items = self.find_all(self.DANMAKU_ITEMS)
        return len(items)

    @allure.step("检查弹幕容器是否存在")
    def is_danmaku_visible(self) -> bool:
        return self.is_element_present(self.DANMAKU_CONTAINER, timeout=5)

    @allure.step("检查背景是否可见")
    def is_cover_visible(self) -> bool:
        return self.is_element_visible(self.PAGE_COVER, timeout=5)
