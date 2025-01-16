package com.example.inzynier.models.dto;

import lombok.Data;

@Data
public class TrainingDto {
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private Long coachId;
    private Long room;
    private Long ticketId;
}
