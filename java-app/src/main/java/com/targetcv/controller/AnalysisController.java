package com.targetcv.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import com.targetcv.repository.JobAnalysisRepository;
import com.targetcv.repository.ResumeRepository;
import com.targetcv.repository.GeneratedResumeRepository;
import com.targetcv.service.ResumeMatchingService;
import com.targetcv.service.ResumeGenerationService;
import com.targetcv.model.GeneratedResume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for job analysis and resume matching functionality
 */
@Controller
@RequestMapping("/analysis")
public class AnalysisController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    
    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final ResumeMatchingService resumeMatchingService;
    private final ResumeGenerationService resumeGenerationService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AnalysisController(ResumeRepository resumeRepository,
                            JobAnalysisRepository jobAnalysisRepository,
                            GeneratedResumeRepository generatedResumeRepository,
                            ResumeMatchingService resumeMatchingService,
                            ResumeGenerationService resumeGenerationService,
                            ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.generatedResumeRepository = generatedResumeRepository;
        this.resumeMatchingService = resumeMatchingService;
        this.resumeGenerationService = resumeGenerationService;
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
     * Generate optimized resume from analysis
     */
    @PostMapping("/generate/{analysisId}")
    public String generateResume(@PathVariable Long analysisId, RedirectAttributes redirectAttributes) {
        logger.info("Generating optimized resume for analysis ID: {}", analysisId);
        
        try {
            JobAnalysis analysis = jobAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            // Start async generation and get the initial GeneratedResume record
            GeneratedResume generatedResume = resumeGenerationService.startResumeGeneration(analysis);
            
            redirectAttributes.addFlashAttribute("success", "Resume generation started! Please wait...");
            return "redirect:/analysis/generation/progress/" + generatedResume.getId();
            
        } catch (Exception e) {
            logger.error("Error starting resume generation for analysis: {}", analysisId, e);
            redirectAttributes.addFlashAttribute("error", "Error starting resume generation: " + e.getMessage());
            return "redirect:/analysis/results/" + analysisId;
        }
    }
    
    /**
     * Show progress page for resume generation
     */
    @GetMapping("/generation/progress/{generatedResumeId}")
    public String showGenerationProgress(@PathVariable Long generatedResumeId, Model model) {
        try {
            GeneratedResume generatedResume = generatedResumeRepository.findById(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
            
            model.addAttribute("generatedResume", generatedResume);
            model.addAttribute("jobAnalysis", generatedResume.getJobAnalysis());
            
            return "analysis/generation-progress";
            
        } catch (Exception e) {
            logger.error("Error showing generation progress: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }
    
    /**
     * API endpoint to check generation progress
     */
    @GetMapping("/api/generation/progress/{generatedResumeId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGenerationProgress(@PathVariable Long generatedResumeId) {
        try {
            GeneratedResume generatedResume = generatedResumeRepository.findById(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", generatedResume.getId());
            response.put("status", generatedResume.getStatus().toString());
            response.put("progressPercentage", generatedResume.getProgressPercentage());
            response.put("progressMessage", generatedResume.getProgressMessage());
            response.put("isCompleted", generatedResume.isCompleted());
            response.put("hasFailed", generatedResume.hasFailed());
            
            if (generatedResume.getErrorMessage() != null) {
                response.put("errorMessage", generatedResume.getErrorMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting generation progress: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get progress: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Show generated resume result
     */
    @GetMapping("/generation/result/{generatedResumeId}")
    public String showGenerationResult(@PathVariable Long generatedResumeId, Model model) {
        logger.info("Showing generation result for ID: {}", generatedResumeId);
        
        try {
            GeneratedResume generatedResume = resumeGenerationService.getGeneratedResume(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
            
            model.addAttribute("generatedResume", generatedResume);
            model.addAttribute("pageTitle", "Generated Resume - TargetCV");
            
            return "analysis/generation-result";
            
        } catch (Exception e) {
            logger.error("Error showing generation result: {}", generatedResumeId, e);
            model.addAttribute("error", "Error loading generated resume: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Download generated resume file
     */
    @GetMapping("/generation/download/{generatedResumeId}")
    public ResponseEntity<Resource> downloadGeneratedResume(@PathVariable Long generatedResumeId) {
        logger.info("Downloading generated resume ID: {}", generatedResumeId);
        
        try {
            GeneratedResume generatedResume = resumeGenerationService.getGeneratedResume(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
            
            if (generatedResume.getFilePath() == null) {
                throw new RuntimeException("Generated resume file not found");
            }
            
            Path filePath = Paths.get(generatedResume.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String filename = String.format("optimized_resume_%d.%s", 
                    generatedResumeId, generatedResume.getFileFormat());
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(generatedResume.getFileFormat()))
                    .body(resource);
            } else {
                throw new RuntimeException("Generated resume file not accessible");
            }
            
        } catch (Exception e) {
            logger.error("Error downloading generated resume: {}", generatedResumeId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Show generated resumes for a resume
     */
    @GetMapping("/generation/history/{resumeId}")
    public String showGenerationHistory(@PathVariable Long resumeId, Model model) {
        logger.info("Showing generation history for resume ID: {}", resumeId);
        
        try {
            Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            List<GeneratedResume> generatedResumes = resumeGenerationService.getGeneratedResumes(resume);
            
            model.addAttribute("resume", resume);
            model.addAttribute("generatedResumes", generatedResumes);
            model.addAttribute("pageTitle", "Generated Resumes - TargetCV");
            
            return "analysis/generation-history";
            
        } catch (Exception e) {
            logger.error("Error showing generation history: {}", resumeId, e);
            model.addAttribute("error", "Error loading generation history: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Delete generated resume
     */
    @PostMapping("/generation/delete/{generatedResumeId}")
    public String deleteGeneratedResume(@PathVariable Long generatedResumeId, RedirectAttributes redirectAttributes) {
        logger.info("Deleting generated resume ID: {}", generatedResumeId);
        
        try {
            GeneratedResume generatedResume = resumeGenerationService.getGeneratedResume(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
            
            Long resumeId = generatedResume.getOriginalResume().getId();
            resumeGenerationService.deleteGeneratedResume(generatedResumeId);
            
            redirectAttributes.addFlashAttribute("success", "Generated resume deleted successfully");
            return "redirect:/analysis/generation/history/" + resumeId;
            
        } catch (Exception e) {
            logger.error("Error deleting generated resume: {}", generatedResumeId, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting generated resume: " + e.getMessage());
            return "redirect:/analysis/generation/history";
        }
    }
    
    /**
     * Get content type for file format
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
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
