package com.example.inzynier.repositories;

import com.example.inzynier.models.SportDiscipline;
import com.example.inzynier.models.enums.SportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportDisciplineRepository extends JpaRepository<SportDiscipline,Long> {
    List<SportDiscipline> getSportDisciplinesBySportTypeIsIn(List<SportType> sportTypes);
    SportDiscipline getSportDisciplineBySportType(SportType sportType);
}
