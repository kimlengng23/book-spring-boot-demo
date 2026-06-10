DROP DATABASE IF EXISTS BookAppDemo;
CREATE DATABASE BookAppDemo;
USE BookAppDemo;

CREATE TABLE Students (
    id BIGINT NOT NULL AUTO_INCREMENT,
    studentNumber VARCHAR(50) NOT NULL,
    photoFileName VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    passwordSalt VARCHAR(255) NOT NULL,
    isVerified BOOLEAN NOT NULL DEFAULT FALSE,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    dateCreated DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT chk_students_student_number_digits CHECK (studentNumber REGEXP '^[0-9]{3,}$'),
    CONSTRAINT chk_students_phone_digits CHECK (phone REGEXP '^[0-9]{9,10}$'),
    UNIQUE KEY uk_students_student_number (studentNumber),
    UNIQUE KEY uk_students_phone (phone),
    UNIQUE KEY uk_students_email (email)
);

CREATE TABLE Books (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    category VARCHAR(255),
    description VARCHAR(255),
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    dateCreated DATETIME NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id)
);

CREATE TABLE BookReservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    studentId BIGINT NOT NULL,
    bookId BIGINT NOT NULL,
    dateCreated DATETIME NOT NULL DEFAULT NOW(),
    returnedAt DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY(studentId) REFERENCES Students(id),
    FOREIGN KEY(bookId) REFERENCES Books(id)
);

-- Example reserved-books query:
-- SET @studentId = 1;
-- SELECT
--     br.id AS reservationId,
--     b.id AS bookId,
--     b.title,
--     b.author,
--     b.category,
--     b.description,
--     br.dateCreated,
--     DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY) AS dueDate,
--     CASE
--         WHEN CURRENT_DATE BETWEEN
--             DATE_SUB(DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY), INTERVAL 3 DAY)
--             AND DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY)
--         THEN 'true'
--         ELSE 'false'
--     END AS nearDueDate
-- FROM BookReservations br
-- JOIN Books b ON b.id = br.bookId
-- WHERE br.studentId = @studentId;
