package com.example.inzynier.models.dto;

import com.example.inzynier.models.Client;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MyAccountDto {

    private Client client;
    private ClientsPlansDto currentPlans;
    private ClientsPlansDto historicalPlans;

}
