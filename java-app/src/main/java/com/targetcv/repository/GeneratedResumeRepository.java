package com.targetcv.repository;

import com.targetcv.model.GeneratedResume;
import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GeneratedResume entity operations
 */
@Repository
public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    
    /**
     * Find all generated resumes for a specific original resume
     */
    List<GeneratedResume> findByOriginalResumeOrderByCreatedAtDesc(Resume originalResume);
    
    /**
     * Find generated resume by job analysis
     */
    Optional<GeneratedResume> findByJobAnalysis(JobAnalysis jobAnalysis);
    
    /**
     * Find generated resumes by original resume ID
     */
    List<GeneratedResume> findByOriginalResumeIdOrderByCreatedAtDesc(Long originalResumeId);
    
    /**
     * Find generated resumes by status
     */
    List<GeneratedResume> findByStatusOrderByCreatedAtDesc(GeneratedResume.GenerationStatus status);
    
    /**
     * Find generated resumes by file format
     */
    List<GeneratedResume> findByFileFormat(String fileFormat);
    
    /**
     * Find generated resumes created after a specific date
     */
    List<GeneratedResume> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    
    /**
     * Find the most recent generated resume for an original resume
     */
    @Query("SELECT gr FROM GeneratedResume gr WHERE gr.originalResume = :resume " +
           "AND gr.status = 'COMPLETED' ORDER BY gr.createdAt DESC")
    List<GeneratedResume> findRecentGeneratedResumes(@Param("resume") Resume resume);
    
    /**
     * Count generated resumes by original resume
     */
    long countByOriginalResume(Resume originalResume);
    
    /**
     * Count generated resumes by status
     */
    long countByStatus(GeneratedResume.GenerationStatus status);
    
    /**
     * Find generated resumes by AI model used
     */
    List<GeneratedResume> findByAiModelOrderByCreatedAtDesc(String aiModel);
    
    /**
     * Check if a generated resume exists for a specific job analysis
     */
    boolean existsByJobAnalysis(JobAnalysis jobAnalysis);
    
    /**
     * Delete generated resumes older than specified date
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
    
    /**
     * Find failed generations for retry
     */
    @Query("SELECT gr FROM GeneratedResume gr WHERE gr.status = 'FAILED' " +
           "AND gr.createdAt > :since ORDER BY gr.createdAt DESC")
    List<GeneratedResume> findFailedGenerationsSince(@Param("since") LocalDateTime since);
}
