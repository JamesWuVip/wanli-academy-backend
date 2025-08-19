package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentFileResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        String assignmentTitle = "数学作业第一章";
        String fileName = "homework_template.pdf";
        String filePath = "/uploads/assignments/homework_template.pdf";
        Long fileSize = 1024000L;
        String fileType = "application/pdf";
        String fileCategory = "TEMPLATE";
        Long uploaderId = 1L;
        String uploaderUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AssignmentFileResponse response = new AssignmentFileResponse(
                id, assignmentId, assignmentTitle, fileName, filePath,
                fileSize, fileType, fileCategory, uploaderId, uploaderUsername,
                createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(assignmentId, response.getAssignmentId());
        assertEquals(assignmentTitle, response.getAssignmentTitle());
        assertEquals(fileName, response.getFileName());
        assertEquals(filePath, response.getFilePath());
        assertEquals(fileSize, response.getFileSize());
        assertEquals(fileType, response.getFileType());
        assertEquals(fileCategory, response.getFileCategory());
        assertEquals(uploaderId, response.getUploaderId());
        assertEquals(uploaderUsername, response.getUploaderUsername());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetAssignmentId() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        UUID assignmentId = UUID.randomUUID();
        
        response.setAssignmentId(assignmentId);
        
        assertEquals(assignmentId, response.getAssignmentId());
    }

    @Test
    void should_setAndGetAssignmentTitle() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String title = "数学作业第一章";
        
        response.setAssignmentTitle(title);
        
        assertEquals(title, response.getAssignmentTitle());
    }

    @Test
    void should_setAndGetFileName() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String fileName = "homework_template.pdf";
        
        response.setFileName(fileName);
        
        assertEquals(fileName, response.getFileName());
    }

    @Test
    void should_setAndGetFilePath() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String filePath = "/uploads/assignments/homework_template.pdf";
        
        response.setFilePath(filePath);
        
        assertEquals(filePath, response.getFilePath());
    }

    @Test
    void should_setAndGetFileSize() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        Long fileSize = 1024000L;
        
        response.setFileSize(fileSize);
        
        assertEquals(fileSize, response.getFileSize());
    }

    @Test
    void should_setAndGetFileType() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String fileType = "application/pdf";
        
        response.setFileType(fileType);
        
        assertEquals(fileType, response.getFileType());
    }

    @Test
    void should_setAndGetFileCategory() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String fileCategory = "TEMPLATE";
        
        response.setFileCategory(fileCategory);
        
        assertEquals(fileCategory, response.getFileCategory());
    }

    @Test
    void should_setAndGetUploaderId() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        Long uploaderId = 1L;
        
        response.setUploaderId(uploaderId);
        
        assertEquals(uploaderId, response.getUploaderId());
    }

    @Test
    void should_setAndGetUploaderUsername() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        String username = "张老师";
        
        response.setUploaderUsername(username);
        
        assertEquals(username, response.getUploaderUsername());
    }

    @Test
    void should_setAndGetCreatedAt() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_formatFileSize_whenFileSizeIsNull() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setFileSize(null);
        
        String formatted = response.getFormattedFileSize();
        
        assertEquals("未知", formatted);
    }

    @Test
    void should_formatFileSize_whenFileSizeInBytes() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setFileSize(512L);
        
        String formatted = response.getFormattedFileSize();
        
        assertEquals("512 B", formatted);
    }

    @Test
    void should_formatFileSize_whenFileSizeInKB() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setFileSize(1536L); // 1.5 KB
        
        String formatted = response.getFormattedFileSize();
        
        assertEquals("1.5 KB", formatted);
    }

    @Test
    void should_formatFileSize_whenFileSizeInMB() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setFileSize(1572864L); // 1.5 MB
        
        String formatted = response.getFormattedFileSize();
        
        assertEquals("1.5 MB", formatted);
    }

    @Test
    void should_formatFileSize_whenFileSizeInGB() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setFileSize(1610612736L); // 1.5 GB
        
        String formatted = response.getFormattedFileSize();
        
        assertEquals("1.5 GB", formatted);
    }

    @Test
    void should_returnCorrectToString() {
        UUID id = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        String assignmentTitle = "数学作业第一章";
        String fileName = "homework_template.pdf";
        String filePath = "/uploads/assignments/homework_template.pdf";
        Long fileSize = 1024000L;
        String fileType = "application/pdf";
        String fileCategory = "TEMPLATE";
        Long uploaderId = 1L;
        String uploaderUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AssignmentFileResponse response = new AssignmentFileResponse(
                id, assignmentId, assignmentTitle, fileName, filePath,
                fileSize, fileType, fileCategory, uploaderId, uploaderUsername,
                createdAt, updatedAt
        );

        String toString = response.toString();

        assertTrue(toString.contains("AssignmentFileResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("assignmentId=" + assignmentId));
        assertTrue(toString.contains("assignmentTitle='" + assignmentTitle + "'"));
        assertTrue(toString.contains("fileName='" + fileName + "'"));
        assertTrue(toString.contains("filePath='" + filePath + "'"));
        assertTrue(toString.contains("fileSize=" + fileSize));
        assertTrue(toString.contains("fileType='" + fileType + "'"));
        assertTrue(toString.contains("fileCategory='" + fileCategory + "'"));
        assertTrue(toString.contains("uploaderId=" + uploaderId));
        assertTrue(toString.contains("uploaderUsername='" + uploaderUsername + "'"));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_handleNullValues() {
        AssignmentFileResponse response = new AssignmentFileResponse();
        
        assertNull(response.getId());
        assertNull(response.getAssignmentId());
        assertNull(response.getAssignmentTitle());
        assertNull(response.getFileName());
        assertNull(response.getFilePath());
        assertNull(response.getFileSize());
        assertNull(response.getFileType());
        assertNull(response.getFileCategory());
        assertNull(response.getUploaderId());
        assertNull(response.getUploaderUsername());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }
}