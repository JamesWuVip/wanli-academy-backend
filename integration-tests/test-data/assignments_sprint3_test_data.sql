-- 万里书院集成测试作业数据（Sprint 3 - assignments表）
-- 为submissions表提供关联的assignments数据

-- 清理现有测试assignments数据
DELETE FROM assignment_files WHERE assignment_id IN (
    SELECT id FROM assignments WHERE title LIKE 'Test_%'
);
DELETE FROM submissions WHERE assignment_id IN (
    SELECT id FROM assignments WHERE title LIKE 'Test_%'
);
DELETE FROM assignments WHERE title LIKE 'Test_%';

-- 插入测试assignments数据
INSERT INTO assignments (id, title, description, creator_id, max_score, due_date, status, created_at, updated_at) VALUES
-- 数学作业
('660e8400-e29b-41d4-a716-446655440101', 'Test_数学基础练习作业', '测试用数学基础练习作业，包含加减乘除运算。请认真完成所有题目，注意计算准确性', (SELECT id FROM users WHERE username = 'test_hq_teacher'), 100, CURRENT_TIMESTAMP + INTERVAL '7 days', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),

('660e8400-e29b-41d4-a716-446655440102', 'Test_数学应用题作业', '测试用数学应用题作业，培养逻辑思维能力。请仔细阅读题目，理解题意后再作答', (SELECT id FROM users WHERE username = 'test_hq_teacher'), 100, CURRENT_TIMESTAMP + INTERVAL '5 days', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),

-- 语文作业
('660e8400-e29b-41d4-a716-446655440103', 'Test_语文阅读理解作业', '测试用语文阅读理解练习作业。请认真阅读文章，理解文章内容后回答问题', (SELECT id FROM users WHERE username = 'test_hq_teacher'), 100, CURRENT_TIMESTAMP + INTERVAL '6 days', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),

-- 英语作业
('660e8400-e29b-41d4-a716-446655440104', 'Test_英语单词练习作业', '测试用英语单词记忆和拼写练习作业。请认真记忆单词拼写和含义', (SELECT id FROM users WHERE username = 'test_franchise_teacher'), 100, CURRENT_TIMESTAMP + INTERVAL '4 days', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),

-- 综合作业
('660e8400-e29b-41d4-a716-446655440105', 'Test_综合能力测试作业', '测试用综合能力评估作业。请综合运用所学知识完成测试', (SELECT id FROM users WHERE username = 'test_hq_teacher'), 100, CURRENT_TIMESTAMP + INTERVAL '10 days', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days');

-- 验证插入结果
SELECT 
    a.title,
    a.description,
    a.status,
    a.max_score,
    u.username as creator
FROM assignments a
JOIN users u ON a.creator_id = u.id
WHERE a.title LIKE 'Test_%'
ORDER BY a.title;

-- 显示assignments统计信息
SELECT 'Test Assignments' as category, COUNT(*) as count FROM assignments WHERE title LIKE 'Test_%';