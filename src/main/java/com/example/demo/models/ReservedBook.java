package com.example.demo.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservedBook {

    private Long reservationId;
    private Long bookId;
    private String title;
    private String author;
    private String category;
    private String description;
    private LocalDateTime reservedAt;
    private LocalDate dueDate;
    private boolean nearDueDate;

    public ReservedBook() {
    }

    public ReservedBook(
        Long reservationId,
        Long bookId,
        String title,
        String author,
        String category,
        String description,
        LocalDateTime reservedAt,
        LocalDate dueDate,
        boolean nearDueDate
    ) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.reservedAt = reservedAt;
        this.dueDate = dueDate;
        this.nearDueDate = nearDueDate;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isNearDueDate() {
        return nearDueDate;
    }

    public void setNearDueDate(boolean nearDueDate) {
        this.nearDueDate = nearDueDate;
    }
}
