package com.example.inzynier.configuration;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.IndividualTraining;
import com.example.inzynier.models.Reservation;
import com.example.inzynier.repositories.ClientRepository;
import com.example.inzynier.repositories.IndividualTrainingRepository;
import com.example.inzynier.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationCleanupScheduler {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private IndividualTrainingRepository individualTrainingRepository;

    public ReservationCleanupScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Scheduler uruchamiany raz w miesiącu, na koniec miesiąca o 23:59.
     */
    @Scheduled(cron = "0 59 23 L * ?")
    public void cleanUpOldReservations() {
        final LocalDate halfYearAgo = LocalDate.now().minusMonths(3);
        int deletedCount = reservationRepository.deleteByEndDateBefore(halfYearAgo);
        System.out.println("Usunięto " + deletedCount + " starych rezerwacji.");
    }

    /**
     * Scheduler uruchamiany codziennie o północy
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateIndividualTrainingEntries() {
        List<Client> clients = clientRepository.findAll();

        for (Client client : clients) {
            final List<IndividualTraining> trainings = individualTrainingRepository.findAllByClient(client);
            final Reservation reservation = reservationRepository.getReservationByClientAndNumberOfEntriesLeftGreaterThan(client, 0);

            for (final IndividualTraining training : trainings) {
                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                final LocalDateTime trainingEndDate = training.getEndDate();
                if (trainingEndDate != null && trainingEndDate.isAfter(yesterday) && trainingEndDate.isBefore(now)) {
                    reservation.decreaseNumberOfEntriesByOne();
                    reservationRepository.save(reservation);
                }
            }
        }
    }

}
