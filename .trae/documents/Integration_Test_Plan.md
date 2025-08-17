# 万里书院系统集成测试方案

## 文档信息
- **项目名称**: 万里书院在线作业管理系统
- **文档版本**: v1.0
- **创建日期**: 2025-08-17
- **适用范围**: Sprint 1-3 集成测试
- **测试类型**: 端到端集成测试

## 1. 环境配置

### 1.1 硬件要求

#### 测试服务器配置
- **CPU**: 最低4核心，推荐8核心 Intel/AMD x64处理器
- **内存**: 最低8GB RAM，推荐16GB RAM
- **存储**: 最低100GB可用磁盘空间，推荐SSD存储
- **网络**: 稳定的互联网连接，带宽不低于100Mbps

#### 数据库服务器配置
- **CPU**: 最低2核心，推荐4核心
- **内存**: 最低4GB RAM，推荐8GB RAM
- **存储**: 最低50GB可用磁盘空间，推荐SSD存储
- **网络**: 与应用服务器低延迟连接

### 1.2 软件要求

#### 操作系统
- **推荐**: Ubuntu 20.04 LTS 或 CentOS 8
- **备选**: macOS 12+ 或 Windows 10/11 Pro

#### 核心软件栈
- **Java**: OpenJDK 17 或 Oracle JDK 17
- **Maven**: 3.8.0+
- **PostgreSQL**: 14.0+ (生产级实例)
- **Node.js**: 18.0+ (用于前端和测试脚本)
- **Git**: 2.30+

#### 监控和工具
- **Docker**: 20.10+ (可选，用于容器化部署)
- **Postman**: 最新版本 (API测试)
- **JMeter**: 5.4+ (性能测试)
- **SonarQube**: 9.0+ (代码质量分析)

### 1.3 环境配置步骤

#### 1.3.1 数据库环境配置
```sql
-- 创建集成测试数据库
CREATE DATABASE wanli_academy_integration_test;

-- 创建测试用户
CREATE USER integration_test_user WITH PASSWORD 'IntegrationTest2025!';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE wanli_academy_integration_test TO integration_test_user;
```

#### 1.3.2 应用配置
```yaml
# application-integration.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wanli_academy_integration_test
    username: integration_test_user
    password: IntegrationTest2025!
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  profiles:
    active: integration

logging:
  level:
    com.wanli.academy.backend: DEBUG
    org.springframework.security: DEBUG
```

#### 1.3.3 环境验证脚本
```bash
#!/bin/bash
# environment-check.sh

echo "=== 万里书院集成测试环境检查 ==="

# 检查Java版本
java -version
if [ $? -ne 0 ]; then
    echo "错误: Java未安装或版本不兼容"
    exit 1
fi

# 检查PostgreSQL连接
psql -h localhost -U integration_test_user -d wanli_academy_integration_test -c "SELECT version();"
if [ $? -ne 0 ]; then
    echo "错误: PostgreSQL连接失败"
    exit 1
fi

# 检查Maven
mvn -version
if [ $? -ne 0 ]; then
    echo "错误: Maven未安装"
    exit 1
fi

echo "✅ 环境检查通过"
```

## 2. 测试数据准备

### 2.1 数据来源和分类

#### 2.1.1 基础数据
- **用户数据**: 包含不同角色的用户账户
  - 管理员账户: 2个
  - 总部教师账户: 5个
  - 加盟教师账户: 10个
  - 学生账户: 50个

- **机构数据**: 教育机构信息
  - 总部机构: 1个
  - 加盟机构: 5个

#### 2.1.2 业务数据
- **作业数据**: 覆盖不同类型和状态的作业
  - 已发布作业: 20个
  - 草稿作业: 10个
  - 已截止作业: 15个
  - 不同难度级别作业: 各10个

- **提交数据**: 学生作业提交记录
  - 已提交未批改: 100个
  - 已批改: 150个
  - 逾期提交: 30个

- **文件数据**: 各种格式的测试文件
  - PDF文件: 50个 (大小从1KB到10MB)
  - Word文档: 30个
  - 图片文件: 40个
  - 其他格式: 20个

