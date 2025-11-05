// Email Service for sending question papers via email
// Uses Jakarta Mail API for email functionality

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailService {
    
    // Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    
    // Use environment variables if set, otherwise use defaults
    private static final String SMTP_USER = System.getenv("SMTP_USER") != null
            ? System.getenv("SMTP_USER")
            : "ghadgesoham2006@gmail.com";
    
    private static final String SMTP_PASS = System.getenv("SMTP_PASS") != null
            ? System.getenv("SMTP_PASS")
            : "xjhvnwmhgynhqbfq";
    
    private static final String PDF_BASE_DIR = "PDF";
    
    /**
     * Sends an email with a PDF attachment
     * @param recipientEmail Email address of the recipient
     * @param subject Email subject
     * @param body Email body text
     * @param pdfFile PDF file to attach (can be relative to PDF folder or absolute path)
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendEmailWithAttachment(String recipientEmail, String subject, 
                                                   String body, String pdfFile) {
        // Resolve PDF file path
        File attachmentFile = resolvePdfFile(pdfFile);
        
        if (!attachmentFile.exists()) {
            System.err.println("❌ PDF file not found: " + attachmentFile.getAbsolutePath());
            return false;
        }
        
        try {
            // Setup mail properties
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            
            // Create text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body, "utf-8");
            
            // Create attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachmentFile);
            attachmentPart.setFileName(attachmentFile.getName());
            
            // Combine parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);
            
            message.setContent(multipart);
            
            // Send email
            Transport.send(message);
            
            System.out.println("✅ Email sent successfully to " + recipientEmail);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sends a question paper to an email address
     * @param recipientEmail Email address of the recipient
     * @param questionPaper The question paper to send
     * @return true if email sent successfully, false otherwise
     */
    public static boolean sendQuestionPaper(String recipientEmail, QuestionPaper questionPaper) {
        String subject = "Question Paper: " + questionPaper.getSubject() + " (" + 
                        questionPaper.getYear() + " - Semester " + questionPaper.getSemester() + ")";
        
        String body = String.format(
            "Dear Recipient,\n\n" +
            "Please find attached the requested question paper:\n\n" +
            "Subject: %s\n" +
            "Year: %d\n" +
            "Semester: %d\n" +
            "Status: %s\n\n" +
            "Best regards,\n" +
            "Question Paper Management System",
            questionPaper.getSubject(),
            questionPaper.getYear(),
            questionPaper.getSemester(),
            questionPaper.getStatus()
        );
        
        return sendEmailWithAttachment(recipientEmail, subject, body, questionPaper.getFilePath());
    }
    
    /**
     * Resolves PDF file path - checks if it's absolute, relative to PDF folder, or just filename
     * @param pdfFile The PDF file path or filename
     * @return Resolved File object
     */
    private static File resolvePdfFile(String pdfFile) {
        // Remove any path separators and clean the filename
        String cleanFileName = pdfFile.replace("\\", "/").replaceAll(".*/", ""); // Get just filename
        
        System.out.println("   📁 Looking for PDF file: " + cleanFileName);
        
        File file = new File(pdfFile);
        
        // If absolute path and exists, return it
        if (file.isAbsolute() && file.exists()) {
            System.out.println("   ✓ Found at absolute path: " + file.getAbsolutePath());
            return file;
        }
        
        // Candidate locations to search for PDFs (handles running from src/ or project root)
        String userDir = System.getProperty("user.dir");
        String projectRoot = null;
        try {
            // If running from src/, parent is project root
            File ud = new File(userDir);
            File parent = ud.getParentFile();
            if (parent != null) projectRoot = parent.getAbsolutePath();
        } catch (Exception ex) {
            projectRoot = null;
        }

        String[] candidates;
        if (projectRoot != null) {
            candidates = new String[] {
                PDF_BASE_DIR + File.separator + cleanFileName,
                "." + File.separator + PDF_BASE_DIR + File.separator + cleanFileName,
                projectRoot + File.separator + PDF_BASE_DIR + File.separator + cleanFileName,
                userDir + File.separator + PDF_BASE_DIR + File.separator + cleanFileName,
                ".." + File.separator + PDF_BASE_DIR + File.separator + cleanFileName
            };
        } else {
            candidates = new String[] {
                PDF_BASE_DIR + File.separator + cleanFileName,
                "." + File.separator + PDF_BASE_DIR + File.separator + cleanFileName,
                userDir + File.separator + PDF_BASE_DIR + File.separator + cleanFileName
            };
        }

        for (String p : candidates) {
            File f = new File(p);
            if (f.exists()) {
                System.out.println("   ✓ Found at: " + f.getAbsolutePath());
                return f;
            }
        }
        
        // List available PDFs for debugging
        File pdfDir = new File(PDF_BASE_DIR);
        if (pdfDir.exists() && pdfDir.isDirectory()) {
            System.out.println("   📋 Available PDF files in " + pdfDir.getAbsolutePath() + ":");
            File[] files = pdfDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (files != null) {
                for (File f : files) {
                    System.out.println("      - " + f.getName());
                }
            }
        }
        
        System.out.println("   ❌ File not found. Tried these candidate paths:");
        for (String p : candidates) {
            System.out.println("      - " + p);
        }

        // Return a non-existing file object from the first candidate so caller sees attempted path
        return new File(candidates.length > 0 ? candidates[0] : (PDF_BASE_DIR + File.separator + cleanFileName));
    }
    
    /**
     * Validates email configuration
     * @return true if configuration is valid
     */
    public static boolean isEmailConfigured() {
        return SMTP_USER != null && SMTP_PASS != null && 
               !SMTP_USER.isEmpty() && !SMTP_PASS.isEmpty();
    }
    
    /**
     * Gets the configured SMTP user (email address)
     * @return SMTP user email
     */
    public static String getSmtpUser() {
        return SMTP_USER;
    }
}

