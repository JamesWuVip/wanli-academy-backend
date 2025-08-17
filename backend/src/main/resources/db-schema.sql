-- 万里书院数据库表结构定义
-- 用于Sprint 2的homeworks和questions核心表

-- 创建homeworks表
CREATE TABLE IF NOT EXISTS homeworks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_homeworks_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建questions表
CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    homework_id UUID NOT NULL,
    question_type VARCHAR(50),
    content JSONB,
    standard_answer JSONB,
    order_index INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_questions_homework FOREIGN KEY (homework_id) REFERENCES homeworks(id) ON DELETE CASCADE
);

-- 创建索引以优化查询性能

-- homeworks表索引
CREATE INDEX IF NOT EXISTS idx_homeworks_creator_id ON homeworks(creator_id);
CREATE INDEX IF NOT EXISTS idx_homeworks_title ON homeworks(title);
CREATE INDEX IF NOT EXISTS idx_homeworks_created_at ON homeworks(created_at);

-- questions表索引
CREATE INDEX IF NOT EXISTS idx_questions_homework_id ON questions(homework_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(question_type);
CREATE INDEX IF NOT EXISTS idx_questions_order ON questions(homework_id, order_index);

-- JSONB字段索引（用于内容搜索）
CREATE INDEX IF NOT EXISTS idx_questions_content_gin ON questions USING GIN(content);
CREATE INDEX IF NOT EXISTS idx_questions_answer_gin ON questions USING GIN(standard_answer);

-- 添加注释
COMMENT ON TABLE homeworks IS '作业表，存储作业基本信息';
COMMENT ON COLUMN homeworks.id IS '作业唯一标识符';
COMMENT ON COLUMN homeworks.title IS '作业标题';
COMMENT ON COLUMN homeworks.description IS '作业描述';
COMMENT ON COLUMN homeworks.creator_id IS '创建者用户ID（总部教师）';
COMMENT ON COLUMN homeworks.created_at IS '创建时间';
COMMENT ON COLUMN homeworks.updated_at IS '更新时间';

COMMENT ON TABLE questions IS '题目表，存储作业中的具体题目';
COMMENT ON COLUMN questions.id IS '题目唯一标识符';
COMMENT ON COLUMN questions.homework_id IS '所属作业ID';
COMMENT ON COLUMN questions.question_type IS '题目类型（如TEXT_ANSWER、SINGLE_CHOICE等）';
COMMENT ON COLUMN questions.content IS '题目内容（JSONB格式，支持富文本、图片等）';
COMMENT ON COLUMN questions.standard_answer IS '标准答案（JSONB格式，支持复杂答案结构）';
COMMENT ON COLUMN questions.order_index IS '题目在作业中的顺序';
COMMENT ON COLUMN questions.created_at IS '创建时间';
COMMENT ON COLUMN questions.updated_at IS '更新时间';