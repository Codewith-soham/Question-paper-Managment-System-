# Question Paper Management System - Presentation Script

## 1. Project Overview
This is a Java application that helps manage question papers digitally. Think of it like a digital filing cabinet for question papers where you can:
- Store question papers
- Search for specific papers
- Send papers via email
- Keep track of all papers in a database

## 2. Project Structure

### Main Components:
```
project/
├── src/           → All Java code files
│   ├── LaunchQPMS.java    → One-click launcher
│   ├── WebServer.java     → Web server implementation
│   ├── EmailService.java  → Email functionality
│   └── ...               → Other core classes
├── lib/           → Required libraries (auto-downloaded)
├── frontend/      → Web interface
└── PDF/           → Storage for PDF files
```

## 3. Code Explanation

### A. Main.java - The Entry Point
```java
public class Main {
    public static void main(String[] args) {
        // Shows menu and handles user choices
    }
}
```
- This is like the reception desk of our application
- Shows a menu with options (Add, Search, View, Delete, Email)
- Takes user input and directs to the right service

### B. QuestionPaper.java - The Data Model
```java
public class QuestionPaper {
    private String subject;
    private int year;
    private int semester;
    private String filePath;
    private String status;
}
```
- Think of this as a form that describes a question paper
- Contains information like:
  * Which subject is it for?
  * Which year?
  * Which semester?
  * Where is the PDF file stored?
  * What's its current status?

### C. DatabaseConnection.java
- Works like a telephone line to our database
- Helps us connect to MySQL database where we store all information
- Keeps username, password, and connection details secure

### D. QuestionPaperDAO.java - Database Operations
- DAO stands for "Data Access Object"
- This is like a librarian who:
  * Adds new papers to the database
  * Finds papers when you search
  * Shows all available papers
  * Removes papers when needed

### E. EmailService.java - Email Functionality
- Works like a digital post office
- Uses Gmail to send question papers
- Can attach PDF files to emails
- Handles all the complex email sending logic

### F. QuestionPaperService.java - Business Logic
- This is like a manager who:
  * Coordinates between user input and database operations
  * Ensures data is valid before saving
  * Handles file uploads
  * Manages email sending process

## 4. Frontend Explanation

### index.html
- The main webpage where users can:
  * Upload new question papers
  * Search existing papers
  * View all papers
  * Send papers via email

### style.css
- Makes the website look good and professional
- Handles layout and design

### main.js
- Makes the website interactive
- Handles user actions like clicks and form submissions

## How the backend (Java) and frontend (HTML/JS) are connected

This project uses a small Java HTTP server to serve the frontend files and provide a tiny JSON API. The frontend (in `frontend/`) talks to the Java backend over HTTP — there is no separate framework required. Here is a simple picture of the connection:

- Browser (requests UI) -> GET /frontend/index.html (static files served by WebServer)
- Frontend JS -> API endpoints (JSON over HTTP) exposed by WebServer

Key endpoints implemented:

- GET /papers
   - What it does: returns a JSON array of all stored question papers.
   - Frontend usage: `renderAllPapers()` calls `fetch('/papers')` and renders the table.

- POST /papers/add
   - What it does: accepts a JSON body with { subject, year, semester, filePath, status } and saves a new record in the database via `QuestionPaperService` -> `QuestionPaperDAO`.
   - Frontend usage: Add form (`add.html`) serializes the fields and POSTs JSON to `/papers/add` (see `initAddForm()` in `main.js`).

- POST /papers/{id}/email?recipientEmail=you@example.com
   - What it does: finds the paper by id, resolves the PDF file from the `PDF/` folder, and calls `EmailService.sendQuestionPaper(...)` to send the PDF as an attachment.
   - Frontend usage: clicking a row's "Send to Email" button opens a modal where the user types the recipient address; the modal form POSTs to this endpoint.

Data flow examples (happy paths):

- Add paper (frontend -> backend -> DB)
   1. User fills Add form and clicks "Add Paper".
   2. `main.js` builds a JSON object and POSTs to `/papers/add`.
   3. `WebServer` reads the JSON, constructs a `QuestionPaper` object and calls `QuestionPaperService.addPaper(paper)`.
   4. `QuestionPaperDAO` runs an INSERT statement into MySQL (`questionpaper.question_paper`).
   5. Server returns a 200 JSON response. Frontend shows a toast and refreshes the list.

- View all papers (backend -> frontend)
   1. `main.js` calls GET `/papers`.
   2. `WebServer` calls `QuestionPaperService.getAllPapers()` which calls `QuestionPaperDAO.viewAllPapers()`.
   3. DAO executes SELECT, builds `QuestionPaper` objects and returns a list.
   4. `WebServer` writes that list as JSON; `main.js` receives and renders the table.

