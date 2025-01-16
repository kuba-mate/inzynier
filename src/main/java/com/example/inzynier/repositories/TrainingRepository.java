package com.example.inzynier.repositories;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> getTrainingsByCoach(Coach coach);

}
