import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class WebServer {
    private static final int PORT = 8080;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final QuestionPaperService service = new QuestionPaperService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Serve static files from frontend directory
        server.createContext("/frontend", staticFileHandler());
        // Serve PDF files from project PDF directory
        server.createContext("/pdf", pdfFileHandler());
        
    // Handle /papers routing (list, add, send email)
    server.createContext("/papers", routePapersHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    private static HttpHandler getAllPapersHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = objectMapper.writeValueAsString(service.getAllPapers());
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    private static HttpHandler pdfFileHandler() {
        return exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String requestPath = exchange.getRequestURI().getPath(); // e.g. /pdf/filename.pdf
                String fileName = requestPath.substring("/pdf".length());
                if (fileName.isEmpty() || "/".equals(fileName)) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                // Normalize and ensure we only serve by filename to prevent path traversal
                fileName = fileName.replace("\\", "/");
                if (fileName.startsWith("/")) fileName = fileName.substring(1);
                int slash = fileName.lastIndexOf('/');
                if (slash != -1) fileName = fileName.substring(slash + 1);

                // Candidate locations for PDF directory (project root vs running from src/)
                String userDir = System.getProperty("user.dir");
                File[] candidates = new File[] {
                    new File(userDir + File.separator + "PDF" + File.separator + fileName),
                    new File(userDir + File.separator + ".." + File.separator + "PDF" + File.separator + fileName),
                    new File("PDF" + File.separator + fileName)
                };

                File pdfFile = null;
                for (File f : candidates) {
                    if (f.exists() && f.isFile()) { pdfFile = f; break; }
                }

                if (pdfFile == null) {
                    String notFound = "PDF not found";
                    exchange.sendResponseHeaders(404, notFound.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFound.getBytes(StandardCharsets.UTF_8));
                    }
                    return;
                }

                // Serve inline as application/pdf
                exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                exchange.getResponseHeaders().set("Content-Disposition", "inline; filename=\"" + fileName + "\"");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, pdfFile.length());
                try (FileInputStream fs = new FileInputStream(pdfFile);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = fs.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                String response = "Internal Server Error";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        };
    }

    private static HttpHandler addPaperHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Read request body
                    String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                            .lines().collect(Collectors.joining());
                    
                    // Parse JSON into QuestionPaper object
                    Map<String, Object> data = objectMapper.readValue(body, Map.class);
                    QuestionPaper paper = new QuestionPaper(
                        (String) data.get("subject"),
                        ((Number) data.get("year")).intValue(),
                        ((Number) data.get("semester")).intValue(),
                        (String) data.get("filePath"),
                        (String) data.get("status")
                    );
                    
                    // Add paper using service
                    service.addPaper(paper);
                    
                    sendResponse(exchange, 200, "{\"message\": \"Paper added successfully\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        };
    }

    private static HttpHandler staticFileHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath().substring("/frontend".length());
                    if (path.isEmpty() || path.equals("/")) path = "/index.html";

                    // Try multiple candidate frontend locations so server works
                    // whether started from project root or from src/
                    String[] candidates = new String[] {"frontend", "../frontend", "./frontend"};
                    File file = null;
                    for (String base : candidates) {
                        File f = new File(base + path);
                        if (f.exists() && f.isFile()) {
                            file = f;
                            break;
                        }
                    }

                    // Final fallback: check absolute path or project-root style
                    if (file == null) {
                        File f = new File(System.getProperty("user.dir") + File.separator + "frontend" + path);
                        if (f.exists() && f.isFile()) file = f;
                    }

                    if (file == null) {
                        String response = "404 Not Found";
                        System.out.println("[WebServer] Static file not found for path: " + path);
                        System.out.println("[WebServer] Tried frontend locations: frontend, ../frontend, ./frontend, and user.dir/frontend");
                        exchange.sendResponseHeaders(404, response.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                        return;
                    }

                    String contentType = "text/html";
                    if (path.endsWith(".css")) {
                        contentType = "text/css";
                    } else if (path.endsWith(".js")) {
                        contentType = "text/javascript";
                    }

                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, file.length());
                    try (FileInputStream fs = new FileInputStream(file);
                         OutputStream os = exchange.getResponseBody()) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = fs.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    String response = "500 Internal Server Error";
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        };
    }

    /**
     * Route handler for /papers and subpaths like:
     *  - GET  /papers                -> list all papers
     *  - POST /papers/add            -> add a paper (JSON body)
     *  - POST /papers/{id}/email?recipientEmail=... -> send email for paper id
     */
    private static HttpHandler routePapersHandler() {
        return exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            // path begins with /papers
            try {
                if ("GET".equalsIgnoreCase(method) && (path.equals("/papers") || path.equals("/papers/"))) {
                    String response = objectMapper.writeValueAsString(service.getAllPapers());
                    sendResponse(exchange, 200, response);
                    return;
                }

                if ("POST".equalsIgnoreCase(method) && path.equals("/papers/add")) {
                    // reuse addPaperHandler logic: read JSON body and add
                    String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                            .lines().collect(Collectors.joining());
                    Map<String, Object> data = objectMapper.readValue(body, Map.class);
                    QuestionPaper paper = new QuestionPaper(
                        (String) data.get("subject"),
                        ((Number) data.get("year")).intValue(),
                        ((Number) data.get("semester")).intValue(),
                        (String) data.get("filePath"),
                        (String) data.get("status")
                    );
                    service.addPaper(paper);
                    sendResponse(exchange, 200, "{\"message\": \"Paper added successfully\"}");
                    return;
                }

                // GET /papers/search?subject=...&year=...&semester=...
                if ("GET".equalsIgnoreCase(method) && path.equals("/papers/search")) {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = new HashMap<>();
                    if (query != null && !query.isEmpty()) {
                        for (String kv : query.split("&")) {
                            String[] parts = kv.split("=", 2);
                            String key = java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name());
                            String val = parts.length == 2 ? java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()) : "";
                            params.put(key, val);
                        }
                    }

                    String subject = params.getOrDefault("subject", "").trim();
                    String yearStr = params.getOrDefault("year", "").trim();
                    String semStr = params.getOrDefault("semester", "").trim();

                    if (subject.isEmpty() || yearStr.isEmpty() || semStr.isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\": \"subject, year, and semester are required\"}");
                        return;
                    }

                    int year = Integer.parseInt(yearStr);
                    int semester = Integer.parseInt(semStr);
                    String response = objectMapper.writeValueAsString(service.search(subject, year, semester));
                    sendResponse(exchange, 200, response);
                    return;
                }

                // POST /papers/{id}/email
                Pattern p = Pattern.compile("^/papers/(\\d+)/email/?$");
                java.util.regex.Matcher m = p.matcher(path);
                if ("POST".equalsIgnoreCase(method) && m.find()) {
                    int id = Integer.parseInt(m.group(1));
                    // read recipient from query param
                    String query = exchange.getRequestURI().getQuery();
                    String recipient = null;
                    if (query != null) {
                        for (String kv : query.split("&")) {
                            String[] parts = kv.split("=", 2);
                            if (parts.length == 2 && parts[0].equals("recipientEmail")) {
                                recipient = java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name());
                            }
                        }
                    }
                    if (recipient == null || recipient.isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\": \"recipientEmail query parameter is required\"}");
                        return;
                    }

                    QuestionPaper paper = service.getPaperById(id);
                    if (paper == null) {
                        sendResponse(exchange, 404, "{\"error\": \"Paper not found\"}");
                        return;
                    }

                    boolean ok = EmailService.sendQuestionPaper(recipient, paper);
                    if (ok) sendResponse(exchange, 200, "{\"message\": \"Email sent\"}");
                    else sendResponse(exchange, 500, "{\"error\": \"Failed to send email\"}");
                    return;
                }

                // DELETE /papers/{id} -> delete a paper
                Pattern pDel = Pattern.compile("^/papers/(\\d+)/?$");
                java.util.regex.Matcher mDel = pDel.matcher(path);
                if ("DELETE".equalsIgnoreCase(method) && mDel.find()) {
                    int id = Integer.parseInt(mDel.group(1));
                    try {
                        service.deletePaperById(id);
                        sendResponse(exchange, 200, "{\"message\": \"Paper deleted\"}");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        sendResponse(exchange, 500, "{\"error\": \"Failed to delete paper\"}");
                    }
                    return;
                }

                sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"" + ex.getMessage() + "\"}");
            }
        };
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}