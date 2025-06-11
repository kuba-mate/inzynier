package com.example.inzynier.services;

import com.example.inzynier.models.*;
import com.example.inzynier.models.Client;
import com.example.inzynier.models.dto.ClientsPlansDto;
import com.example.inzynier.models.dto.GroupReservationDto;
import com.example.inzynier.models.dto.MyAccountDto;
import com.example.inzynier.models.exception.LoginNotUniqueException;
import com.example.inzynier.models.exception.WrongEmailException;
import com.example.inzynier.models.exception.WrongLoginPasswordException;
import com.example.inzynier.models.exception.WrongPhoneNumberException;
import com.example.inzynier.repositories.*;
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
    @Autowired
    private PersonRepository personRepository;

    public void addNewPersonToDatabase(final Client client) {
        clientRepository.save(client);
    }

    public void editProfile(final Client newClientInfo, final HttpServletRequest request) throws Exception {
        final Client oldClientInfo = (Client) request.getSession().getAttribute("user");
        if (!oldClientInfo.getLogin().equals(newClientInfo.getLogin())) {
            if (newClientInfo.getLogin() == null || newClientInfo.getLogin().length() < 3) {
                throw new WrongLoginPasswordException();
            }
            final List<Person> persons = personRepository.findAll();
            for (final Person person : persons) {
                if (person.getLogin().equals(newClientInfo.getLogin())) {
                    throw new LoginNotUniqueException();
                }
            }
            oldClientInfo.setLogin(newClientInfo.getLogin());
        }
        if (!oldClientInfo.getPassword().equals(newClientInfo.getPassword())) {
            if (newClientInfo.getPassword() == null || newClientInfo.getPassword().length() < 3) {
                throw new WrongLoginPasswordException();
            }
            oldClientInfo.setPassword(newClientInfo.getPassword());
        }
        if (!oldClientInfo.getEmail().equals(newClientInfo.getEmail())) {
            final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            final String email = newClientInfo.getEmail();
            if (email == null || !email.matches(emailRegex)) {
                throw new WrongEmailException();
            }
            oldClientInfo.setEmail(newClientInfo.getEmail());
        }
        if (!oldClientInfo.getPhoneNumber().equals(newClientInfo.getPhoneNumber())) {
            final String phoneNumber = newClientInfo.getPhoneNumber();
            final String phoneRegex = "^\\+?[0-9]{9,15}$";
            if (phoneNumber == null || !phoneNumber.matches(phoneRegex)) {
                throw new WrongPhoneNumberException();
            }
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