package com.targetcv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for text analysis, keyword extraction, and content matching
 */
@Service
public class TextAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisService.class);
    
    @Value("${targetcv.analysis.max-keywords:20}")
    private int maxKeywords;
    
    @Value("${targetcv.analysis.similarity-threshold:0.6}")
    private double similarityThreshold;
    
    // Common stop words to filter out
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is", "it",
        "its", "of", "on", "that", "the", "to", "was", "were", "will", "with", "this", "but",
        "they", "have", "had", "what", "said", "each", "which", "she", "do", "how", "their", "if",
        "up", "out", "many", "then", "them", "these", "so", "some", "her", "would", "make", "like",
        "into", "him", "time", "two", "more", "go", "no", "way", "could", "my", "than", "first",
        "been", "call", "who", "oil", "sit", "now", "find", "down", "day", "did", "get", "come",
        "made", "may", "part"
    );
    
    // Technical skills patterns
    private static final List<Pattern> TECH_SKILL_PATTERNS = Arrays.asList(
        Pattern.compile("\\b(?i)(java|python|javascript|c\\+\\+|c#|php|ruby|go|rust|swift|kotlin)\\b"),
        Pattern.compile("\\b(?i)(react|angular|vue|node\\.?js|django|flask|spring|laravel)\\b"),
        Pattern.compile("\\b(?i)(sql|mysql|postgresql|mongodb|redis|elasticsearch|oracle)\\b"),
        Pattern.compile("\\b(?i)(aws|azure|gcp|docker|kubernetes|jenkins|git|github)\\b"),
        Pattern.compile("\\b(?i)(machine\\s+learning|ai|data\\s+science|analytics|statistics)\\b"),
        Pattern.compile("\\b(?i)(html|css|bootstrap|tailwind|sass|less)\\b"),
        Pattern.compile("\\b(?i)(rest|api|graphql|microservices|agile|scrum|devops)\\b")
    );
    
    // Soft skills patterns
    private static final List<Pattern> SOFT_SKILL_PATTERNS = Arrays.asList(
        Pattern.compile("\\b(?i)(communication|leadership|teamwork|problem[\\s-]solving|analytical)\\b"),
        Pattern.compile("\\b(?i)(project\\s+management|time\\s+management|attention\\s+to\\s+detail)\\b"),
        Pattern.compile("\\b(?i)(creative|innovative|adaptable|flexible|collaborative)\\b")
    );
    
    /**
     * Extract keywords from text using frequency analysis
     */
    public List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Clean and tokenize text
        String cleanText = text.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s\\-\\.]", " ")
            .replaceAll("\\s+", " ")
            .trim();
        
        // Split into words and filter
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = cleanText.split("\\s+");
        
        for (String word : words) {
            word = word.trim();
            if (word.length() > 2 && !STOP_WORDS.contains(word) && !word.matches("\\d+")) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // Sort by frequency and return top keywords
        return wordFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(maxKeywords)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Extract technical and soft skills from text
     */
    public SkillAnalysis extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SkillAnalysis(new ArrayList<>(), new ArrayList<>());
        }
        
        Set<String> techSkills = new HashSet<>();
        Set<String> softSkills = new HashSet<>();
        
        // Extract technical skills
        for (Pattern pattern : TECH_SKILL_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                techSkills.add(matcher.group().toLowerCase());
            }
        }
        
        // Extract soft skills
        for (Pattern pattern : SOFT_SKILL_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                softSkills.add(matcher.group().toLowerCase());
            }
        }
        
        return new SkillAnalysis(
            new ArrayList<>(techSkills),
            new ArrayList<>(softSkills)
        );
    }
    
    /**
     * Determine experience level from job description
     */
    public String determineExperienceLevel(String jobDescription) {
        if (jobDescription == null) {
            return "Not specified";
        }
        
        String text = jobDescription.toLowerCase();
        
        // Entry level indicators
        if (text.contains("entry level") || text.contains("junior") || 
            text.contains("0-2 years") || text.contains("new grad") ||
            text.contains("recent graduate")) {
            return "Entry Level";
        }
        
        // Senior level indicators
        if (text.contains("senior") || text.contains("5+ years") || 
            text.contains("7+ years") || text.contains("lead") ||
            text.contains("principal") || text.contains("architect")) {
            return "Senior Level";
        }
        
        // Mid level indicators
        if (text.contains("mid level") || text.contains("3-5 years") || 
            text.contains("2-4 years") || text.contains("experienced")) {
            return "Mid Level";
        }
        
        return "Not specified";
    }
    
    /**
     * Calculate text similarity using Jaccard similarity
     */
    public double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return 0.0;
        }
        
        Set<String> words1 = getWordSet(text1);
        Set<String> words2 = getWordSet(text2);
        
        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Extract education requirements from job description
     */
    public List<String> extractEducationRequirements(String jobDescription) {
        if (jobDescription == null) {
            return new ArrayList<>();
        }
        
        List<String> requirements = new ArrayList<>();
        
        // Education patterns
        Pattern educationPattern = Pattern.compile(
            "\\b(?i)(bachelor|bs|ba|master|ms|ma|phd|doctorate)\\b.*?(?i)(degree|education)",
            Pattern.CASE_INSENSITIVE
        );
        
        Pattern fieldPattern = Pattern.compile(
            "\\b(?i)(computer\\s+science|engineering|mathematics|statistics|business|finance)\\b",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher educationMatcher = educationPattern.matcher(jobDescription);
        while (educationMatcher.find()) {
            requirements.add(educationMatcher.group().trim());
        }
        
        Matcher fieldMatcher = fieldPattern.matcher(jobDescription);
        while (fieldMatcher.find()) {
            requirements.add(fieldMatcher.group().trim());
        }
        
        return requirements.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Extract key responsibilities from job description
     */
    public List<String> extractResponsibilities(String jobDescription) {
        if (jobDescription == null) {
            return new ArrayList<>();
        }
        
        List<String> responsibilities = new ArrayList<>();
        String[] lines = jobDescription.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            // Look for bullet points or numbered lists
            if (line.matches("^[•\\-*◦]\\s+.*") || line.matches("^\\d+\\.\\s+.*")) {
                String cleanLine = line.replaceAll("^[•\\-*◦\\d\\.\\s]+", "").trim();
                if (cleanLine.length() > 10) { // Filter out very short items
                    responsibilities.add(cleanLine);
                }
            }
        }
        
        return responsibilities.stream().limit(10).collect(Collectors.toList());
    }
    
    /**
     * Convert text to word set for similarity calculation
     */
    private Set<String> getWordSet(String text) {
        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+"))
                .filter(word -> word.length() > 2 && !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
    
    /**
     * Inner class to hold skill analysis results
     */
    public static class SkillAnalysis {
        private final List<String> technicalSkills;
        private final List<String> softSkills;
        
        public SkillAnalysis(List<String> technicalSkills, List<String> softSkills) {
            this.technicalSkills = technicalSkills;
            this.softSkills = softSkills;
        }
        
        public List<String> getTechnicalSkills() { return technicalSkills; }
        public List<String> getSoftSkills() { return softSkills; }
        public List<String> getAllSkills() {
            List<String> allSkills = new ArrayList<>(technicalSkills);
            allSkills.addAll(softSkills);
            return allSkills;
        }
    }
}
