-- 万里书院集成测试提交数据
-- 包含不同状态的作业提交记录，特别是已批改的提交

-- 清理现有测试提交数据
DELETE FROM submissions WHERE student_id IN (
    SELECT id FROM users WHERE username LIKE 'test_%'
);

-- 插入测试作业提交数据（基于实际submissions表结构）
INSERT INTO submissions (id, assignment_id, student_id, content, file_path, submitted_at, status, score, feedback, graded_at, graded_by) VALUES
('770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440101', 16, '第一题答案：5+3=8\n第二题答案：10-4=6\n第三题答案：7×2=14', NULL, '2024-01-15 10:30:00', 'GRADED', 85, '计算正确，但需要注意书写格式。', '2024-01-15 14:20:00', (SELECT id FROM users WHERE username = 'test_hq_teacher')),
('770e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440102', 17, '第一题：小明有10个苹果，吃了3个，还剩7个\n第二题：一共有24个学生，分成3组，每组8人', NULL, '2024-01-16 09:15:00', 'GRADED', 92, '逻辑清晰，解题思路正确，继续保持！', '2024-01-16 16:45:00', (SELECT id FROM users WHERE username = 'test_hq_teacher')),
('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440103', 18, '综合测试答案：\n数学部分：90分\n语文部分：88分\n英语部分：85分', NULL, '2024-01-17 11:00:00', 'GRADED', 88, '综合能力表现良好，各科发展均衡。', '2024-01-17 15:30:00', (SELECT id FROM users WHERE username = 'test_hq_teacher')),
('770e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440104', 16, 'apple - 苹果\nbook - 书\ncat - 猫\ndog - 狗\nhouse - 房子', NULL, '2024-01-18 08:45:00', 'GRADED', 95, '单词拼写完全正确，记忆效果很好！', '2024-01-18 17:10:00', (SELECT id FROM users WHERE username = 'test_franchise_teacher')),
('770e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440105', 17, '阅读理解答案：\n1. 文章主要讲述了...\n2. 作者的观点是...\n3. 我的感想：这篇文章让我明白了...', NULL, '2024-01-19 13:20:00', 'GRADED', 90, '理解深入，表达清晰，阅读能力有明显提升。', '2024-01-19 18:00:00', (SELECT id FROM users WHERE username = 'test_hq_teacher'));

-- 验证插入结果
SELECT 
    u.username as student,
    a.title as assignment,
    s.status,
    s.score,
    s.submitted_at,
    s.graded_at
FROM submissions s
JOIN users u ON s.student_id = u.id
JOIN assignments a ON s.assignment_id = a.id
WHERE u.username LIKE 'test_%'
ORDER BY s.submitted_at DESC;

-- 显示统计信息
SELECT 
    s.status,
    COUNT(*) as count,
    AVG(s.score) as avg_score
FROM submissions s
JOIN users u ON s.student_id = u.id
WHERE u.username LIKE 'test_%'
GROUP BY s.status
ORDER BY s.status;

-- 显示已批改作业的详细信息
SELECT 
    u.username as student,
    a.title as assignment,
    s.score,
    s.feedback,
    grader.username as graded_by
FROM submissions s
JOIN users u ON s.student_id = u.id
JOIN assignments a ON s.assignment_id = a.id
LEFT JOIN users grader ON s.graded_by = grader.id
WHERE u.username LIKE 'test_%' AND s.status = 'GRADED'
ORDER BY s.score DESC;