package com.targetcv.service;

import com.targetcv.model.GeneratedResume;
import com.targetcv.model.JobAnalysis;
import com.targetcv.model.Resume;
import com.targetcv.repository.GeneratedResumeRepository;
import com.targetcv.service.DocumentGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for generating optimized resumes using AI
 */
@Service
public class ResumeGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResumeGenerationService.class);
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private DocumentGenerationService documentGenerationService;
    
    @Autowired
    private GeneratedResumeRepository generatedResumeRepository;
    
    @Value("${targetcv.ai.generation.output-directory:generated/}")
    private String outputDirectory;
    
    @Value("${targetcv.ai.generation.default-format:docx}")
    private String defaultFormat;
    
    @Value("${targetcv.ai.deepseek.model:deepseek-chat}")
    private String aiModel;
    
    /**
     * Generate an optimized resume using AI (synchronous for immediate response)
     */
    @Transactional
    public GeneratedResume startResumeGeneration(JobAnalysis jobAnalysis) {
        GeneratedResume generatedResume = new GeneratedResume();
        generatedResume.setOriginalResume(jobAnalysis.getResume());
        generatedResume.setJobAnalysis(jobAnalysis);
        generatedResume.setStatus(GeneratedResume.GenerationStatus.PENDING);
        generatedResume.setProgressPercentage(0);
        generatedResume.setProgressMessage("Starting generation...");
        generatedResume.setEstimatedCompletionTime(LocalDateTime.now().plusSeconds(30));
        
        // Save initial state
        generatedResume = generatedResumeRepository.save(generatedResume);
        
        // Start async generation
        generateOptimizedResumeAsync(generatedResume.getId());
        
        return generatedResume;
    }
    
    /**
     * Generate an optimized resume using AI (asynchronous with progress tracking)
     */
    @Async
    @Transactional
    public void generateOptimizedResumeAsync(Long generatedResumeId) {
        GeneratedResume generatedResume = generatedResumeRepository.findById(generatedResumeId)
                .orElseThrow(() -> new RuntimeException("Generated resume not found"));
        
        JobAnalysis jobAnalysis = generatedResume.getJobAnalysis();
        logger.info("Starting async resume generation for job analysis ID: {}", jobAnalysis.getId());
        
        try {
            // Update progress: Starting
            updateProgress(generatedResume, 10, "Analyzing job requirements...", GeneratedResume.GenerationStatus.ANALYZING);
            Thread.sleep(1000); // Simulate analysis time
            
            // Update progress: Generating
            updateProgress(generatedResume, 50, "Generating optimized content...", GeneratedResume.GenerationStatus.GENERATING);
            
            // Check if AI service is configured
            if (!aiService.isConfigured()) {
                logger.warn("DeepSeek API key not configured, using fallback content generation");
                // Use fallback content generation for testing
                String fallbackContent = generateFallbackContent(jobAnalysis);
                generatedResume.setGeneratedContent(fallbackContent);
            } else {
                // Generate optimized content using AI
                String optimizedContent = generateOptimizedContent(jobAnalysis);
                generatedResume.setGeneratedContent(optimizedContent);
            }
            
            // Update progress: Formatting
            updateProgress(generatedResume, 80, "Formatting document...", GeneratedResume.GenerationStatus.FORMATTING);
            Thread.sleep(500);
            
            // Create the prompt for reference
            String prompt = buildGenerationPrompt(jobAnalysis);
            generatedResume.setGenerationPrompt(prompt);
            
            // Set file format
            generatedResume.setFileFormat(defaultFormat);
            
            // Update progress: Completed
            updateProgress(generatedResume, 100, "Generation completed!", GeneratedResume.GenerationStatus.COMPLETED);
            
            logger.info("Successfully generated resume for job analysis ID: {}", jobAnalysis.getId());
            
        } catch (Exception e) {
            logger.error("Error generating resume for job analysis ID: {}", jobAnalysis.getId(), e);
            generatedResume.setStatus(GeneratedResume.GenerationStatus.FAILED);
            generatedResume.setErrorMessage(e.getMessage());
            generatedResume.setProgressMessage("Generation failed: " + e.getMessage());
            generatedResumeRepository.save(generatedResume);
        }
    }
    
    /**
     * Generate optimized content using AI service
     */
    private String generateOptimizedContent(JobAnalysis jobAnalysis) {
        if (!aiService.isConfigured()) {
            throw new IllegalStateException("AI service is not properly configured. Please set OPENAI_API_KEY environment variable.");
        }
        
        Resume originalResume = jobAnalysis.getResume();
        String originalContent = originalResume.getExtractedText();
        String jobDescription = jobAnalysis.getJobDescription();
        String jobTitle = jobAnalysis.getJobTitle();
        String companyName = jobAnalysis.getCompanyName();
        
        return aiService.generateOptimizedResume(originalContent, jobDescription, jobTitle, companyName);
    }
    
    /**
     * Update progress for a generated resume
     */
    private void updateProgress(GeneratedResume generatedResume, int percentage, String message, GeneratedResume.GenerationStatus status) {
        generatedResume.setProgressPercentage(percentage);
        generatedResume.setProgressMessage(message);
        generatedResume.setStatus(status);
        generatedResumeRepository.save(generatedResume);
        logger.info("Progress updated: {}% - {}", percentage, message);
    }
    
    /**
     * Generate fallback content when AI service is not available
     */
    private String generateFallbackContent(JobAnalysis jobAnalysis) {
        Resume originalResume = jobAnalysis.getResume();
        String jobTitle = jobAnalysis.getJobTitle() != null ? jobAnalysis.getJobTitle() : "Target Position";
        String companyName = jobAnalysis.getCompanyName() != null ? jobAnalysis.getCompanyName() : "Target Company";
        
        StringBuilder fallbackContent = new StringBuilder();
        fallbackContent.append("OPTIMIZED RESUME FOR: ").append(jobTitle);
        if (companyName != null) {
            fallbackContent.append(" at ").append(companyName);
        }
        fallbackContent.append("\n\n");
        fallbackContent.append("[Note: This is a demo version. Set DEEPSEEK_API_KEY environment variable to enable AI-powered optimization]\n\n");
        fallbackContent.append("ORIGINAL RESUME CONTENT:\n");
        fallbackContent.append("=========================\n\n");
        fallbackContent.append(originalResume.getExtractedText());
        fallbackContent.append("\n\n=========================\n");
        fallbackContent.append("OPTIMIZATION SUGGESTIONS:\n");
        fallbackContent.append("• Add relevant keywords from the job description\n");
        fallbackContent.append("• Emphasize skills that match the position requirements\n");
        fallbackContent.append("• Quantify achievements with specific numbers and results\n");
        fallbackContent.append("• Tailor the professional summary to the target role\n");
        
        return fallbackContent.toString();
    }
    
    /**
     * Generate document file from optimized content
     */
    private String generateDocumentFile(GeneratedResume generatedResume, String format) throws IOException {
        // Ensure output directory exists
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        
        // Generate filename
        String filename = String.format("resume_%d_%s.%s", 
            generatedResume.getId(),
            LocalDateTime.now().toString().replaceAll("[:\\-.]", ""),
            format);
        
        String filePath = outputPath.resolve(filename).toString();
        
        // Generate document based on format
        if ("pdf".equalsIgnoreCase(format)) {
            documentGenerationService.generatePDF(generatedResume.getGeneratedContent(), filePath);
        } else if ("docx".equalsIgnoreCase(format)) {
            documentGenerationService.generateDOCX(generatedResume.getGeneratedContent(), filePath);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        
        return filePath;
    }
    
    /**
     * Build generation prompt for reference
     */
    private String buildGenerationPrompt(JobAnalysis jobAnalysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Resume optimization request:\n");
        
        if (jobAnalysis.getJobTitle() != null) {
            prompt.append("Job Title: ").append(jobAnalysis.getJobTitle()).append("\n");
        }
        
        if (jobAnalysis.getCompanyName() != null) {
            prompt.append("Company: ").append(jobAnalysis.getCompanyName()).append("\n");
        }
        
        prompt.append("Original Resume: ").append(jobAnalysis.getResume().getOriginalFilename()).append("\n");
        prompt.append("Analysis Score: ").append(jobAnalysis.getOverallMatchScore()).append("\n");
        prompt.append("Generated at: ").append(LocalDateTime.now()).append("\n");
        
        return prompt.toString();
    }
    
    /**
     * Get all generated resumes for an original resume
     */
    public List<GeneratedResume> getGeneratedResumes(Resume originalResume) {
        return generatedResumeRepository.findByOriginalResumeOrderByCreatedAtDesc(originalResume);
    }
    
    /**
     * Get generated resume by ID
     */
    public Optional<GeneratedResume> getGeneratedResume(Long id) {
        return generatedResumeRepository.findById(id);
    }
    
    /**
     * Delete generated resume and associated file
     */
    public void deleteGeneratedResume(Long id) {
        Optional<GeneratedResume> generatedResume = generatedResumeRepository.findById(id);
        if (generatedResume.isPresent()) {
            GeneratedResume resume = generatedResume.get();
            
            // Delete file if exists
            if (resume.getFilePath() != null) {
                try {
                    Path filePath = Paths.get(resume.getFilePath());
                    Files.deleteIfExists(filePath);
                    logger.info("Deleted generated resume file: {}", resume.getFilePath());
                } catch (IOException e) {
                    logger.warn("Could not delete generated resume file: {}", resume.getFilePath(), e);
                }
            }
            
            // Delete database record
            generatedResumeRepository.delete(resume);
            logger.info("Deleted generated resume record with ID: {}", id);
        }
    }
    
    /**
     * Regenerate resume with different format
     */
    public GeneratedResume regenerateWithFormat(Long generatedResumeId, String format) throws IOException {
        Optional<GeneratedResume> existing = generatedResumeRepository.findById(generatedResumeId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Generated resume not found with ID: " + generatedResumeId);
        }
        
        GeneratedResume generatedResume = existing.get();
        
        // Generate new document file with different format
        String newFilePath = generateDocumentFile(generatedResume, format);
        
        // Update record
        generatedResume.setFilePath(newFilePath);
        generatedResume.setFileFormat(format);
        
        // Update file size
        Path file = Paths.get(newFilePath);
        if (Files.exists(file)) {
            generatedResume.setFileSize(Files.size(file));
        }
        
        return generatedResumeRepository.save(generatedResume);
    }
    
    /**
     * Get generation statistics
     */
    public GenerationStats getGenerationStats() {
        long totalGenerated = generatedResumeRepository.count();
        long completed = generatedResumeRepository.countByStatus(GeneratedResume.GenerationStatus.COMPLETED);
        long failed = generatedResumeRepository.countByStatus(GeneratedResume.GenerationStatus.FAILED);
        long inProgress = generatedResumeRepository.countByStatus(GeneratedResume.GenerationStatus.IN_PROGRESS);
        
        return new GenerationStats(totalGenerated, completed, failed, inProgress);
    }
    
    /**
     * Statistics for resume generation
     */
    public static class GenerationStats {
        private final long totalGenerated;
        private final long completed;
        private final long failed;
        private final long inProgress;
        
        public GenerationStats(long totalGenerated, long completed, long failed, long inProgress) {
            this.totalGenerated = totalGenerated;
            this.completed = completed;
            this.failed = failed;
            this.inProgress = inProgress;
        }
        
        // Getters
        public long getTotalGenerated() { return totalGenerated; }
        public long getCompleted() { return completed; }
        public long getFailed() { return failed; }
        public long getInProgress() { return inProgress; }
        public double getSuccessRate() { 
            return totalGenerated > 0 ? (double) completed / totalGenerated * 100 : 0; 
        }
    }
}
