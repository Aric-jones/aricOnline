"""
Pytest 全局 Fixture 和 Hook

Fixture 作用域说明：
- session: 整个测试会话共享一个浏览器（最快）
- class:   每个测试类一个浏览器
- function: 每个测试函数一个浏览器（最隔离，但最慢）
"""
import os
import pytest
import allure
from datetime import datetime

from utils.driver_factory import create_driver, create_mobile_driver
from utils.helpers import take_screenshot
from config.settings import BASE_URL


# ==================== 浏览器 Fixture ====================

@pytest.fixture(scope="session")
def driver():
    """
    会话级浏览器实例（所有测试共享一个浏览器，速度最快）
    """
    _driver = create_driver()
    yield _driver
    _driver.quit()


@pytest.fixture(scope="function")
def fresh_driver():
    """
    函数级浏览器实例（每个测试独立浏览器，完全隔离）
    """
    _driver = create_driver()
    yield _driver
    _driver.quit()


@pytest.fixture(scope="function")
def mobile_driver():
    """
    移动端模拟浏览器
    """
    _driver = create_mobile_driver()
    yield _driver
    _driver.quit()


# ==================== 页面对象 Fixture ====================

@pytest.fixture
def home_page(driver):
    from pages.home_page import HomePage
    return HomePage(driver)


@pytest.fixture
def header_page(driver):
    from pages.header_page import HeaderPage
    return HeaderPage(driver)


@pytest.fixture
def login_page(driver):
    from pages.login_page import LoginPage
    return LoginPage(driver)


@pytest.fixture
def message_page(driver):
    from pages.message_page import MessagePage
    return MessagePage(driver)


@pytest.fixture
def search_page(driver):
    from pages.search_page import SearchPage
    return SearchPage(driver)


@pytest.fixture
def article_page(driver):
    from pages.article_page import ArticlePage
    return ArticlePage(driver)


# ==================== Hook: 失败自动截图 ====================

@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """测试失败时自动截图并附加到 Allure 报告"""
    outcome = yield
    report = outcome.get_result()

    if report.when == "call" and report.failed:
        driver = None
        # 从 fixture 中获取 driver
        for fixture_name in ("driver", "fresh_driver", "mobile_driver"):
            if fixture_name in item.funcargs:
                driver = item.funcargs[fixture_name]
                break

        if driver:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            screenshot_name = f"FAIL_{item.name}_{timestamp}"
            take_screenshot(driver, screenshot_name)


# ==================== Allure 环境信息 ====================

def pytest_configure(config):
    """写入 Allure 环境信息"""
    allure_dir = config.getoption("--alluredir", default=None)
    if allure_dir:
        os.makedirs(allure_dir, exist_ok=True)
        env_file = os.path.join(allure_dir, "environment.properties")
        with open(env_file, "w", encoding="utf-8") as f:
            f.write(f"Base.URL={BASE_URL}\n")
            f.write(f"Browser={os.getenv('TEST_BROWSER', 'chrome')}\n")
            f.write(f"Headless={os.getenv('TEST_HEADLESS', 'false')}\n")
            f.write(f"Python.Version={os.sys.version}\n")
            f.write(f"Timestamp={datetime.now().isoformat()}\n")
