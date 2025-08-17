-- 万里书院集成测试作业数据
-- 包含不同类型和状态的测试作业数据

-- 清理现有测试作业数据
DELETE FROM questions WHERE homework_id IN (
    SELECT id FROM homeworks WHERE title LIKE 'Test_%'
);
DELETE FROM homeworks WHERE title LIKE 'Test_%';

-- 插入测试作业数据
INSERT INTO homeworks (id, title, description, creator_id, created_at, updated_at) VALUES
-- 数学作业
('550e8400-e29b-41d4-a716-446655440101', 'Test_数学基础练习', '测试用数学基础练习题，包含加减乘除运算', (SELECT id FROM users WHERE username = 'test_hq_teacher'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440102', 'Test_数学应用题', '测试用数学应用题，培养逻辑思维能力', (SELECT id FROM users WHERE username = 'test_hq_teacher'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 语文作业
('550e8400-e29b-41d4-a716-446655440103', 'Test_语文阅读理解', '测试用语文阅读理解练习', (SELECT id FROM users WHERE username = 'test_hq_teacher'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 英语作业
('550e8400-e29b-41d4-a716-446655440104', 'Test_英语单词练习', '测试用英语单词记忆和拼写练习', (SELECT id FROM users WHERE username = 'test_franchise_teacher'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 综合作业
('550e8400-e29b-41d4-a716-446655440105', 'Test_综合能力测试', '测试用综合能力评估作业', (SELECT id FROM users WHERE username = 'test_hq_teacher'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入作业题目数据
INSERT INTO questions (id, homework_id, question_type, content, standard_answer, order_index, created_at, updated_at) VALUES
-- 数学基础练习题目
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440101', 'SINGLE_CHOICE', '{"text": "计算：25 + 37 = ?", "options": ["A. 52", "B. 62", "C. 72", "D. 82"]}', '{"answer": "B", "explanation": "25 + 37 = 62，选择B"}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440101', 'SINGLE_CHOICE', '{"text": "计算：8 × 9 = ?", "options": ["A. 63", "B. 71", "C. 72", "D. 81"]}', '{"answer": "C", "explanation": "8 × 9 = 72，选择C"}', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440203', '550e8400-e29b-41d4-a716-446655440101', 'FILL_BLANK', '{"text": "计算：100 - 45 = ?"}', '{"answer": "55", "explanation": "100 - 45 = 55"}', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 数学应用题题目
('550e8400-e29b-41d4-a716-446655440204', '550e8400-e29b-41d4-a716-446655440102', 'SINGLE_CHOICE', '{"text": "小明有苹果24个，分给3个朋友，每人分得几个？", "options": ["A. 6个", "B. 8个", "C. 9个", "D. 12个"]}', '{"answer": "B", "explanation": "24 ÷ 3 = 8，每人分得8个苹果"}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440205', '550e8400-e29b-41d4-a716-446655440102', 'FILL_BLANK', '{"text": "一本书有120页，小红每天看15页，几天能看完？"}', '{"answer": "8", "explanation": "120 ÷ 15 = 8天"}', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 语文阅读理解题目
('550e8400-e29b-41d4-a716-446655440206', '550e8400-e29b-41d4-a716-446655440103', 'ESSAY', '{"text": "根据短文内容，主人公的性格特点是什么？"}', '{"answer": "勇敢、善良、乐于助人", "explanation": "需要结合文章内容分析人物性格特点"}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440207', '550e8400-e29b-41d4-a716-446655440103', 'MULTIPLE_CHOICE', '{"text": "文章的中心思想是什么？", "options": ["A. 友谊的珍贵", "B. 金钱的重要", "C. 互相帮助的意义", "D. 个人成功的价值"]}', '{"answer": ["A", "C"], "explanation": "文章强调友谊和互助的重要性"}', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 英语单词练习题目
('550e8400-e29b-41d4-a716-446655440208', '550e8400-e29b-41d4-a716-446655440104', 'FILL_BLANK', '{"text": "What is the English word for \"苹果\"?"}', '{"answer": "apple", "explanation": "apple是苹果的英文单词"}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('550e8400-e29b-41d4-a716-446655440209', '550e8400-e29b-41d4-a716-446655440104', 'SINGLE_CHOICE', '{"text": "Choose the correct spelling:", "options": ["A. beautiful", "B. beatiful", "C. beutiful", "D. beautyful"]}', '{"answer": "A", "explanation": "beautiful是正确的拼写"}', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 综合能力测试题目
('550e8400-e29b-41d4-a716-446655440210', '550e8400-e29b-41d4-a716-446655440105', 'ESSAY', '{"text": "请描述你对环境保护的看法，并提出三个具体的保护措施。"}', '{"answer": "需要表达个人观点并提出具体措施", "explanation": "考查学生的综合表达能力和环保意识"}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 验证插入结果
SELECT 
    h.title,
    h.description,
    COUNT(q.id) as question_count
FROM homeworks h
LEFT JOIN questions q ON h.id = q.homework_id
WHERE h.title LIKE 'Test_%'
GROUP BY h.id, h.title, h.description
ORDER BY h.title;

-- 显示题目类型统计
SELECT 
    q.question_type,
    COUNT(*) as question_count
FROM questions q
JOIN homeworks h ON q.homework_id = h.id
WHERE h.title LIKE 'Test_%'
GROUP BY q.question_type
ORDER BY question_count DESC;

-- 验证导入结果 - 显示统计信息
SELECT 'Test Users' as category, COUNT(*) as count FROM users WHERE username LIKE 'test_%'
UNION ALL
SELECT 'Test Homeworks' as category, COUNT(*) as count FROM homeworks WHERE title LIKE 'Test_%'
UNION ALL
SELECT 'Test Questions' as category, COUNT(*) as count FROM questions q JOIN homeworks h ON q.homework_id = h.id WHERE h.title LIKE 'Test_%'
UNION ALL
SELECT 'User Role Assignments' as category, COUNT(*) as count FROM user_roles ur JOIN users u ON ur.user_id = u.id WHERE u.username LIKE 'test_%';

-- 显示测试账户信息
SELECT 
    u.username,
    u.email,
    r.name as role_name
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username LIKE 'test_%'
ORDER BY r.name, u.username;