package com.targetcv.controller;

import com.targetcv.model.SavedJobDescription;
import com.targetcv.model.Resume;
import com.targetcv.model.JobAnalysis;
import com.targetcv.service.SavedJobDescriptionService;
import com.targetcv.service.ResumeMatchingService;
import com.targetcv.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for managing saved job descriptions
 */
@Controller
@RequestMapping("/jobs")
public class SavedJobController {
    
    private static final Logger logger = LoggerFactory.getLogger(SavedJobController.class);
    
    private final SavedJobDescriptionService savedJobService;
    private final ResumeRepository resumeRepository;
    private final ResumeMatchingService resumeMatchingService;
    
    @Autowired
    public SavedJobController(SavedJobDescriptionService savedJobService,
                            ResumeRepository resumeRepository,
                            ResumeMatchingService resumeMatchingService) {
        this.savedJobService = savedJobService;
        this.resumeRepository = resumeRepository;
        this.resumeMatchingService = resumeMatchingService;
    }
    
    /**
     * Show all saved job descriptions
     */
    @GetMapping
    public String showSavedJobs(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String priority) {
        try {
            List<SavedJobDescription> jobs;
            
            if (search != null || status != null || priority != null) {
                // Perform search
                SavedJobDescription.ApplicationStatus statusEnum = null;
                if (status != null && !status.isEmpty()) {
                    statusEnum = SavedJobDescription.ApplicationStatus.valueOf(status.toUpperCase());
                }
                
                SavedJobDescription.Priority priorityEnum = null;
                if (priority != null && !priority.isEmpty()) {
                    priorityEnum = SavedJobDescription.Priority.valueOf(priority.toUpperCase());
                }
                
                jobs = savedJobService.searchJobs(search, search, statusEnum, priorityEnum);
            } else {
                jobs = savedJobService.getAllSavedJobs();
            }
            
            model.addAttribute("jobs", jobs);
            model.addAttribute("statistics", savedJobService.getJobStatistics());
            model.addAttribute("upcomingDeadlines", savedJobService.getUpcomingDeadlines());
            model.addAttribute("search", search);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedPriority", priority);
            
            return "jobs/list";
            
        } catch (Exception e) {
            logger.error("Error loading saved jobs: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading saved jobs: " + e.getMessage());
            return "jobs/list";
        }
    }
    
    /**
     * Show form to create new saved job description
     */
    @GetMapping("/new")
    public String showNewJobForm(Model model) {
        model.addAttribute("job", new SavedJobDescription());
        model.addAttribute("isEdit", false);
        return "jobs/form";
    }
    
