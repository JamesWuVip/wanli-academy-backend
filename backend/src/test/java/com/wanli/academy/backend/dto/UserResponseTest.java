package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserResponse DTO 测试类
 */
class UserResponseTest {

    @Test
    void should_create_user_response_with_default_constructor() {
        UserResponse userResponse = new UserResponse();
        
        assertNull(userResponse.getId());
        assertNull(userResponse.getUsername());
        assertNull(userResponse.getEmail());
        assertNull(userResponse.getFirstName());
        assertNull(userResponse.getLastName());
        assertNull(userResponse.getPhoneNumber());
        assertNull(userResponse.getIsActive());
        assertNull(userResponse.getRoles());
        assertNull(userResponse.getCreatedAt());
        assertNull(userResponse.getUpdatedAt());
    }

    @Test
    void should_create_user_response_with_all_parameters() {
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String phoneNumber = "1234567890";
        Boolean isActive = true;
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        UserResponse userResponse = new UserResponse(id, username, email, firstName, 
                lastName, phoneNumber, isActive, roles, createdAt, updatedAt);
        
        assertEquals(id, userResponse.getId());
        assertEquals(username, userResponse.getUsername());
        assertEquals(email, userResponse.getEmail());
        assertEquals(firstName, userResponse.getFirstName());
        assertEquals(lastName, userResponse.getLastName());
        assertEquals(phoneNumber, userResponse.getPhoneNumber());
        assertEquals(isActive, userResponse.getIsActive());
        assertEquals(roles, userResponse.getRoles());
        assertEquals(createdAt, userResponse.getCreatedAt());
        assertEquals(updatedAt, userResponse.getUpdatedAt());
    }

    @Test
    void should_set_and_get_all_properties() {
        UserResponse userResponse = new UserResponse();
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String phoneNumber = "1234567890";
        Boolean isActive = false;
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        userResponse.setId(id);
        userResponse.setUsername(username);
        userResponse.setEmail(email);
        userResponse.setFirstName(firstName);
        userResponse.setLastName(lastName);
        userResponse.setPhoneNumber(phoneNumber);
        userResponse.setIsActive(isActive);
        userResponse.setRoles(roles);
        userResponse.setCreatedAt(createdAt);
        userResponse.setUpdatedAt(updatedAt);
        
        assertEquals(id, userResponse.getId());
        assertEquals(username, userResponse.getUsername());
        assertEquals(email, userResponse.getEmail());
        assertEquals(firstName, userResponse.getFirstName());
        assertEquals(lastName, userResponse.getLastName());
        assertEquals(phoneNumber, userResponse.getPhoneNumber());
        assertEquals(isActive, userResponse.getIsActive());
        assertEquals(roles, userResponse.getRoles());
        assertEquals(createdAt, userResponse.getCreatedAt());
        assertEquals(updatedAt, userResponse.getUpdatedAt());
    }

    @Test
    void should_return_correct_toString() {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setPhoneNumber("1234567890");
        userResponse.setIsActive(true);
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        userResponse.setRoles(roles);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 10, 0);
        userResponse.setCreatedAt(createdAt);
        userResponse.setUpdatedAt(updatedAt);
        
        String result = userResponse.toString();
        
        assertTrue(result.contains("UserResponse{"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("email='test@example.com'"));
        assertTrue(result.contains("firstName='John'"));
        assertTrue(result.contains("lastName='Doe'"));
        assertTrue(result.contains("phoneNumber='1234567890'"));
        assertTrue(result.contains("isActive=true"));
        assertTrue(result.contains("roles=[USER]"));
        assertTrue(result.contains("createdAt=2023-01-01T10:00"));
        assertTrue(result.contains("updatedAt=2023-01-02T10:00"));
    }

    @Test
    void should_return_full_name_when_both_names_present() {
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        
        String fullName = userResponse.getFullName();
        
        assertEquals("John Doe", fullName);
    }

    @Test
    void should_return_first_name_when_only_first_name_present() {
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName("John");
        userResponse.setLastName(null);
        
        String fullName = userResponse.getFullName();
        
        assertEquals("John", fullName);
    }

    @Test
    void should_return_last_name_when_only_last_name_present() {
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName(null);
        userResponse.setLastName("Doe");
        
        String fullName = userResponse.getFullName();
        
        assertEquals("Doe", fullName);
    }

