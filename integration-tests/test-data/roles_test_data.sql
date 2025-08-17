-- 万里书院集成测试角色和权限数据
-- 确保测试环境有完整的角色权限体系

-- 清理现有测试角色数据（保留系统角色）
-- 注意：不删除系统预定义角色，只确保它们存在

-- 插入或更新系统角色（使用ON CONFLICT处理重复）
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ROLE_ADMIN', '系统管理员角色，拥有所有权限', NOW(), NOW()),
(2, 'ROLE_HQ_TEACHER', '总部教师角色，可以创建和管理作业', NOW(), NOW()),
(3, 'ROLE_FRANCHISE_TEACHER', '加盟教师角色，可以查看和批改作业', NOW(), NOW()),
(4, 'ROLE_STUDENT', '学生角色，可以提交和查看作业', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    updated_at = NOW();

-- 重置序列（如果使用自增ID）
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));

-- 验证角色数据
SELECT 
    id,
    name,
    description,
    created_at
FROM roles
ORDER BY id;

-- 显示角色统计
SELECT 
    r.name as role_name,
    COUNT(ur.user_id) as user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
GROUP BY r.id, r.name
ORDER BY r.id;

-- 创建测试权限数据（如果有权限表）
-- 注意：根据实际数据库结构调整
/*
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    resource VARCHAR(100),
    action VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 插入基础权限
INSERT INTO permissions (name, description, resource, action) VALUES
('homework:create', '创建作业权限', 'homework', 'create'),
('homework:read', '查看作业权限', 'homework', 'read'),
('homework:update', '更新作业权限', 'homework', 'update'),
('homework:delete', '删除作业权限', 'homework', 'delete'),
('homework:grade', '批改作业权限', 'homework', 'grade'),
('user:manage', '用户管理权限', 'user', 'manage'),
('system:admin', '系统管理权限', 'system', 'admin')
ON CONFLICT (name) DO NOTHING;

-- 分配角色权限
-- 管理员：所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions
ON CONFLICT DO NOTHING;

-- 总部教师：作业相关权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('homework:create', 'homework:read', 'homework:update', 'homework:delete', 'homework:grade')
ON CONFLICT DO NOTHING;

-- 加盟教师：查看和批改权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name IN ('homework:read', 'homework:grade')
ON CONFLICT DO NOTHING;

-- 学生：查看权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions WHERE name = 'homework:read'
ON CONFLICT DO NOTHING;
*/

-- 显示完整的角色权限信息
SELECT 
    'Role-User Mapping' as info_type,
    r.name as role_name,
    u.username,
    u.full_name
FROM roles r
JOIN user_roles ur ON r.id = ur.role_id
JOIN users u ON ur.user_id = u.id
WHERE u.username LIKE 'test_%'
ORDER BY r.id, u.username;

CO