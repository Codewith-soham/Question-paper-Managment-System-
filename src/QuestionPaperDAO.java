// Manages saving and retrieving question paper data from files or storage

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionPaperDAO {

    public void addPaper(QuestionPaper paper) {
        String query = "INSERT INTO question_paper (subject, year, semester, file_path, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, paper.getSubject());
            ps.setInt(2, paper.getYear());
            ps.setInt(3, paper.getSemester());
            ps.setString(4, paper.getFilePath());
            ps.setString(5, paper.getStatus());
            ps.executeUpdate();
            System.out.println("Paper added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<QuestionPaper> searchPaper(String subject, int year, int semester) {
        List<QuestionPaper> list = new ArrayList<>();
        String query = "SELECT * FROM question_paper WHERE subject=? AND year=? AND semester=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, subject);
            ps.setInt(2, year);
            ps.setInt(3, semester);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new QuestionPaper(
                        rs.getInt("id"),
                        rs.getString("subject"),
                        rs.getInt("year"),
                        rs.getInt("semester"),
                        rs.getString("file_path"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<QuestionPaper> viewAllPapers() {
        List<QuestionPaper> list = new ArrayList<>();
        String query = "SELECT * FROM question_paper";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new QuestionPaper(
                        rs.getInt("id"),
                        rs.getString("subject"),
                        rs.getInt("year"),
                        rs.getInt("semester"),
                        rs.getString("file_path"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deletePaper(int id) {
        String query = "DELETE FROM question_paper WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("🗑️ Record deleted successfully!");
            else System.out.println("No record found with ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}