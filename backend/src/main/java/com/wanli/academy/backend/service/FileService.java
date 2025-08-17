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
 * 文件管理服务类
 * 处理文件上传、存储、下载和安全检查
 */
@Service
@Transactional
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    // 文件存储根路径
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    // 最大文件大小（默认10MB）
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;
    
    // 允许的文件类型
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "pdf", "doc", "docx", "txt", "rtf",
        "jpg", "jpeg", "png", "gif", "bmp",
        "zip", "rar", "7z",
        "mp4", "avi", "mov", "wmv",
        "mp3", "wav", "aac",
        "xls", "xlsx", "ppt", "pptx"
    );
    
    // 危险文件类型黑名单
    private static final Set<String> DANGEROUS_FILE_TYPES = Set.of(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar",
        "sh", "php", "asp", "aspx", "jsp", "py", "rb", "pl"
    );
    
    // 文件名安全检查正则
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    @Autowired
    private AssignmentFileRepository assignmentFileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 上传文件
     * @param file 上传的文件
     * @param assignmentId 作业ID（可选）
     * @param fileType 文件类型（ASSIGNMENT_ATTACHMENT, SUBMISSION_FILE等）
     * @return 文件信息
     */
    public FileResponse uploadFile(MultipartFile file, UUID assignmentId, String fileType) {
        logger.info("Processing file upload: {}", file.getOriginalFilename());
        
        // 文件安全检查
        validateFile(file);
        
        // 生成安全的文件名
        String safeFileName = generateSafeFileName(file.getOriginalFilename());
        
        // 创建存储路径
        Path uploadPath = createUploadPath(fileType);
        
        try {
            // 确保目录存在
            Files.createDirectories(uploadPath);
            
            // 保存文件
            Path filePath = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 创建文件记录
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
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     * @param fileId 文件ID
     * @return 文件资源
     */
    public Resource downloadFile(UUID fileId) {
        logger.info("Processing file download: {}", fileId);
        
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        
        // 权限检查
        validateFileAccess(assignmentFile);
        
        try {
            Path filePath = Paths.get(assignmentFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                logger.info("File download successful: {}", assignmentFile.getFileName());
                return resource;
            } else {
                throw new RuntimeException("文件不存在或无法读取");
            }
            
        } catch (MalformedURLException e) {
            logger.error("Failed to download file: {}", fileId, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     * @param fileId 文件ID
     */
    public void deleteFile(UUID fileId) {
        logger.info("Processing file deletion: {}", fileId);
        
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        
        // 权限检查：只有上传者可以删除
        if (!assignmentFile.getUploadedBy().equals(getCurrentUserId())) {
            throw new AccessDeniedException("您只能删除自己上传的文件");
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
            throw new RuntimeException("文件删除失败: " + e.getMessage());
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
     * 获取作业相关文件列表
     * @param assignmentId 作业ID
     * @return 文件列表
     */
    public List<FileResponse> getAssignmentFiles(UUID assignmentId) {
        List<AssignmentFile> files = assignmentFileRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
        return files.stream().map(this::convertToFileResponse).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取用户上传的文件列表
     * @return 文件列表
     */
    public List<FileResponse> getUserFiles() {
        Long currentUserId = getCurrentUserId();
        List<AssignmentFile> files = assignmentFileRepository.findByUploadedByOrderByCreatedAtDesc(currentUserId);
        return files.stream().map(this::convertToFileResponse).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 文件安全验证
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        
        // 检查危险文件类型
        if (DANGEROUS_FILE_TYPES.contains(fileExtension)) {
            throw new IllegalArgumentException("不允许上传此类型的文件: " + fileExtension);
        }
        
        // 检查允许的文件类型
        if (!ALLOWED_FILE_TYPES.contains(fileExtension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }
        
        // 检查文件名安全性
        if (!isFileNameSafe(originalFilename)) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
        
        // 检查MIME类型
        validateMimeType(file, fileExtension);
    }
    
    /**
     * 验证MIME类型
     * @param file 文件
     * @param fileExtension 文件扩展名
     */
    private void validateMimeType(MultipartFile file, String fileExtension) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("无法确定文件类型");
        }
        
        // 简单的MIME类型验证
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
            // 注意：某些情况下MIME类型可能不准确，这里只记录警告而不抛出异常
        }
    }
    
    /**
     * 生成安全的文件名
     * @param originalFilename 原始文件名
     * @return 安全的文件名
     */
    private String generateSafeFileName(String originalFilename) {
        String cleanName = StringUtils.cleanPath(originalFilename);
        String fileExtension = getFileExtension(cleanName);
        String baseName = cleanName.substring(0, cleanName.lastIndexOf('.'));
        
        // 移除特殊字符，只保留字母、数字、点、下划线和连字符
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // 生成时间戳避免文件名冲突
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return baseName + "_" + timestamp + "." + fileExtension;
    }
    
    /**
     * 创建上传路径
     * @param fileType 文件类型
     * @return 上传路径
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
     * 验证文件访问权限
     * @param assignmentFile 文件信息
     */
    private void validateFileAccess(AssignmentFile assignmentFile) {
        Long currentUserId = getCurrentUserId();
        
        // 文件上传者可以访问
        if (assignmentFile.getUploadedBy().equals(currentUserId)) {
            return;
        }
        
        // 如果是作业相关文件，作业创建者也可以访问
        if (assignmentFile.getAssignmentId() != null) {
            // 这里可以添加更复杂的权限逻辑
            // 例如检查当前用户是否是作业的创建者或参与者
        }
        
        // 其他情况拒绝访问
        throw new AccessDeniedException("您没有权限访问此文件");
    }
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * 检查文件名是否安全
     * @param filename 文件名
     * @return 是否安全
     */
    private boolean isFileNameSafe(String filename) {
        // 检查路径遍历攻击
        if (filename.contains("../") || filename.contains("..\\")
            || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        
        // 检查特殊字符
        return !filename.matches(".*[<>:\"|?*].*");
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未登录");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("用户不存在"));
        
        return user.getId();
    }
    
    /**
     * 清理过期的临时文件
     * 可以通过定时任务调用
     */
    public void cleanupExpiredTempFiles() {
        logger.info("Starting cleanup of expired files");
        
        try {
            // 查找超过30天的临时文件
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
      * 转换AssignmentFile为FileResponse
      * @param assignmentFile 文件实体
      * @return 文件响应对象
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
         
         // 设置上传者用户名（如果需要的话）
         if (assignmentFile.getUploader() != null) {
             response.setUploaderUsername(assignmentFile.getUploader().getUsername());
         }
         
         return response;
     }
}