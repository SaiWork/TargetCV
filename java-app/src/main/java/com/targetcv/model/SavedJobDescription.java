package com.targetcv.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a saved job description for future reuse
 */
@Entity
@Table(name = "saved_job_descriptions")
public class SavedJobDescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "salary_range")
    private String salaryRange;
    
    @Column(name = "employment_type")
    private String employmentType; // Full-time, Part-time, Contract, etc.
    
    @Column(name = "remote_option")
    private String remoteOption; // Remote, Hybrid, On-site
    
    @Column(name = "tags")
    private String tags; // Comma-separated tags for categorization
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // User's personal notes about this job
    
    @Column(name = "application_status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus = ApplicationStatus.SAVED;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;
    
    @Column(name = "source_url")
    private String sourceUrl; // URL where the job was found
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public SavedJobDescription() {}
    
    public SavedJobDescription(String title, String companyName, String jobDescription) {
        this.title = title;
        this.companyName = companyName;
        this.jobDescription = jobDescription;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getJobDescription() {
        return jobDescription;
    }
    
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getSalaryRange() {
        return salaryRange;
    }
    
    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }
    
    public String getEmploymentType() {
        return employmentType;
    }
    
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
    
    public String getRemoteOption() {
        return remoteOption;
    }
    
    public void setRemoteOption(String remoteOption) {
        this.remoteOption = remoteOption;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }
    
    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.toString(); // Can be formatted as needed
    }
    
    public boolean isHighPriority() {
        return priority == Priority.HIGH;
    }
    
    public boolean hasDeadline() {
        return applicationDeadline != null && applicationDeadline.isAfter(LocalDateTime.now());
    }
    
    // Enums
    public enum ApplicationStatus {
        SAVED,
        APPLIED,
        INTERVIEWING,
        OFFERED,
        REJECTED,
        WITHDRAWN
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
