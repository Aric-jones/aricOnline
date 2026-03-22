# 博客前台 UI 自动化测试

基于 **pytest + Selenium + Allure** 的 UI 自动化测试项目，采用 **Page Object Model（POM）** 设计模式。

---

## 项目结构

```
ui-tests/
├── config/                  # 配置
│   └── settings.py          # 环境、浏览器、超时、账号配置
├── pages/                   # Page Object（页面对象）
│   ├── base_page.py         # 基类：封装通用操作
│   ├── home_page.py         # 首页
│   ├── header_page.py       # 导航栏
│   ├── login_page.py        # 登录/注册对话框
│   ├── message_page.py      # 留言板
│   ├── search_page.py       # 搜索结果
│   ├── article_page.py      # 文章详情
│   └── common_pages.py      # 归档/分类/标签/友链/关于/说说/404
├── tests/                   # 测试用例
│   ├── test_home.py         # 首页测试（6条）
│   ├── test_navigation.py   # 导航+各页面可访问性（15条）
│   ├── test_login.py        # 登录注册测试（7条）
│   ├── test_message.py      # 留言板测试（5条）
│   ├── test_search.py       # 搜索功能测试（4条）
│   ├── test_article.py      # 文章详情测试（3条）
│   ├── test_404.py          # 404测试（2条）
│   └── test_responsive.py   # 响应式布局测试（10条）
├── utils/                   # 工具
│   ├── driver_factory.py    # 浏览器驱动工厂
│   └── helpers.py           # 截图、滚动等辅助函数
├── conftest.py              # Fixture 和 Hook
├── pytest.ini               # pytest 配置
├── requirements.txt         # Python 依赖
├── run.bat                  # Windows 运行脚本
├── run.sh                   # Linux/Mac 运行脚本
├── reports/                 # Allure 报告（自动生成）
└── screenshots/             # 截图（自动生成）
```

---

## 快速开始

### 1. 环境准备

**必须安装：**
- Python 3.9+
- Chrome 浏览器（推荐，也支持 Firefox、Edge）
- Java 8+（Allure 报告需要）

**安装 Allure 命令行：**

```bash
# Windows（Scoop）
scoop install allure

# Windows（Chocolatey）
choco install allure

# macOS
brew install allure

# Linux
sudo apt install allure
```

### 2. 安装依赖

```bash
cd ui-tests

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 3. 启动被测网站

```bash
# 在另一个终端，启动前端开发服务器
cd blog-vue/shoka-blog
npm run dev
# 默认地址：http://localhost:1314
```

### 4. 运行测试

```bash
# 方式一：使用脚本（推荐）
run.bat                  # Windows
./run.sh                 # Linux/Mac

# 方式二：直接运行 pytest
pytest                   # 全部测试
pytest -m smoke          # 只跑冒烟测试
pytest -m guest          # 只跑游客测试
pytest -m navigation     # 只跑导航测试
pytest -m responsive     # 只跑响应式测试
pytest -k "test_home"    # 只跑首页测试
pytest -k "test_login"   # 只跑登录测试

# 方式三：无头模式（不弹出浏览器窗口）
set TEST_HEADLESS=true   # Windows
export TEST_HEADLESS=true  # Linux/Mac
pytest
```

### 5. 查看 Allure 报告

```bash
# 生成报告
allure generate reports/allure-results -o reports/allure-report --clean

# 打开报告（自动在浏览器中打开）
allure open reports/allure-report

# 或实时查看（边跑边看）
allure serve reports/allure-results
```

---

## 配置说明

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TEST_BASE_URL` | `http://localhost:1314` | 被测网站地址 |
| `TEST_BROWSER` | `chrome` | 浏览器类型（chrome/firefox/edge） |
| `TEST_HEADLESS` | `false` | 是否无头模式 |
| `TEST_USER_EMAIL` | `test@example.com` | 测试账号邮箱 |
| `TEST_USER_PASSWORD` | `123456` | 测试账号密码 |

### 修改配置

编辑 `config/settings.py`，或通过环境变量覆盖：

```bash
# 测试线上环境
set TEST_BASE_URL=https://ariconline.top
pytest -m smoke
```

---

## 测试用例清单

### 冒烟测试（@smoke，核心功能快速验证）

| 编号 | 测试点 | 预期结果 |
|------|--------|---------|
| S01 | 首页打开 | 页面正常加载 |
| S02 | Brand 区域可见 | 站名、一言显示 |
| S03 | 文章列表加载 | 至少1篇文章 |
| S04 | 点击文章跳转 | URL 包含 /article/ |
| S05 | 导航栏显示 | 导航栏可见 |
| S06 | 导航到留言板 | URL 包含 /message |
| S07 | 搜索功能 | 跳转到搜索结果 |
| S08 | 打开登录框 | 登录对话框可见 |
| S09 | 留言板加载 | 标题可见 |
| S10 | 404 处理 | 跳转到404页面 |

### 全量回归测试

- **首页**：Brand、文章列表、侧边栏、推荐轮播
- **导航**：所有导航链接、下拉菜单、搜索
- **登录**：打开对话框、表单字段、空提交、错误邮箱、错误密码、切换注册
- **留言板**：页面加载、输入框、发送按钮、弹幕容器
- **搜索**：有结果搜索、无结果搜索、空搜索
- **文章**：从首页进入、标题显示、评论区
- **404**：无效URL跳转、直接访问
- **响应式**：7种分辨率、移动端、平板端

---

## 设计模式说明

### Page Object Model（POM）

每个页面对应一个 Page 类，封装：
- **定位器**：CSS/XPath 选择器，集中管理
- **操作方法**：点击、输入、获取文本等
- **断言辅助**：is_xxx_visible 等方法

```python
# 示例：使用 Page Object
def test_home_page(home_page):
    home_page.open_home()
    assert home_page.is_brand_visible()
    articles = home_page.get_article_links()
    assert len(articles) > 0
```

**好处：**
- 页面结构变化时只需修改 Page 类，测试用例不用动
- 测试代码可读性高，像自然语言

### Allure 报告层级

```
Epic（史诗）    → 博客前台
  Feature（特性） → 首页 / 导航 / 登录 / 留言板 / ...
    Story（故事）   → 页面加载 / 文章展示 / 搜索功能 / ...
      Test（用例）    → test_home_page_loads / ...
```

---

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: UI Tests
on: [push, pull_request]

jobs:
  ui-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Setup Chrome
        uses: browser-actions/setup-chrome@v1

      - name: Install dependencies
        run: |
          cd ui-tests
          pip install -r requirements.txt

      - name: Run tests
        run: |
          cd ui-tests
          TEST_BASE_URL=https://ariconline.top TEST_HEADLESS=true pytest -m smoke

      - name: Upload Allure results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: allure-results
          path: ui-tests/reports/allure-results/
```

---

## 常见问题

### Q: ChromeDriver 版本不匹配？
A: 项目使用 `webdriver-manager` 自动下载匹配的驱动，通常无需手动处理。

### Q: 元素定位失败？
A: SPA 应用加载需要时间，可在 `config/settings.py` 中增大 `EXPLICIT_WAIT`。

### Q: 测试太慢？
A: 使用无头模式 `TEST_HEADLESS=true`，或只运行冒烟测试 `pytest -m smoke`。

### Q: 如何并行运行？
A: 安装 pytest-xdist 后：`pytest -n 4`（4个并行进程）。
