package com.example.inzynier.models.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GroupTicketNamePriceDto {
    private Long id;
    private String name;
    private int price;
}
