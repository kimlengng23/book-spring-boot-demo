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

    public boolean reserveBook(Long studentId, Long bookId) {
        if (!bookRepository.existsStudentById(studentId) || !bookRepository.existsBookById(bookId)) {
            return false;
        }

        if (bookRepository.isBookCurrentlyReserved(bookId)) {
            return false;
        }

        if (bookRepository.countActiveReservationsByStudentId(studentId) >= MAX_ACTIVE_RESERVATIONS_PER_STUDENT) {
            return false;
        }

        return bookRepository.reserveBook(studentId, bookId);
    }

    public boolean returnBook(Long studentId, Long reservationId) {
        if (!bookRepository.existsStudentById(studentId)) {
            return false;
        }

        return bookRepository.returnBook(studentId, reservationId);
    }

    public List<ReservedBook> getReservedBooksByStudentId(Long studentId) {
        return bookRepository.getReservedBooksByStudentId(studentId);
    }
}
