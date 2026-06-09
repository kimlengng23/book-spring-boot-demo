package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.models.Book;
import com.example.demo.models.ReservedBook;
import com.example.demo.repositories.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book createBook(Book book) {
        book.setId(null);
        return bookRepository.save(book);
    }

    public Optional<Book> updateBook(Long id, Book bookDetails) {
        boolean updated = bookRepository.update(id, bookDetails);
        if (!updated) {
            return Optional.empty();
        }

        return bookRepository.findById(id);
    }

    public boolean deleteBook(Long id) {
        return bookRepository.deleteById(id);
    }

    public Optional<ReservedBook> reserveBook(Long studentId, Long bookId) {
        if (!bookRepository.existsStudentById(studentId) || !bookRepository.existsBookById(bookId)) {
            return Optional.empty();
        }

        return bookRepository.reserveBook(studentId, bookId);
    }

    public List<ReservedBook> getReservedBooksByStudentId(Long studentId) {
        return bookRepository.findReservedBooksByStudentId(studentId);
    }
}
