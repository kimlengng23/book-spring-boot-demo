package com.example.reservationreport.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.reservationreport.dtos.ReservationReportItem;
import com.example.reservationreport.repositories.ReservationReportRepository;

@Service
public class ReservationReportService {

    private final ReservationReportRepository reservationReportRepository;

    public ReservationReportService(ReservationReportRepository reservationReportRepository) {
        this.reservationReportRepository = reservationReportRepository;
    }

    public List<ReservationReportItem> getAllReservations() {
        return reservationReportRepository.getAllReservations();
    }
}
