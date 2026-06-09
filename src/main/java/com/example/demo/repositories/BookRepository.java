package com.example.demo.repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import com.example.demo.models.Book;
import com.example.demo.models.ReservedBook;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createTables() {
        String createBookSql = """
            CREATE TABLE IF NOT EXISTS book (
                id BIGINT NOT NULL AUTO_INCREMENT,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255),
                category VARCHAR(255),
                description VARCHAR(255),
                PRIMARY KEY (id)
            ) ENGINE=InnoDB
            """;
        jdbcTemplate.execute(createBookSql);

        String createReservationSql = """
            CREATE TABLE IF NOT EXISTS book_reservation (
                id BIGINT NOT NULL AUTO_INCREMENT,
                student_id BIGINT NOT NULL,
                book_id BIGINT NOT NULL,
                reserved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id)
            ) ENGINE=InnoDB
            """;

        jdbcTemplate.execute(createReservationSql);
    }

    public List<Book> findAll() {
        String sql = """
            SELECT id, title, author, category, description
            FROM book
            ORDER BY id
            """;

        return jdbcTemplate.query(sql, this::mapRowToBook);
    }

    public Optional<Book> findById(Long id) {
        String sql = """
            SELECT id, title, author, category, description
            FROM book
            WHERE id = ?
            """;

        try {
            Book book = jdbcTemplate.queryForObject(sql, this::mapRowToBook, id);
            return Optional.of(book);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Book save(Book book) {
        String sql = """
            INSERT INTO book (title, author, category, description)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getCategory());
            statement.setString(4, book.getDescription());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            book.setId(generatedId.longValue());
        }

        return book;
    }

    public boolean update(Long id, Book book) {
        String sql = """
            UPDATE book
            SET title = ?, author = ?, category = ?, description = ?
            WHERE id = ?
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            book.getTitle(),
            book.getAuthor(),
            book.getCategory(),
            book.getDescription(),
            id
        );

        return updatedRows > 0;
    }

    public boolean deleteById(Long id) {
        String sql = """
            DELETE FROM book
            WHERE id = ?
            """;

        int deletedRows = jdbcTemplate.update(sql, id);
        return deletedRows > 0;
    }

    public boolean existsBookById(Long bookId) {
        String sql = """
            SELECT COUNT(*)
            FROM book
            WHERE id = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }

    public boolean existsStudentById(Long studentId) {
        String sql = """
            SELECT COUNT(*)
            FROM student
            WHERE id = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;
    }

    public Optional<ReservedBook> reserveBook(Long studentId, Long bookId) {
        String sql = """
            INSERT INTO book_reservation (student_id, book_id)
            VALUES (?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, studentId);
            statement.setLong(2, bookId);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            return Optional.empty();
        }

        return findReservedBookByReservationId(generatedId.longValue());
    }

    public List<ReservedBook> findReservedBooksByStudentId(Long studentId) {
        String sql = """
            SELECT
                br.id AS reservation_id,
                b.id AS book_id,
                b.title,
                b.author,
                b.category,
                b.description,
                br.reserved_at,
                DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY) AS due_date,
                CASE
                    WHEN CURRENT_DATE BETWEEN
                        DATE_SUB(DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY), INTERVAL 3 DAY)
                        AND DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY)
                    THEN 'true'
                    ELSE 'false'
                END AS near_due_date
            FROM book_reservation br
            JOIN book b ON b.id = br.book_id
            WHERE br.student_id = ?
            ORDER BY br.reserved_at DESC
            """;

        return jdbcTemplate.query(sql, this::mapRowToReservedBook, studentId);
    }

    private Optional<ReservedBook> findReservedBookByReservationId(Long reservationId) {
        String sql = """
            SELECT
                br.id AS reservation_id,
                b.id AS book_id,
                b.title,
                b.author,
                b.category,
                b.description,
                br.reserved_at,
                DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY) AS due_date,
                CASE
                    WHEN CURRENT_DATE BETWEEN
                        DATE_SUB(DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY), INTERVAL 3 DAY)
                        AND DATE_ADD(DATE(br.reserved_at), INTERVAL 14 DAY)
                    THEN 'true'
                    ELSE 'false'
                END AS near_due_date
            FROM book_reservation br
            JOIN book b ON b.id = br.book_id
            WHERE br.id = ?
            """;

        try {
            ReservedBook reservedBook = jdbcTemplate.queryForObject(sql, this::mapRowToReservedBook, reservationId);
            return Optional.of(reservedBook);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private Book mapRowToBook(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new Book(
            resultSet.getLong("id"),
            resultSet.getString("title"),
            resultSet.getString("author"),
            resultSet.getString("category"),
            resultSet.getString("description")
        );
    }

    private ReservedBook mapRowToReservedBook(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        Timestamp reservedAt = resultSet.getTimestamp("reserved_at");

        return new ReservedBook(
            resultSet.getLong("reservation_id"),
            resultSet.getLong("book_id"),
            resultSet.getString("title"),
            resultSet.getString("author"),
            resultSet.getString("category"),
            resultSet.getString("description"),
            reservedAt.toLocalDateTime(),
            resultSet.getDate("due_date").toLocalDate(),
            Boolean.parseBoolean(resultSet.getString("near_due_date"))
        );
    }
}
