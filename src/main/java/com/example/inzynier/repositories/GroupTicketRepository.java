package com.example.inzynier.repositories;

import com.example.inzynier.models.GroupTicket;
import com.example.inzynier.models.enums.SportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupTicketRepository extends JpaRepository<GroupTicket,Long> {
    List<GroupTicket> findGroupTicketsBySportDiscipline_SportType(final SportType sportType);
    GroupTicket getGroupTicketByName(String name);
}
