package com.example.inzynier.models;

import com.example.inzynier.models.enums.SportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sport_discipline")
public class SportDiscipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sportDisciplineId;

    private String name;
    private String requiredTools;
    private SportType sportType;

    @OneToMany(mappedBy = "sportDiscipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Coach> coaches = new ArrayList<>();

    @OneToMany(mappedBy = "sportDiscipline", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

}
