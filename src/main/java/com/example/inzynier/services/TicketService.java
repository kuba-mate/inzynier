package com.example.inzynier.services;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.Reservation;
import com.example.inzynier.models.Status;
import com.example.inzynier.models.Ticket;
import com.example.inzynier.repositories.ReservationRepository;
import com.example.inzynier.repositories.TicketRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    public List<Ticket> findAllTickets(){
        return ticketRepository.findAll();
    }
    public Ticket getTicketById(final Long id){
        return ticketRepository.getTicketById(id);
    }
    public void saveReservation(final Long ticketId, HttpServletRequest request){
        final Client client = (Client) request.getSession().getAttribute("user");
        Reservation reservation = new Reservation();
        reservation.setStatus(Status.AKTYWNE);
        reservation.setEndDate(LocalDate.now().plusMonths(1));
        reservation.setClient(client);
        reservation.setTicket(ticketRepository.getTicketById(ticketId));
        reservationRepository.save(reservation);
    }

}
