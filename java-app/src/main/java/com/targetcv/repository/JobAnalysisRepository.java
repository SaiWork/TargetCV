package com.targetcv.repository;

import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobAnalysis entity operations
 */
@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    
    /**
     * Find all job analyses for a specific resume
     */
    List<JobAnalysis> findByResumeOrderByCreatedAtDesc(Resume resume);
    
    /**
     * Find job analyses by resume ID
     */
    List<JobAnalysis> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    
    /**
     * Find job analyses with match score above threshold
     */
    List<JobAnalysis> findByOverallMatchScoreGreaterThanEqualOrderByOverallMatchScoreDesc(BigDecimal threshold);
    
    /**
     * Find job analyses by company name (case-insensitive)
     */
    List<JobAnalysis> findByCompanyNameContainingIgnoreCase(String companyName);
    
    /**
     * Find job analyses by job title (case-insensitive)
     */
    List<JobAnalysis> findByJobTitleContainingIgnoreCase(String jobTitle);
    
    /**
     * Find job analyses by experience level
     */
    List<JobAnalysis> findByExperienceLevel(String experienceLevel);
    
    /**
     * Find job analyses created after a specific date
     */
    List<JobAnalysis> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    
    /**
     * Find the best match for a resume (highest overall match score)
     */
    @Query("SELECT ja FROM JobAnalysis ja WHERE ja.resume = :resume ORDER BY ja.overallMatchScore DESC")
    List<JobAnalysis> findBestMatchesForResume(@Param("resume") Resume resume);
    
    /**
     * Find recent analyses (last 7 days)
     */
    @Query("SELECT ja FROM JobAnalysis ja WHERE ja.createdAt >= :sevenDaysAgo ORDER BY ja.createdAt DESC")
    List<JobAnalysis> findRecentAnalyses(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
    
    /**
     * Get average match score for all analyses
     */
    @Query("SELECT AVG(ja.overallMatchScore) FROM JobAnalysis ja")
    Optional<BigDecimal> getAverageMatchScore();
    
    /**
     * Count analyses by match score range
     */
    @Query("SELECT COUNT(ja) FROM JobAnalysis ja WHERE ja.overallMatchScore BETWEEN :minScore AND :maxScore")
    long countByMatchScoreRange(@Param("minScore") BigDecimal minScore, @Param("maxScore") BigDecimal maxScore);
    
    /**
     * Find top performing job analyses (top 10% by match score)
     */
    @Query(value = "SELECT * FROM job_analyses ORDER BY overall_match_score DESC LIMIT :limit", nativeQuery = true)
    List<JobAnalysis> findTopPerformingAnalyses(@Param("limit") int limit);
    
    /**
     * Check if analysis exists for resume and job description combination
     */
    @Query("SELECT COUNT(ja) > 0 FROM JobAnalysis ja WHERE ja.resume = :resume AND ja.jobDescription = :jobDescription")
    boolean existsByResumeAndJobDescription(@Param("resume") Resume resume, @Param("jobDescription") String jobDescription);
    
    /**
     * Find analyses with specific keywords in job description
     */
    @Query("SELECT ja FROM JobAnalysis ja WHERE ja.jobDescription LIKE %:keyword%")
    List<JobAnalysis> findByJobDescriptionContaining(@Param("keyword") String keyword);
}
