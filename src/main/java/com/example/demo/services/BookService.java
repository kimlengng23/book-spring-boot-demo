package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.models.Book;
import com.example.demo.models.ReservedBook;
import com.example.demo.repositories.BookRepository;

@Service
public class BookService {

    private static final int MAX_ACTIVE_RESERVATIONS_PER_STUDENT = 3;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.getAllBooks();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.getBookById(id);
    }

    public Book createBook(Book book) {
        book.setId(null);
        return bookRepository.createBook(book);
    }

    public Optional<Book> updateBook(Book bookDetails) {
        boolean updated = bookRepository.updateBook(bookDetails);
        if (!updated) {
            return Optional.empty();
        }

        return bookRepository.getBookById(bookDetails.getId());
    }

    public boolean deleteBook(Long id) {
        return bookRepository.deleteBookById(id);
    }

    public ReservationResult reserveBook(Long studentId, Long bookId) {
        if (!bookRepository.existsStudentById(studentId) || !bookRepository.existsBookById(bookId)) {
            return new ReservationResult(ReservationStatus.NOT_FOUND, null);
        }

        if (bookRepository.countActiveReservationsByStudentId(studentId) >= MAX_ACTIVE_RESERVATIONS_PER_STUDENT) {
            return new ReservationResult(ReservationStatus.LIMIT_REACHED, null);
        }

        if (bookRepository.isBookCurrentlyReserved(bookId)) {
            return new ReservationResult(ReservationStatus.BOOK_ALREADY_RESERVED, null);
        }

        return bookRepository.reserveBook(studentId, bookId)
            .map(reservation -> new ReservationResult(ReservationStatus.SUCCESS, reservation))
            .orElse(new ReservationResult(ReservationStatus.BOOK_ALREADY_RESERVED, null));
    }

    public Optional<ReservedBook> returnBook(Long studentId, Long reservationId) {
        if (!bookRepository.existsStudentById(studentId)) {
            return Optional.empty();
        }

        return bookRepository.returnBook(studentId, reservationId);
    }

    public List<ReservedBook> getReservedBooksByStudentId(Long studentId) {
        return bookRepository.getReservedBooksByStudentId(studentId);
    }

    public record ReservationResult(ReservationStatus status, ReservedBook reservation) {
    }

    public enum ReservationStatus {
        SUCCESS,
        NOT_FOUND,
        LIMIT_REACHED,
        BOOK_ALREADY_RESERVED
    }
}
