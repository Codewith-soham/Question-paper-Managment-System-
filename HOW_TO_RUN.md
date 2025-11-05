# How to Run the Question Paper Management System

## Quick Start Guide

### One-Click Launch
The easiest way to run the system is using our launcher:

1. Open the project in VS Code
2. Navigate to `src` folder
3. Run `run-launcher.bat`

Or from command line:
```cmd
cd src
run-launcher.bat
```

This will:
- Download required libraries
- Set up the database
- Start the web server
- Open the web interface automatically

---

## One-click start (recommended)

If you want a single script that downloads libraries, compiles the Java sources and starts the web server and opens the frontend, use the PowerShell helper `start-all.ps1`.

From the project root (PowerShell):

```powershell
# (Optional) - set SMTP credentials before starting if you will send email
$env:SMTP_USER = 'your.email@example.com'
$env:SMTP_PASS = 'your_smtp_password_or_app_password'

# Run the one-click starter
.\start-all.ps1
```

Notes:
- The script will not start MySQL for you. Make sure MySQL is running and import `setup-database.sql` before using the web UI.
- If you already have the `lib/` folder populated, `start-all.ps1` will skip the download step.


## Running the Application

### Option 1: Console Application (Command Line)

This is the main console-based application with menu options:

```cmd
run-main.bat
```

**What it does:**
- Shows a menu with options:
  1. Add Question Paper
  2. Search Question Paper
  3. View All Question Papers
  4. Delete Question Paper
  5. Send Question Paper via Email ✉️
  6. Exit

**Requirements:**
- MySQL database running
- Database connection configured in `DatabaseConnection.java`
- PDF files in the PDF folder

---

### Option 2: Web Application with Email Server

To use the web frontend with email functionality:

#### A. Start the Email Server

**Option A - Interactive (Prompts if port is in use):**
```cmd
run-email-server.bat
```

**Option B - Automatic (Auto-kills existing process):**
```cmd
run-email-server-auto.bat
```

**What it does:**
- Starts an HTTP server on port 8000
- Handles email sending requests from the web frontend
- Shows: `✅ EmailServer running on port 8000`

**Important:** Keep this running while using the web frontend!

#### B. Open the Web Frontend

1. Navigate to the `frontend` folder
2. Open `index.html` in your web browser
   - Right-click → Open with → Your browser
   - Or double-click if HTML files open in browser by default

3. You can now:
   - Add question papers
   - Search for papers
   - View all papers
   - **Send papers via email** (requires EmailServer to be running)

---

## Complete Setup Instructions

### Prerequisites

1. **Java Installation**
   - Install Java Development Kit (JDK) 8 or higher
   - Add Java to your system PATH

2. **MySQL Setup**
   - Install MySQL Server
   - Use these default credentials:
     - Username: root
     - Password: soham1234
     - Port: 3306

3. **Email Configuration (Optional)**
   - Gmail account with App Password
   - Set environment variables:
     ```
     SMTP_USER=your.email@gmail.com
     SMTP_PASS=your_app_password
     ```

### Running the Application

1. **Start the System**
   ```cmd
   cd src
   run-launcher.bat
   ```

2. **Access the Web Interface**
   - Browser will open automatically to:
     http://localhost:8080/frontend/index.html
   - If not, open the URL manually

3. **Using the Interface**
   - Add new papers
   - Search existing papers
   - Send papers via email
   - View all papers

---

## Troubleshooting

### Port 8000 Already in Use

**Solution 1 - Use auto-kill script:**
```cmd
run-email-server-auto.bat
```

**Solution 2 - Manual kill:**
```cmd
kill-email-server.bat
```

**Solution 3 - Check what's using port 8000:**
```cmd
netstat -ano | findstr ":8000"
```

### Email Not Working

1. **Check EmailServer is running:**
   - You should see: `✅ EmailServer running on port 8000`

2. **Check email configuration:**
   - EmailServer uses environment variables or fallback credentials
   - Default: Uses Gmail with app password

3. **Check browser console:**
   - Press F12 in browser
   - Look for errors in Console tab

### Database Connection Issues

1. **Ensure MySQL is running**
2. **Check database credentials** in `DatabaseConnection.java`:
   - Database: `questionpaper`
   - User: `root`
   - Password: `soham1234` (change if needed)

---

## File Structure

```
Question Paper Managment System/
├── src/                    # Java source files
│   ├── LaunchQPMS.java    # One-click launcher
│   ├── WebServer.java     # Web server
│   └── *.java             # Other Java files
├── lib/                    # JAR dependencies (auto-downloaded)
├── PDF/                    # PDF question papers
├── frontend/              # Web interface
│   ├── index.html
│   ├── add.html
│   ├── search.html
│   ├── js/
│   │   └── main.js
│   └── style.css
└── .vscode/               # VS Code configuration
    └── launch.json        # Launch configuration
```

## Quick Reference

| Task | Command |
|------|---------|
| Start System | `cd src && run-launcher.bat` |
| Web Interface | http://localhost:8080/frontend/index.html |
| Database | MySQL on port 3306 |
| PDF Storage | Place files in PDF/ folder |

---

## Notes

- **Email Server** must be running for email functionality in web frontend
- **Console app** doesn't need EmailServer (has its own email option)
- Both applications can run simultaneously
- Port 8000 must be free for EmailServer
- PDF files should be in the `PDF` folder

---

## Example Usage Flow

### Console Application:
```
1. compile.bat
2. run-main.bat
3. Select option from menu (1-6)
4. Follow prompts
```

### Web Application:
```
1. compile.bat
2. run-email-server.bat (keep running)
3. Open frontend/index.html in browser
4. Use web interface to manage papers
5. Click "Send to Email" to email papers
```

Happy coding! 🚀

