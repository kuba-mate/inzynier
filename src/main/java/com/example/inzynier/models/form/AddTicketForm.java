package com.example.inzynier.models.form;

import com.example.inzynier.models.enums.SportType;
import com.example.inzynier.models.enums.TicketType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddTicketForm {
    private String name;
    private Integer price;
    private String description;
    private TicketType type;
    private SportType sportType;
    private Integer entriesCount;
    private String groupLevel;
    private Boolean studentsOnly;
}
