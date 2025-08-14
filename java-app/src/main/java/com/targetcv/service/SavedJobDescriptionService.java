package com.targetcv.service;

import com.targetcv.model.SavedJobDescription;
import com.targetcv.repository.SavedJobDescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing saved job descriptions
 */
@Service
@Transactional
public class SavedJobDescriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SavedJobDescriptionService.class);
    
    private final SavedJobDescriptionRepository repository;
    
    @Autowired
    public SavedJobDescriptionService(SavedJobDescriptionRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Save a new job description
     */
    public SavedJobDescription saveJobDescription(SavedJobDescription jobDescription) {
        logger.info("Saving job description: {} at {}", jobDescription.getTitle(), jobDescription.getCompanyName());
        
        // Check for duplicates
        Optional<SavedJobDescription> existing = repository.findByTitleAndCompanyName(
            jobDescription.getTitle(), jobDescription.getCompanyName());
        
        if (existing.isPresent()) {
            logger.info("Job description already exists, updating existing record");
            SavedJobDescription existingJob = existing.get();
            existingJob.setJobDescription(jobDescription.getJobDescription());
            existingJob.setLocation(jobDescription.getLocation());
            existingJob.setSalaryRange(jobDescription.getSalaryRange());
            existingJob.setEmploymentType(jobDescription.getEmploymentType());
            existingJob.setRemoteOption(jobDescription.getRemoteOption());
            existingJob.setTags(jobDescription.getTags());
            existingJob.setNotes(jobDescription.getNotes());
            existingJob.setSourceUrl(jobDescription.getSourceUrl());
            existingJob.setApplicationDeadline(jobDescription.getApplicationDeadline());
            return repository.save(existingJob);
        }
        
        return repository.save(jobDescription);
    }
    
    /**
     * Save job description from analysis form data
     */
    public SavedJobDescription saveFromAnalysisData(String jobTitle, String companyName, String jobDescription) {
        SavedJobDescription saved = new SavedJobDescription();
        saved.setTitle(jobTitle != null ? jobTitle : "Untitled Position");
        saved.setCompanyName(companyName != null ? companyName : "Unknown Company");
        saved.setJobDescription(jobDescription);
        saved.setApplicationStatus(SavedJobDescription.ApplicationStatus.SAVED);
        saved.setPriority(SavedJobDescription.Priority.MEDIUM);
        
        return saveJobDescription(saved);
    }
    
    /**
     * Get all saved job descriptions
     */
    @Transactional(readOnly = true)
    public List<SavedJobDescription> getAllSavedJobs() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get saved job descriptions by priority
     */
    @Transactional(readOnly = true)
    public List<SavedJobDescription> getJobsByPriority() {
        return repository.findAllByPriorityAndDate();
    }
    
    /**
     * Get recent saved job descriptions (last 30 days)
     */
    @Transactional(readOnly = true)
    public List<SavedJobDescription> getRecentJobs() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return repository.findRecentJobs(thirtyDaysAgo);
    }
    
    /**
     * Get jobs with upcoming deadlines
     */
    @Transactional(readOnly = true)
    public List<SavedJobDescription> getUpcomingDeadlines() {
        return repository.findUpcomingDeadlines(LocalDateTime.now());
    }
    
    /**
     * Search saved job descriptions
     */
    @Transactional(readOnly = true)
    public List<SavedJobDescription> searchJobs(String title, String company, 
                                              SavedJobDescription.ApplicationStatus status,
                                              SavedJobDescription.Priority priority) {
        return repository.searchJobs(title, company, status, priority);
    }
    
    /**
     * Get saved job description by ID
     */
    @Transactional(readOnly = true)
    public Optional<SavedJobDescription> getJobById(Long id) {
        return repository.findById(id);
    }
    
    /**
     * Update job description
     */
    public SavedJobDescription updateJobDescription(SavedJobDescription jobDescription) {
        logger.info("Updating job description ID: {}", jobDescription.getId());
        return repository.save(jobDescription);
    }
    
    /**
     * Update application status
     */
    public SavedJobDescription updateApplicationStatus(Long id, SavedJobDescription.ApplicationStatus status) {
        Optional<SavedJobDescription> jobOpt = repository.findById(id);
        if (jobOpt.isPresent()) {
            SavedJobDescription job = jobOpt.get();
            job.setApplicationStatus(status);
            logger.info("Updated application status for job ID {} to {}", id, status);
            return repository.save(job);
        }
        throw new RuntimeException("Job description not found with ID: " + id);
    }
    
    /**
     * Update priority
     */
    public SavedJobDescription updatePriority(Long id, SavedJobDescription.Priority priority) {
        Optional<SavedJobDescription> jobOpt = repository.findById(id);
        if (jobOpt.isPresent()) {
            SavedJobDescription job = jobOpt.get();
            job.setPriority(priority);
            logger.info("Updated priority for job ID {} to {}", id, priority);
            return repository.save(job);
        }
        throw new RuntimeException("Job description not found with ID: " + id);
    }
    
    /**
     * Add notes to job description
     */
    public SavedJobDescription addNotes(Long id, String notes) {
        Optional<SavedJobDescription> jobOpt = repository.findById(id);
        if (jobOpt.isPresent()) {
            SavedJobDescription job = jobOpt.get();
            job.setNotes(notes);
            logger.info("Added notes to job ID {}", id);
            return repository.save(job);
        }
        throw new RuntimeException("Job description not found with ID: " + id);
    }
    
    /**
     * Delete saved job description
     */
    public void deleteJobDescription(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            logger.info("Deleted job description ID: {}", id);
        } else {
            throw new RuntimeException("Job description not found with ID: " + id);
        }
    }
    
    /**
     * Get statistics about saved jobs
     */
    @Transactional(readOnly = true)
    public JobStatistics getJobStatistics() {
        JobStatistics stats = new JobStatistics();
        stats.totalJobs = repository.count();
        stats.savedJobs = repository.countByApplicationStatus(SavedJobDescription.ApplicationStatus.SAVED);
        stats.appliedJobs = repository.countByApplicationStatus(SavedJobDescription.ApplicationStatus.APPLIED);
        stats.interviewingJobs = repository.countByApplicationStatus(SavedJobDescription.ApplicationStatus.INTERVIEWING);
        stats.offeredJobs = repository.countByApplicationStatus(SavedJobDescription.ApplicationStatus.OFFERED);
        stats.rejectedJobs = repository.countByApplicationStatus(SavedJobDescription.ApplicationStatus.REJECTED);
        return stats;
    }
    
    /**
     * Statistics class for job counts
     */
    public static class JobStatistics {
        public long totalJobs;
        public long savedJobs;
        public long appliedJobs;
        public long interviewingJobs;
        public long offeredJobs;
        public long rejectedJobs;
    }
}
