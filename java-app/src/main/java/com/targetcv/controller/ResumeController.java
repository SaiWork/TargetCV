package com.targetcv.controller;

import com.targetcv.model.Resume;
import com.targetcv.repository.ResumeRepository;
import com.targetcv.service.DocumentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for resume upload and management functionality
 */
@Controller
@RequestMapping("/resume")
public class ResumeController {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);
    
    private final DocumentProcessingService documentProcessingService;
    private final ResumeRepository resumeRepository;
    
    @Autowired
    public ResumeController(DocumentProcessingService documentProcessingService,
                           ResumeRepository resumeRepository) {
        this.documentProcessingService = documentProcessingService;
        this.resumeRepository = resumeRepository;
    }
    
    /**
     * Show resume upload form
     */
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        logger.info("Displaying resume upload form");
        model.addAttribute("pageTitle", "Upload Resume - TargetCV");
        return "resume/upload";
    }
    
    /**
     * Handle resume file upload
     */
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        logger.info("Processing resume upload: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/resume/upload";
            }
            
            // Save file and extract text
            DocumentProcessingService.FileInfo fileInfo = documentProcessingService.saveUploadedFile(file);
            String extractedText = documentProcessingService.extractTextFromFile(fileInfo.getFilePath());
            
            // Create and save resume entity
            Resume resume = new Resume(
                fileInfo.getFilename(),
                fileInfo.getOriginalFilename(),
                fileInfo.getFilePath(),
                fileInfo.getFileSize(),
                fileInfo.getContentType()
            );
            resume.setExtractedText(extractedText);
            
            Resume savedResume = resumeRepository.save(resume);
            logger.info("Resume saved successfully with ID: {}", savedResume.getId());
            
            redirectAttributes.addFlashAttribute("success", 
                "Resume uploaded successfully! You can now analyze it against job descriptions.");
            
            return "redirect:/analysis/analyze/" + savedResume.getId();
            
        } catch (Exception e) {
            logger.error("Error uploading resume: {}", file.getOriginalFilename(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Error uploading file: " + e.getMessage());
            return "redirect:/resume/upload";
        }
    }
    
    /**
     * List all uploaded resumes
     */
    @GetMapping("/list")
    public String listResumes(Model model) {
        logger.info("Displaying resume list");
        
        try {
            List<Resume> resumes = resumeRepository.findAllByOrderByCreatedAtDesc();
            model.addAttribute("resumes", resumes);
            model.addAttribute("pageTitle", "My Resumes - TargetCV");
            return "resume/list";
            
        } catch (Exception e) {
            logger.error("Error loading resume list", e);
            model.addAttribute("error", "Unable to load resumes");
            return "error";
        }
    }
    
    /**
     * View resume details
     */
    @GetMapping("/view/{id}")
    public String viewResume(@PathVariable Long id, Model model) {
        logger.info("Viewing resume with ID: {}", id);
        
        try {
            Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            model.addAttribute("resume", resume);
            model.addAttribute("pageTitle", "Resume Details - TargetCV");
            return "resume/view";
            
        } catch (Exception e) {
            logger.error("Error viewing resume with ID: {}", id, e);
            model.addAttribute("error", "Resume not found");
            return "error";
        }
    }
    
    /**
     * Delete resume
     */
    @PostMapping("/delete/{id}")
    public String deleteResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Deleting resume with ID: {}", id);
        
        try {
            Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
            
            // Delete file from disk
            documentProcessingService.deleteFile(resume.getFilePath());
            
            // Delete from database
            resumeRepository.delete(resume);
            
            redirectAttributes.addFlashAttribute("success", "Resume deleted successfully");
            logger.info("Resume deleted successfully: {}", id);
            
        } catch (Exception e) {
            logger.error("Error deleting resume with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting resume: " + e.getMessage());
        }
        
        return "redirect:/resume/list";
    }
    
    /**
     * Download resume file
     */
    @GetMapping("/download/{id}")
    public String downloadResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // This would typically return a ResponseEntity<Resource> for file download
        // For now, redirect back with a message
        redirectAttributes.addFlashAttribute("info", "Download functionality will be implemented soon");
        return "redirect:/resume/view/" + id;
    }
}
