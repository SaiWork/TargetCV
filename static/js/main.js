// Main JavaScript functionality for TargetCV

document.addEventListener('DOMContentLoaded', function() {
    // File upload validation
    const fileInput = document.getElementById('resume');
    if (fileInput) {
        fileInput.addEventListener('change', function() {
            validateFileUpload(this);
        });
    }

    // Form submission loading states
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                showLoadingState(submitBtn);
            }
        });
    });

    // Initialize tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined') {
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert.classList.contains('show')) {
                alert.classList.remove('show');
                setTimeout(() => alert.remove(), 150);
            }
        }, 5000);
    });
});

function validateFileUpload(input) {
    const file = input.files[0];
    const maxSize = 16 * 1024 * 1024; // 16MB
    const allowedTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
    
    if (!file) return;

    // Check file size
    if (file.size > maxSize) {
        showAlert('File size must be less than 16MB', 'danger');
        input.value = '';
        return false;
    }

    // Check file type
    if (!allowedTypes.includes(file.type)) {
        showAlert('Please upload a PDF, DOCX, or TXT file', 'danger');
        input.value = '';
        return false;
    }

    // Show file info
    const fileInfo = document.createElement('div');
    fileInfo.className = 'mt-2 text-success small';
    fileInfo.innerHTML = `<i class="fas fa-check-circle me-1"></i>Selected: ${file.name} (${formatFileSize(file.size)})`;
    
    // Remove any existing file info
    const existingInfo = input.parentNode.querySelector('.text-success');
    if (existingInfo) {
        existingInfo.remove();
    }
    
    input.parentNode.appendChild(fileInfo);
    return true;
}

function showLoadingState(button) {
    const originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Processing...';
    
    // Store original text for potential restoration
    button.dataset.originalText = originalText;
}

function restoreButtonState(button) {
    if (button.dataset.originalText) {
        button.innerHTML = button.dataset.originalText;
        button.disabled = false;
    }
}

function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Insert at the top of the main container
    const main = document.querySelector('main');
    if (main) {
        main.insertBefore(alertDiv, main.firstChild);
    }
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        if (alertDiv.classList.contains('show')) {
            alertDiv.classList.remove('show');
            setTimeout(() => alertDiv.remove(), 150);
        }
    }, 5000);
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// API helper functions
async function extractKeywords(text) {
    try {
        const response = await fetch('/api/keywords', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ text: text })
        });
        
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        
        const data = await response.json();
        return data.keywords;
    } catch (error) {
        console.error('Error extracting keywords:', error);
        return [];
    }
}

// Utility function to copy text to clipboard
function copyToClipboard(text) {
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showAlert('Copied to clipboard!', 'success');
        }).catch(err => {
            console.error('Failed to copy: ', err);
            fallbackCopyTextToClipboard(text);
        });
    } else {
        fallbackCopyTextToClipboard(text);
    }
}

function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";
    
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        const successful = document.execCommand('copy');
        if (successful) {
            showAlert('Copied to clipboard!', 'success');
        } else {
            showAlert('Failed to copy to clipboard', 'danger');
        }
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
        showAlert('Failed to copy to clipboard', 'danger');
    }
    
    document.body.removeChild(textArea);
}

// Progress bar animation
function animateProgressBar(progressBar, targetWidth) {
    let currentWidth = 0;
    const increment = targetWidth / 50; // 50 steps for smooth animation
    
    const timer = setInterval(() => {
        currentWidth += increment;
        if (currentWidth >= targetWidth) {
            currentWidth = targetWidth;
            clearInterval(timer);
        }
        progressBar.style.width = currentWidth + '%';
    }, 20);
}
