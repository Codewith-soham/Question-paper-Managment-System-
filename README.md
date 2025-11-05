<<<<<<< HEAD
# Question-Paper-Managment-System-
A  system used to add , view and send question papers via email to students .
=======
# Question Paper Management System (Spring Boot Version)

A Spring Boot application for managing and emailing question papers.

## Features

- Add question papers with metadata
- Search papers by subject, year, and semester
- View all papers in a table format
- Send papers via email
- RESTful API backend
- Modern web frontend

## Prerequisites

- Java 17 or newer
- Maven
- MySQL Database
- Modern web browser
- PDF viewer

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd question-paper-management
   ```

2. **Configure MySQL:**
   - Create a database named `questionpaper`
   - Update `src/main/resources/application.properties` if your credentials differ
   - Default credentials:
     ```properties
     spring.datasource.username=root
     spring.datasource.password=soham1234
     ```

3. **Configure Email (Gmail):**
   - Enable 2FA on your Gmail account
   - Generate an App Password
   - Update `application.properties` or set environment variables:
     ```properties
     spring.mail.username=your.email@gmail.com
     spring.mail.password=your-app-password
     ```
   Or set environment variables:
   ```bash
   export SMTP_USER=your.email@gmail.com
   export SMTP_PASS=your-app-password
   ```

4. **Build and Run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The application will start on http://localhost:8080

5. **Access the Web Interface:**
   - Open `frontend/index.html` in your browser
   - Or serve it using any static file server:
     ```bash
     # Using Python (option 1)
     cd frontend
     python -m http.server 8000

     # Using Node.js (option 2)
     npx http-server frontend -p 8000
     ```

## API Endpoints

### Question Papers

- `GET /api/papers` - List all papers
- `GET /api/papers/search?subject=&year=&semester=` - Search papers
- `POST /api/papers` - Add new paper
- `DELETE /api/papers/{id}` - Delete paper
- `POST /api/papers/{id}/email?recipientEmail=` - Email paper

### Example Requests

Add Paper:
```bash
curl -X POST http://localhost:8080/api/papers \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Database Management",
    "year": 2025,
    "semester": 1,
    "filePath": "dbms2025.pdf",
    "status": "AVAILABLE"
  }'
```

Send Email:
```bash
curl -X POST "http://localhost:8080/api/papers/1/email?recipientEmail=student@example.com"
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── qpms/
│   │           ├── QuestionPaperApplication.java
│   │           ├── controller/
│   │           │   └── QuestionPaperController.java
│   │           ├── model/
│   │           │   └── QuestionPaper.java
│   │           ├── repository/
│   │           │   └── QuestionPaperRepository.java
│   │           └── service/
│   │               ├── EmailService.java
│   │               └── QuestionPaperService.java
│   └── resources/
│       └── application.properties
frontend/
├── index.html
├── add.html
├── search.html
├── js/
│   └── main.js
└── style.css
PDF/
└── (your PDF files)
```

## Development

### Adding New Features

1. Create entity classes in `model/`
2. Create repository interfaces extending `JpaRepository`
3. Implement service layer logic in `service/`
4. Add REST endpoints in `controller/`
5. Update frontend JavaScript as needed

### Building for Production

```bash
# Create production build
mvn clean package

# Run the JAR
java -jar target/question-paper-management-1.0.0.jar
```

## Troubleshooting

### Email Issues

1. Verify SMTP credentials in `application.properties`
2. For Gmail:
   - Ensure 2FA is enabled
   - Use App Password, not regular password
   - Check if less secure app access is needed

### Database Issues

1. Verify MySQL is running:
   ```bash
   sudo systemctl status mysql
   ```
2. Check database exists:
   ```sql
   mysql -u root -p
   SHOW DATABASES;
   ```
3. Verify table structure:
   ```sql
   USE questionpaper;
   SHOW TABLES;
   DESCRIBE question_paper;
   ```

### PDF Access

1. Ensure PDFs exist in the `PDF` directory
2. Check file permissions
3. Verify file paths in database match actual files

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
>>>>>>> 87357bd (Question Paper Management System.)
