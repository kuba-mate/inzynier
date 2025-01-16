package com.example.inzynier.repositories;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.GroupTicket;
import com.example.inzynier.models.GroupTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupTrainingRepository extends JpaRepository<GroupTraining,Long> {

    List<GroupTraining> getGroupTrainingsByGroupTicket(final GroupTicket groupTicket);
    List<GroupTraining> findAllByCoach(Coach coach);

}
