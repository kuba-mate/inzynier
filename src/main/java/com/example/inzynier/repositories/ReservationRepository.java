package com.example.inzynier.repositories;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> getReservationsByClientAndNumberOfEntriesLeftNotNullAndNumberOfEntriesLeftNot(final Client client, final Integer value);
    List<Reservation> getReservationsByClientAndNumberOfEntriesLeftEquals(final Client client, final Integer value);
    List<Reservation> getReservationsByClientAndNumberOfEntriesLeftNull(final Client client);

    List<Reservation> getReservationsByClientAndNumberOfEntriesLeftNullAndEndDateIsAfter(final Client client, final LocalDate now);
    Reservation getReservationByClientAndNumberOfEntriesLeftGreaterThan(final Client client, final Integer value);
    int deleteByEndDateBefore(LocalDate date);

}
