#!/bin/bash
# 博客 UI 自动化测试运行脚本
set -e

echo "=========================================="
echo "  博客 UI 自动化测试"
echo "=========================================="
echo ""

# 检查虚拟环境
if [ ! -d "venv" ]; then
    echo "[INFO] 创建虚拟环境..."
    python3 -m venv venv
fi

# 激活虚拟环境
source venv/bin/activate

# 安装依赖
echo "[INFO] 安装依赖..."
pip install -r requirements.txt -q

# 运行测试
case "$1" in
    smoke)
        echo "[INFO] 运行冒烟测试..."
        pytest -m smoke --alluredir=reports/allure-results
        ;;
    regression)
        echo "[INFO] 运行全量回归测试..."
        pytest --alluredir=reports/allure-results
        ;;
    headless)
        echo "[INFO] 无头模式运行..."
        TEST_HEADLESS=true pytest --alluredir=reports/allure-results
        ;;
    *)
        echo "[INFO] 运行全部测试..."
        pytest --alluredir=reports/allure-results
        ;;
esac

echo ""
echo "[INFO] 测试完成！"

# 生成 Allure 报告
if command -v allure &> /dev/null; then
    echo "[INFO] 生成 Allure 报告..."
    allure generate reports/allure-results -o reports/allure-report --clean
    echo "[INFO] 打开报告: allure open reports/allure-report"
else
    echo "[WARN] 未安装 allure 命令行工具"
    echo "[WARN] 安装方法: brew install allure (Mac) / sudo apt install allure (Linux)"
fi
