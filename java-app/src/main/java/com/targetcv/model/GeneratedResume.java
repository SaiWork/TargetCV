package com.targetcv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a generated resume based on job analysis
 */
@Entity
@Table(name = "generated_resumes")
public class GeneratedResume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_resume_id", nullable = false)
    @NotNull(message = "Original resume is required")
    private Resume originalResume;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_analysis_id", nullable = false)
    @NotNull(message = "Job analysis is required")
    private JobAnalysis jobAnalysis;
    
    @NotBlank(message = "Generated content is required")
    @Column(name = "generated_content", columnDefinition = "LONGTEXT")
    private String generatedContent;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_format")
    private String fileFormat; // "pdf", "docx"
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;
    
    @Column(name = "ai_model")
    private String aiModel;
    
    @Column(name = "generation_status")
    @Enumerated(EnumType.STRING)
    private GenerationStatus status = GenerationStatus.PENDING;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;
    
    @Column(name = "progress_message")
    private String progressMessage = "Initializing...";
    
    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public GeneratedResume() {}
    
    public GeneratedResume(Resume originalResume, JobAnalysis jobAnalysis, String generatedContent) {
        this.originalResume = originalResume;
        this.jobAnalysis = jobAnalysis;
        this.generatedContent = generatedContent;
        this.status = GenerationStatus.COMPLETED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Resume getOriginalResume() {
        return originalResume;
    }
    
    public void setOriginalResume(Resume originalResume) {
        this.originalResume = originalResume;
    }
    
    public JobAnalysis getJobAnalysis() {
        return jobAnalysis;
    }
    
    public void setJobAnalysis(JobAnalysis jobAnalysis) {
        this.jobAnalysis = jobAnalysis;
    }
    
    public String getGeneratedContent() {
        return generatedContent;
    }
    
    public void setGeneratedContent(String generatedContent) {
        this.generatedContent = generatedContent;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileFormat() {
        return fileFormat;
    }
    
    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getGenerationPrompt() {
        return generationPrompt;
    }
    
    public void setGenerationPrompt(String generationPrompt) {
        this.generationPrompt = generationPrompt;
    }
    
    public String getAiModel() {
        return aiModel;
    }
    
    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }
    
    public GenerationStatus getStatus() {
        return status;
    }
    
    public void setStatus(GenerationStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
    
    public Integer getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getProgressMessage() {
        return progressMessage;
    }
    
    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }
    
    public LocalDateTime getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }
    
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }
    
    // Utility methods
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    public boolean isCompleted() {
        return status == GenerationStatus.COMPLETED;
    }
    
    public boolean hasFailed() {
        return status == GenerationStatus.FAILED;
    }
    
    // Enum for generation status
    public enum GenerationStatus {
        PENDING,
        IN_PROGRESS,
        ANALYZING,
        GENERATING,
        FORMATTING,
        COMPLETED,
        FAILED
    }
}
