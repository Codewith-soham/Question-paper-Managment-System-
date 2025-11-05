import java.sql.Connection;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful!");
                System.out.println("Database product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
                conn.close();
            } else {
                System.out.println("❌ Could not connect to database!");
            }
        } catch (Exception e) {
            System.out.println("❌ Error connecting to database:");
            e.printStackTrace();
        }
    }
}