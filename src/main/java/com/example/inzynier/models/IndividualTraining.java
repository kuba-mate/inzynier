package com.example.inzynier.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "individual_training")
public class IndividualTraining extends Training {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Transient
    private String clientsGoal;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

}
