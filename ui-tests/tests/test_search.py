"""
搜索功能测试
"""
import time
import allure
import pytest

from pages.search_page import SearchPage


@allure.epic("博客前台")
@allure.feature("搜索")
class TestSearch:
    """搜索功能测试"""

    @allure.story("搜索结果页")
    @allure.title("通过 URL 参数搜索")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    @pytest.mark.guest
    def test_search_by_url(self, search_page: SearchPage):
        """直接通过 URL 参数访问搜索结果页"""
        search_page.open_search("Java")
        time.sleep(2)
        assert "/search" in search_page.current_url
        search_page.screenshot("搜索结果_Java")

    @allure.story("搜索结果页")
    @allure.title("搜索有结果的关键词")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_search_with_results(self, search_page: SearchPage):
        """搜索一个常见关键词，预期有结果"""
        search_page.open_search("博客")
        time.sleep(2)
        search_page.screenshot("搜索结果_博客")

    @allure.story("搜索结果页")
    @allure.title("搜索无结果的关键词")
    @allure.severity(allure.severity_level.NORMAL)
    @pytest.mark.guest
    def test_search_no_results(self, search_page: SearchPage):
        """搜索一个不太可能存在的关键词"""
        search_page.open_search("xyzabc123不存在的关键词")
        time.sleep(2)
        search_page.screenshot("搜索无结果")

    @allure.story("搜索结果页")
    @allure.title("空关键词搜索")
    @allure.severity(allure.severity_level.MINOR)
    @pytest.mark.guest
    def test_search_empty_keyword(self, search_page: SearchPage):
        """空关键词不应报错"""
        search_page.open_search("")
        time.sleep(1)
        search_page.screenshot("空搜索")
