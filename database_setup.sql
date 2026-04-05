CREATE DATABASE IF NOT EXISTS registration_db;
USE registration_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'admin') NOT NULL DEFAULT 'student'
);

CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE IF NOT EXISTS registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL DEFAULT '',
    course_id VARCHAR(20) NOT NULL,
    registration_date DATE NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    UNIQUE KEY unique_registration (student_id, course_id)
);

-- Insert default admin
INSERT IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin');

-- Pre-populate 24 courses (6 per semester, 4 semesters)
INSERT IGNORE INTO courses (course_id, course_name, semester, credits) VALUES
-- Semester 1
('CS101', 'Introduction to Programming', 1, 3),
('CS102', 'Data Structures', 1, 3),
('MAT101', 'Calculus I', 1, 4),
('PHY101', 'Physics I', 1, 4),
('ENG101', 'English Composition', 1, 2),
('HUM101', 'Introduction to Humanities', 1, 2),
-- Semester 2
('CS201', 'Algorithms', 2, 3),
('CS202', 'Object Oriented Programming', 2, 3),
('MAT201', 'Linear Algebra', 2, 3),
('PHY201', 'Physics II', 2, 4),
('ENG201', 'Technical Writing', 2, 2),
('EE201', 'Basic Electronics', 2, 3),
-- Semester 3
('CS301', 'Database Systems', 3, 3),
('CS302', 'Operating Systems', 3, 3),
('CS303', 'Computer Networks', 3, 3),
('MAT301', 'Probability and Statistics', 3, 3),
('CS304', 'Software Engineering', 3, 3),
('CS305', 'Web Technologies', 3, 3),
-- Semester 4
('CS401', 'Artificial Intelligence', 4, 3),
('CS402', 'Machine Learning', 4, 3),
('CS403', 'Cloud Computing', 4, 3),
('CS404', 'Cyber Security', 4, 3),
('CS405', 'Project Management', 4, 2),
('CS406', 'Capstone Project', 4, 4);

-- student_name column is already defined in the CREATE TABLE above.
