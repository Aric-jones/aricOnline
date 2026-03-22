"""
WebDriver 工厂：创建和管理浏览器驱动实例
"""
from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.edge.service import Service as EdgeService
from webdriver_manager.chrome import ChromeDriverManager
from webdriver_manager.firefox import GeckoDriverManager
from webdriver_manager.microsoft import EdgeChromiumDriverManager

from config.settings import (
    BROWSER, HEADLESS, WINDOW_WIDTH, WINDOW_HEIGHT,
    IMPLICIT_WAIT, PAGE_LOAD_TIMEOUT,
)


def create_driver(browser: str = None, headless: bool = None) -> webdriver.Remote:
    """
    创建 WebDriver 实例

    Args:
        browser: 浏览器类型，默认从配置读取
        headless: 是否无头模式，默认从配置读取

    Returns:
        WebDriver 实例
    """
    browser = (browser or BROWSER).lower()
    headless = headless if headless is not None else HEADLESS

    if browser == "chrome":
        driver = _create_chrome(headless)
    elif browser == "firefox":
        driver = _create_firefox(headless)
    elif browser == "edge":
        driver = _create_edge(headless)
    else:
        raise ValueError(f"不支持的浏览器: {browser}")

    driver.implicitly_wait(IMPLICIT_WAIT)
    driver.set_page_load_timeout(PAGE_LOAD_TIMEOUT)
    driver.set_window_size(WINDOW_WIDTH, WINDOW_HEIGHT)

    return driver


def create_mobile_driver(headless: bool = None) -> webdriver.Remote:
    """创建移动端模拟浏览器"""
    headless = headless if headless is not None else HEADLESS
    options = webdriver.ChromeOptions()
    mobile_emulation = {
        "deviceMetrics": {"width": 375, "height": 812, "pixelRatio": 3.0},
        "userAgent": (
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) "
            "AppleWebKit/605.1.15 (KHTML, like Gecko) "
            "Version/16.0 Mobile/15E148 Safari/604.1"
        ),
    }
    options.add_experimental_option("mobileEmulation", mobile_emulation)
    if headless:
        options.add_argument("--headless=new")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")

    service = ChromeService(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=options)
    driver.implicitly_wait(IMPLICIT_WAIT)
    return driver


def _create_chrome(headless: bool) -> webdriver.Chrome:
    options = webdriver.ChromeOptions()
    if headless:
        options.add_argument("--headless=new")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_experimental_option("excludeSwitches", ["enable-automation"])

    service = ChromeService(ChromeDriverManager().install())
    return webdriver.Chrome(service=service, options=options)


def _create_firefox(headless: bool) -> webdriver.Firefox:
    options = webdriver.FirefoxOptions()
    if headless:
        options.add_argument("--headless")

    service = FirefoxService(GeckoDriverManager().install())
    return webdriver.Firefox(service=service, options=options)


def _create_edge(headless: bool) -> webdriver.Edge:
    options = webdriver.EdgeOptions()
    if headless:
        options.add_argument("--headless=new")
    options.add_argument("--disable-gpu")

    service = EdgeService(EdgeChromiumDriverManager().install())
    return webdriver.Edge(service=service, options=options)
