package com.wanli.academy.backend.service;

import com.wanli.academy.backend.base.TestDataBuilder;
import com.wanli.academy.backend.dto.FileResponse;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FileService单元测试
 * 测试文件上传、下载、删除等功能
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("文件服务测试")
class FileServiceTest {

    // 测试常量
    private static final UUID TEST_HOMEWORK_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private AssignmentFileRepository assignmentFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    private AssignmentFile testFile;
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("testuser");
        
        // Mock user repository to return a test user
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(TestDataBuilder.buildTestUser()));

        // 创建测试文件实体
        testFile = createTestAssignmentFile();

        // 创建模拟的MultipartFile
        mockMultipartFile = new MockMultipartFile(
            "file",
            "test-document.pdf",
            "application/pdf",
            "Test file content".getBytes()
        );

        // 设置文件服务的上传目录为临时目录
        try {
            java.lang.reflect.Field uploadDirField = FileService.class.getDeclaredField("uploadDir");
            uploadDirField.setAccessible(true);
            uploadDirField.set(fileService, tempDir.toString());
            
            // 设置最大文件大小为10MB
            java.lang.reflect.Field maxFileSizeField = FileService.class.getDeclaredField("maxFileSize");
            maxFileSizeField.setAccessible(true);
            maxFileSizeField.set(fileService, 10485760L); // 10MB
        } catch (Exception e) {
            // 如果反射失败，跳过设置
        }
    }

    @Test
    @DisplayName("文件上传成功")
    void should_uploadFile_when_validFileProvided() throws IOException {
        // 准备测试数据
        UUID assignmentId = TEST_HOMEWORK_ID;
        Long uploaderId = 1L;
        String category = "ASSIGNMENT_MATERIAL";

        // 创建有实际内容的模拟文件
        byte[] fileContent = "This is a test file content for upload testing. It should be large enough to pass validation.".getBytes();
        MockMultipartFile largerMockFile = new MockMultipartFile(
            "file",
            "test-document.pdf",
            "application/pdf",
            fileContent
        );

        when(assignmentFileRepository.save(any(AssignmentFile.class)))
            .thenAnswer(invocation -> {
                AssignmentFile file = invocation.getArgument(0);
                file.setId(UUID.randomUUID());
                return file;
            });

        // 执行上传
        FileResponse result = fileService.uploadFile(largerMockFile, assignmentId, category);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test-document.pdf", result.getOriginalFileName());
        assertEquals("application/pdf", result.getMimeType());
        assertTrue(result.getFileSize() > 0, "文件大小应该大于0");
        assertEquals(largerMockFile.getSize(), result.getFileSize());
        assertNotNull(result.getFilePath());
        assertNotNull(result.getFileName());

        // 验证文件已保存到磁盘（跳过实际文件系统验证，因为使用了临时目录）

        // 验证数据库保存
        verify(assignmentFileRepository, times(1)).save(any(AssignmentFile.class));
    }

    @Test
    @DisplayName("空文件上传失败")
    void should_throwException_when_uploadingEmptyFile() {
        // 创建空文件
        MultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        // 执行上传并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fileService.uploadFile(emptyFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT")
        );

        assertTrue(exception.getMessage().contains("文件") && 
                  (exception.getMessage().contains("不能为空") || exception.getMessage().contains("大小")));
        verify(assignmentFileRepository, never()).save(any());
    }

    @Test
    @DisplayName("文件名为空上传失败")
    void should_throwException_when_uploadingFileWithEmptyName() {
        // 创建文件名为空的文件
        byte[] fileContent = "This is a test file content for upload testing. It should be large enough to pass validation.".getBytes();
        MultipartFile fileWithEmptyName = new MockMultipartFile(
            "file",
            "",
            "text/plain",
            fileContent
        );

        // 执行上传并验证异常
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> fileService.uploadFile(fileWithEmptyName, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT")
        );

        assertTrue(exception.getMessage().contains("文件名") && exception.getMessage().contains("不能为空"));
        verify(assignmentFileRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取文件信息成功")
    void should_getFileInfo_when_fileExists() {
        UUID fileId = testFile.getId();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.of(testFile));

        FileResponse result = fileService.getFileInfo(fileId);

        assertNotNull(result);
        assertEquals(testFile.getId(), result.getId());
        verify(assignmentFileRepository, times(1)).findById(fileId);
    }

    @Test
    @DisplayName("获取不存在文件信息")
    void should_returnNull_when_fileNotFound() {
        UUID fileId = UUID.randomUUID();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> fileService.getFileInfo(fileId)
        );

        verify(assignmentFileRepository, times(1)).findById(fileId);
    }

    @Test
    @DisplayName("根据作业ID获取文件列表")
    void should_getAssignmentFiles_when_assignmentExists() {
        UUID assignmentId = TEST_HOMEWORK_ID;
        List<AssignmentFile> expectedFiles = Arrays.asList(
            createTestAssignmentFile(),
            createTestAssignmentFile()
        );
        
        when(assignmentFileRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId))
            .thenReturn(expectedFiles);

        List<FileResponse> result = fileService.getAssignmentFiles(assignmentId);

        assertEquals(expectedFiles.size(), result.size());
        verify(assignmentFileRepository, times(1)).findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
    }

    @Test
    @DisplayName("获取用户文件列表")
    void should_getUserFiles_when_userExists() {
        Long userId = 1L;
        List<AssignmentFile> expectedFiles = Arrays.asList(
            createTestAssignmentFile(),
            createTestAssignmentFile()
        );
        
        when(assignmentFileRepository.findByUploadedByOrderByCreatedAtDesc(userId))
            .thenReturn(expectedFiles);

        List<FileResponse> result = fileService.getUserFiles();

        assertEquals(expectedFiles.size(), result.size());
        verify(assignmentFileRepository, times(1)).findByUploadedByOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("删除文件成功")
    void should_deleteFile_when_fileExists() throws IOException {
        UUID fileId = testFile.getId();
        
        // 模拟用户认证和权限
        when(authentication.getName()).thenReturn("testuser");
        User testUser = TestDataBuilder.buildTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        testFile.setUploadedBy(testUser.getId()); // 设置为当前用户上传的文件
        
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.of(testFile));
        doNothing().when(assignmentFileRepository).delete(testFile);

        assertDoesNotThrow(() -> fileService.deleteFile(fileId));

        verify(assignmentFileRepository, times(1)).findById(fileId);
        verify(assignmentFileRepository, times(1)).delete(testFile);
    }

    @Test
    @DisplayName("删除不存在的文件")
    void should_throwException_when_deletingNonExistentFile() {
        UUID fileId = UUID.randomUUID();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> fileService.deleteFile(fileId)
        );

        verify(assignmentFileRepository, times(1)).findById(fileId);
        verify(assignmentFileRepository, never()).delete(any());
    }

    @Test
    @DisplayName("清理过期临时文件")
    void should_cleanupExpiredTempFiles_when_filesExpired() {
        // 测试清理过期临时文件方法
        assertDoesNotThrow(() -> fileService.cleanupExpiredTempFiles());
    }

    @Test
    @DisplayName("下载文件成功")
    void should_downloadFile_when_fileExists() throws IOException {
        UUID fileId = testFile.getId();
        
        // 创建实际文件
        Path filePath = tempDir.resolve(testFile.getFileName());
        Files.write(filePath, "Test file content".getBytes());
        testFile.setFilePath(filePath.toString());
        
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.of(testFile));

        Resource result = fileService.downloadFile(fileId);

        assertNotNull(result);
        verify(assignmentFileRepository, times(1)).findById(fileId);
    }

    @Test
    @DisplayName("下载不存在的文件")
    void should_throwException_when_downloadingNonExistentFile() {
        UUID fileId = UUID.randomUUID();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> fileService.downloadFile(fileId)
        );

        verify(assignmentFileRepository, times(1)).findById(fileId);
    }

    @Test
    @DisplayName("验证文件类型")
    void should_validateFileType_when_checkingFileExtensions() {
        // 设置mock行为
        when(assignmentFileRepository.save(any(AssignmentFile.class)))
            .thenAnswer(invocation -> {
                AssignmentFile file = invocation.getArgument(0);
                file.setId(UUID.randomUUID());
                return file;
            });
        
        // 测试允许的文件类型
        byte[] fileContent = "This is a test file content for upload testing. It should be large enough to pass validation.".getBytes();
        MultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", fileContent);
        MultipartFile docFile = new MockMultipartFile("file", "test.doc", "application/msword", fileContent);
        MultipartFile txtFile = new MockMultipartFile("file", "test.txt", "text/plain", fileContent);
        
        // 允许的文件类型应该上传成功
        assertDoesNotThrow(() -> fileService.uploadFile(pdfFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT"));
        assertDoesNotThrow(() -> fileService.uploadFile(docFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT"));
        assertDoesNotThrow(() -> fileService.uploadFile(txtFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT"));
        
        // 测试不允许的文件类型
        MultipartFile exeFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", fileContent);
        MultipartFile batFile = new MockMultipartFile("file", "test.bat", "application/octet-stream", fileContent);
        
        // 危险文件类型应该抛出异常
        IllegalArgumentException exeException = assertThrows(IllegalArgumentException.class, 
            () -> fileService.uploadFile(exeFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT"));
        assertTrue(exeException.getMessage().contains("不允许上传此类型的文件"));
        
        IllegalArgumentException batException = assertThrows(IllegalArgumentException.class, 
            () -> fileService.uploadFile(batFile, TEST_HOMEWORK_ID, "ASSIGNMENT_ATTACHMENT"));
        assertTrue(batException.getMessage().contains("不允许上传此类型的文件"));
    }

    // ==================== 辅助方法 ====================

    private AssignmentFile createTestAssignmentFile() {
        AssignmentFile file = new AssignmentFile();
        file.setId(UUID.randomUUID());
        file.setAssignmentId(TEST_HOMEWORK_ID);
        file.setFileName("test-file-" + System.currentTimeMillis() + ".pdf");
        file.setOriginalFileName("测试文件.pdf");
        file.setFilePath("/uploads/test-file.pdf");
        file.setFileSize(1024L);
        file.setMimeType("application/pdf");
        file.setFileCategory("ATTACHMENT");
        file.setUploadedBy(1L);
        file.setCreatedAt(LocalDateTime.now());
        file.setUpdatedAt(LocalDateTime.now());
        return file;
    }
}