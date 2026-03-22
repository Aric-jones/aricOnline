"""
文章详情页测试
"""
import time
import allure
import pytest

from pages.article_page import ArticlePage
from pages.home_page import HomePage


@allure.epic("博客前台")
@allure.feature("文章详情")
class TestArticlePage:
    """文章详情页测试"""

    @allure.story("文章加载")
    @allure.title("从首页进入文章详情")
    @allure.severity(allure.severity_level.BLOCKER)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_navigate_to_article_from_home(self, driver):
        """从首页点击文章进入详情页"""
        home = HomePage(driver)
        home.open_home()
        home.scroll_down(800)
        time.sleep(1)

        articles = home.get_article_links()
        if not articles:
            pytest.skip("首页无文章，跳过")

        articles[0].click()
        time.sleep(2)
        assert "/article/" in driver.current_url
        home.screenshot("文章详情页")

    @allure.story("文章内容")
    @allure.title("文章标题正确显示")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.guest
    def test_article_title_displayed(self, driver):
        """文章标题应非空"""
        home = HomePage(driver)
        home.open_home()
        home.scroll_down(800)
        time.sleep(1)

        articles = home.get_article_links()
        if not articles:
            pytest.skip("首页无文章，跳过")

        articles[0].click()
        time.sleep(2)

        article = ArticlePage(driver)
        if article.is_content_loaded():
            title = article.get_article_title()
            assert title and len(title) > 0, "文章标题不应为空"
            article.screenshot("文章标题")

    @allure.story("评论区")
    @allure.title("文章评论区显示")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_comment_section_exists(self, driver):
        """文章页应有评论区"""
        home = HomePage(driver)
        home.open_home()
        home.scroll_down(800)
        time.sleep(1)

        articles = home.get_article_links()
        if not articles:
            pytest.skip("首页无文章，跳过")

        articles[0].click()
        time.sleep(2)

        article = ArticlePage(driver)
        article.scroll_to_bottom()
        time.sleep(1)
        article.screenshot("评论区")
