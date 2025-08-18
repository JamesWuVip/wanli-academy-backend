# 万里书院项目 - Sprint 4 设计与任务规划

**项目**: 万里书院在线作业系统 V1.0  
**Sprint**: 4 - 核心价值交付  
**日期**: 2025年8月17日  
**作者**: 首席系统架构师  

## 1. 概述 (Overview)

本Sprint的核心目标是交付项目的核心价值功能。我们将利用AI Agent在上一个Sprint中构建的作业提交与批改系统，为学员开发一个全新的"作业结果查看"界面。学员在作业被批改后，将能够清晰地看到自己的得分、教师的反馈、标准答案、文字解析，以及最重要的——与题目关联的教师讲解视频。

这将完成从"做作业 -> 提交 -> 获取反馈 -> 观看讲解"的完整学习闭环，是MVP版本最重要的功能交付。

## 2. 数据库设计 (Database Design)

为支持答案解析和视频讲解，我们需要对AI Agent创建的数据模型进行扩展。

### Table: questions (或AI Agent创建的等效表，如 assignment_items)
我们将在此表中新增两个关键字段。

| 列名 (Column) | 类型 (Type) | 约束 (Constraints) | 描述 (Description) |
|---------------|-------------|-------------------|--------------------|  
| ... | ... | ... | ... |
| explanation | TEXT | | (新增) 题目的官方文字解析 |
| video_url | VARCHAR(500) | | (新增) 教师讲解视频的URL |
| ... | ... | ... | ... |

## 3. API 端点设计 (API Endpoint Design)

我们将开发一个新的API端点，供学员查询已批改的作业结果。

### 获取作业提交结果详情
**Endpoint**: GET /api/submissions/{submissionId}  
**描述**: 获取单次作业提交的完整结果，包括得分、反馈、每道题的答案对比、解析和视频链接。  
**权限**: ROLE_STUDENT (仅限提交者本人) 或 ROLE_TEACHER  
**成功响应 (Success Response)**: 200 OK

```json
{
  "submissionId": "uuid-...",
  "homeworkTitle": "第一单元：古诗词鉴赏",
  "score": 85,
  "teacherFeedback": "完成得很好，但要注意书写规范。",
  "questions": [
    {
      "questionId": "uuid-q1",
      "content": { "stem": ""白日依山尽"的下一句是？" },
      "studentAnswer": { "text": "黄河入海流" },
      "standardAnswer": { "text": "黄河入海流" },
      "isCorrect": true,
      "explanation": "此句出自王之涣的《登鹳雀楼》...",
      "videoUrl": "https://videos.wanli.com/gu-shi-01.mp4"
    }
    // ... more questions
  ]
}
```

## 4. 任务拆解 (Task Breakdown for AI Agent)

### 任务 4.1: 后端 - 数据模型扩展
**指令 (AI Agent)**: 修改Question（或等效的）JPA实体类，新增explanation和videoUrl两个字段，并生成相应的数据库迁移脚本。

### 任务 4.2: 后端 - 结果查询API开发
**步骤 1 (架构师)**: 我将提供SubmissionServiceTest.java的单元测试和集成测试骨架，用于测试获取作业结果的复杂查询逻辑。

**步骤 2 (AI Agent)**:
- **指令**: 创建SubmissionResultDTO.java等用于API响应的数据传输对象。
- **指令**: 在SubmissionService.java中实现getSubmissionResult方法，该方法需要关联查询Submission, Assignment, Question等多张表的数据。
- **指令**: 填充所有测试骨架，确保查询逻辑的正确性。
- **指令**: 在SubmissionController.java中实现GET /api/submissions/{submissionId}端点，并配置相应的权限。

### 任务 4.3: 前端 - "作业结果"页面开发
- **指令 (AI Agent)**: 在前端项目中创建一个新的页面组件 SubmissionResult.vue。
- **指令 (AI Agent)**: 在学员的"我的作业"列表中，为已批改的作业添加一个"查看结果"的链接，点击后路由到SubmissionResult页面。
- **指令 (AI Agent)**: 在SubmissionResult.vue页面中，调用GET /api/submissions/{submissionId}接口获取数据。
- **指令 (AI Agent)**: 设计并实现一个清晰的UI，用于展示总分、教师评语，并逐题展示题目内容、学生答案、标准答案、文字解析。
- **指令 (AI Agent)**: 在每道题的解析部分，嵌入一个视频播放器组件，用于播放在videoUrl字段中指定的视频。

### 任务 4.4: 验收测试
**指令 (架构师)**: 我将编写并提交sprint4_acceptance_test.js脚本，该脚本将完整地测试学员查看已批改作业结果的全流程。

## 5. 验收标准 (Acceptance Criteria)

- 所有后端的单元测试和集成测试骨架都已被成功填充并通过。
- sprint4_acceptance_test.js自动化验收测试脚本在CI流水线中成功通过。
- 一个真实用户（您）可以：
  - 以学员身份登录。
  - 在作业列表中找到一个已批改的作业，并点击"查看结果"。
  - 在结果页面上，能清楚地看到自己的总分和教师评语。
  - 能够逐题查看自己的答案与标准答案的对比。
  - 能够在该页面上直接点击并播放教师为题目讲解的视频。