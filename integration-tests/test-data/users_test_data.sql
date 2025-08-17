-- 万里书院集成测试用户数据
-- 包含不同角色的测试用户数据

-- 清理现有测试数据
DELETE FROM user_roles WHERE user_id IN (
    SELECT id FROM users WHERE username LIKE 'test_%'
);
DELETE FROM users WHERE username LIKE 'test_%';

-- 插入测试用户
INSERT INTO users (username, password, email, display_name, is_active) VALUES
-- 管理员用户
('test_admin', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'admin@wanli.academy', '管理员', true),

-- 总部教师用户
('test_hq_teacher', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'hq.teacher@wanli.academy', '总部教师', true),

-- 加盟教师用户
('test_franchise_teacher', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'franchise.teacher@wanli.academy', '加盟教师', true),

-- 学生用户
('test_student1', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'student1@wanli.academy', '学生1', true),
('test_student2', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'student2@wanli.academy', '学生2', true),
('test_student3', '$2b$10$aFPC5njXck.YuK8OafL6qOGQLgazzXwo011.jkf894HNqwT8fco/i', 'student3@wanli.academy', '学生3', true);

-- 插入用户角色关联
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_admin' AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_hq_teacher' AND r.name = 'ROLE_HQ_TEACHER';
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_franchise_teacher' AND r.name = 'ROLE_FRANCHISE_TEACHER';
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_student1' AND r.name = 'ROLE_STUDENT';
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_student2' AND r.name = 'ROLE_STUDENT';
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'test_student3' AND r.name = 'ROLE_STUDENT';

-- 验证插入结果
SELECT 
    u.username,
    u.email,
    r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username LIKE 'test_%'
ORDER BY u.username;

-- 显示测试用户统计
SELECT 
    r.name as role_name,
    COUNT(*) as user_count
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username LIKE 'test_%'
GROUP BY r.name
ORDER BY r.name;