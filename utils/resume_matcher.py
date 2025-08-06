from .text_analyzer import extract_keywords, calculate_text_similarity, extract_skills
import re
from collections import Counter

def match_resume_to_job(resume_text, job_description):
    """Match resume content to job requirements and provide recommendations"""
    
    results = {
        'overall_match_score': 0.0,
        'keyword_matches': [],
        'missing_keywords': [],
        'skill_matches': [],
        'missing_skills': [],
        'recommendations': [],
        'strengths': [],
        'areas_for_improvement': []
    }
    
    try:
        # Extract keywords from both texts
        resume_keywords = set(extract_keywords(resume_text, 30))
        job_keywords = set(extract_keywords(job_description, 30))
        
        # Find matches and missing keywords
        keyword_matches = resume_keywords.intersection(job_keywords)
        missing_keywords = job_keywords - resume_keywords
        
        results['keyword_matches'] = list(keyword_matches)
        results['missing_keywords'] = list(missing_keywords)
        
        # Calculate keyword match percentage
        keyword_match_score = len(keyword_matches) / len(job_keywords) if job_keywords else 0
        
        # Extract and compare skills
        resume_skills = extract_skills_from_resume(resume_text)
        job_skills = extract_skills(job_description)
        
        skill_matches = set(resume_skills).intersection(set(job_skills['required'] + job_skills['preferred']))
        missing_skills = set(job_skills['required']) - set(resume_skills)
        
        results['skill_matches'] = list(skill_matches)
        results['missing_skills'] = list(missing_skills)
        
        # Calculate skill match score
        total_job_skills = len(job_skills['required']) + len(job_skills['preferred'])
        skill_match_score = len(skill_matches) / total_job_skills if total_job_skills > 0 else 0
        
        # Calculate text similarity
        text_similarity = calculate_text_similarity(resume_text, job_description)
        
        # Calculate overall match score (weighted average)
        results['overall_match_score'] = (
            keyword_match_score * 0.4 + 
            skill_match_score * 0.4 + 
            text_similarity * 0.2
        )
        
        # Generate recommendations
        results['recommendations'] = generate_recommendations(
            missing_keywords, missing_skills, results['overall_match_score']
        )
        
        # Identify strengths
        results['strengths'] = identify_strengths(keyword_matches, skill_matches)
        
        # Areas for improvement
        results['areas_for_improvement'] = identify_improvements(
            missing_keywords, missing_skills
        )
        
    except Exception as e:
        print(f"Error in resume matching: {e}")
    
    return results

def extract_skills_from_resume(resume_text):
    """Extract skills mentioned in resume"""
    # Common skill patterns (similar to job description analysis)
    skill_patterns = [
        r'\b(?:Python|Java|JavaScript|C\+\+|C#|PHP|Ruby|Go|Rust|Swift|Kotlin)\b',
        r'\b(?:React|Angular|Vue|Node\.js|Django|Flask|Spring|Laravel)\b',
        r'\b(?:SQL|MySQL|PostgreSQL|MongoDB|Redis|Elasticsearch)\b',
        r'\b(?:AWS|Azure|GCP|Docker|Kubernetes|Jenkins|Git)\b',
        r'\b(?:Machine Learning|AI|Data Science|Analytics|Statistics)\b',
        r'\b(?:HTML|CSS|Bootstrap|Tailwind|SASS|LESS)\b',
        r'\b(?:REST|API|GraphQL|Microservices|Agile|Scrum)\b'
    ]
    
    skills = []
    for pattern in skill_patterns:
        matches = re.findall(pattern, resume_text, re.IGNORECASE)
        skills.extend(matches)
    
    return list(set(skills))

def generate_recommendations(missing_keywords, missing_skills, match_score):
    """Generate actionable recommendations for resume improvement"""
    recommendations = []
    
    if match_score < 0.3:
        recommendations.append("Consider significantly restructuring your resume to better align with this job description.")
    elif match_score < 0.6:
        recommendations.append("Your resume has some alignment but needs improvement to better match the job requirements.")
    else:
        recommendations.append("Your resume shows good alignment with the job requirements!")
    
    if missing_keywords:
        top_missing = missing_keywords[:5]
        recommendations.append(f"Consider incorporating these important keywords: {', '.join(top_missing)}")
    
    if missing_skills:
        top_missing_skills = missing_skills[:3]
        recommendations.append(f"Highlight or develop these key skills: {', '.join(top_missing_skills)}")
    
    # General recommendations
    recommendations.extend([
        "Quantify your achievements with specific numbers and metrics where possible.",
        "Use action verbs to describe your accomplishments (e.g., 'implemented', 'optimized', 'led').",
        "Tailor your professional summary to match the job requirements.",
        "Ensure your resume is ATS-friendly with clear formatting and standard section headers."
    ])
    
    return recommendations

def identify_strengths(keyword_matches, skill_matches):
    """Identify strong points in the resume"""
    strengths = []
    
    if len(keyword_matches) > 10:
        strengths.append(f"Strong keyword alignment with {len(keyword_matches)} matching terms")
    
    if len(skill_matches) > 5:
        strengths.append(f"Good technical skill match with {len(skill_matches)} relevant skills")
    
    if keyword_matches:
        top_keywords = list(keyword_matches)[:3]
        strengths.append(f"Key strengths in: {', '.join(top_keywords)}")
    
    return strengths

def identify_improvements(missing_keywords, missing_skills):
    """Identify areas that need improvement"""
    improvements = []
    
    if len(missing_keywords) > 10:
        improvements.append("Significant keyword gaps - consider restructuring content")
    
    if missing_skills:
        improvements.append(f"Missing key skills: {', '.join(list(missing_skills)[:3])}")
    
    if len(missing_keywords) > 5:
        improvements.append("Consider adding more industry-specific terminology")
    
    return improvements

def calculate_ats_score(resume_text):
    """Calculate how ATS-friendly the resume is"""
    score = 100
    issues = []
    
    # Check for common ATS issues
    if len(re.findall(r'[^\w\s\-\.\,\:\;\(\)]', resume_text)) > 50:
        score -= 20
        issues.append("Too many special characters that might confuse ATS")
    
    # Check for standard sections
    standard_sections = ['experience', 'education', 'skills', 'summary']
    found_sections = sum(1 for section in standard_sections if section in resume_text.lower())
    
    if found_sections < 3:
        score -= 15
        issues.append("Missing standard resume sections")
    
    return {
        'score': max(0, score),
        'issues': issues
    }
