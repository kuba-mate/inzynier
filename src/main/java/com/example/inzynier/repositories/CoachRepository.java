package com.example.inzynier.repositories;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.SportDiscipline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachRepository extends JpaRepository<Coach,Long> {
    List<Coach> getCoachesBySportDisciplineIsIn(List<SportDiscipline> sportDisciplines);
    Coach getCoachById(Long coachId);
    Coach getCoachByNameAndLastName(String name, String lastName);
    Coach findByLoginAndPassword(final String login, final String password);
}
