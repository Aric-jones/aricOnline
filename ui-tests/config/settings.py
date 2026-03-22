"""
测试环境配置
"""
import os

# ==================== 环境配置 ====================

# 被测网站地址（优先读取环境变量，方便 CI/CD 覆盖）
BASE_URL = os.getenv("TEST_BASE_URL", "http://localhost:1314")

# 后台管理地址
ADMIN_URL = f"{BASE_URL}/admin"

# ==================== 浏览器配置 ====================

# 浏览器类型：chrome / firefox / edge
BROWSER = os.getenv("TEST_BROWSER", "chrome")

# 是否无头模式（CI 环境建议 True）
HEADLESS = os.getenv("TEST_HEADLESS", "false").lower() == "true"

# 浏览器窗口大小
WINDOW_WIDTH = 1920
WINDOW_HEIGHT = 1080

# 移动端模拟尺寸
MOBILE_WIDTH = 375
MOBILE_HEIGHT = 812

# ==================== 超时配置（秒） ====================

IMPLICIT_WAIT = 10
EXPLICIT_WAIT = 15
PAGE_LOAD_TIMEOUT = 30

# ==================== 测试账号 ====================

TEST_USER_EMAIL = os.getenv("TEST_USER_EMAIL", "test@example.com")
TEST_USER_PASSWORD = os.getenv("TEST_USER_PASSWORD", "123456")

# ==================== 路径配置 ====================

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCREENSHOT_DIR = os.path.join(PROJECT_ROOT, "screenshots")
REPORT_DIR = os.path.join(PROJECT_ROOT, "reports")

# ==================== 页面路由 ====================

ROUTES = {
    "home": "/",
    "message": "/message",
    "about": "/about",
    "friend": "/friend",
    "archive": "/archive",
    "category": "/category",
    "tag": "/tag",
    "talk": "/talk",
    "album": "/album",
    "picture": "/picture",
    "user": "/user",
    "todo": "/todo",
    "search": "/search",
    "not_found": "/404",
}
