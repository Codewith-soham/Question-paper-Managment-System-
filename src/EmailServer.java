// Simple HTTP and Email Server using Jakarta Mail
// ------------------------------------------------
// Requirements:
// 1. jakarta.mail-api-2.2.0-M1.jar
// 2. jakarta.mail-2.0.1.jar
// Place both in the "library" folder.
//
// This server provides HTTP endpoints for the web frontend to send emails.
// It uses the EmailService class for email functionality.

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class EmailServer {

    static final int PORT = 8000; // HTTP port
    
    /** Static helper to set CORS headers */
    private static void setCorsHeadersStatic(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    public static void main(String[] args) throws Exception {
        // Check if email is configured
        if (!EmailService.isEmailConfigured()) {
            System.err.println("❌ Email is not configured. Please set SMTP_USER and SMTP_PASS environment variables.");
            System.err.println("   Using fallback credentials from EmailService if available.");
        }

        try {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/send-email", new SendEmailHandler());
        server.createContext("/health", exchange -> {
            setCorsHeadersStatic(exchange);
            String response = "EmailServer is running";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
            exchange.close();
        });
        server.setExecutor(null);
        System.out.println("✅ EmailServer running on port " + PORT);
        System.out.println("   Configured SMTP User: " + EmailService.getSmtpUser());
        System.out.println("   Health check: http://localhost:" + PORT + "/health");
        System.out.println("   Press Ctrl+C to stop the server");
        server.start();
        } catch (java.net.BindException e) {
            System.err.println("❌ Error: Port " + PORT + " is already in use!");
            System.err.println("   Another instance of EmailServer may be running.");
            System.err.println("   Please:");
            System.err.println("   1. Close the existing EmailServer instance, or");
            System.err.println("   2. Use run-email-server-auto.bat to automatically kill it");
            System.exit(1);
        }
    }

    // ---------------- HANDLER ----------------
    static class SendEmailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            // Handle CORS preflight request
            if ("OPTIONS".equalsIgnoreCase(method)) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return;
            }
            
            // Set CORS headers early for all requests
            setCorsHeaders(exchange);
            
            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                exchange.close();
                return;
            }
            
            System.out.println("📧 Received POST request from: " + exchange.getRemoteAddress());

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
            String result;

            // Validate
            if (recipient == null || filename == null) {
                result = "Bad request: recipient and filename are required.";
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(400, result.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(result.getBytes(StandardCharsets.UTF_8));
                }
                exchange.close();
                return;
            }

            // Send email using EmailService
            System.out.println("📧 Email request received:");
            System.out.println("   Recipient: " + recipient);
            System.out.println("   Filename: " + filename);
            
            boolean success = EmailService.sendEmailWithAttachment(
                    recipient,
                    "Requested Question Paper",
                    "Please find your requested question paper attached.",
                    filename
            );

            if (success) {
                result = "✅ Email sent successfully to " + recipient;
                System.out.println("✅ Email sent successfully");
            } else {
                result = "❌ Failed to send email. Check server console for details.";
                System.err.println("❌ Email sending failed for: " + recipient);
            }
            
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(success ? 200 : 500, result.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(result.getBytes(StandardCharsets.UTF_8));
            }
            exchange.close();
        }

        /** Extracts a simple string value from JSON (e.g. {"key":"value"}) */
        private String extractJsonValue(String json, String key) {
            String p = String.format("\\\"%s\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"", key);
            Matcher m = Pattern.compile(p).matcher(json);
            return m.find() ? m.group(1) : null;
        }
        
        /** Sets CORS headers to allow cross-origin requests */
        private void setCorsHeaders(HttpExchange exchange) {
            setCorsHeadersStatic(exchange);
        }
    }
}
