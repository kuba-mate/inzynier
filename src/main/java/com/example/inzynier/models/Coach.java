package com.example.inzynier.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "coach")
public class Coach extends Person{

    private int yearsOfExperience;
    private String scholarships;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "sport_discipline_id", nullable = false)
    private SportDiscipline sportDiscipline;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Training> trainings = new ArrayList<>();

}
