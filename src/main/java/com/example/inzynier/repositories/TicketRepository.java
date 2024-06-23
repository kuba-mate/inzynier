package com.example.inzynier.repositories;

import com.example.inzynier.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Ticket getTicketById(Long id);
}
