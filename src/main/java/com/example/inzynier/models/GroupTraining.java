package com.example.inzynier.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "group_training")
public class GroupTraining extends Training{

    private Integer groupSize;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "group_ticket_id", nullable = false)
    private GroupTicket groupTicket;

    public void increaseGroupSizeByOne(){
        this.groupSize++;
    }

}
