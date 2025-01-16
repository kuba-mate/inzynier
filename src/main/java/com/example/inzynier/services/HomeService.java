package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.Client;
import com.example.inzynier.models.dto.ClientsPlansDto;
import com.example.inzynier.models.dto.GroupReservationDto;
import com.example.inzynier.models.dto.MyAccountDto;
import com.example.inzynier.repositories.ClientRepository;
import com.example.inzynier.repositories.GroupTicketRepository;
import com.example.inzynier.repositories.GroupTrainingRepository;
import com.example.inzynier.repositories.ReservationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HomeService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private GroupTicketRepository groupTicketRepository;
    @Autowired
    private GroupTrainingRepository groupTrainingRepository;

    public void addNewPersonToDatabase(Client client) {
        clientRepository.save(client);
    }

    public void editProfile(final Client newClientInfo, final HttpServletRequest request) {
        final Client oldClientInfo = (Client) request.getSession().getAttribute("user");
        if (newClientInfo.getLogin() != null && !newClientInfo.getLogin().equals(oldClientInfo.getLogin())) {
            oldClientInfo.setLogin(newClientInfo.getLogin());
        }
        if (newClientInfo.getPassword() != null && !newClientInfo.getPassword().equals(oldClientInfo.getPassword())) {
            oldClientInfo.setPassword(newClientInfo.getPassword());
        }
        if (newClientInfo.getEmail() != null && !newClientInfo.getEmail().equals(oldClientInfo.getEmail())) {
            oldClientInfo.setEmail(newClientInfo.getEmail());
        }
        if (newClientInfo.getPhoneNumber() != null && !newClientInfo.getPhoneNumber().equals(oldClientInfo.getPhoneNumber())) {
            oldClientInfo.setPhoneNumber(newClientInfo.getPhoneNumber());
        }
        if (newClientInfo.getAddress() != null && !newClientInfo.getAddress().equals(oldClientInfo.getAddress())) {
            oldClientInfo.setAddress(newClientInfo.getAddress());
        }
        clientRepository.save(oldClientInfo);
    }

    public MyAccountDto setMyAccount(final HttpServletRequest request){
        final Client client = (Client) request.getSession().getAttribute("user");
        final List<Reservation> activeGymReservations = new ArrayList<>();
        final List<Reservation> oldGymReservations = new ArrayList<>();
        final List<GroupReservationDto> activeGroupReservations = new ArrayList<>();
        final List<GroupReservationDto> oldGroupReservations = new ArrayList<>();
        final List<Reservation> periodicReservations = reservationRepository.getReservationsByClientAndNumberOfEntriesLeftNull(client);
        final List<Reservation> activeNonPeriodicReservations = reservationRepository.getReservationsByClientAndNumberOfEntriesLeftNotNullAndNumberOfEntriesLeftNot(client, 0);
        final List<Reservation> oldNonPeriodicReservations = reservationRepository.getReservationsByClientAndNumberOfEntriesLeftEquals(client, 0);
        periodicReservations.forEach(reservation -> {
            if(getGroupTrainings(reservation.getTicket().getId()) == null){
                if(reservation.getEndDate().isBefore(LocalDate.now())){
                    oldGymReservations.add(reservation);
                } else {
                    activeGymReservations.add(reservation);
                }
            } else {
                List<GroupTraining> groupTrainings = getGroupTrainings(reservation.getTicket().getId());
                final GroupReservationDto groupReservationDto = GroupReservationDto.builder()
                        .ticket(reservation.getTicket())
                        .coach(groupTrainings.get(0).getCoach())
                        .endDate(reservation.getEndDate())
                        .groupTrainings(groupTrainings)
                        .build();
                if(reservation.getEndDate().isBefore(LocalDate.now())){
                    oldGroupReservations.add(groupReservationDto);
                } else {
                    activeGroupReservations.add(groupReservationDto);
                }
            }
        });
        final ClientsPlansDto currentPlans = ClientsPlansDto.builder()
                .gymReservations(activeGymReservations)
                .groupReservations(activeGroupReservations)
                .nonPeriodicReservations(activeNonPeriodicReservations)
                .build();
        final ClientsPlansDto historicalPlans = ClientsPlansDto.builder()
                .gymReservations(oldGymReservations)
                .groupReservations(oldGroupReservations)
                .nonPeriodicReservations(oldNonPeriodicReservations)
                .build();

        return MyAccountDto.builder()
                .client(client)
                .currentPlans(currentPlans)
                .historicalPlans(historicalPlans)
                .build();
    }

    private List<GroupTraining> getGroupTrainings(final Long ticketId){
        final Optional<GroupTicket> optGroupTicket = groupTicketRepository.findById(ticketId);
        if(optGroupTicket.isEmpty()){
            return null;
        }
        final GroupTicket groupTicket = optGroupTicket.get();
        return groupTrainingRepository.getGroupTrainingsByGroupTicket(groupTicket);
    }



}