package com.example.inzynier.models.form;

import com.example.inzynier.models.enums.TicketType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainingMonitorForm {

    private Long id;
    private String date;
    private String startHour;
    private String endHour;
    private String room;
    private TicketType ticketType;

}
