package com.example.inzynier.models.form;

import com.example.inzynier.models.Coach;
import com.example.inzynier.models.enums.SportType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoachForm {
    private Coach coach;
    private SportType sportType;
}
