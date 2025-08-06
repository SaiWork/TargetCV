# TargetCV - Java Spring Boot Implementation

A professional resume targeting tool built with Java Spring Boot, MySQL, and modern web technologies. This application helps users optimize their resumes for specific job applications through intelligent analysis and personalized recommendations.

## 🚀 Features

- **Resume Upload & Processing**: Support for PDF, DOCX, DOC, and TXT files
- **Intelligent Text Analysis**: Advanced keyword extraction and skill matching
- **Job Description Analysis**: Comprehensive parsing of job requirements
- **Resume-Job Matching**: AI-powered compatibility scoring
- **Personalized Recommendations**: Actionable suggestions for improvement
- **MySQL Database**: Persistent storage for resumes and analysis history
- **Modern Web UI**: Responsive Thymeleaf templates with Bootstrap
- **RESTful Architecture**: Clean separation of concerns with MVC pattern

## 🛠 Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: MySQL 8.0 (with H2 for development)
- **Web Framework**: Spring MVC with Thymeleaf
- **Document Processing**: Apache Tika
- **Text Analysis**: Apache OpenNLP
- **Build Tool**: Maven
- **Frontend**: Bootstrap 5, Font Awesome, jQuery

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
cd /Users/wangsai/Documents/TargetCV/java-app
```

### 2. Database Setup

#### For Development (H2 Database)
No setup required - H2 runs in-memory automatically.

#### For Production (MySQL)
```sql
CREATE DATABASE targetcv_db;
CREATE USER 'targetcv_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON targetcv_db.* TO 'targetcv_user'@'localhost';
FLUSH PRIVILEGES;
```

Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/targetcv_db
    username: targetcv_user
    password: your_password
```

### 3. Build the Application
```bash
mvn clean install
```

### 4. Run the Application

#### Development Mode (with H2)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Mode (with MySQL)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. Access the Application
- **Application URL**: http://localhost:8080/targetcv
- **H2 Console** (dev mode): http://localhost:8080/targetcv/h2-console

## 📁 Project Structure

```
java-app/
├── src/main/java/com/targetcv/
│   ├── TargetCvApplication.java          # Main Spring Boot application
│   ├── controller/                       # Web controllers
│   │   ├── HomeController.java
│   │   ├── ResumeController.java
│   │   └── AnalysisController.java
│   ├── model/                           # JPA entities
│   │   ├── Resume.java
│   │   └── JobAnalysis.java
│   ├── repository/                      # Data access layer
│   │   ├── ResumeRepository.java
│   │   └── JobAnalysisRepository.java
│   ├── service/                         # Business logic
│   │   ├── DocumentProcessingService.java
│   │   ├── TextAnalysisService.java
│   │   └── ResumeMatchingService.java
│   └── util/                           # Utility classes
├── src/main/resources/
│   ├── application.yml                  # Configuration
│   ├── templates/                       # Thymeleaf templates
│   └── static/                         # CSS, JS, images
└── src/test/java/                      # Unit tests
```

## 🎯 Usage Guide

### 1. Upload Resume
- Navigate to "Upload Resume"
- Select your resume file (PDF, DOCX, DOC, or TXT)
- File will be processed and text extracted automatically

### 2. Analyze Against Job Description
- Go to "My Resumes" and select a resume
- Click "Analyze" and paste the complete job description
- Optionally add job title and company name
- Submit for analysis

### 3. View Results
- Review your match score and detailed analysis
- Check keyword matches and missing elements
- Read personalized recommendations
- View strengths and areas for improvement

### 4. Track Progress
- Access analysis history for each resume
- Compare different job analyses
- Monitor improvement over time

## 🔧 Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
# Server configuration
server:
  port: 8080
  servlet:
    context-path: /targetcv

# File upload limits
spring:
  servlet:
    multipart:
      max-file-size: 16MB
      max-request-size: 16MB

# Custom application settings
targetcv:
  upload:
    directory: uploads/
    allowed-extensions: pdf,docx,txt,doc
  analysis:
    max-keywords: 20
    similarity-threshold: 0.6
```

## 🚀 Deployment

### Tomcat Deployment
1. Build WAR file:
```bash
mvn clean package
```

2. Deploy to Tomcat:
```bash
cp target/targetcv-app-1.0.0.war $TOMCAT_HOME/webapps/
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/targetcv-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 📊 Database Schema

### Resume Table
- `id` (Primary Key)
- `filename`, `original_filename`
- `file_path`, `file_size`, `content_type`
- `extracted_text` (LONGTEXT)
- `summary`
- `created_at`, `updated_at`

### Job Analysis Table
- `id` (Primary Key)
- `resume_id` (Foreign Key)
- `job_description` (LONGTEXT)
- `job_title`, `company_name`
- `overall_match_score`, `keyword_match_score`, `skill_match_score`
- `matched_keywords`, `missing_keywords` (JSON)
- `matched_skills`, `missing_skills` (JSON)
- `recommendations`, `strengths`, `improvements` (JSON)
- `experience_level`
- `created_at`

## 🔍 API Endpoints

### Web Controllers
- `GET /` - Home page
- `GET /resume/upload` - Upload form
- `POST /resume/upload` - Handle upload
- `GET /resume/list` - List resumes
- `GET /resume/view/{id}` - View resume
- `GET /analysis/analyze/{resumeId}` - Analysis form
- `POST /analysis/analyze/{resumeId}` - Process analysis
- `GET /analysis/results/{analysisId}` - View results

## 🛡 Security Considerations

- File upload validation and size limits
- SQL injection prevention with JPA
- XSS protection with Thymeleaf
- CSRF protection enabled by default
- Input validation and sanitization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:
1. Check the logs in `logs/targetcv.log`
2. Verify database connectivity
3. Ensure file upload directory permissions
4. Check application.yml configuration

## 🔄 Version History

- **v1.0.0** - Initial Java Spring Boot implementation
  - Resume upload and text extraction
  - Job description analysis
  - Resume-job matching algorithm
  - Web interface with Thymeleaf
  - MySQL database integration
