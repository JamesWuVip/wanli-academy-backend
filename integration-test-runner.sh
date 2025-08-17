#!/bin/bash

# 万里书院集成测试执行器
# 协调整个集成测试流程：环境检查、数据准备、应用启动、测试执行、报告生成

set -e

echo "==========================================="
echo "万里书院集成测试执行器"
echo "==========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 配置变量
TEST_PROFILE=${TEST_PROFILE:-"integration"}
SPRING_BOOT_JAR="target/backend-0.0.1-SNAPSHOT.jar"
APP_PORT=${APP_PORT:-"8080"}
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"5432"}
DB_NAME=${DB_NAME:-"wanli_academy_test"}
DB_USER=${DB_USER:-"wanli_test"}
DB_PASSWORD=${DB_PASSWORD:-"test_password_123"}
TEST_TIMEOUT=${TEST_TIMEOUT:-"300"}
REPORT_DIR="integration-tests/reports"
LOG_DIR="integration-tests/logs"

# 进程ID文件
APP_PID_FILE="$LOG_DIR/app.pid"

# 创建必要目录
mkdir -p "$REPORT_DIR" "$LOG_DIR"

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}清理测试环境...${NC}"
    
    # 停止Spring Boot应用
    if [ -f "$APP_PID_FILE" ]; then
        local app_pid=$(cat "$APP_PID_FILE")
        if kill -0 "$app_pid" 2>/dev/null; then
            echo "停止Spring Boot应用 (PID: $app_pid)"
            kill "$app_pid" || true
            sleep 3
            kill -9 "$app_pid" 2>/dev/null || true
        fi
        rm -f "$APP_PID_FILE"
    fi
    
    # 清理端口占用
    local pids=$(lsof -ti:$APP_PORT 2>/dev/null || true)
    if [ -n "$pids" ]; then
        echo "清理端口 $APP_PORT 占用"
        echo "$pids" | xargs kill -9 2>/dev/null || true
    fi
    
    echo -e "${GREEN}清理完成${NC}"
}

# 设置信号处理
trap cleanup EXIT INT TERM

# 步骤1: 环境检查
step_environment_check() {
    echo -e "\n${BLUE}步骤1: 环境检查${NC}"
    echo "=========================================="
    
    if [ -f "./environment-check.sh" ]; then
        ./environment-check.sh
        local check_result=$?
        if [ $check_result -eq 0 ]; then
            echo -e "${GREEN}✓ 环境检查完全通过${NC}"
        elif [ $check_result -eq 1 ]; then
            echo -e "${YELLOW}⚠ 环境检查基本通过，继续执行测试${NC}"
        else
            echo -e "${RED}✗ 环境检查失败${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}⚠ 环境检查脚本不存在，跳过检查${NC}"
    fi
}

# 步骤2: 构建应用
step_build_application() {
    echo -e "\n${BLUE}步骤2: 构建应用${NC}"
    echo "=========================================="
    
    cd backend
    
    echo "清理之前的构建..."
    mvn clean > "../$LOG_DIR/build.log" 2>&1
    
    echo "编译和打包应用..."
    if mvn package -DskipTests >> "../$LOG_DIR/build.log" 2>&1; then
        echo -e "${GREEN}✓ 应用构建成功${NC}"
        
        # 检查JAR文件
        if [ -f "$SPRING_BOOT_JAR" ]; then
            local jar_size=$(du -h "$SPRING_BOOT_JAR" | cut -f1)
            echo "  JAR文件: $SPRING_BOOT_JAR ($jar_size)"
        else
            echo -e "${RED}✗ JAR文件不存在: $SPRING_BOOT_JAR${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ 应用构建失败${NC}"
        echo "查看构建日志: $LOG_DIR/build.log"
        tail -20 "../$LOG_DIR/build.log"
        exit 1
    fi
    
    cd ..
}

# 步骤3: 准备测试数据
step_prepare_test_data() {
    echo -e "\n${BLUE}步骤3: 准备测试数据${NC}"
    echo "=========================================="
    
    if [ -f "./import-test-data.sh" ]; then
        echo "导入测试数据..."
        if ./import-test-data.sh > "$LOG_DIR/test-data.log" 2>&1; then
            echo -e "${GREEN}✓ 测试数据准备完成${NC}"
        else
            echo -e "${RED}✗ 测试数据准备失败${NC}"
            echo "查看数据导入日志: $LOG_DIR/test-data.log"
            tail -10 "$LOG_DIR/test-data.log"
            exit 1
        fi
    else
        echo -e "${YELLOW}⚠ 测试数据导入脚本不存在，跳过数据准备${NC}"
    fi
}

