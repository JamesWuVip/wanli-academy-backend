# 项目拆分指南：前后端分离部署方案

## 1. 当前项目结构分析

### 1.1 现有单体仓库结构
当前 `wanliRepo` 项目采用单体仓库（Monorepo）架构：

```
wanliRepo/
├── backend/          # Spring Boot 后端项目
│   ├── src/
│   ├── pom.xml
│   ├── railway.json
│   └── nixpacks.toml
├── frontend/         # Vue.js 前端项目
│   ├── src/
│   ├── package.json
│   ├── railway.json
│   └── nixpacks.toml
├── integration-tests/ # 集成测试
├── .github/workflows/ # CI/CD 配置
├── pom.xml           # 根级别 Maven 配置
└── railway.json      # 根级别部署配置
```

### 1.2 当前部署问题
- Railway 平台误识别项目类型（Java vs Node.js）
- 前端服务部署失败（404 错误）
- 单一仓库导致部署配置冲突
- 无法独立扩展前后端服务

## 2. 拆分策略和步骤

### 2.1 拆分原则
- **独立性**：前后端完全独立开发、测试、部署
- **可维护性**：清晰的代码组织和依赖管理
- **可扩展性**：支持独立的技术栈升级
- **部署灵活性**：支持不同的部署策略和环境

### 2.2 拆分步骤

#### 步骤1：创建独立仓库
1. 创建新的 GitHub 仓库：
   - `wanli-backend`：后端 Spring Boot 项目
   - `wanli-frontend`：前端 Vue.js 项目

2. 迁移代码：
   ```bash
   # 后端仓库
   git clone https://github.com/JamesWuVip/wanli-backend.git
   cp -r wanliRepo/backend/* wanli-backend/
   
   # 前端仓库
   git clone https://github.com/JamesWuVip/wanli-frontend.git
   cp -r wanliRepo/frontend/* wanli-frontend/
   ```

#### 步骤2：调整项目结构
- 将 `backend/` 目录内容提升到根目录
- 将 `frontend/` 目录内容提升到根目录
- 移除不必要的嵌套结构

#### 步骤3：更新配置文件
- 调整构建脚本和配置路径
- 更新 CI/CD 流水线
- 修改部署配置

## 3. 新的仓库结构设计

### 3.1 后端仓库结构 (wanli-backend)
```
wanli-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
├── railway.json
├── nixpacks.toml
├── Dockerfile
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── deploy.yml
├── .gitignore
└── README.md
```

### 3.2 前端仓库结构 (wanli-frontend)
```
wanli-frontend/
├── src/
│   ├── components/
│   ├── views/
│   ├── router/
│   ├── stores/
│   └── api/
├── public/
├── package.json
├── vite.config.ts
├── railway.json
├── nixpacks.toml
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── deploy.yml
├── .gitignore
└── README.md
```

## 4. 部署配置调整

### 4.1 后端部署配置

**railway.json**
```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "mvn clean package -DskipTests"
  },
  "deploy": {
    "startCommand": "java -jar target/*.jar",
    "numReplicas": 1,
    "sleepApplication": false,
    "restartPolicyType": "ON_FAILURE"
  }
}
```

**nixpacks.toml**
```toml
[phases.setup]
nixPkgs = ['maven', 'jdk17']

[phases.build]
cmds = ['mvn clean package -DskipTests']

[phases.start]
cmd = 'java -jar target/*.jar'
```

### 4.2 前端部署配置

**railway.json**
```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "npm ci && npm run build"
  },
  "deploy": {
    "startCommand": "npx vite preview --host 0.0.0.0 --port $PORT",
    "numReplicas": 1,
    "sleepApplication": false,
    "restartPolicyType": "ON_FAILURE"
  }
}
```

**nixpacks.toml**
```toml
[phases.setup]
nixPkgs = ['nodejs_20', 'npm']

[phases.build]
cmds = ['npm ci', 'npm run build']

[phases.start]
cmd = 'npx vite preview --host 0.0.0.0 --port $PORT'
```

