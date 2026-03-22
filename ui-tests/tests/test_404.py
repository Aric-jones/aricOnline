"""
404 页面测试
"""
import time
import allure
import pytest

from pages.common_pages import NotFoundPage


@allure.epic("博客前台")
@allure.feature("404页面")
class TestNotFound:
    """404 页面测试"""

    @allure.story("错误处理")
    @allure.title("访问不存在的页面跳转404")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_404_on_invalid_url(self, driver):
        """访问一个不存在的路径，应该跳转到404页面"""
        page = NotFoundPage(driver)
        page.open_random_page()
        time.sleep(2)
        assert "/404" in page.current_url or page.is_404_displayed()
        page.screenshot("404页面")

    @allure.story("错误处理")
    @allure.title("直接访问 /404")
    @allure.severity(allure.severity_level.MINOR)
    @pytest.mark.guest
    def test_direct_404_access(self, driver):
        """直接访问 /404 路径"""
        page = NotFoundPage(driver)
        page.open_404()
        time.sleep(2)
        page.screenshot("直接访问404")
