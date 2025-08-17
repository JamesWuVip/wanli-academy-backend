package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User 实体测试类
 */
class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setUsers(new HashSet<>());
    }

    @Test
    void should_create_user_with_default_constructor() {
        User newUser = new User();
        
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
        assertNull(newUser.getPassword());
        assertNull(newUser.getFirstName());
        assertNull(newUser.getLastName());
        assertNull(newUser.getPhoneNumber());
        assertTrue(newUser.getIsActive()); // 默认为true
        assertNull(newUser.getCreatedAt());
        assertNull(newUser.getUpdatedAt());
        assertNotNull(newUser.getRoles());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    void should_create_user_with_parameters() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        User newUser = new User(username, email, password);
        
        assertEquals(username, newUser.getUsername());
        assertEquals(email, newUser.getEmail());
        assertEquals(password, newUser.getPassword());
        assertTrue(newUser.getIsActive()); // 默认为true
        assertNotNull(newUser.getRoles());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    void should_set_and_get_all_properties() {
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";
        String phoneNumber = "1234567890";
        Boolean isActive = false;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setIsActive(isActive);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        user.setRoles(roles);
        
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(phoneNumber, user.getPhoneNumber());
        assertEquals(isActive, user.getIsActive());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void should_add_role_successfully() {
        user.addRole(role);
        
        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));
        assertEquals(1, user.getRoles().size());
        assertEquals(1, role.getUsers().size());
    }

    @Test
    void should_remove_role_successfully() {
        // 先添加角色
        user.addRole(role);
        assertTrue(user.getRoles().contains(role));
        assertTrue(role.getUsers().contains(user));
        
        // 然后移除角色
        user.removeRole(role);
        
        assertFalse(user.getRoles().contains(role));
        assertFalse(role.getUsers().contains(user));
        assertEquals(0, user.getRoles().size());
        assertEquals(0, role.getUsers().size());
    }

    @Test
    void should_handle_multiple_roles() {
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        adminRole.setUsers(new HashSet<>());
        
        user.addRole(role);
        user.addRole(adminRole);
        
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(role));
        assertTrue(user.getRoles().contains(adminRole));
    }

    @Test
    void should_call_onCreate_lifecycle_method() {
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        
        user.onCreate();
        
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        // 比较到秒级精度，避免纳秒级差异
        assertEquals(user.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), 
                    user.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void should_call_onUpdate_lifecycle_method() throws InterruptedException {
        user.onCreate(); // 先设置创建时间
        LocalDateTime originalCreatedAt = user.getCreatedAt();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(10);
        
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        user.onUpdate(); // 模拟@PreUpdate调用
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertEquals(originalCreatedAt, user.getCreatedAt()); // 创建时间不变
        assertNotEquals(originalUpdatedAt, user.getUpdatedAt()); // 更新时间改变
        assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(user.getUpdatedAt().isBefore(afterUpdate));
        assertTrue(user.getUpdatedAt().isAfter(user.getCreatedAt()));
    }

    @Test
    void should_return_correct_toString() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setIsActive(true);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        user.setCreatedAt(createdAt);
        
        String result = user.toString();
        
        assertTrue(result.contains("User{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("email='test@example.com'"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("isActive=true"));
        assertTrue(result.contains("createdAt=2023-01-01T10:00"));
    }

    @Test
    void should_be_equal_when_same_id() {
        User user1 = new User();
        User user2 = new User();
        
        user1.setId(1L);
        user2.setId(1L);
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_id() {
        User user1 = new User();
        User user2 = new User();
        
        user1.setId(1L);
        user2.setId(2L);
        
        assertNotEquals(user1, user2);
    }

    @Test
    void should_not_be_equal_when_id_is_null() {
        User user1 = new User();
        User user2 = new User();
        
        user1.setId(null);
        user2.setId(null);
        
        assertNotEquals(user1, user2);
    }

    @Test
    void should_be_equal_to_itself() {
        user.setId(1L);
        
        assertEquals(user, user);
    }

    @Test
    void should_not_be_equal_to_null() {
        user.setId(1L);
        
        assertNotEquals(user, null);
    }

    @Test
    void should_not_be_equal_to_different_class() {
        user.setId(1L);
        
        assertNotEquals(user, "not a user");
    }

    @Test
    void should_have_consistent_hashCode() {
        user.setId(1L);
        
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void should_handle_null_values() {
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhoneNumber(null);
        user.setIsActive(null);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);
        user.setRoles(null);
        
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getPhoneNumber());
        assertNull(user.getIsActive());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        assertNull(user.getRoles());
    }

    @Test
    void should_handle_empty_strings() {
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        user.setFirstName("");
        user.setLastName("");
        user.setPhoneNumber("");
        
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        assertEquals("", user.getPhoneNumber());
    }

    @Test
    void should_maintain_roles_set_reference() {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        user.setRoles(roles);
        
        assertSame(roles, user.getRoles());
        
        // 修改原始集合应该反映在User中
        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setName("ADMIN");
        roles.add(newRole);
        
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(newRole));
    }
}