## 5. CI/CD 流程更新

### 5.1 后端 CI/CD (.github/workflows/ci.yml)
```yaml
name: Backend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run tests
      run: mvn test
    - name: Build
      run: mvn clean package -DskipTests

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - name: Deploy to Railway
      run: echo "Deploy to Railway backend service"
```

### 5.2 前端 CI/CD (.github/workflows/ci.yml)
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '20'
        cache: 'npm'
    - name: Install dependencies
      run: npm ci
    - name: Run tests
      run: npm run test:unit
    - name: Build
      run: npm run build

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - name: Deploy to Railway
      run: echo "Deploy to Railway frontend service"
```

## 6. 环境变量和配置管理

### 6.1 后端环境变量
```bash
# Railway 后端服务环境变量
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://user:password@host:port/database
JWT_SECRET=your-jwt-secret
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
SERVER_PORT=8080
```

### 6.2 前端环境变量
```bash
# Railway 前端服务环境变量
VITE_API_BASE_URL=https://your-backend-domain.com/api
VITE_APP_TITLE=万里学院
NODE_ENV=production
PORT=4173
```

### 6.3 本地开发环境配置

**后端 application-dev.yml**
```yaml
server:
  port: 8080
  
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wanli_dev
    username: dev_user
    password: dev_password
  
cors:
  allowed-origins: http://localhost:5173
```

**前端 .env.development**
```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=万里学院 (开发环境)
```

## 7. 数据库连接配置

### 7.1 生产环境数据库配置
```yaml
# application-production.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 7.2 数据库迁移策略
- 使用 Flyway 或 Liquibase 进行数据库版本控制
- 确保数据库迁移脚本在独立部署中正确执行
- 配置数据库连接池和性能优化参数

## 8. 跨域和API通信配置

### 8.1 后端CORS配置
```java
@Configuration
@EnableWebSecurity
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://*.railway.app",
            "https://your-frontend-domain.com",
            "http://localhost:5173" // 开发环境
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 8.2 前端API客户端配置
```typescript
// src/api/client.ts
import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default apiClient
```

## 9. 迁移执行计划

### 9.1 准备阶段（1-2天）
1. 创建新的 GitHub 仓库
2. 设置 Railway 项目和服务
3. 配置环境变量和密钥

### 9.2 代码迁移阶段（2-3天）
1. 迁移后端代码到新仓库
2. 迁移前端代码到新仓库
3. 调整配置文件和构建脚本
4. 更新 CI/CD 流水线

### 9.3 测试阶段（2-3天）
1. 本地环境测试
2. 部署到测试环境
3. 集成测试和端到端测试
4. 性能测试

### 9.4 生产部署阶段（1天）
1. 部署后端服务
2. 部署前端服务
3. 数据库迁移（如需要）
4. DNS 和域名配置
5. 监控和日志配置

## 10. 风险评估和缓解措施

### 10.1 主要风险
- **数据丢失**：代码迁移过程中可能丢失历史记录
- **服务中断**：部署过程中可能导致服务不可用
- **配置错误**：环境变量和配置文件错误
- **依赖冲突**：新环境中的依赖版本问题

### 10.2 缓解措施
- 完整备份现有代码和数据
- 使用蓝绿部署策略减少停机时间
- 详细的配置检查清单
- 充分的测试和回滚计划

## 11. 后续优化建议

### 11.1 技术优化
- 实施微服务架构
- 添加 API 网关
- 实施容器化部署
- 添加服务监控和日志聚合

### 11.2 开发流程优化
- 实施 GitFlow 工作流
- 添加代码质量检查
- 实施自动化测试
- 建立发布管理流程

---

**注意**：本指南提供了完整的项目拆分方案，建议在实施前进行充分的测试和验证。如有疑问，请及时沟通和调整方案。