package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.ReservationReportItem;
import com.example.demo.models.Book;
import com.example.demo.models.ReservedBook;
import com.example.demo.services.BookService;
import com.example.demo.services.ReservationReportServiceClient;
import com.example.demo.services.StudentService;

@RestController
@RequestMapping("/api/book/")
public class BookController {

    private final BookService bookService;
    private final StudentService studentService;
    private final ReservationReportServiceClient reservationReportServiceClient;

    public BookController(
        BookService bookService,
        StudentService studentService,
        ReservationReportServiceClient reservationReportServiceClient
    ) {
        this.bookService = bookService;
        this.studentService = studentService;
        this.reservationReportServiceClient = reservationReportServiceClient;
    }

    @GetMapping("/get/all")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/get/by/id/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/get/reserved")
    public ResponseEntity<List<ReservedBook>> getReservedBooks(@RequestAttribute("studentId") Long studentId) {
        if (studentService.getStudentById(studentId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bookService.getReservedBooksByStudentId(studentId));
    }
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    @PutMapping("/update")
    public ResponseEntity<Book> updateBook(@RequestBody Book bookDetails) {
        return bookService.updateBook(bookDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reserve/by/id/{bookId}")
    public ResponseEntity<Void> reserveBook(
        @PathVariable Long bookId,
        @RequestAttribute("studentId") Long studentId
    ) {
        if (!bookService.reserveBook(studentId, bookId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/return/by/id/{reservationId}")
    public ResponseEntity<Void> returnBook(
        @PathVariable Long reservationId,
        @RequestAttribute("studentId") Long studentId
    ) {
        if (!bookService.returnBook(studentId, reservationId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations/report")
    public List<ReservationReportItem> getReservationsReport() {
        return reservationReportServiceClient.getAllReservations();
    }
}
