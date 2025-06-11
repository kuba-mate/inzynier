package com.example.inzynier.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EditTicketDto {

    private String name;
    private Integer price;
    private String description;

}
