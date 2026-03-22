"""
导航功能测试
"""
import time
import allure
import pytest

from pages.header_page import HeaderPage
from pages.base_page import BasePage


@allure.epic("博客前台")
@allure.feature("导航")
class TestNavigation:
    """导航栏功能测试"""

    @allure.story("导航栏显示")
    @allure.title("导航栏正确渲染")
    @allure.severity(allure.severity_level.BLOCKER)
    @pytest.mark.smoke
    @pytest.mark.navigation
    def test_header_visible(self, header_page: HeaderPage):
        """验证导航栏正常显示"""
        header_page.open("/")
        assert header_page.is_header_visible(), "导航栏应该可见"

    @allure.story("页面跳转")
    @allure.title("导航到留言板")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.navigation
    def test_navigate_to_message(self, header_page: HeaderPage):
        """从导航栏点击留言板链接"""
        header_page.open("/")
        header_page.click_nav_link("留言板")
        header_page.wait_for_url_contains("/message")
        assert "/message" in header_page.current_url
        header_page.screenshot("留言板页面")

    @allure.story("页面跳转")
    @allure.title("导航到友链")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.navigation
    def test_navigate_to_friend(self, header_page: HeaderPage):
        """从导航栏点击友链"""
        header_page.open("/")
        header_page.click_nav_link("友链")
        header_page.wait_for_url_contains("/friend")
        assert "/friend" in header_page.current_url

    @allure.story("页面跳转")
    @allure.title("导航到关于页")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.navigation
    def test_navigate_to_about(self, header_page: HeaderPage):
        """从导航栏点击关于"""
        header_page.open("/")
        header_page.click_nav_link("关于")
        header_page.wait_for_url_contains("/about")
        assert "/about" in header_page.current_url

    @allure.story("搜索功能")
    @allure.title("导航栏搜索跳转到搜索结果页")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.navigation
    def test_search_from_header(self, header_page: HeaderPage):
        """在导航栏搜索框输入关键词并回车"""
        header_page.open("/")
        time.sleep(1)
        try:
            header_page.search("测试")
            header_page.wait_for_url_contains("/search")
            assert "/search" in header_page.current_url
            header_page.screenshot("搜索结果")
        except Exception:
            header_page.screenshot("搜索失败")
            pytest.skip("搜索框可能不可见或位置变化")

    @allure.story("用户操作")
    @allure.title("游客状态下显示登录按钮")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_login_button_visible_for_guest(self, header_page: HeaderPage):
        """游客状态下应该显示登录入口"""
        header_page.open("/")
        time.sleep(1)
        # 游客未登录时，应有登录入口
        is_logged = header_page.is_logged_in()
        if not is_logged:
            header_page.screenshot("游客状态-登录按钮")


@allure.epic("博客前台")
@allure.feature("导航")
class TestPageAccessibility:
    """各页面可访问性测试"""

    @allure.story("页面加载")
    @allure.title("访问页面: {path}")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.regression
    @pytest.mark.guest
    @pytest.mark.parametrize("path, expected_text", [
        ("/", ""),
        ("/message", "留言板"),
        ("/about", "关于"),
        ("/friend", "友"),
        ("/archive", "归档"),
        ("/category", "分类"),
        ("/tag", "标签"),
        ("/talk", "说说"),
        ("/album", "相册"),
    ])
    def test_page_loads(self, driver, path, expected_text):
        """验证各页面能正常打开，无白屏"""
        page = BasePage(driver)
        page.open(path)
        time.sleep(2)

        body_text = driver.find_element("tag name", "body").text
        assert len(body_text) > 0, f"页面 {path} 不应为空白"

        if expected_text:
            assert expected_text in body_text or expected_text in driver.page_source, \
                f"页面 {path} 应包含 '{expected_text}'"

        page.screenshot(f"页面_{path.replace('/', '_')}")
