package com.example.demo.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.demo.dtos.ReservationReportItem;

@Service
public class ReservationReportServiceClient {

    private final RestClient restClient;

    public ReservationReportServiceClient(
        @Value("${app.reservation-report-service.url}") String reservationReportServiceUrl
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(reservationReportServiceUrl)
            .build();
    }

    public List<ReservationReportItem> getAllReservations() {
        ReservationReportItem[] reservations = restClient.get()
            .uri("/api/reservations/all")
            .retrieve()
            .body(ReservationReportItem[].class);

        if (reservations == null) {
            return List.of();
        }

        return Arrays.asList(reservations);
    }
}
