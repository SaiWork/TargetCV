package com.targetcv.repository;

import com.targetcv.model.SavedJobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SavedJobDescription entity
 */
@Repository
public interface SavedJobDescriptionRepository extends JpaRepository<SavedJobDescription, Long> {
    
    /**
     * Find saved job descriptions by title (case-insensitive)
     */
    List<SavedJobDescription> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find saved job descriptions by company name (case-insensitive)
     */
    List<SavedJobDescription> findByCompanyNameContainingIgnoreCase(String companyName);
    
    /**
     * Find saved job descriptions by application status
     */
    List<SavedJobDescription> findByApplicationStatus(SavedJobDescription.ApplicationStatus status);
    
    /**
     * Find saved job descriptions by priority
     */
    List<SavedJobDescription> findByPriority(SavedJobDescription.Priority priority);
    
    /**
     * Find saved job descriptions with upcoming deadlines
     */
    @Query("SELECT s FROM SavedJobDescription s WHERE s.applicationDeadline IS NOT NULL AND s.applicationDeadline > :now ORDER BY s.applicationDeadline ASC")
    List<SavedJobDescription> findUpcomingDeadlines(@Param("now") LocalDateTime now);
    
    /**
     * Find saved job descriptions by tags (contains search)
     */
    @Query("SELECT s FROM SavedJobDescription s WHERE s.tags LIKE %:tag%")
    List<SavedJobDescription> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * Find recent saved job descriptions (last 30 days)
     */
    @Query("SELECT s FROM SavedJobDescription s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SavedJobDescription> findRecentJobs(@Param("since") LocalDateTime since);
    
    /**
     * Find all saved job descriptions ordered by creation date (newest first)
     */
    List<SavedJobDescription> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find all saved job descriptions ordered by priority and creation date
     */
    @Query("SELECT s FROM SavedJobDescription s ORDER BY s.priority DESC, s.createdAt DESC")
    List<SavedJobDescription> findAllByPriorityAndDate();
    
    /**
     * Search saved job descriptions by multiple criteria
     */
    @Query("SELECT s FROM SavedJobDescription s WHERE " +
           "(:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:company IS NULL OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :company, '%'))) AND " +
           "(:status IS NULL OR s.applicationStatus = :status) AND " +
           "(:priority IS NULL OR s.priority = :priority) " +
           "ORDER BY s.createdAt DESC")
    List<SavedJobDescription> searchJobs(@Param("title") String title,
                                       @Param("company") String company,
                                       @Param("status") SavedJobDescription.ApplicationStatus status,
                                       @Param("priority") SavedJobDescription.Priority priority);
    
    /**
     * Count saved job descriptions by status
     */
    long countByApplicationStatus(SavedJobDescription.ApplicationStatus status);
    
    /**
     * Find duplicate job descriptions by title and company
     */
    Optional<SavedJobDescription> findByTitleAndCompanyName(String title, String companyName);
}
