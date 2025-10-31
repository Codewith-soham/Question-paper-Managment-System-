// Simple HTTP and Email Server using Jakarta Mail
// ------------------------------------------------
// Requirements:
// 1. jakarta.mail-api-2.2.0-M1.jar
// 2. jakarta.mail-2.0.1.jar
// Place both in the "library" folder.

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.*;

public class EmailServer {

    // ---------------- CONFIGURATION ----------------
    static final String PDF_DIR = "D:/Question Paper Managment System/PDF/"; // Folder containing PDF files
    static final String SMTP_HOST = "smtp.gmail.com"; // SMTP server

    // ✅ Use environment variables if set, otherwise use local defaults for testing
    static final String SMTP_USER = System.getenv("SMTP_USER") != null
            ? System.getenv("SMTP_USER")
            : "ghadgesoham2006@gmail.com";  // fallback email

    static final String SMTP_PASS = System.getenv("SMTP_PASS") != null
            ? System.getenv("SMTP_PASS")
            : "blmz sqib rmsf vrry";  // fallback Gmail App Password

    static final int PORT = 8000; // HTTP port

    public static void main(String[] args) throws Exception {
        if (SMTP_USER == null || SMTP_PASS == null) {
            System.err.println("❌ Please set environment variables SMTP_USER and SMTP_PASS");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/send-email", new SendEmailHandler());
        server.setExecutor(null);
        System.out.println("✅ EmailServer running on port " + PORT);
        server.start();
    }

    // ---------------- HANDLER ----------------
    static class SendEmailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Read JSON body
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            String requestBody = sb.toString();

            // Parse JSON
            String recipient = extractJsonValue(requestBody, "recipient");
            String filename = extractJsonValue(requestBody, "filename");
            File pdf = new File(PDF_DIR, filename);
            String result;

            // Validate
            if (recipient == null || filename == null || !pdf.exists()) {
                result = "Bad request or file not found.";
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(400, result.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(result.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            // Send email
            boolean success = sendMailWithAttachment(recipient,
                    "Requested Question Paper",
                    "Please find your requested question paper attached.",
                    pdf);

            result = success ? "✅ Email sent to " + recipient : "❌ Failed to send email.";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(success ? 200 : 500, result.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(result.getBytes(StandardCharsets.UTF_8));
            }
        }

        /** Extracts a simple string value from JSON (e.g. {"key":"value"}) */
        private String extractJsonValue(String json, String key) {
            String p = String.format("\\\"%s\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", key);
            Matcher m = Pattern.compile(p).matcher(json);
            return m.find() ? m.group(1) : null;
        }

        /** Sends an email with a PDF attachment using Jakarta Mail */
        private boolean sendMailWithAttachment(String to, String subject, String body, File attachment) {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", SMTP_HOST);
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USER));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);

                // Text body
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body, "utf-8");

                // Attachment
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);

                // Combine
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);
                Transport.send(message);

                System.out.println("✅ Sent email to " + to);
                return true;
            } catch (Exception e) {
                System.err.println("❌ Email sending failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }
}
