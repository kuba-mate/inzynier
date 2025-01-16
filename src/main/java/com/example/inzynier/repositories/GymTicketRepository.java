package com.example.inzynier.repositories;

import com.example.inzynier.models.GymTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymTicketRepository extends JpaRepository<GymTicket, Long> {
}
