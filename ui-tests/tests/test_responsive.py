"""
响应式布局测试
"""
import time
import allure
import pytest

from pages.base_page import BasePage
from config.settings import MOBILE_WIDTH, MOBILE_HEIGHT


@allure.epic("博客前台")
@allure.feature("响应式设计")
class TestResponsive:
    """不同屏幕尺寸下的布局测试"""

    @allure.story("移动端")
    @allure.title("移动端首页正常显示")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.responsive
    def test_mobile_home(self, fresh_driver):
        """模拟移动端访问首页"""
        fresh_driver.set_window_size(MOBILE_WIDTH, MOBILE_HEIGHT)
        page = BasePage(fresh_driver)
        page.open("/")
        time.sleep(2)

        body_text = fresh_driver.find_element("tag name", "body").text
        assert len(body_text) > 0, "移动端首页不应为空"
        page.screenshot("移动端_首页")

    @allure.story("移动端")
    @allure.title("移动端留言板正常显示")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.responsive
    def test_mobile_message(self, fresh_driver):
        """模拟移动端访问留言板"""
        fresh_driver.set_window_size(MOBILE_WIDTH, MOBILE_HEIGHT)
        page = BasePage(fresh_driver)
        page.open("/message")
        time.sleep(2)

        body_text = fresh_driver.find_element("tag name", "body").text
        assert len(body_text) > 0, "移动端留言板不应为空"
        page.screenshot("移动端_留言板")

    @allure.story("平板端")
    @allure.title("平板端首页正常显示")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.responsive
    def test_tablet_home(self, fresh_driver):
        """模拟平板端访问首页"""
        fresh_driver.set_window_size(768, 1024)
        page = BasePage(fresh_driver)
        page.open("/")
        time.sleep(2)
        page.screenshot("平板端_首页")

    @allure.story("多分辨率")
    @allure.title("不同分辨率下页面无崩溃: {width}x{height}")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.responsive
    @pytest.mark.parametrize("width, height", [
        (320, 568),   # iPhone SE
        (375, 812),   # iPhone X
        (414, 896),   # iPhone XR
        (768, 1024),  # iPad
        (1024, 768),  # iPad 横屏
        (1280, 720),  # 小笔记本
        (1920, 1080), # Full HD
    ])
    def test_various_resolutions(self, fresh_driver, width, height):
        """各分辨率下页面不崩溃"""
        fresh_driver.set_window_size(width, height)
        page = BasePage(fresh_driver)
        page.open("/")
        time.sleep(2)

        body = fresh_driver.find_element("tag name", "body")
        assert body.is_displayed(), f"{width}x{height} 下页面应正常显示"
        page.screenshot(f"分辨率_{width}x{height}")
