#!/bin/bash

# 万里书院集成测试数据导入脚本
# 自动导入用户、角色、作业等测试数据

set -e

echo "==========================================="
echo "万里书院集成测试数据导入"
echo "==========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 数据库连接配置
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"5432"}
DB_NAME=${DB_NAME:-"wanli_academy_test"}
DB_USER=${DB_USER:-"wujames"}
DB_PASSWORD=${DB_PASSWORD:-""}

# 测试数据文件路径
TEST_DATA_DIR="integration-tests/test-data"

# 检查必要文件
check_files() {
    echo -e "${BLUE}检查测试数据文件...${NC}"
    
    local files=(
        "$TEST_DATA_DIR/roles_test_data.sql"
        "$TEST_DATA_DIR/users_test_data.sql"
        "$TEST_DATA_DIR/assignments_test_data.sql"
        "$TEST_DATA_DIR/assignments_sprint3_test_data.sql"
        "$TEST_DATA_DIR/submissions_test_data.sql"
    )
    
    for file in "${files[@]}"; do
        if [ -f "$file" ]; then
            echo -e "${GREEN}✓${NC} $file"
        else
            echo -e "${RED}✗${NC} $file 不存在"
            exit 1
        fi
    done
}

# 检查数据库连接
check_database() {
    echo -e "\n${BLUE}检查数据库连接...${NC}"
    
    # 构建连接字符串
    if [ -n "$DB_PASSWORD" ]; then
        export PGPASSWORD="$DB_PASSWORD"
    fi
    
    # 测试连接
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} 数据库连接成功"
        
        # 显示数据库信息
        DB_VERSION=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT version();" | head -n1 | xargs)
        echo "  数据库: $DB_NAME@$DB_HOST:$DB_PORT"
        echo "  版本: $DB_VERSION"
    else
        echo -e "${RED}✗${NC} 数据库连接失败"
        echo "  请检查数据库配置:"
        echo "  - 主机: $DB_HOST"
        echo "  - 端口: $DB_PORT"
        echo "  - 数据库: $DB_NAME"
        echo "  - 用户: $DB_USER"
        exit 1
    fi
}

# 备份现有数据（可选）
backup_data() {
    echo -e "\n${BLUE}备份现有测试数据...${NC}"
    
    local backup_dir="integration-tests/backups"
    local backup_file="$backup_dir/test_data_backup_$(date +%Y%m%d_%H%M%S).sql"
    
    mkdir -p "$backup_dir"
    
    # 备份测试相关表数据
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        \copy (SELECT * FROM users WHERE username LIKE 'test_%') TO '$backup_file.users.csv' WITH CSV HEADER;
        \copy (SELECT * FROM homeworks WHERE title LIKE 'Test_%') TO '$backup_file.homeworks.csv' WITH CSV HEADER;
    " > /dev/null 2>&1 || true
    
    echo -e "${GREEN}✓${NC} 备份完成: $backup_file"
}

# 执行SQL文件
execute_sql_file() {
    local file=$1
    local description=$2
    
    echo -e "\n${BLUE}导入 $description...${NC}"
    echo "文件: $file"
    
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$file"; then
        echo -e "${GREEN}✓${NC} $description 导入成功"
    else
        echo -e "${RED}✗${NC} $description 导入失败"
        exit 1
    fi
}

# 验证导入结果
verify_import() {
    echo -e "\n${BLUE}验证导入结果...${NC}"
    
    # 检查用户数据
    local user_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM users WHERE username LIKE 'test_%';" | xargs)
    echo -e "${GREEN}✓${NC} 测试用户: $user_count 个"
    
    # 检查作业数据
    local homework_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM homeworks WHERE title LIKE 'Test_%';" | xargs)
    echo -e "${GREEN}✓${NC} 测试作业: $homework_count 个"
    
    # 检查题目数据
    local question_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM questions q JOIN homeworks h ON q.homework_id = h.id WHERE h.title LIKE 'Test_%';" | xargs)
    echo -e "${GREEN}✓${NC} 测试题目: $question_count 个"
    
    # 检查角色分配
    local role_assignment_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM user_roles ur JOIN users u ON ur.user_id = u.id WHERE u.username LIKE 'test_%';" | xargs)
    echo -e "${GREEN}✓${NC} 角色分配: $role_assignment_count 个"
}

