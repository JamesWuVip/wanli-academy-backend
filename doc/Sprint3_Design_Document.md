# 万里书院项目 - Sprint 3 设计文档

**项目**: 万里书院在线作业系统 V1.0  
**Sprint**: 3 - 作业管理与提交系统  
**日期**: 2025年1月17日  
**作者**: 首席系统架构师  
**参考文档**: [Sprint 3 设计与任务规划](https://docs.google.com/document/d/1h_2snHXJIXD0yZHRqvw8rf4yZA-VnyTGUI29Y6yNBAg/edit?usp=sharing)

## 1. 概述 (Overview)

本Sprint的核心目标是在已有的用户认证和基础架构之上，构建完整的作业管理与提交系统。这包括作业的创建、分发、学生提交、教师批改以及成绩管理等核心业务功能。我们将继续基于Spring Boot框架，扩展现有的数据模型，并实现更复杂的业务逻辑和权限控制。

### 主要功能模块
- 作业创建与管理（教师端）
- 作业分发与查看（学生端）
- 作业提交系统
- 批改与评分系统
- 成绩统计与分析
- 文件上传与管理

## 2. 数据库设计 (Database Design)

基于Sprint 1和Sprint 2的基础数据结构，我们将新增以下核心表来支持作业管理功能。

### Table: assignments
存储作业基本信息。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 作业唯一标识符 |
| title | VARCHAR(200) | Not Null | 作业标题 |
| description | TEXT | | 作业描述 |
| creator_id | UUID | Foreign Key (users.id), Not Null | 创建者（教师）ID |
| due_date | TIMESTAMPTZ | Not Null | 截止时间 |
| max_score | INTEGER | Not Null, Default 100 | 满分分值 |
| status | VARCHAR(20) | Not Null, Default 'DRAFT' | 作业状态 |
| created_at | TIMESTAMPTZ | Not Null | 创建时间 |
| updated_at | TIMESTAMPTZ | Not Null | 更新时间 |

### Table: submissions
存储学生作业提交记录。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 提交记录唯一标识符 |
| assignment_id | UUID | Foreign Key (assignments.id), Not Null | 作业ID |
| student_id | UUID | Foreign Key (users.id), Not Null | 学生ID |
| content | TEXT | | 提交内容 |
| file_path | VARCHAR(500) | | 附件文件路径 |
| score | INTEGER | | 得分 |
| feedback | TEXT | | 教师反馈 |
| status | VARCHAR(20) | Not Null, Default 'SUBMITTED' | 提交状态 |
| submitted_at | TIMESTAMPTZ | Not Null | 提交时间 |
| graded_at | TIMESTAMPTZ | | 批改时间 |

### Table: assignment_files
存储作业相关文件信息。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 文件唯一标识符 |
| assignment_id | UUID | Foreign Key (assignments.id), Not Null | 作业ID |
| file_name | VARCHAR(255) | Not Null | 文件名 |
| file_path | VARCHAR(500) | Not Null | 文件存储路径 |
| file_size | BIGINT | Not Null | 文件大小（字节） |
| file_type | VARCHAR(50) | Not Null | 文件类型 |
| uploaded_by | UUID | Foreign Key (users.id), Not Null | 上传者ID |
| uploaded_at | TIMESTAMPTZ | Not Null | 上传时间 |

## 3. API 端点设计 (API Endpoint Design)

基于RESTful设计原则，我们将实现以下API端点来支持作业管理功能。

### 作业管理 API

#### 创建作业
**Endpoint**: POST /api/assignments  
**描述**: 教师创建新作业  
**权限**: ROLE_TEACHER  
**请求体 (Request Body)**:
```json
{
  "title": "Spring Boot 实战项目",
  "description": "使用Spring Boot开发一个简单的Web应用",
  "dueDate": "2025-02-15T23:59:59Z",
  "maxScore": 100
}
```
**成功响应 (Success Response)**: 201 Created

#### 获取作业列表
**Endpoint**: GET /api/assignments  
**描述**: 获取作业列表（教师看到所有，学生看到分配给自己的）  
**权限**: 已认证用户  
**查询参数**: ?page=0&size=10&status=PUBLISHED  
**成功响应 (Success Response)**: 200 OK

#### 获取作业详情
**Endpoint**: GET /api/assignments/{id}  
**描述**: 获取指定作业的详细信息  
**权限**: 已认证用户  
**成功响应 (Success Response)**: 200 OK

#### 更新作业
**Endpoint**: PUT /api/assignments/{id}  
**描述**: 更新作业信息  
**权限**: ROLE_TEACHER（仅创建者）  
**成功响应 (Success Response)**: 200 OK

#### 发布作业
**Endpoint**: POST /api/assignments/{id}/publish  
**描述**: 发布作业给学生  
**权限**: ROLE_TEACHER（仅创建者）  
**成功响应 (Success Response)**: 200 OK

### 作业提交 API

#### 提交作业
**Endpoint**: POST /api/assignments/{id}/submissions  
**描述**: 学生提交作业  
**权限**: ROLE_STUDENT  
**请求体 (Request Body)**:
```json
{
  "content": "这是我的作业内容...",
  "files": ["file1.pdf", "file2.docx"]
}
```
**成功响应 (Success Response)**: 201 Created

#### 获取提交记录
**Endpoint**: GET /api/assignments/{id}/submissions  
**描述**: 获取作业的提交记录（教师看到所有，学生只看到自己的）  
**权限**: 已认证用户  
**成功响应 (Success Response)**: 200 OK

#### 批改作业
**Endpoint**: PUT /api/submissions/{id}/grade  
**描述**: 教师批改学生作业  
**权限**: ROLE_TEACHER  
**请求体 (Request Body)**:
```json
{
  "score": 85,
  "feedback": "作业完成得很好，但还有改进空间..."
}
```
**成功响应 (Success Response)**: 200 OK

### 文件管理 API

#### 上传文件
**Endpoint**: POST /api/files/upload  
**描述**: 上传作业相关文件  
**权限**: 已认证用户  
**请求类型**: multipart/form-data  
**成功响应 (Success Response)**: 201 Created

#### 下载文件
**Endpoint**: GET /api/files/{id}/download  
**描述**: 下载文件  
**权限**: 已认证用户（权限检查）  
**成功响应 (Success Response)**: 200 OK

## 4. 任务拆解 (Task Breakdown for AI Agent)

以下是为AI Agent准备的详细任务列表，按优先级和依赖关系排序。

### 任务 3.1: 数据模型扩展
- **指令**: 基于数据库设计，创建Assignment.java、Submission.java、AssignmentFile.java实体类
- **指令**: 配置实体间的关联关系（一对多、多对一）
- **指令**: 创建对应的Repository接口：AssignmentRepository、SubmissionRepository、AssignmentFileRepository
- **指令**: 编写数据库迁移脚本，创建新表结构

### 任务 3.2: 作业管理服务层
- **指令**: 创建AssignmentService.java，实现作业的CRUD操作
- **指令**: 实现作业状态管理（草稿、已发布、已截止等）
- **指令**: 添加权限验证逻辑，确保只有创建者可以修改作业
- **指令**: 实现作业列表的分页和筛选功能

### 任务 3.3: 作业提交服务层
- **指令**: 创建SubmissionService.java，处理学生作业提交逻辑
- **指令**: 实现提交状态管理和重复提交检查
- **指令**: 添加截止时间验证，防止逾期提交
- **指令**: 实现批改功能，包括评分和反馈

### 任务 3.4: 文件管理服务
- **指令**: 创建FileService.java，处理文件上传、存储和下载
- **指令**: 实现文件类型和大小限制
- **指令**: 配置文件存储路径和访问权限
- **指令**: 添加文件安全检查，防止恶意文件上传

### 任务 3.5: 控制器层实现
- **指令**: 创建AssignmentController.java，实现作业管理相关API端点
- **指令**: 创建SubmissionController.java，实现作业提交相关API端点
- **指令**: 创建FileController.java，实现文件管理API端点
- **指令**: 添加统一的异常处理和参数验证

### 任务 3.6: 权限控制增强
- **指令**: 扩展SecurityConfig.java，添加新的权限规则
- **指令**: 实现基于角色和资源的访问控制
- **指令**: 添加方法级别的安全注解
- **指令**: 实现自定义权限验证逻辑

### 任务 3.7: 单元测试和集成测试
- **指令**: 为新增的Service类编写单元测试
- **指令**: 为Controller层编写集成测试
- **指令**: 测试文件上传和下载功能
- **指令**: 验证权限控制的正确性

### 任务 3.8: API文档更新
- **指令**: 更新Swagger配置，添加新的API文档
- **指令**: 为所有新增端点添加详细的API说明
- **指令**: 提供API使用示例和错误码说明

## 5. 验收标准 (Acceptance Criteria)

本次Sprint的验收标准如下，所有功能必须通过测试并在生产环境中正常运行。

### 功能验收标准
- **作业管理**：教师可以创建、编辑、发布和管理作业
- **作业提交**：学生可以查看作业要求并提交作业内容
- **文件处理**：支持文件上传、下载和安全访问控制
- **批改系统**：教师可以对学生提交的作业进行评分和反馈
- **权限控制**：严格的角色权限验证，防止越权访问

### 技术验收标准
- 所有新增API端点通过Postman或Swagger UI测试
- 单元测试覆盖率达到80%以上
- 集成测试覆盖所有主要业务流程
- 代码通过SonarQube质量检查
- 性能测试：支持并发100用户同时操作

### 部署验收标准
- CI/CD流水线成功构建和部署
- 生产环境功能正常，无重大bug
- 数据库迁移成功，数据完整性保持
- 监控和日志系统正常工作

### 用户体验验收标准
- API响应时间在2秒以内
- 文件上传支持常见格式（PDF、DOC、TXT等）
- 错误信息清晰明确，便于用户理解
- 支持大文件上传（最大50MB）

通过本Sprint的实施，万里书院在线作业系统将具备完整的作业管理和提交功能，为师生提供便捷的在线教学工具。