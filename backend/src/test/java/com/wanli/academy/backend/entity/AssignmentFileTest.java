package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentFileTest {

    private AssignmentFile assignmentFile;
    private UUID testAssignmentId;
    private Long testUploadedBy;

    @BeforeEach
    void setUp() {
        assignmentFile = new AssignmentFile();
        testAssignmentId = UUID.randomUUID();
        testUploadedBy = 123L;
    }

    @Test
    void should_createAssignmentFileWithDefaultConstructor() {
        AssignmentFile newFile = new AssignmentFile();
        
        assertNotNull(newFile);
        assertNull(newFile.getId());
        assertNull(newFile.getAssignmentId());
        assertNull(newFile.getFileName());
        assertNull(newFile.getFilePath());
        assertNull(newFile.getFileSize());
        assertNull(newFile.getFileType());
        assertEquals("ATTACHMENT", newFile.getFileCategory());
        assertNull(newFile.getOriginalFileName());
        assertNull(newFile.getMimeType());
        assertNull(newFile.getUploadedBy());
        assertNull(newFile.getCreatedAt());
        assertNull(newFile.getUpdatedAt());
        assertNull(newFile.getAssignment());
        assertNull(newFile.getUploader());
    }

    @Test
    void should_createAssignmentFileWithParameterizedConstructor() {
        String fileName = "test.pdf";
        String filePath = "/uploads/test.pdf";
        
        AssignmentFile newFile = new AssignmentFile(testAssignmentId, fileName, filePath, testUploadedBy);
        
        assertNotNull(newFile);
        assertEquals(testAssignmentId, newFile.getAssignmentId());
        assertEquals(fileName, newFile.getFileName());
        assertEquals(filePath, newFile.getFilePath());
        assertEquals(testUploadedBy, newFile.getUploadedBy());
        assertNull(newFile.getId());
        assertNull(newFile.getFileSize());
        assertNull(newFile.getFileType());
        assertEquals("ATTACHMENT", newFile.getFileCategory());
        assertNull(newFile.getOriginalFileName());
        assertNull(newFile.getMimeType());
        assertNull(newFile.getCreatedAt());
        assertNull(newFile.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        UUID testId = UUID.randomUUID();
        assignmentFile.setId(testId);
        
        assertEquals(testId, assignmentFile.getId());
    }

    @Test
    void should_setAndGetAssignmentId() {
        assignmentFile.setAssignmentId(testAssignmentId);
        
        assertEquals(testAssignmentId, assignmentFile.getAssignmentId());
    }

    @Test
    void should_setAndGetFileName() {
        String fileName = "document.docx";
        assignmentFile.setFileName(fileName);
        
        assertEquals(fileName, assignmentFile.getFileName());
    }

    @Test
    void should_handleNullFileName() {
        assignmentFile.setFileName(null);
        
        assertNull(assignmentFile.getFileName());
    }

    @Test
    void should_handleEmptyFileName() {
        assignmentFile.setFileName("");
        
        assertEquals("", assignmentFile.getFileName());
    }

    @Test
    void should_handleLongFileName() {
        String longFileName = "a".repeat(255);
        assignmentFile.setFileName(longFileName);
        
        assertEquals(longFileName, assignmentFile.getFileName());
        assertEquals(255, assignmentFile.getFileName().length());
    }

    @Test
    void should_setAndGetFilePath() {
        String filePath = "/uploads/assignments/file.pdf";
        assignmentFile.setFilePath(filePath);
        
        assertEquals(filePath, assignmentFile.getFilePath());
    }

    @Test
    void should_handleNullFilePath() {
        assignmentFile.setFilePath(null);
        
        assertNull(assignmentFile.getFilePath());
    }

    @Test
    void should_handleEmptyFilePath() {
        assignmentFile.setFilePath("");
        
        assertEquals("", assignmentFile.getFilePath());
    }

    @Test
    void should_setAndGetFileSize() {
        Long fileSize = 1024L;
        assignmentFile.setFileSize(fileSize);
        
        assertEquals(fileSize, assignmentFile.getFileSize());
    }

    @Test
    void should_handleNullFileSize() {
        assignmentFile.setFileSize(null);
        
        assertNull(assignmentFile.getFileSize());
    }

    @Test
    void should_handleZeroFileSize() {
        assignmentFile.setFileSize(0L);
        
        assertEquals(0L, assignmentFile.getFileSize());
    }

    @Test
    void should_handleLargeFileSize() {
        Long largeSize = 1073741824L; // 1GB
        assignmentFile.setFileSize(largeSize);
        
        assertEquals(largeSize, assignmentFile.getFileSize());
    }

    @Test
    void should_setAndGetFileType() {
        String fileType = "application/pdf";
        assignmentFile.setFileType(fileType);
        
        assertEquals(fileType, assignmentFile.getFileType());
    }

    @Test
    void should_handleNullFileType() {
        assignmentFile.setFileType(null);
        
        assertNull(assignmentFile.getFileType());
    }

    @Test
    void should_handleEmptyFileType() {
        assignmentFile.setFileType("");
        
        assertEquals("", assignmentFile.getFileType());
    }

    @Test
    void should_setAndGetFileCategory() {
        String category = "TEMPLATE";
        assignmentFile.setFileCategory(category);
        
        assertEquals(category, assignmentFile.getFileCategory());
    }

    @Test
    void should_handleDefaultFileCategory() {
        AssignmentFile newFile = new AssignmentFile();
        
        assertEquals("ATTACHMENT", newFile.getFileCategory());
    }

    @Test
    void should_handleNullFileCategory() {
        assignmentFile.setFileCategory(null);
        
        assertNull(assignmentFile.getFileCategory());
    }

    @Test
    void should_handleValidFileCategories() {
        String[] validCategories = {"ATTACHMENT", "TEMPLATE", "REFERENCE"};
        
        for (String category : validCategories) {
            assignmentFile.setFileCategory(category);
            assertEquals(category, assignmentFile.getFileCategory());
        }
    }

    @Test
    void should_setAndGetOriginalFileName() {
        String originalFileName = "original_document.pdf";
        assignmentFile.setOriginalFileName(originalFileName);
        
        assertEquals(originalFileName, assignmentFile.getOriginalFileName());
    }

    @Test
    void should_handleNullOriginalFileName() {
        assignmentFile.setOriginalFileName(null);
        
        assertNull(assignmentFile.getOriginalFileName());
    }

    @Test
    void should_handleEmptyOriginalFileName() {
        assignmentFile.setOriginalFileName("");
        
        assertEquals("", assignmentFile.getOriginalFileName());
    }

    @Test
    void should_setAndGetMimeType() {
        String mimeType = "application/pdf";
        assignmentFile.setMimeType(mimeType);
        
        assertEquals(mimeType, assignmentFile.getMimeType());
    }

    @Test
    void should_handleNullMimeType() {
        assignmentFile.setMimeType(null);
        
        assertNull(assignmentFile.getMimeType());
    }

    @Test
    void should_handleEmptyMimeType() {
        assignmentFile.setMimeType("");
        
        assertEquals("", assignmentFile.getMimeType());
    }

    @Test
    void should_handleVariousMimeTypes() {
        String[] mimeTypes = {
            "application/pdf",
            "image/jpeg",
            "text/plain",
            "application/vnd.ms-excel",
            "video/mp4"
        };
        
        for (String mimeType : mimeTypes) {
            assignmentFile.setMimeType(mimeType);
            assertEquals(mimeType, assignmentFile.getMimeType());
        }
    }

    @Test
    void should_setAndGetUploadedBy() {
        assignmentFile.setUploadedBy(testUploadedBy);
        
        assertEquals(testUploadedBy, assignmentFile.getUploadedBy());
    }

    @Test
    void should_handleNullUploadedBy() {
        assignmentFile.setUploadedBy(null);
        
        assertNull(assignmentFile.getUploadedBy());
    }

    @Test
    void should_setAndGetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        assignmentFile.setCreatedAt(now);
        
        assertEquals(now, assignmentFile.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        assignmentFile.setUpdatedAt(now);
        
        assertEquals(now, assignmentFile.getUpdatedAt());
    }

    @Test
    void should_setAndGetAssignment() {
        Assignment assignment = new Assignment();
        assignmentFile.setAssignment(assignment);
        
        assertEquals(assignment, assignmentFile.getAssignment());
    }

    @Test
    void should_handleNullAssignment() {
        assignmentFile.setAssignment(null);
        
        assertNull(assignmentFile.getAssignment());
    }

    @Test
    void should_setAndGetUploader() {
        User uploader = new User();
        assignmentFile.setUploader(uploader);
        
        assertEquals(uploader, assignmentFile.getUploader());
    }

    @Test
    void should_handleNullUploader() {
        assignmentFile.setUploader(null);
        
        assertNull(assignmentFile.getUploader());
    }

    @Test
    void should_returnCorrectToString() {
        UUID testId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0);
        
        assignmentFile.setId(testId);
        assignmentFile.setAssignmentId(testAssignmentId);
        assignmentFile.setFileName("test.pdf");
        assignmentFile.setFilePath("/uploads/test.pdf");
        assignmentFile.setFileSize(1024L);
        assignmentFile.setFileType("application/pdf");
        assignmentFile.setFileCategory("ATTACHMENT");
        assignmentFile.setUploadedBy(testUploadedBy);
        assignmentFile.setCreatedAt(createdAt);
        assignmentFile.setUpdatedAt(updatedAt);
        
        String result = assignmentFile.toString();
        
        assertTrue(result.contains("AssignmentFile{"));
        assertTrue(result.contains("id=" + testId));
        assertTrue(result.contains("assignmentId=" + testAssignmentId));
        assertTrue(result.contains("fileName='test.pdf'"));
        assertTrue(result.contains("filePath='/uploads/test.pdf'"));
        assertTrue(result.contains("fileSize=1024"));
        assertTrue(result.contains("fileType='application/pdf'"));
        assertTrue(result.contains("fileCategory='ATTACHMENT'"));
        assertTrue(result.contains("uploadedBy=" + testUploadedBy));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_returnCorrectToStringWithNullValues() {
        String result = assignmentFile.toString();
        
        assertTrue(result.contains("AssignmentFile{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("assignmentId=null"));
        assertTrue(result.contains("fileName='null'"));
        assertTrue(result.contains("filePath='null'"));
        assertTrue(result.contains("fileSize=null"));
        assertTrue(result.contains("fileType='null'"));
        assertTrue(result.contains("fileCategory='ATTACHMENT'"));
        assertTrue(result.contains("uploadedBy=null"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
    }

    @Test
    void should_handleCompleteFileUploadWorkflow() {
        // Simulate file upload workflow
        assignmentFile.setAssignmentId(testAssignmentId);
        assignmentFile.setFileName("homework_template.pdf");
        assignmentFile.setOriginalFileName("Math Homework Template.pdf");
        assignmentFile.setFilePath("/uploads/assignments/" + testAssignmentId + "/homework_template.pdf");
        assignmentFile.setFileSize(2048L);
        assignmentFile.setFileType("pdf");
        assignmentFile.setMimeType("application/pdf");
        assignmentFile.setFileCategory("TEMPLATE");
        assignmentFile.setUploadedBy(testUploadedBy);
        
        // Verify all properties are set correctly
        assertEquals(testAssignmentId, assignmentFile.getAssignmentId());
        assertEquals("homework_template.pdf", assignmentFile.getFileName());
        assertEquals("Math Homework Template.pdf", assignmentFile.getOriginalFileName());
        assertTrue(assignmentFile.getFilePath().contains(testAssignmentId.toString()));
        assertEquals(2048L, assignmentFile.getFileSize());
        assertEquals("pdf", assignmentFile.getFileType());
        assertEquals("application/pdf", assignmentFile.getMimeType());
        assertEquals("TEMPLATE", assignmentFile.getFileCategory());
        assertEquals(testUploadedBy, assignmentFile.getUploadedBy());
    }

    @Test
    void should_handleDifferentFileExtensions() {
        String[][] fileData = {
            {"document.pdf", "application/pdf", "pdf"},
            {"image.jpg", "image/jpeg", "jpg"},
            {"spreadsheet.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"},
            {"presentation.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"},
            {"text.txt", "text/plain", "txt"}
        };
        
        for (String[] data : fileData) {
            assignmentFile.setFileName(data[0]);
            assignmentFile.setMimeType(data[1]);
            assignmentFile.setFileType(data[2]);
            
            assertEquals(data[0], assignmentFile.getFileName());
            assertEquals(data[1], assignmentFile.getMimeType());
            assertEquals(data[2], assignmentFile.getFileType());
        }
    }

    @Test
    void should_handleFileSizeConstraints() {
        // Test various file sizes
        Long[] fileSizes = {0L, 1L, 1024L, 1048576L, 1073741824L}; // 0B, 1B, 1KB, 1MB, 1GB
        
        for (Long size : fileSizes) {
            assignmentFile.setFileSize(size);
            assertEquals(size, assignmentFile.getFileSize());
        }
    }

    @Test
    void should_maintainRelationshipWithAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(testAssignmentId);
        assignment.setTitle("Math Assignment");
        
        assignmentFile.setAssignment(assignment);
        assignmentFile.setAssignmentId(testAssignmentId);
        
        assertEquals(assignment, assignmentFile.getAssignment());
        assertEquals(testAssignmentId, assignmentFile.getAssignmentId());
        assertEquals(testAssignmentId, assignment.getId());
    }

    @Test
    void should_maintainRelationshipWithUploader() {
        User uploader = new User();
        uploader.setId(testUploadedBy);
        uploader.setUsername("teacher1");
        
        assignmentFile.setUploader(uploader);
        assignmentFile.setUploadedBy(testUploadedBy);
        
        assertEquals(uploader, assignmentFile.getUploader());
        assertEquals(testUploadedBy, assignmentFile.getUploadedBy());
        assertEquals(testUploadedBy, uploader.getId());
    }

    @Test
    void should_handleStringLengthConstraints() {
        // Test fileName constraint (255 characters)
        String maxFileName = "a".repeat(255);
        assignmentFile.setFileName(maxFileName);
        assertEquals(255, assignmentFile.getFileName().length());
        
        // Test fileType constraint (100 characters)
        String maxFileType = "a".repeat(100);
        assignmentFile.setFileType(maxFileType);
        assertEquals(100, assignmentFile.getFileType().length());
        
        // Test fileCategory constraint (20 characters)
        String maxFileCategory = "a".repeat(20);
        assignmentFile.setFileCategory(maxFileCategory);
        assertEquals(20, assignmentFile.getFileCategory().length());
        
        // Test originalFileName constraint (255 characters)
        String maxOriginalFileName = "a".repeat(255);
        assignmentFile.setOriginalFileName(maxOriginalFileName);
        assertEquals(255, assignmentFile.getOriginalFileName().length());
        
        // Test mimeType constraint (100 characters)
        String maxMimeType = "a".repeat(100);
        assignmentFile.setMimeType(maxMimeType);
        assertEquals(100, assignmentFile.getMimeType().length());
    }
}