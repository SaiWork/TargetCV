# TargetCV - Resume Targeting Tool

A web application that helps users customize their resumes for specific job applications to improve their chances of getting interviews.

## Features

- **Job Description Analysis**: Parse and analyze job postings to identify key requirements
- **Resume Optimization**: Suggest modifications to match job requirements
- **Keyword Matching**: Optimize resume content with relevant keywords
- **Multiple Versions**: Generate and manage different versions of your resume
- **ATS-Friendly**: Ensure resumes are compatible with Applicant Tracking Systems

## Tech Stack

- **Frontend**: HTML, CSS, JavaScript (with modern frameworks)
- **Backend**: Python with Flask/FastAPI
- **Document Processing**: PDF generation and parsing
- **AI/NLP**: Text analysis and matching algorithms

## Getting Started

1. Clone the repository
2. Install dependencies: `pip install -r requirements.txt`
3. Run the application: `python app.py`
4. Open your browser to `http://localhost:5000`

## Project Structure

```
TargetCV/
├── app.py                 # Main application entry point
├── requirements.txt       # Python dependencies
├── static/               # CSS, JS, and other static files
│   ├── css/
│   ├── js/
│   └── uploads/          # Uploaded resume files
├── templates/            # HTML templates
├── utils/                # Utility functions
│   ├── pdf_processor.py  # PDF handling
│   ├── text_analyzer.py  # Text analysis
│   └── resume_matcher.py # Resume-job matching logic
└── data/                 # Sample data and templates
```

## Development

This project is in active development. Current focus areas:
- Setting up basic web interface
- Implementing PDF upload and processing
- Building job description analysis features
- Creating resume optimization algorithms
