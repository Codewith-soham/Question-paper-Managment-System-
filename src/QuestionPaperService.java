// Handles user actions like adding, viewing, deleting, and opening question papers

import java.io.IOException;
import java.util.*;
import java.io.File;
import java.awt.Desktop;

public class QuestionPaperService {
    private final QuestionPaperDAO dao = new QuestionPaperDAO();
    private final String baseFolder = "D:\\Question Paper Managment System\\PDF";
    private final Scanner sc = new Scanner(System.in);

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

}