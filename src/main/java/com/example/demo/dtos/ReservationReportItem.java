package com.example.demo.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationReportItem(
    Long reservationId,
    Long studentId,
    String studentName,
    Long bookId,
    String bookTitle,
    LocalDateTime reservedAt,
    LocalDate dueDate,
    LocalDateTime returnedAt
) {
}
