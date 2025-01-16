package com.example.inzynier.models.dto;

import com.example.inzynier.models.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GroupTicketForm {
    private GroupTicket groupTicket;
    private Coach coach;
    private List<GroupTraining> groupTrainings;
    private Room room;

}
