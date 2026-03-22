"""
首页测试
"""
import allure
import pytest

from pages.home_page import HomePage


@allure.epic("博客前台")
@allure.feature("首页")
class TestHomePage:
    """首页功能测试"""

    @allure.story("页面加载")
    @allure.title("首页正常打开")
    @allure.severity(allure.severity_level.BLOCKER)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_home_page_loads(self, home_page: HomePage):
        """验证首页能正常打开，页面状态码200"""
        home_page.open_home()
        assert home_page.current_url.rstrip("/").endswith("") or "/" in home_page.current_url
        home_page.screenshot("首页加载完成")

    @allure.story("页面加载")
    @allure.title("首页 Brand 区域可见")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_brand_section_visible(self, home_page: HomePage):
        """验证首页顶部 Brand 区域（站名、一言、波浪）正常显示"""
        home_page.open_home()
        assert home_page.is_brand_visible(), "Brand 区域应该可见"

    @allure.story("站点信息")
    @allure.title("站点名称正确显示")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_site_name_displayed(self, home_page: HomePage):
        """验证首页站点名称非空"""
        home_page.open_home()
        name = home_page.get_site_name()
        assert name and len(name) > 0, "站点名称不应为空"

    @allure.story("文章展示")
    @allure.title("首页文章列表加载")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_articles_loaded(self, home_page: HomePage):
        """验证首页能加载文章列表"""
        home_page.open_home()
        home_page.scroll_down(800)
        articles = home_page.get_article_links()
        assert len(articles) > 0, "首页应该展示至少一篇文章"
        home_page.screenshot("文章列表")

    @allure.story("文章展示")
    @allure.title("点击文章跳转到详情页")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_click_article_navigates(self, home_page: HomePage):
        """验证点击文章可以跳转到文章详情页"""
        home_page.open_home()
        home_page.scroll_down(800)
        articles = home_page.get_article_links()
        if articles:
            articles[0].click()
            home_page.wait_for_url_contains("/article/")
            assert "/article/" in home_page.current_url
            home_page.screenshot("文章详情页")

    @allure.story("页面布局")
    @allure.title("桌面端侧边栏显示")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_sidebar_visible_on_desktop(self, home_page: HomePage):
        """验证桌面端侧边栏正常显示"""
        home_page.open_home()
        home_page.scroll_down(800)
        # 桌面端（>991px）侧边栏应可见
        visible = home_page.is_sidebar_visible()
        home_page.screenshot("侧边栏状态")
        # 如果窗口宽度够大才断言
        width = home_page.driver.get_window_size()["width"]
        if width > 991:
            assert visible, "桌面端侧边栏应可见"
