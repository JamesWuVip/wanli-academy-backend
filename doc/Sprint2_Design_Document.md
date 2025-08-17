# 万里书院项目 - Sprint 2 最终设计与任务规划

**项目**: 万里书院在线作业系统 V1.0  
**Sprint**: 2 - 内容管理核心 (已集成测试规范)  
**日期**: 2025年8月16日  
**作者**: 首席系统架构师  

## 1. 概述 (Overview)

本Sprint的核心目标是：使具备"总部教师" (ROLE_HQ_TEACHER) 角色的用户能够创建、管理和发布作业。

为确保代码质量和数据库交互的正确性，本次Sprint将全面实施分层的自动化测试策略。所有功能的开发都将遵循"测试先行"的原则，即由我首先提供单元测试和集成测试的骨架作为"契约"，再由AI Agent编写功能代码并填充测试细节以使所有测试通过。

### 1.1. 工程规范 (Engineering Specification)

本Sprint及所有后续开发，必须严格遵守新制定的**《万里书院项目 - 统一工程规范》**。该规范定义了代码风格、API设计和Git提交信息的统一标准，是保障项目高质量、高效率协作的基础。AI Agent在执行所有任务时，都必须以该规范为准则。

### 1.2. 架构性增强 (Architectural Enhancements)

为提升代码的长期可维护性和健壮性，本Sprint将明确引入以下两个架构最佳实践：

- **数据传输对象 (DTOs)**: 所有API的输入和输出都必须通过专门的DTO类进行，以实现API契约与内部数据模型的解耦。
- **全局异常处理**: 将建立一个全局异常处理器（@ControllerAdvice），以统一处理业务异常并返回标准化的JSON错误响应。

## 2. 数据库设计 (Database Design)

数据库将新增homeworks和questions两张表。

### Table: homeworks

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 作业唯一标识符 |
| title | VARCHAR(255) | Not Null | 作业标题 |
| description | TEXT | | 作业描述 |
| creator_id | UUID | Foreign Key (users.id), Not Null | 创建该作业的总部教师ID |
| created_at | TIMESTAMPTZ | Not Null | |
| updated_at | TIMESTAMPTZ | Not Null | |

### Table: questions

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|
| id | UUID | Primary Key, Not Null | 题目唯一标识符 |
| homework_id | UUID | Foreign Key (homeworks.id), Not Null | 所属作业ID |
| question_type | VARCHAR(50) | Not Null | 题型 (e.g., 'TEXT_ANSWER') |
| content | JSONB | Not Null | 题目内容 (JSON格式) |
| standard_answer | JSONB | | 标准答案 (JSON格式) |
| order_index | INTEGER | Not Null | 题目在作业中的顺序 |
| created_at | TIMESTAMPTZ | Not Null | |
| updated_at | TIMESTAMPTZ | Not Null | |

## 3. API 端点设计 (API Endpoint Design)

所有端点均需认证，且调用者必须拥有ROLE_HQ_TEACHER角色。

- **创建新作业**: POST /api/homeworks
- **获取总部教师的作业列表**: GET /api/homeworks
- **向作业中添加题目**: POST /api/homeworks/{homeworkId}/questions

## 4. 统一测试框架规范 (Sprint 2)

本次Sprint将严格执行三层测试策略：

- **单元测试 (Unit Tests)**
- **集成测试 (Integration Tests)**
- **验收测试 (Acceptance Tests)**

## 5. 任务拆解 (Task Breakdown for AI Agent)

Sprint 2的开发将严格按以下顺序进行：

### 任务 2.1: 角色模型扩展
**指令 (AI Agent)**: 修改Role实体和数据库初始化脚本，新增ROLE_HQ_TEACHER和ROLE_FRANCHISE_TEACHER两个角色。

### 任务 2.2 (新增): 建立API基础框架
**指令 (AI Agent)**: 创建用于API数据传输的DTO包，并定义HomeworkCreateRequest.java等基础DTO类。  
**指令 (AI Agent)**: 创建全局异常处理器GlobalExceptionHandler.java，并定义一个基础的ResourceNotFoundException异常类。

### 任务 2.3: 开发作业数据访问层 (Repository)
**步骤 1 (架构师)**: 提供HomeworkRepositoryIntegrationTest.java的集成测试契约。  
**步骤 2 (AI Agent)**: 实现Homework.java实体、HomeworkRepository.java接口，并填充集成测试。

### 任务 2.4: 开发作业业务逻辑层 (Service)
**步骤 1 (架构师)**: 提供HomeworkServiceTest.java的单元测试契约。  
**步骤 2 (AI Agent)**: 实现HomeworkService.java的业务逻辑，并填充单元测试。

### 任务 2.5: 开发API接口层 (Controller)
**指令 (AI Agent)**: 创建HomeworkController.java，实现API端点，并使用Spring Security进行保护。

### 任务 2.6: 编写Sprint 2验收测试
**指令 (架构师)**: 我将编写并提交sprint2_acceptance_test.js脚本。

## 6. 验收标准 (Acceptance Criteria)

- 所有由我提供的单元测试和集成测试骨架都已被AI Agent成功填充，并通过CI流水线中的测试。
- sprint2_acceptance_test.js自动化验收测试脚本在CI流水线中成功通过。
- 部署后，可以通过应用的/swagger-ui.html路径，查看到所有新建的API并能进行交互测试。