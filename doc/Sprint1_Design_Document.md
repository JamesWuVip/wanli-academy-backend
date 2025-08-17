# 万里书院项目 - Sprint 1 设计文档

**项目**: 万里书院在线作业系统 V1.0  
**Sprint**: 1 - 用户与数据基石  
**日期**: 2025年8月15日  
**作者**: 首席系统架构师  

## 1. 概述 (Overview)

本Sprint的核心目标是建立系统的核心数据模型，打通与数据库的连接，并实现最基础的用户身份认证功能。这是构建所有后续业务功能的基石。我们将采用Spring Boot作为后端框架，集成Spring Data JPA进行数据持久化，并利用Spring Security和JWT (JSON Web Tokens) 构建安全、无状态的认证体系。

## 2. 数据库设计 (Database Design)

我们将为wanli-staging应用在Fly.io上配置一个PostgreSQL数据库实例。初期，我们将设计两张核心表users和roles，以及一张用于关联它们的中间表user_roles。

### Table: users
存储所有系统用户的基本信息。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 用户唯一标识符 |
| username | VARCHAR(50) | Unique, Not Null | 用户名 |
| password | VARCHAR(255) | Not Null | 加密后的用户密码 |
| email | VARCHAR(100) | Unique, Not Null | 用户邮箱 |
| created_at | TIMESTAMPTZ | Not Null | 记录创建时间 |
| updated_at | TIMESTAMPTZ | Not Null | 记录更新时间 |

### Table: roles
存储系统中的角色信息。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | INTEGER | Primary Key, Not Null | 角色唯一标识符 |
| name | VARCHAR(20) | Unique, Not Null | 角色名称 (e.g., 'ROLE_STUDENT', 'ROLE_TEACHER') |

### Table: user_roles
用户与角色的多对多关联表。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| user_id | UUID | Foreign Key (users.id) | 用户ID |
| role_id | INTEGER | Foreign Key (roles.id) | 角色ID |

## 3. API 端点设计 (API Endpoint Design)

我们将定义一套RESTful API用于用户认证。

### 用户注册
**Endpoint**: POST /api/auth/register  
**描述**: 创建一个新用户账户。  
**请求体 (Request Body)**:
```json
{
  "username": "newstudent",
  "email": "student@example.com",
  "password": "securepassword123"
}
```
**成功响应 (Success Response)**: 201 Created

### 用户登录
**Endpoint**: POST /api/auth/login  
**描述**: 用户登录并获取JWT。  
**请求体 (Request Body)**:
```json
{
  "username": "newstudent",
  "password": "securepassword123"
}
```
**成功响应 (Success Response)**: 200 OK
```json
{
  "token": "ey...",
  "username": "newstudent"
}
```

### 获取当前用户信息 (需认证)
**Endpoint**: GET /api/users/me  
**描述**: 获取当前已登录用户的详细信息。  
**请求头 (Headers)**: Authorization: Bearer <token>  
**成功响应 (Success Response)**: 200 OK
```json
{
  "id": "...",
  "username": "newstudent",
  "email": "student@example.com",
  "roles": ["ROLE_STUDENT"]
}
```

## 4. 任务拆解 (Task Breakdown for AI Agent)

以下是为AI Agent准备的一系列明确、可执行的任务。

### 任务 1.1: 数据库配置与连接
- **指令**: 指导在Fly.io平台为应用wanli-staging创建一个PostgreSQL数据库。
- **指令**: 更新fly.toml文件，将数据库信息以环境变量的形式注入应用。
- **指令**: 修改pom.xml，添加spring-boot-starter-data-jpa和postgresql驱动依赖。
- **指令**: 在application.properties中配置Spring Data JPA，使其通过环境变量连接到数据库。

### 任务 1.2: 实体与数据访问层建模
- **指令**: 根据上文的数据库设计，创建User.java和Role.java两个JPA实体类，并正确配置它们之间的多对多关系。
- **指令**: 创建UserRepository.java和RoleRepository.java两个接口，继承JpaRepository。

### 任务 1.3: 安全框架与JWT集成
- **指令**: 在pom.xml中添加spring-boot-starter-security和JWT相关的库（如jjwt-api, jjwt-impl, jjwt-jackson）。
- **指令**: 创建一个SecurityConfig.java文件，配置HTTP安全规则：允许对/api/auth/**的公开访问，并保护所有其他API端点。
- **指令**: 创建一个JwtService.java，负责生成、解析和验证JWT。

### 任务 1.4: 认证API实现
- **指令**: 创建AuthService.java和AuthController.java，实现用户注册和登录的业务逻辑与API端点。
- **指令**: 实现UserDetailsService接口，用于从数据库加载用户信息。

### 任务 1.5: 用户信息API实现
- **指令**: 创建UserController.java，实现受保护的GET /api/users/me端点，用于返回当前登录用户的信息。

### 任务 1.6: 单元测试
- **指令**: 为AuthService和JwtService中的关键逻辑编写单元测试，确保其正确性与安全性。

## 5. 验收标准 (Acceptance Criteria)

本次Sprint的可交付成果是一套可以通过API工具（如Postman）进行测试的用户认证服务。验收时需满足：

- 所有代码已提交，CI/CD流水线成功运行并部署到wanli-staging。
- 可以通过POST /api/auth/register成功创建一个新用户。
- 可以使用新用户凭证通过POST /api/auth/login成功登录并获取有效的JWT。
- 使用获取到的JWT，可以成功访问GET /api/users/me并得到正确的用户信息。
- 不提供或提供无效JWT访问GET /api/users/me时，会返回401或403错误。
- 系统的"用户"和"安全"骨架已经成型。

我将根据此设计文档，开始为您准备【第17号指令】。