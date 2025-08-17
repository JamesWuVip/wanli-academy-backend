#!/bin/bash

# 万里书院集成测试环境检查脚本
# 检查Java、PostgreSQL、Maven、Node.js等环境依赖

set -e

echo "==========================================="
echo "万里书院集成测试环境检查"
echo "==========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果统计
PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0

# 检查函数
check_command() {
    local cmd=$1
    local name=$2
    local required_version=$3
    
    if command -v $cmd &> /dev/null; then
        local version=$(eval "$cmd --version 2>/dev/null | head -n1" || echo "未知版本")
        echo -e "${GREEN}✓${NC} $name: $version"
        ((PASS_COUNT++))
        return 0
    else
        echo -e "${RED}✗${NC} $name: 未安装"
        ((FAIL_COUNT++))
        return 1
    fi
}

check_service() {
    local service=$1
    local name=$2
    local port=$3
    
    if nc -z localhost $port 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $name: 运行中 (端口 $port)"
        ((PASS_COUNT++))
        return 0
    else
        echo -e "${YELLOW}⚠${NC} $name: 未运行 (端口 $port)"
        ((WARN_COUNT++))
        return 1
    fi
}

echo "\n1. 检查基础开发环境..."
echo "-------------------------------------------"

# 检查Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f1)
    if [ "$JAVA_MAJOR" -ge "17" ]; then
        echo -e "${GREEN}✓${NC} Java: $JAVA_VERSION (满足要求 >= 17)"
        ((PASS_COUNT++))
    else
        echo -e "${RED}✗${NC} Java: $JAVA_VERSION (需要 >= 17)"
        ((FAIL_COUNT++))
    fi
else
    echo -e "${RED}✗${NC} Java: 未安装"
    ((FAIL_COUNT++))
fi

# 检查Maven
check_command "mvn" "Maven" "3.6+"

# 检查Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    NODE_MAJOR=$(echo $NODE_VERSION | cut -d'.' -f1 | sed 's/v//')
    if [ "$NODE_MAJOR" -ge "18" ]; then
        echo -e "${GREEN}✓${NC} Node.js: $NODE_VERSION (满足要求 >= 18)"
        ((PASS_COUNT++))
    else
        echo -e "${YELLOW}⚠${NC} Node.js: $NODE_VERSION (推荐 >= 18)"
        ((WARN_COUNT++))
    fi
else
    echo -e "${RED}✗${NC} Node.js: 未安装"
    ((FAIL_COUNT++))
fi

# 检查npm
check_command "npm" "npm" "8+"

echo "\n2. 检查数据库环境..."
echo "-------------------------------------------"

# 检查PostgreSQL客户端
check_command "psql" "PostgreSQL客户端" "13+"

# 检查PostgreSQL服务
check_service "postgresql" "PostgreSQL服务" "5432"

# 检查数据库连接
if command -v psql &> /dev/null; then
    echo "\n测试数据库连接..."
    
    # 尝试连接默认数据库
    if psql -h localhost -U wujames -d postgres -c "SELECT version();" &> /dev/null; then
        echo -e "${GREEN}✓${NC} 数据库连接: 成功 (用户: wujames)"
        ((PASS_COUNT++))
    else
        echo -e "${YELLOW}⚠${NC} 数据库连接: 失败 (请检查PostgreSQL配置)"
        echo "  提示: 确保PostgreSQL服务运行且允许本地连接"
        ((WARN_COUNT++))
    fi
fi

echo "\n3. 检查网络工具..."
echo "-------------------------------------------"

# 检查curl
check_command "curl" "curl" "7+"

# 检查netcat (用于端口检查)
if command -v nc &> /dev/null; then
    echo -e "${GREEN}✓${NC} netcat: 已安装"
    ((PASS_COUNT++))
elif command -v netcat &> /dev/null; then
    echo -e "${GREEN}✓${NC} netcat: 已安装"
    ((PASS_COUNT++))
else
    echo -e "${YELLOW}⚠${NC} netcat: 未安装 (推荐安装用于端口检查)"
    ((WARN_COUNT++))
fi

echo "\n4. 检查项目结构..."
echo "-------------------------------------------"

# 检查项目文件
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}✓${NC} Maven项目: pom.xml存在"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗${NC} Maven项目: pom.xml不存在"
    ((FAIL_COUNT++))
fi

if [ -f "backend/src/main/resources/application.yml" ] || [ -f "backend/src/main/resources/application.properties" ]; then
    echo -e "${GREEN}✓${NC} Spring Boot配置: 存在"
    ((PASS_COUNT++))
else
    echo -e "${RED}✗${NC} Spring Boot配置: 不存在"
    ((FAIL_COUNT++))
fi

if [ -d "backend/src/test" ]; then
    echo -e "${GREEN}✓${NC} 测试目录: 存在"
    ((PASS_COUNT++))
else
    echo -e "${YELLOW}⚠${NC} 测试目录: 不存在"
    ((WARN_COUNT++))
fi

echo "\n5. 检查端口占用..."
echo "-------------------------------------------"

# 检查常用端口
PORTS=("8080" "5432" "3000")
PORT_NAMES=("Spring Boot" "PostgreSQL" "前端开发服务器")

for i in "${!PORTS[@]}"; do
    port=${PORTS[$i]}
    name=${PORT_NAMES[$i]}
    
    if nc -z localhost $port 2>/dev/null; then
        echo -e "${YELLOW}⚠${NC} 端口 $port ($name): 已被占用"
        ((WARN_COUNT++))
    else
        echo -e "${GREEN}✓${NC} 端口 $port ($name): 可用"
        ((PASS_COUNT++))
    fi
done

echo "\n==========================================="
echo "环境检查结果汇总"
echo "==========================================="
echo -e "${GREEN}通过: $PASS_COUNT${NC}"
echo -e "${YELLOW}警告: $WARN_COUNT${NC}"
echo -e "${RED}失败: $FAIL_COUNT${NC}"

TOTAL=$((PASS_COUNT + WARN_COUNT + FAIL_COUNT))
SUCCESS_RATE=$((PASS_COUNT * 100 / TOTAL))

echo "\n成功率: $SUCCESS_RATE%"

if [ $FAIL_COUNT -eq 0 ]; then
    if [ $WARN_COUNT -eq 0 ]; then
        echo -e "\n${GREEN}✓ 环境检查完全通过！可以开始集成测试。${NC}"
        exit 0
    else
        echo -e "\n${YELLOW}⚠ 环境检查基本通过，但有警告项目。建议解决后再进行集成测试。${NC}"
        exit 1
    fi
else
    echo -e "\n${RED}✗ 环境检查失败！请解决上述问题后再进行集成测试。${NC}"
    echo "\n建议解决步骤:"
    echo "1. 安装缺失的软件包"
    echo "2. 检查服务运行状态"
    echo "3. 验证数据库连接配置"
    echo "4. 重新运行此脚本验证"
    exit 2
fi