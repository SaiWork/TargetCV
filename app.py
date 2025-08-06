from flask import Flask, render_template, request, jsonify, redirect, url_for, flash
import os
from werkzeug.utils import secure_filename
from utils.pdf_processor import extract_text_from_pdf
from utils.text_analyzer import analyze_job_description, extract_keywords
from utils.resume_matcher import match_resume_to_job

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key-here'
app.config['UPLOAD_FOLDER'] = 'static/uploads'
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max file size

# Ensure upload directory exists
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

ALLOWED_EXTENSIONS = {'pdf', 'docx', 'txt'}

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/')
def index():
    """Main landing page"""
    return render_template('index.html')

@app.route('/upload', methods=['GET', 'POST'])
def upload_resume():
    """Handle resume upload"""
    if request.method == 'POST':
        if 'resume' not in request.files:
            flash('No file selected')
            return redirect(request.url)
        
        file = request.files['resume']
        if file.filename == '':
            flash('No file selected')
            return redirect(request.url)
        
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            
            # Extract text from uploaded resume
            try:
                resume_text = extract_text_from_pdf(filepath)
                return render_template('analyze.html', 
                                     resume_text=resume_text, 
                                     filename=filename)
            except Exception as e:
                flash(f'Error processing file: {str(e)}')
                return redirect(request.url)
        else:
            flash('Invalid file type. Please upload PDF, DOCX, or TXT files.')
            return redirect(request.url)
    
    return render_template('upload.html')

@app.route('/analyze', methods=['POST'])
def analyze():
    """Analyze job description and provide resume recommendations"""
    job_description = request.form.get('job_description', '')
    resume_text = request.form.get('resume_text', '')
    
    if not job_description or not resume_text:
        flash('Both resume and job description are required')
        return redirect(url_for('upload_resume'))
    
    try:
        # Analyze job description
        job_analysis = analyze_job_description(job_description)
        
        # Match resume to job
        match_results = match_resume_to_job(resume_text, job_description)
        
        return render_template('results.html', 
                             job_analysis=job_analysis,
                             match_results=match_results,
                             resume_text=resume_text,
                             job_description=job_description)
    
    except Exception as e:
        flash(f'Error during analysis: {str(e)}')
        return redirect(url_for('upload_resume'))

@app.route('/api/keywords', methods=['POST'])
def get_keywords():
    """API endpoint to extract keywords from text"""
    data = request.get_json()
    text = data.get('text', '')
    
    if not text:
        return jsonify({'error': 'No text provided'}), 400
    
    try:
        keywords = extract_keywords(text)
        return jsonify({'keywords': keywords})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
