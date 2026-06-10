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

import com.example.demo.models.Book;
import com.example.demo.models.ReservedBook;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> getAllBooks() {
        String sql = """
            SELECT id, title, author, category, description, isActive
            FROM Books
            WHERE isActive = TRUE
            ORDER BY id
            """;

        return jdbcTemplate.query(sql, this::mapRowToBook);
    }

    public Optional<Book> getBookById(Long id) {
        String sql = """
            SELECT id, title, author, category, description, isActive
            FROM Books
            WHERE id = ? AND isActive = TRUE
        """;

        try {
            Book book = jdbcTemplate.queryForObject(sql, this::mapRowToBook, id);
            return Optional.of(book);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Book createBook(Book book) {
        String sql = """
            INSERT INTO Books (title, author, category, description, isActive)
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getCategory());
            statement.setString(4, book.getDescription());
            statement.setBoolean(5, getActiveValue(book.getIsActive()));
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            book.setId(generatedId.longValue());
        }

        return book;
    }

    public boolean updateBook(Book book) {
        String sql = """
            UPDATE Books
            SET title = ?, author = ?, category = ?, description = ?, isActive = COALESCE(?, isActive)
            WHERE id = ? AND isActive = TRUE
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            book.getTitle(),
            book.getAuthor(),
            book.getCategory(),
            book.getDescription(),
            book.getIsActive(),
            book.getId()
        );

        return updatedRows > 0;
    }

    public boolean deleteBookById(Long id) {
        String sql = """
            UPDATE Books
            SET isActive = FALSE
            WHERE id = ? AND isActive = TRUE
            """;

        int updatedRows = jdbcTemplate.update(sql, id);
        return updatedRows > 0;
    }

    public boolean existsBookById(Long bookId) {
        String sql = """
            SELECT COUNT(*)
            FROM Books
            WHERE id = ? AND isActive = TRUE
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }

    
    public boolean existsStudentById(Long studentId) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE id = ? AND isActive = TRUE
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;
    }

    public boolean isBookCurrentlyReserved(Long bookId) {
        String sql = """
            SELECT COUNT(*)
            FROM BookReservations
            WHERE bookId = ? AND returnedAt IS NULL
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }

    public int countActiveReservationsByStudentId(Long studentId) {
        String sql = """
            SELECT COUNT(*)
            FROM BookReservations
            WHERE studentId = ? AND returnedAt IS NULL
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count == null ? 0 : count;
    }

    public Optional<ReservedBook> reserveBook(Long studentId, Long bookId) {
        String sql = """
            INSERT INTO BookReservations (studentId, bookId)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT 1
                FROM BookReservations
                WHERE bookId = ? AND returnedAt IS NULL
            )
            """;

        int insertedRows = jdbcTemplate.update(sql, studentId, bookId, bookId);
        if (insertedRows <= 0) {
            return Optional.empty();
        }

        return getActiveReservationByStudentIdAndBookId(studentId, bookId);
    }

    public Optional<ReservedBook> returnBook(Long studentId, Long reservationId) {
        String sql = """
            UPDATE BookReservations
            SET returnedAt = NOW()
            WHERE id = ? AND studentId = ? AND returnedAt IS NULL
            """;

        int updatedRows = jdbcTemplate.update(sql, reservationId, studentId);
        if (updatedRows <= 0) {
            return Optional.empty();
        }

        return getReservationById(reservationId);
    }

    public Optional<ReservedBook> getReservationById(Long reservationId) {
        String sql = """
            SELECT
                br.id AS reservationId,
                b.id AS bookId,
                b.title,
                b.author,
                b.category,
                b.description,
                br.dateCreated,
                br.returnedAt,
                DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY) AS dueDate,
                CASE
                    WHEN CURRENT_DATE BETWEEN
                        DATE_SUB(DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY), INTERVAL 3 DAY)
                        AND DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY)
                    THEN 'true'
                    ELSE 'false'
                END AS nearDueDate
            FROM BookReservations br
            JOIN Books b ON b.id = br.bookId
            WHERE br.id = ? AND b.isActive = TRUE
            """;

        try {
            ReservedBook reservation = jdbcTemplate.queryForObject(sql, this::mapRowToReservedBook, reservationId);
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private Optional<ReservedBook> getActiveReservationByStudentIdAndBookId(Long studentId, Long bookId) {
        String sql = """
            SELECT
                br.id AS reservationId,
                b.id AS bookId,
                b.title,
                b.author,
                b.category,
                b.description,
                br.dateCreated,
                br.returnedAt,
                DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY) AS dueDate,
                CASE
                    WHEN CURRENT_DATE BETWEEN
                        DATE_SUB(DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY), INTERVAL 3 DAY)
                        AND DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY)
                    THEN 'true'
                    ELSE 'false'
                END AS nearDueDate
            FROM BookReservations br
            JOIN Books b ON b.id = br.bookId
            WHERE br.studentId = ? AND br.bookId = ? AND br.returnedAt IS NULL AND b.isActive = TRUE
            ORDER BY br.id DESC
            LIMIT 1
            """;

        try {
            ReservedBook reservation = jdbcTemplate.queryForObject(sql, this::mapRowToReservedBook, studentId, bookId);
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<ReservedBook> getReservedBooksByStudentId(Long studentId) {
        String sql = """
            SELECT
                br.id AS reservationId,
                b.id AS bookId,
                b.title,
                b.author,
                b.category,
                b.description,
                br.dateCreated,
                br.returnedAt,
                DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY) AS dueDate,
                CASE
                    WHEN CURRENT_DATE BETWEEN
                        DATE_SUB(DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY), INTERVAL 3 DAY)
                        AND DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY)
                    THEN 'true'
                    ELSE 'false'
                END AS nearDueDate
            FROM BookReservations br
            JOIN Books b ON b.id = br.bookId
            WHERE br.studentId = ? AND b.isActive = TRUE
            ORDER BY br.dateCreated DESC
            """;

        return jdbcTemplate.query(sql, this::mapRowToReservedBook, studentId);
    }

    private Book mapRowToBook(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new Book(
            resultSet.getLong("id"),
            resultSet.getString("title"),
            resultSet.getString("author"),
            resultSet.getString("category"),
            resultSet.getString("description"),
            resultSet.getBoolean("isActive")
        );
    }

    private boolean getActiveValue(Boolean isActive) {
        return isActive == null || isActive;
    }

    private ReservedBook mapRowToReservedBook(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        Timestamp dateCreated = resultSet.getTimestamp("dateCreated");
        Timestamp returnedAt = resultSet.getTimestamp("returnedAt");

        return new ReservedBook(
            resultSet.getLong("reservationId"),
            resultSet.getLong("bookId"),
            resultSet.getString("title"),
            resultSet.getString("author"),
            resultSet.getString("category"),
            resultSet.getString("description"),
            dateCreated.toLocalDateTime(),
            returnedAt == null ? null : returnedAt.toLocalDateTime(),
            resultSet.getDate("dueDate").toLocalDate(),
            Boolean.parseBoolean(resultSet.getString("nearDueDate"))
        );
    }
}
