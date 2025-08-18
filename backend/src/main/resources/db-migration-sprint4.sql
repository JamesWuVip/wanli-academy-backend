-- 万里书院数据库表结构定义
-- Sprint 4: 题目解析和视频讲解功能数据库迁移脚本
-- 为questions表添加explanation和video_url字段

-- 为questions表添加新字段
ALTER TABLE questions 
ADD COLUMN IF NOT EXISTS explanation TEXT,
ADD COLUMN IF NOT EXISTS video_url VARCHAR(500);

-- 添加字段注释
COMMENT ON COLUMN questions.explanation IS '题目解析，提供详细的解题思路和方法';
COMMENT ON COLUMN questions.video_url IS '视频讲解链接，提供视频形式的题目讲解';

-- 创建索引以优化查询性能（可选）
CREATE INDEX IF NOT EXISTS idx_questions_has_explanation ON questions(explanation) WHERE explanation IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_questions_has_video ON questions(video_url) WHERE video_url IS NOT NULL;