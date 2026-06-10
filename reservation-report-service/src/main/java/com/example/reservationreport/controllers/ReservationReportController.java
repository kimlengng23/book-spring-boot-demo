package com.example.reservationreport.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.reservationreport.dtos.ReservationReportItem;
import com.example.reservationreport.services.ReservationReportService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationReportController {

    private final ReservationReportService reservationReportService;

    public ReservationReportController(ReservationReportService reservationReportService) {
        this.reservationReportService = reservationReportService;
    }

    @GetMapping("/all")
    public List<ReservationReportItem> getAllReservations() {
        return reservationReportService.getAllReservations();
    }
}
