@echo off
chcp 65001 >nul
echo ==========================================
echo   博客 UI 自动化测试
echo ==========================================
echo.

:: 检查虚拟环境
if not exist "venv" (
    echo [INFO] 创建虚拟环境...
    python -m venv venv
)

:: 激活虚拟环境
call venv\Scripts\activate.bat

:: 安装依赖
echo [INFO] 安装依赖...
pip install -r requirements.txt -q

:: 运行模式选择
if "%1"=="smoke" (
    echo [INFO] 运行冒烟测试...
    pytest -m smoke --alluredir=reports/allure-results
) else if "%1"=="regression" (
    echo [INFO] 运行全量回归测试...
    pytest --alluredir=reports/allure-results
) else if "%1"=="headless" (
    echo [INFO] 无头模式运行...
    set TEST_HEADLESS=true
    pytest --alluredir=reports/allure-results
) else (
    echo [INFO] 运行全部测试...
    pytest --alluredir=reports/allure-results
)

echo.
echo [INFO] 测试完成！

:: 生成 Allure 报告
where allure >nul 2>&1
if %ERRORLEVEL%==0 (
    echo [INFO] 生成 Allure 报告...
    allure generate reports/allure-results -o reports/allure-report --clean
    echo [INFO] 打开报告...
    allure open reports/allure-report
) else (
    echo [WARN] 未安装 allure 命令行工具
    echo [WARN] 安装方法: scoop install allure  或  choco install allure
    echo [WARN] 或手动查看: reports/allure-results/
)

pause
