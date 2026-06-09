CREATE DATABASE IF NOT EXISTS demo;
USE demo;

CREATE TABLE IF NOT EXISTS student (
    id BIGINT NOT NULL AUTO_INCREMENT,
    photo_file_name VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    category VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book_reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reserved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_book_reservation_student_id (student_id),
    KEY idx_book_reservation_book_id (book_id),
    CONSTRAINT fk_book_reservation_student
        FOREIGN KEY (student_id) REFERENCES student (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_book_reservation_book
        FOREIGN KEY (book_id) REFERENCES book (id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Example reserved-books query:
-- SET @student_id = 1;
-- SELECT
--     br.id AS reservation_id,
--     b.id AS book_id,
--     b.title,
--     b.author,
--     b.category,
--     b.description,
--     br.reserved_at,
--     DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY) AS due_date,
--     CASE
--         WHEN CURRENT_DATE BETWEEN
--             DATE_SUB(DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY), INTERVAL 3 DAY)
--             AND DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY)
--         THEN 'true'
--         ELSE 'false'
--     END AS near_due_date
-- FROM book_reservation br
-- JOIN book b ON b.id = br.book_id
-- WHERE br.student_id = @student_id;
