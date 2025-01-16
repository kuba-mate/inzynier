package com.example.inzynier.models.dto;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.GroupTraining;
import com.example.inzynier.models.Ticket;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class GroupReservationDto {

    private Ticket ticket;
    private Coach coach;
    private LocalDate endDate;
    private List<GroupTraining> groupTrainings;

}
