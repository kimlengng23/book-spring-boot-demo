package com.example.demo.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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
import com.example.demo.services.BookService.ReservationResult;
import com.example.demo.services.BookService.ReservationStatus;
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

    @GetMapping("/get/{bookId}")
    public ResponseEntity<Book> getBookById(@PathVariable Long bookId) {
        return bookService.getBookById(bookId)
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
    @PostMapping("/create")
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    @PutMapping("/update")
    public ResponseEntity<Book> updateBook(@RequestBody Book bookDetails) {
        return bookService.updateBook(bookDetails)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reserve/{bookId}")
    public ResponseEntity<?> reserveBook(
        @PathVariable Long bookId,
        @RequestAttribute("studentId") Long studentId
    ) {
        ReservationResult result = bookService.reserveBook(studentId, bookId);
        if (result.status() == ReservationStatus.SUCCESS) {
            return ResponseEntity.ok(result.reservation());
        }

        if (result.status() == ReservationStatus.NOT_FOUND) {
            return ResponseEntity.status(404)
                .body(message("Student or book not found."));
        }

        if (result.status() == ReservationStatus.LIMIT_REACHED) {
            return ResponseEntity.status(409)
                .body(message("Reservation limit reached. Students can have up to 3 active reservations."));
        }

        return ResponseEntity.status(409)
            .body(message("This book is already reserved and cannot be reserved by another student."));
    }

    @PutMapping("/return/{reservationId}")
    public ResponseEntity<ReservedBook> returnBook(
        @PathVariable Long reservationId,
        @RequestAttribute("studentId") Long studentId
    ) {
        return bookService.returnBook(studentId, reservationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get/reservations/report")
    public List<ReservationReportItem> getReservationsReport() {
        return reservationReportServiceClient.getAllReservations();
    }

    private Map<String, String> message(String message) {
        return Map.of("message", message);
    }
}
