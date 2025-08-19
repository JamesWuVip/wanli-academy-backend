# 万里学院后端系统

这是万里学院的后端API系统，基于Spring Boot开发。

## 功能特性

- 用户认证与授权（JWT）
- 作业管理系统
- 学生提交管理
- 角色权限控制
- RESTful API设计

## 技术栈

- **框架**: Spring Boot 2.7.18
- **数据库**: PostgreSQL
- **认证**: JWT (JSON Web Token)
- **文档**: Swagger/OpenAPI 3
- **部署**: Railway

## 快速开始

### 环境要求

- Java 11+
- Maven 3.6+
- PostgreSQL 12+

### 本地开发

1. 克隆项目
```bash
git clone https://github.com/JamesWuVip/wanli-academy-backend.git
cd wanli-academy-backend
```

2. 配置数据库
```bash
# 创建数据库
createdb wanli_academy

# 更新 application-dev.properties 中的数据库配置
```

3. 运行应用
```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

### API文档

启动应用后，访问以下地址查看API文档：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 部署

### Railway部署

本项目配置了自动部署到Railway平台：

1. **自动部署**: 推送到 `main` 分支会自动触发部署
2. **环境配置**: 生产环境使用Railway PostgreSQL数据库
3. **访问地址**: https://wanli-academy-backend-production.up.railway.app

### 环境变量

生产环境需要配置以下环境变量：
- `DATABASE_URL`: PostgreSQL连接字符串
- `JWT_SECRET`: JWT密钥
- `PORT`: 服务端口（Railway自动设置）

## 项目结构

```
src/
├── main/
│   ├── java/com/wanli/academy/backend/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── repository/     # 数据访问层
│   │   ├── service/        # 业务逻辑层
│   │   └── util/           # 工具类
│   └── resources/
│       ├── application.properties
│       └── application-dev.properties
└── test/                   # 测试代码
```

## API端点

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 作业管理
- `GET /api/assignments` - 获取作业列表
- `POST /api/assignments` - 创建作业
- `GET /api/assignments/{id}` - 获取作业详情
- `PUT /api/assignments/{id}` - 更新作业
- `DELETE /api/assignments/{id}` - 删除作业

### 提交管理
- `GET /api/submissions` - 获取提交列表
- `POST /api/submissions` - 创建提交
- `GET /api/submissions/{id}` - 获取提交详情

## 开发指南

### 代码规范

- 使用Java标准命名规范
- 控制器方法添加适当的注解
- 实体类使用JPA注解
- 服务层处理业务逻辑

### 数据库迁移

使用JPA自动建表功能，生产环境设置为 `update` 模式。

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库配置
   - 确认数据库服务运行状态

2. **JWT认证失败**
   - 检查JWT密钥配置
   - 确认token格式正确

3. **Railway部署失败**
   - 检查环境变量配置
   - 查看部署日志

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

本项目采用MIT许可证。

## 联系方式

如有问题，请联系项目维护者。