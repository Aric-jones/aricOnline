"""
搜索页面对象
"""
import allure
from selenium.webdriver.common.by import By

from pages.base_page import BasePage


class SearchPage(BasePage):
    """搜索结果页 /search"""

    # ==================== 定位器 ====================

    SEARCH_RESULTS = (By.CSS_SELECTOR, ".article-card, .article-item, .search-result")
    ARTICLE_TITLES = (By.CSS_SELECTOR, ".article-title, h2 a, h3 a")
    NO_RESULT = (By.XPATH, "//*[contains(text(), '找不到')]")
    PAGINATION = (By.CSS_SELECTOR, ".n-pagination")

    # ==================== 操作 ====================

    @allure.step("打开搜索页面，关键词: {keyword}")
    def open_search(self, keyword: str):
        self.open(f"/search?keyword={keyword}")

    @allure.step("获取搜索结果数量")
    def get_result_count(self) -> int:
        return len(self.find_all(self.SEARCH_RESULTS))

    @allure.step("检查是否有搜索结果")
    def has_results(self) -> bool:
        return self.is_element_present(self.SEARCH_RESULTS, timeout=5)

    @allure.step("检查是否显示无结果")
    def is_no_result_shown(self) -> bool:
        return self.is_element_visible(self.NO_RESULT, timeout=5)

    @allure.step("获取结果标题列表")
    def get_result_titles(self) -> list[str]:
        elements = self.find_all(self.ARTICLE_TITLES)
        return [el.text for el in elements if el.text.strip()]