### 2.2 数据准备脚本

#### 2.2.1 用户数据初始化
```sql
-- users_test_data.sql
INSERT INTO users (id, username, email, password_hash, role, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'admin1', 'admin1@wanli.edu', '$2a$10$...', 'ROLE_ADMIN', NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'hq_teacher1', 'teacher1@wanli.edu', '$2a$10$...', 'ROLE_HQ_TEACHER', NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'franchise_teacher1', 'fteacher1@partner.edu', '$2a$10$...', 'ROLE_FRANCHISE_TEACHER', NOW()),
('550e8400-e29b-41d4-a716-446655440004', 'student1', 'student1@student.edu', '$2a$10$...', 'ROLE_STUDENT', NOW());
```

#### 2.2.2 作业数据初始化
```sql
-- assignments_test_data.sql
INSERT INTO assignments (id, title, description, due_date, creator_id, status, created_at) VALUES
('660e8400-e29b-41d4-a716-446655440001', '数学基础练习', '包含代数和几何基础题目', '2025-09-01 23:59:59', '550e8400-e29b-41d4-a716-446655440002', 'PUBLISHED', NOW()),
('660e8400-e29b-41d4-a716-446655440002', '语文阅读理解', '现代文阅读理解练习', '2025-09-05 23:59:59', '550e8400-e29b-41d4-a716-446655440002', 'PUBLISHED', NOW());
```

#### 2.2.3 数据导入脚本
```bash
#!/bin/bash
# import-test-data.sh

DB_HOST="localhost"
DB_NAME="wanli_academy_integration_test"
DB_USER="integration_test_user"

echo "开始导入测试数据..."

# 导入用户数据
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f users_test_data.sql
echo "✅ 用户数据导入完成"

# 导入作业数据
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f assignments_test_data.sql
echo "✅ 作业数据导入完成"

# 导入提交数据
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f submissions_test_data.sql
echo "✅ 提交数据导入完成"

# 验证数据完整性
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) as user_count FROM users;"
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) as assignment_count FROM assignments;"
psql -h $DB_HOST -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) as submission_count FROM submissions;"

echo "✅ 测试数据导入完成"
```

### 2.3 边界条件数据

#### 2.3.1 极限数据场景
- **大文件上传**: 测试文件大小接近系统限制(50MB)
- **批量操作**: 单次操作涉及100+记录
- **并发访问**: 模拟100个用户同时访问
- **特殊字符**: 包含Unicode、特殊符号的数据

#### 2.3.2 异常数据场景
- **无效数据**: 格式错误、类型不匹配的数据
- **过期数据**: 已过截止时间的作业和提交
- **权限边界**: 跨角色访问尝试
- **空值处理**: 必填字段为空的情况

## 3. 测试流程

### 3.1 测试阶段划分

#### 3.1.1 准备阶段 (1天)
**负责人**: 测试经理 + DevOps工程师
**主要任务**:
1. 环境搭建和配置验证
2. 测试数据准备和导入
3. 测试工具配置
4. 基线性能测试

**验收标准**:
- 所有环境检查脚本通过
- 测试数据完整性验证通过
- 应用启动正常，健康检查通过

#### 3.1.2 功能集成测试 (3天)
**负责人**: 高级测试工程师 + 开发团队
**主要任务**:
1. 用户认证和授权流程测试
2. 作业管理完整流程测试
3. 提交和批改流程测试
4. 文件上传下载流程测试
5. 跨模块数据一致性测试

**测试用例执行顺序**:
```
1. 用户管理模块测试
   ├── 用户注册登录
   ├── 角色权限验证
   └── 用户信息管理

2. 作业管理模块测试
   ├── 作业创建和编辑
   ├── 作业发布和撤回
   ├── 作业查询和筛选
   └── 作业截止时间处理

3. 提交批改模块测试
   ├── 学生作业提交
   ├── 教师批改评分
   ├── 批改结果反馈
   └── 提交历史管理

4. 文件管理模块测试
   ├── 文件上传下载
   ├── 文件格式验证
   ├── 文件大小限制
   └── 文件安全检查

5. 集成流程测试
   ├── 端到端业务流程
   ├── 数据一致性验证
   └── 异常场景处理
```

