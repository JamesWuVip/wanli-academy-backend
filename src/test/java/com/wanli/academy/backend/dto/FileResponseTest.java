package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        FileResponse response = new FileResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        String fileName = "homework_template.pdf";
        String originalFileName = "作业模板.pdf";
        String filePath = "/uploads/assignments/homework_template.pdf";
        Long fileSize = 1024000L;
        String fileType = "application/pdf";
        String mimeType = "application/pdf";
        String fileCategory = "ASSIGNMENT_FILE";
        Long uploaderId = 1L;
        String uploaderUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        FileResponse response = new FileResponse(
                id, assignmentId, fileName, originalFileName, filePath,
                fileSize, fileType, mimeType, fileCategory, uploaderId,
                uploaderUsername, createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(assignmentId, response.getAssignmentId());
        assertEquals(fileName, response.getFileName());
        assertEquals(originalFileName, response.getOriginalFileName());
        assertEquals(filePath, response.getFilePath());
        assertEquals(fileSize, response.getFileSize());
        assertEquals(fileType, response.getFileType());
        assertEquals(mimeType, response.getMimeType());
        assertEquals(fileCategory, response.getFileCategory());
        assertEquals(uploaderId, response.getUploaderId());
        assertEquals(uploaderUsername, response.getUploaderUsername());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        FileResponse response = new FileResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetAssignmentId() {
        FileResponse response = new FileResponse();
        UUID assignmentId = UUID.randomUUID();
        
        response.setAssignmentId(assignmentId);
        
        assertEquals(assignmentId, response.getAssignmentId());
    }

    @Test
    void should_setAndGetFileName() {
        FileResponse response = new FileResponse();
        String fileName = "homework_template.pdf";
        
        response.setFileName(fileName);
        
        assertEquals(fileName, response.getFileName());
    }

    @Test
    void should_setAndGetOriginalFileName() {
        FileResponse response = new FileResponse();
        String originalFileName = "作业模板.pdf";
        
        response.setOriginalFileName(originalFileName);
        
        assertEquals(originalFileName, response.getOriginalFileName());
    }

    @Test
    void should_setAndGetFilePath() {
        FileResponse response = new FileResponse();
        String filePath = "/uploads/assignments/homework_template.pdf";
        
        response.setFilePath(filePath);
        
        assertEquals(filePath, response.getFilePath());
    }

    @Test
    void should_setAndGetFileSize() {
        FileResponse response = new FileResponse();
        Long fileSize = 1024000L;
        
        response.setFileSize(fileSize);
        
        assertEquals(fileSize, response.getFileSize());
    }

    @Test
    void should_setAndGetFileType() {
        FileResponse response = new FileResponse();
        String fileType = "application/pdf";
        
        response.setFileType(fileType);
        
        assertEquals(fileType, response.getFileType());
    }

    @Test
    void should_setAndGetMimeType() {
        FileResponse response = new FileResponse();
        String mimeType = "application/pdf";
        
        response.setMimeType(mimeType);
        
        assertEquals(mimeType, response.getMimeType());
    }

    @Test
    void should_setAndGetFileCategory() {
        FileResponse response = new FileResponse();
        String fileCategory = "ASSIGNMENT_FILE";
        
        response.setFileCategory(fileCategory);
        
        assertEquals(fileCategory, response.getFileCategory());
    }

    @Test
    void should_setAndGetUploaderId() {
        FileResponse response = new FileResponse();
        Long uploaderId = 1L;
        
        response.setUploaderId(uploaderId);
        
        assertEquals(uploaderId, response.getUploaderId());
    }

    @Test
    void should_setAndGetUploaderUsername() {
        FileResponse response = new FileResponse();
        String username = "张老师";
        
        response.setUploaderUsername(username);
        
        assertEquals(username, response.getUploaderUsername());
    }

    @Test
    void should_setAndGetCreatedAt() {
        FileResponse response = new FileResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        FileResponse response = new FileResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_returnCorrectToString() {
        UUID id = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        String fileName = "homework_template.pdf";
        String originalFileName = "作业模板.pdf";
        String filePath = "/uploads/assignments/homework_template.pdf";
        Long fileSize = 1024000L;
        String fileType = "application/pdf";
        String mimeType = "application/pdf";
        String fileCategory = "ASSIGNMENT_FILE";
        Long uploaderId = 1L;
        String uploaderUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        FileResponse response = new FileResponse(
                id, assignmentId, fileName, originalFileName, filePath,
                fileSize, fileType, mimeType, fileCategory, uploaderId,
                uploaderUsername, createdAt, updatedAt
        );

        String toString = response.toString();

        assertTrue(toString.contains("FileResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("assignmentId=" + assignmentId));
        assertTrue(toString.contains("fileName='" + fileName + "'"));
        assertTrue(toString.contains("originalFileName='" + originalFileName + "'"));
        assertTrue(toString.contains("filePath='" + filePath + "'"));
        assertTrue(toString.contains("fileSize=" + fileSize));
        assertTrue(toString.contains("fileType='" + fileType + "'"));
        assertTrue(toString.contains("mimeType='" + mimeType + "'"));
        assertTrue(toString.contains("fileCategory='" + fileCategory + "'"));
        assertTrue(toString.contains("uploaderId=" + uploaderId));
        assertTrue(toString.contains("uploaderUsername='" + uploaderUsername + "'"));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_handleNullValues() {
        FileResponse response = new FileResponse();
        
        assertNull(response.getId());
        assertNull(response.getAssignmentId());
        assertNull(response.getFileName());
        assertNull(response.getOriginalFileName());
        assertNull(response.getFilePath());
        assertNull(response.getFileSize());
        assertNull(response.getFileType());
        assertNull(response.getMimeType());
        assertNull(response.getFileCategory());
        assertNull(response.getUploaderId());
        assertNull(response.getUploaderUsername());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void should_handleEmptyStrings() {
        FileResponse response = new FileResponse();
        
        response.setFileName("");
        response.setOriginalFileName("");
        response.setFilePath("");
        response.setFileType("");
        response.setMimeType("");
        response.setFileCategory("");
        response.setUploaderUsername("");
        
        assertEquals("", response.getFileName());
        assertEquals("", response.getOriginalFileName());
        assertEquals("", response.getFilePath());
        assertEquals("", response.getFileType());
        assertEquals("", response.getMimeType());
        assertEquals("", response.getFileCategory());
        assertEquals("", response.getUploaderUsername());
    }

    @Test
    void should_handleZeroFileSize() {
        FileResponse response = new FileResponse();
        response.setFileSize(0L);
        
        assertEquals(0L, response.getFileSize());
    }

    @Test
    void should_handleLargeFileSize() {
        FileResponse response = new FileResponse();
        Long largeSize = Long.MAX_VALUE;
        response.setFileSize(largeSize);
        
        assertEquals(largeSize, response.getFileSize());
    }
}