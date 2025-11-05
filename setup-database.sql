-- Create database if not exists
CREATE DATABASE IF NOT EXISTS questionpaper;
USE questionpaper;

-- Create question_paper table if not exists
CREATE TABLE IF NOT EXISTS question_paper (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add any indexes
CREATE INDEX idx_subject_year_sem ON question_paper(subject, year, semester);