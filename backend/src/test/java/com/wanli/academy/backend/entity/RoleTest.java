package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Role 实体测试类
 */
class RoleTest {

    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        role = new Role();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>());
    }

    @Test
    void should_create_role_with_default_constructor() {
        Role newRole = new Role();
        
        assertNull(newRole.getId());
        assertNull(newRole.getName());
        assertNull(newRole.getDescription());
        assertTrue(newRole.getIsActive()); // 默认为true
        assertNull(newRole.getCreatedAt());
        assertNull(newRole.getUpdatedAt());
        assertNotNull(newRole.getUsers());
        assertTrue(newRole.getUsers().isEmpty());
    }

    @Test
    void should_create_role_with_name() {
        String roleName = "USER";
        
        Role newRole = new Role(roleName);
        
        assertEquals(roleName, newRole.getName());
        assertTrue(newRole.getIsActive()); // 默认为true
        assertNotNull(newRole.getUsers());
        assertTrue(newRole.getUsers().isEmpty());
    }

    @Test
    void should_create_role_with_name_and_description() {
        String roleName = "ADMIN";
        String description = "Administrator role";
        
        Role newRole = new Role(roleName, description);
        
        assertEquals(roleName, newRole.getName());
        assertEquals(description, newRole.getDescription());
        assertTrue(newRole.getIsActive()); // 默认为true
        assertNotNull(newRole.getUsers());
        assertTrue(newRole.getUsers().isEmpty());
    }

    @Test
    void should_set_and_get_all_properties() {
        Long id = 1L;
        String name = "USER";
        String description = "User role";
        Boolean isActive = false;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Set<User> users = new HashSet<>();
        users.add(user);
        
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        role.setIsActive(isActive);
        role.setCreatedAt(createdAt);
        role.setUpdatedAt(updatedAt);
        role.setUsers(users);
        
        assertEquals(id, role.getId());
        assertEquals(name, role.getName());
        assertEquals(description, role.getDescription());
        assertEquals(isActive, role.getIsActive());
        assertEquals(createdAt, role.getCreatedAt());
        assertEquals(updatedAt, role.getUpdatedAt());
        assertEquals(users, role.getUsers());
    }

    @Test
    void should_add_user_successfully() {
        role.addUser(user);
        
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRoles().contains(role));
        assertEquals(1, role.getUsers().size());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void should_remove_user_successfully() {
        // 先添加用户
        role.addUser(user);
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRoles().contains(role));
        
        // 然后移除用户
        role.removeUser(user);
        
        assertFalse(role.getUsers().contains(user));
        assertFalse(user.getRoles().contains(role));
        assertEquals(0, role.getUsers().size());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    void should_handle_multiple_users() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setRoles(new HashSet<>());
        
        role.addUser(user);
        role.addUser(user2);
        
        assertEquals(2, role.getUsers().size());
        assertTrue(role.getUsers().contains(user));
        assertTrue(role.getUsers().contains(user2));
    }

    @Test
    void should_call_onCreate_lifecycle_method() {
        assertNull(role.getCreatedAt());
        assertNull(role.getUpdatedAt());
        
        role.onCreate();
        
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
        // 比较到秒级精度，避免纳秒级差异
        assertEquals(role.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), 
                    role.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void should_call_onUpdate_lifecycle_method() throws InterruptedException {
        role.onCreate(); // 先设置创建时间
        LocalDateTime originalCreatedAt = role.getCreatedAt();
        LocalDateTime originalUpdatedAt = role.getUpdatedAt();
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(10);
        
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        role.onUpdate(); // 模拟@PreUpdate调用
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertEquals(originalCreatedAt, role.getCreatedAt()); // 创建时间不变
        assertNotEquals(originalUpdatedAt, role.getUpdatedAt()); // 更新时间改变
        assertTrue(role.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(role.getUpdatedAt().isBefore(afterUpdate));
        assertTrue(role.getUpdatedAt().isAfter(role.getCreatedAt()));
    }

    @Test
    void should_return_correct_toString() {
        role.setId(1L);
        role.setName("USER");
        role.setDescription("User role");
        role.setIsActive(true);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        role.setCreatedAt(createdAt);
        
        String result = role.toString();
        
        assertTrue(result.contains("Role{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='USER'"));
        assertTrue(result.contains("description='User role'"));
        assertTrue(result.contains("isActive=true"));
        assertTrue(result.contains("createdAt=2023-01-01T10:00"));
    }

    @Test
    void should_be_equal_when_same_id() {
        Role role1 = new Role();
        Role role2 = new Role();
        
        role1.setId(1L);
        role2.setId(1L);
        
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_id() {
        Role role1 = new Role();
        Role role2 = new Role();
        
        role1.setId(1L);
        role2.setId(2L);
        
        assertNotEquals(role1, role2);
    }

    @Test
    void should_not_be_equal_when_id_is_null() {
        Role role1 = new Role();
        Role role2 = new Role();
        
        role1.setId(null);
        role2.setId(null);
        
        assertNotEquals(role1, role2);
    }

    @Test
    void should_be_equal_to_itself() {
        role.setId(1L);
        
        assertEquals(role, role);
    }

    @Test
    void should_not_be_equal_to_null() {
        role.setId(1L);
        
        assertNotEquals(role, null);
    }

    @Test
    void should_not_be_equal_to_different_class() {
        role.setId(1L);
        
        assertNotEquals(role, "not a role");
    }

    @Test
    void should_have_consistent_hashCode() {
        role.setId(1L);
        
        int hashCode1 = role.hashCode();
        int hashCode2 = role.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void should_handle_null_values() {
        role.setId(null);
        role.setName(null);
        role.setDescription(null);
        role.setIsActive(null);
        role.setCreatedAt(null);
        role.setUpdatedAt(null);
        role.setUsers(null);
        
        assertNull(role.getId());
        assertNull(role.getName());
        assertNull(role.getDescription());
        assertNull(role.getIsActive());
        assertNull(role.getCreatedAt());
        assertNull(role.getUpdatedAt());
        assertNull(role.getUsers());
    }

    @Test
    void should_handle_empty_strings() {
        role.setName("");
        role.setDescription("");
        
        assertEquals("", role.getName());
        assertEquals("", role.getDescription());
    }

    @Test
    void should_maintain_users_set_reference() {
        Set<User> users = new HashSet<>();
        users.add(user);
        
        role.setUsers(users);
        
        assertSame(users, role.getUsers());
        
        // 修改原始集合应该反映在Role中
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        users.add(newUser);
        
        assertEquals(2, role.getUsers().size());
        assertTrue(role.getUsers().contains(newUser));
    }

    @Test
    void should_handle_role_name_constraints() {
        // 测试角色名称的长度限制（数据库约束为50字符）
        String longName = "A".repeat(51); // 超过50字符
        role.setName(longName);
        
        assertEquals(longName, role.getName());
    }

    @Test
    void should_handle_description_constraints() {
        // 测试描述的长度限制（数据库约束为200字符）
        String longDescription = "A".repeat(201); // 超过200字符
        role.setDescription(longDescription);
        
        assertEquals(longDescription, role.getDescription());
    }

    @Test
    void should_handle_bidirectional_relationship_consistency() {
        // 测试双向关系的一致性
        role.addUser(user);
        
        // 验证双向关系
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRoles().contains(role));
        
        // 从用户端移除角色，应该同时更新角色端
        user.removeRole(role);
        
        assertFalse(role.getUsers().contains(user));
        assertFalse(user.getRoles().contains(role));
    }
}