#### 3.1.3 性能集成测试 (2天)
**负责人**: 性能测试工程师
**主要任务**:
1. 负载测试 (正常负载下的性能表现)
2. 压力测试 (系统极限性能测试)
3. 并发测试 (多用户并发访问)
4. 稳定性测试 (长时间运行稳定性)

#### 3.1.4 安全集成测试 (1天)
**负责人**: 安全测试工程师
**主要任务**:
1. 身份认证安全测试
2. 权限控制测试
3. 数据传输安全测试
4. SQL注入和XSS防护测试

### 3.2 测试执行管理

#### 3.2.1 测试用例管理
```json
{
  "testCase": {
    "id": "TC_INT_001",
    "title": "用户登录集成测试",
    "priority": "High",
    "category": "Authentication",
    "preconditions": [
      "测试环境已启动",
      "测试用户数据已导入"
    ],
    "steps": [
      {
        "step": 1,
        "action": "访问登录页面",
        "expected": "页面正常加载"
      },
      {
        "step": 2,
        "action": "输入有效用户名密码",
        "expected": "登录成功，跳转到主页"
      }
    ],
    "expectedResult": "用户成功登录系统",
    "actualResult": "",
    "status": "Not Executed",
    "executedBy": "",
    "executedDate": "",
    "defects": []
  }
}
```

#### 3.2.2 缺陷管理流程

**缺陷严重级别定义**:
- **Critical**: 系统崩溃、数据丢失、安全漏洞
- **High**: 核心功能无法使用、性能严重下降
- **Medium**: 功能部分异常、用户体验问题
- **Low**: 界面问题、文档错误

**缺陷处理流程**:
```
发现缺陷 → 记录缺陷 → 分配开发人员 → 修复验证 → 关闭缺陷
     ↓           ↓           ↓           ↓           ↓
  详细描述    优先级评估    修复开发      回归测试    状态更新
```

#### 3.2.3 测试报告模板
```markdown
# 集成测试执行报告

## 测试概要
- 测试日期: [日期]
- 测试版本: [版本号]
- 测试环境: [环境信息]
- 执行人员: [测试团队]

## 测试结果统计
- 总用例数: [数量]
- 通过用例: [数量]
- 失败用例: [数量]
- 阻塞用例: [数量]
- 通过率: [百分比]

## 缺陷统计
- Critical: [数量]
- High: [数量]
- Medium: [数量]
- Low: [数量]

## 性能指标
- 平均响应时间: [毫秒]
- 并发用户数: [数量]
- 系统稳定性: [评估]

## 风险评估
[风险描述和建议]

## 结论和建议
[测试结论和下一步建议]
```

## 4. 验收标准

### 4.1 功能验收标准

#### 4.1.1 通过标准
- **用户认证模块**: 100%测试用例通过
- **作业管理模块**: 95%以上测试用例通过
- **提交批改模块**: 95%以上测试用例通过
- **文件管理模块**: 90%以上测试用例通过
- **集成流程**: 90%以上端到端流程测试通过

#### 4.1.2 失败标准
- 存在Critical级别缺陷
- 核心业务流程无法完成
- 数据一致性问题
- 安全漏洞

### 4.2 性能验收标准

#### 4.2.1 关键性能指标 (KPIs)

**响应时间要求**:
- API响应时间: 平均 < 500ms, 95%分位 < 1000ms
- 页面加载时间: < 3秒
- 文件上传时间: < 30秒 (10MB文件)
- 数据库查询时间: < 200ms

**并发性能要求**:
- 支持并发用户数: ≥ 200
- 系统吞吐量: ≥ 1000 TPS
- 错误率: < 0.1%

**资源使用要求**:
- CPU使用率: < 80% (正常负载)
- 内存使用率: < 85%
- 磁盘I/O: < 80%
- 网络带宽: < 70%

#### 4.2.2 质量指标 (QIs)

**稳定性指标**:
- 系统可用性: ≥ 99.9%
- 平均故障间隔时间(MTBF): ≥ 720小时
- 平均恢复时间(MTTR): ≤ 30分钟