- Send Email (frontend -> backend -> EmailService)
   1. User clicks "Send to Email" and types recipient address.
   2. `main.js` POSTs to `/papers/{id}/email?recipientEmail=...`.
   3. `WebServer` finds the paper by id (via `QuestionPaperService.getPaperById`) and calls `EmailService.sendQuestionPaper(recipient, paper)`.
   4. `EmailService` resolves the requested PDF (tries absolute path, `PDF/` folder, etc.), composes a MIME message and sends it through SMTP.
   5. Server returns success/failure and the frontend shows the result.

Where static files live and how they are served
- Files under `frontend/` (HTML, CSS, JS) are served by the Java `WebServer` at the route prefix `/frontend`. Example:
   - http://localhost:8080/frontend/index.html

PDF storage and resolution
- PDFs must be placed in the `PDF/` directory (project root). `EmailService.resolvePdfFile()` contains a few fallbacks and prints diagnostics showing where it looked. If the file is not found the email will not be sent.

Quick troubleshooting tips for the connection
- If list is empty or fetch fails: ensure `WebServer` is running and reachable at http://localhost:8080. Use browser devtools (Network tab) to inspect the GET /papers request and the server response.
- If Add fails: open the browser console and check the POST to `/papers/add`. You can also call the endpoint from PowerShell or curl to see the JSON reply.
- If Send Email fails: check the server console for messages from `EmailService.resolvePdfFile()` (it prints available PDFs and attempted paths) and for SMTP errors. Validate `SMTP_USER` and `SMTP_PASS` environment variables before starting the server.

Example curl commands (useful for demo or testing):

```bash
# List papers
curl http://localhost:8080/papers

# Add a paper (JSON)
curl -X POST http://localhost:8080/papers/add \
   -H "Content-Type: application/json" \
   -d '{"subject":"DBMS","year":2025,"semester":3,"filePath":"dbms2025.pdf","status":"AVAILABLE"}'

# Send paper id=3 to an email
curl -X POST "http://localhost:8080/papers/3/email?recipientEmail=student@example.com"
```

What to show in your presentation when explaining connectivity
- Show the `frontend/js/main.js` lines where `fetch()` is called (GET /papers, POST /papers/add, POST /papers/{id}/email). Explain the tiny JSON contract (fields and expected responses).
- Show the `WebServer` routing handler for `/papers` and briefly step through the add and email branches so the audience sees how the JSON becomes Java objects and then database rows or email sends.
- Show `EmailService.resolvePdfFile()` output in server console when you click Send — it demonstrates how the backend locates the file you clicked in the frontend.

## 5. How Everything Works Together

1. When you start the application:
   - Main.java shows the menu
   - User selects an option

2. When adding a paper:
   - User enters paper details
   - QuestionPaperService validates the information
   - QuestionPaperDAO saves it to database
   - PDF file is stored in PDF folder

3. When searching:
   - User enters search criteria
   - QuestionPaperDAO searches database
   - Results are shown to user

4. When sending email:
   - User selects a paper to send
   - EmailService prepares the email
   - Attaches PDF file
   - Sends via Gmail

## 6. Key Features to Highlight

1. **Security**
   - Secure database connection
   - Password protection
   - Safe file storage

2. **User-Friendly**
   - Simple menu interface
   - Easy to understand options
   - Clear feedback messages

3. **Reliability**
   - Error handling
   - Data validation
   - Secure email sending

4. **Flexibility**
   - Can handle multiple subjects
   - Supports different years and semesters
   - Easy to extend functionality

## 7. Technical Highlights

1. **Java Core Features Used**
   - Object-Oriented Programming
   - File handling
   - Exception management
   - Database connectivity

2. **Libraries Used**
   - Jakarta Mail for email
   - MySQL Connector for database
   - Web technologies (HTML, CSS, JavaScript)

## 8. Demo Script

1. **Start Application**
   ```bash
   run.bat
   ```

2. **Add Question Paper**
   - Show how to add new paper
   - Explain validation process
   - Show success message

3. **Search Function**
   - Demo search by subject
   - Show multiple results
   - Explain display format

4. **Email Function**
   - Select a paper
   - Enter email address
   - Show successful sending

## 9. Conclusion

This system demonstrates:
- Practical use of Java programming
- Database management
- File handling
- Email integration
- Web interface development

Remember to highlight how this system:
- Saves time in managing question papers
- Reduces paper waste
- Makes sharing papers easier
- Keeps everything organized digitally