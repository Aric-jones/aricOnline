"""
登录功能测试
"""
import time
import allure
import pytest

from pages.header_page import HeaderPage
from pages.login_page import LoginPage, RegisterPage
from config.settings import TEST_USER_EMAIL, TEST_USER_PASSWORD


@allure.epic("博客前台")
@allure.feature("登录注册")
class TestLoginDialog:
    """登录对话框测试"""

    def _open_login_dialog(self, header_page, login_page):
        """辅助方法：打开登录对话框"""
        header_page.open("/")
        time.sleep(1)
        header_page.click_login()
        time.sleep(1)

    @allure.story("登录对话框")
    @allure.title("打开登录对话框")
    @allure.severity(allure.severity_level.BLOCKER)
    @pytest.mark.smoke
    def test_open_login_dialog(self, header_page: HeaderPage, login_page: LoginPage):
        """点击登录按钮，弹出登录对话框"""
        self._open_login_dialog(header_page, login_page)
        assert login_page.is_login_modal_visible(), "登录对话框应该可见"
        login_page.screenshot("登录对话框")

    @allure.story("登录对话框")
    @allure.title("登录表单包含邮箱和密码字段")
    @allure.severity(allure.severity_level.CRITICAL)
    @pytest.mark.smoke
    def test_login_form_fields(self, header_page: HeaderPage, login_page: LoginPage):
        """登录表单应包含邮箱输入框和密码输入框"""
        self._open_login_dialog(header_page, login_page)
        assert login_page.is_element_visible(login_page.EMAIL_INPUT), "应有邮箱输入框"
        assert login_page.is_element_visible(login_page.PASSWORD_INPUT), "应有密码输入框"

    @allure.story("登录验证")
    @allure.title("空表单提交登录")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_empty_fields(self, header_page: HeaderPage, login_page: LoginPage):
        """不输入任何信息直接点击登录"""
        self._open_login_dialog(header_page, login_page)
        login_page.click_submit()
        time.sleep(1)
        login_page.screenshot("空表单登录")

    @allure.story("登录验证")
    @allure.title("错误邮箱格式")
    @allure.severity(allure.severity_level.NORMAL)
    def test_login_invalid_email(self, header_page: HeaderPage, login_page: LoginPage):
        """输入格式错误的邮箱"""
        self._open_login_dialog(header_page, login_page)
        login_page.input_email("invalid-email")
        login_page.input_password("123456")
        login_page.click_submit()
        time.sleep(1)
        login_page.screenshot("错误邮箱格式")

    @allure.story("登录验证")
    @allure.title("错误密码登录")
    @allure.severity(allure.severity_level.CRITICAL)
    def test_login_wrong_password(self, header_page: HeaderPage, login_page: LoginPage):
        """输入正确邮箱但错误密码"""
        self._open_login_dialog(header_page, login_page)
        login_page.login("wrong@test.com", "wrong_password")
        time.sleep(2)
        login_page.screenshot("错误密码登录")

    @allure.story("对话框切换")
    @allure.title("从登录切换到注册")
    @allure.severity(allure.severity_level.NORMAL)
    def test_switch_to_register(self, header_page: HeaderPage, login_page: LoginPage):
        """点击"立即注册"链接，切换到注册对话框"""
        self._open_login_dialog(header_page, login_page)
        login_page.click_register()
        time.sleep(1)
        register = RegisterPage(login_page.driver)
        assert register.is_register_modal_visible(), "注册对话框应该可见"
        register.screenshot("注册对话框")

    @allure.story("正常登录")
    @allure.title("使用测试账号登录")
    @allure.severity(allure.severity_level.BLOCKER)
    @pytest.mark.smoke
    @pytest.mark.login
    def test_login_success(self, header_page: HeaderPage, login_page: LoginPage):
        """使用配置的测试账号登录（需要后端运行）"""
        if TEST_USER_EMAIL == "test@example.com":
            pytest.skip("未配置真实测试账号，跳过")

        self._open_login_dialog(header_page, login_page)
        login_page.login(TEST_USER_EMAIL, TEST_USER_PASSWORD)
        time.sleep(3)

        assert header_page.is_logged_in(), "登录后应显示用户头像"
        header_page.screenshot("登录成功")
