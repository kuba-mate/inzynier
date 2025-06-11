package com.example.inzynier.models.form;

import com.example.inzynier.models.Ticket;
import com.example.inzynier.models.enums.TicketType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketForm {

    private Ticket ticket;
    private TicketType ticketType;
    private boolean hasTrainings;

}