# 步骤4: 启动应用
step_start_application() {
    echo -e "\n${BLUE}步骤4: 启动应用${NC}"
    echo "=========================================="
    
    cd backend
    
    # 检查端口占用
    if nc -z localhost $APP_PORT 2>/dev/null; then
        echo -e "${YELLOW}端口 $APP_PORT 已被占用，尝试清理...${NC}"
        local pids=$(lsof -ti:$APP_PORT 2>/dev/null || true)
        if [ -n "$pids" ]; then
            echo "$pids" | xargs kill -9 2>/dev/null || true
            sleep 2
        fi
    fi
    
    # 启动Spring Boot应用
    echo "启动Spring Boot应用..."
    echo "配置文件: application-$TEST_PROFILE.yml"
    echo "端口: $APP_PORT"
    
    # 设置环境变量
    export SPRING_PROFILES_ACTIVE="$TEST_PROFILE"
    export SERVER_PORT="$APP_PORT"
    export SPRING_DATASOURCE_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
    export SPRING_DATASOURCE_USERNAME="$DB_USER"
    
    # 启动应用（后台运行）
    nohup java -jar "$SPRING_BOOT_JAR" \
        --spring.profiles.active="$TEST_PROFILE" \
        --server.port="$APP_PORT" \
        --spring.datasource.url="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
        --spring.datasource.username="$DB_USER" \
        > "../$LOG_DIR/app.log" 2>&1 &
    
    local app_pid=$!
    echo $app_pid > "../$APP_PID_FILE"
    
    echo "应用启动中... (PID: $app_pid)"
    
    # 等待应用启动
    local max_wait=60
    local wait_count=0
    
    while [ $wait_count -lt $max_wait ]; do
        if nc -z localhost $APP_PORT 2>/dev/null; then
            echo -e "${GREEN}✓ 应用启动成功${NC}"
            echo "  URL: http://localhost:$APP_PORT"
            echo "  健康检查: http://localhost:$APP_PORT/actuator/health"
            
            # 验证健康状态
            sleep 2
            if curl -s "http://localhost:$APP_PORT/actuator/health" | grep -q '"status":"UP"'; then
                echo -e "${GREEN}✓ 应用健康检查通过${NC}"
                cd ..
                return 0
            fi
        fi
        
        echo -n "."
        sleep 1
        ((wait_count++))
    done
    
    echo -e "\n${RED}✗ 应用启动超时${NC}"
    echo "查看应用日志: $LOG_DIR/app.log"
    tail -20 "../$LOG_DIR/app.log"
    cd ..
    exit 1
}

# 步骤5: 执行集成测试
step_run_integration_tests() {
    echo -e "\n${BLUE}步骤5: 执行集成测试${NC}"
    echo "=========================================="
    
    if [ -f "integration-tests/test-runner.js" ]; then
        echo "运行Node.js测试执行器..."
        
        # 设置测试环境变量
        export TEST_BASE_URL="http://localhost:$APP_PORT"
        export TEST_TIMEOUT="$TEST_TIMEOUT"
        export REPORT_DIR="$REPORT_DIR"
        
        if node integration-tests/test-runner.js > "$LOG_DIR/integration-tests.log" 2>&1; then
            echo -e "${GREEN}✓ 集成测试执行完成${NC}"
        else
            echo -e "${RED}✗ 集成测试执行失败${NC}"
            echo "查看测试日志: $LOG_DIR/integration-tests.log"
            tail -20 "$LOG_DIR/integration-tests.log"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ 集成测试执行器不存在，跳过测试${NC}"
        return 1
    fi
}

# 步骤6: 生成测试报告
step_generate_report() {
    echo -e "\n${BLUE}步骤6: 生成测试报告${NC}"
    echo "=========================================="
    
    if [ -f "integration-tests/report-generator.js" ]; then
        echo "生成测试报告..."
        
        if node integration-tests/report-generator.js > "$LOG_DIR/report-generation.log" 2>&1; then
            echo -e "${GREEN}✓ 测试报告生成完成${NC}"
            
            # 显示报告文件
            if [ -f "$REPORT_DIR/integration-test-report.html" ]; then
                echo "  HTML报告: $REPORT_DIR/integration-test-report.html"
            fi
            if [ -f "$REPORT_DIR/integration-test-report.json" ]; then
                echo "  JSON报告: $REPORT_DIR/integration-test-report.json"
            fi
        else
            echo -e "${YELLOW}⚠ 测试报告生成失败${NC}"
            echo "查看报告生成日志: $LOG_DIR/report-generation.log"
        fi
    else
        echo -e "${YELLOW}⚠ 报告生成器不存在，跳过报告生成${NC}"
    fi
}

