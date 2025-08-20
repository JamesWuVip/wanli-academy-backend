package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.FileResponse;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * File management service class
 * Handles file upload, storage, download and security checks
 */
@Service
@Transactional
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    // File storage root path
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    // Maximum file size (default 10MB)
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;
    
    // Allowed file types
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "pdf", "doc", "docx", "txt", "rtf",
        "jpg", "jpeg", "png", "gif", "bmp",
        "zip", "rar", "7z",
        "mp4", "avi", "mov", "wmv",
        "mp3", "wav", "aac",
        "xls", "xlsx", "ppt", "pptx"
    );
    
    // Dangerous file types blacklist
    private static final Set<String> DANGEROUS_FILE_TYPES = Set.of(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar",
        "sh", "php", "asp", "aspx", "jsp", "py", "rb", "pl"
    );
    
    // Filename security check regex
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    @Autowired
    private AssignmentFileRepository assignmentFileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Upload file
     * @param file uploaded file
     * @param assignmentId assignment ID (optional)
     * @param fileType file type (ASSIGNMENT_ATTACHMENT, SUBMISSION_FILE, etc.)
     * @return file information
     */
    public FileResponse uploadFile(MultipartFile file, UUID assignmentId, String fileType) {
        logger.info("Processing file upload: {}", file.getOriginalFilename());
        
        // File security check
        validateFile(file);
        
        // Generate safe filename
        String safeFileName = generateSafeFileName(file.getOriginalFilename());
        
        // Create storage path
        Path uploadPath = createUploadPath(fileType);
        
        try {
            // Ensure directory exists
            Files.createDirectories(uploadPath);
            
            // Save file
            Path filePath = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create file record
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignmentId(assignmentId);
            assignmentFile.setFileName(safeFileName);
            assignmentFile.setOriginalFileName(file.getOriginalFilename());
            assignmentFile.setFilePath(filePath.toString());
            assignmentFile.setFileSize(file.getSize());
            assignmentFile.setFileType(fileType);
            assignmentFile.setMimeType(file.getContentType());
            assignmentFile.setUploadedBy(getCurrentUserId());
            assignmentFile.setCreatedAt(LocalDateTime.now());
            assignmentFile.setUpdatedAt(LocalDateTime.now());
            
            AssignmentFile savedFile = assignmentFileRepository.save(assignmentFile);
            
            logger.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), safeFileName);
            
            return convertToFileResponse(savedFile);
            
        } catch (IOException e) {
            logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
    
    /**
     * Download file
     * @param fileId file ID
     * @return file resource
     */
    public Resource downloadFile(UUID fileId) {
        logger.info("Processing file download: {}", fileId);
        
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File does not exist"));
        
        // Permission check
        validateFileAccess(assignmentFile);
        
        try {
            Path filePath = Paths.get(assignmentFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                logger.info("File download successful: {}", assignmentFile.getFileName());
                return resource;
            } else {
                throw new RuntimeException("File does not exist or cannot be read");
            }
            
        } catch (MalformedURLException e) {
            logger.error("Failed to download file: {}", fileId, e);
            throw new RuntimeException("File download failed: " + e.getMessage());
        }
    }
    
    /**
     * Delete file
     * @param fileId file ID
     */
    public void deleteFile(UUID fileId) {
        logger.info("Processing file deletion: {}", fileId);
        
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File does not exist"));
        
        // Permission check: only uploader can delete
        if (!assignmentFile.getUploadedBy().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You can only delete files you uploaded");
        }
        
        try {
            // 删除物理文件
            Path filePath = Paths.get(assignmentFile.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            assignmentFileRepository.delete(assignmentFile);
            
            logger.info("File deleted successfully: {}", assignmentFile.getFileName());
            
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileId, e);
            throw new RuntimeException("File deletion failed: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    public FileResponse getFileInfo(UUID fileId) {
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        
        // 权限检查
        validateFileAccess(assignmentFile);
        
        return convertToFileResponse(assignmentFile);
    }
    
    /**
     * Get assignment related file list
     * @param assignmentId assignment ID
     * @return file list
     */
    public List<FileResponse> getAssignmentFiles(UUID assignmentId) {
        List<AssignmentFile> files = assignmentFileRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
        return files.stream().map(this::convertToFileResponse).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get user uploaded file list
     * @return file list
     */
    public List<FileResponse> getUserFiles() {
        Long currentUserId = getCurrentUserId();
        List<AssignmentFile> files = assignmentFileRepository.findByUploadedByOrderByCreatedAtDesc(currentUserId);
        return files.stream().map(this::convertToFileResponse).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Validate file security
     * @param file uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds limit: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // Get file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        
        // Check dangerous file types
        if (DANGEROUS_FILE_TYPES.contains(fileExtension)) {
            throw new IllegalArgumentException("Dangerous file type: " + fileExtension);
        }
        
        // Check allowed file types
        if (!ALLOWED_FILE_TYPES.contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }
        
        // Check filename security
        if (!isFileNameSafe(originalFilename)) {
            throw new IllegalArgumentException("Filename contains illegal characters");
        }
        
        // Check MIME type
        validateMimeType(file, fileExtension);
    }
    
    /**
     * Validate MIME type
     * @param file file
     * @param fileExtension file extension
     */
    private void validateMimeType(MultipartFile file, String fileExtension) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Cannot determine file type");
        }
        
        // Simple MIME type validation
        Map<String, Set<String>> allowedMimeTypes = Map.of(
            "pdf", Set.of("application/pdf"),
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"),
            "gif", Set.of("image/gif"),
            "txt", Set.of("text/plain"),
            "doc", Set.of("application/msword"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "zip", Set.of("application/zip", "application/x-zip-compressed")
        );
        
        Set<String> expectedMimeTypes = allowedMimeTypes.get(fileExtension);
        if (expectedMimeTypes != null && !expectedMimeTypes.contains(contentType)) {
            logger.warn("MIME type mismatch for file extension {}: expected {}, got {}", 
                       fileExtension, expectedMimeTypes, contentType);
            // Note: MIME type may be inaccurate in some cases, only log warning without throwing exception
        }
    }
    
    /**
     * Generate safe filename
     * @param originalFilename original filename
     * @return safe filename
     */
    private String generateSafeFileName(String originalFilename) {
        String cleanName = StringUtils.cleanPath(originalFilename);
        String fileExtension = getFileExtension(cleanName);
        String baseName = cleanName.substring(0, cleanName.lastIndexOf('.'));
        
        // Remove unsafe characters
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Generate timestamp to avoid filename conflicts
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return baseName + "_" + timestamp + "." + fileExtension;
    }
    
    /**
     * Create upload path
     * @param fileType file type
     * @return upload path
     */
    private Path createUploadPath(String fileType) {
        String subDir = switch (fileType) {
            case "ASSIGNMENT_ATTACHMENT" -> "assignments";
            case "SUBMISSION_FILE" -> "submissions";
            default -> "others";
        };
        
        return Paths.get(uploadDir, subDir);
    }
    
    /**
     * Validate file access permission
     * @param assignmentFile file information
     */
    private void validateFileAccess(AssignmentFile assignmentFile) {
        Long currentUserId = getCurrentUserId();
        
        // File uploader can access
        if (assignmentFile.getUploadedBy().equals(currentUserId)) {
            return;
        }
        
        // If it's assignment-related file, assignment creator can also access
        if (assignmentFile.getAssignmentId() != null) {
            // More complex permission logic can be added here
            // For example, check if current user is assignment creator or participant
        }
        
        // Deny access in other cases
        throw new AccessDeniedException("You do not have permission to access this file");
    }
    
    /**
     * Get file extension
     * @param filename filename
     * @return extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * Check if filename is safe
     * @param filename filename
     * @return whether it's safe
     */
    private boolean isFileNameSafe(String filename) {
        // Check for path traversal attacks
        if (filename.contains("../") || filename.contains("..\\")
            || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        
        // Check for special characters
        return !filename.matches(".*[<>:\"|?*].*");
    }
    
    /**
     * Get current user ID
     * @return user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not logged in");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User does not exist"));
        
        return user.getId();
    }
    
    /**
     * Clean up expired temporary files
     * Can be called by scheduled tasks
     */
    public void cleanupExpiredTempFiles() {
        logger.info("Starting cleanup of expired files");
        
        try {
            // Find temporary files older than 30 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<AssignmentFile> expiredFiles = assignmentFileRepository.findByCreatedAtBeforeAndFileType(
                cutoffDate, "TEMP");
            
            for (AssignmentFile file : expiredFiles) {
                try {
                    Path filePath = Paths.get(file.getFilePath());
                    Files.deleteIfExists(filePath);
                    assignmentFileRepository.delete(file);
                    logger.info("Deleted expired file: {}", file.getFileName());
                } catch (Exception e) {
                    logger.error("Failed to delete expired file: {}", file.getFileName(), e);
                }
            }
            
            logger.info("Cleanup completed. Deleted {} expired files", expiredFiles.size());
            
        } catch (Exception e) {
            logger.error("Error during file cleanup", e);
        }
    }
    
    /**
      * Convert AssignmentFile to FileResponse
      * @param assignmentFile file entity
      * @return file response object
      */
     private FileResponse convertToFileResponse(AssignmentFile assignmentFile) {
         FileResponse response = new FileResponse();
         response.setId(assignmentFile.getId());
         response.setAssignmentId(assignmentFile.getAssignmentId());
         response.setFileName(assignmentFile.getFileName());
         response.setOriginalFileName(assignmentFile.getOriginalFileName());
         response.setFilePath(assignmentFile.getFilePath());
         response.setFileSize(assignmentFile.getFileSize());
         response.setFileType(assignmentFile.getFileType());
         response.setMimeType(assignmentFile.getMimeType());
         response.setFileCategory(assignmentFile.getFileCategory());
         response.setUploaderId(assignmentFile.getUploadedBy());
         response.setCreatedAt(assignmentFile.getCreatedAt());
         response.setUpdatedAt(assignmentFile.getUpdatedAt());
         
         // Set uploader username (if needed)
         if (assignmentFile.getUploader() != null) {
             response.setUploaderUsername(assignmentFile.getUploader().getUsername());
         }
         
         return response;
     }
}