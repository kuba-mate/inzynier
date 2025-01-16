package com.example.inzynier.models.dto;

import com.example.inzynier.models.Reservation;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ClientsPlansDto {
    private List<Reservation> gymReservations;
    private List<GroupReservationDto> groupReservations;
    private List<Reservation> nonPeriodicReservations;
}
