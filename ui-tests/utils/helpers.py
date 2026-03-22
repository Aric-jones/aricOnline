"""
通用工具函数
"""
import os
import time
import allure
from datetime import datetime
from selenium.webdriver.remote.webdriver import WebDriver

from config.settings import SCREENSHOT_DIR


def take_screenshot(driver: WebDriver, name: str = "") -> str:
    """截图并附加到 Allure 报告"""
    os.makedirs(SCREENSHOT_DIR, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{name}_{timestamp}.png" if name else f"screenshot_{timestamp}.png"
    filepath = os.path.join(SCREENSHOT_DIR, filename)

    driver.save_screenshot(filepath)

    with open(filepath, "rb") as f:
        allure.attach(
            f.read(),
            name=name or "截图",
            attachment_type=allure.attachment_type.PNG,
        )
    return filepath


def scroll_to_bottom(driver: WebDriver, pause: float = 0.5):
    """滚动到页面底部"""
    last_height = driver.execute_script("return document.body.scrollHeight")
    while True:
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(pause)
        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height


def scroll_to_element(driver: WebDriver, element):
    """滚动到指定元素"""
    driver.execute_script("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element)
    time.sleep(0.3)


def wait_for_page_load(driver: WebDriver, timeout: int = 10):
    """等待页面加载完成"""
    for _ in range(timeout * 2):
        state = driver.execute_script("return document.readyState")
        if state == "complete":
            return True
        time.sleep(0.5)
    return False


def get_page_title(driver: WebDriver) -> str:
    """获取页面标题"""
    return driver.title


def get_current_url(driver: WebDriver) -> str:
    """获取当前URL"""
    return driver.current_url
