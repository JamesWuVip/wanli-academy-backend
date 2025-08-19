-- 万里书院数据库表结构定义
-- Sprint 3: 作业管理模块数据库迁移脚本
-- 创建assignments、submissions、assignment_files表

-- 创建assignments表（作业表）
CREATE TABLE IF NOT EXISTS assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    instructions TEXT,
    creator_id BIGINT NOT NULL,
    max_score DECIMAL(5,2) DEFAULT 100.00,
    due_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    allow_late_submission BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_assignments_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建submissions表（作业提交表）
CREATE TABLE IF NOT EXISTS submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    content TEXT,
    file_path VARCHAR(500),
    score DECIMAL(5,2),
    feedback TEXT,
    status VARCHAR(20) DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED', 'GRADED', 'RETURNED')),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    graded_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 唯一约束：每个学生对每个作业只能提交一次
    CONSTRAINT uk_submissions_assignment_student UNIQUE (assignment_id, student_id)
);

-- 创建assignment_files表（作业文件表）
CREATE TABLE IF NOT EXISTS assignment_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    file_category VARCHAR(50) DEFAULT 'ATTACHMENT' CHECK (file_category IN ('ATTACHMENT', 'TEMPLATE', 'REFERENCE')),
    uploader_id BIGINT NOT NULL,
    uploader_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_assignment_files_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_files_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建索引以优化查询性能

-- assignments表索引
CREATE INDEX IF NOT EXISTS idx_assignments_creator_id ON assignments(creator_id);
CREATE INDEX IF NOT EXISTS idx_assignments_title ON assignments(title);
CREATE INDEX IF NOT EXISTS idx_assignments_status ON assignments(status);
CREATE INDEX IF NOT EXISTS idx_assignments_due_date ON assignments(due_date);
CREATE INDEX IF NOT EXISTS idx_assignments_created_at ON assignments(created_at);

-- submissions表索引
CREATE INDEX IF NOT EXISTS idx_submissions_assignment_id ON submissions(assignment_id);
CREATE INDEX IF NOT EXISTS idx_submissions_student_id ON submissions(student_id);
CREATE INDEX IF NOT EXISTS idx_submissions_status ON submissions(status);
CREATE INDEX IF NOT EXISTS idx_submissions_submitted_at ON submissions(submitted_at);
CREATE INDEX IF NOT EXISTS idx_submissions_graded_at ON submissions(graded_at);

-- assignment_files表索引
CREATE INDEX IF NOT EXISTS idx_assignment_files_assignment_id ON assignment_files(assignment_id);
CREATE INDEX IF NOT EXISTS idx_assignment_files_uploader_id ON assignment_files(uploader_id);
CREATE INDEX IF NOT EXISTS idx_assignment_files_file_category ON assignment_files(file_category);
CREATE INDEX IF NOT EXISTS idx_assignment_files_file_type ON assignment_files(file_type);

-- 添加表和字段注释

-- assignments表注释
COMMENT ON TABLE assignments IS '作业表，存储作业基本信息和配置';
COMMENT ON COLUMN assignments.id IS '作业唯一标识符';
COMMENT ON COLUMN assignments.title IS '作业标题';
COMMENT ON COLUMN assignments.description IS '作业描述';
COMMENT ON COLUMN assignments.instructions IS '作业说明和要求';
COMMENT ON COLUMN assignments.creator_id IS '创建者用户ID（总部教师）';
COMMENT ON COLUMN assignments.max_score IS '作业满分';
COMMENT ON COLUMN assignments.due_date IS '截止时间';
COMMENT ON COLUMN assignments.status IS '作业状态：DRAFT-草稿，PUBLISHED-已发布，CLOSED-已关闭';
COMMENT ON COLUMN assignments.allow_late_submission IS '是否允许迟交';
COMMENT ON COLUMN assignments.created_at IS '创建时间';
COMMENT ON COLUMN assignments.updated_at IS '更新时间';

-- submissions表注释
COMMENT ON TABLE submissions IS '作业提交表，存储学生提交的作业信息';
COMMENT ON COLUMN submissions.id IS '提交记录唯一标识符';
COMMENT ON COLUMN submissions.assignment_id IS '所属作业ID';
COMMENT ON COLUMN submissions.student_id IS '提交学生用户ID';
COMMENT ON COLUMN submissions.student_name IS '提交学生姓名';
COMMENT ON COLUMN submissions.content IS '提交内容';
COMMENT ON COLUMN submissions.file_path IS '提交文件路径';
COMMENT ON COLUMN submissions.score IS '得分';
COMMENT ON COLUMN submissions.feedback IS '教师反馈';
COMMENT ON COLUMN submissions.status IS '提交状态：SUBMITTED-已提交，GRADED-已批改，RETURNED-已返回';
COMMENT ON COLUMN submissions.submitted_at IS '提交时间';
COMMENT ON COLUMN submissions.graded_at IS '批改时间';
COMMENT ON COLUMN submissions.created_at IS '创建时间';
COMMENT ON COLUMN submissions.updated_at IS '更新时间';

-- assignment_files表注释
COMMENT ON TABLE assignment_files IS '作业文件表，存储作业相关的文件信息';
COMMENT ON COLUMN assignment_files.id IS '文件记录唯一标识符';
COMMENT ON COLUMN assignment_files.assignment_id IS '所属作业ID';
COMMENT ON COLUMN assignment_files.file_name IS '文件名';
COMMENT ON COLUMN assignment_files.file_path IS '文件存储路径';
COMMENT ON COLUMN assignment_files.file_size IS '文件大小（字节）';
COMMENT ON COLUMN assignment_files.file_type IS '文件类型/MIME类型';
COMMENT ON COLUMN assignment_files.file_category IS '文件分类：ATTACHMENT-附件，TEMPLATE-模板，REFERENCE-参考资料';
COMMENT ON COLUMN assignment_files.uploader_id IS '上传者用户ID';
COMMENT ON COLUMN assignment_files.uploader_name IS '上传者姓名';
COMMENT ON COLUMN assignment_files.created_at IS '创建时间';
COMMENT ON COLUMN assignment_files.updated_at IS '更新时间';