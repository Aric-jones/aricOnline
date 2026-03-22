"""
留言板测试
"""
import time
import allure
import pytest

from pages.message_page import MessagePage


@allure.epic("博客前台")
@allure.feature("留言板")
class TestMessagePage:
    """留言板功能测试"""

    @allure.story("页面加载")
    @allure.title("留言板页面正常打开")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_message_page_loads(self, message_page: MessagePage):
        """验证留言板页面正常加载"""
        message_page.open_message()
        assert message_page.is_title_visible(), "留言板标题应可见"
        message_page.screenshot("留言板页面")

    @allure.story("页面加载")
    @allure.title("留言板背景可见")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_message_cover_visible(self, message_page: MessagePage):
        """验证留言板背景图正常显示"""
        message_page.open_message()
        assert message_page.is_cover_visible(), "留言板背景应可见"

    @allure.story("留言输入")
    @allure.title("留言输入框存在")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_message_input_exists(self, message_page: MessagePage):
        """验证留言输入框可见"""
        message_page.open_message()
        assert message_page.is_element_visible(
            message_page.MESSAGE_INPUT, timeout=10
        ), "留言输入框应可见"

    @allure.story("留言输入")
    @allure.title("发送按钮存在")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_send_button_exists(self, message_page: MessagePage):
        """验证发送按钮存在"""
        message_page.open_message()
        assert message_page.is_element_visible(
            message_page.SEND_BUTTON, timeout=10
        ), "发送按钮应可见"

    @allure.story("弹幕显示")
    @allure.title("弹幕容器加载")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_danmaku_container_loaded(self, message_page: MessagePage):
        """验证弹幕组件正常加载"""
        message_page.open_message()
        time.sleep(2)
        message_page.screenshot("弹幕状态")
