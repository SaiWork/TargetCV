import PyPDF2
import docx
import os

def extract_text_from_pdf(file_path):
    """Extract text content from PDF file"""
    try:
        with open(file_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            text = ""
            
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
            
            return text.strip()
    
    except Exception as e:
        raise Exception(f"Error reading PDF: {str(e)}")

def extract_text_from_docx(file_path):
    """Extract text content from DOCX file"""
    try:
        doc = docx.Document(file_path)
        text = ""
        
        for paragraph in doc.paragraphs:
            text += paragraph.text + "\n"
        
        return text.strip()
    
    except Exception as e:
        raise Exception(f"Error reading DOCX: {str(e)}")

def extract_text_from_txt(file_path):
    """Extract text content from TXT file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return file.read().strip()
    
    except Exception as e:
        raise Exception(f"Error reading TXT: {str(e)}")

def extract_text_from_file(file_path):
    """Extract text from various file formats"""
    file_extension = os.path.splitext(file_path)[1].lower()
    
    if file_extension == '.pdf':
        return extract_text_from_pdf(file_path)
    elif file_extension == '.docx':
        return extract_text_from_docx(file_path)
    elif file_extension == '.txt':
        return extract_text_from_txt(file_path)
    else:
        raise Exception(f"Unsupported file format: {file_extension}")

def clean_text(text):
    """Clean and normalize extracted text"""
    # Remove extra whitespace and normalize line breaks
    lines = [line.strip() for line in text.split('\n') if line.strip()]
    return '\n'.join(lines)
