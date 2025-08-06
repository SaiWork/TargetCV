package com.targetcv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JobAnalysis entity representing the analysis results of a resume against a job description
 */
@Entity
@Table(name = "job_analyses")
public class JobAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @NotNull(message = "Resume is required")
    private Resume resume;
    
    @NotBlank(message = "Job description is required")
    @Lob
    @Column(name = "job_description", columnDefinition = "LONGTEXT", nullable = false)
    private String jobDescription;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "company_name")
    private String companyName;
    
    @DecimalMin(value = "0.0", message = "Match score must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Match score must be between 0 and 1")
    @Column(name = "overall_match_score", precision = 5, scale = 4)
    private BigDecimal overallMatchScore;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "keyword_match_score", precision = 5, scale = 4)
    private BigDecimal keywordMatchScore;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    @Column(name = "skill_match_score", precision = 5, scale = 4)
    private BigDecimal skillMatchScore;
    
    @Column(name = "experience_level")
    private String experienceLevel;
    
    @Lob
    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords; // JSON array as string
    
    @Lob
    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords; // JSON array as string
    
    @Lob
    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills; // JSON array as string
    
    @Lob
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills; // JSON array as string
    
    @Lob
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations; // JSON array as string
    
    @Lob
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // JSON array as string
    
    @Lob
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements; // JSON array as string
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public JobAnalysis() {}
    
    public JobAnalysis(Resume resume, String jobDescription) {
        this.resume = resume;
        this.jobDescription = jobDescription;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Resume getResume() {
        return resume;
    }
    
    public void setResume(Resume resume) {
        this.resume = resume;
    }
    
    public String getJobDescription() {
        return jobDescription;
    }
    
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public BigDecimal getOverallMatchScore() {
        return overallMatchScore;
    }
    
    public void setOverallMatchScore(BigDecimal overallMatchScore) {
        this.overallMatchScore = overallMatchScore;
    }
    
    public BigDecimal getKeywordMatchScore() {
        return keywordMatchScore;
    }
    
    public void setKeywordMatchScore(BigDecimal keywordMatchScore) {
        this.keywordMatchScore = keywordMatchScore;
    }
    
    public BigDecimal getSkillMatchScore() {
        return skillMatchScore;
    }
    
    public void setSkillMatchScore(BigDecimal skillMatchScore) {
        this.skillMatchScore = skillMatchScore;
    }
    
    public String getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public String getMatchedKeywords() {
        return matchedKeywords;
    }
    
    public void setMatchedKeywords(String matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }
    
    public String getMissingKeywords() {
        return missingKeywords;
    }
    
    public void setMissingKeywords(String missingKeywords) {
        this.missingKeywords = missingKeywords;
    }
    
    public String getMatchedSkills() {
        return matchedSkills;
    }
    
    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }
    
    public String getMissingSkills() {
        return missingSkills;
    }
    
    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }
    
    public String getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
    
    public String getStrengths() {
        return strengths;
    }
    
    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }
    
    public String getImprovements() {
        return improvements;
    }
    
    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public int getOverallMatchPercentage() {
        return overallMatchScore != null ? 
            overallMatchScore.multiply(BigDecimal.valueOf(100)).intValue() : 0;
    }
    
    public int getKeywordMatchPercentage() {
        return keywordMatchScore != null ? 
            keywordMatchScore.multiply(BigDecimal.valueOf(100)).intValue() : 0;
    }
    
    public int getSkillMatchPercentage() {
        return skillMatchScore != null ? 
            skillMatchScore.multiply(BigDecimal.valueOf(100)).intValue() : 0;
    }
    
    public String getMatchScoreCategory() {
        if (overallMatchScore == null) return "Unknown";
        
        double score = overallMatchScore.doubleValue();
        if (score >= 0.7) return "Excellent";
        if (score >= 0.5) return "Good";
        if (score >= 0.3) return "Fair";
        return "Needs Improvement";
    }
    
    @Override
    public String toString() {
        return "JobAnalysis{" +
                "id=" + id +
                ", jobTitle='" + jobTitle + '\'' +
                ", companyName='" + companyName + '\'' +
                ", overallMatchScore=" + overallMatchScore +
                ", createdAt=" + createdAt +
                '}';
    }
}
