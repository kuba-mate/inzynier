package com.example.inzynier.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "training")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long training_id;

    private String trainingDay;
    private String startHour;
    private String endHour;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

}
