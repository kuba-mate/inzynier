package com.example.inzynier.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "individualTicket")
public class IndividualTicket extends Ticket{

    private Integer numberOfEntries;

}
