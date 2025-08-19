package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 作业文件数据访问接口
 * 提供作业文件相关的数据库操作方法
 */
@Repository
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, UUID> {
    
    /**
     * 根据作业ID查询文件列表
     * @param assignmentId 作业ID
     * @return 文件列表
     */
    List<AssignmentFile> findByAssignmentId(UUID assignmentId);
    
    /**
     * 根据上传者ID查询文件列表
     * @param uploadedBy 上传者ID
     * @return 文件列表
     */
    List<AssignmentFile> findByUploadedBy(Long uploadedBy);
    
    /**
     * 根据文件名查询文件（精确匹配）
     * @param fileName 文件名
     * @return 文件对象
     */
    Optional<AssignmentFile> findByFileName(String fileName);
    
    /**
     * 根据文件名模糊查询文件列表
     * @param fileName 文件名关键词
     * @return 文件列表
     */
    List<AssignmentFile> findByFileNameContainingIgnoreCase(String fileName);
    
    /**
     * 根据文件类型查询文件列表
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<AssignmentFile> findByFileType(String fileType);
    
    /**
     * 根据文件分类查询文件列表
     * @param fileCategory 文件分类
     * @return 文件列表
     */
    List<AssignmentFile> findByFileCategory(String fileCategory);
    
    /**
     * 根据作业ID和文件分类查询文件列表
     * @param assignmentId 作业ID
     * @param fileCategory 文件分类
     * @return 文件列表
     */
    List<AssignmentFile> findByAssignmentIdAndFileCategory(UUID assignmentId, String fileCategory);
    
    /**
     * 根据作业ID和文件类型查询文件列表
     * @param assignmentId 作业ID
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<AssignmentFile> findByAssignmentIdAndFileType(UUID assignmentId, String fileType);
    
    /**
     * 根据作业ID和上传者ID查询文件列表
     * @param assignmentId 作业ID
     * @param uploadedBy 上传者ID
     * @return 文件列表
     */
    List<AssignmentFile> findByAssignmentIdAndUploadedBy(UUID assignmentId, Long uploadedBy);
    
    /**
     * 根据文件路径查询文件
     * @param filePath 文件路径
     * @return 文件对象
     */
    Optional<AssignmentFile> findByFilePath(String filePath);
    
    /**
     * 根据作业ID查询文件列表，按创建时间倒序排列
     * @param assignmentId 作业ID
     * @return 文件列表
     */
    List<AssignmentFile> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId);
    
    /**
     * 根据上传者ID查询文件列表，按创建时间倒序排列
     * @param uploadedBy 上传者ID
     * @return 文件列表
     */
    List<AssignmentFile> findByUploadedByOrderByCreatedAtDesc(Long uploadedBy);
    
    /**
     * 查询文件大小大于指定值的文件列表
     * @param fileSize 文件大小
     * @return 文件列表
     */
    List<AssignmentFile> findByFileSizeGreaterThan(Long fileSize);
    
    /**
     * 查询文件大小小于指定值的文件列表
     * @param fileSize 文件大小
     * @return 文件列表
     */
    List<AssignmentFile> findByFileSizeLessThan(Long fileSize);
    
    /**
     * 查询创建时间在指定时间之后的文件列表
     * @param createdAt 创建时间
     * @return 文件列表
     */
    List<AssignmentFile> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * 查询创建时间在指定时间之前的文件列表
     * @param createdAt 创建时间
     * @return 文件列表
     */
    List<AssignmentFile> findByCreatedAtBefore(LocalDateTime createdAt);
    
    /**
     * 查询创建时间在指定时间范围内的文件列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件列表
     */
    List<AssignmentFile> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据创建时间和文件类型查询文件列表
     * @param createdAt 创建时间
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<AssignmentFile> findByCreatedAtBeforeAndFileType(LocalDateTime createdAt, String fileType);
    
    /**
     * 检查指定作业是否存在指定文件名的文件
     * @param assignmentId 作业ID
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean existsByAssignmentIdAndFileName(UUID assignmentId, String fileName);
    
    /**
     * 检查指定文件路径是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean existsByFilePath(String filePath);
    
    /**
     * 统计指定作业的文件数量
     * @param assignmentId 作业ID
     * @return 文件数量
     */
    long countByAssignmentId(UUID assignmentId);
    
    /**
     * 根据作业ID删除所有相关文件
     * @param assignmentId 作业ID
     */
    void deleteByAssignmentId(UUID assignmentId);
    
    /**
     * 统计指定上传者的文件数量
     * @param uploadedBy 上传者ID
     * @return 文件数量
     */
    long countByUploadedBy(Long uploadedBy);
    
    /**
     * 统计指定文件类型的文件数量
     * @param fileType 文件类型
     * @return 文件数量
     */
    long countByFileType(String fileType);
    
    /**
     * 统计指定文件分类的文件数量
     * @param fileCategory 文件分类
     * @return 文件数量
     */
    long countByFileCategory(String fileCategory);
    
    /**
     * 统计指定作业和文件分类的文件数量
     * @param assignmentId 作业ID
     * @param fileCategory 文件分类
     * @return 文件数量
     */
    long countByAssignmentIdAndFileCategory(UUID assignmentId, String fileCategory);
    
    /**
     * 查询指定作业的总文件大小
     * @param assignmentId 作业ID
     * @return 总文件大小
     */
    @Query("SELECT SUM(af.fileSize) FROM AssignmentFile af WHERE af.assignmentId = :assignmentId")
    Long findTotalFileSizeByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询指定上传者的总文件大小
     * @param uploadedBy 上传者ID
     * @return 总文件大小
     */
    @Query("SELECT SUM(af.fileSize) FROM AssignmentFile af WHERE af.uploadedBy = :uploadedBy")
    Long findTotalFileSizeByUploadedBy(@Param("uploadedBy") Long uploadedBy);
    
    /**
     * 查询指定作业的附件文件列表
     * @param assignmentId 作业ID
     * @return 附件文件列表
     */
    @Query("SELECT af FROM AssignmentFile af WHERE af.assignmentId = :assignmentId AND af.fileCategory = 'ATTACHMENT' ORDER BY af.createdAt DESC")
    List<AssignmentFile> findAttachmentsByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询指定作业的模板文件列表
     * @param assignmentId 作业ID
     * @return 模板文件列表
     */
    @Query("SELECT af FROM AssignmentFile af WHERE af.assignmentId = :assignmentId AND af.fileCategory = 'TEMPLATE' ORDER BY af.createdAt DESC")
    List<AssignmentFile> findTemplatesByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询指定作业的参考文件列表
     * @param assignmentId 作业ID
     * @return 参考文件列表
     */
    @Query("SELECT af FROM AssignmentFile af WHERE af.assignmentId = :assignmentId AND af.fileCategory = 'REFERENCE' ORDER BY af.createdAt DESC")
    List<AssignmentFile> findReferencesByAssignmentId(@Param("assignmentId") UUID assignmentId);
}