    @Test
    void should_return_empty_string_when_both_names_null() {
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName(null);
        userResponse.setLastName(null);
        
        String fullName = userResponse.getFullName();
        
        assertEquals("", fullName);
    }

    @Test
    void should_return_true_when_user_has_role() {
        UserResponse userResponse = new UserResponse();
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");
        userResponse.setRoles(roles);
        
        assertTrue(userResponse.hasRole("USER"));
        assertTrue(userResponse.hasRole("ADMIN"));
    }

    @Test
    void should_return_false_when_user_does_not_have_role() {
        UserResponse userResponse = new UserResponse();
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        userResponse.setRoles(roles);
        
        assertFalse(userResponse.hasRole("ADMIN"));
        assertFalse(userResponse.hasRole("MODERATOR"));
    }

    @Test
    void should_return_false_when_roles_is_null() {
        UserResponse userResponse = new UserResponse();
        userResponse.setRoles(null);
        
        assertFalse(userResponse.hasRole("USER"));
        assertFalse(userResponse.hasRole("ADMIN"));
    }

    @Test
    void should_return_false_when_roles_is_empty() {
        UserResponse userResponse = new UserResponse();
        userResponse.setRoles(new HashSet<>());
        
        assertFalse(userResponse.hasRole("USER"));
        assertFalse(userResponse.hasRole("ADMIN"));
    }

    @Test
    void should_handle_null_values_properly() {
        UserResponse userResponse = new UserResponse();
        
        userResponse.setId(null);
        userResponse.setUsername(null);
        userResponse.setEmail(null);
        userResponse.setFirstName(null);
        userResponse.setLastName(null);
        userResponse.setPhoneNumber(null);
        userResponse.setIsActive(null);
        userResponse.setRoles(null);
        userResponse.setCreatedAt(null);
        userResponse.setUpdatedAt(null);
        
        assertNull(userResponse.getId());
        assertNull(userResponse.getUsername());
        assertNull(userResponse.getEmail());
        assertNull(userResponse.getFirstName());
        assertNull(userResponse.getLastName());
        assertNull(userResponse.getPhoneNumber());
        assertNull(userResponse.getIsActive());
        assertNull(userResponse.getRoles());
        assertNull(userResponse.getCreatedAt());
        assertNull(userResponse.getUpdatedAt());
    }

    @Test
    void should_handle_empty_strings() {
        UserResponse userResponse = new UserResponse();
        
        userResponse.setUsername("");
        userResponse.setEmail("");
        userResponse.setFirstName("");
        userResponse.setLastName("");
        userResponse.setPhoneNumber("");
        
        assertEquals("", userResponse.getUsername());
        assertEquals("", userResponse.getEmail());
        assertEquals("", userResponse.getFirstName());
        assertEquals("", userResponse.getLastName());
        assertEquals("", userResponse.getPhoneNumber());
    }

    @Test
    void should_maintain_roles_set_reference() {
        UserResponse userResponse = new UserResponse();
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        
        userResponse.setRoles(roles);
        
        assertSame(roles, userResponse.getRoles());
        
        // 修改原始集合应该反映在UserResponse中
        roles.add("ADMIN");
        assertTrue(userResponse.hasRole("ADMIN"));
    }

    @Test
    void should_handle_large_roles_set() {
        UserResponse userResponse = new UserResponse();
        Set<String> roles = new HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            roles.add("ROLE_" + i);
        }
        
        userResponse.setRoles(roles);
        
        assertEquals(100, userResponse.getRoles().size());
        assertTrue(userResponse.hasRole("ROLE_50"));
        assertFalse(userResponse.hasRole("ROLE_100"));
    }

    @Test
    void should_handle_boolean_active_status() {
        UserResponse userResponse = new UserResponse();
        
        userResponse.setIsActive(true);
        assertTrue(userResponse.getIsActive());
        
        userResponse.setIsActive(false);
        assertFalse(userResponse.getIsActive());
        
        userResponse.setIsActive(null);
        assertNull(userResponse.getIsActive());
    }

    @Test
    void should_handle_time_sequence() {
        UserResponse userResponse = new UserResponse();
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 10, 0);
        
        userResponse.setCreatedAt(createdAt);
        userResponse.setUpdatedAt(updatedAt);
        
        assertTrue(userResponse.getUpdatedAt().isAfter(userResponse.getCreatedAt()));
    }
}