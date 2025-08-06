package com.targetcv.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import com.targetcv.repository.JobAnalysisRepository;
import com.targetcv.service.TextAnalysisService.SkillAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for matching resumes against job descriptions and generating analysis results
 */
@Service
@Transactional
public class ResumeMatchingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeMatchingService.class);
    
    private final TextAnalysisService textAnalysisService;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public ResumeMatchingService(TextAnalysisService textAnalysisService,
                               JobAnalysisRepository jobAnalysisRepository,
                               ObjectMapper objectMapper) {
        this.textAnalysisService = textAnalysisService;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Perform comprehensive resume-to-job matching analysis
     */
    public JobAnalysis analyzeResumeMatch(Resume resume, String jobDescription) {
        logger.info("Starting resume analysis for resume ID: {}", resume.getId());
        
        try {
            // Extract content from both resume and job description
            String resumeText = resume.getExtractedText();
            if (resumeText == null || resumeText.trim().isEmpty()) {
                throw new IllegalArgumentException("Resume text is empty or null");
            }
            
            // Perform analysis
            MatchingResult matchingResult = performMatching(resumeText, jobDescription);
            
            // Create and populate JobAnalysis entity
            JobAnalysis analysis = new JobAnalysis(resume, jobDescription);
            populateJobAnalysis(analysis, matchingResult, jobDescription);
            
            // Save analysis to database
            JobAnalysis savedAnalysis = jobAnalysisRepository.save(analysis);
            logger.info("Resume analysis completed and saved with ID: {}", savedAnalysis.getId());
            
            return savedAnalysis;
            
        } catch (Exception e) {
            logger.error("Error during resume analysis for resume ID: {}", resume.getId(), e);
            throw new RuntimeException("Failed to analyze resume match", e);
        }
    }
    
    /**
     * Perform the core matching logic
     */
    private MatchingResult performMatching(String resumeText, String jobDescription) {
        // Extract keywords
        List<String> resumeKeywords = textAnalysisService.extractKeywords(resumeText);
        List<String> jobKeywords = textAnalysisService.extractKeywords(jobDescription);
        
        // Extract skills
        SkillAnalysis resumeSkills = textAnalysisService.extractSkills(resumeText);
        SkillAnalysis jobSkills = textAnalysisService.extractSkills(jobDescription);
        
        // Calculate matches
        Set<String> resumeKeywordSet = new HashSet<>(resumeKeywords);
        Set<String> jobKeywordSet = new HashSet<>(jobKeywords);
        Set<String> resumeSkillSet = new HashSet<>(resumeSkills.getAllSkills());
        Set<String> jobSkillSet = new HashSet<>(jobSkills.getAllSkills());
        
        // Find intersections
        Set<String> matchedKeywords = new HashSet<>(resumeKeywordSet);
        matchedKeywords.retainAll(jobKeywordSet);
        
        Set<String> matchedSkills = new HashSet<>(resumeSkillSet);
        matchedSkills.retainAll(jobSkillSet);
        
        // Find missing elements
        Set<String> missingKeywords = new HashSet<>(jobKeywordSet);
        missingKeywords.removeAll(resumeKeywordSet);
        
        Set<String> missingSkills = new HashSet<>(jobSkillSet);
        missingSkills.removeAll(resumeSkillSet);
        
        // Calculate scores
        double keywordMatchScore = calculateMatchScore(matchedKeywords.size(), jobKeywordSet.size());
        double skillMatchScore = calculateMatchScore(matchedSkills.size(), jobSkillSet.size());
        double textSimilarity = textAnalysisService.calculateTextSimilarity(resumeText, jobDescription);
        
        // Calculate overall score (weighted average)
        double overallScore = (keywordMatchScore * 0.4) + (skillMatchScore * 0.4) + (textSimilarity * 0.2);
        
        return new MatchingResult(
            overallScore,
            keywordMatchScore,
            skillMatchScore,
            new ArrayList<>(matchedKeywords),
            new ArrayList<>(missingKeywords),
            new ArrayList<>(matchedSkills),
            new ArrayList<>(missingSkills)
        );
    }
    
    /**
     * Populate JobAnalysis entity with matching results
     */
    private void populateJobAnalysis(JobAnalysis analysis, MatchingResult result, String jobDescription) 
            throws JsonProcessingException {
        
        // Set scores
        analysis.setOverallMatchScore(BigDecimal.valueOf(result.overallScore).setScale(4, RoundingMode.HALF_UP));
        analysis.setKeywordMatchScore(BigDecimal.valueOf(result.keywordMatchScore).setScale(4, RoundingMode.HALF_UP));
        analysis.setSkillMatchScore(BigDecimal.valueOf(result.skillMatchScore).setScale(4, RoundingMode.HALF_UP));
        
        // Extract job details
        analysis.setExperienceLevel(textAnalysisService.determineExperienceLevel(jobDescription));
        analysis.setJobTitle(extractJobTitle(jobDescription));
        analysis.setCompanyName(extractCompanyName(jobDescription));
        
        // Convert lists to JSON strings
        analysis.setMatchedKeywords(objectMapper.writeValueAsString(result.matchedKeywords));
        analysis.setMissingKeywords(objectMapper.writeValueAsString(result.missingKeywords));
        analysis.setMatchedSkills(objectMapper.writeValueAsString(result.matchedSkills));
        analysis.setMissingSkills(objectMapper.writeValueAsString(result.missingSkills));
        
        // Generate recommendations and insights
        List<String> recommendations = generateRecommendations(result);
        List<String> strengths = identifyStrengths(result);
        List<String> improvements = identifyImprovements(result);
        
        analysis.setRecommendations(objectMapper.writeValueAsString(recommendations));
        analysis.setStrengths(objectMapper.writeValueAsString(strengths));
        analysis.setImprovements(objectMapper.writeValueAsString(improvements));
    }
    
    /**
     * Generate personalized recommendations
     */
    private List<String> generateRecommendations(MatchingResult result) {
        List<String> recommendations = new ArrayList<>();
        
        double overallScore = result.overallScore;
        
        if (overallScore < 0.3) {
            recommendations.add("Consider significantly restructuring your resume to better align with this job description.");
        } else if (overallScore < 0.6) {
            recommendations.add("Your resume has some alignment but needs improvement to better match the job requirements.");
        } else {
            recommendations.add("Your resume shows good alignment with the job requirements!");
        }
        
        if (!result.missingKeywords.isEmpty()) {
            List<String> topMissing = result.missingKeywords.stream().limit(5).collect(Collectors.toList());
            recommendations.add("Consider incorporating these important keywords: " + String.join(", ", topMissing));
        }
        
        if (!result.missingSkills.isEmpty()) {
            List<String> topMissingSkills = result.missingSkills.stream().limit(3).collect(Collectors.toList());
            recommendations.add("Highlight or develop these key skills: " + String.join(", ", topMissingSkills));
        }
        
        // General recommendations
        recommendations.add("Quantify your achievements with specific numbers and metrics where possible.");
        recommendations.add("Use action verbs to describe your accomplishments (e.g., 'implemented', 'optimized', 'led').");
        recommendations.add("Tailor your professional summary to match the job requirements.");
        recommendations.add("Ensure your resume is ATS-friendly with clear formatting and standard section headers.");
        
        return recommendations;
    }
    
    /**
     * Identify resume strengths
     */
    private List<String> identifyStrengths(MatchingResult result) {
        List<String> strengths = new ArrayList<>();
        
        if (result.matchedKeywords.size() > 10) {
            strengths.add("Strong keyword alignment with " + result.matchedKeywords.size() + " matching terms");
        }
        
        if (result.matchedSkills.size() > 5) {
            strengths.add("Good technical skill match with " + result.matchedSkills.size() + " relevant skills");
        }
        
        if (!result.matchedKeywords.isEmpty()) {
            List<String> topKeywords = result.matchedKeywords.stream().limit(3).collect(Collectors.toList());
            strengths.add("Key strengths in: " + String.join(", ", topKeywords));
        }
        
        if (result.overallScore > 0.7) {
            strengths.add("Excellent overall match score indicates strong alignment");
        }
        
        return strengths;
    }
    
    /**
     * Identify areas for improvement
     */
    private List<String> identifyImprovements(MatchingResult result) {
        List<String> improvements = new ArrayList<>();
        
        if (result.missingKeywords.size() > 10) {
            improvements.add("Significant keyword gaps - consider restructuring content");
        }
        
        if (!result.missingSkills.isEmpty()) {
            List<String> topMissing = result.missingSkills.stream().limit(3).collect(Collectors.toList());
            improvements.add("Missing key skills: " + String.join(", ", topMissing));
        }
        
        if (result.missingKeywords.size() > 5) {
            improvements.add("Consider adding more industry-specific terminology");
        }
        
        if (result.overallScore < 0.5) {
            improvements.add("Overall match score needs improvement - focus on better alignment");
        }
        
        return improvements;
    }
    
    /**
     * Calculate match score as percentage
     */
    private double calculateMatchScore(int matches, int total) {
        if (total == 0) return 0.0;
        return (double) matches / total;
    }
    
    /**
     * Extract job title from job description (simple heuristic)
     */
    private String extractJobTitle(String jobDescription) {
        // Simple extraction - look for common patterns
        String[] lines = jobDescription.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 5 && line.length() < 100 && 
                (line.toLowerCase().contains("developer") || 
                 line.toLowerCase().contains("engineer") ||
                 line.toLowerCase().contains("manager") ||
                 line.toLowerCase().contains("analyst"))) {
                return line;
            }
        }
        return null;
    }
    
    /**
     * Extract company name from job description (simple heuristic)
     */
    private String extractCompanyName(String jobDescription) {
        // Simple extraction - this could be enhanced with more sophisticated NLP
        if (jobDescription.toLowerCase().contains("company:")) {
            String[] parts = jobDescription.split("(?i)company:");
            if (parts.length > 1) {
                String companyPart = parts[1].split("\n")[0].trim();
                if (companyPart.length() < 100) {
                    return companyPart;
                }
            }
        }
        return null;
    }
    
    /**
     * Inner class to hold matching results
     */
    private static class MatchingResult {
        final double overallScore;
        final double keywordMatchScore;
        final double skillMatchScore;
        final List<String> matchedKeywords;
        final List<String> missingKeywords;
        final List<String> matchedSkills;
        final List<String> missingSkills;
        
        MatchingResult(double overallScore, double keywordMatchScore, double skillMatchScore,
                      List<String> matchedKeywords, List<String> missingKeywords,
                      List<String> matchedSkills, List<String> missingSkills) {
            this.overallScore = overallScore;
            this.keywordMatchScore = keywordMatchScore;
            this.skillMatchScore = skillMatchScore;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
        }
    }
}
