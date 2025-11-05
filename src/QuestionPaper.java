// Represents a question paper with subject, year, and file info

public class QuestionPaper {
    private int id;
    private String subject;
    private int year;
    private int semester;
    private String filePath;
    private String status;

    // Constructor without ID (for inserting)
    public QuestionPaper(String subject, int year, int semester, String filePath, String status) {
        this.subject = subject;
        this.year = year;
        this.semester = semester;
        this.filePath = filePath;
        this.status = status;
    }

    // Constructor with ID (for retrieving)
    public QuestionPaper(int id, String subject, int year, int semester, String filePath, String status) {
        this.id = id;
        this.subject = subject;
        this.year = year;
        this.semester = semester;
        this.filePath = filePath;
        this.status = status;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getSubject() { return subject; }
    public int getYear() { return year; }
    public int getSemester() { return semester; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return id + " | " + subject + " | " + year + " | Sem " + semester + " | " + status + " | " + filePath;
    }
}
