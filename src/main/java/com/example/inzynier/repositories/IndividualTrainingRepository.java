package com.example.inzynier.repositories;

import com.example.inzynier.models.Client;
import com.example.inzynier.models.Coach;
import com.example.inzynier.models.IndividualTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndividualTrainingRepository extends JpaRepository<IndividualTraining,Long> {

    List<IndividualTraining> findAllByCoach(Coach coach);
    List<IndividualTraining> findAllByClient(Client client);
}
