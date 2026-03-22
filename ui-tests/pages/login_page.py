"""
登录/注册对话框页面对象
"""
import allure
from selenium.webdriver.common.by import By

from pages.base_page import BasePage


class LoginPage(BasePage):
    """登录对话框"""

    # ==================== 定位器 ====================

    # 登录对话框
    LOGIN_MODAL = (By.CSS_SELECTOR, ".n-modal, .n-card.n-modal")
    DIALOG_TITLE = (By.XPATH, "//div[contains(text(), '登录')]")

    # 表单字段
    EMAIL_INPUT = (By.CSS_SELECTOR, "input[placeholder*='邮箱']")
    PASSWORD_INPUT = (By.CSS_SELECTOR, "input[placeholder*='密码']")

    # 按钮
    SUBMIT_BUTTON = (By.XPATH, "//button[contains(., '登录')]")
    REGISTER_LINK = (By.XPATH, "//*[contains(text(), '立即注册')]")
    FORGET_LINK = (By.XPATH, "//*[contains(text(), '忘记密码')]")

    # 第三方登录
    QQ_LOGIN = (By.CSS_SELECTOR, "img[alt*='QQ'], .qq-login")
    GITEE_LOGIN = (By.CSS_SELECTOR, "img[alt*='Gitee'], .gitee-login")
    GITHUB_LOGIN = (By.CSS_SELECTOR, "img[alt*='GitHub'], .github-login")

    # 错误提示
    ERROR_MESSAGE = (By.CSS_SELECTOR, ".n-message, .n-message--error")

    # ==================== 操作 ====================

    @allure.step("输入邮箱: {email}")
    def input_email(self, email: str):
        self.type_text(self.EMAIL_INPUT, email)

    @allure.step("输入密码")
    def input_password(self, password: str):
        self.type_text(self.PASSWORD_INPUT, password)

    @allure.step("点击登录按钮")
    def click_submit(self):
        self.click(self.SUBMIT_BUTTON)

    @allure.step("执行登录: {email}")
    def login(self, email: str, password: str):
        self.input_email(email)
        self.input_password(password)
        self.click_submit()

    @allure.step("点击立即注册")
    def click_register(self):
        self.click(self.REGISTER_LINK)

    @allure.step("点击忘记密码")
    def click_forget(self):
        self.click(self.FORGET_LINK)

    @allure.step("检查登录对话框是否可见")
    def is_login_modal_visible(self) -> bool:
        return self.is_element_visible(self.EMAIL_INPUT, timeout=5)

    @allure.step("检查错误提示是否出现")
    def is_error_visible(self) -> bool:
        return self.is_element_visible(self.ERROR_MESSAGE, timeout=5)


class RegisterPage(BasePage):
    """注册对话框"""

    EMAIL_INPUT = (By.CSS_SELECTOR, "input[placeholder*='邮箱']")
    CODE_INPUT = (By.CSS_SELECTOR, "input[placeholder*='验证码']")
    PASSWORD_INPUT = (By.CSS_SELECTOR, "input[placeholder*='密码']")
    SEND_CODE_BTN = (By.XPATH, "//button[contains(., '发送')]")
    REGISTER_BTN = (By.XPATH, "//button[contains(., '注册')]")
    LOGIN_LINK = (By.XPATH, "//*[contains(text(), '已有账号')]")

    @allure.step("检查注册对话框是否可见")
    def is_register_modal_visible(self) -> bool:
        return self.is_element_visible(self.CODE_INPUT, timeout=5)
