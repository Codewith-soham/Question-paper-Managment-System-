// Handles user actions like adding, viewing, deleting, and opening question papers

import java.util.*;
import java.io.File;
import java.awt.Desktop;

public class QuestionPaperService {
    private final QuestionPaperDAO dao = new QuestionPaperDAO();
    private final String baseFolder = "PDF";
    private final Scanner sc = new Scanner(System.in);

    // Method for web interface to add paper
    public void addPaper(QuestionPaper paper) {
        dao.addPaper(paper);
    }

    // Method for web interface to get all papers
    public List<QuestionPaper> getAllPapers() {
        return dao.viewAllPapers();
    }

    // Find a paper by its ID (returns null if not found)
    public QuestionPaper getPaperById(int id) {
        List<QuestionPaper> all = dao.viewAllPapers();
        for (QuestionPaper p : all) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public void addPaper() {
        System.out.print("Enter Subject: ");
        String subject = sc.next();
        System.out.print("Enter Year: ");
        int year = sc.nextInt();
        System.out.print("Enter Semester: ");
        int sem = sc.nextInt();
        System.out.print("Enter File Name (e.g. dbms2025.pdf): "); //Automate this part
        String file = sc.next();
        System.out.print("Enter Status (AVAILABLE/NOT AVAILABLE): ");  //link this part when saved automatically updates status
        String status = sc.next();

        QuestionPaper paper = new QuestionPaper(subject, year, sem, file, status);
        dao.addPaper(paper);
    }

    public void searchPaper() {
        System.out.print("Enter Subject: ");
        String subject = sc.next();
        System.out.print("Enter Year: ");
        int year = sc.nextInt();
        System.out.print("Enter Semester: ");
        int sem = sc.nextInt();

        List<QuestionPaper> papers = dao.searchPaper(subject, year, sem);
        if (papers.isEmpty()) {
            System.out.println("No papers found.");
        } else {
            for (QuestionPaper q : papers) {
                System.out.println(q);
                File f = new File(baseFolder, q.getFilePath());
                if (f.exists()) {
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (Exception e) {
                        System.out.println("Error opening file.");
                    }
                }
            }
        }
    }

    public void viewAllPapers() {
        List<QuestionPaper> papers = dao.viewAllPapers();
        if (papers.isEmpty()) System.out.println("No papers available.");
        else papers.forEach(System.out::println);
    }

    public void deletePaper() {
        System.out.print("Enter ID of paper to delete: ");
        int id = sc.nextInt();
        dao.deletePaper(id);
    }

    public void sendPaperByEmail() {
        // Check if email is configured
        if (!EmailService.isEmailConfigured()) {
            System.out.println("❌ Email is not configured. Please set SMTP_USER and SMTP_PASS environment variables.");
            return;
        }
        
        // First, show available papers
        viewAllPapers();
        
        System.out.print("\nEnter ID of paper to send: ");
        int id = sc.nextInt();
        
        // Find the paper by ID
        List<QuestionPaper> allPapers = dao.viewAllPapers();
        QuestionPaper selectedPaper = null;
        for (QuestionPaper paper : allPapers) {
            if (paper.getId() == id) {
                selectedPaper = paper;
                break;
            }
        }
        
        if (selectedPaper == null) {
            System.out.println("❌ Paper with ID " + id + " not found.");
            return;
        }
        
        System.out.print("Enter recipient email address: ");
        String recipientEmail = sc.next();
        
        System.out.println("\nSending email...");
        boolean success = EmailService.sendQuestionPaper(recipientEmail, selectedPaper);
        
        if (success) {
            System.out.println("✅ Email sent successfully to " + recipientEmail);
        } else {
            System.out.println("❌ Failed to send email. Please check the error messages above.");
        }
    }

}