**可靠性指标**:
- 数据完整性: 100%
- 事务一致性: 100%
- 备份恢复成功率: 100%

**安全性指标**:
- 身份认证成功率: 100%
- 权限控制有效性: 100%
- 安全漏洞数量: 0

### 4.3 验收决策矩阵

| 测试类型 | 通过标准 | 权重 | 必须通过 |
|---------|---------|------|----------|
| 功能测试 | ≥95% | 40% | 是 |
| 性能测试 | 所有KPI达标 | 25% | 是 |
| 安全测试 | 100%通过 | 20% | 是 |
| 稳定性测试 | ≥99%可用性 | 10% | 是 |
| 兼容性测试 | ≥90% | 5% | 否 |

**进入下一阶段的条件**:
1. 所有"必须通过"项目达到标准
2. 综合得分 ≥ 90%
3. 无Critical和High级别未修复缺陷
4. 性能测试报告通过技术委员会审核
5. 安全测试报告通过安全委员会审核

### 4.4 测试完成标准

#### 4.4.1 退出标准
- 所有计划测试用例执行完毕
- 所有Critical和High级别缺陷已修复并验证
- 性能指标达到预期要求
- 测试覆盖率达到要求 (功能覆盖率≥95%)
- 测试报告已完成并通过评审

#### 4.4.2 风险评估

**高风险场景**:
- 数据库连接池耗尽
- 大文件上传导致内存溢出
- 并发访问导致死锁
- 权限绕过漏洞

**风险缓解措施**:
- 实施连接池监控和自动扩容
- 配置文件上传大小限制和流式处理
- 数据库锁优化和死锁检测
- 多层权限验证和审计日志

## 5. 测试工具和自动化

### 5.1 自动化测试脚本

#### 5.1.1 集成测试启动脚本
```bash
#!/bin/bash
# integration-test-runner.sh

set -e

echo "=== 万里书院集成测试执行器 ==="

# 环境检查
./scripts/environment-check.sh

# 启动应用
echo "启动应用服务..."
cd backend
mvn spring-boot:run -Dspring.profiles.active=integration &
APP_PID=$!

# 等待应用启动
echo "等待应用启动..."
sleep 30

# 健康检查
curl -f http://localhost:8080/actuator/health || {
    echo "应用启动失败"
    kill $APP_PID
    exit 1
}

# 执行集成测试
echo "执行集成测试..."
node ../integration-tests/test-runner.js

# 生成测试报告
echo "生成测试报告..."
node ../integration-tests/report-generator.js

# 清理
kill $APP_PID
echo "✅ 集成测试完成"
```

### 5.2 持续集成配置

#### 5.2.1 GitHub Actions配置
```yaml
# .github/workflows/integration-test.yml
name: Integration Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  integration-test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_DB: wanli_academy_integration_test
          POSTGRES_USER: integration_test_user
          POSTGRES_PASSWORD: IntegrationTest2025!
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: Install dependencies
      run: |
        npm install
        cd backend && mvn dependency:resolve
    
    - name: Run integration tests
      run: ./scripts/integration-test-runner.sh
      env:
        DATABASE_URL: postgresql://integration_test_user:IntegrationTest2025!@localhost:5432/wanli_academy_integration_test
    
    - name: Upload test reports
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: integration-test-reports
        path: |
          integration-test-report.html
          integration-test-report.json
```

## 6. 总结

本集成测试方案为万里书院系统提供了全面的测试框架，确保三个Sprint的功能能够无缝集成并满足生产环境要求。通过严格的环境配置、真实数据测试、系统化的测试流程和明确的验收标准，我们能够有效识别和解决集成过程中的问题，保证系统的稳定性、性能和安全性。

**关键成功因素**:
1. 使用真实PostgreSQL数据库实例，确保测试环境与生产环境一致
2. 全面的测试数据覆盖各种业务场景和边界条件
3. 系统化的测试流程和明确的角色职责
4. 严格的验收标准和风险控制措施
5. 自动化测试工具和持续集成支持

通过执行本测试方案，我们将确保万里书院系统能够稳定、高效地为用户提供在线作业管理服务。