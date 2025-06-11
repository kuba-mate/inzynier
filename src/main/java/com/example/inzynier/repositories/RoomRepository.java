package com.example.inzynier.repositories;

import com.example.inzynier.models.Room;
import com.example.inzynier.models.enums.SportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> getRoomsBySportType(SportType sportType);

    List<Room> getRoomsBySportTypeIsIn(List<SportType> sportTypes);
}
