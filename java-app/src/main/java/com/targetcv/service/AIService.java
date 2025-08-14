package com.targetcv.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for AI-powered text generation using OpenAI GPT
 */
@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    @Value("${targetcv.ai.deepseek.api-key}")
    private String apiKey;
    
    @Value("${targetcv.ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    
    @Value("${targetcv.ai.deepseek.model:deepseek-chat}")
    private String model;
    
    @Value("${targetcv.ai.deepseek.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${targetcv.ai.deepseek.temperature:0.7}")
    private Double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Check if DeepSeek API is properly configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.equals("your-deepseek-api-key-here");
    }
    
    /**
     * Generate optimized resume content using DeepSeek AI
     */
    public String generateOptimizedResume(String originalResumeText, String jobDescription, String jobTitle, String companyName) {
        try {
            String prompt = buildResumeOptimizationPrompt(originalResumeText, jobDescription, jobTitle, companyName);
            
            // Create request payload for DeepSeek API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", getSystemPrompt()),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", false);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call to DeepSeek
            String url = baseUrl + "/chat/completions";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");
            
            if (choices != null && choices.size() > 0) {
                String generatedContent = choices.get(0).get("message").get("content").asText();
                logger.info("Successfully generated optimized resume content using DeepSeek model: {}", model);
                return generatedContent;
            } else {
                throw new RuntimeException("No response received from DeepSeek API");
            }
            
        } catch (Exception e) {
            logger.error("Error generating optimized resume with DeepSeek: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate optimized resume: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build the prompt for resume optimization
     */
    private String buildResumeOptimizationPrompt(String originalResumeText, String jobDescription, String jobTitle, String companyName) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Please optimize the following resume for the specific job opportunity:\n\n");
        
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            prompt.append("**Job Title:** ").append(jobTitle).append("\n");
        }
        
        if (companyName != null && !companyName.trim().isEmpty()) {
            prompt.append("**Company:** ").append(companyName).append("\n");
        }
        
        prompt.append("\n**Job Description:**\n")
               .append(jobDescription)
               .append("\n\n**Original Resume:**\n")
               .append(originalResumeText)
               .append("\n\n**Instructions:**\n")
               .append("1. Rewrite the resume to better match the job requirements\n")
               .append("2. Incorporate relevant keywords from the job description naturally\n")
               .append("3. Emphasize skills and experiences that align with the role\n")
               .append("4. Maintain the original structure but improve content\n")
               .append("5. Use strong action verbs and quantifiable achievements\n")
               .append("6. Ensure the resume remains truthful to the original content\n")
               .append("7. Format the output as clean, professional resume text\n\n")
               .append("Please provide only the optimized resume content without any additional commentary.");
        
        return prompt.toString();
    }
    
    /**
     * Get the system prompt for resume optimization
     */
    private String getSystemPrompt() {
        return "You are an expert resume writer and career coach with extensive experience in optimizing resumes for specific job applications. " +
               "Your task is to rewrite resumes to better match job requirements while maintaining truthfulness and professionalism. " +
               "Focus on keyword optimization, skill emphasis, and content restructuring to improve the candidate's chances of getting interviews. " +
               "Always maintain the factual accuracy of the original resume while presenting the information in the most compelling way for the target role.";
    }
    
    /**
     * Generate a summary of changes made to the resume
     */
    public String generateChangeSummary(String originalResume, String optimizedResume, String jobTitle) {
        try {
            String prompt = String.format(
                "Compare the original resume with the optimized version and provide a brief summary of the key changes made for the %s position:\n\n" +
                "**Original Resume:**\n%s\n\n" +
                "**Optimized Resume:**\n%s\n\n" +
                "Please provide a concise bullet-point summary of the main improvements and changes made.",
                jobTitle != null ? jobTitle : "target job",
                originalResume.length() > 1000 ? originalResume.substring(0, 1000) + "..." : originalResume,
                optimizedResume.length() > 1000 ? optimizedResume.substring(0, 1000) + "..." : optimizedResume
            );
            
            // Create request payload for DeepSeek API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", "You are a resume analysis expert. Provide clear, concise summaries of resume changes."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.3);
            requestBody.put("stream", false);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make API call to DeepSeek
            String url = baseUrl + "/chat/completions";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode choices = responseJson.get("choices");
            
            if (choices != null && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
            
        } catch (Exception e) {
            logger.warn("Could not generate change summary with DeepSeek: {}", e.getMessage());
        }
        
        return "Resume optimized for the target position with improved keyword matching and content emphasis.";
    }
    

}