# 显示测试账户信息
show_test_accounts() {
    echo -e "\n${BLUE}测试账户信息:${NC}"
    echo "==========================================="
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT 
            u.username,
            u.email,
            r.name as role_name
        FROM users u
        JOIN user_roles ur ON u.id = ur.user_id
        JOIN roles r ON ur.role_id = r.id
        WHERE u.username LIKE 'test_%'
        ORDER BY u.username;
    "
    
    echo -e "\n${YELLOW}默认密码: password123${NC}"
    echo -e "${YELLOW}密码已使用BCrypt加密存储${NC}"
}

# 清理测试数据函数
clean_test_data() {
    echo -e "\n${YELLOW}清理现有测试数据...${NC}"
    
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        -- 清理作业相关数据
        DELETE FROM questions WHERE homework_id IN (
            SELECT id FROM homeworks WHERE title LIKE 'Test_%'
        );
        DELETE FROM homeworks WHERE title LIKE 'Test_%';
        
        -- 清理用户相关数据
        DELETE FROM user_roles WHERE user_id IN (
            SELECT id FROM users WHERE username LIKE 'test_%'
        );
        DELETE FROM users WHERE username LIKE 'test_%';
    " > /dev/null 2>&1
    
    echo -e "${GREEN}✓${NC} 清理完成"
}

# 主函数
main() {
    echo "开始导入测试数据..."
    echo "数据库: $DB_NAME@$DB_HOST:$DB_PORT"
    echo "用户: $DB_USER"
    echo ""
    
    # 检查参数
    if [ "$1" = "--clean" ]; then
        clean_test_data
        echo -e "\n${GREEN}测试数据清理完成！${NC}"
        exit 0
    fi
    
    # 执行导入流程
    check_files
    check_database
    
    # 可选备份
    if [ "$1" = "--backup" ]; then
        backup_data
    fi
    
    # 导入数据
    execute_sql_file "$TEST_DATA_DIR/roles_test_data.sql" "角色和权限数据"
    execute_sql_file "$TEST_DATA_DIR/users_test_data.sql" "用户数据"
    execute_sql_file "$TEST_DATA_DIR/assignments_test_data.sql" "作业数据"
    execute_sql_file "$TEST_DATA_DIR/assignments_sprint3_test_data.sql" "Sprint3作业数据"
    execute_sql_file "$TEST_DATA_DIR/submissions_test_data.sql" "作业提交数据"
    
    # 验证结果
    verify_import
    show_test_accounts
    
    echo -e "\n${GREEN}===========================================${NC}"
    echo -e "${GREEN}测试数据导入完成！${NC}"
    echo -e "${GREEN}===========================================${NC}"
    
    echo -e "\n使用说明:"
    echo "1. 使用测试账户登录系统进行集成测试"
    echo "2. 运行 './import-test-data.sh --clean' 清理测试数据"
    echo "3. 运行 './import-test-data.sh --backup' 导入前备份数据"
}

# 显示帮助信息
show_help() {
    echo "万里书院集成测试数据导入脚本"
    echo ""
    echo "用法:"
    echo "  $0                导入测试数据"
    echo "  $0 --clean       清理测试数据"
    echo "  $0 --backup      导入前备份现有数据"
    echo "  $0 --help        显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  DB_HOST          数据库主机 (默认: localhost)"
    echo "  DB_PORT          数据库端口 (默认: 5432)"
    echo "  DB_NAME          数据库名称 (默认: wanli_academy)"
    echo "  DB_USER          数据库用户 (默认: postgres)"
    echo "  DB_PASSWORD      数据库密码"
    echo ""
    echo "示例:"
    echo "  DB_PASSWORD=mypass ./import-test-data.sh"
    echo "  DB_NAME=test_db ./import-test-data.sh --backup"
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