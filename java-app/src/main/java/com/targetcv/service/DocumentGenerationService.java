package com.targetcv.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service for generating documents (PDF, DOCX) from resume content
 */
@Service
public class DocumentGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentGenerationService.class);
    
    /**
     * Generate PDF document from resume content
     */
    public void generatePDF(String resumeContent, String filePath) throws IOException {
        logger.info("Generating PDF document: {}", filePath);
        
        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Split content into paragraphs
            String[] paragraphs = resumeContent.split("\n\n");
            
            for (String paragraphText : paragraphs) {
                if (!paragraphText.trim().isEmpty()) {
                    Paragraph paragraph = new Paragraph(paragraphText.trim());
                    document.add(paragraph);
                }
            }
            
            document.close();
            logger.info("Successfully generated PDF: {}", filePath);
            
        } catch (Exception e) {
            logger.error("Error generating PDF: {}", filePath, e);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate DOCX document from resume content
     */
    public void generateDOCX(String resumeContent, String filePath) throws IOException {
        logger.info("Generating DOCX document: {}", filePath);
        
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {
            
            // Split content into sections and paragraphs
            String[] sections = resumeContent.split("\n\n");
            
            for (String sectionText : sections) {
                if (!sectionText.trim().isEmpty()) {
                    // Check if this looks like a header (all caps, short line, etc.)
                    if (isHeader(sectionText.trim())) {
                        addHeader(document, sectionText.trim());
                    } else {
                        addParagraph(document, sectionText.trim());
                    }
                }
            }
            
            document.write(out);
            logger.info("Successfully generated DOCX: {}", filePath);
            
        } catch (Exception e) {
            logger.error("Error generating DOCX: {}", filePath, e);
            throw new IOException("Failed to generate DOCX: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a header to the DOCX document
     */
    private void addHeader(XWPFDocument document, String headerText) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(headerText);
        run.setBold(true);
        run.setFontSize(14);
        run.addBreak();
    }
    
    /**
     * Add a regular paragraph to the DOCX document
     */
    private void addParagraph(XWPFDocument document, String paragraphText) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        
        // Handle bullet points
        if (paragraphText.startsWith("•") || paragraphText.startsWith("-") || paragraphText.startsWith("*")) {
            run.setText(paragraphText);
        } else {
            run.setText(paragraphText);
        }
        
        run.setFontSize(11);
        run.addBreak();
    }
    
    /**
     * Determine if a line of text should be treated as a header
     */
    private boolean isHeader(String text) {
        // Simple heuristics to identify headers
        if (text.length() < 50 && 
            (text.equals(text.toUpperCase()) || 
             isCommonResumeSection(text) ||
             text.endsWith(":"))) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if text matches common resume section headers
     */
    private boolean isCommonResumeSection(String text) {
        List<String> commonSections = Arrays.asList(
            "PROFESSIONAL SUMMARY", "SUMMARY", "OBJECTIVE",
            "WORK EXPERIENCE", "EXPERIENCE", "EMPLOYMENT HISTORY",
            "EDUCATION", "ACADEMIC BACKGROUND",
            "SKILLS", "TECHNICAL SKILLS", "CORE COMPETENCIES",
            "CERTIFICATIONS", "CERTIFICATES",
            "PROJECTS", "KEY PROJECTS",
            "ACHIEVEMENTS", "ACCOMPLISHMENTS",
            "REFERENCES", "CONTACT INFORMATION",
            "PROFESSIONAL EXPERIENCE", "CAREER HIGHLIGHTS"
        );
        
        String upperText = text.toUpperCase().trim();
        return commonSections.stream().anyMatch(section -> 
            upperText.equals(section) || upperText.contains(section));
    }
    
    /**
     * Generate HTML-formatted resume for web display
     */
    public String generateHTML(String resumeContent) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Generated Resume</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 40px; }\n");
        html.append("h1, h2, h3 { color: #333; margin-top: 20px; }\n");
        html.append("h1 { border-bottom: 2px solid #333; }\n");
        html.append("h2 { border-bottom: 1px solid #666; }\n");
        html.append("p { margin: 10px 0; }\n");
        html.append("ul { margin: 10px 0; padding-left: 20px; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        
        // Convert content to HTML
        String[] sections = resumeContent.split("\n\n");
        
        for (String section : sections) {
            if (!section.trim().isEmpty()) {
                if (isHeader(section.trim())) {
                    html.append("<h2>").append(escapeHtml(section.trim())).append("</h2>\n");
                } else {
                    // Handle bullet points
                    if (section.contains("•") || section.contains("-")) {
                        html.append("<ul>\n");
                        String[] lines = section.split("\n");
                        for (String line : lines) {
                            if (line.trim().startsWith("•") || line.trim().startsWith("-")) {
                                String bulletText = line.trim().substring(1).trim();
                                html.append("<li>").append(escapeHtml(bulletText)).append("</li>\n");
                            } else if (!line.trim().isEmpty()) {
                                html.append("<p>").append(escapeHtml(line.trim())).append("</p>\n");
                            }
                        }
                        html.append("</ul>\n");
                    } else {
                        html.append("<p>").append(escapeHtml(section.trim())).append("</p>\n");
                    }
                }
            }
        }
        
        html.append("</body>\n</html>");
        return html.toString();
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * Get supported document formats
     */
    public List<String> getSupportedFormats() {
        return Arrays.asList("pdf", "docx", "html");
    }
    
    /**
     * Validate if format is supported
     */
    public boolean isFormatSupported(String format) {
        return getSupportedFormats().contains(format.toLowerCase());
    }
}
