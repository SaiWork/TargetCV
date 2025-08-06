package com.targetcv.controller;

import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import com.targetcv.repository.JobAnalysisRepository;
import com.targetcv.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for home page and dashboard functionality
 */
@Controller
@RequestMapping("/")
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    
    @Autowired
    public HomeController(ResumeRepository resumeRepository, 
                         JobAnalysisRepository jobAnalysisRepository) {
        this.resumeRepository = resumeRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
    }
    
    /**
     * Home page with application overview
     */
    @GetMapping
    public String home(Model model) {
        logger.info("Accessing home page");
        
        try {
            // Initialize default values
            long totalResumes = 0;
            long totalAnalyses = 0;
            List<Resume> recentResumes = new ArrayList<>();
            List<JobAnalysis> recentAnalyses = new ArrayList<>();
            
            // Try to get statistics from database (gracefully handle if tables don't exist yet)
            try {
                totalResumes = resumeRepository.count();
                totalAnalyses = jobAnalysisRepository.count();
                
                // Get recent resumes and analyses
                recentResumes = resumeRepository.findRecentResumes(
                    LocalDateTime.now().minusDays(30)
                );
                
                recentAnalyses = jobAnalysisRepository.findRecentAnalyses(
                    LocalDateTime.now().minusDays(7)
                );
                
                logger.info("Successfully loaded dashboard data: {} resumes, {} analyses", 
                          totalResumes, totalAnalyses);
                          
            } catch (Exception dbException) {
                logger.warn("Database not ready yet, using default values: {}", dbException.getMessage());
                // Continue with default values - this is normal on first startup
            }
            
            // Add data to model
            model.addAttribute("totalResumes", totalResumes);
            model.addAttribute("totalAnalyses", totalAnalyses);
            model.addAttribute("recentResumes", recentResumes);
            model.addAttribute("recentAnalyses", recentAnalyses);
            model.addAttribute("pageTitle", "TargetCV - Resume Targeting Tool");
            model.addAttribute("welcomeMessage", "Welcome to TargetCV - Your Professional Resume Targeting Tool");
            
            return "index";
            
        } catch (Exception e) {
            logger.error("Error loading home page", e);
            // Fallback to a simple welcome page
            model.addAttribute("pageTitle", "TargetCV - Resume Targeting Tool");
            model.addAttribute("welcomeMessage", "Welcome to TargetCV");
            model.addAttribute("totalResumes", 0);
            model.addAttribute("totalAnalyses", 0);
            model.addAttribute("recentResumes", new ArrayList<>());
            model.addAttribute("recentAnalyses", new ArrayList<>());
            return "index";
        }
    }
    
    /**
     * About page
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About - TargetCV");
        return "about";
    }
    
    /**
     * Help/FAQ page
     */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("pageTitle", "Help - TargetCV");
        return "help";
    }
}