    /**
     * Show form to edit existing job description
     */
    @GetMapping("/edit/{id}")
    public String showEditJobForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<SavedJobDescription> jobOpt = savedJobService.getJobById(id);
            if (jobOpt.isPresent()) {
                model.addAttribute("job", jobOpt.get());
                model.addAttribute("isEdit", true);
                return "jobs/form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Job description not found");
                return "redirect:/jobs";
            }
        } catch (Exception e) {
            logger.error("Error loading job for edit: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error loading job: " + e.getMessage());
            return "redirect:/jobs";
        }
    }
    
    /**
     * View detailed job description
     */
    @GetMapping("/view/{id}")
    public String viewJobDescription(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<SavedJobDescription> jobOpt = savedJobService.getJobById(id);
            if (jobOpt.isPresent()) {
                model.addAttribute("job", jobOpt.get());
                return "jobs/view";
            } else {
                redirectAttributes.addFlashAttribute("error", "Job description not found");
                return "redirect:/jobs";
            }
        } catch (Exception e) {
            logger.error("Error viewing job: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error viewing job: " + e.getMessage());
            return "redirect:/jobs";
        }
    }
    
    /**
     * Save job description (create or update)
     */
    @PostMapping("/save")
    public String saveJobDescription(@ModelAttribute SavedJobDescription job, 
                                   RedirectAttributes redirectAttributes) {
        try {
            SavedJobDescription savedJob = savedJobService.saveJobDescription(job);
            redirectAttributes.addFlashAttribute("success", 
                "Job description saved successfully: " + savedJob.getTitle());
            return "redirect:/jobs/view/" + savedJob.getId();
            
        } catch (Exception e) {
            logger.error("Error saving job description: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error saving job: " + e.getMessage());
            return "redirect:/jobs/new";
        }
    }
    
    /**
     * Quick save job from analysis form (AJAX endpoint)
     */
    @PostMapping("/quick-save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickSaveJob(@RequestParam String jobTitle,
                                                          @RequestParam String companyName,
                                                          @RequestParam String jobDescription) {
        try {
            SavedJobDescription savedJob = savedJobService.saveFromAnalysisData(jobTitle, companyName, jobDescription);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job description saved successfully!");
            response.put("jobId", savedJob.getId());
            response.put("jobTitle", savedJob.getTitle());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error quick-saving job: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error saving job: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Load saved job description for analysis form (AJAX endpoint)
     */
    @GetMapping("/load/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loadJobForAnalysis(@PathVariable Long id) {
        try {
            Optional<SavedJobDescription> jobOpt = savedJobService.getJobById(id);
            if (jobOpt.isPresent()) {
                SavedJobDescription job = jobOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("jobTitle", job.getTitle());
                response.put("companyName", job.getCompanyName());
                response.put("jobDescription", job.getJobDescription());
                response.put("location", job.getLocation());
                response.put("salaryRange", job.getSalaryRange());
                response.put("employmentType", job.getEmploymentType());
                response.put("remoteOption", job.getRemoteOption());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Job description not found");
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error loading job for analysis: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error loading job: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Update application status
     */
    @PostMapping("/status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, 
                                                          @RequestParam String status) {
        try {
            SavedJobDescription.ApplicationStatus statusEnum = 
                SavedJobDescription.ApplicationStatus.valueOf(status.toUpperCase());
            
            SavedJobDescription updatedJob = savedJobService.updateApplicationStatus(id, statusEnum);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("newStatus", updatedJob.getApplicationStatus().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating job status: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating status: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Delete saved job description
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable Long id) {
        try {
            savedJobService.deleteJobDescription(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job description deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting job: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting job: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Analyze saved job with selected resume (Job-First Approach)
     */
    @PostMapping("/analyze-with-resume")
    public String analyzeJobWithResume(@RequestParam Long jobId,
                                     @RequestParam Long resumeId,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Get the saved job description
            Optional<SavedJobDescription> jobOpt = savedJobService.getJobById(jobId);
            if (!jobOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Job description not found");
                return "redirect:/jobs";
            }
            
            // Get the resume
            Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
            if (!resumeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Resume not found");
                return "redirect:/jobs";
            }
            
            SavedJobDescription job = jobOpt.get();
            Resume resume = resumeOpt.get();
            
            // Perform the analysis using the resume matching service
            JobAnalysis analysisResult = resumeMatchingService.analyzeResumeMatch(
                resume, 
                job.getJobDescription()
            );
            
            // Set additional job information
            analysisResult.setJobTitle(job.getTitle());
            analysisResult.setCompanyName(job.getCompanyName());
            
            logger.info("Job-first analysis completed for job ID {} with resume ID {}", jobId, resumeId);
            
            // Redirect to analysis results
            redirectAttributes.addFlashAttribute("success", 
                "Analysis completed for \"" + job.getTitle() + "\" with resume \"" + resume.getOriginalFilename() + "\"");
            return "redirect:/analysis/results/" + analysisResult.getId();
            
        } catch (Exception e) {
            logger.error("Error analyzing job with resume: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error performing analysis: " + e.getMessage());
            return "redirect:/jobs";
        }
    }
    
    /**
     * Get saved jobs list for dropdown/selection (AJAX endpoint)
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<SavedJobDescription>> getJobsList() {
        try {
            List<SavedJobDescription> jobs = savedJobService.getAllSavedJobs();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            logger.error("Error getting jobs list: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
