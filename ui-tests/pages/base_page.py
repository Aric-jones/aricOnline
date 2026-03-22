"""
页面基类：封装所有页面的公共操作
"""
import time
import allure
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.common.exceptions import (
    TimeoutException, NoSuchElementException, ElementNotInteractableException,
)

from config.settings import BASE_URL, EXPLICIT_WAIT
from utils.helpers import take_screenshot


class BasePage:
    """所有 Page Object 的基类"""

    def __init__(self, driver: WebDriver):
        self.driver = driver
        self.wait = WebDriverWait(driver, EXPLICIT_WAIT)
        self.base_url = BASE_URL

    # ==================== 导航 ====================

    def open(self, path: str = ""):
        url = f"{self.base_url}{path}"
        with allure.step(f"打开页面: {url}"):
            self.driver.get(url)
            self.wait_for_page_ready()

    def refresh(self):
        self.driver.refresh()
        self.wait_for_page_ready()

    def go_back(self):
        self.driver.back()

    @property
    def current_url(self) -> str:
        return self.driver.current_url

    @property
    def title(self) -> str:
        return self.driver.title

    # ==================== 查找元素 ====================

    def find(self, locator: tuple, timeout: int = None) -> WebElement:
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        return wait.until(EC.presence_of_element_located(locator))

    def find_visible(self, locator: tuple, timeout: int = None) -> WebElement:
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        return wait.until(EC.visibility_of_element_located(locator))

    def find_clickable(self, locator: tuple, timeout: int = None) -> WebElement:
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        return wait.until(EC.element_to_be_clickable(locator))

    def find_all(self, locator: tuple) -> list[WebElement]:
        return self.driver.find_elements(*locator)

    def is_element_present(self, locator: tuple, timeout: int = 3) -> bool:
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located(locator)
            )
            return True
        except TimeoutException:
            return False

    def is_element_visible(self, locator: tuple, timeout: int = 3) -> bool:
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.visibility_of_element_located(locator)
            )
            return True
        except TimeoutException:
            return False

    # ==================== 元素操作 ====================

    def click(self, locator: tuple):
        element = self.find_clickable(locator)
        element.click()

    def type_text(self, locator: tuple, text: str, clear: bool = True):
        element = self.find_visible(locator)
        if clear:
            element.clear()
        element.send_keys(text)

    def get_text(self, locator: tuple) -> str:
        return self.find_visible(locator).text

    def get_attribute(self, locator: tuple, attr: str) -> str:
        return self.find(locator).get_attribute(attr)

    def hover(self, locator: tuple):
        element = self.find_visible(locator)
        ActionChains(self.driver).move_to_element(element).perform()

    def press_enter(self, locator: tuple):
        self.find(locator).send_keys(Keys.ENTER)

    # ==================== 等待 ====================

    def wait_for_page_ready(self, timeout: int = 15):
        """等待 Vue SPA 页面完成渲染"""
        for _ in range(timeout * 2):
            state = self.driver.execute_script("return document.readyState")
            if state == "complete":
                break
            time.sleep(0.5)
        time.sleep(0.5)

    def wait_for_url_contains(self, text: str, timeout: int = None):
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        wait.until(EC.url_contains(text))

    def wait_for_text_present(self, locator: tuple, text: str, timeout: int = None):
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        wait.until(EC.text_to_be_present_in_element(locator, text))

    def wait_for_element_disappear(self, locator: tuple, timeout: int = None):
        wait = WebDriverWait(self.driver, timeout or EXPLICIT_WAIT)
        wait.until(EC.invisibility_of_element_located(locator))

    # ==================== 滚动 ====================

    def scroll_to(self, element: WebElement):
        self.driver.execute_script(
            "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});",
            element,
        )
        time.sleep(0.3)

    def scroll_down(self, pixels: int = 500):
        self.driver.execute_script(f"window.scrollBy(0, {pixels});")
        time.sleep(0.3)

    def scroll_to_top(self):
        self.driver.execute_script("window.scrollTo(0, 0);")
        time.sleep(0.3)

    def scroll_to_bottom(self):
        self.driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(0.5)

    # ==================== JavaScript ====================

    def js_click(self, locator: tuple):
        """JS 点击，用于被遮挡的元素"""
        element = self.find(locator)
        self.driver.execute_script("arguments[0].click();", element)

    def js_set_value(self, locator: tuple, value: str):
        element = self.find(locator)
        self.driver.execute_script(
            "arguments[0].value = arguments[1]; "
            "arguments[0].dispatchEvent(new Event('input'));",
            element, value,
        )

    # ==================== 截图 ====================

    def screenshot(self, name: str = ""):
        return take_screenshot(self.driver, name)
