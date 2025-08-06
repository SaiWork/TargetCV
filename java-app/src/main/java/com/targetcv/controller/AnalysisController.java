package com.targetcv.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import com.targetcv.repository.JobAnalysisRepository;
import com.targetcv.repository.ResumeRepository;
import com.targetcv.service.ResumeMatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for job analysis and resume matching functionality
 */
@Controller
@RequestMapping("/analysis")
public class AnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    
    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final ResumeMatchingService resumeMatchingService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AnalysisController(ResumeRepository resumeRepository,
                            JobAnalysisRepository jobAnalysisRepository,
                            ResumeMatchingService resumeMatchingService,
                            ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.resumeMatchingService = resumeMatchingService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Show analysis form for a specific resume
     */
    @GetMapping("/analyze/{resumeId}")
    public String showAnalysisForm(@PathVariable Long resumeId, Model model) {
        logger.info("Displaying analysis form for resume ID: {}", resumeId);
        
        try {
            Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            model.addAttribute("resume", resume);
            model.addAttribute("pageTitle", "Analyze Resume - TargetCV");
            return "analysis/analyze";
            
        } catch (Exception e) {
            logger.error("Error loading analysis form for resume ID: {}", resumeId, e);
            model.addAttribute("error", "Resume not found");
            return "error";
        }
    }
    
    /**
     * Process job analysis
     */
    @PostMapping("/analyze/{resumeId}")
    public String processAnalysis(@PathVariable Long resumeId,
                                @RequestParam("jobDescription") String jobDescription,
                                @RequestParam(value = "jobTitle", required = false) String jobTitle,
                                @RequestParam(value = "companyName", required = false) String companyName,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        logger.info("Processing analysis for resume ID: {}", resumeId);
        
        try {
            // Validate input
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Job description is required");
                return "redirect:/analysis/analyze/" + resumeId;
            }
            
            // Get resume
            Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            // Perform analysis
            JobAnalysis analysis = resumeMatchingService.analyzeResumeMatch(resume, jobDescription);
            
            // Set additional job details if provided
            if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                analysis.setJobTitle(jobTitle.trim());
            }
            if (companyName != null && !companyName.trim().isEmpty()) {
                analysis.setCompanyName(companyName.trim());
            }
            
            // Save updated analysis
            jobAnalysisRepository.save(analysis);
            
            logger.info("Analysis completed successfully with ID: {}", analysis.getId());
            redirectAttributes.addFlashAttribute("success", "Analysis completed successfully!");
            
            return "redirect:/analysis/results/" + analysis.getId();
            
        } catch (Exception e) {
            logger.error("Error processing analysis for resume ID: {}", resumeId, e);
            redirectAttributes.addFlashAttribute("error", "Error during analysis: " + e.getMessage());
            return "redirect:/analysis/analyze/" + resumeId;
        }
    }
    
    /**
     * Display analysis results
     */
    @GetMapping("/results/{analysisId}")
    public String showResults(@PathVariable Long analysisId, Model model) {
        logger.info("Displaying analysis results for ID: {}", analysisId);
        
        try {
            JobAnalysis analysis = jobAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            // Parse JSON fields back to lists for display
            List<String> matchedKeywords = parseJsonToList(analysis.getMatchedKeywords());
            List<String> missingKeywords = parseJsonToList(analysis.getMissingKeywords());
            List<String> matchedSkills = parseJsonToList(analysis.getMatchedSkills());
            List<String> missingSkills = parseJsonToList(analysis.getMissingSkills());
            List<String> recommendations = parseJsonToList(analysis.getRecommendations());
            List<String> strengths = parseJsonToList(analysis.getStrengths());
            List<String> improvements = parseJsonToList(analysis.getImprovements());
            
            // Add data to model
            model.addAttribute("analysis", analysis);
            model.addAttribute("matchedKeywords", matchedKeywords);
            model.addAttribute("missingKeywords", missingKeywords);
            model.addAttribute("matchedSkills", matchedSkills);
            model.addAttribute("missingSkills", missingSkills);
            model.addAttribute("recommendations", recommendations);
            model.addAttribute("strengths", strengths);
            model.addAttribute("improvements", improvements);
            model.addAttribute("pageTitle", "Analysis Results - TargetCV");
            
            return "analysis/results";
            
        } catch (Exception e) {
            logger.error("Error loading analysis results for ID: {}", analysisId, e);
            model.addAttribute("error", "Analysis not found");
            return "error";
        }
    }
    
    /**
     * List all analyses for a resume
     */
    @GetMapping("/history/{resumeId}")
    public String showAnalysisHistory(@PathVariable Long resumeId, Model model) {
        logger.info("Displaying analysis history for resume ID: {}", resumeId);
        
        try {
            Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            List<JobAnalysis> analyses = jobAnalysisRepository.findByResumeOrderByCreatedAtDesc(resume);
            
            model.addAttribute("resume", resume);
            model.addAttribute("analyses", analyses);
            model.addAttribute("pageTitle", "Analysis History - TargetCV");
            
            return "analysis/history";
            
        } catch (Exception e) {
            logger.error("Error loading analysis history for resume ID: {}", resumeId, e);
            model.addAttribute("error", "Unable to load analysis history");
            return "error";
        }
    }
    
    /**
     * Delete analysis
     */
    @PostMapping("/delete/{analysisId}")
    public String deleteAnalysis(@PathVariable Long analysisId, RedirectAttributes redirectAttributes) {
        logger.info("Deleting analysis with ID: {}", analysisId);
        
        try {
            JobAnalysis analysis = jobAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            Long resumeId = analysis.getResume().getId();
            jobAnalysisRepository.delete(analysis);
            
            redirectAttributes.addFlashAttribute("success", "Analysis deleted successfully");
            logger.info("Analysis deleted successfully: {}", analysisId);
            
            return "redirect:/analysis/history/" + resumeId;
            
        } catch (Exception e) {
            logger.error("Error deleting analysis with ID: {}", analysisId, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting analysis: " + e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * Compare multiple analyses
     */
    @GetMapping("/compare")
    public String compareAnalyses(@RequestParam(required = false) List<Long> analysisIds, Model model) {
        if (analysisIds == null || analysisIds.isEmpty()) {
            model.addAttribute("error", "Please select analyses to compare");
            return "error";
        }
        
        try {
            List<JobAnalysis> analyses = jobAnalysisRepository.findAllById(analysisIds);
            model.addAttribute("analyses", analyses);
            model.addAttribute("pageTitle", "Compare Analyses - TargetCV");
            
            return "analysis/compare";
            
        } catch (Exception e) {
            logger.error("Error comparing analyses", e);
            model.addAttribute("error", "Error loading analyses for comparison");
            return "error";
        }
    }
    
    /**
     * Helper method to parse JSON string to List
     */
    private List<String> parseJsonToList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            logger.warn("Error parsing JSON to list: {}", jsonString, e);
            return new ArrayList<>();
        }
    }
}
