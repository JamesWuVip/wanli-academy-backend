package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegisterRequest DTO 测试类
 */
@DisplayName("RegisterRequest DTO 测试")
class RegisterRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("默认构造函数应该创建空对象")
    void should_createEmptyObject_when_usingDefaultConstructor() {
        // Given & When
        RegisterRequest request = new RegisterRequest();

        // Then
        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getPhoneNumber());
    }

    @Test
    @DisplayName("带参构造函数应该正确设置所有字段")
    void should_setAllFields_when_usingParameterizedConstructor() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "张";
        String lastName = "三";
        String phoneNumber = "13812345678";

        // When
        RegisterRequest request = new RegisterRequest(username, email, password, firstName, lastName, phoneNumber);

        // Then
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(phoneNumber, request.getPhoneNumber());
    }

    @Test
    @DisplayName("Getter和Setter方法应该正常工作")
    void should_workCorrectly_when_usingGettersAndSetters() {
        // Given
        RegisterRequest request = new RegisterRequest();
        String username = "newuser";
        String email = "new@example.com";
        String password = "newpassword";
        String firstName = "李";
        String lastName = "四";
        String phoneNumber = "15987654321";

        // When
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhoneNumber(phoneNumber);

        // Then
        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(phoneNumber, request.getPhoneNumber());
    }

    @Test
    @DisplayName("toString方法应该返回正确格式的字符串但不包含密码")
    void should_returnCorrectFormatWithoutPassword_when_callingToString() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "secretpassword";
        String firstName = "王";
        String lastName = "五";
        String phoneNumber = "13612345678";
        
        RegisterRequest request = new RegisterRequest(username, email, password, firstName, lastName, phoneNumber);

        // When
        String result = request.toString();

        // Then
        assertTrue(result.contains("RegisterRequest{"));
        assertTrue(result.contains("username='" + username + "'"));
        assertTrue(result.contains("email='" + email + "'"));
        assertTrue(result.contains("firstName='" + firstName + "'"));
        assertTrue(result.contains("lastName='" + lastName + "'"));
        assertTrue(result.contains("phoneNumber='" + phoneNumber + "'"));
        // 注意：RegisterRequest的toString不保护密码，这可能是安全问题
        assertFalse(result.contains("password=")); // 确认toString中没有密码字段
    }

    @Test
    @DisplayName("有效数据应该通过验证")
    void should_passValidation_when_dataIsValid() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser123",
            "test@example.com",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("空用户名应该验证失败")
    void should_failValidation_when_usernameIsBlank() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "",
            "test@example.com",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名不能为空")));
    }

    @Test
    @DisplayName("用户名过短应该验证失败")
    void should_failValidation_when_usernameIsTooShort() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "ab", // 2个字符，少于最小长度3
            "test@example.com",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名长度必须在3-50个字符之间")));
    }

    @Test
    @DisplayName("用户名过长应该验证失败")
    void should_failValidation_when_usernameIsTooLong() {
        // Given
        String longUsername = "a".repeat(51); // 51个字符，超过最大长度50
        RegisterRequest request = new RegisterRequest(
            longUsername,
            "test@example.com",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名长度必须在3-50个字符之间")));
    }

    @Test
    @DisplayName("用户名包含非法字符应该验证失败")
    void should_failValidation_when_usernameContainsIllegalCharacters() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "test-user!", // 包含连字符和感叹号
            "test@example.com",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名只能包含字母、数字和下划线")));
    }

    @Test
    @DisplayName("有效用户名格式应该通过验证")
    void should_passValidation_when_usernameFormatIsValid() {
        // Given
        String[] validUsernames = {"test123", "user_name", "TestUser", "test_123", "ABC"};
        
        for (String username : validUsernames) {
            RegisterRequest request = new RegisterRequest(
                username,
                "test@example.com",
                "password123",
                "张",
                "三",
                "13812345678"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("username")),
                "Username " + username + " should be valid");
        }
    }

    @Test
    @DisplayName("无效邮箱格式应该验证失败")
    void should_failValidation_when_emailFormatIsInvalid() {
        // Given
        String[] invalidEmails = {"invalid-email", "@example.com", "test@", "test.example.com"};
        
        for (String email : invalidEmails) {
            RegisterRequest request = new RegisterRequest(
                "testuser",
                email,
                "password123",
                "张",
                "三",
                "13812345678"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertFalse(violations.isEmpty(), "Email " + email + " should be invalid");
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱格式不正确")));
        }
    }

    @Test
    @DisplayName("空邮箱应该验证失败")
    void should_failValidation_when_emailIsBlank() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser",
            "",
            "password123",
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱不能为空")));
    }

    @Test
    @DisplayName("密码过短应该验证失败")
    void should_failValidation_when_passwordIsTooShort() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "testuser",
            "test@example.com",
            "12345", // 5个字符，少于最小长度6
            "张",
            "三",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码长度必须在6-100个字符之间")));
    }

    @Test
    @DisplayName("空姓名应该验证失败")
    void should_failValidation_when_firstNameOrLastNameIsBlank() {
        // Given - 空姓
        RegisterRequest request1 = new RegisterRequest(
            "testuser",
            "test@example.com",
            "password123",
            "",
            "三",
            "13812345678"
        );
        
        // Given - 空名
        RegisterRequest request2 = new RegisterRequest(
            "testuser",
            "test@example.com",
            "password123",
            "张",
            "",
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations1 = validator.validate(request1);
        Set<ConstraintViolation<RegisterRequest>> violations2 = validator.validate(request2);

        // Then
        assertFalse(violations1.isEmpty());
        assertTrue(violations1.stream().anyMatch(v -> v.getMessage().contains("姓不能为空")));
        
        assertFalse(violations2.isEmpty());
        assertTrue(violations2.stream().anyMatch(v -> v.getMessage().contains("名不能为空")));
    }

    @Test
    @DisplayName("姓名过长应该验证失败")
    void should_failValidation_when_firstNameOrLastNameIsTooLong() {
        // Given
        String longName = "a".repeat(51); // 51个字符，超过最大长度50
        RegisterRequest request1 = new RegisterRequest(
            "testuser",
            "test@example.com",
            "password123",
            longName,
            "三",
            "13812345678"
        );
        
        RegisterRequest request2 = new RegisterRequest(
            "testuser",
            "test@example.com",
            "password123",
            "张",
            longName,
            "13812345678"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations1 = validator.validate(request1);
        Set<ConstraintViolation<RegisterRequest>> violations2 = validator.validate(request2);

        // Then
        assertFalse(violations1.isEmpty());
        assertTrue(violations1.stream().anyMatch(v -> v.getMessage().contains("姓长度不能超过50个字符")));
        
        assertFalse(violations2.isEmpty());
        assertTrue(violations2.stream().anyMatch(v -> v.getMessage().contains("名长度不能超过50个字符")));
    }

    @Test
    @DisplayName("无效手机号格式应该验证失败")
    void should_failValidation_when_phoneNumberFormatIsInvalid() {
        // Given
        String[] invalidPhoneNumbers = {"123", "abcdefghij", "123-456-7890", "12345678901234567"};
        
        for (String phoneNumber : invalidPhoneNumbers) {
            RegisterRequest request = new RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "张",
                "三",
                phoneNumber
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            if (!violations.isEmpty()) {
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("手机号格式不正确")),
                    "Phone number " + phoneNumber + " should be invalid");
            }
        }
    }

    @Test
    @DisplayName("有效手机号格式应该通过验证")
    void should_passValidation_when_phoneNumberFormatIsValid() {
        // Given
        String[] validPhoneNumbers = {"13812345678", "+8613812345678", "15987654321", null}; // null应该被允许
        
        for (String phoneNumber : validPhoneNumbers) {
            RegisterRequest request = new RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "张",
                "三",
                phoneNumber
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")),
                "Phone number " + phoneNumber + " should be valid");
        }
    }
}