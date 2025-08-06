package com.targetcv.repository;

import com.targetcv.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Resume entity operations
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    
    /**
     * Find resume by filename
     */
    Optional<Resume> findByFilename(String filename);
    
    /**
     * Find resumes by original filename (case-insensitive)
     */
    List<Resume> findByOriginalFilenameContainingIgnoreCase(String originalFilename);
    
    /**
     * Find resumes created after a specific date
     */
    List<Resume> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find resumes by content type
     */
    List<Resume> findByContentType(String contentType);
    
    /**
     * Find resumes with extracted text containing specific keywords
     */
    @Query("SELECT r FROM Resume r WHERE r.extractedText LIKE %:keyword%")
    List<Resume> findByExtractedTextContaining(@Param("keyword") String keyword);
    
    /**
     * Find recent resumes (last 30 days)
     */
    @Query("SELECT r FROM Resume r WHERE r.createdAt >= :thirtyDaysAgo ORDER BY r.createdAt DESC")
    List<Resume> findRecentResumes(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    /**
     * Count resumes by file extension
     */
    @Query("SELECT COUNT(r) FROM Resume r WHERE LOWER(r.originalFilename) LIKE %:extension%")
    long countByFileExtension(@Param("extension") String extension);
    
    /**
     * Find resumes with file size greater than specified bytes
     */
    List<Resume> findByFileSizeGreaterThan(Long fileSize);
    
    /**
     * Find resumes ordered by creation date (newest first)
     */
    List<Resume> findAllByOrderByCreatedAtDesc();
    
    /**
     * Check if resume with filename exists
     */
    boolean existsByFilename(String filename);
}
