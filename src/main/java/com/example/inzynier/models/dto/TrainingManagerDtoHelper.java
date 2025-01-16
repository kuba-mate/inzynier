package com.example.inzynier.models.dto;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.SportDiscipline;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class TrainingManagerDtoHelper {
    private List<Coach> coaches;
    private List<SportDiscipline> sportDisciplines;
}