# 显示测试结果摘要
show_test_summary() {
    echo -e "\n${PURPLE}===========================================${NC}"
    echo -e "${PURPLE}集成测试结果摘要${NC}"
    echo -e "${PURPLE}===========================================${NC}"
    
    # 显示测试统计
    if [ -f "$REPORT_DIR/integration-test-report.json" ]; then
        echo "测试报告: $REPORT_DIR/integration-test-report.json"
        
        # 尝试解析JSON报告（如果有jq工具）
        if command -v jq &> /dev/null; then
            local total_tests=$(jq -r '.summary.total_tests // "N/A"' "$REPORT_DIR/integration-test-report.json")
            local passed_tests=$(jq -r '.summary.passed_tests // "N/A"' "$REPORT_DIR/integration-test-report.json")
            local failed_tests=$(jq -r '.summary.failed_tests // "N/A"' "$REPORT_DIR/integration-test-report.json")
            local success_rate=$(jq -r '.summary.success_rate // "N/A"' "$REPORT_DIR/integration-test-report.json")
            
            echo "总测试数: $total_tests"
            echo "通过测试: $passed_tests"
            echo "失败测试: $failed_tests"
            echo "成功率: $success_rate%"
        fi
    fi
    
    # 显示日志文件
    echo -e "\n日志文件:"
    echo "- 构建日志: $LOG_DIR/build.log"
    echo "- 应用日志: $LOG_DIR/app.log"
    echo "- 测试日志: $LOG_DIR/integration-tests.log"
    echo "- 数据导入日志: $LOG_DIR/test-data.log"
    
    # 显示报告文件
    if [ -d "$REPORT_DIR" ] && [ "$(ls -A $REPORT_DIR)" ]; then
        echo -e "\n报告文件:"
        ls -la "$REPORT_DIR"/
    fi
}

# 主函数
main() {
    local start_time=$(date +%s)
    
    echo "开始集成测试..."
    echo "测试配置: $TEST_PROFILE"
    echo "应用端口: $APP_PORT"
    echo "数据库: $DB_NAME@$DB_HOST:$DB_PORT"
    echo "超时时间: ${TEST_TIMEOUT}秒"
    echo ""
    
    # 执行测试步骤
    local test_result=0
    
    step_environment_check || test_result=1
    
    if [ $test_result -eq 0 ]; then
        step_build_application || test_result=1
    fi
    
    if [ $test_result -eq 0 ]; then
        step_prepare_test_data || test_result=1
    fi
    
    if [ $test_result -eq 0 ]; then
        step_start_application || test_result=1
    fi
    
    if [ $test_result -eq 0 ]; then
        step_run_integration_tests || test_result=1
    fi
    
    step_generate_report
    
    # 计算执行时间
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    show_test_summary
    
    echo -e "\n执行时间: ${duration}秒"
    
    if [ $test_result -eq 0 ]; then
        echo -e "\n${GREEN}===========================================${NC}"
        echo -e "${GREEN}集成测试执行成功！${NC}"
        echo -e "${GREEN}===========================================${NC}"
    else
        echo -e "\n${RED}===========================================${NC}"
        echo -e "${RED}集成测试执行失败！${NC}"
        echo -e "${RED}===========================================${NC}"
    fi
    
    exit $test_result
}

# 显示帮助信息
show_help() {
    echo "万里书院集成测试执行器"
    echo ""
    echo "用法:"
    echo "  $0                运行完整集成测试"
    echo "  $0 --help        显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  TEST_PROFILE     测试配置文件 (默认: integration)"
    echo "  APP_PORT         应用端口 (默认: 8080)"
    echo "  DB_HOST          数据库主机 (默认: localhost)"
    echo "  DB_PORT          数据库端口 (默认: 5432)"
    echo "  DB_NAME          数据库名称 (默认: wanli_academy)"
    echo "  DB_USER          数据库用户 (默认: postgres)"
    echo "  TEST_TIMEOUT     测试超时时间秒数 (默认: 300)"
    echo ""
    echo "示例:"
    echo "  TEST_PROFILE=test ./integration-test-runner.sh"
    echo "  APP_PORT=8081 DB_NAME=test_db ./integration-test-runner.sh"
}

# 处理命令行参数
case "$1" in
    --help|-h)
        show_help
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac