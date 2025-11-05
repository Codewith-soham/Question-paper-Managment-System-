## Question Paper Management System — README

This repository is a lightweight Java-based Question Paper Management System (QPMS). It uses plain Java for the backend, simple static HTML/CSS/vanilla JS for the frontend, MySQL as the persistence layer, and Jakarta Mail for SMTP email sending.

This README documents the architecture, technology stack, where code lives, how to run the app (Windows), available scripts and endpoints, troubleshooting tips, and recommended next steps.

## High-level architecture

- Frontend: static HTML/CSS/vanilla JavaScript located under `frontend/`.
- Backend: Java classes under `src/` that use the JDK's embedded HTTP server (`com.sun.net.httpserver`) to serve static files and implement a REST-like API for question papers.
- Database: MySQL, accessed via JDBC and MySQL Connector/J (jar in `lib/`).
- Email: Jakarta Mail + Activation (jars in `lib/`) used by `EmailService` to send attachments from the server.
- File store: PDFs are stored in the project `PDF/` directory and served/attached by the backend.

## Project layout

- `src/` — Java source files (WebServer, EmailService, DAO, Service classes, Main, LaunchQPMS, etc.)
- `frontend/` — static UI files (HTML, CSS, `js/main.js`)
- `lib/` — external jars used at runtime (mysql connector, jakarta.mail, jakarta.activation)
- `PDF/` — place your attachment PDFs here
- `run-main.bat`, `start-all.bat`, `src/run-launcher.bat` — convenience scripts to compile/run on Windows
- `setup-database.sql` — SQL to create the required tables (run manually with mysql client)

## Technology stack

- Language/runtime: Java (JDK 8+ recommended)
- HTTP server: com.sun.net.httpserver (JDK built-in)
- DB: MySQL (server)
- JDBC driver: mysql-connector-j (Connector/J jar)
- Email: Jakarta Mail + Jakarta Activation
- Frontend: HTML, CSS, vanilla JavaScript (fetch API)
- VCS: Git

## Important files to inspect

- `src/WebServer.java` — routes static files and API endpoints. Key endpoints:
  - `GET /papers` — list papers (JSON)
  - `POST /papers/add` — add paper (form data)
  - `DELETE /papers/{id}` — delete paper by id
  - `POST /papers/{id}/email` — send paper by email
  - `GET /frontend/...` — static frontend files
- `src/QuestionPaperDAO.java` / `src/QuestionPaperService.java` — DB access and business logic
- `src/EmailService.java` — SMTP send logic and PDF resolution
- `src/LaunchQPMS.java` — Java launcher (one-file) useful for VS Code Run
- `frontend/js/main.js` — UI logic; fetches API, renders lists, sends email, delete button wired here

## How the code runs (flow)

1. Web server (`WebServer`) starts and registers HTTP handlers for static files and API endpoints.
2. The frontend (static files) calls backend endpoints via fetch() to list/add/delete papers and request emails.
3. `QuestionPaperDAO` handles SQL queries to MySQL. `QuestionPaperService` provides higher-level operations and calls DAO.
4. When email is requested, `EmailService` locates the PDF in the `PDF/` folder (or other candidate paths), builds a JavaMail message and sends via SMTP using credentials provided by environment variables.

## Environment prerequisites

- JDK (8+) installed and available on PATH (java, javac)
- MySQL server installed and running
  - Default DB user used in code may be `root` with a configured password — check `src/DatabaseConnection.java` and update credentials if needed
  - Run `setup-database.sql` to create required tables. Example:

```powershell
mysql -u root -p < setup-database.sql
```

- `lib/` must contain the required jars if not using the launcher which downloads them:
  - `mysql-connector-j-*.jar`
  - `jakarta.mail-*.jar`
  - `jakarta.activation-*.jar`

## How to run (Windows)

1) Quick start (one-click batch helper):

 - Double-click `start-all.bat` in the project root. This will:
   - Ensure jars are present (downloads if your launcher does so), compile sources, and start the web server in a new console window.
   - It prints a manual SQL command to run for DB setup (safer than auto-run).

2) From VS Code (Run button):

 - Use the provided `LaunchQPMS` configuration (if present) to run `src/LaunchQPMS.java`. This launcher downloads jars if needed and starts `WebServer`.

3) Manual (cmd/powershell):

```powershell
cd "C:\Java Programs\Question Paper Managment System\src"
javac -cp .;..\lib\* *.java
java -cp .;..\lib\* WebServer
```

4) Run the console `Main` (interactive console app):

 - Double-click `run-main.bat` in the project root (it compiles and runs `Main`).

## Frontend usage

- Open http://localhost:8080/frontend/index.html
- Add paper via the UI (Add page) or upload from the frontend.
- Use the Delete button (added to the table UI) to delete an entry. It triggers `DELETE /papers/{id}`.
- Click the Send to Email button to open a modal and send the paper to an address via SMTP.

## API quick reference

- GET /papers — returns JSON list
- POST /papers/add — multipart/form-data to add a paper (fields: subject, year, semester, status, filePath?)
- DELETE /papers/{id} — delete by ID
- POST /papers/{id}/email — send email for specific paper id (body contains to/email details)

Use browser devtools or curl to test endpoints.

Example curl (list):

```powershell
curl -i http://localhost:8080/papers
```

Example curl (delete):

```powershell
curl -X DELETE http://localhost:8080/papers/3
```

## Environment variables / configuration

- SMTP credentials (used by `EmailService`): set `SMTP_USER` and `SMTP_PASS` in the environment before starting the server. Example (PowerShell):

```powershell
$env:SMTP_USER = "your-smtp-username"
$env:SMTP_PASS = "your-smtp-password"
```

- Database credentials: configure in `src/DatabaseConnection.java` or update the code to read env vars.

## File & resource resolution notes

- The server tries to be robust about where it's started from. `EmailService` and `WebServer` attempt multiple candidate paths for `frontend/` and `PDF/` so the server works whether started from `src/` or project root.

## Troubleshooting

- "No suitable driver found for jdbc:mysql://...": ensure MySQL connector jar is on the classpath (`lib/mysql-connector-j-*.jar`) and that you start Java with `-cp .;..\lib\*` when running from `src`.
- 500/DB errors: check the `WebServer` console window for stack traces and SQL statements; verify MySQL is running and `setup-database.sql` has been applied.
- Email fails: check SMTP creds and the server console for `MessagingException` details; ensure PDFs exist in `PDF/` or in candidate paths.
- PowerShell vs cmd differences: some repository scripts assume cmd.exe semantics; for predictable results on Windows use the provided `.bat` files or open cmd.exe.

## Testing & validation

- There are no automated tests currently in the repo. Manual test plan:
  - Start server
  - Open frontend and confirm list loads
  - Add a paper and confirm DB row appears
  - Delete a paper and confirm it is removed (and DB row gone)
  - Send email and confirm SMTP logs show success and recipient receives mail

## Recommendations / next steps

- Add a simple build tool (Maven or Gradle) to manage dependencies and build lifecycle.
- Add automated tests (unit tests for service/DAO and an integration test for endpoints).
- Improve DB credential handling (read from env vars or external config file instead of hard-coded values).
- Add a small health endpoint (e.g., `/health`) that checks DB connectivity and returns a succinct JSON response for monitoring.
- Optionally containerize the app (Docker) with a prepared MySQL image to make local dev reproducible.

## Contact / author

Repo owner: Codewith-soham

---
If you'd like, I can:
- create a small `README_SHORT.md` with just the run commands,
- add a `pom.xml` or `build.gradle` to convert this to a Maven/Gradle build,
- or add a small `health` endpoint and minimal tests.
Tell me which and I'll implement it.
<<<<<<< HEAD
# Question-Paper-Managment-System-
A  system used to add , view and send question papers via email to students .
=======
