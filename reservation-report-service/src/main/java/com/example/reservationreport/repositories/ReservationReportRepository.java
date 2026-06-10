package com.example.reservationreport.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.reservationreport.dtos.ReservationReportItem;

@Repository
public class ReservationReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ReservationReportItem> getAllReservations() {
        String sql = """
            SELECT
                br.id AS reservationId,
                s.id AS studentId,
                s.name AS studentName,
                b.id AS bookId,
                b.title AS bookTitle,
                br.dateCreated AS reservedAt,
                DATE_ADD(DATE(br.dateCreated), INTERVAL 14 DAY) AS dueDate,
                br.returnedAt
            FROM BookReservations br
            JOIN Students s ON s.id = br.studentId
            JOIN Books b ON b.id = br.bookId
            ORDER BY br.dateCreated DESC
            """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> {
            Timestamp returnedAt = resultSet.getTimestamp("returnedAt");

            return new ReservationReportItem(
                resultSet.getLong("reservationId"),
                resultSet.getLong("studentId"),
                resultSet.getString("studentName"),
                resultSet.getLong("bookId"),
                resultSet.getString("bookTitle"),
                resultSet.getTimestamp("reservedAt").toLocalDateTime(),
                resultSet.getDate("dueDate").toLocalDate(),
                returnedAt == null ? null : returnedAt.toLocalDateTime()
            );
        });
    }
}
