import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.sql.*;
import java.awt.Desktop;
import java.net.URI;

public class LaunchQPMS {
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "soham1234";

    public static void main(String[] args) {
        try {
            System.out.println("\n=== Question Paper Management System Launcher ===\n");
            
            // 1. Create directories
            createDirectories();
            
            // 2. Download dependencies
            downloadDependencies();
            
            // Wait a moment for files to be available
            Thread.sleep(1000);
            
            // 3. Setup database
            setupDatabase();
            
            // 4. Start WebServer
            startWebServer();
            
            // 5. Open browser
            openBrowser();
            
            System.out.println("\n✓ System is ready! Access the web interface at:");
            System.out.println("  http://localhost:8080/frontend/index.html");
            System.out.println("\nPress Ctrl+C to stop the server.");
            
            // Keep the program running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createDirectories() throws IOException {
        System.out.println("Creating directories...");
        Files.createDirectories(Paths.get("lib"));
        Files.createDirectories(Paths.get("PDF"));
        System.out.println("✓ Directories created");
    }

    private static void downloadDependencies() throws Exception {
        System.out.println("\nChecking dependencies...");
        String[] dependencies = {
            "https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar",
            "https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar",
            "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
        };

        for (String urlStr : dependencies) {
            String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
            Path filePath = Paths.get("lib", fileName);
            
            if (Files.exists(filePath)) {
                System.out.println("✓ Found " + fileName);
                continue;
            }

            System.out.println("Downloading " + fileName + "...");
            URL url = new URL(urlStr);
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            System.out.println("✓ Downloaded " + fileName);
        }
    }

    private static void setupDatabase() throws SQLException, ClassNotFoundException {
        System.out.println("\nSetting up database...");
        
        // Load MySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // First try connecting to MySQL
        try (Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS)) {
            Statement stmt = conn.createStatement();
            
            // Create database
            stmt.execute("CREATE DATABASE IF NOT EXISTS questionpaper");
            stmt.execute("USE questionpaper");
            
            // Create table
            stmt.execute("CREATE TABLE IF NOT EXISTS question_paper (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "subject VARCHAR(100) NOT NULL," +
                "year INT NOT NULL," +
                "semester INT NOT NULL," +
                "file_path VARCHAR(255) NOT NULL," +
                "status VARCHAR(50) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            System.out.println("✓ Database setup complete");
        }
    }

    private static void startWebServer() {
        System.out.println("\nStarting web server...");
        
        // Start WebServer in a new thread
        Thread serverThread = new Thread(() -> {
            try {
                WebServer.main(new String[0]);
            } catch (Exception e) {
                System.err.println("Error starting WebServer: " + e.getMessage());
                System.exit(1);
            }
        });
        serverThread.start();
        
        // Give it a moment to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✓ Web server started");
    }

    private static void openBrowser() {
        System.out.println("\nOpening web browser...");
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI("http://localhost:8080/frontend/index.html"));
            System.out.println("✓ Browser opened");
        } catch (Exception e) {
            System.out.println("! Could not open browser automatically.");
            System.out.println("  Please open http://localhost:8080/frontend/index.html manually");
        }
    }
}