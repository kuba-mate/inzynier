package com.example.inzynier.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "groupTicket")
public class GroupTicket extends Ticket{

    private String levelOfAdvancement;

    @OneToMany(mappedBy = "groupTicket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroupTraining> groupTrainings = new ArrayList<>();

}
