import nltk
import re
from collections import Counter
from sklearn.feature_extraction.text import TfidfVectorizer
import pandas as pd

# Download required NLTK data (run once)
try:
    nltk.data.find('tokenizers/punkt')
    nltk.data.find('corpora/stopwords')
    nltk.data.find('taggers/averaged_perceptron_tagger')
except LookupError:
    nltk.download('punkt')
    nltk.download('stopwords')
    nltk.download('averaged_perceptron_tagger')

from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize, sent_tokenize
from nltk.tag import pos_tag

def extract_keywords(text, num_keywords=20):
    """Extract important keywords from text using TF-IDF"""
    try:
        # Clean and preprocess text
        text = clean_text_for_analysis(text)
        
        # Use TF-IDF to extract keywords
        vectorizer = TfidfVectorizer(
            max_features=100,
            stop_words='english',
            ngram_range=(1, 2),  # Include both single words and bigrams
            min_df=1
        )
        
        tfidf_matrix = vectorizer.fit_transform([text])
        feature_names = vectorizer.get_feature_names_out()
        tfidf_scores = tfidf_matrix.toarray()[0]
        
        # Create keyword-score pairs and sort by score
        keyword_scores = list(zip(feature_names, tfidf_scores))
        keyword_scores.sort(key=lambda x: x[1], reverse=True)
        
        return [keyword for keyword, score in keyword_scores[:num_keywords]]
    
    except Exception as e:
        print(f"Error extracting keywords: {e}")
        return []

def analyze_job_description(job_text):
    """Analyze job description to extract key information"""
    analysis = {
        'keywords': [],
        'required_skills': [],
        'preferred_skills': [],
        'experience_level': '',
        'education_requirements': [],
        'key_responsibilities': []
    }
    
    try:
        # Extract keywords
        analysis['keywords'] = extract_keywords(job_text, 15)
        
        # Extract skills (look for common skill patterns)
        skills = extract_skills(job_text)
        analysis['required_skills'] = skills['required']
        analysis['preferred_skills'] = skills['preferred']
        
        # Determine experience level
        analysis['experience_level'] = determine_experience_level(job_text)
        
        # Extract education requirements
        analysis['education_requirements'] = extract_education_requirements(job_text)
        
        # Extract key responsibilities
        analysis['key_responsibilities'] = extract_responsibilities(job_text)
        
    except Exception as e:
        print(f"Error analyzing job description: {e}")
    
    return analysis

def extract_skills(text):
    """Extract technical and soft skills from job description"""
    # Common technical skills patterns
    tech_skills_patterns = [
        r'\b(?:Python|Java|JavaScript|C\+\+|C#|PHP|Ruby|Go|Rust|Swift|Kotlin)\b',
        r'\b(?:React|Angular|Vue|Node\.js|Django|Flask|Spring|Laravel)\b',
        r'\b(?:SQL|MySQL|PostgreSQL|MongoDB|Redis|Elasticsearch)\b',
        r'\b(?:AWS|Azure|GCP|Docker|Kubernetes|Jenkins|Git)\b',
        r'\b(?:Machine Learning|AI|Data Science|Analytics|Statistics)\b'
    ]
    
    # Soft skills patterns
    soft_skills_patterns = [
        r'\b(?:communication|leadership|teamwork|problem.solving|analytical)\b',
        r'\b(?:project.management|time.management|attention.to.detail)\b'
    ]
    
    required_skills = []
    preferred_skills = []
    
    # Look for required vs preferred sections
    text_lower = text.lower()
    
    # Split text into sections
    required_section = ""
    preferred_section = ""
    
    if "required" in text_lower:
        parts = re.split(r'\b(?:preferred|nice.to.have|bonus)\b', text_lower, flags=re.IGNORECASE)
        if len(parts) > 1:
            required_section = parts[0]
            preferred_section = parts[1]
        else:
            required_section = text_lower
    else:
        required_section = text_lower
    
    # Extract skills from each section
    for pattern in tech_skills_patterns + soft_skills_patterns:
        required_matches = re.findall(pattern, required_section, re.IGNORECASE)
        preferred_matches = re.findall(pattern, preferred_section, re.IGNORECASE)
        
        required_skills.extend(required_matches)
        preferred_skills.extend(preferred_matches)
    
    return {
        'required': list(set(required_skills)),
        'preferred': list(set(preferred_skills))
    }

def determine_experience_level(text):
    """Determine experience level from job description"""
    text_lower = text.lower()
    
    if any(term in text_lower for term in ['entry level', 'junior', '0-2 years', 'new grad']):
        return 'Entry Level'
    elif any(term in text_lower for term in ['senior', '5+ years', '7+ years', 'lead', 'principal']):
        return 'Senior Level'
    elif any(term in text_lower for term in ['mid level', '3-5 years', '2-4 years']):
        return 'Mid Level'
    else:
        return 'Not specified'

def extract_education_requirements(text):
    """Extract education requirements"""
    education_patterns = [
        r'\b(?:Bachelor|BS|BA|Master|MS|MA|PhD|Doctorate)\b.*?(?:degree|education)',
        r'\b(?:Computer Science|Engineering|Mathematics|Statistics)\b'
    ]
    
    requirements = []
    for pattern in education_patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        requirements.extend(matches)
    
    return list(set(requirements))

def extract_responsibilities(text):
    """Extract key responsibilities from job description"""
    # Look for bullet points or numbered lists
    responsibilities = []
    
    # Split by common bullet point indicators
    lines = text.split('\n')
    for line in lines:
        line = line.strip()
        if line.startswith(('•', '-', '*', '◦')) or re.match(r'^\d+\.', line):
            # Remove bullet point and clean up
            clean_line = re.sub(r'^[•\-*◦\d\.]\s*', '', line).strip()
            if len(clean_line) > 10:  # Filter out very short items
                responsibilities.append(clean_line)
    
    return responsibilities[:10]  # Limit to top 10

def clean_text_for_analysis(text):
    """Clean text for analysis"""
    # Remove extra whitespace and special characters
    text = re.sub(r'\s+', ' ', text)
    text = re.sub(r'[^\w\s\-\.]', ' ', text)
    return text.strip()

def calculate_text_similarity(text1, text2):
    """Calculate similarity between two texts using TF-IDF"""
    try:
        vectorizer = TfidfVectorizer(stop_words='english')
        tfidf_matrix = vectorizer.fit_transform([text1, text2])
        
        # Calculate cosine similarity
        from sklearn.metrics.pairwise import cosine_similarity
        similarity = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])[0][0]
        
        return similarity
    except:
        return 0.0
