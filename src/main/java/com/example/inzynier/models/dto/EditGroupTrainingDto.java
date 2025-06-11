package com.example.inzynier.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EditGroupTrainingDto {

    @JsonProperty("day")
    private String day;

    @JsonProperty("startHour")
    private String startHour;

    @JsonProperty("endHour")
    private String endHour;

    @JsonProperty("coaches")
    private Long coaches;

    @JsonProperty("ticket")
    private String ticket;

    @JsonProperty("room")
    private Long room;

}
